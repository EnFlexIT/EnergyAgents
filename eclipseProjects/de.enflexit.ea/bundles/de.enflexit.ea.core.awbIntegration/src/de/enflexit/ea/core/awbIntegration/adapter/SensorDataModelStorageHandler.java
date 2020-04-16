package de.enflexit.ea.core.awbIntegration.adapter;

import java.util.TreeMap;
import java.util.Vector;

import org.awb.env.networkModel.DataModelNetworkElement;

import de.enflexit.eom.awb.adapter.EomDataModelAdapterOntology;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandlerOntology;
import de.enflexit.eom.awb.adapter.EomDataModelAdapterOntology.DataModelTab;
import energy.optionModel.ScheduleList;
import energy.persistence.ScheduleList_StorageHandler;

/**
 * The Class SensorDataModelStorageHandler.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class SensorDataModelStorageHandler extends EomDataModelStorageHandlerOntology {

	private static final int SCHEDULE_LIST_INDEX = 2;
	
	/**
	 * Instantiates a new sensor data model storage handler.
	 * @param eomDataModelAdapterOntology the eom data model adapter ontology
	 */
	public SensorDataModelStorageHandler(EomDataModelAdapterOntology eomDataModelAdapterOntology) {
		super(eomDataModelAdapterOntology);
	}

	/* (non-Javadoc)
	 * @see de.enflexit.eom.awb.EomDataModelStorageHandler#getDefaultEomModelType()
	 */
	@Override
	protected EomModelType getDefaultEomModelType() {
		return EomModelType.ScheduleList;
	}

	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.adapter.dataModel.AbstractDataModelStorageHandler#loadDataModel(org.awb.env.networkModel.DataModelNetworkElement)
	 */
	@Override
	public Object loadDataModel(DataModelNetworkElement networkElement) {
		
		Object dmTreeMap = null;
		if (networkElement.getDataModelStorageSettings()==null) {
			// --- Load from the old style structure ------
			dmTreeMap = this.loadDataModelFromOldStylePersistenceStructure(networkElement);
			this.setRequiresPersistenceUpdate(true);
			
		} else {
			// --- The regular case -----------------------
			dmTreeMap = super.loadDataModel(networkElement);
		}
		return convertToObjectArray(dmTreeMap);
	}
	
	/**
	 * Load data model from old style persistence structure.
	 *
	 * @param networkElement the network element
	 * @return the object
	 */
	private Object loadDataModelFromOldStylePersistenceStructure(DataModelNetworkElement networkElement) {
		
		if (networkElement.getDataModelBase64()==null) return null;
		
		Object eomDataModel = null;
		Object ontoDataModel = null;
		
		Vector<String> dataModelVector64Work = new Vector<>(networkElement.getDataModelBase64());
		if (dataModelVector64Work.size()==3) {
			// --- Get the ScheduleList instance --------------------
			String scheduleListBase64 = dataModelVector64Work.get(SCHEDULE_LIST_INDEX);
			ScheduleList_StorageHandler slsh = new ScheduleList_StorageHandler();
			eomDataModel = slsh.getScheduleListFromBase64String(scheduleListBase64);
			dataModelVector64Work.remove(SCHEDULE_LIST_INDEX);
		}
		
		// --- Get the instances of the ontology objects ------------
		DataModelNetworkElement tmpNetElement = this.createTemporaryNetworkElement(networkElement);
		tmpNetElement.setDataModelBase64(dataModelVector64Work);
		ontoDataModel = this.getOntologyStorageHandler().loadDataModel(tmpNetElement);
		
		TreeMap<String, Object> dmTreeMap = new TreeMap<>();
		dmTreeMap.put(DataModelTab.EOM_Model.toString(), eomDataModel);
		dmTreeMap.put(DataModelTab.Ontology_Model.toString(), ontoDataModel);
		return dmTreeMap;
	}
	
	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.adapter.dataModel.AbstractDataModelStorageHandler#saveDataModel(org.awb.env.networkModel.DataModelNetworkElement)
	 */
	@Override
	public TreeMap<String, String> saveDataModel(DataModelNetworkElement networkElement) {
		
		// --- Convert data model to TreeMap --------------
		Object sourceDataModel = networkElement.getDataModel();
		TreeMap<String, Object> treeDataMoel = convertToTreeMap(sourceDataModel);
		
		// --- Temporary set TreeMap data model -----------
		networkElement.setDataModel(treeDataMoel);
		TreeMap<String, String> storageSettings = super.saveDataModel(networkElement);
		
		// --- Revert data model --------------------------
		networkElement.setDataModel(sourceDataModel);
		
		return storageSettings;
	}
	

	// ----------------------------------------------------------------------------------
	// --- From here the static help methods to switch between array and TreeMap --------
	// ----------------------------------------------------------------------------------	
	/**
	 * Convert the specified TreeMap into to a object array, were at the last index position, the EOM (ScheduleList) model will be placed.
	 *
	 * @param treeMapInstance the TreeMap instance
	 * @return the object[]
	 */
	public static Object[] convertToObjectArray(Object treeMapInstance) {
	
		if (treeMapInstance instanceof TreeMap<?, ?>) {
			
			TreeMap<?, ?> dmTreeMap = (TreeMap<?, ?>) treeMapInstance;
			
			Object eomDataModel = dmTreeMap.get(DataModelTab.EOM_Model.toString());
			Object ontoDataModel = dmTreeMap.get(DataModelTab.Ontology_Model.toString());
			
			Object[] ontoArray = null;
			if (ontoDataModel!=null && ontoDataModel.getClass().isArray()==true) {
				ontoArray = (Object[]) ontoDataModel;
			}
			
			Object[] dmArray = new Object[SCHEDULE_LIST_INDEX+1];
			if (ontoArray!=null) {
				dmArray[0] = ontoArray[0];
				dmArray[1] = ontoArray[1];
			}
			dmArray[SCHEDULE_LIST_INDEX] = eomDataModel;
			return dmArray;
			
		}
		return null;
	}
	
	/**
	 * Convert the specified model into a TreeMap for storage handling.
	 * @return the tree map
	 */
	public static TreeMap<String, Object> convertToTreeMap(Object objectArrayInstance) {
		
		Object eomDataModel = null;
		Object ontoDataModel =  null;
		
		if (objectArrayInstance!=null && objectArrayInstance.getClass().isArray()==true) {
			
			Object[] dataModelArray = (Object[]) objectArrayInstance;
			
			Object[] ontoArray = new Object[SCHEDULE_LIST_INDEX];
			for (int i = 0; i < ontoArray.length; i++) {
				ontoArray[i] = dataModelArray[i];
			}
			ontoDataModel = ontoArray;
			
			if (dataModelArray.length==(SCHEDULE_LIST_INDEX+1) && dataModelArray[SCHEDULE_LIST_INDEX] instanceof ScheduleList) {
				eomDataModel = dataModelArray[SCHEDULE_LIST_INDEX];
			}
		}
			
		TreeMap<String, Object> dmTreeMap = new TreeMap<>();
		dmTreeMap.put(DataModelTab.EOM_Model.toString(), eomDataModel);
		dmTreeMap.put(DataModelTab.Ontology_Model.toString(), ontoDataModel);
		return dmTreeMap;
	}
	
}
