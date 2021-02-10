package de.enflexit.ea.electricity.aggregation;

import de.enflexit.ea.core.dataModel.ontology.UnitValue;

/**
 * The Class CableLosses can be used to calculate the cable losses between two nodes 
 * and their voltage levels (real and complex) by considering the cable current and cos phi.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class CableLosses {
	
	private double uKRealNode1 = 0;
	private double uKImagNode1 = 0;
	private double uKRealNode2 = 0;
	private double uKImagNode2 = 0;
	
	private double current;
	private double cosPhi;
	
	private UnitValue lossesP;
	private UnitValue lossesQ;
	
	
	/**
	 * Instantiates a new cable losses and will immediately provide the real and complex losses.
	 *
	 * @param cableCurrent the cable current
	 * @param cosPhi the cos phi
	 * @param uKRealNode1 the real voltage node 1
	 * @param uKImagNode1 the complex voltage node 1
	 * @param uKRealNode2 the real voltage node 2
	 * @param uKImagNode2 the complex voltage node 2
	 */
	public CableLosses(double cableCurrent, double cosPhi, double uKRealNode1, double uKImagNode1, double uKRealNode2, double uKImagNode2) {
		this.current = cableCurrent;
		this.cosPhi = cosPhi;
		this.uKRealNode1 = uKRealNode1;
		this.uKImagNode1 = uKImagNode1;
		this.uKRealNode2 = uKRealNode2;
		this.uKImagNode2 = uKImagNode2;
		this.calculate();
	}
	/**
	 * Calculate.
	 */
	private void calculate() {

		try {
			
			double sinPhi = Math.sin(Math.acos(this.cosPhi));
			double voltageRealDiff    = Math.abs(this.uKRealNode1 - this.uKRealNode2);
			double voltageComplexDiff = Math.abs(this.uKImagNode1 - this.uKImagNode2);
			
			// --- Complex assignment for P and Q losses --------
			//double I = (current * cosPhi) + j * (current * sinPhi);
			//double dU = (voltageRealDiff + j * voltageComplexDiff);
			//double dS = dU * I;
			//dS = (voltageRealDiff + j * voltageComplexDiff) * ((current * cosPhi) + j * (current * sinPhi));

			double dP = (voltageRealDiff * this.current * this.cosPhi) - (voltageComplexDiff * (this.current * sinPhi));
			double dQ =  voltageComplexDiff * this.current * this.cosPhi + (voltageRealDiff * (this.current * sinPhi));
			
			// --- Set result -----------------------------------
			this.getLossesP().setValue((float)dP);
			this.getLossesQ().setValue((float)dQ);
			
		} catch (Exception ex) {
			System.err.println("[" + this.getClass().getSimpleName() + "] Error in cable losses calculation:");
		}
	}
	
	/**
	 * Returns the real losses P.
	 * @return the losses P
	 */
	public UnitValue getLossesP() {
		if (lossesP==null) {
			lossesP = new UnitValue(0, "W");
		}
		return lossesP;
	}
	/**
	 * Returns the complex losses Q.
	 * @return the losses Q
	 */
	public UnitValue getLossesQ() {
		if (lossesQ==null) {
			lossesQ = new UnitValue(0, "var");
		}
		return lossesQ;
	}
}