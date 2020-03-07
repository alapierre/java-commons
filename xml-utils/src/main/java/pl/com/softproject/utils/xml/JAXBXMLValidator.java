/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.com.softproject.utils.xml;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 *
 * @author Adrian Lapierre
 */
public class JAXBXMLValidator {
    
    private JAXBContext jc;
    private SchemaFactory sf;
    private Schema schema;
    
    public String schemaLoaction;//= "http://www.uke.gov.pl/euro http://schema.softproject.com.pl/uke/uke-euro.xsd";

    public JAXBXMLValidator(String contextPath, String xsdFileName, String schemaLocation) {
        this.schemaLoaction = schemaLocation;
        
        try {
            jc = JAXBContext.newInstance(contextPath);

            sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI); 
            URL url = getClass().getClassLoader().getResource(xsdFileName);
            requireNonNull(url, "problem z odczytaniem z classpath pliku schemy XML " + xsdFileName);
            schema = sf.newSchema(url);
        } catch (SAXException | JAXBException ex) {
            throw new XMLParseException(ex.getMessage(), ex);
        }
    }
    
    /**
     * Validuje XML względem XML Schemy, lokalizacja schemy b�dzie pobrana z atrybutu schemaLocation z dokumentu XML
     * Metoda z założenia, nigdy nie rzuca wyjątkami. Gdy walidacje nie przejdzie zwraca po prostu "false".
     * @param <T>
     * @param jaxbXmlObject - dokument który powstał w wyniku wywołania metody "unmarshal".
     * @param exceptions - kolekcja, w której zostaną zwrócone błędy.
     * @return - true jeśli dokument przechodzi poprawnie walidację.
     */
    public <T> boolean validate(T jaxbXmlObject, List<SAXParseException> exceptions) {
        
            try {
                JAXBSource source = new JAXBSource(jc, jaxbXmlObject);
               
                Validator validator = schema.newValidator();        

                if (exceptions == null)
                    exceptions = new ArrayList<>();

                validator.setErrorHandler(new XMLValidator.XMLErrorExtensionHandler(exceptions));        
                validator.validate(source);

                return exceptions.isEmpty();
    
            } catch (SAXException | JAXBException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            } catch(IOException ex) {
                throw new XMLParseException(ex.getMessage(), ex);
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }           
    }
}
