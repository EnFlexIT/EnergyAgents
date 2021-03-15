package de.enflexit.ea.electricity.aggregation;

import de.enflexit.ea.core.dataModel.ontology.CableProperties;
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
	
	private double currentReal;
	private double currentImag;
	
	private CableProperties cableProperties = null;
	
	private UnitValue lossesP;
	private UnitValue lossesQ;
	
	
	/**
	 * Instantiates a new cable losses and will immediately provide the real and complex losses.
	 *
	 * @param cableCurrentReal the cable current real
	 * @param cableCurrentImag the cable current imaginary
	 * @param uKRealNode1 the real voltage node 1
	 * @param uKImagNode1 the complex voltage node 1
	 * @param uKRealNode2 the real voltage node 2
	 * @param uKImagNode2 the complex voltage node 2
	 */
	public CableLosses(double cableCurrentReal, double cableCurrentImag, double uKRealNode1, double uKImagNode1, double uKRealNode2, double uKImagNode2, CableProperties cableProperties) {
		this.currentReal = cableCurrentReal;
		this.currentImag = cableCurrentImag;
		this.uKRealNode1 = uKRealNode1;
		this.uKImagNode1 = uKImagNode1;
		this.uKRealNode2 = uKRealNode2;
		this.uKImagNode2 = uKImagNode2;
		this.cableProperties = cableProperties;
		this.calculate();
	}
	
	/**
	 * Calculate.
	 */
	private void calculate() {

		try {

//			// --- Complex assignment for P and Q losses --------
//			//double I = (current * cosPhi) + j * (current * sinPhi);
//			//double dU = (voltageRealDiff + j * voltageComplexDiff);
//			//double dS = dU * I;
//			//dS = (voltageRealDiff + j * voltageComplexDiff) * ((current * cosPhi) + j * (current * sinPhi));
//
//			double voltageRealDiff    = this.uKRealNode1 - this.uKRealNode2;
//			double voltageComplexDiff = this.uKImagNode1 - this.uKImagNode2;
//
//			double dP = voltageRealDiff * this.currentReal - voltageComplexDiff * this.currentImag;
//			double dQ =  voltageComplexDiff * this.currentReal + voltageRealDiff * this.currentImag;
//			
//			// --- Set result -----------------------------------
//			this.getLossesP().setValue((float)dP);
//			this.getLossesQ().setValue((float)dQ);
			
			// --- Calculation of cableParameters
			double length = this.cableProperties.getLength().getValue() / 1000.0;
			double R = this.cableProperties.getLinearResistance().getValue() * length;
			double X = this.cableProperties.getLinearReactance()==null   ? 0.0 : this.cableProperties.getLinearReactance().getValue() * length;
			double G = this.cableProperties.getLinearConductance()==null ? 0.0 : this.cableProperties.getLinearConductance().getValue() * length;
			double B = this.cableProperties.getLinearCapacitance()==null ? 0.0 : 100 * Math.PI * this.cableProperties.getLinearCapacitance().getValue()*1E-9 * length;
			
			// --- Complex assignment for longitudinal losses ---
			// S = U * I
			// U = R * I
			// S = R * I²
			// P + j*Q = (R + j*X) * (currentReal + j*currentImag) * (currentReal + j*currentImag)
			// P + j*Q = (R + j*X) * ((currentReal*currentReal - currentImag*currentImag) + j*(2*currentReal*currentImag)
			// P + j*Q = R*(currentReal*currentReal - currentImag*currentImag) - 2*X*currentReal*currentImag + j*(X*(currentReal*currentReal - currentImag*currentImag) + 2*R*currentReal*currentImag)
			double dPlen = R*(currentReal*currentReal - currentImag*currentImag) - 2*X*currentReal*currentImag;
			double dQlen = X*(currentReal*currentReal - currentImag*currentImag) + 2*R*currentReal*currentImag;
			
			// --- Complex assignment for cross losses ---
			// S = U * I
			// U = R * I
			// I = U * G
			// S = U² * G
			// P + j*Q = (G + j*B) * (uKReal + j*uKImag) * (uKReal + j*uKImag)
			// P + j*Q = (G + j*B) * (uKReal*uKReal - uKImag*uKImag + j*(2*uKReal*uKImag))
			// P + j*Q = G*(uKReal*uKReal - uKImag*uKImag) - 2*B*uKReal*uKImag + j*(B*(uKReal*uKReal - uKImag*uKImag) + 2*G*uKReal*uKImag)
			double dPNode1 = 0;
//			double dQNode1 = 0;
			double dPNode2 = 0;
//			double dQNode2 = 0;
			
//			double dPNode1 = 0.5*(G*(uKRealNode1*uKRealNode1 - uKImagNode1*uKImagNode1) - 2*B*uKRealNode1*uKImagNode1);
			double dQNode1 = 0.5*(B*(uKRealNode1*uKRealNode1 - uKImagNode1*uKImagNode1) + 2*G*uKRealNode1*uKImagNode1);
//			double dPNode2 = 0.5*(G*(uKRealNode2*uKRealNode2 - uKImagNode2*uKImagNode2) - 2*B*uKRealNode2*uKImagNode2);
			double dQNode2 = 0.5*(B*(uKRealNode2*uKRealNode2 - uKImagNode2*uKImagNode2) + 2*G*uKRealNode2*uKImagNode2);
			
			this.getLossesP().setValue((float) (dPlen + dPNode1 + dPNode2));
			this.getLossesQ().setValue((float) (dQlen + dQNode1 + dQNode2));
			
		} catch (Exception ex) {
			System.err.println("[" + this.getClass().getSimpleName() + "] Error in cable losses calculation:");
			ex.printStackTrace();
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