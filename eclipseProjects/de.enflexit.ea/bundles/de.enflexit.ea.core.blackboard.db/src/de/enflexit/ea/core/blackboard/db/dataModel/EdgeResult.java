package de.enflexit.ea.core.blackboard.db.dataModel;

import java.util.Calendar;

public class EdgeResult extends AbstractStateResult {

	private static final long serialVersionUID = 1151410790396704632L;
	
	private int idExecution;	
	private String idEdge;	
	private Calendar timestamp;
	
	private double currentL1Real;
	private double currentL1Imag;
	private double currentL1Abs;
	private double cosPhiL1;
	private double lossesL1P;
    private double lossesL1Q;
    private double powerP1;
    private double powerQ1;
	
    private double currentL2Real;
	private double currentL2Imag;
	private double currentL2Abs;
	private double cosPhiL2;
	private double lossesL2P;
    private double lossesL2Q;
    private double powerP2;
    private double powerQ2;
    
    private double currentL3Real;
	private double currentL3Imag;
	private double currentL3Abs;
	private double cosPhiL3;
	private double lossesL3P;
    private double lossesL3Q;
    private double powerP3;
    private double powerQ3;
    
	private double currentReal;
	private double currentImag;
	private double currentAbs;
	private double cosPhi;
	private double lossesP;
    private double lossesQ;
    private double powerP;
    private double powerQ;
    
    private double utilization;	
	
    
    public int getIdExecution() {
		return idExecution;
	}
	public void setIdExecution(int idExecution) {
		this.idExecution = idExecution;
	}
	
	public String getIdEdge() {
		return idEdge;
	}
	public void setIdEdge(String idEdge) {
		this.idEdge = idEdge;
	}
	
