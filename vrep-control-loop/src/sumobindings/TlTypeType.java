//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0-b170531.0717 
//         See <a href="https://jaxb.java.net/">https://jaxb.java.net/</a> 
//         Any modifications to this file will be lost upon recompilation of the source schema. 
//         Generated on: 2018.01.03 at 06:19:16 PM CET 
//


package sumobindings;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for tlTypeType.
 * 
 * <p>The following schema fragment specifies the expected         content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="tlTypeType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="actuated"/&gt;
 *     &lt;enumeration value="delay_based"/&gt;
 *     &lt;enumeration value="static"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "tlTypeType")
@XmlEnum
public enum TlTypeType {

    @XmlEnumValue("actuated")
    ACTUATED("actuated"),
    @XmlEnumValue("delay_based")
    DELAY_BASED("delay_based"),
    @XmlEnumValue("static")
    STATIC("static");
    private final String value;

    TlTypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TlTypeType fromValue(String v) {
        for (TlTypeType c: TlTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
