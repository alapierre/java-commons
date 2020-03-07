/*
 * Copyright 2011-04-02 the original author or authors.
 */

package pl.com.softproject.utils.excelexporter;

/**
 * Służy do opisania pustej kolumny, takiej która nie pochodzi z beana
 *
 * @author <a href="mailto:alapierre@soft-project.pl">Adrian Lapierre</a>
 * $Rev: $, $LastChangedBy: $
 * $LastChangedDate: $
 */
public class EmptyColumnDescriptor extends ColumnDescriptor{

    public EmptyColumnDescriptor(String headerName) {
        super(headerName, null);
    }

}
