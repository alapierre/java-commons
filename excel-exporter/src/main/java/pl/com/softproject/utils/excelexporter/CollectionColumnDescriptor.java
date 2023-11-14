/*
 * Copyright 2011-07-21 the original author or authors.
 */
package pl.com.softproject.utils.excelexporter;

import jodd.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import java.util.Collection;
import java.util.Iterator;

/**
 * Służy do obsługi property, które są kolekcjami
 *
 * @author Adrian Lapierre {@literal al@alapierre.io}
 */
@Slf4j
public abstract class CollectionColumnDescriptor extends EnumeratedColumnDescription<String> {

    private final String propertyName;

    /**
     * Jest wywoływana dla każdego elementu w kolekcji, powinna zwrócić wartość,
     * która ma zostać wyświetlona dla pojedyńczego elementu w kolekcji
     *
     * @param rowNumber - numer przetwarzanego wiersza
     * @param value     - wartość z kolekcji
     * @return
     */
    public abstract String formatValue(int rowNumber, Object value);

    protected CollectionColumnDescriptor(String headerName, String propertyName) {
        super(headerName);
        this.propertyName = propertyName;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public String getValue(int rowNumber, Object bean) {

        StringBuilder sb = new StringBuilder();

        Object obj = BeanUtil.pojo.getProperty(bean, propertyName);

        if (obj instanceof Collection) {

            Collection collection = (Collection) obj;
            Iterator iterator = collection.iterator();
            while (iterator.hasNext()) {
                sb.append(formatValue(rowNumber, iterator.next()));
                if (iterator.hasNext())
                    sb.append(", ");
            }
        } else {
            log.warn("property " + propertyName + " is not instance of Collection - " + obj.getClass());
            return null;
        }

        return sb.toString();
    }

}
