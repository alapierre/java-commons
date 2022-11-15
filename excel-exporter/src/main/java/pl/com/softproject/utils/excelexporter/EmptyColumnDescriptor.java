/*
 * Copyright 2011-04-02 the original author or authors.
 */

package pl.com.softproject.utils.excelexporter;

/**
 * Służy do opisania pustej kolumny, takiej która nie pochodzi z beana
 *
 * @author {@literal al@alapierre.io}
 * $Rev: $, $LastChangedBy: $
 * $LastChangedDate: $
 */
public class EmptyColumnDescriptor extends ColumnDescriptor{

    public EmptyColumnDescriptor(String headerName) {
        super(headerName, null);
    }

}
