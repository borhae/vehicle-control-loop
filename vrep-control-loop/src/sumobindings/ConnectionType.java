//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0-b170531.0717 
//         See <a href="https://jaxb.java.net/">https://jaxb.java.net/</a> 
//         Any modifications to this file will be lost upon recompilation of the source schema. 
//         Generated on: 2018.01.03 at 06:19:16 PM CET 
//


package sumobindings;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for connectionType complex type.
 * 
 * <p>The following schema fragment specifies the expected         content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="connectionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="from" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="to" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="fromLane" use="required" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" /&gt;
 *       &lt;attribute name="toLane" use="required" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" /&gt;
 *       &lt;attribute name="pass" type="{}boolType" /&gt;
 *       &lt;attribute name="keepClear" type="{}boolType" /&gt;
 *       &lt;attribute name="contPos" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *       &lt;attribute name="visibility" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *       &lt;attribute name="speed" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *       &lt;attribute name="shape" type="{}shapeType" /&gt;
 *       &lt;attribute name="via" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="tl" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="linkIndex" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *       &lt;attribute name="dir" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="s"/&gt;
 *             &lt;enumeration value="t"/&gt;
 *             &lt;enumeration value="T"/&gt;
 *             &lt;enumeration value="l"/&gt;
 *             &lt;enumeration value="r"/&gt;
 *             &lt;enumeration value="L"/&gt;
 *             &lt;enumeration value="R"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="state" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="M"/&gt;
 *             &lt;enumeration value="m"/&gt;
 *             &lt;enumeration value="o"/&gt;
 *             &lt;enumeration value="="/&gt;
 *             &lt;enumeration value="-"/&gt;
 *             &lt;enumeration value="s"/&gt;
 *             &lt;enumeration value="w"/&gt;
 *             &lt;enumeration value="Z"/&gt;
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
@XmlType(name = "connectionType")
public class ConnectionType {

    @XmlAttribute(name = "from", required = true)
    protected String from;
    @XmlAttribute(name = "to", required = true)
    protected String to;
    @XmlAttribute(name = "fromLane", required = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger fromLane;
    @XmlAttribute(name = "toLane", required = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger toLane;
    @XmlAttribute(name = "pass")
    protected String pass;
    @XmlAttribute(name = "keepClear")
    protected String keepClear;
    @XmlAttribute(name = "contPos")
    protected Float contPos;
    @XmlAttribute(name = "visibility")
    protected Float visibility;
    @XmlAttribute(name = "speed")
    protected Float speed;
    @XmlAttribute(name = "shape")
    protected String shape;
    @XmlAttribute(name = "via")
    protected String via;
    @XmlAttribute(name = "tl")
    protected String tl;
    @XmlAttribute(name = "linkIndex")
    protected BigInteger linkIndex;
    @XmlAttribute(name = "dir", required = true)
    protected String dir;
    @XmlAttribute(name = "state", required = true)
    protected String state;

    /**
     * Gets the value of the from property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFrom(String value) {
        this.from = value;
    }

    /**
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTo(String value) {
        this.to = value;
    }

    /**
     * Gets the value of the fromLane property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getFromLane() {
        return fromLane;
    }

    /**
     * Sets the value of the fromLane property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setFromLane(BigInteger value) {
        this.fromLane = value;
    }

    /**
     * Gets the value of the toLane property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getToLane() {
        return toLane;
    }

    /**
     * Sets the value of the toLane property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setToLane(BigInteger value) {
        this.toLane = value;
    }

    /**
     * Gets the value of the pass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPass() {
        return pass;
    }

    /**
     * Sets the value of the pass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPass(String value) {
        this.pass = value;
    }

    /**
     * Gets the value of the keepClear property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeepClear() {
        return keepClear;
    }

    /**
     * Sets the value of the keepClear property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeepClear(String value) {
        this.keepClear = value;
    }

    /**
     * Gets the value of the contPos property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getContPos() {
        return contPos;
    }

    /**
     * Sets the value of the contPos property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setContPos(Float value) {
        this.contPos = value;
    }

    /**
     * Gets the value of the visibility property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getVisibility() {
        return visibility;
    }

    /**
     * Sets the value of the visibility property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setVisibility(Float value) {
        this.visibility = value;
    }

    /**
     * Gets the value of the speed property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getSpeed() {
        return speed;
    }

    /**
     * Sets the value of the speed property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setSpeed(Float value) {
        this.speed = value;
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
     * Gets the value of the via property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVia() {
        return via;
    }

    /**
     * Sets the value of the via property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVia(String value) {
        this.via = value;
    }

    /**
     * Gets the value of the tl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTl() {
        return tl;
    }

    /**
     * Sets the value of the tl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTl(String value) {
        this.tl = value;
    }

    /**
     * Gets the value of the linkIndex property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getLinkIndex() {
        return linkIndex;
    }

    /**
     * Sets the value of the linkIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setLinkIndex(BigInteger value) {
        this.linkIndex = value;
    }

    /**
     * Gets the value of the dir property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDir() {
        return dir;
    }

    /**
     * Sets the value of the dir property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDir(String value) {
        this.dir = value;
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