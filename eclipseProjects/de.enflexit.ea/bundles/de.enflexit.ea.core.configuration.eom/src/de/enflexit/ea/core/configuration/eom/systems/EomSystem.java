package de.enflexit.ea.core.configuration.eom.systems;

import java.util.TreeMap;
import java.util.Vector;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.awb.env.networkModel.DataModelNetworkElement;

import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler.EomModelType;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler.EomStorageLocation;;


/**
 * The Class EomSystem describes a single system that is part of a configuration.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EomSystem", propOrder = {
    "id",
    "storageSettings",
    "dmBase64"
})
public class EomSystem implements DataModelNetworkElement {

	private String id;
	
	@XmlTransient
	private Object eomDataModel;
	private TreeMap<String, String> storageSettings;
	private Vector<String> dmBase64;
	
	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.DataModelNetworkElement#getId()
	 */
	@Override
	public String getId() {
		return this.id;
	}
	/**
	 * Sets the ID of the EomSystem.
	 * @param id the new id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Returns the current EomModelType.
	 * @return the eom model type
	 */
	public EomModelType getEomModelType() {
		
		EomModelType eomModelType = null;
		
    	String eomModelTypeString = this.getDataModelStorageSettings().get(EomDataModelStorageHandler.EOM_SETTING_EOM_MODEL_TYPE);
    	if (eomModelTypeString==null) {
    		eomModelType = EomModelType.TechnicalSystem;
    		this.getDataModelStorageSettings().put(EomDataModelStorageHandler.EOM_SETTING_EOM_MODEL_TYPE, eomModelType.name());
    	} else {
    		eomModelType = EomModelType.valueOf(eomModelTypeString);
    	}
    	return eomModelType;
	}
	/**
	 * Sets the EomModelType.
	 * @param eomModelType the new EomModelType
	 */
	public void setEomModelType(EomModelType eomModelType) {
		this.getDataModelStorageSettings().put(EomDataModelStorageHandler.EOM_SETTING_EOM_MODEL_TYPE, eomModelType.name());
	}
	
	/**
	 * Returns the storage location of the EOM model.
	 * @return the storage location
	 */
	public EomStorageLocation getStorageLocation() {
		
		EomStorageLocation eomStorageLocation = null;
		
		String eomStorageLocationString = this.getDataModelStorageSettings().get(EomDataModelStorageHandler.EOM_SETTING_STORAGE_LOCATION);
		if (eomStorageLocationString==null) {
			eomStorageLocation = EomStorageLocation.File;
    		this.getDataModelStorageSettings().put(EomDataModelStorageHandler.EOM_SETTING_STORAGE_LOCATION, eomStorageLocation.name());
    	} else {
    		eomStorageLocation = EomStorageLocation.valueOf(eomStorageLocationString);
    	}
		return eomStorageLocation;
	}
	/**
	 * Sets the storage location of the EOM Model.
	 * @param eomStorageLocation the new storage location
	 */
	public void setStorageLocation(EomStorageLocation eomStorageLocation) {
		this.getDataModelStorageSettings().put(EomDataModelStorageHandler.EOM_SETTING_STORAGE_LOCATION, eomStorageLocation.name());
	}

	/**
	 * Returns the specific storage information as a string.
	 * @return the storage info
	 */
	public String getStorageInfo() {
		
		String storageInfo = " - ";
		
		EomStorageLocation storageLocation = this.getStorageLocation(); 
		if (storageLocation!=null) {
			switch (storageLocation) {
			case File:
				storageInfo = this.getDataModelStorageSettings().get(EomDataModelStorageHandler.EOM_SETTING_EOM_FILE_LOCATION);
				break;
			case BundleLocation:
				String symbolicBundleName  = getDataModelStorageSettings().get(EomDataModelStorageHandler.EOM_SETTING_BUNDLE_MODEL_SYMBOLIC_BUNDLE_NAME);
				String bundleFileReference = getDataModelStorageSettings().get(EomDataModelStorageHandler.EOM_SETTING_BUNDLE_MODEL_FILE_REFERENCE);
				if (symbolicBundleName!=null && bundleFileReference!=null) {
					storageInfo = "(" + symbolicBundleName + ") " + bundleFileReference;
				}
				break;
			case Database:
				String dbID = this.getDataModelStorageSettings().get(EomDataModelStorageHandler.EOM_SETTING_DATABASE_ID);
				storageInfo = "DB-ID: " + (dbID!=null ? dbID : "?"); 
				break;
			default:
				// --- Nothing to do here -------
				break;
			}
		}
		return storageInfo;
	}
	/**
	 * Clears the storage info from the current setting.
	 */
	public void clearStorageInfo() {
		this.getDataModelStorageSettings().remove(EomDataModelStorageHandler.EOM_SETTING_EOM_FILE_LOCATION);
		this.getDataModelStorageSettings().remove(EomDataModelStorageHandler.EOM_SETTING_DATABASE_ID);
	}
	
	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.DataModelNetworkElement#getDataModel()
	 */
	@Override
	public Object getDataModel() {
		return eomDataModel;
	}
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.DataModelNetworkElement#setDataModel(java.lang.Object)
	 */
	@Override
	public void setDataModel(Object dataModel) {
		this.eomDataModel = dataModel;
	}

	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.DataModelNetworkElement#getDataModelStorageSettings()
	 */
	@Override
	public TreeMap<String, String> getDataModelStorageSettings() {
		if (storageSettings==null) {
			storageSettings = new TreeMap<>();
		}
		return storageSettings;
	}
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.DataModelNetworkElement#setDataModelStorageSettings(java.util.TreeMap)
	 */
	@Override
	public void setDataModelStorageSettings(TreeMap<String, String> dataModelStorageSettings) {
		this.storageSettings = dataModelStorageSettings;
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.DataModelNetworkElement#getDataModelBase64()
	 */
	@Override
	public Vector<String> getDataModelBase64() {
		if (dmBase64==null) {
			dmBase64 = new Vector<>();
		}
		return dmBase64;
	}
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.DataModelNetworkElement#setDataModelBase64(java.util.Vector)
	 */
	@Override
	public void setDataModelBase64(Vector<String> dataModelBase64) {
		this.dmBase64 = dataModelBase64;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getId();
	}
	
}
