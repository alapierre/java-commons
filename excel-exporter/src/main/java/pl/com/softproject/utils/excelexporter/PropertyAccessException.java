/*
 * Copyright 2011-03-30 the original author or authors.
 */

package pl.com.softproject.utils.excelexporter;

/**
 *
 * @author <a href="mailto:alapierre@soft-project.pl">Adrian Lapierre</a>
 * $Rev: $, $LastChangedBy: $
 * $LastChangedDate: $
 */
public class PropertyAccessException extends RuntimeException {

    public PropertyAccessException(Throwable cause) {
        super(cause);
    }

    public PropertyAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public PropertyAccessException(String message) {
        super(message);
    }

    public PropertyAccessException() {
    }

}
