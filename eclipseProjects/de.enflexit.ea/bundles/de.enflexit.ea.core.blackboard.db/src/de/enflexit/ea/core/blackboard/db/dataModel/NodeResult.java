package de.enflexit.ea.core.blackboard.db.dataModel;

import java.util.Calendar;

public class NodeResult  extends AbstractStateResult {

	private static final long serialVersionUID = 1151410790396704632L;
	
	private int idExecution;	
	private String idNode;	
	private Calendar timestamp;
	
	private double voltageL1Real;
	private double voltageL1Imag;
	private double voltageL1Abs;
	private double voltageL2Real;
	private double voltageL2Imag;
	private double voltageL2Abs;
	private double voltageL3Real;
	private double voltageL3Imag;
	private double voltageL3Abs;

	private double currentL1;
	private double currentL2;
	private double currentL3;

	private double cosPhiL1;
	private double cosPhiL2;
	private double cosPhiL3;
	
	private double powerP1;
	private double powerQ1;
	private double powerP2;
	private double powerQ2;
	private double powerP3;
	private double powerQ3;
	
	
	private double voltageReal;
	private double voltageImag;
	private double voltageAbs;
	private double voltageViolations;
	
	private double current;
	private double cosPhi;
	
	private double powerP;
	private double powerQ;
	
	
	
	public int getIdExecution() {
		return idExecution;
	}
	public void setIdExecution(int idExecution) {
		this.idExecution = idExecution;
	}

	public String getIdNode() {
		return idNode;
	}
	public void setIdNode(String idNode) {
		this.idNode = idNode;
	}
	
