//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0-b170531.0717 
//         See <a href="https://jaxb.java.net/">https://jaxb.java.net/</a> 
//         Any modifications to this file will be lost upon recompilation of the source schema. 
//         Generated on: 2018.01.03 at 06:19:16 PM CET 
//


package sumobindings;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for laneType complex type.
 * 
 * <p>The following schema fragment specifies the expected         content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="laneType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="neigh" type="{}neighType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="param" type="{}paramType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="index" use="required" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" /&gt;
 *       &lt;attribute name="allow" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="disallow" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="prefer" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="speed" use="required" type="{}positiveFloatType" /&gt;
 *       &lt;attribute name="length" use="required" type="{}positiveFloatType" /&gt;
 *       &lt;attribute name="endOffset" type="{}positiveFloatType" /&gt;
 *       &lt;attribute name="width" type="{}nonNegativeFloatType" /&gt;
 *       &lt;attribute name="acceleration" type="{}boolType" /&gt;
 *       &lt;attribute name="shape" use="required" type="{}shapeTypeTwo" /&gt;
 *       &lt;attribute name="customShape" type="{}boolType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "laneType", propOrder = {
    "neigh",
    "param"
})
public class LaneType {

    protected List<NeighType> neigh;
    protected List<ParamType> param;
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "index", required = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger index;
    @XmlAttribute(name = "allow")
    protected String allow;
    @XmlAttribute(name = "disallow")
    protected String disallow;
    @XmlAttribute(name = "prefer")
    protected String prefer;
    @XmlAttribute(name = "speed", required = true)
    protected float speed;
    @XmlAttribute(name = "length", required = true)
    protected float length;
    @XmlAttribute(name = "endOffset")
    protected Float endOffset;
    @XmlAttribute(name = "width")
    protected Float width;
    @XmlAttribute(name = "acceleration")
    protected String acceleration;
    @XmlAttribute(name = "shape", required = true)
    protected String shape;
    @XmlAttribute(name = "customShape")
    protected String customShape;

    /**
     * Gets the value of the neigh property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the neigh property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNeigh().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NeighType }
     * 
     * 
     */
    public List<NeighType> getNeigh() {
        if (neigh == null) {
            neigh = new ArrayList<NeighType>();
        }
        return this.neigh;
    }

    /**
     * Gets the value of the param property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the param property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParam().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ParamType }
     * 
     * 
     */
    public List<ParamType> getParam() {
        if (param == null) {
            param = new ArrayList<ParamType>();
        }
        return this.param;
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
     * Gets the value of the index property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getIndex() {
        return index;
    }

    /**
     * Sets the value of the index property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setIndex(BigInteger value) {
        this.index = value;
    }

    /**
     * Gets the value of the allow property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAllow() {
        return allow;
    }

    /**
     * Sets the value of the allow property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAllow(String value) {
        this.allow = value;
    }

    /**
     * Gets the value of the disallow property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisallow() {
        return disallow;
    }

    /**
     * Sets the value of the disallow property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisallow(String value) {
        this.disallow = value;
    }

    /**
     * Gets the value of the prefer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrefer() {
        return prefer;
    }

    /**
     * Sets the value of the prefer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrefer(String value) {
        this.prefer = value;
    }

    /**
     * Gets the value of the speed property.
     * 
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Sets the value of the speed property.
     * 
     */
    public void setSpeed(float value) {
        this.speed = value;
    }

    /**
     * Gets the value of the length property.
     * 
     */
    public float getLength() {
        return length;
    }

    /**
     * Sets the value of the length property.
     * 
     */
    public void setLength(float value) {
        this.length = value;
    }

    /**
     * Gets the value of the endOffset property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getEndOffset() {
        return endOffset;
    }

    /**
     * Sets the value of the endOffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setEndOffset(Float value) {
        this.endOffset = value;
    }

    /**
     * Gets the value of the width property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setWidth(Float value) {
        this.width = value;
    }

    /**
     * Gets the value of the acceleration property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAcceleration() {
        return acceleration;
    }

    /**
     * Sets the value of the acceleration property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAcceleration(String value) {
        this.acceleration = value;
    }

    /**
     * Gets the value of the shape property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShape() {
        return shape;
    }

    /**
     * Sets the value of the shape property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShape(String value) {
        this.shape = value;
    }

    /**
     * Gets the value of the customShape property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomShape() {
        return customShape;
    }

    /**
     * Sets the value of the customShape property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomShape(String value) {
        this.customShape = value;
    }

}