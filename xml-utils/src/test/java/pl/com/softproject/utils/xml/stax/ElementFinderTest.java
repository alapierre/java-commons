package pl.com.softproject.utils.xml.stax;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2021.12.27
 */
class ElementFinderTest {

    @Test
    void find() throws XMLStreamException, IOException {

        ElementFinder finder = new ElementFinder();

        val element = finder.find(
                new File("src/test/resources/Przyklad 1.xml"),
                "Faktura/Fa/P_2");

        val result = element.orElseThrow(IllegalStateException::new);

        Assertions.assertEquals("FV2022/02/150", result.getValue());
        Assertions.assertEquals("P_2", result.getName().getLocalPart());

    }

    @Test
    void FindElements() throws XMLStreamException, IOException {

        ElementFinder finder = new ElementFinder();

        val elements = new HashSet<String>();
        elements.add("Faktura/Fa/P_2");
        elements.add("Faktura/Podmiot1/DaneIdentyfikacyjne/NIP");

        val resp = finder.find(
                new File("src/test/resources/Przyklad 1.xml"),
                elements);

        resp.forEach(System.out::println);

        val map = resp.stream()
                .collect(Collectors.toMap(e -> e.getName().getLocalPart(), XmlElement::getValue));

        Assertions.assertEquals("FV2022/02/150", map.get("P_2"));
        Assertions.assertEquals("9999999999", map.get("NIP"));

    }

}
