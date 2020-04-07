package hygrid.env;

import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import jade.util.leap.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GraphElementLayoutSettings", propOrder = {
    "domain",
    "settingsTreeMap"
})
public class GraphElementLayoutSettingsPersistenceTreeMap implements Serializable, Comparable<GraphElementLayoutSettingsPersistenceTreeMap> {

	private static final long serialVersionUID = -4640124533716849798L;

	@XmlAttribute(name = "domain")
	private String domain;
	private TreeMap<String, String> settingsTreeMap;
	
	/**
	 * Gets the domain.
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}
	
	/**
	 * Sets the domain.
	 * @param domain the new domain
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	/**
	 * Gets the settings tree map.
	 * @return the settings tree map
	 */
	public TreeMap<String, String> getSettingsTreeMap() {
		return settingsTreeMap;
	}
	
	/**
	 * Sets the settings tree map.
	 * @param settingsTreeMap the settings tree map
	 */
	public void setSettingsTreeMap(TreeMap<String, String> settingsTreeMap) {
		this.settingsTreeMap = settingsTreeMap;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(GraphElementLayoutSettingsPersistenceTreeMap otherSettings) {
		return this.getDomain().compareTo(otherSettings.getDomain());
	}
	
}