	public Calendar getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Calendar timestamp) {
		this.timestamp = timestamp;
	}
	
	
	public double getCurrentL1Real() {
		return currentL1Real;
	}
	public void setCurrentL1Real(double currentL1Real) {
		this.currentL1Real = currentL1Real;
	}
	public double getCurrentL1Imag() {
		return currentL1Imag;
	}
	public void setCurrentL1Imag(double currentL1Complex) {
		this.currentL1Imag = currentL1Complex;
	}
	public double getCurrentL1Abs() {
		return currentL1Abs;
	}
	public void setCurrentL1Abs(double currentL1Abs) {
		this.currentL1Abs = currentL1Abs;
	}
	public double getCosPhiL1() {
		return cosPhiL1;
	}
	public void setCosPhiL1(double cosPhiL1) {
		this.cosPhiL1 = cosPhiL1;
	}
	public double getLossesL1P() {
		return lossesL1P;
	}
	public void setLossesL1P(double lossesL1P) {
		this.lossesL1P = lossesL1P;
	}
	public double getLossesL1Q() {
		return lossesL1Q;
	}
	public void setLossesL1Q(double lossesL1Q) {
		this.lossesL1Q = lossesL1Q;
	}
	public double getPowerP1() {
		return powerP1;
	}
	public void setPowerP1(double powerP1) {
		this.powerP1 = powerP1;
	}
	public double getPowerQ1() {
		return powerQ1;
	}
	public void setPowerQ1(double powerQ1) {
		this.powerQ1 = powerQ1;
	}
	
	
	public double getCurrentL2Real() {
		return currentL2Real;
	}
	public void setCurrentL2Real(double currentL2Real) {
		this.currentL2Real = currentL2Real;
	}
	public double getCurrentL2Imag() {
		return currentL2Imag;
	}
	public void setCurrentL2Imag(double currentL2Imag) {
		this.currentL2Imag = currentL2Imag;
	}
	public double getCurrentL2Abs() {
		return currentL2Abs;
	}
	public void setCurrentL2Abs(double currentL2Abs) {
		this.currentL2Abs = currentL2Abs;
	}
	public double getCosPhiL2() {
		return cosPhiL2;
	}
	public void setCosPhiL2(double cosPhiL2) {
		this.cosPhiL2 = cosPhiL2;
	}
	public double getLossesL2P() {
		return lossesL2P;
	}
	public void setLossesL2P(double lossesL2P) {
		this.lossesL2P = lossesL2P;
	}
	public double getLossesL2Q() {
		return lossesL2Q;
	}
	public void setLossesL2Q(double lossesL2Q) {
		this.lossesL2Q = lossesL2Q;
	}
	public double getPowerP2() {
		return powerP2;
	}
	public void setPowerP2(double powerP2) {
		this.powerP2 = powerP2;
	}
	public double getPowerQ2() {
		return powerQ2;
	}
	public void setPowerQ2(double powerQ2) {
		this.powerQ2 = powerQ2;
	}
	
	
	public double getCurrentL3Real() {
		return currentL3Real;
	}
	public void setCurrentL3Real(double currentL3Real) {
		this.currentL3Real = currentL3Real;
	}
	public double getCurrentL3Imag() {
		return currentL3Imag;
	}
	public void setCurrentL3Imag(double currentL3Imag) {
		this.currentL3Imag = currentL3Imag;
	}
	public double getCurrentL3Abs() {
		return currentL3Abs;
	}
	public void setCurrentL3Abs(double currentL3Abs) {
		this.currentL3Abs = currentL3Abs;
	}
	public double getCosPhiL3() {
		return cosPhiL3;
	}
	public void setCosPhiL3(double cosPhiL3) {
		this.cosPhiL3 = cosPhiL3;
	}
	public double getLossesL3P() {
		return lossesL3P;
	}
	public void setLossesL3P(double lossesL3P) {
		this.lossesL3P = lossesL3P;
	}
	public double getLossesL3Q() {
		return lossesL3Q;
	}
	public void setLossesL3Q(double lossesL3Q) {
		this.lossesL3Q = lossesL3Q;
	}
	public double getPowerP3() {
		return powerP3;
	}
	public void setPowerP3(double powerP3) {
		this.powerP3 = powerP3;
	}
	public double getPowerQ3() {
		return powerQ3;
	}
	public void setPowerQ3(double powerQ3) {
		this.powerQ3 = powerQ3;
	}
	
	
	public double getCurrentReal() {
		return currentReal;
	}
	public void setCurrentReal(double currentReal) {
		this.currentReal = currentReal;
	}
	public double getCurrentImag() {
		return currentImag;
	}
	public void setCurrentImag(double currentImag) {
		this.currentImag = currentImag;
	}
	public double getCurrentAbs() {
		return currentAbs;
	}
	public void setCurrentAbs(double currentAbs) {
		this.currentAbs = currentAbs;
	}
	public double getCosPhi() {
		return cosPhi;
	}
	public void setCosPhi(double cosPhi) {
		this.cosPhi = cosPhi;
	}
	public double getLossesP() {
		return lossesP;
	}
	public void setLossesP(double lossesP) {
		this.lossesP = lossesP;
	}
	public double getLossesQ() {
		return lossesQ;
	}
	public void setLossesQ(double lossesQ) {
		this.lossesQ = lossesQ;
	}
	public double getPowerP() {
		return powerP;
	}
	public void setPowerP(double powerP) {
		this.powerP = powerP;
	}
	public double getPowerQ() {
		return powerQ;
	}
	public void setPowerQ(double powerQ) {
		this.powerQ = powerQ;
	}
	
    
	public double getUtilization() {
		return utilization;
	}
	public void setUtilization(double utilization) {
		this.utilization = utilization;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object compObejct) {
		
		if (compObejct==null || !(compObejct instanceof EdgeResult)) return false;
		EdgeResult erComp = (EdgeResult) compObejct;
		
		if (erComp.getIdExecution()!=this.getIdExecution()) return false;
		
		String idComp = erComp.getIdEdge();
		String idLocal = this.getIdEdge();
		if (idComp==null && idLocal==null) {
			// --- equals ---
		} else if ((idComp==null && idLocal!=null) || (idComp!=null && idLocal==null)) {
			return false;
		} else {
			if (idComp.equals(idLocal)==false) return false;
		}
		
		if (erComp.getTimestamp()==null && this.getTimestamp()==null) {
			// --- equals ---
		} else if ((erComp.getTimestamp()==null && this.getTimestamp()!=null) || (erComp.getTimestamp()!=null && this.getTimestamp()==null)) {
			return false;
		} else if (erComp.getTimestamp().equals(this.getTimestamp())==false) {
			return false;
		}
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		
		String hashCodeString = "" + this.getIdExecution();
		
		if (this.getIdEdge()==null) {
			hashCodeString += "null";
		} else {
			hashCodeString += this.getIdEdge();
		}

		if (this.getTimestamp()==null) {
			hashCodeString += "null";
		} else {
			hashCodeString += this.getTimestamp();
		}
		return hashCodeString.hashCode();
	}
	
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.blackboard.db.dataModel.AbstractStateResult#getSQLInsertValueArray()
	 */
	@Override
	public String getSQLInsertValueArray() {
		
		String valueString = "(";
		valueString += this.getIdExecution() + ",";
		valueString += "'" + this.getIdEdge() + "',";
		valueString += "'" + AbstractStateResult.getTimeStampAsSQLString(this.getTimestamp()) + "',";

		valueString += this.getCurrentL1Real() + ",";
		valueString += this.getCurrentL1Imag() + ",";
		valueString += this.getCurrentL1Abs() + ",";
		valueString += this.getCosPhiL1() + ",";
		valueString += this.getLossesL1P() + ",";
		valueString += this.getLossesL1Q() + ",";		
		valueString += this.getPowerP1() + ",";
		valueString += this.getPowerQ1() + ",";
		
		valueString += this.getCurrentL2Real() + ",";
		valueString += this.getCurrentL2Imag() + ",";
		valueString += this.getCurrentL2Abs() + ",";
		valueString += this.getCosPhiL2() + ",";
		valueString += this.getLossesL2P() + ",";
		valueString += this.getLossesL2Q() + ",";
		valueString += this.getPowerP2() + ",";
		valueString += this.getPowerQ2() + ",";
		
		valueString += this.getCurrentL3Real() + ",";
		valueString += this.getCurrentL3Imag() + ",";
		valueString += this.getCurrentL3Abs() + ",";
		valueString += this.getCosPhiL3() + ",";
		valueString += this.getLossesL3P() + ",";
		valueString += this.getLossesL3Q() + ",";
		valueString += this.getPowerP3() + ",";
		valueString += this.getPowerQ3() + ",";

		valueString += this.getCurrentReal() + ",";
		valueString += this.getCurrentImag() + ",";
		valueString += this.getCurrentAbs() + ",";
		valueString += this.getCosPhi() + ",";
		valueString += this.getLossesP() + ",";
		valueString += this.getLossesQ() + ",";		
		valueString += this.getPowerP() + ",";
		valueString += this.getPowerQ() + ",";
		
		valueString += this.getUtilization() + ")";
		return valueString;
	}
	
}
