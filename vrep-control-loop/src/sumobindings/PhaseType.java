//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0-b170531.0717 
//         See <a href="https://jaxb.java.net/">https://jaxb.java.net/</a> 
//         Any modifications to this file will be lost upon recompilation of the source schema. 
//         Generated on: 2018.01.03 at 06:19:16 PM CET 
//


package sumobindings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for phaseType complex type.
 * 
 * <p>The following schema fragment specifies the expected         content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="phaseType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="duration" use="required" type="{}nonNegativeFloatType" /&gt;
 *       &lt;attribute name="minDur" type="{}nonNegativeFloatType" /&gt;
 *       &lt;attribute name="maxDur" type="{}nonNegativeFloatType" /&gt;
 *       &lt;attribute name="state" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;pattern value="[ruyYgGoOs]+"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "phaseType")
public class PhaseType {

    @XmlAttribute(name = "duration", required = true)
    protected float duration;
    @XmlAttribute(name = "minDur")
    protected Float minDur;
    @XmlAttribute(name = "maxDur")
    protected Float maxDur;
    @XmlAttribute(name = "state", required = true)
    protected String state;

    /**
     * Gets the value of the duration property.
     * 
     */
    public float getDuration() {
        return duration;
    }

    /**
     * Sets the value of the duration property.
     * 
     */
    public void setDuration(float value) {
        this.duration = value;
    }

    /**
     * Gets the value of the minDur property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getMinDur() {
        return minDur;
    }

    /**
     * Sets the value of the minDur property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setMinDur(Float value) {
        this.minDur = value;
    }

    /**
     * Gets the value of the maxDur property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getMaxDur() {
        return maxDur;
    }

    /**
     * Sets the value of the maxDur property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setMaxDur(Float value) {
        this.maxDur = value;
    }

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setState(String value) {
        this.state = value;
    }

}
