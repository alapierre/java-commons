package io.alapierre.gpg;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;

import java.io.*;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;
import java.util.Iterator;

/**
 * Created 12.12.18 copyright original authors 2018
 *
 * @author Adrian Lapierre {@literal <adrian@soft-project.pl>}
 */
public class PGPEncryptUtil {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void encryptFile(OutputStream out, String fileName,
                                   PGPPublicKey encKey, boolean armor, boolean withIntegrityCheck) throws IOException {

        if (armor) {
            out = new ArmoredOutputStream(out);
        }

        try {
            byte[] bytes = compressFile(fileName, CompressionAlgorithmTags.ZIP);

            PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(
                    new JcePGPDataEncryptorBuilder(PGPEncryptedData.AES_256)
                            .setWithIntegrityPacket(withIntegrityCheck)
                            .setSecureRandom(new SecureRandom()).setProvider("BC"));

            encGen.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(encKey).setProvider("BC"));

            OutputStream cOut = encGen.open(out, bytes.length);

            cOut.write(bytes);
            cOut.close();

            if (armor) {
                out.close();
            }
        } catch (PGPException e) {
            System.err.println(e);
            if (e.getUnderlyingException() != null) {
                e.getUnderlyingException().printStackTrace();
            }
        }
    }

    public static void signAndEncryptFile(
            OutputStream out,
            String fileName,
            PGPPublicKey publicKey,
            PGPSecretKey secretKey,
            String password,
            boolean armor,
            boolean withIntegrityCheck ) throws Exception {

        if (armor) {
            out = new ArmoredOutputStream(out);
        }

        // Initialize encrypted data generator
        PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator(
                new JcePGPDataEncryptorBuilder(PGPEncryptedData.AES_256)
                        .setWithIntegrityPacket(withIntegrityCheck)
                        .setSecureRandom(new SecureRandom()).setProvider("BC"));


        encryptedDataGenerator.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(publicKey).setProvider("BC"));
        OutputStream encryptedOut = encryptedDataGenerator.open(out, new byte[1024]);

        // Initialize compressed data generator
        PGPCompressedDataGenerator compressedDataGenerator = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);
        OutputStream compressedOut = compressedDataGenerator.open(encryptedOut, new byte [1024]);

        // Initialize signature generator
        PGPPrivateKey privateKey = PGPKeyUtil.extractPrivateKey(secretKey, password.toCharArray());

        PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(new JcaPGPContentSignerBuilder(
                secretKey.getPublicKey().getAlgorithm(),
                PGPUtil.SHA1)
                .setProvider("BC"));

        signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, privateKey);

        boolean firstTime = true;
        Iterator it = secretKey.getPublicKey().getUserIDs();
        while (it.hasNext() && firstTime) {
            PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
            spGen.setSignerUserID(false, (String)it.next());
            signatureGenerator.setHashedSubpackets(spGen.generate());
            // Exit the loop after the first iteration
            firstTime = false;
        }
        signatureGenerator.generateOnePassVersion(false).encode(compressedOut);

        // Initialize literal data generator
        PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator();
        OutputStream literalOut = literalDataGenerator.open(
                compressedOut,
                PGPLiteralData.BINARY,
                fileName,
                new Date(),
                new byte [1024] );

        // Main loop - read the "in" stream, compress, encrypt and write to the "out" stream
        FileInputStream in = new FileInputStream(fileName);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            literalOut.write(buf, 0, len);
            signatureGenerator.update(buf, 0, len);
        }

        in.close();
        literalDataGenerator.close();
        // Generate the signature, compress, encrypt and write to the "out" stream
        signatureGenerator.generate().encode(compressedOut);
        compressedDataGenerator.close();
        encryptedDataGenerator.close();
        if (armor) {
            out.close();
        }
    }

    public static void encrypt(OutputStream out, byte[] in, PGPPublicKey encKey, boolean armor)
            throws IOException, PGPException {

        if (armor) {
            out = new ArmoredOutputStream(out);
        }

        byte[] bytes = compress(in, CompressionAlgorithmTags.ZIP);

        PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(
                new JcePGPDataEncryptorBuilder(PGPEncryptedData.AES_256)
                        .setWithIntegrityPacket(true)
                        .setSecureRandom(new SecureRandom()).setProvider("BC"));

        encGen.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(encKey).setProvider("BC"));

        try (OutputStream encOut = encGen.open(out, new byte[1024])) {
            encOut.write(bytes);
        } finally {
            out.close();
        }
    }

    public static String encryptToString(byte[] in, PGPPublicKey encKey) throws IOException, PGPException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        encrypt(out, in, encKey, true);

        return new String(out.toByteArray());
    }

    static byte[] compressFile(String fileName, int algorithm) throws IOException {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(algorithm);
        PGPUtil.writeFileToLiteralData(comData.open(bOut), PGPLiteralData.BINARY, new File(fileName));
        comData.close();
        return bOut.toByteArray();
    }

    private static void writeBytesToLiteralData(OutputStream out, char fileType, String name, byte[] bytes)
            throws IOException {

        PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();
        OutputStream pOut = lData.open(out, fileType, name, bytes.length, new Date());
        pOut.write(bytes);
    }

    static byte[] compress(byte[] bytes, int algorithm) throws IOException {

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(algorithm);
        writeBytesToLiteralData(
                bOut,
                PGPLiteralData.BINARY,
                "",
                bytes);

        comData.close();
        return bOut.toByteArray();
    }

}