	public Calendar getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Calendar timestamp) {
		this.timestamp = timestamp;
	}
	
	
	public double getVoltageL1Real() {
		return voltageL1Real;
	}
	public void setVoltageL1Real(double voltageL1Real) {
		this.voltageL1Real = voltageL1Real;
	}
	public double getVoltageL1Imag() {
		return voltageL1Imag;
	}
	public void setVoltageL1Imag(double voltageL1Imag) {
		this.voltageL1Imag = voltageL1Imag;
	}
	public double getVoltageL1Abs() {
		return voltageL1Abs;
	}
	public void setVoltageL1Abs(double voltageL1Abs) {
		this.voltageL1Abs = voltageL1Abs;
	}
	
	
	public double getVoltageL2Real() {
		return voltageL2Real;
	}
	public void setVoltageL2Real(double voltageL2Real) {
		this.voltageL2Real = voltageL2Real;
	}
	public double getVoltageL2Imag() {
		return voltageL2Imag;
	}
	public void setVoltageL2Imag(double voltageL2Imag) {
		this.voltageL2Imag = voltageL2Imag;
	}
	public double getVoltageL2Abs() {
		return voltageL2Abs;
	}
	public void setVoltageL2Abs(double voltageL2Abs) {
		this.voltageL2Abs = voltageL2Abs;
	}
	
	
	public double getVoltageL3Real() {
		return voltageL3Real;
	}
	public void setVoltageL3Real(double voltageL3Real) {
		this.voltageL3Real = voltageL3Real;
	}
	public double getVoltageL3Imag() {
		return voltageL3Imag;
	}
	public void setVoltageL3Imag(double voltageL3Imag) {
		this.voltageL3Imag = voltageL3Imag;
	}
	public double getVoltageL3Abs() {
		return voltageL3Abs;
	}
	public void setVoltageL3Abs(double voltageL3Abs) {
		this.voltageL3Abs = voltageL3Abs;
	}
	
	
	public double getCurrentL1() {
		return currentL1;
	}
	public void setCurrentL1(double currentL1) {
		this.currentL1 = currentL1;
	}
	
	public double getCurrentL2() {
		return currentL2;
	}
	public void setCurrentL2(double currentL2) {
		this.currentL2 = currentL2;
	}
	
	public double getCurrentL3() {
		return currentL3;
	}
	public void setCurrentL3(double currentL3) {
		this.currentL3 = currentL3;
	}
	
	
	public double getCosPhiL1() {
		return cosPhiL1;
	}
	public void setCosPhiL1(double cosPhiL1) {
		this.cosPhiL1 = cosPhiL1;
	}
	
	public double getCosPhiL2() {
		return cosPhiL2;
	}
	public void setCosPhiL2(double cosPhiL2) {
		this.cosPhiL2 = cosPhiL2;
	}
	
	public double getCosPhiL3() {
		return cosPhiL3;
	}
	public void setCosPhiL3(double cosPhiL3) {
		this.cosPhiL3 = cosPhiL3;
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
	
	
	
	public double getVoltageReal() {
		return voltageReal;
	}
	public void setVoltageReal(double voltageReal) {
		this.voltageReal = voltageReal;
	}
	public double getVoltageImag() {
		return voltageImag;
	}
	public void setVoltageImag(double voltageImag) {
		this.voltageImag = voltageImag;
	}
	public double getVoltageAbs() {
		return voltageAbs;
	}
	public void setVoltageAbs(double voltageAbs) {
		this.voltageAbs = voltageAbs;
	}
	
	
	public double getCurrent() {
		return current;
	}
	public void setCurrent(double current) {
		this.current = current;
	}
	
	
	public double getCosPhi() {
		return cosPhi;
	}
	public void setCosPhi(double cosPhi) {
		this.cosPhi = cosPhi;
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
	
	
	public double getVoltageViolations() {
		return voltageViolations;
	}
	public void setVoltageViolations(double voltageViolations) {
		this.voltageViolations = voltageViolations;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object compObejct) {
		
		if (compObejct==null || !(compObejct instanceof NodeResult)) return false;
		NodeResult nrComp = (NodeResult) compObejct;
		
		if (nrComp.getIdExecution()!=this.getIdExecution()) return false;
		
		String idComp = nrComp.getIdNode();
		String idLocal = this.getIdNode();
		if (idComp==null && idLocal==null) {
			// --- equals ---
		} else if ((idComp==null && idLocal!=null) || (idComp!=null && idLocal==null)) {
			return false;
		} else {
			if (idComp.equals(idLocal)==false) return false;
		}
		
		if (nrComp.getTimestamp()==null && this.getTimestamp()==null) {
			// --- equals ---
		} else if ((nrComp.getTimestamp()==null && this.getTimestamp()!=null) || (nrComp.getTimestamp()!=null && this.getTimestamp()==null)) {
			return false;
		} else if (nrComp.getTimestamp().equals(this.getTimestamp())==false) {
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
		
		if (this.getIdNode()==null) {
			hashCodeString += "null";
		} else {
			hashCodeString += this.getIdNode();
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
		valueString += "'" + this.getIdNode() + "',";
		valueString += "'" + AbstractStateResult.getTimeStampAsSQLString(this.getTimestamp()) + "',";
		
		valueString += this.getVoltageL1Real() + ",";
		valueString += this.getVoltageL1Imag() + ",";
		valueString += this.getVoltageL1Abs() + ",";
		
		valueString += this.getVoltageL2Real() + ",";
		valueString += this.getVoltageL2Imag() + ",";
		valueString += this.getVoltageL2Abs() + ",";
		
		valueString += this.getVoltageL3Real() + ",";
		valueString += this.getVoltageL3Imag() + ",";
		valueString += this.getVoltageL3Abs() + ",";
		
		valueString += this.getCurrentL1() + ",";
		valueString += this.getCurrentL2() + ",";
		valueString += this.getCurrentL3() + ",";

		valueString += this.getCosPhiL1() + ",";
		valueString += this.getCosPhiL2() + ",";
		valueString += this.getCosPhiL3() + ",";
		
		valueString += this.getPowerP1() + ",";
		valueString += this.getPowerQ1() + ",";

		valueString += this.getPowerP2() + ",";
		valueString += this.getPowerQ2() + ",";
		
		valueString += this.getPowerP3() + ",";
		valueString += this.getPowerQ3() + ",";
		
		
		valueString += this.getVoltageReal() + ",";
		valueString += this.getVoltageImag() + ",";
		valueString += this.getVoltageAbs() + ",";
		
		valueString += this.getCurrent() + ",";
		valueString += this.getCosPhi() + ",";
		
		valueString += this.getPowerP() + ",";
		valueString += this.getPowerQ() + ",";

		valueString += this.getVoltageViolations() + ")";
		
		return valueString;
	}
}
