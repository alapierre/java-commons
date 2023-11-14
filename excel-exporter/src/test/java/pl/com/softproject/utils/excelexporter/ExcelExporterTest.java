package pl.com.softproject.utils.excelexporter;

import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.Test;
import java.util.List;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2023.11.13
 */
public class ExcelExporterTest {

    @Test
    public void test() {

        val r = new Report();
        r.setName("Jan");
        r.setLastName("Kowalski");

        List<Report> rows = List.of(r);

        ExcelExporter excelExporter = new ExcelExporter("report");

        excelExporter.addColumn(new ColumnDescriptor("Name", "name"));
        excelExporter.addColumn(new ColumnDescriptor("Last Name", "lastName"));

        rows.forEach(excelExporter::createRow);

    }

    @Data
    public static class Report {

        private String name;
        private String LastName;

    }

}
