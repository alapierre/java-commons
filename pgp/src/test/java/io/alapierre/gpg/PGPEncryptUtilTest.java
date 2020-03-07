package io.alapierre.gpg;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static io.alapierre.gpg.PGPKeyUtil.extractPublicKey;
import static io.alapierre.gpg.PGPKeyUtil.extractSecretKey;

/**
 * Created 12.12.18 copyright original authors 2018
 *
 * @author Adrian Lapierre {@literal <adrian@soft-project.pl>}
 */
public class PGPEncryptUtilTest {


    @Test
    public void encryptFileTest() throws Exception {

        PGPPublicKey pk = PGPKeyUtil.extractPublicKey(new FileInputStream("src/test/resources/public_from_id_pgp.asc"), PGPPublicKey::isEncryptionKey)
                .orElseThrow(() -> new IllegalArgumentException("Brak klucza publicznego do szyfrowania"));

        FileOutputStream out = new FileOutputStream(createTmpFile("message", ".dat"));

        PGPEncryptUtil.encryptFile(
                out,
                "src/test/resources/document",
                pk,
                true,
                true
        );
    }

    @Test
    public void encrypt() throws Exception {

        PGPPublicKey pk = PGPKeyUtil.extractPublicKey(new FileInputStream("src/test/resources/public_from_id_pgp.asc"), PGPPublicKey::isEncryptionKey)
                .orElseThrow(() -> new IllegalArgumentException("Brak klucza publicznego do szyfrowania"));

        FileOutputStream out = new FileOutputStream(createTmpFile("message", ".dat"));

        PGPEncryptUtil.encrypt(
                out,
                "To jest test".getBytes(),
                pk,
                true);
    }

    @Test
    public void encryptToString() throws Exception {

        PGPPublicKey pk = PGPKeyUtil.extractPublicKey(new FileInputStream("src/test/resources/public_from_id_pgp.asc"), PGPPublicKey::isEncryptionKey)
                .orElseThrow(() -> new IllegalArgumentException("Brak klucza publicznego do szyfrowania"));

        String res = PGPEncryptUtil.encryptToString(
                "To jest test".getBytes(),
                pk);

        System.out.println(res);

    }

    private File createTmpFile(String prefix, String suffix) throws IOException {
        File f = File.createTempFile(prefix, suffix);
        System.out.println(f.getAbsolutePath());
        return f;
    }

    @Test
    public void signAndEncryptFile() throws Exception {

        FileOutputStream out = new FileOutputStream(createTmpFile("message", ".dat"));

        PGPPublicKey pk = PGPKeyUtil.extractPublicKey(new FileInputStream("src/test/resources/public_from_id_pgp.asc"), PGPPublicKey::isEncryptionKey)
                .orElseThrow(() -> new IllegalArgumentException("Brak klucza publicznego do szyfrowania"));

        PGPSecretKey sk = PGPKeyUtil.extractSecretKey(
                new FileInputStream("src/test/resources/id_pgp.asc"),
                PGPSecretKey::isSigningKey
        );

        PGPEncryptUtil.signAndEncryptFile(
                out,
                "src/test/resources/document",
                pk,
                sk,
                "123ewqasd",
                true,
                true
        );
    }

}
