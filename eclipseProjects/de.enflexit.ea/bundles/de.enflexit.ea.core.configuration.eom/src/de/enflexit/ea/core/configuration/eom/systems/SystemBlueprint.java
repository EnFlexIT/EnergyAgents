package de.enflexit.ea.core.configuration.eom.systems;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * The Class SystemBlueprint describes a single configuration of EOM systems.
 * 
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SystemBlueprint", propOrder = {
    "id",
    "description",
    "eomSystemID"
})
public class SystemBlueprint {

	private String id;
	private String description;
	private List<String> eomSystemID;

	
	/**
	 * Returns the id of the system configuration.
	 * @return the id
	 */
	public String getID() {
		return id;
	}
	/**
	 * Sets the id.
	 * @param id the new id
	 */
	public void setID(String id) {
		this.id = id;
	}

	/**
	 * Returns the description of the system configuration.
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * Sets the description.
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the list of ID's of the corresponding {@link EomSystem}.
	 * @return the eom system ID
	 */
	public List<String> getEomSystemIdList() {
		if (eomSystemID==null) {
			eomSystemID = new ArrayList<>();
		}
		return eomSystemID;
	}
	
}
