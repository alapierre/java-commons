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
import org.bouncycastle.util.encoders.Hex;

import java.io.*;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Adrian Lapierre {@literal <adrian.lapierre@sidgroup.pl>}
 * created 04.09.18
 */
@Slf4j
public class PGPKeyUtil {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Genereuje praę kluczy oraz odwołany klucz publiczny
     *
     * @param id id użytkownika
     * @param passwd hasło klucza prywatnego
     * @param strenght siła klucza
     * @param validSeconds czas ważności w sekundach
     * @return zestaw kluczy PGP
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws PGPException
     */
    public static PGPKeySet generateKeyPair(String id, String passwd, int strenght, long validSeconds) throws NoSuchProviderException, NoSuchAlgorithmException, IOException, PGPException {

        PGPKeyRingGenerator krgen = generateKeyRingGenerator(id, passwd.toCharArray(),strenght, validSeconds);

        // Generate public key ring
        PGPPublicKeyRing publicKeyRing = krgen.generatePublicKeyRing();

        // Generate private key
        PGPSecretKeyRing privateKeyRing = krgen.generateSecretKeyRing();

        PGPPublicKey publicKey = extractPublicKey(publicKeyRing, PGPPublicKey::isMasterKey)
                .orElseThrow(() -> new IllegalStateException("brak klucza publicznego Master"));

        PGPPublicKey revoke = generateRevoke(id, publicKey, privateKeyRing, passwd.toCharArray());

        return new PGPKeySet(
                publicKeyRing,
                privateKeyRing,
                revoke);
    }

    private static PGPPublicKey generateRevoke(String id, PGPPublicKey oldKey, PGPSecretKeyRing secretKeyRing, char[] passPhrase) throws PGPException {

        PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
                new JcaPGPContentSignerBuilder( oldKey.getAlgorithm(), PGPUtil.SHA256 ) );

        PGPSecretKey secretKey = extractSecretKey(secretKeyRing, PGPSecretKey::isMasterKey)
                .orElseThrow(() -> new IllegalStateException("brak klucza prywatnego Master"));

        PGPPrivateKey pgpPrivateKey = extractPrivateKey(secretKey, passPhrase);

        signatureGenerator.init( PGPSignature.CERTIFICATION_REVOCATION, pgpPrivateKey );

        PGPSignature signature = signatureGenerator.generateCertification(id, oldKey);

