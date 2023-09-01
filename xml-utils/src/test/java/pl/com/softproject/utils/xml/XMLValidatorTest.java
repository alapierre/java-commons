package pl.com.softproject.utils.xml;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2023.09.01
 */
class XMLValidatorTest {

    @Test
    void validate() throws Exception {

        List<SAXParseException> errors = new ArrayList<>();

        try (Reader reader = new FileReader("src/test/resources/FA2.xml");
             InputStream schemaReader = new FileInputStream("src/test/resources/Schemat_FA_VAT(2)_v1-0E.xsd")) {
             StreamSource schema = new StreamSource(schemaReader);

            XMLValidator.validate(reader, schema, errors, Set.of(new XMLValidator.SchemaFactoryFeature(XMLConstants.FEATURE_SECURE_PROCESSING,false)));

            errors.forEach(System.out::println);
        }
    }

}
