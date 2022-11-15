/*
 * Copyright 2009 the original author or authors.
 */
package pl.com.softproject.utils.xml;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

/**
 *
 * @author Adrian Lapierre {@literal <al@alapierre.io>}
 */
public class XSLTTransformator {

    private Transformer transformer;

    public XSLTTransformator(File xsltFile) throws FileNotFoundException, TransformerConfigurationException {

        InputStream xsltImputStream = new FileInputStream(xsltFile);
        Source xsltSource = new StreamSource(xsltImputStream);

        TransformerFactory transFactory = TransformerFactory.newInstance();
        transformer = transFactory.newTransformer(xsltSource);

        //transformer.setOutputProperty(OutputKeys.ENCODING, "windows-1250");
    }

    public XSLTTransformator(InputStream is) throws FileNotFoundException, TransformerConfigurationException {

        Source xsltSource = new StreamSource(is);
        TransformerFactory transFactory = TransformerFactory.newInstance();
        transformer = transFactory.newTransformer(xsltSource);
        //transformer.setOutputProperty(OutputKeys.ENCODING, "windows-1250");
    }

    public void setEncoding(String encoding) {
        transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
    }

    public String getEncoding() {
        return transformer.getOutputProperty(OutputKeys.ENCODING);
    }

    public void transform(Reader in, OutputStream out) throws TransformerException {

        Result streamResult = new StreamResult(out);
        transformer.transform(new StreamSource(in), streamResult);

    }

     public void transform(Reader in, Writer out) throws TransformerException {

        Result streamResult = new StreamResult(out);
        transformer.transform(new StreamSource(in), streamResult);

    }

    public void transform(Source in, OutputStream out) throws TransformerException {
        Result streamResult = new StreamResult(out);
        transformer.transform(in, streamResult);
    }

    public void transform(Source in, Result out) throws TransformerException {
        transformer.transform(in, out);
    }

    public String transform(String source) throws TransformerException {

        OutputStream result = new ByteArrayOutputStream();

        transform(new StringReader(source), result);
        return result.toString();
    }

    public void transform(String source, OutputStream out) throws TransformerException {
        transform(new StringReader(source), out);
    }
}
