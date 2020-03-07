/*
 * Copyright 2015-06-24 the original author or authors.
 */
package pl.com.softproject.utils.pivot;

import java.text.DecimalFormat;
import java.util.Map;

/**
 *
 * @author Adrian Lapierre <adrian@soft-project.pl>
 */
public class PivotTableHtmlExporter {

    private PivotTableModel pivotTableModel;
    private String[] columns;
    final DecimalFormat format = new DecimalFormat("#,###0.00");
    
    public PivotTableHtmlExporter(PivotTableModel pivotTableModel, String[] columns) {
        this.columns = columns;
        this.pivotTableModel = pivotTableModel;
    }
    
    public String export(String caption) {
        
        StringBuilder sb = new StringBuilder();
        sb.append("<table border =\"1\">");
        sb.append("<caption>").append(caption).append("</caption>");
        sb.append(header());
        
        PivotTableModelImpl.RowIterator rows = (PivotTableModelImpl.RowIterator) pivotTableModel.iterator();
        
        while (rows.hasNext()) {
        
            Map<String, Object> pivotRow = rows.next();
            String rowKey = rows.rowKey();
            
            sb.append("<tr>");
            sb.append("<td>").append(rowKey).append("</td>");

            for (String column : columns) {                              
                    
                Object cellValue = pivotRow.get(column);
                if(cellValue == null) {
                    sb.append("<td align=\"center\">").append("-").append("</td>");
                    continue;
                }
                     
                String val;
                if(cellValue instanceof Integer) {
                    val = String.format("%d", cellValue);
                } else if (cellValue instanceof Number) {
                    val = format.format(cellValue);
                } else {
                    val = cellValue.toString();
                }

                sb.append("<td align=\"right\">").append(val).append("</td>");
            }
        }
        
        sb.append("</tr>");
        sb.append("</table>");
        
        return sb.toString();
        
    }
    
    private StringBuilder header() {
        
        StringBuilder sb = new StringBuilder();
        sb.append("<tr>");
        sb.append("<th/>");
        for(String column : columns) {
            sb.append("<th>").append(column).append("</th>"); 
        }
        sb.append("</tr>");
        return sb;
    }
    
}
