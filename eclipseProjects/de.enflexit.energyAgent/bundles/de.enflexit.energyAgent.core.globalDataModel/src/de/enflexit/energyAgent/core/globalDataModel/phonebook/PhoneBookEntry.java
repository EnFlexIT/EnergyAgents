package de.enflexit.energyAgent.core.globalDataModel.phonebook;

import java.io.Serializable;
import java.util.Vector;

import jade.core.AID;

// TODO: Auto-generated Javadoc
/**
 * This class defines the set of data that is kept in the phone book about an agent. 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class PhoneBookEntry implements Serializable {
	
	private static final long serialVersionUID = -1340574290046087999L;
	
	private AID aid;
	private boolean controllable;
	private String componentType;
	private Vector<String> mtpAddresses;

	/**
	 * Gets the aid.
	 * @return the aid
	 */
	public AID getAID() {
		// --- Restore MTP addresses if necessary ---------------------------------------
		if (aid.getAddressesArray().length<this.getMtpAddresses().size()) {
			for (String mtpAddress : this.getMtpAddresses()) {
				aid.addAddresses(mtpAddress);
			}
			
		}
		return aid;
	}
	/**
	 * Sets the aid.
	 * @param aid the new aid
	 */
	public void setAID(AID aid) {
		this.aid = aid;
		
		// --- Remember MTP addresses separately to make sure they are saved ------------
		if (aid.getAddressesArray().length>0) {
			this.getMtpAddresses().clear();
			for (int i=0; i<aid.getAddressesArray().length; i++) {
				this.getMtpAddresses().add(aid.getAddressesArray()[i]);
			}
		}
	}
	
	/**
	 * Checks if the component represented by this entry is controllable.
	 * @return true, if is controllable
	 */
	public boolean isControllable() {
		return controllable;
	}
	
	/**
	 * Sets the controllable.
	 * @param controllable the new controllable
	 */
	public void setControllable(boolean controllable) {
		this.controllable = controllable;
	}
	
	/**
	 * Gets the component type.
	 * @return the component type
	 */
	public String getComponentType() {
		return componentType;
	}
	
	/**
	 * Sets the component type.
	 * @param componentType the new component type
	 */
	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}
	
	/**
	 * Gets the mtp addresses.
	 * @return the mtp addresses
	 */
	public Vector<String> getMtpAddresses() {
		if (mtpAddresses==null) {
			mtpAddresses = new Vector<>();
		}
		return mtpAddresses;
	}

	/**
	 * Sets the mtp addresses.
	 * @param mtpAddresses the new mtp addresses
	 */
	public void setMtpAddresses(Vector<String> mtpAddresses) {
		this.mtpAddresses = mtpAddresses;
	}
	
}
