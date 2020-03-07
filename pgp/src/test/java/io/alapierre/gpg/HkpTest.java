package io.alapierre.gpg;

import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Adrian Lapierre {@literal <adrian.lapierre@sidgroup.pl>}
 * created 21.06.18
 */
public class HkpTest {

    @Test
    @Ignore
    public void publish() throws Exception {

        String content = new String(Files.readAllBytes(Paths.get("src/test/resources/pub.dat")));

        System.out.println(content);

        Hkp server = new Hkp("keyserver.searchy.nl", 11371);

        //pl.sid.commons.gpg.Hkp server = new pl.sid.commons.gpg.Hkp("localhost", 8080);

        server.publish(content);

    }
}