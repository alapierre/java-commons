/*
 * Copyright 2011-07-21 the original author or authors.
 */
package pl.com.softproject.utils.excelexporter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;

/**
 * Służy do obsługi property, które są kolekcjami
 *
 * @author Adrian Lapierre <adrian@softproject.com.pl>
 */
@Slf4j
public abstract class CollectionColumnDescriptor extends EnumeratedColumnDescription<String> {

    private String propertyName;

    /**
     * Jest wywoływana dla każdego elemantu w kolekcji, powinna zwrócić wartość
     * która ma zostać wyświetlona dla pojedyńczego elementu w kolekcji
     *
     * @param rowNumber - numer przetwarzanego wiersza
     * @param value - wartość z kolecji
     * @return
     */
    public abstract String formatValue(int rowNumber, Object value);

    public CollectionColumnDescriptor(String headerName, String propertyName) {
        super(headerName);
        this.propertyName = propertyName;
    }

    @Override
    public String getValue(int rowNumber, Object bean) {

        StringBuilder sb = new StringBuilder();


        try {
        Object obj = PropertyUtils.getNestedProperty(bean, propertyName);

        if(obj instanceof Collection) {

            Collection collection = (Collection)obj;
            Iterator iterator = collection.iterator();
            while(iterator.hasNext()) {
                sb.append(formatValue(rowNumber, iterator.next()));
                if(iterator.hasNext())
                    sb.append(", ");
            }

//            for(Object tmp : (Collection)obj) {
//                sb.append(formatValue(rowNumber, tmp));
//
//                sb.append(", ");
//
//            }
        } else {
            log.warn("property " + propertyName + " is not instance of Collection - " + obj.getClass());
            return null;
        }

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            throw new PropertyAccessException(ex);
        } catch (IndexOutOfBoundsException ex) {
            if(log.isDebugEnabled())
                log.debug("for property " + propertyName + " " + ex.getMessage());
            return null;
        }
        //return sb.substring(0, sb.length()-1);
        return sb.toString();
    }


}
