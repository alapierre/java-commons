package io.alapierre.rsa;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8EncryptorBuilder;
import org.bouncycastle.operator.*;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Adrian Lapierre {@literal <adrian.lapierre@sidgroup.pl>}
 * created 27.08.18
 */
@Slf4j
public class RsaUtil {

    public static void init() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public static KeyPair generateKeyPair(int keySize) throws NoSuchProviderException, NoSuchAlgorithmException {

        KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA", "BC");
        kpGen.initialize(keySize, new SecureRandom());

        KeyPair pair = kpGen.generateKeyPair();
        return pair;

    }

    public static PKCS10CertificationRequest generateCSR(String subject, KeyUsageEnum usage, KeyPair keyPair) throws OperatorCreationException, IOException {

        PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(
                new X500Principal(subject),
                keyPair.getPublic());

        JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA256withRSA");

        ContentSigner signer = csBuilder.build(keyPair.getPrivate());

        ExtensionsGenerator extGen = new ExtensionsGenerator();
        extGen.addExtension(Extension.keyUsage, false, usage.getKeyUsage());

        p10Builder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, extGen.generate());

        PKCS10CertificationRequest csr = p10Builder.build(signer);
        return csr;
    }

    public static void savePem(PKCS10CertificationRequest csr, OutputStream out) throws IOException {
        JcaPEMWriter pemWrt = new JcaPEMWriter(new OutputStreamWriter(out));
        pemWrt.writeObject(csr);
        pemWrt.close();
    }

    public static void savePem(PrivateKey privateKey, char[] passwd, OutputStream out) throws IOException, OperatorCreationException {

        JcaPEMWriter pemWrt = new JcaPEMWriter(new OutputStreamWriter(out));
        JceOpenSSLPKCS8EncryptorBuilder encryptorBuilder = new JceOpenSSLPKCS8EncryptorBuilder(PKCS8Generator.PBE_SHA1_3DES);
        encryptorBuilder.setPasssword(passwd);

        JcaPKCS8Generator gen = new JcaPKCS8Generator(privateKey,encryptorBuilder.build());
        pemWrt.writeObject(gen.generate());
        pemWrt.close();
    }

    public static void savePem(PublicKey publicKey, OutputStream out) throws IOException {
        JcaPEMWriter pemWrt = new JcaPEMWriter(new OutputStreamWriter(out));
        pemWrt.writeObject(publicKey);
        pemWrt.close();
    }

    public static void packToPKCS12(File outFile, char[] pkPass, char[] keestorePass, PrivateKey privateKey, List<X509CertificateHolder> certChain) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {

        KeyStore outStore = KeyStore.getInstance("PKCS12");

        outStore.load(null, keestorePass);

        outStore.setKeyEntry("mykey", privateKey, pkPass, convertToX509Certificates(certChain));
        OutputStream outputStream = new FileOutputStream(outFile);
        outStore.store(outputStream, keestorePass);
        outputStream.flush();
        outputStream.close();

    }

    public static void packToPKCS12(File outFile, char[] pkPass, char[] keestorePass, PrivateKey privateKey, X509Certificate[] certChain) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {

        KeyStore outStore = KeyStore.getInstance("PKCS12");

        outStore.load(null, keestorePass);

        outStore.setKeyEntry("mykey", privateKey, pkPass, certChain);
        OutputStream outputStream = new FileOutputStream(outFile);
        outStore.store(outputStream, keestorePass);
        outputStream.flush();
        outputStream.close();

    }

    private static X509Certificate[] convertToX509Certificates(List<X509CertificateHolder> certChain) {
        JcaX509CertificateConverter converter = new JcaX509CertificateConverter();

        return certChain.stream().map(it -> {
            try {
                return converter.getCertificate(it);
            } catch (CertificateException e) {
                log.warn("problem converting certificate " + e.getMessage());
                return null;
            }
        }).filter(Objects::nonNull)
                .collect(Collectors.toList()).toArray(new X509Certificate[0]);
    }

    public static List<X509CertificateHolder> loadCert(File p7bFile) throws CMSException, FileNotFoundException {
        CMSSignedData signature = new CMSSignedData(new FileInputStream(p7bFile));
        return new ArrayList<>(signature.getCertificates().getMatches(null));
    }

    public static PrivateKey loadPrivateKey(File file, char[] password) throws IOException, PKCSException {

        PEMParser pp = new PEMParser(new BufferedReader(new FileReader(file)));

        Object object = pp.readObject();

        PrivateKey privateKey;

        InputDecryptorProvider i = new JcePKCSPBEInputDecryptorProviderBuilder().build(password);

        if (object instanceof PKCS8EncryptedPrivateKeyInfo) {
            PrivateKeyInfo key = ((PKCS8EncryptedPrivateKeyInfo) object).decryptPrivateKeyInfo(i);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            privateKey = converter.getPrivateKey(key);
        } else {
            throw new RuntimeException("Problem z ładowaniem klucza, niewłaściwa klasa " + object.getClass());
        }

        pp.close();
        return privateKey;
    }

    private static X509Certificate createCertificate(PKCS10CertificationRequest request, PublicKey tobeSigned, X509Certificate caCert, PrivateKey caKey, Date from, Date to) throws IOException, OperatorCreationException, CertificateException {
        return createCertificate(request.getSubject(), tobeSigned, extractExtensions(request), caCert, caKey, from, to);
    }

    private static X509Certificate createCertificate(String subject, PublicKey tobeSigned, Extension[] extensions, X509Certificate caCert, PrivateKey caKey, Date from, Date to) throws IOException, OperatorCreationException, CertificateException {
        return createCertificate(new X500Name(subject), tobeSigned, extensions, caCert, caKey, from, to);

    }

    public static X509Certificate createCertificate(X500Name subject, PublicKey tobeSigned, Extension[] extensions, X509Certificate caCert, PrivateKey caKey, Date from, Date to) throws IOException, OperatorCreationException, CertificateException {

        // nie testowane

        SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(tobeSigned.getEncoded());
        AsymmetricKeyParameter privateKeyAsymKeyParam = PrivateKeyFactory.createKey(caKey.getEncoded());

        X509v3CertificateBuilder certGenerator = new X509v3CertificateBuilder(
                new X500Name(caCert.getIssuerX500Principal().getName()),
                new BigInteger(64, new SecureRandom()),
                from,
                to,
                subject,
                subPubKeyInfo
        );

        for(Extension extension : extensions) {
            certGenerator.addExtension(extension);
        }

        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256withRSA");
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);

        ContentSigner sigGen = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(privateKeyAsymKeyParam);
        X509CertificateHolder certificateHolder = certGenerator.build(sigGen);

        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(certificateHolder);
    }

    public static Extension[] extractExtensions(PKCS10CertificationRequest csr) {

        List<Extension> res = new ArrayList<>();

        for(Attribute attribute : csr.getAttributes()) {
            // TODO: należy sprawdzić czy jest odpowiedniego typu - PKCSObjectIdentifiers.pkcs_9_at_extensionRequest
            for(ASN1Encodable value : attribute.getAttributeValues()) {
                Extensions extensions = Extensions.getInstance(value);
                for(ASN1ObjectIdentifier oid : extensions.getExtensionOIDs()) {
                    res.add(extensions.getExtension(oid));
                }
            }
        }
        return res.toArray(new Extension[0]);
    }


}
