package io.alapierre.io;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Created by adrian on 2017-12-08.
 */
@Slf4j
public class SystemUtils {

    public static String OS_NAME = getSystemProperty("os.name").orElse("unknown");

    public static boolean isWindows() {

        if (OS_NAME.equals("unknown")) {
            return false;
        }
        return OS_NAME.startsWith("Windows");
    }

    public static Optional<String> getSystemProperty(final String property) {
        try {
            return Optional.ofNullable(System.getProperty(property));
        } catch (final SecurityException ex) {
            // we are not allowed to look at this property
            log.error("Caught a SecurityException reading the system property '" + property
                    + "'; the SystemUtils property value will default to null.");
            return Optional.empty();
        }
    }
}
