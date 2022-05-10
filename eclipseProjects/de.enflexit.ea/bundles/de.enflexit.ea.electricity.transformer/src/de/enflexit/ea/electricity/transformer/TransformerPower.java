package de.enflexit.ea.electricity.transformer;

import java.io.Serializable;

/**
 * The Class TransformerPower can be used any kind of power flow in the context of the transformer calculation.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class TransformerPower implements Serializable {

	private static final long serialVersionUID = 1830204772847799316L;

	private double activePower;
	private double reactivePower;
	
	public TransformerPower() {}

	public TransformerPower(double activePower, double reactivePower) {
		this.setActivePower(activePower);
		this.setReactivePower(reactivePower);
	}
	
	public double getActivePower() {
		return activePower;
	}
	public void setActivePower(double activePower) {
		this.activePower = activePower;
	}
	public double getReactivePower() {
		return reactivePower;
	}
	public void setReactivePower(double reactivePower) {
		this.reactivePower = reactivePower;
	}
	public double getApparentPower() {
		return Math.sqrt(Math.pow(this.activePower, 2) + Math.pow(this.reactivePower, 2));
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "P = " + this.getActivePower() + ", Q = " + this.getReactivePower();
	}
	
}
