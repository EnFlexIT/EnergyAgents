package de.enflexit.ea.topologies.pandaPower.psIngo;

import de.enflexit.ea.topologies.pandaPower.ColumnName;
import de.enflexit.ea.topologies.pandaPower.PandaPowerNamingMap;

/**
 * The Class PSIngoNamingMap.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class PSIngoNamingMap extends PandaPowerNamingMap {

	private static final long serialVersionUID = -13030255430950557L;

	public static final String UUID = "uuid";
	
	/**
	 * Instantiates a new PS ingo naming map.
	 */
	public PSIngoNamingMap() {
		super();
		this.customizeMapping();
	}
	/**
	 * Customizes the name mappings for PSIngo.
	 */
	private void customizeMapping() {
		this.put(ColumnName.Bus_Type, "psingo_node_type");
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.topologies.pandaPower.PandaPowerNamingMap#isAddNodeType(java.lang.String)
	 */
	@Override
	public boolean isAddNodeTypeToNetworkModel(String type) {
		
		// --- Known: HOUSECONNECTION, JOINT, BUSBAR ----- 
		if (type==null) {
			return false;
		} else if (type.toLowerCase().equals("HOUSECONNECTION".toLowerCase())==true) {
			return true;
		} else if (type.toLowerCase().equals("BUSBAR".toLowerCase())==true) {
			return true;
		} else if (type.toLowerCase().equals("JOINT".toLowerCase())==true) {
			return true;
		}
		return false;
	}
}
