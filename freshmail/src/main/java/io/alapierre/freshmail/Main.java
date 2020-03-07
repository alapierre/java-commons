package io.alapierre.freshmail;

import io.alapierre.freshmail.util.ConfigReader;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Adrian Lapierre {@literal <adrian.lapierre@sidgroup.pl>}
 * created 12.06.18
 */
public class Main {

    // Wymaga aby w katalogu domowym był plik konfiguracjyny freshmail.properties

    public static void main(String[] args) {

        String url = "https://api.freshmail.com";
        //String url = "http://localhost:8080";

        ConfigReader config = new ConfigReader("freshmail.properties", true);

        FreshMailClient freshMail = new FreshMailClient(
                config.getProperty("api-key").get(),
                config.getProperty("api-secret").get(),
                url);

        freshMail.ping();

        Map<String, String> map = new HashMap<>();
        map.put("ala", "kot");
        map.put("ola", "pies");

        freshMail.addSubscriber(
                config.getProperty("list").get(),
                "Adrian Lapierre",
                "adrian@soft-project.pl");



    }
}
