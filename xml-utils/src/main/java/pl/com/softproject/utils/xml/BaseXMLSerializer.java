/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.com.softproject.utils.xml;


import jakarta.xml.bind.*;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;


import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.net.URL;
import java.util.Set;


/**
 * Klasa serializera dla pliku XML wniosku EURO
 *
 * @author adrian
 * @param <T>
 */
@Slf4j
public class BaseXMLSerializer<T> {

    private final JAXBContext jc;
    private final Schema schema;
    private final boolean noNameSpace;

    public String schemaLoaction;

    public BaseXMLSerializer(String contextPath, String xsdFileName, String schemaLocation, Set<XMLValidator.SchemaFactoryFeature> features) {
        this(contextPath, xsdFileName, schemaLocation, false, features);
    }

    public BaseXMLSerializer(String contextPath, String xsdFileName, String schemaLocation) {
        this(contextPath, xsdFileName, schemaLocation, false, Set.of());
    }

    public BaseXMLSerializer(String contextPath, String xsdFileName, String schemaLocation, boolean noNameSpace) {
        this(contextPath, xsdFileName, schemaLocation, noNameSpace, Set.of());
    }

    public BaseXMLSerializer(String contextPath, String xsdFileName, String schemaLocation, boolean noNameSpace, Set<XMLValidator.SchemaFactoryFeature> features) {
        this.schemaLoaction = schemaLocation;
        this.noNameSpace = noNameSpace;

        try {
            jc = JAXBContext.newInstance(contextPath);

            SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);

            features.forEach(feature -> {
                try {
                    sf.setFeature(feature.getKey(),feature.isValue());
                } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
                    log.warn(e.getMessage());
                }
            });

            URL url = getClass().getClassLoader().getResource(xsdFileName);
            schema = sf.newSchema(url);
        } catch (SAXException | JAXBException ex) {
            throw new XMLParseException(ex.getMessage(), ex);
        }
    }

    public T fromFile(File file) {
        return fromFile(file, true);
    }

    public T fromFile(File file, boolean validate) {
        try {
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            if(validate)
                unmarshaller.setSchema(schema);
            @SuppressWarnings("unchecked") T document = (T) unmarshaller.unmarshal(file);

            return document;

        } catch (JAXBException ex) {
            throw new XMLParseException(ex.getMessage(), ex);
        }

    }

    public T fromStream(InputStream stream) {
        return fromStream(stream, true);
    }

    public T fromStream(InputStream stream, boolean validate) {
        try {
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            if(validate)
                unmarshaller.setSchema(schema);
            @SuppressWarnings("unchecked") T document = (T) unmarshaller.unmarshal(stream);

            return document;

        } catch (JAXBException ex) {
            throw new XMLParseException(ex.getMessage(), ex);
        }

    }

    public T fromReader(Reader reader) {
        return fromReader(reader, true);
    }

    public T fromReader(Reader reader, boolean validate) {
        try {
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            if(validate)
                unmarshaller.setSchema(schema);
            @SuppressWarnings("unchecked") T document = (T) unmarshaller.unmarshal(reader);

            return document;

        } catch (JAXBException ex) {
            throw new XMLParseException(ex.getMessage(), ex);
        }

    }

    public T fromString(String xml) {
        return fromString(xml, true);
    }

    public T fromString(String xml, boolean validate) {
        try {
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            if(validate)
                unmarshaller.setSchema(schema);
            @SuppressWarnings("unchecked") T wniosek = (T) unmarshaller.unmarshal(new StringReader(xml));
            return wniosek;
        } catch (JAXBException ex) {
            throw new XMLParseException(ex.getMessage(), ex);
        }
    }

    public void toStream(T order, OutputStream os) {
        toStream(order, os, false);
    }

    public void toStream(T order, OutputStream os, boolean validate) {
        try {
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            if(validate)
                marshaller.setSchema(schema);

            marshaller.marshal(order, os);

        } catch (JAXBException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public void toFile(T dictionarys, String fileName) {
        toFile(dictionarys, fileName, true);
    }

    public void toFile(T document, String fileName, boolean validate) {

        try (OutputStream out = new FileOutputStream(fileName)) {
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            if(noNameSpace) marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, schemaLoaction);
            else  marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, schemaLoaction);

            if(validate)
                marshaller.setSchema(schema);

            marshaller.marshal(document, out);

        } catch (JAXBException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new XMLParseException(ex.getMessage(), ex);
        }
    }

    public void toFile(T document, String fileName, String encoding) {
        toFile(document, fileName, encoding, true);
    }

    public void toFile(T document, String fileName, String encoding, boolean validate) {

        try (OutputStream out = new FileOutputStream(fileName)) {
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);

            if(noNameSpace) marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, schemaLoaction);
            else  marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, schemaLoaction);

            if(validate)
                marshaller.setSchema(schema);

            marshaller.marshal(document, out);

        } catch (JAXBException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new XMLParseException(ex.getMessage(), ex);
        }
    }

    public String toString(T document) {
        return toString(document, true);
    }

    public String toString(T document, boolean validate) {
        try {
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            if(noNameSpace) marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, schemaLoaction);
            else  marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, schemaLoaction);

            if(validate)
                marshaller.setSchema(schema);
            StringWriter sw = new StringWriter();
            marshaller.marshal(document, sw);

            return sw.toString();

        } catch (JAXBException ex) {
            throw new XMLParseException(ex.getMessage(), ex);
        }

    }

    public JAXBElement convertFromDomNode(Node domNode, Class jaxbType) {
        try {
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            //noinspection unchecked
            return unmarshaller.unmarshal(domNode, jaxbType);

        } catch (JAXBException ex) {
            throw new XMLParseException(ex.getMessage(), ex);
        }
    }

    private void quietlyClose(OutputStream out) {
        if(out != null)
            try {
            out.close();
        } catch (IOException ex) {
            log.warn(ex.getMessage());
        }
    }
}
