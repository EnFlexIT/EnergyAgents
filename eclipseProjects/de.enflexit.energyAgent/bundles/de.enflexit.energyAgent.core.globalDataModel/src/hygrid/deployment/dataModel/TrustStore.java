//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.03.14 um 12:34:23 PM CET 
//


package hygrid.deployment.dataModel;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für TrustStore complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="TrustStore">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TrustStoreName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Password" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="TrustedCertificates" type="{http://www.dawis.wiwi.uni-due.de/HyGridAgentConfiguration}Certificate"/>
 *         &lt;element name="Path" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TrustStore", propOrder = {
    "trustStoreName",
    "password",
    "trustedCertificates",
    "path"
})
public class TrustStore
    implements Serializable
{

    private final static long serialVersionUID = 201404191434L;
    @XmlElement(name = "TrustStoreName", required = true)
    protected String trustStoreName;
    @XmlElement(name = "Password", required = true)
    protected String password;
    @XmlElement(name = "TrustedCertificates", required = true)
    protected Certificate trustedCertificates;
    @XmlElement(name = "Path", required = true)
    protected String path;

    /**
     * Ruft den Wert der trustStoreName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTrustStoreName() {
        return trustStoreName;
    }

    /**
     * Legt den Wert der trustStoreName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTrustStoreName(String value) {
        this.trustStoreName = value;
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
     * Ruft den Wert der trustedCertificates-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Certificate }
     *     
     */
    public Certificate getTrustedCertificates() {
        return trustedCertificates;
    }

    /**
     * Legt den Wert der trustedCertificates-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Certificate }
     *     
     */
    public void setTrustedCertificates(Certificate value) {
        this.trustedCertificates = value;
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

}
