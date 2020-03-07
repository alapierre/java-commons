package io.alapierre.freshmail;

import org.junit.Test;

import java.security.MessageDigest;

/**
 * @author Adrian Lapierre {@literal <adrian.lapierre@sidgroup.pl>}
 * created 13.06.18
 */
public class SignTest {

    @Test
    public void test() throws Exception{

        String message = "d8650983912c2c6a1910cdcbf9444684/rest/pinge0c1863ee67eb0570a488473c01d85aca7c9699a";

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] result = md.digest(message.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        System.out.println(sb.toString());
    }
}
