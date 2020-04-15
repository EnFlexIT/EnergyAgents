package de.enflexit.ea.core.dataModel.deployment;
//
//Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
//Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
//Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
//Generiert: 2018.03.14 um 12:34:23 PM CET 
//

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
* <p>Java-Klasse für StartArgument complex type.
* 
* <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
* 
* <pre>
* &lt;complexType name="StartArgument">
*   &lt;complexContent>
*     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
*       &lt;sequence>
*         &lt;element name="OntologyMainClassReference" type="{http://www.w3.org/2001/XMLSchema}string"/>
*         &lt;element name="EncodedInstance" type="{http://www.w3.org/2001/XMLSchema}string"/>
*       &lt;/sequence>
*     &lt;/restriction>
*   &lt;/complexContent>
* &lt;/complexType>
* </pre>
* 
* 
*/
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StartArgument", propOrder = {
 "ontologyMainClassReference",
 "encodedInstance"
})
public class StartArgument
 implements Serializable
{

 private final static long serialVersionUID = 201404191434L;
 @XmlElement(name = "OntologyMainClassReference", required = true)
 protected String ontologyMainClassReference;
 @XmlElement(name = "EncodedInstance", required = true)
 protected String encodedInstance;

 /**
  * Ruft den Wert der ontologyMainClassReference-Eigenschaft ab.
  * 
  * @return
  *     possible object is
  *     {@link String }
  *     
  */
 public String getOntologyMainClassReference() {
     return ontologyMainClassReference;
 }

 /**
  * Legt den Wert der ontologyMainClassReference-Eigenschaft fest.
  * 
  * @param value
  *     allowed object is
  *     {@link String }
  *     
  */
 public void setOntologyMainClassReference(String value) {
     this.ontologyMainClassReference = value;
 }

 /**
  * Ruft den Wert der encodedInstance-Eigenschaft ab.
  * 
  * @return
  *     possible object is
  *     {@link String }
  *     
  */
 public String getEncodedInstance() {
     return encodedInstance;
 }

 /**
  * Legt den Wert der encodedInstance-Eigenschaft fest.
  * 
  * @param value
  *     allowed object is
  *     {@link String }
  *     
  */
 public void setEncodedInstance(String value) {
     this.encodedInstance = value;
 }

}
