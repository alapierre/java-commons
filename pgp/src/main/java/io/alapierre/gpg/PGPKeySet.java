package io.alapierre.gpg;

import lombok.Value;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;

/**
 * @author Adrian Lapierre {@literal <adrian.lapierre@sidgroup.pl>}
 * created 04.09.18
 */
@Value
public class PGPKeySet {

    PGPPublicKeyRing publicKey;
    PGPSecretKeyRing secretKey;
    PGPPublicKey revoke;

}
