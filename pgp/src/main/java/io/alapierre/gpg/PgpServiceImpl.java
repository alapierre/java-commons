package io.alapierre.gpg;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.bcpg.sig.Features;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PBESecretKeyEncryptor;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.bc.BcPGPKeyPair;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;

import java.io.*;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Adrian Lapierre {@literal <adrian.lapierre@sidgroup.pl>}
 * created 20.06.18
 */
@Slf4j
@Deprecated
public class PgpServiceImpl {

    private static final BouncyCastleProvider provider = new BouncyCastleProvider();

    static {
        Security.addProvider(provider);
    }

    @Deprecated
    public void generateKeyPair(String id, String passwd, OutputStream privKey, OutputStream pubKey, OutputStream revokeKey) throws NoSuchProviderException, NoSuchAlgorithmException, IOException, PGPException {

        PGPKeyRingGenerator krgen = generateKeyRingGenerator(id, passwd.toCharArray(),4096, 31556952L);

        // Generate public key ring
        PGPPublicKeyRing publicKey = krgen.generatePublicKeyRing();
        toStream(publicKey, pubKey);

        // Generate private key
        PGPSecretKeyRing privateKey = krgen.generateSecretKeyRing();
        ByteArrayOutputStream tmp = toStream(privateKey);

        ByteArrayInputStream in = new ByteArrayInputStream(tmp.toByteArray());
        PGPSecretKey k = readSecretKey(in, passwd.toCharArray());
        generateRevoke(id, k, passwd.toCharArray(), revokeKey);

        privKey.write(tmp.toByteArray());
        tmp.close();
    }

    private void toStream(PGPKeyRing keys, OutputStream out) throws IOException {
        ArmoredOutputStream pubout = new ArmoredOutputStream(new BufferedOutputStream(out));
        keys.encode(pubout);
        pubout.close();
    }

    private ByteArrayOutputStream toStream(PGPKeyRing keys) throws IOException {
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        ArmoredOutputStream pubout = new ArmoredOutputStream(new BufferedOutputStream(tmp));
        keys.encode(pubout);
        pubout.close();
        return tmp;
    }

    public PGPKeyRingGenerator generateKeyRingGenerator(String id, char[] pass, int strenght, long validSeconds) throws PGPException {
        return generateKeyRingGenerator(id, pass, 0xc0, strenght, validSeconds);
    }

