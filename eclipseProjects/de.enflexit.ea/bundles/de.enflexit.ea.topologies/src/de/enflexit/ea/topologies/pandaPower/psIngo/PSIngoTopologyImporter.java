package de.enflexit.ea.topologies.pandaPower.psIngo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.ea.topologies.pandaPower.PandaPowerNamingMap;
import de.enflexit.ea.topologies.pandaPower.PandaPowerNamingMap.NamingMap;
import de.enflexit.ea.topologies.pandaPower.PandaPowerTopologyImporter;

/**
 * The Class PSIngoTopologyImporter.
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class PSIngoTopologyImporter extends PandaPowerTopologyImporter {

	/** The file filter list. */
	private List<FileFilter> fileFilterList;

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.persistence.AbstractNetworkModelFileImporter#getFileFilters()
	 */
	@Override
	public List<FileFilter> getFileFilters() {
		if (fileFilterList==null) {
			fileFilterList = new ArrayList<>();
			fileFilterList.add(this.createFileFilter(".json", "PSIngo Eletrical Network (PandaPower json file)"));
		}
		return fileFilterList;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.topologies.pandaPower.PandaPowerTopologyImporter#setPandaPowerNamingMapToStaticMap()
	 */
	@Override
	protected void setPandaPowerNamingMapToStaticMap() {
		PandaPowerNamingMap.setPandaPowerNamingMap(NamingMap.PandaPowerOfPSIingo);
	}
	
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.topologies.pandaPower.PandaPowerTopologyImporter#addAdditionalNodeProperties(java.util.HashMap, org.awb.env.networkModel.NetworkComponent)
	 */
	@Override
	protected void addAdditionalNodeProperties(HashMap<String, Object> busRowHashMap, NetworkComponent newNC) {
		String uuid = (String) busRowHashMap.get(PSIngoNamingMap.UUID);
		newNC.putAlternativeID(PSIngoNamingMap.UUID, uuid);
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.topologies.pandaPower.PandaPowerTopologyImporter#addAdditionalLineProperties(java.util.HashMap, org.awb.env.networkModel.NetworkComponent)
	 */
	@Override
	protected void addAdditionalLineProperties(HashMap<String, Object> lineRowHashMap, NetworkComponent newNC) {
		String uuid = (String) lineRowHashMap.get(PSIngoNamingMap.UUID);
		newNC.putAlternativeID(PSIngoNamingMap.UUID, uuid);
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.topologies.pandaPower.PandaPowerTopologyImporter#addAdditionalSwitchProperties(java.util.HashMap, org.awb.env.networkModel.NetworkComponent)
	 */
	@Override
	protected void addAdditionalSwitchProperties(HashMap<String, Object> switchRowHashMap, NetworkComponent newNC) {
		String uuid = (String) switchRowHashMap.get(PSIngoNamingMap.UUID);
		newNC.putAlternativeID(PSIngoNamingMap.UUID, uuid);
	}
	
}
