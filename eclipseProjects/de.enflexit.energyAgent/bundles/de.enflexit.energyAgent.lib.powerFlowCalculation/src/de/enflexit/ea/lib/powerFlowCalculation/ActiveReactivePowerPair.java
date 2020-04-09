package de.enflexit.ea.lib.powerFlowCalculation;

/**
 * The Class ActiveReactivePowerPair stores the active and reactive power 
 * of a specific node in Watt (that means for example not in kW).
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class ActiveReactivePowerPair {

	private double activePowerInWatt;
	private double reactivePowerInWatt;
	
	
	/**
	 * Constructor of this class.
	 *
	 * @param nodeID the node ID
	 * @param activePowerInWatt the active power in watt (not kW)
	 * @param reactivePowerInWatt the reactive power in watt (not kW)
	 */
	public ActiveReactivePowerPair(double activePowerInWatt, double reactivePowerInWatt) {
		this.activePowerInWatt = activePowerInWatt;
		this.reactivePowerInWatt = reactivePowerInWatt;
	}
	
	/**
	 * Gets the active power in watt.
	 * @return the activePowerInWatt
	 */
	public double getActivePowerInWatt() {
		return activePowerInWatt;
	}
	/**
	 * Sets the active power in watt.
	 * @param activePowerInWatt the activePowerInWatt to set
	 */
	public void setActivePowerInWatt(double activePowerInWatt) {
		this.activePowerInWatt = activePowerInWatt;
	}

	/**
	 * Gets the reactive power in watt.
	 * @return the reactivePowerInWatt
	 */
	public double getReactivePowerInWatt() {
		return reactivePowerInWatt;
	}
	/**
	 * Sets the reactive power in watt.
	 * @param reactivePowerInWatt the reactivePowerInWatt to set
	 */
	public void setReactivePowerInWatt(double reactivePowerInWatt) {
		this.reactivePowerInWatt = reactivePowerInWatt;
	}

}