        return PGPPublicKey.addCertification(oldKey, id, signature);
    }

    /**
     * Tworzy nowy keyring z podpisanym kluczem publicznym
     *
     * @param secretKeyRing keyring klucza prywatnego
     * @param passPhrase hasło klucza prywatnego
     * @param publicKeyRing keyring klucza publicznego
     * @return nowy keyring z podpisanym kluczem publicznym
     * @throws PGPException
     */
    public static PGPPublicKeyRing signPublicKey(PGPSecretKeyRing secretKeyRing, char[] passPhrase, PGPPublicKeyRing publicKeyRing) throws PGPException {

        PGPPublicKey keyToBeSigned = extractPublicKey(publicKeyRing, PGPPublicKey::isMasterKey)
                .orElseThrow(() -> new IllegalStateException("brak klucza prywatnego Master"));

        PGPPublicKey signedKey = signPublicKey(secretKeyRing, passPhrase, keyToBeSigned);
        //PGPPublicKeyRing newPublicKeyRing = PGPPublicKeyRing.removePublicKey(publicKeyRing, keyToBeSigned);

        return PGPPublicKeyRing.insertPublicKey(publicKeyRing, signedKey);
    }

    /**
     * Do użytku wewnętrznego, zwraca podpisany klucz publiczny
     *
     * @param secretKeyRing keyring klucza prywatnego
     * @param passPhrase hasło klucza prywatnego
     * @param keyToBeSigned klucz publiczny
     * @return podpisany klucz
     * @throws PGPException
     */
    static PGPPublicKey signPublicKey(PGPSecretKeyRing secretKeyRing, char[] passPhrase, PGPPublicKey keyToBeSigned) throws PGPException {

        PGPSecretKey secretKey = extractSecretKey(secretKeyRing, PGPSecretKey::isMasterKey)
                .orElseThrow(() -> new IllegalStateException("brak klucza prywatnego Master"));

        PGPPrivateKey pgpPrivateKey = extractPrivateKey(secretKey, passPhrase);

        PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
                new JcaPGPContentSignerBuilder( secretKey.getPublicKey().getAlgorithm(), PGPUtil.SHA256));

        signatureGenerator.init( PGPSignature.DEFAULT_CERTIFICATION, pgpPrivateKey);

        String id = getFirstUserId(keyToBeSigned);

        PGPSignature signature = signatureGenerator.generateCertification(id, keyToBeSigned);
        return PGPPublicKey.addCertification(keyToBeSigned, id, signature);
    }

    public static void toStream(PGPKeyRing keys, OutputStream out) throws IOException {
        ArmoredOutputStream pubout = new ArmoredOutputStream(new BufferedOutputStream(out));
        keys.encode(pubout);
        pubout.close();
    }

    public static void toStream(PGPPublicKey key, OutputStream out) throws IOException {
        ArmoredOutputStream pubout = new ArmoredOutputStream(new BufferedOutputStream(out));
        key.encode(pubout);
        pubout.close();
    }

    public static String keyToString(PGPKeyRing keyRing ) throws IOException {
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        toStream(keyRing, tmp);
        return new String(tmp.toByteArray());
    }

    public static String keyToString(PGPPublicKey key) throws IOException {
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        ArmoredOutputStream pubout = new ArmoredOutputStream(tmp);
        key.encode(pubout);
        pubout.close();
        return new String(tmp.toByteArray());
    }

    static PGPKeyRingGenerator generateKeyRingGenerator(String id, char[] pass, int strenght, long validSeconds) throws PGPException {
        return generateKeyRingGenerator(id, pass, 0xc0, strenght, validSeconds);
    }

    /**
     * Note: s2kcount is a number between 0 and 0xff that controls the number of times to iterate the password hash before use. More
     * iterations are useful against offline attacks, as it takes more time to check each password. The actual number of iterations is
     * rather complex, and also depends on the hash function in use. Refer to Section 3.7.1.3 in rfc4880.txt. Bigger numbers give
     * you more iterations.  As a rough rule of thumb, when using SHA256 as the hashing function, 0x10 gives you about 64
     * iterations, 0x20 about 128, 0x30 about 256 and so on till 0xf0, or about 1 million iterations. The maximum you can go to is
     * 0xff, or about 2 million iterations.  I'll use 0xc0 as a default -- about 130,000 iterations.
     * https://stackoverflow.com/questions/28245669/using-bouncy-castle-to-create-public-pgp-key-usable-by-thunderbird
     *
     * @param id
     * @param pass
     * @param s2kcount
     * @param strenght
     * @param validSeconds
     * @return
     * @throws PGPException
     */
    @SuppressWarnings("Duplicates")
    private static final PGPKeyRingGenerator generateKeyRingGenerator(String id, char[] pass, int s2kcount, int strenght, long validSeconds) throws PGPException {
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

    @SuppressWarnings("Duplicates")
    public static PGPSecretKey extractSecretKey(final InputStream secretKeyRing, Predicate<PGPSecretKey> filter) throws IOException, PGPException {

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
                if (filter.test(tmp2)) {
                    key = tmp2;
                }
            }
        }
        return key;
    }

    /**
     *
     * @param ring
     * @param filter
     * @return
     */
    public static Optional<PGPSecretKey> extractSecretKey(PGPSecretKeyRing ring, Predicate<PGPSecretKey> filter) {

        Iterator<PGPSecretKey> it = ring.getSecretKeys();

        while (it.hasNext()) {
            PGPSecretKey key = it.next();
            if (filter.test(key)) {
                return Optional.of(key);
            }
        }
        return Optional.empty();
    }

    /**
     * Odszyfrowuje klucz prywatny z podanego secretKey
     * @param secretKey klucz źródłowy
     * @param passPhrase hasło do klucza prywatnego
     * @return klucz prywatny
     * @throws PGPException
     */
    public static PGPPrivateKey extractPrivateKey(PGPSecretKey secretKey, char[] passPhrase) throws PGPException {
        return secretKey.extractPrivateKey(
                new JcePBESecretKeyDecryptorBuilder().setProvider("BC")
                        .build(passPhrase));
    }

    /**
     * Zwraca klucz publiczny określonego typu
     *
     * @param kRing źródłowy public key ring
     * @param filter filtr
     * @return znaleziony klucz lub Optional.empty()
     * @throws PGPException w przypadku błędu biblioteki PGP
     */
    public static Optional<PGPPublicKey> extractPublicKey(PGPPublicKeyRing kRing, Predicate<PGPPublicKey> filter) throws PGPException {
        PGPPublicKey key = null;
        Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();
        while (key == null && kIt.hasNext()) {
            PGPPublicKey k = kIt.next();
            if(log.isDebugEnabled()) log.debug("processing public key " +  fingerPrint(k));
            if(log.isDebugEnabled()) log.debug("public key user ID's: " +  keyIds(k));
            if (filter.test(k)) {
                key = k;
                if(log.isDebugEnabled()) log.debug("returning public key " +  fingerPrint(k));
            }
        }
        return Optional.ofNullable(key);
    }

    public static Optional<PGPPublicKey> extractPublicKey(InputStream input, Predicate<PGPPublicKey> filter ) throws IOException, PGPException {

        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(
                PGPUtil.getDecoderStream(input), new JcaKeyFingerprintCalculator());

        Iterator keyRingIter = pgpPub.getKeyRings();
        while (keyRingIter.hasNext()) {
            PGPPublicKeyRing keyRing = (PGPPublicKeyRing) keyRingIter.next();
            Optional<PGPPublicKey> res = extractPublicKey(keyRing, filter);
            if(res.isPresent()) return res;
        }

        return Optional.empty();
    }

    public static String fingerPrint(PGPPublicKey pk) throws PGPException {

        JcaKeyFingerprintCalculator calculator = new JcaKeyFingerprintCalculator();
        byte[] r = calculator.calculateFingerprint(pk.getPublicKeyPacket());

        String hexFingerPrint = Hex.toHexString(r);
        return String.join( " ", hexFingerPrint.toUpperCase().split("(?<=\\G.{4})"));
    }

