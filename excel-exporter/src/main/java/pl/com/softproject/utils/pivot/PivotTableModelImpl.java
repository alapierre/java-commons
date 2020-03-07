/*
 * Copyright 2012-07-17 the original author or authors.
 */
package pl.com.softproject.utils.pivot;

import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @author Adrian Lapierre <adrian@softproject.com.pl>
 */
public class PivotTableModelImpl implements Iterable<Map<String, Object>>, PivotTableModel {
    
    private Map<String, Map<String, Object>> map = new LinkedHashMap<String, Map<String, Object>>();
    
    @Override
    public void add(String rowKey, String columnKey, Object value) {
        Map<String, Object> row = map.get(rowKey);
        if(row == null) {
            row = new LinkedHashMap<String, Object>();    
            row.put(columnKey, value);
            map.put(rowKey, row);
        } else {
            row.put(columnKey, value);
        }
    }
    
    @Override
    public void addRow(String rowKey, Map<String, Object> rowValue) {
        map.put(rowKey, rowValue);
    }
    
    /**
     * Metoda do wypełniania całej kolumny jedną wartością
     * @param columnKey
     * @param columnValue
     * @throws java.lang.Exception
     **/
    @Override
    public void addColumn(String columnKey, Object columnValue) throws Exception {
    
        if (columnValue instanceof Cloneable) {
        
            for (String rowKey : map.keySet()) {
                               
                Method clone = columnValue.getClass().getMethod("clone");                
                columnValue = clone.invoke(columnValue);                
                add(rowKey, columnKey, columnValue);
            }        
        } else {
            throw new RuntimeException("Not Supported Yet.");
        }
    }
            
    /**
     * Metoda do dodawania całej kolumny różnymi wartościami
     * 
     * @param columnKey - klucz kolumny
     * @param rowKeyToColumnValueMap - mapowanie rowKey na columnValue 
     */
    @Override
    public void addColumn(String columnKey, Map<String, Object> rowKeyToColumnValueMap) {
        
        for (Map.Entry<String, Object> rowKeyToColumnValue : rowKeyToColumnValueMap.entrySet()) {
            
            String rowKey = rowKeyToColumnValue.getKey();
            Object columnValue = rowKeyToColumnValue.getValue();
            
            add(rowKey, columnKey, columnValue);            
        }
    }
    
    @Override
    public void removeColumn(String columnKey) {
        
        RowIterator iter = (RowIterator)iterator();
        
        while (iter.hasNext()) {            
            Map<String, Object> columnMap = iter.next();
            
            if (columnMap != null) {                
                columnMap.remove(columnKey);            
                if (columnMap.isEmpty())
                    iter.remove();                
            }        
        }
    }
    
    @Override
    public void removeRow(String rowKey) {
        RowIterator iter = (RowIterator)iterator();        
        while (iter.hasNext()) {  
            iter.next();
            
            String currentRowKey = iter.rowKey();            
            if (rowKey.equals(currentRowKey)) {
                
                iter.remove();
            }
        }
    }
        
    @Override
    public Iterator  iterator() {
        return new RowIterator(this);
    }
    
    @Override
    public Object get(String rowKey, String columnKey) {
        Map<String, Object> tmp = map.get(rowKey);
        return tmp != null ? tmp.get(columnKey) : null;
    }
    
    @Override
    public List<String> getRowNames() {
        
        List<String> rows = new ArrayList<String>();
        
        for(Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
            rows.add(entry.getKey());
        }
        return rows;
    }
        
    @Override
    public Set<String> getColumnNames() {
        
        Set<String> columns = new LinkedHashSet<String>();
        
        for(Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
            //columns.addAll(entry.getValue().keySet());
            Map<String, Object> m = entry.getValue();       
            for (Map.Entry<String, Object> e : m.entrySet()) {
                columns.add(e.getKey());                
            }            
        }        
        return columns;
    }
    
    public void test() {
        System.out.println("map: " + map);
        
        for(Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
//            for(Map.Entry<String, Object> t : entry.getValue().entrySet()) {
//                System.out.println("");
//            }
        }
    }
    
    public static class RowIterator implements Iterator<Map<String, Object>> {
        
        private final Set<Entry<String, Map<String, Object>>> entrys;
        //private final Iterator<Map<String, Object>> it;
        private PivotTableModelImpl impl;
        private Entry<String, Map<String, Object>> currentEntry;
        private final Iterator<Entry<String, Map<String, Object>>> it;
        

        RowIterator(PivotTableModelImpl impl) {
            this.impl = impl;
            //it = impl.map.values().iterator();
            entrys = impl.map.entrySet();
            it = entrys.iterator();
        }
        
        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public Map<String, Object> next() {
            currentEntry = it.next();
            return currentEntry.getValue();
        }

        @Override
        public void remove() {
            //Kasujemy cały rekord bo nie ma sensu,
            //żeby istniał wiersz, który dla każdej kolumny ma wartość null
            it.remove();
        }
        
        public String rowKey() {
            return currentEntry.getKey();
        }
    }
    
}
