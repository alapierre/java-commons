package io.alapierre.gpg;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.sig.NotationData;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.security.Security;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Adrian Lapierre {@literal <adrian.lapierre@sidgroup.pl>}
 * created 20.06.18
 */
public class PgpServiceImplTest {

    @Test
    //@Ignore
    public void testKeyProps() throws Exception{

        PgpServiceImpl pgp = new PgpServiceImpl();

        InputStream in = new FileInputStream("src/test/resources/pub.dat");
        in = PGPUtil.getDecoderStream(in);

        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(in, new JcaKeyFingerprintCalculator());

        PGPPublicKey key = null;

        //
        // iterate through the key rings.
        //
        Iterator<PGPPublicKeyRing> rIt = pgpPub.getKeyRings();

        while (key == null && rIt.hasNext()) {
            PGPPublicKeyRing kRing = rIt.next();
            Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();
            while (key == null && kIt.hasNext()) {
                PGPPublicKey k = kIt.next();
                k.getUserIDs().forEachRemaining(System.out::println);
                System.out.println(k.isMasterKey());
            }
        }
    }

    @Test
    public void testToSec() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime toDateTime = now.plus(1, ChronoUnit.YEARS);
        long seconds = now.until( toDateTime, ChronoUnit.SECONDS);
        System.out.println(seconds);
    }

    @Test
    @Ignore
    public void generateKeyPair() throws Exception {

        PgpServiceImpl pgp = new PgpServiceImpl();
        pgp.generateKeyPair("Test (test) <test@sample.com>", "alamakota",
                new FileOutputStream("/tmp/secret.dat"),
                new FileOutputStream("/tmp/pub.dat"),
                new FileOutputStream("/tmp/revoke.dat"));
    }

    @Test
    @Ignore
    public void generateKeyPairToString() throws Exception {

        ByteArrayOutputStream pub = new ByteArrayOutputStream();
        ByteArrayOutputStream revoke = new ByteArrayOutputStream();

        PgpServiceImpl pgp = new PgpServiceImpl();
        pgp.generateKeyPair("Adrian (test) <al@p-c.pl>", "alamakota",
                new FileOutputStream("/tmp/secret.dat"),
                pub,
                revoke);

        String pubStr = new String(pub.toByteArray());

        System.out.println(pubStr);

    }

    @Test
    @Ignore
    public void readPrivateKey() throws Exception {

        FileInputStream in = new FileInputStream("/tmp/secret.dat");

        PgpServiceImpl pgp = new PgpServiceImpl();

        PGPPrivateKey key = pgp.readPrivateKey(in, "alamakota".toCharArray());

        System.out.println(key.getKeyID());

    }

    @Test
    @Ignore
    public void testSignPublicKeyFromNET() throws Exception {

        Security.addProvider(new BouncyCastleProvider());

        PGPSecretKeyRing secRing = new PGPSecretKeyRing(PGPUtil.getDecoderStream(
                new FileInputStream("src/test/resources/secret.dat")),
                new JcaKeyFingerprintCalculator());

        PGPPublicKeyRing ring = new PGPPublicKeyRing(PGPUtil.getDecoderStream(
                new FileInputStream("src/test/resources/pub_other.dat")),
                new JcaKeyFingerprintCalculator());

        PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
                new JcaPGPContentSignerBuilder(secRing.getPublicKey().getAlgorithm(), PGPUtil.SHA1));

        PGPPrivateKey pgpPrivKey = secRing.getSecretKey()
                .extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("BC")
                        .build("alamakota".toCharArray()));

        signatureGenerator.init(PGPSignature.DEFAULT_CERTIFICATION, pgpPrivKey);
        //PgpSignature.CasualCertification

        signatureGenerator.generateOnePassVersion(false);

        PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
        //spGen.setSignerUserID(false, "s4time <timesheet@sidgroup.pl>");
        PGPSignatureSubpacketVector packetVector = spGen.generate();
        spGen.setExportable(false, true);
        signatureGenerator.setHashedSubpackets(packetVector);

        Iterator<byte[]> ids = ring.getPublicKey().getRawUserIDs();
        PGPPublicKey res = PGPPublicKey.addCertification(ring.getPublicKey(), ids.next(), signatureGenerator.generate());
        saveKey(res);
    }

    private OutputStream createTempOutputStream(String prefix, String suffix) throws IOException {
        return new FileOutputStream(createTmpFile(prefix, suffix));
    }

    private File createTmpFile(String prefix, String suffix) throws IOException {
        File file = File.createTempFile(prefix, suffix);
        System.out.println(file);
        return file;
    }

    private void saveKey(PGPPublicKey res) throws Exception {

        FileOutputStream buff = new FileOutputStream(createTmpFile("nowy", ".asc"));
        OutputStream out = new ArmoredOutputStream(buff);

        res.encode(out);
        out.close();
    }

    private void printKey(PGPPublicKey res) throws Exception {
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        OutputStream out = new ArmoredOutputStream(buff);

        res.encode(out);
        out.close();

        System.out.println(new String(buff.toByteArray()));
    }

    @Test
    //@Ignore
    public void signPublicKey() throws Exception {

        PgpServiceImpl pgp = new PgpServiceImpl();

        FileInputStream in = new FileInputStream("src/test/resources/id_pgp.asc");
        PGPSecretKey mySecretKey = pgp.readSecretKey(in, "alamakota".toCharArray());

        PGPPublicKey publicKeyToBeSigned = pgp.readPublicKey(new FileInputStream("src/test/resources/pub.dat"));

        FileOutputStream out = new FileOutputStream("SignedKey.asc");

        pgp.signPublicKey(mySecretKey, "123ewqasd".toCharArray(), publicKeyToBeSigned,  createTempOutputStream("SignedKey", ".asc"));
    }

    @Test
    @Ignore
    public void signKeyT() throws Exception {

        PgpServiceImpl pgp = new PgpServiceImpl();

        FileInputStream in = new FileInputStream("src/test/resources/secret.dat");
        PGPSecretKey mySecretKey = pgp.readSecretKey(in, "alamakota".toCharArray());

        PGPPublicKey publicKeyToBeSigned = pgp.readPublicKey(new FileInputStream("src/test/resources/pub_other.dat"));

        PGPPrivateKey pgpPrivKey = mySecretKey
                .extractPrivateKey(new JcePBESecretKeyDecryptorBuilder()
                        .setProvider("BC").build("alamakota".toCharArray()));

        //JcaPGPContentSignerBuilder
        //BcPGPContentSignerBuilder

        PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
                new BcPGPContentSignerBuilder(mySecretKey.getPublicKey().getAlgorithm(), PGPUtil.SHA1));

        signatureGenerator.init(PGPSignature.DEFAULT_CERTIFICATION, pgpPrivKey);

        signatureGenerator.generateOnePassVersion(false);

        PGPSignatureSubpacketGenerator g = new PGPSignatureSubpacketGenerator();
        //g.setTrust(false, 120, 120);

        signatureGenerator.setHashedSubpackets(g.generate());
        //signatureGenerator.g

        PGPUserAttributeSubpacketVectorGenerator g1;

        PGPSignature signature = signatureGenerator.generateCertification(mySecretKey.getPublicKey(), publicKeyToBeSigned);

        Iterator<byte[]> ids = publicKeyToBeSigned.getRawUserIDs();

        PGPPublicKey res = PGPPublicKey.addCertification(publicKeyToBeSigned, ids.next(), signature);

        saveKey(res);
    }

    private void valid(PGPPublicKey nKey, PGPPublicKey publicKeyToBeSigned) throws Exception {

        nKey.getSignatures().forEachRemaining(it -> {
            try {
                PGPSignature s = (PGPSignature) it;
                s.init(new BcPGPContentVerifierBuilderProvider(), publicKeyToBeSigned);
                System.out.println(s.getCreationTime());



            } catch (PGPException e) {
                e.printStackTrace();
            }
        });

        Iterator it = nKey.getUserAttributes();
        int count = 0;
        while (it.hasNext())
        {
            PGPUserAttributeSubpacketVector attributes = (PGPUserAttributeSubpacketVector)it.next();

            Iterator    sigs = nKey.getSignatures();
            int sigCount = 0;
            while (sigs.hasNext())
            {
                PGPSignature s = (PGPSignature)sigs.next();

                s.init(new BcPGPContentVerifierBuilderProvider(), publicKeyToBeSigned);

                if (!s.verifyCertification(attributes, publicKeyToBeSigned))
                {
                    System.out.println("added signature failed verification");
                } else {
                    System.out.println(s.getCreationTime() + " OK");
                }

                sigCount++;
            }

            if (sigCount != 1)
            {
                System.out.println("Failed added user attributes signature check");
            }
            count++;
        }

        if (count != 1)
        {
            System.out.println("didn't find added user attributes");
        }


        count = 0;
        for (it = nKey.getUserAttributes(); it.hasNext();)
        {
            count++;
        }
        if (count != 0)
        {
            System.out.println("found attributes where none expected");
        }
    }

    @Test
    @Ignore
    public void generateRevoke() throws Exception {

        PgpServiceImpl pgp = new PgpServiceImpl();

        FileInputStream in = new FileInputStream("/tmp/secret.dat");
        PGPSecretKey pgpSecretKey = pgp.readSecretKey(in, "alamakota".toCharArray());

        //ByteArrayOutputStream res = new ByteArrayOutputStream();
        FileOutputStream res = new FileOutputStream("/tmp/rcert.dat");
        pgp.generateRevoke("Adrian (test) <al@p-c.pl>", pgpSecretKey, "alamakota".toCharArray(), res);

        //System.out.println(new String(res.toByteArray()));
    }

    @Test
    public void fingerPrint() throws Exception {

        FileInputStream in = new FileInputStream("src/test/resources/pub.dat");
        PgpServiceImpl pgp = new PgpServiceImpl();

        PGPPublicKey pk = pgp.readPublicKey(in);

        JcaKeyFingerprintCalculator calculator = new JcaKeyFingerprintCalculator();
        byte[] r = calculator.calculateFingerprint(pk.getPublicKeyPacket());
        String hexFingerPrint = Hex.toHexString(r);

        System.out.println(String.join( " ", hexFingerPrint.toUpperCase().split("(?<=\\G.{4})")));
    }

    @Test
    public void keyId() throws Exception {

        FileInputStream in = new FileInputStream("src/test/resources/pub.dat");
        PgpServiceImpl pgp = new PgpServiceImpl();

        PGPPublicKey pk = pgp.readPublicKey(in);


        pk.getUserIDs().forEachRemaining(System.out::println);

    }

    @Test
    @Ignore
    public void listSigns() throws Exception {

        Security.addProvider(new BouncyCastleProvider());

        PGPPublicKeyRing ring = new PGPPublicKeyRing(PGPUtil.getDecoderStream(
                new FileInputStream("src/test/resources/pub_other_signed.dat")),
                new JcaKeyFingerprintCalculator());
        PGPPublicKey key = ring.getPublicKey();

        // iterate through all direct key signautures and look for NotationData subpackets
        Iterator iter = key.getSignaturesOfType(PGPSignature.DIRECT_KEY);
        while(iter.hasNext())
        {
            PGPSignature    sig = (PGPSignature)iter.next();

            System.out.println("Signature date is: " + sig.getHashedSubPackets().getSignatureCreationTime());

            NotationData[] data = sig.getHashedSubPackets().getNotationDataOccurrences();//.getSubpacket(SignatureSubpacketTags.NOTATION_DATA);

            System.out.println(data.length);

            for (int i = 0; i < data.length; i++)
            {
                System.out.println("Found Notaion named '"+data[i].getNotationName()+"' with content '"+data[i].getNotationValue()+"'.");
            }
        }
    }

    @Test
    public void testLoadSig() throws Exception {

        //PGPUtil.getDecoderStream()

        PGPObjectFactory factory = new PGPObjectFactory(PGPUtil.getDecoderStream(
                new FileInputStream("src/test/resources/revoke_cert.asc")),
                new JcaKeyFingerprintCalculator());

        Object obj;

        List<PGPSignature> res = new ArrayList<>();

        while ((obj = factory.nextObject()) != null) {

            if(obj instanceof PGPSignatureList) {
                PGPSignatureList signatures = (PGPSignatureList) obj;
                signatures.iterator().forEachRemaining(res::add);
            }
        }

        res.forEach(System.out::println);


    }

}