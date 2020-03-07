package io.alapierre.freshmail.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

/**
 * @author Adrian Lapierre {@literal <adrian.lapierre@sidgroup.pl>}
 * created 13.06.18
 */
@Slf4j
public class ConfigReader {

    private final Properties properties;
    private boolean readError;

    public ConfigReader(String fileName, boolean failWhenCantLoad) {

        String homeDir = System.getProperty("user.home");

        properties = new Properties();
        try {
            properties.load(new FileReader(new File(homeDir, fileName)));
        } catch (IOException e) {
            log.warn("brak konfiguracji do odczytania " + homeDir + "/" + fileName);
            if(failWhenCantLoad) throw new RuntimeException(e);
            readError = true;
        }
    }

    public Optional<String> getProperty(String name) {
        if(readError) log.warn("konfiguracja nie została załadowana");
        return Optional.ofNullable(properties.getProperty(name));
    }

    public String getProperty(String name, String defaultValue) {
        if(readError) log.warn("konfiguracja nie została załadowana, zwrócona zostanie domyślna wartość");
        return properties.getProperty(name, defaultValue);
    }

}
