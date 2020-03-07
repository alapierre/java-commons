package pl.sid.commons;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

/**
 * @author Adrian Lapierre {@literal <adrian.lapierre@sidgroup.pl>}
 * created 28.08.18
 */
public class RsaServiceUtilTest {

    @BeforeClass
    public static void init() {
        RsaUtil.init();
    }

    private File createTmpFile(String prefix, String suffix) throws IOException {
        File f = File.createTempFile(prefix, suffix);
        System.out.println(f.getAbsolutePath());
        return f;
    }

    @Test
    public void generateKeyPair() throws Exception {

        KeyPair keyPair = RsaUtil.generateKeyPair(4094);

        RsaUtil.savePem(keyPair.getPrivate(), "alamakota".toCharArray(),
                new FileOutputStream(createTmpFile("test", ".pem")));
    }

    @Test
    public void generateCSR() throws Exception {


        KeyPair keyPair = RsaUtil.generateKeyPair(4094);

        PKCS10CertificationRequest csr = RsaUtil.generateCSR("CN=Adrian Lapierre, OU=Java, O=SID sp. z o.o., C=PL, emailAddress=adrian.lapierre@sidgroup.pl",
                KeyUsageEnum.SIGN_ENCRYPT,
                keyPair);

        RsaUtil.savePem(csr, new FileOutputStream(createTmpFile("csr", ".csr")));

        RsaUtil.savePem(keyPair.getPrivate(), "alamakota".toCharArray(), new FileOutputStream(createTmpFile("id_key", ".pem")));
        // klucza publiczniego nie da się wyświetlić w linux, ale można go zaimportować co XCA
        RsaUtil.savePem(keyPair.getPublic(), new FileOutputStream(createTmpFile("public", ".pem")));
    }

    @Test
    public void testCSRAtryb() throws Exception {

        KeyPair keyPair = RsaUtil.generateKeyPair(2048);

        PKCS10CertificationRequest csr = RsaUtil.generateCSR("CN=Adrian Lapierre, OU=Java, O=SID sp. z o.o., C=PL, emailAddress=adrian.lapierre@sidgroup.pl",
                KeyUsageEnum.SIGN_ENCRYPT,
                keyPair);

        System.out.println(csr.getSubject());

        Arrays.stream(csr.getAttributes()).forEach(it -> {
            System.out.println(it.getAttrType());

            Arrays.stream(it.getAttributeValues()).forEach(ex -> {


                //Extension.getInstance(ex);

                Extensions x = Extensions.getInstance(ex);
                Extension a = x.getExtension(Extension.keyUsage);

                ASN1ObjectIdentifier[] oids = x.getExtensionOIDs();
                Arrays.stream(oids).forEach(qq->{
                    System.out.println(x.getExtension(qq).getExtnValue());
                });
            });
        });
    }

    @Test
    public void loadCert() throws Exception {

        RsaUtil.loadCert(new File("src/test/resources/Requested_Test_Certificate.p7b"));
    }

    @Test
    public void packToPKCS12() throws Exception {

        List<X509CertificateHolder> chain = RsaUtil.loadCert(new File("src/test/resources/Requested_Test_Certificate.p7b"));
        PrivateKey privateKey = RsaUtil.loadPrivateKey(new File("src/test/resources/pk.pem"), "alamakota".toCharArray());

        RsaUtil.packToPKCS12(
                createTmpFile("result", ".p12"),
                "123ewqasd".toCharArray(),
                "123ewqasd".toCharArray(),
                privateKey,
                chain
                );


    }

    @Test
    @Ignore
    public void packToPKCS12a() throws Exception {

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        CertificateFactory fact = CertificateFactory.getInstance("X.509");

        X509Certificate[] chain = new X509Certificate[1];
        //chain[0] = (X509Certificate) fact.generateCertificate(new FileInputStream ("/home/adrian/realizacje/PKO/PKI/signed/root_37.cer"));
        chain[0] = (X509Certificate) fact.generateCertificate(new FileInputStream ("/home/adrian/realizacje/PKO/PKI/signed/Adrian_Lapierre_enc.cer"));

        PrivateKey privateKey = RsaUtil.loadPrivateKey(new File("/home/adrian/realizacje/PKO/PKI/encrypt_key.pem"), "".toCharArray());

        RsaUtil.packToPKCS12(
                createTmpFile("result", ".p12"),
                "123ewqasd".toCharArray(),
                "123ewqasd".toCharArray(),
                privateKey,
                chain
        );


    }

}