//    public static Stream<String> signatures(PGPPublicKey pk) {
//        Iterable<String> iterable = pk::getKeySignatures;
//        return StreamSupport.stream(iterable.spliterator(), false);
//    }

    public static Stream<String> keyidsAsStream(PGPPublicKey key) {
        Iterable<String> iterable = key::getUserIDs;
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    private static String keyIds(PGPPublicKey key) {
        StringBuilder sb = new StringBuilder();
        key.getUserIDs().forEachRemaining(it-> sb.append(it).append('|'));
        return sb.toString();
    }

    public static String getFirstUserId(PGPPublicKey publicKey) {
        return keyidsAsStream(publicKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("klucz nie posiada żadnego User ID"));
    }

    public static PGPSecretKeyRing readFirstSecretKeyRing(final InputStream secretKeyRing) throws IOException, PGPException {

        final PGPSecretKeyRingCollection keys =
                new PGPSecretKeyRingCollection(
                        PGPUtil.getDecoderStream(secretKeyRing),
                        new JcaKeyFingerprintCalculator());

        Iterator<PGPSecretKeyRing> it = keys.getKeyRings();

        if (it.hasNext()) {
            return it.next();
        } else {
            throw new IllegalArgumentException("Brak SecretKeyRing");
        }
    }

    public static PGPPublicKeyRing readFirstPublicKeyRing(InputStream in) throws IOException, PGPException {

        in = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(in);
        PGPPublicKeyRingCollection keys = new PGPPublicKeyRingCollection(in, new JcaKeyFingerprintCalculator());

        Iterator<PGPPublicKeyRing> it = keys.getKeyRings();

        if (it.hasNext()) {
            return it.next();
        } else {
            throw new IllegalArgumentException("Brak SecretKeyRing");
        }
    }

    public static PGPPublicKeyRing readFirstPublicKeyRing(String keyAsString) throws IOException, PGPException {
        ByteArrayInputStream tmp = new ByteArrayInputStream(keyAsString.getBytes());
        return readFirstPublicKeyRing(tmp);
    }
}
