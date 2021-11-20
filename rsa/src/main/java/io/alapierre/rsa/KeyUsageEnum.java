package io.alapierre.rsa;

import org.bouncycastle.asn1.x509.KeyUsage;

/**
 * @author Adrian Lapierre {@literal <adrian.lapierre@sidgroup.pl>}
 * created 11.09.18
 */
public enum KeyUsageEnum {

    SIGN(new KeyUsage(KeyUsage.nonRepudiation | KeyUsage.digitalSignature)),
    ENCYPR(new KeyUsage(KeyUsage.dataEncipherment)),
    SIGN_ENCRYPT(new KeyUsage(KeyUsage.nonRepudiation | KeyUsage.digitalSignature | KeyUsage.dataEncipherment));

    KeyUsageEnum(KeyUsage keyUsage) {
        this.keyUsage = keyUsage;
    }

    private KeyUsage keyUsage;

    public KeyUsage getKeyUsage() {
        return keyUsage;
    }
}
