package pl.sid.commons.io;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXParseException;
import pl.com.softproject.utils.xml.XMLValidator;

import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.core.Is.is;

/**
 * Created by adrian on 2017-12-05.
 */
public class MultipleReaderTest {

    static final String lineSeparator = System.getProperty("line.separator");

    @Test
    public void testByteArray() throws Exception {

        MultipleReader reader = new MultipleReader(new FileReader("src/test/resources/pit_11.xml"));
        String s = new String(reader.content, StandardCharsets.UTF_8);

        Assert.assertThat(s, is(loadTemplate("src/test/resources/pit_11.xml")));
    }

    @Test
    public void readByReader() throws Exception {

        MultipleReader reader = new MultipleReader(new FileReader("src/test/resources/pit_11.xml"));

        StringBuilder sb = new StringBuilder();

        try(BufferedReader br = new BufferedReader(reader.getReader())) {
            for(String line; (line = br.readLine()) != null; ) {
                sb.append(line).append(lineSeparator);
            }
        }

        Assert.assertThat(sb.toString(), is(loadTemplate("src/test/resources/pit_11.xml")));
    }

    @Test
    public void readByStream() throws Exception {

        MultipleReader reader = new MultipleReader(new FileInputStream("src/test/resources/pit_11.xml"));

        String s = new String(reader.content, StandardCharsets.UTF_8);

        Assert.assertThat(s, is(loadTemplate("src/test/resources/pit_11.xml")));

    }

    //@Test
    public void readGetInputStream() throws Exception {

        //TODO: problem ze znamakiem powrtou karetki \r na końcu pliku

        MultipleReader reader = new MultipleReader(new FileInputStream("src/test/resources/pit_11.xml"));

        String result = new BufferedReader(new InputStreamReader(reader.getInputStream()))
                .lines().collect(Collectors.joining(lineSeparator));

        Assert.assertThat(result + "\r\n", is(loadTemplate("src/test/resources/pit_11.xml")));
    }

    private String loadTemplate(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    @Test
    public void readAndValid() throws Exception{

        MultipleReader reader = new MultipleReader(new FileReader("src/test/resources/pit_11.xml"));

        String namespace = XMLValidator.getNemespaceFromXMLDocument(reader.getReader());
        System.out.println("namespase from XML: " + namespace);
        List<SAXParseException> errors = new ArrayList<>();
        XMLValidator.validate(reader.getReader(), new StreamSource(namespace + "schemat.xsd"), errors);

    }

}