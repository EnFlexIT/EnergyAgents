package de.enflexit.ea.core.blackboard.db.dataModel;

import java.util.Calendar;

public class EdgeResult extends AbstractStateResult {

	private static final long serialVersionUID = 1151410790396704632L;
	
	private int idExecution;	
	private String idEdge;	
	private Calendar timestamp;
	
	private double currentL1Real;
	private double currentL1Complex;
	private double lossesL1P;
    private double lossesL1Q;
	
    private double currentL2Real;
	private double currentL2Complex;
	private double lossesL2P;
    private double lossesL2Q;
    
    private double currentL3Real;
	private double currentL3Complex;
	private double lossesL3P;
    private double lossesL3Q;
    
	private double currentReal;
	private double currentComplex;
	private double lossesP;
    private double lossesQ;

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
	public double getCurrentL1Complex() {
		return currentL1Complex;
	}
	public void setCurrentL1Complex(double currentL1Complex) {
		this.currentL1Complex = currentL1Complex;
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
	
	
	public double getCurrentL2Real() {
		return currentL2Real;
	}
	public void setCurrentL2Real(double currentL2Real) {
		this.currentL2Real = currentL2Real;
	}
	public double getCurrentL2Complex() {
		return currentL2Complex;
	}
	public void setCurrentL2Complex(double currentL2Complex) {
		this.currentL2Complex = currentL2Complex;
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
	
	
	public double getCurrentL3Real() {
		return currentL3Real;
	}
	public void setCurrentL3Real(double currentL3Real) {
		this.currentL3Real = currentL3Real;
	}
	public double getCurrentL3Complex() {
		return currentL3Complex;
	}
	public void setCurrentL3Complex(double currentL3Complex) {
		this.currentL3Complex = currentL3Complex;
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
	
	
	public double getCurrentReal() {
		return currentReal;
	}
	public void setCurrentReal(double currentReal) {
		this.currentReal = currentReal;
	}
	public double getCurrentComplex() {
		return currentComplex;
	}
	public void setCurrentComplex(double currentComplex) {
		this.currentComplex = currentComplex;
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
		valueString += this.getCurrentL1Complex() + ",";
		valueString += this.getLossesL1P() + ",";
		valueString += this.getLossesL1Q() + ",";		

		valueString += this.getCurrentL2Real() + ",";
		valueString += this.getCurrentL2Complex() + ",";
		valueString += this.getLossesL2P() + ",";
		valueString += this.getLossesL2Q() + ",";
		
		valueString += this.getCurrentL3Real() + ",";
		valueString += this.getCurrentL3Complex() + ",";
		valueString += this.getLossesL3P() + ",";
		valueString += this.getLossesL3Q() + ",";
		
		valueString += this.getCurrentReal() + ",";
		valueString += this.getCurrentComplex() + ",";
		valueString += this.getLossesP() + ",";
		valueString += this.getLossesQ() + ",";		

		valueString += this.getUtilization() + ")";
		return valueString;
	}
	
}
