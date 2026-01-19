package pl.com.softproject.utils.xml.stax;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.NotNull;

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

    public Optional<XmlElement> find(@NonNull String xml, @NonNull String pathToFind) throws XMLStreamException {
        return find(new StringReader(xml), pathToFind);
    }

    public Optional<XmlElement> find(@NonNull File xml, @NonNull String pathToFind) throws XMLStreamException, IOException {
        try (val reader = new FileReader(xml)) {
            return find(reader, pathToFind);
        }
    }

    public Set<XmlElement> find(@NonNull File xml, @NonNull Set<String> pathsToFind) throws XMLStreamException, IOException {
        try (val reader = new FileReader(xml)) {
            return find(reader, pathsToFind);
        }
    }

    @NotNull
    public Set<XmlElement> find(@NonNull Reader xml, @NonNull Set<String> pathsToFind) throws XMLStreamException {
        path.clear();

        XMLEventReader eventReader = null;
        val res = new LinkedHashSet<XmlElement>();

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
                } else if (xmlEvent.isEndElement()) {
                    path.removeLast();
                }
            }

        } finally {
            closeQuietly(eventReader);
        }
        return res;
    }

    public Optional<XmlElement> find(@NonNull Reader xml, @NonNull String pathToFind) throws XMLStreamException {
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
                } else if (xmlEvent.isEndElement()) {
                    path.removeLast();
                }
            }

        } finally {
            closeQuietly(eventReader);
        }
        return Optional.empty();
    }

    protected XmlElement createXmlElement(@NonNull XMLEvent event, @NonNull String path, @NonNull XMLEventReader eventReader) throws XMLStreamException {

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

    private void closeQuietly(XMLEventReader eventReader) {
        if (eventReader != null) {
            try {
                eventReader.close();
            } catch (XMLStreamException ignore) {
                // ignore
            }
        }
    }
}