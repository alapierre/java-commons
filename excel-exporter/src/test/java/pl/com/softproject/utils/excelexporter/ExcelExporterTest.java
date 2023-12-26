package pl.com.softproject.utils.excelexporter;

import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2023.11.13
 */
public class ExcelExporterTest {

    @Test
    public void test() throws IOException {

        val r = new Report();
        r.setName("Jan");
        r.setLastName("Kowalski");
        r.setAddress(List.of("Warszawska", "Marszałkowska"));

        List<Report> rows = List.of(r);

        ExcelExporter excelExporter = new ExcelExporter("report");

        excelExporter.addColumn(new ColumnDescriptor("Name", "name"));
        excelExporter.addColumn(new ColumnDescriptor("Last Name", "lastName"));
        excelExporter.addColumn(new ColumnDescriptor("Address", "address[1]"));

        rows.forEach(excelExporter::createRow);

        excelExporter.save(new File("/tmp/sample_excel.xlsx"));

    }

    @Data
    public static class Report {

        private String name;
        private String LastName;
        private List<String> address;

    }

}