    // https://stackoverflow.com/questions/28245669/using-bouncy-castle-to-create-public-pgp-key-usable-by-thunderbird
    // Note: s2kcount is a number between 0 and 0xff that controls the number of times to iterate the password hash before use. More
    // iterations are useful against offline attacks, as it takes more time to check each password. The actual number of iterations is
    // rather complex, and also depends on the hash function in use. Refer to Section 3.7.1.3 in rfc4880.txt. Bigger numbers give
    // you more iterations.  As a rough rule of thumb, when using SHA256 as the hashing function, 0x10 gives you about 64
    // iterations, 0x20 about 128, 0x30 about 256 and so on till 0xf0, or about 1 million iterations. The maximum you can go to is
    // 0xff, or about 2 million iterations.  I'll use 0xc0 as a default -- about 130,000 iterations.
    @SuppressWarnings("Duplicates")
    private final PGPKeyRingGenerator generateKeyRingGenerator(String id, char[] pass, int s2kcount, int strenght, long validSeconds) throws PGPException {
        // This object generates individual key-pairs.
        RSAKeyPairGenerator kpg = new RSAKeyPairGenerator();

        // Boilerplate RSA parameters, no need to change anything
        // except for the RSA key-size (2048). You can use whatever key-size makes sense for you -- 4096, etc.
        kpg.init(new RSAKeyGenerationParameters(BigInteger.valueOf(0x10001), new SecureRandom(), strenght, 12));

        // First create the master (signing) key with the generator.
        PGPKeyPair rsakp_sign = new BcPGPKeyPair(PGPPublicKey.RSA_SIGN, kpg.generateKeyPair(), new Date());
        // Then an encryption subkey.
        PGPKeyPair rsakp_enc = new BcPGPKeyPair(PGPPublicKey.RSA_ENCRYPT, kpg.generateKeyPair(), new Date());

        // Add a self-signature on the id
        PGPSignatureSubpacketGenerator signhashgen = new PGPSignatureSubpacketGenerator();

        signhashgen.setKeyExpirationTime(false, validSeconds);

        // Add signed metadata on the signature.
        // 1) Declare its purpose
        signhashgen.setKeyFlags(false, KeyFlags.SIGN_DATA|KeyFlags.CERTIFY_OTHER);
        // 2) Set preferences for secondary crypto algorithms to use when sending messages to this key.
        signhashgen.setPreferredSymmetricAlgorithms
                (false, new int[] {
                        SymmetricKeyAlgorithmTags.AES_256,
                        SymmetricKeyAlgorithmTags.AES_192,
                        SymmetricKeyAlgorithmTags.AES_128
                });
        signhashgen.setPreferredHashAlgorithms
                (false, new int[] {
                        HashAlgorithmTags.SHA256,
                        HashAlgorithmTags.SHA1,
                        HashAlgorithmTags.SHA384,
                        HashAlgorithmTags.SHA512,
                        HashAlgorithmTags.SHA224,
                });
        // 3) Request senders add additional checksums to the message (useful when verifying unsigned messages.)
        signhashgen.setFeature(false, Features.FEATURE_MODIFICATION_DETECTION);


        // Create a signature on the encryption subkey.
        PGPSignatureSubpacketGenerator enchashgen = new PGPSignatureSubpacketGenerator();
        // Add metadata to declare its purpose
        enchashgen.setKeyFlags(false, KeyFlags.ENCRYPT_COMMS|KeyFlags.ENCRYPT_STORAGE);
        enchashgen.setKeyExpirationTime(false, validSeconds);

        // Objects used to encrypt the secret key.
        PGPDigestCalculator sha1Calc = new BcPGPDigestCalculatorProvider().get(HashAlgorithmTags.SHA1);
        PGPDigestCalculator sha256Calc = new BcPGPDigestCalculatorProvider().get(HashAlgorithmTags.SHA256);

        // bcpg 1.48 exposes this API that includes s2kcount. Earlier versions use a default of 0x60.
        PBESecretKeyEncryptor pske = (new BcPBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_256, sha256Calc, s2kcount)).build(pass);

        // Finally, create the keyring itself. The constructor takes parameters that allow it to generate the self signature.
        PGPKeyRingGenerator keyRingGen =
                new PGPKeyRingGenerator(PGPSignature.POSITIVE_CERTIFICATION, rsakp_sign,
                        id, sha1Calc, signhashgen.generate(), null,
                        new BcPGPContentSignerBuilder(rsakp_sign.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1), pske);

