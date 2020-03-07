/*
 * Copyright 2011-04-03 the original author or authors.
 */

package pl.com.softproject.utils.excelexporter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author <a href="mailto:alapierre@soft-project.pl">Adrian Lapierre</a>
 * $Rev: $, $LastChangedBy: $
 * $LastChangedDate: $
 */
public class ConcatationColumnDescriptor extends ColumnDescriptor {

    protected List<String> propertyNames = new ArrayList<String>();

    public ConcatationColumnDescriptor(String headerName, String propertyName) {
        super(headerName, propertyName);
    }

    public ColumnDescriptor conCat(String propertyName) {
        propertyNames.add(propertyName);
        return this;
    }

    public List<ColumnDescriptor> getColumnDescriptors() {
        List<ColumnDescriptor> result = new ArrayList<ColumnDescriptor>(propertyNames.size());
        for(String propName : propertyNames) {
            result.add(new ColumnDescriptor("", propName));
        }
        return result;
    }

}
