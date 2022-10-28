package pl.com.softproject.utils.xml.stax;

import lombok.Value;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import java.util.Set;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2021.12.27
 */
@Value
public class XmlElement {

    String path;
    QName name;
    String value;
    Set<Attribute> attributes;



}
