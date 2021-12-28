package pl.com.softproject.utils.xml.stax;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;

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

}
