//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.03.14 um 12:34:23 PM CET 
//


package de.enflexit.ea.core.dataModel.deployment;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für AgentOperatingMode.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="AgentOperatingMode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Simulation"/>
 *     &lt;enumeration value="Testbed application - simulated"/>
 *     &lt;enumeration value="Testbed application - real"/>
 *     &lt;enumeration value="Real system"/>
 *     &lt;enumeration value="Real system - simulated IO"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "AgentOperatingMode")
@XmlEnum
public enum AgentOperatingMode {

    Simulation("Simulation"),
    @XmlEnumValue("Testbed application - simulated")
    TestBedSimulation("Testbed application - simulated"),
    @XmlEnumValue("Testbed application - real")
    TestBedReal("Testbed application - real"),
    @XmlEnumValue("Real system")
    RealSystem("Real system"),
    @XmlEnumValue("Real system - simulated IO")
    RealSystemSimulatedIO("Real system - simulated IO");
    private final String value;

    AgentOperatingMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AgentOperatingMode fromValue(String v) {
        for (AgentOperatingMode c: AgentOperatingMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
