//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0-b170531.0717 
//         See <a href="https://jaxb.java.net/">https://jaxb.java.net/</a> 
//         Any modifications to this file will be lost upon recompilation of the source schema. 
//         Generated on: 2018.01.03 at 06:19:16 PM CET 
//


package sumobindings;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for tlLogicType complex type.
 * 
 * <p>The following schema fragment specifies the expected         content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tlLogicType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="phase" type="{}phaseType"/&gt;
 *         &lt;element name="param" type="{}paramType"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="type" type="{}tlTypeType" /&gt;
 *       &lt;attribute name="programID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="offset" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tlLogicType", propOrder = {
    "phaseOrParam"
})
public class TlLogicType {

    @XmlElements({
        @XmlElement(name = "phase", type = PhaseType.class),
        @XmlElement(name = "param", type = ParamType.class)
    })
    protected List<Object> phaseOrParam;
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "type")
    protected TlTypeType type;
    @XmlAttribute(name = "programID", required = true)
    protected String programID;
    @XmlAttribute(name = "offset")
    protected Float offset;

    /**
     * Gets the value of the phaseOrParam property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the phaseOrParam property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPhaseOrParam().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PhaseType }
     * {@link ParamType }
     * 
     * 
     */
    public List<Object> getPhaseOrParam() {
        if (phaseOrParam == null) {
            phaseOrParam = new ArrayList<Object>();
        }
        return this.phaseOrParam;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link TlTypeType }
     *     
     */
    public TlTypeType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link TlTypeType }
     *     
     */
    public void setType(TlTypeType value) {
        this.type = value;
    }

    /**
     * Gets the value of the programID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProgramID() {
        return programID;
    }

    /**
     * Sets the value of the programID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProgramID(String value) {
        this.programID = value;
    }

    /**
     * Gets the value of the offset property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getOffset() {
        return offset;
    }

    /**
     * Sets the value of the offset property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setOffset(Float value) {
        this.offset = value;
    }

}