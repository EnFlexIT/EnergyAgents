//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.03.14 um 12:34:23 PM CET 
//


package de.enflexit.ea.core.dataModel.deployment;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für KeyStore complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="KeyStore">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="KeyStoreName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Password" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Alias" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="FullName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Organization" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="OrginazationalUnit" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CityOrLocality" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="StateOrProvince" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CoutryCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Validity" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Path" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CertificateName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CertificateValidity" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KeyStore", propOrder = {
    "keyStoreName",
    "password",
    "alias",
    "fullName",
    "organization",
    "orginazationalUnit",
    "cityOrLocality",
    "stateOrProvince",
    "coutryCode",
    "validity",
    "path",
    "certificateName",
    "certificateValidity"
})
public class KeyStore
    implements Serializable
{

    private final static long serialVersionUID = 201404191434L;
    @XmlElement(name = "KeyStoreName", required = true)
    protected String keyStoreName;
    @XmlElement(name = "Password", required = true)
    protected String password;
    @XmlElement(name = "Alias", required = true)
    protected String alias;
    @XmlElement(name = "FullName", required = true)
    protected String fullName;
    @XmlElement(name = "Organization", required = true)
    protected String organization;
    @XmlElement(name = "OrginazationalUnit", required = true)
    protected String orginazationalUnit;
    @XmlElement(name = "CityOrLocality", required = true)
    protected String cityOrLocality;
    @XmlElement(name = "StateOrProvince", required = true)
    protected String stateOrProvince;
    @XmlElement(name = "CoutryCode", required = true)
    protected String coutryCode;
    @XmlElement(name = "Validity", required = true)
    protected String validity;
    @XmlElement(name = "Path", required = true)
    protected String path;
    @XmlElement(name = "CertificateName", required = true)
    protected String certificateName;
    @XmlElement(name = "CertificateValidity", required = true)
    protected String certificateValidity;

    /**
     * Ruft den Wert der keyStoreName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeyStoreName() {
        return keyStoreName;
    }

    /**
     * Legt den Wert der keyStoreName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeyStoreName(String value) {
        this.keyStoreName = value;
    }

    /**
     * Ruft den Wert der password-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassword() {
        return password;
    }

    /**
     * Legt den Wert der password-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(String value) {
        this.password = value;
    }

    /**
     * Ruft den Wert der alias-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Legt den Wert der alias-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlias(String value) {
        this.alias = value;
    }

    /**
     * Ruft den Wert der fullName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Legt den Wert der fullName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFullName(String value) {
        this.fullName = value;
    }

    /**
     * Ruft den Wert der organization-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrganization() {
        return organization;
    }

    /**
     * Legt den Wert der organization-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrganization(String value) {
        this.organization = value;
    }

    /**
     * Ruft den Wert der orginazationalUnit-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrginazationalUnit() {
        return orginazationalUnit;
    }

    /**
     * Legt den Wert der orginazationalUnit-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrginazationalUnit(String value) {
        this.orginazationalUnit = value;
    }

    /**
     * Ruft den Wert der cityOrLocality-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCityOrLocality() {
        return cityOrLocality;
    }

    /**
     * Legt den Wert der cityOrLocality-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCityOrLocality(String value) {
        this.cityOrLocality = value;
    }

    /**
     * Ruft den Wert der stateOrProvince-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStateOrProvince() {
        return stateOrProvince;
    }

    /**
     * Legt den Wert der stateOrProvince-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStateOrProvince(String value) {
        this.stateOrProvince = value;
    }

    /**
     * Ruft den Wert der coutryCode-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCoutryCode() {
        return coutryCode;
    }

    /**
     * Legt den Wert der coutryCode-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCoutryCode(String value) {
        this.coutryCode = value;
    }

    /**
     * Ruft den Wert der validity-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValidity() {
        return validity;
    }

    /**
     * Legt den Wert der validity-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValidity(String value) {
        this.validity = value;
    }

    /**
     * Ruft den Wert der path-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPath() {
        return path;
    }

    /**
     * Legt den Wert der path-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPath(String value) {
        this.path = value;
    }

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
     * Ruft den Wert der certificateValidity-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCertificateValidity() {
        return certificateValidity;
    }

    /**
     * Legt den Wert der certificateValidity-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCertificateValidity(String value) {
        this.certificateValidity = value;
    }

}
