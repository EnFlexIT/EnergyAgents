//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.03.14 um 12:34:23 PM CET 
//


package de.enflexit.ea.core.dataModel.deployment;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für Certificate complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Certificate">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CertificateName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CertificateAlias" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CertificateBase64" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CertificatePath" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Certificate", propOrder = {
    "certificateName",
    "certificateAlias",
    "certificateBase64",
    "certificatePath"
})
public class Certificate
    implements Serializable
{

    private final static long serialVersionUID = 201404191434L;
    @XmlElement(name = "CertificateName", required = true)
    protected String certificateName;
    @XmlElement(name = "CertificateAlias", required = true)
    protected String certificateAlias;
    @XmlElement(name = "CertificateBase64", required = true)
    protected String certificateBase64;
    @XmlElement(name = "CertificatePath", required = true)
    protected String certificatePath;

    /**
     * Ruft den Wert der certificateName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCertificateName() {
        return certificateName;
    }

    /**
     * Legt den Wert der certificateName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCertificateName(String value) {
        this.certificateName = value;
    }

    /**
     * Ruft den Wert der certificateAlias-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCertificateAlias() {
        return certificateAlias;
    }

    /**
     * Legt den Wert der certificateAlias-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCertificateAlias(String value) {
        this.certificateAlias = value;
    }

    /**
     * Ruft den Wert der certificateBase64-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCertificateBase64() {
        return certificateBase64;
    }

    /**
     * Legt den Wert der certificateBase64-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCertificateBase64(String value) {
        this.certificateBase64 = value;
    }

    /**
     * Ruft den Wert der certificatePath-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCertificatePath() {
        return certificatePath;
    }

    /**
     * Legt den Wert der certificatePath-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCertificatePath(String value) {
        this.certificatePath = value;
    }

}
