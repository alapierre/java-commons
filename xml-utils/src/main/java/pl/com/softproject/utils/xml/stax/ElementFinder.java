package pl.com.softproject.utils.xml.stax;

import com.sun.istack.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2021.12.27
 */
@SuppressWarnings("unused")
@Slf4j
@RequiredArgsConstructor
public class ElementFinder {

    private final XMLInputFactory factory = XMLInputFactory.newFactory();
    private final LinkedList<String> path = new LinkedList<>();

    public Optional<XmlElement> find(String xml, String pathToFind) throws XMLStreamException {
        return find(new StringReader(xml), pathToFind);
    }

    public Optional<XmlElement> find(File xml, String pathToFind) throws XMLStreamException, IOException {
        try (val reader = new FileReader(xml)) {
            return find(reader, pathToFind);
        }
    }

    public Set<XmlElement> find(File xml, @NotNull Set<String> pathsToFind) throws XMLStreamException, IOException {
        try (val reader = new FileReader(xml)) {
            return find(reader, pathsToFind);
        }
    }

    @NotNull
    public Set<XmlElement> find(@NotNull Reader xml, @NotNull Set<String> pathsToFind) throws XMLStreamException {

        path.clear();

        XMLEventReader eventReader = null;
        val res = new HashSet<XmlElement>();

        try {
            eventReader = factory.createXMLEventReader(xml);

            while (eventReader.hasNext()) {

                val xmlEvent = eventReader.nextEvent();

                if (xmlEvent.isStartElement()) {
                    val startElement = xmlEvent.asStartElement();
                    path.addLast(startElement.getName().getLocalPart());
                    val pathAsString = String.join("/", path);
                    if (pathsToFind.contains(pathAsString)) {
                        res.add(createXmlElement(xmlEvent, pathAsString, eventReader));
                    }
                } else if(xmlEvent.isEndElement()) {
                    path.removeLast();
                }
            }

        } finally {
            if (eventReader != null) {
                try {
                    eventReader.close();
                } catch (XMLStreamException ignore) {
                    // ignore exception on close
                }
            }
        }
        return res;
    }

    public Optional<XmlElement> find(@NotNull Reader xml, @NotNull String pathToFind) throws XMLStreamException {

        path.clear();

        XMLEventReader eventReader = null;

        try {
            eventReader = factory.createXMLEventReader(xml);

            while (eventReader.hasNext()) {

                val xmlEvent = eventReader.nextEvent();

                if (xmlEvent.isStartElement()) {
                    val startElement = xmlEvent.asStartElement();
                    path.addLast(startElement.getName().getLocalPart());
                    val pathAsString = String.join("/", path);
                    if (pathAsString.equals(pathToFind)) {
                        return Optional.of(createXmlElement(xmlEvent, pathAsString, eventReader));
                    }
                } else if(xmlEvent.isEndElement()) {
                    path.removeLast();
                }
            }

        } finally {
            if (eventReader != null) {
                try {
                    eventReader.close();
                } catch (XMLStreamException ignore) {
                    // ignore exception on close
                }
            }
        }
    return Optional.empty();
    }

    protected XmlElement createXmlElement(XMLEvent event, String path, XMLEventReader eventReader) throws XMLStreamException {

        val startElement = event.asStartElement();

        val name = startElement.getName();

        Iterable<Attribute> iterable = startElement::getAttributes;

        val attributes = StreamSupport.stream(iterable.spliterator(), false)
                .collect(Collectors.toSet());

        String value = null;

        if (eventReader.hasNext()) {
            val next = eventReader.nextEvent();
            if(next.isCharacters()) {
                value = next.asCharacters().getData();
            }
        }

        return new XmlElement(path, name, value, attributes);
    }

}