        // Add our encryption subkey, together with its signature.
        keyRingGen.addSubKey(rsakp_enc, enchashgen.generate(), null);
        return keyRingGen;
    }

    public void generateRevoke(String id, PGPPrivateKey pgpPrivKey, PGPPublicKey oldKey, OutputStream out) throws PGPException, IOException {

        PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
                new JcaPGPContentSignerBuilder( oldKey.getAlgorithm(), PGPUtil.SHA1 ) );

        signatureGenerator.init( PGPSignature.CERTIFICATION_REVOCATION, pgpPrivKey );

        PGPSignature signature = signatureGenerator.generateCertification(id, oldKey);

        PGPPublicKey newKey = PGPPublicKey.addCertification(oldKey, id, signature);

        out = new ArmoredOutputStream(out);

        newKey.encode(out);
        out.close();
    }

    public void signPublicKey(PGPSecretKey secretKey, char[] passPhrase, PGPPublicKey keyToBeSigned, OutputStream out) throws PGPException, IOException {

        PGPPrivateKey pgpPrivKey = secretKey.extractPrivateKey(
                new JcePBESecretKeyDecryptorBuilder().setProvider( provider )
                        .build(passPhrase));

        PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
                new JcaPGPContentSignerBuilder( secretKey.getPublicKey().getAlgorithm(), PGPUtil.SHA1 ) );

        signatureGenerator.init( PGPSignature.DEFAULT_CERTIFICATION, pgpPrivKey );

        Iterator<String> ids = keyToBeSigned.getUserIDs();
        if(!ids.hasNext()) throw new IllegalArgumentException("klucz nie posiada żadnego User ID");
        String id = ids.next();

        PGPSignature signature = signatureGenerator.generateCertification(id, keyToBeSigned);
        PGPPublicKey newKey = PGPPublicKey.addCertification(keyToBeSigned, id, signature);

        out = new ArmoredOutputStream(out);

        newKey.encode(out);
        out.close();
    }

    public void generateRevoke(String id, PGPSecretKey secretKey, char[] passPhrase, OutputStream out) throws PGPException, IOException {

        PGPPublicKey oldKey = secretKey.getPublicKey();

        PGPPrivateKey pgpPrivKey = secretKey.extractPrivateKey(
                new JcePBESecretKeyDecryptorBuilder().setProvider( provider )
                        .build(passPhrase));

        PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
                new JcaPGPContentSignerBuilder( secretKey.getPublicKey().getAlgorithm(), PGPUtil.SHA1 ) );

        signatureGenerator.init( PGPSignature.CERTIFICATION_REVOCATION, pgpPrivKey );

        PGPSignature signature = signatureGenerator.generateCertification(id, oldKey);

        PGPPublicKey newKey = PGPPublicKey.addCertification(oldKey, id, signature);

        out = new ArmoredOutputStream(out);

        newKey.encode(out);
        out.close();
    }

    /**
     * Dodaje syngaturę do pierszego user ID klucza publicznego. Tylko pierwsza sygnatura jest dodawana.
     *
     * @param oldKey klucz do którego dodać sygnaturę
     * @param signature sygnaatura
     * @param out wyjście
     * @throws PGPException
     * @throws IOException
     */
    public void appendSignatureToPublicKey(PGPPublicKey oldKey, InputStream signature, OutputStream out) throws PGPException, IOException {

        List<PGPSignature> signs = loadPGPSignatures(signature);

        if(signs.isEmpty()) throw new IllegalArgumentException("Brak certyfikatu");

        Iterator<String> ids = oldKey.getUserIDs();
        if(!ids.hasNext()) throw new IllegalArgumentException("klucz nie posiada żadnego User ID");
        String id = ids.next();

        PGPPublicKey newKey = PGPPublicKey.addCertification(oldKey, id, signs.get(0));

        out = new ArmoredOutputStream(out);

        newKey.encode(out);
        out.close();
    }

    public List<PGPSignature> loadPGPSignatures(InputStream in) throws IOException {

        PGPObjectFactory factory = new PGPObjectFactory(
                PGPUtil.getDecoderStream(in),
                new JcaKeyFingerprintCalculator());

        Object obj;

        List<PGPSignature> res = new ArrayList<>();

        while ((obj = factory.nextObject()) != null) {

            if(obj instanceof PGPSignatureList) {
                PGPSignatureList signatures = (PGPSignatureList) obj;
                signatures.iterator().forEachRemaining(res::add);
            }
        }

        return res;
    }

    public PGPPrivateKey readPrivateKey(final InputStream secretKeyRing, final char[] passPhrase) throws IOException, PGPException {

        PGPPrivateKey key = null;

        final PGPSecretKeyRingCollection keys =
                new PGPSecretKeyRingCollection(
                        PGPUtil.getDecoderStream(secretKeyRing),
                        new JcaKeyFingerprintCalculator());

        Iterator<PGPSecretKeyRing> it = keys.getKeyRings();

        while (key == null && it.hasNext()) {
            PGPSecretKeyRing tmp = it.next();
            Iterator<PGPSecretKey> it2 = tmp.getSecretKeys();
            while (key == null && it2.hasNext()) {
                PGPSecretKey tmp2 = it2.next();
                if (tmp2.isSigningKey()) {
                    key = tmp2.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider(provider)
                            .build(passPhrase));
                }
            }
        }
        return key;
    }

    public PGPSecretKey readSecretKey(final InputStream secretKeyRing, final char[] passPhrase) throws IOException, PGPException {

        PGPSecretKey key = null;

        final PGPSecretKeyRingCollection keys =
                new PGPSecretKeyRingCollection(
                        PGPUtil.getDecoderStream(secretKeyRing),
                        new JcaKeyFingerprintCalculator());

        Iterator<PGPSecretKeyRing> it = keys.getKeyRings();

        while (key == null && it.hasNext()) {
            PGPSecretKeyRing tmp = it.next();
            Iterator<PGPSecretKey> it2 = tmp.getSecretKeys();
            while (key == null && it2.hasNext()) {
                PGPSecretKey tmp2 = it2.next();
                if (tmp2.isSigningKey()) {
                    key = tmp2;
                }
            }
        }
        return key;
    }

    public PGPPublicKey readPublicKey(String keyAsString) throws IOException, PGPException {
        ByteArrayInputStream tmp = new ByteArrayInputStream(keyAsString.getBytes());
        return readPublicKey(tmp);
    }

    public Optional<PGPPublicKey> extractPublicKey(PGPPublicKeyRingCollection pgpPub, Predicate<PGPPublicKey> filter) throws PGPException {

        Optional<PGPPublicKey> key = Optional.empty();

        Iterator<PGPPublicKeyRing> rIt = pgpPub.getKeyRings();

        while (!key.isPresent() && rIt.hasNext()) {
            PGPPublicKeyRing kRing = rIt.next();
            key = extractPublicKey(kRing, filter);
        }

        return key;
    }

    public Optional<PGPPublicKey> extractPublicKey(PGPPublicKeyRing kRing, Predicate<PGPPublicKey> filter) throws PGPException {
        PGPPublicKey key = null;
        Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();
        while (key == null && kIt.hasNext()) {
            PGPPublicKey k = kIt.next();
            if(log.isDebugEnabled()) log.debug("processing public key " +  PGPKeyUtil.fingerPrint(k));
            if(log.isDebugEnabled()) log.debug("public key user ID's" +  keyids(k));
            if (filter.test(k)) {
                key = k;
                if(log.isDebugEnabled()) log.debug("loading public key" +  PGPKeyUtil.fingerPrint(k));
            }
        }
        return Optional.ofNullable(key);
    }

    public PGPPublicKey readPublicKey(InputStream in) throws IOException, PGPException {
        in = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(in);
        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(in, new JcaKeyFingerprintCalculator());
        return extractPublicKey(pgpPub, PGPPublicKey::isMasterKey)
                .orElseThrow(() -> new IllegalArgumentException("Can't find specific by predicate key in key ring."));
    }

    private String keyids(PGPPublicKey key) {
        StringBuilder sb = new StringBuilder();
        key.getUserIDs().forEachRemaining(it-> sb.append(it).append('|'));
        return sb.toString();
    }

    void publishPublicKey(String pk, String host, int port) throws IOException {
        Hkp server = new Hkp(host, port);
        server.publish(pk);
    }

    public Stream<String> userId(PGPPublicKey pk) {
        Iterable<String> iterable = pk::getUserIDs;
        return StreamSupport.stream(iterable.spliterator(), false);
    }

//    public Stream<String> signatures(PGPPublicKey pk) {
//        Iterable<String> iterable = pk::getKeySignatures;
//        return StreamSupport.stream(iterable.spliterator(), false);
//    }



}
