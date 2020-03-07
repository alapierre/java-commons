/*
 * Copyright 2011-04-02 the original author or authors.
 */

package pl.com.softproject.utils.excelexporter;

/**
 * Służy do dodawania kolumn, których wartość jest wyliczana poza beanem
 *
 * @author <a href="mailto:alapierre@soft-project.pl">Adrian Lapierre</a>
 * $Rev: $, $LastChangedBy: $
 * $LastChangedDate: $
 */
public abstract class EnumeratedColumnDescription<T> extends ColumnDescriptor {

    public EnumeratedColumnDescription(String headerName) {
        super(headerName, "not-existent-property");
    }

    /**
     * dostarczona implementacja powinna zwracać wartość która ma zostać
     * wstawiona do arkusza xls
     *
     * @param rowNumber - numer aktualnie przetwarzanego wiersza
     * @param bean - obiekt bean z którego generowany jest wiersz
     * @return
     */
    abstract public T getValue(int rowNumber, Object bean);

}
