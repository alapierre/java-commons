package io.alapierre.gpg;

import org.bouncycastle.openpgp.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Adrian Lapierre {@literal <adrian.lapierre@sidgroup.pl>}
 * created 05.09.18
 */
public class PGPKeyUtilTest {

    @Test
    @Ignore
    public void generateKeyPairToFile() throws Exception {

        PGPKeySet res = PGPKeyUtil.generateKeyPair(
                "New Test (test) <test@example.com>", "alamakota", 4096, 31556952L);

        PGPKeyUtil.toStream(
                res.getPublicKey(),
                new FileOutputStream(createTmpFile("public", ".asc")));

        PGPKeyUtil.toStream(
                res.getSecretKey(),
                new FileOutputStream(createTmpFile("private", ".asc")));

        PGPKeyUtil.toStream(
                res.getRevoke(),
                new FileOutputStream(createTmpFile("revoke", ".asc")));

    }

    @Test
    @Ignore
    public void extractPrivateKey() throws Exception {
        PGPSecretKeyRing systemKey = PGPKeyUtil.readFirstSecretKeyRing(new FileInputStream("/media/adrian/safe/SID/s4t_secret.dat"));

        PGPSecretKey secretKey = PGPKeyUtil.extractSecretKey(systemKey, PGPSecretKey::isMasterKey)
                .orElseThrow(() -> new IllegalStateException("brak klucza prywatnego Master"));

        PGPPrivateKey res = PGPKeyUtil.extractPrivateKey(secretKey, "".toCharArray());
    }

    @Test
    //@Ignore
    public void generateKeyPairWithSignToFile() throws Exception {

        PGPKeySet res = PGPKeyUtil.generateKeyPair(
                "New Test (test) <test@example.com>", "alamakota", 4096, 31556952L);

        PGPSecretKeyRing systemKey = PGPKeyUtil.readFirstSecretKeyRing(new FileInputStream("src/test/resources/id_pgp.asc"));

        PGPPublicKeyRing signedPublicKeyRing = PGPKeyUtil.signPublicKey(
                systemKey,
                "123ewqasd".toCharArray(),
                res.getPublicKey());

        PGPKeyUtil.toStream(
                signedPublicKeyRing,
                new FileOutputStream(createTmpFile("public", ".asc")));

        PGPKeyUtil.toStream(
                res.getSecretKey(),
                new FileOutputStream(createTmpFile("private", ".asc")));

        PGPKeyUtil.toStream(
                res.getRevoke(),
                new FileOutputStream(createTmpFile("revoke", ".asc")));

    }

    @Test
    public void generateKeyPair() throws Exception {

        long validSeconds = 31556952L;

        PGPKeySet res = PGPKeyUtil.generateKeyPair(
                "New Test (test) <test@example.com>", "alamakota",4096, validSeconds);

        //assertThat(res.getRevoke().hasRevocation(), is(true));

        Iterator<PGPSecretKey> keys = res.getSecretKey().getSecretKeys();

        PGPSecretKey secretKey1 = keys.next();
        PGPSecretKey secretKey2 = keys.next();

        assertThat(secretKey1.isSigningKey(), is(true));
        assertThat(secretKey1.isMasterKey(), is(true));

        assertThat(secretKey2.isSigningKey(), is(false));
        assertThat(secretKey2.isMasterKey(), is(false));

        res.getPublicKey().getPublicKeys().forEachRemaining(
                pk -> assertThat(pk.getValidSeconds(), is(validSeconds)));

    }

    private File createTmpFile(String prefix, String suffix) throws IOException {
        File f = File.createTempFile(prefix, suffix);
        System.out.println(f.getAbsolutePath());
        return f;
    }

}
