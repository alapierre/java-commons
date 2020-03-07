/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.com.softproject.utils.xml;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author adrian
 */
@Slf4j
public class XMLValidator {

   /**
     * Sprawdza składnie XML w przekazanym dokumencie
     *
     * @param reader - Reader do dokumentu XML
     * @param errors - zainicjowana kolekcja, w której zostaną zwrócone błędy. Kolekcja zostanie wyzerowana.
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     */
    public static boolean checkSyntax(Reader reader, Collection<SAXParseException> errors) throws ParserConfigurationException, IOException {

       DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
       dbf.setValidating(false);
       dbf.setNamespaceAware(true);

       DocumentBuilder builder = dbf.newDocumentBuilder();
       builder.setErrorHandler(new XMLErrorHandler(errors));

       InputSource source = new InputSource(reader);
        try {
            builder.parse(source);
        } catch (SAXException ignore) {}

       return errors.isEmpty();
    }

    /**
     * Validuje XML względem XML Schemy
     *
     * @param reader - Reader do dokumentu XML
     * @param schemaSource - zasób pozwalający odczytać schemę XML, jeżli jest null to zostanie użyta schema wskazana w atrybucie schemaLocation z dokumentu XML
     * @param errors  - zainicjowana kolekcja, w której zostaną zwrócone błędy. Kolekcja zostanie wyzerowana.
     * @return - true jeśli dokument validuje się
     * @throws SAXException - jeśli nie można zainicjować parsera
     * @throws IOException - jeśli nie można czytać z Readera
     */
    public static boolean validate(Reader reader, Source schemaSource, Collection<SAXParseException> errors) throws SAXException, IOException {
        return validate(reader, new Source[]{schemaSource}, errors);
    }

    public static boolean validate(Reader reader, Source[] schemaSource, Collection<SAXParseException> errors) throws SAXException, IOException {

        errors.clear();

        // 1. Lookup a factory for the W3C XML Schema language
        SchemaFactory factory =
                SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        // 2. Compile the schema.
        Schema schema = schemaSource == null ? factory.newSchema() : factory.newSchema(schemaSource);

        // 3. Get a validator from the schema.
        Validator validator = schema.newValidator();

        // 4. Parse the document you want to check.
        Source source = new StreamSource(reader);

        // 5. Check the document
        validator.setErrorHandler(new XMLErrorHandler(errors));
        validator.validate(source);

        return errors.isEmpty();
    }

    /**
     * Validuje XML względem XML Schemy, lokalizacja schemy będzie pobrana z atrybutu schemaLocation z dokumentu XML
     *
     * @param reader - Reader do dokumentu XML
     * @param errors  - zainicjowana kolekcja, w której zostaną zwrócone błędy. Kolekcja zostanie wyzerowana.
     * @return - true jeśli dokument validuje się
     * @throws SAXException - jeśli nie można zainicjować parsera
     * @throws IOException - jeśli nie moďżna czytać z Readera
     */
    public static boolean validate(Reader reader, Collection<SAXParseException> errors) throws SAXException, IOException {
        return validate(reader, (Source)null, errors);
    }

    /**
     * Waliduje XML względem XML Schemy, lokalizacja schemy będzie pobrana z atrybutu schemaLocation z dokumentu XML
     * Metoda z założenia, nigdy nie rzuca wyąkami. Gdy walidacje nie przejdzie zwraca po prostu "false".
     * @param <T>
     * @param jaxbObject - dokument który powstał‚ w wyniku wywołania metody "unmarshal". Np. DsmlDocument lub DomainsDocument.
     * @param xsdFSource - nazwa pliku xsd, jeśli jest null to zostanie użyta schema wskazana w atrybucie schemaLocation z dokumentu XML.
     * @param exceptions - kolekcja, w której zostaną zwrócone błędy.
     * @return - true jeśli dokument przechodzi poprawnie walidację.
     */
    public static <T> boolean validateJaxbObject(T jaxbObject, Source xsdFSource, List<SAXParseException> exceptions) {
        try {

            String contextPath = jaxbObject.getClass().getPackage().getName();
            JAXBContext jc = JAXBContext.newInstance(contextPath);
            JAXBSource source = new JAXBSource(jc, jaxbObject);

            SchemaFactory factory = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = xsdFSource == null ? factory.newSchema() : factory.newSchema(xsdFSource);
            Validator validator = schema.newValidator();        

            if (exceptions == null)
                exceptions = new ArrayList<>();
            else exceptions.clear();

            validator.setErrorHandler(new XMLErrorExtensionHandler(exceptions));
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

    public static String getNemespaceFromXMLDocument(Reader reader) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder parser = null;
        InputSource source = null;
        try {
            parser = dbf.newDocumentBuilder();
            source = new InputSource(reader);
            Document document = parser.parse(source);
            return document.getChildNodes().item(0).getNamespaceURI();
        } catch (ParserConfigurationException | SAXException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        } catch (IOException ex) {
            log.error("cannot open file to parse");
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static class XMLErrorHandler implements ErrorHandler {

        XMLErrorHandler(Collection<SAXParseException> errors) {
            this.errors = errors;
        }

        private Collection<SAXParseException> errors;

        @Override
        public void warning(SAXParseException exception) throws SAXException {
            errors.add(exception);
        }

        @Override
        public void error(SAXParseException exception) throws SAXException {
            errors.add(exception);
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            errors.add(exception);
        }
    }   
        
    public static class XMLErrorExtensionHandler extends XMLErrorHandler {
        
        XMLErrorExtensionHandler(List<SAXParseException> exceptions) {
            super(exceptions);
        }
        
        @Override
        public void warning(SAXParseException exception) throws SAXException {            
            String message = exception.getMessage() != null ? exception.getMessage() : "";
            log.warn(message, exception);
        }
    }
}