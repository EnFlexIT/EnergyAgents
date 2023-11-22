package de.enflexit.ea.core.configuration.eom.systems;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import de.enflexit.ea.core.configuration.eom.BundleHelper;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler;

/**
 * The Class SystemConfiguration.
 * 
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SystemConfiguration", propOrder = {
    "eomSystemList",
    "systemBlueprintList"
})
public class SystemConfiguration {

	private List<EomSystem> eomSystemList;
	private List<SystemBlueprint> systemBlueprintList;
	
	
	// ------------------------------------------------------------------------
	// --- From here, handling of EomSystem ----------------------------------- 
	// ------------------------------------------------------------------------
	/**
	 * Returns the list of EOM systems.
	 * @return the EOM system list
	 */
	public List<EomSystem> getEomSystemList() {
		if (eomSystemList==null) {
			eomSystemList = new ArrayList<>();
		}
		return eomSystemList;
	}
	/**
	 * Creates a new EomSystem with a unique ID.
	 * @return the new EomSystem
	 */
	public EomSystem createEomSystem() {
		
		EomSystem eomSystem = new EomSystem();
		
		// --- Find a new unique ID -----------------------
		String baseID = "eom ";
		int newIDCounter = 1;
		String newID = baseID + newIDCounter;
		while (this.getEomSystem(newID)!=null) {
			newIDCounter++;
			newID = baseID + newIDCounter;
		}
		eomSystem.setId(newID);
		return eomSystem;
	}
	/**
	 * Return the EomSystem with the specified ID or <code>null</code> if nothing was found.
	 *
	 * @param id the id
	 * @return the EomSystem found
	 */
	public EomSystem getEomSystem(String id) {
		
		for (EomSystem eomSystem : this.getEomSystemList()) {
			if (eomSystem.getId().equals(id)==true) {
				return eomSystem;
			}
		}
		return null;
	}
	/**
	 * Renames the EomSystem.
	 *
	 * @param oldID the old ID
	 * @param newID the new ID
	 */
	public void renameEomSystem(String oldID, String newID) {
		
		EomSystem eomSystem = this.getEomSystem(oldID);
		if (eomSystem!=null) {
			eomSystem.setId(newID);
		}

		// --- Apply new ID in SystemBlueprints ----------- 
		for (SystemBlueprint systemBlueprint : this.getSystemBlueprintList()) {
			int pos = systemBlueprint.getEomSystemIdList().indexOf(oldID);
			if (pos!=-1) {
				systemBlueprint.getEomSystemIdList().set(pos, newID);
			}
		}
	}
	/**
	 * Will removes the specified EomSystem out of this configuration.
	 *
	 * @param eomSystem the EomSystem to remove
	 * @return true, if successful
	 */
	public boolean removeEomSystem(EomSystem eomSystem) {
		
		if (eomSystem==null) return false;
		boolean success = this.getEomSystemList().remove(eomSystem);
		// --- Remove that ID from all blueprints ---------
		for (SystemBlueprint systemBlueprint : this.getSystemBlueprintList()) {
			systemBlueprint.getEomSystemIdList().remove(eomSystem.getId());
		}
		return success;
	}
	/**
	 * Loads the actual EOM models to the local {@link EomSystem}s.
	 */
	public void loadEomSystems() {
		
		EomDataModelStorageHandler storageHandler = new EomDataModelStorageHandler(null);
		for (EomSystem eomSystem : this.getEomSystemList()) {
			try {
				Object eomModel = storageHandler.loadDataModel(eomSystem);
				eomSystem.setDataModel(eomModel);
			} catch (Exception ex) {
				System.err.println("[" + this.getClass().getSimpleName() + "] Error while loading EOM model '" + eomSystem.getId() + "'");
				ex.printStackTrace();
			}
		}
	}
	
	
	// ------------------------------------------------------------------------
	// --- From here, handling of SystemBlueprints ---------------------------- 
	// ------------------------------------------------------------------------
	/**
	 * Returns the system configuration list.
	 * @return the system configuration list
	 */
	public List<SystemBlueprint> getSystemBlueprintList() {
		if (systemBlueprintList==null) {
			systemBlueprintList = new ArrayList<>();
		}
		return systemBlueprintList;
	}

	/**
	 * Creates a new SystemBlueprint with an unique ID.
	 * @return the system blueprint
	 */
	public SystemBlueprint createSystemBlueprint() {
		
		SystemBlueprint systemBlueprint = new SystemBlueprint();
		
		// --- Find a new unique ID -----------------------
		String baseID = "BluePrint ";
		int newIDCounter = 1;
		String newID = baseID + newIDCounter;
		while (this.getSystemBlueprint(newID)!=null) {
			newIDCounter++;
			newID = baseID + newIDCounter;
		}
		systemBlueprint.setID(newID);
		return systemBlueprint;
	}
	/**
	 * Return the EomSystem with the specified ID or <code>null</code> if nothing was found.
	 *
	 * @param id the id
	 * @return the EomSystem found
	 */
	public SystemBlueprint getSystemBlueprint(String id) {
		
		for (SystemBlueprint systemBlueprint : this.getSystemBlueprintList()) {
			if (systemBlueprint.getID().equals(id)==true) {
				return systemBlueprint;
			}
		}
		return null;
	}
	/**
	 * Will removes the specified SystemBlueprint out of this configuration.
	 *
	 * @param systemBlueprint the system blueprint
	 * @return true, if successful
	 */
	public boolean removeSystemBlueprint(SystemBlueprint systemBlueprint) {
		if (systemBlueprint==null) return false;
		return this.getSystemBlueprintList().remove(systemBlueprint);
	}
	
	
	// ------------------------------------------------------------------------
	// --- From here, (static) methods to load/save SystemConfiguration's ----- 
	// ------------------------------------------------------------------------
	/**
	 * Saves the current setup configuration.
	 */
	public void save() {
		SystemConfiguration.save(this, BundleHelper.getSystemConfigurationFile());
	}
	
	/**
	 * Returns the file for the SystemConfiguration.
	 * @return the system configuration file
	 */
	public static File getSystemConfigurationFile() {
		return BundleHelper.getSystemConfigurationFile();
	}
	/**
	 * Saves the specified setup configuration.
	 *
	 * @param sysConfig the sys config
	 * @param file the file
	 */
	public static boolean save(SystemConfiguration sysConfig, File file) {
		
		boolean success = false;
		
		FileWriter fileWriter = null;
		try {
			JAXBContext pc = JAXBContext.newInstance(SystemConfiguration.class);
			Marshaller pm = pc.createMarshaller();
			pm.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			pm.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			// --- Write instance to xml-File ---------
			fileWriter = new FileWriter(file);
			pm.marshal(sysConfig, fileWriter);
			success = true;
			
		} catch (JAXBException | IOException ex) {
			ex.printStackTrace();
		} finally {
			if (fileWriter!=null) {
				try {
					fileWriter.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		return success;
	}
	/**
	 * Loads the SystemConfiguration from the .
	 *
	 * @param file the file
	 * @return the system configuration
	 */
	public static SystemConfiguration load(File file) {
		
		if (file.exists()==false) return null;
		
		SystemConfiguration sysConfig = null;
		
		InputStream inputStream = null;
		InputStreamReader isReader = null;
		try {
			JAXBContext context = JAXBContext.newInstance(SystemConfiguration.class);
			Unmarshaller unMarsh = context.createUnmarshaller();
			
			inputStream = new FileInputStream(file);
			isReader = new InputStreamReader(inputStream, "UTF-8");
			sysConfig = (SystemConfiguration) unMarsh.unmarshal(isReader);
			sysConfig.loadEomSystems();
			
		} catch (JAXBException | IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (isReader!=null) isReader.close();
				if (inputStream!=null) inputStream.close();
			} catch (IOException ioEx) {
				ioEx.printStackTrace();
			}	
		}
		return sysConfig;
	}

}
