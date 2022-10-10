package de.enflexit.ea.core.blackboard.db.dataModel;

import java.util.Calendar;

public class NodeResult  extends AbstractStateResult {

	private static final long serialVersionUID = 1151410790396704632L;
	
	private int idExecution;	
	private String idNode;	
	private Calendar timestamp;
	
	private double voltageL1Real;
	private double voltageL1Complex;
	private double voltageL2Real;
	private double voltageL2Complex;
	private double voltageL3Real;
	private double voltageL3Complex;
	
	private double voltageReal;
	private double voltageComplex;
	private double voltageViolations;

	
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
	
	public double getVoltageL1Complex() {
		return voltageL1Complex;
	}
	public void setVoltageL1Complex(double voltageL1Complex) {
		this.voltageL1Complex = voltageL1Complex;
	}
	
	
	public double getVoltageL2Real() {
		return voltageL2Real;
	}
	public void setVoltageL2Real(double voltageL2Real) {
		this.voltageL2Real = voltageL2Real;
	}
	
	public double getVoltageL2Complex() {
		return voltageL2Complex;
	}
	public void setVoltageL2Complex(double voltageL2Complex) {
		this.voltageL2Complex = voltageL2Complex;
	}
	
	
	public double getVoltageL3Real() {
		return voltageL3Real;
	}
	public void setVoltageL3Real(double voltageL3Real) {
		this.voltageL3Real = voltageL3Real;
	}
	
	public double getVoltageL3Complex() {
		return voltageL3Complex;
	}
	public void setVoltageL3Complex(double voltageL3Complex) {
		this.voltageL3Complex = voltageL3Complex;
	}
	
	
	public double getVoltageReal() {
		return voltageReal;
	}
	public void setVoltageReal(double voltageReal) {
		this.voltageReal = voltageReal;
	}
	
	public double getVoltageComplex() {
		return voltageComplex;
	}
	public void setVoltageComplex(double voltageComplex) {
		this.voltageComplex = voltageComplex;
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
		valueString += this.getVoltageL1Complex() + ",";
		
		valueString += this.getVoltageL2Real() + ",";
		valueString += this.getVoltageL2Complex() + ",";
		
		valueString += this.getVoltageL3Real() + ",";
		valueString += this.getVoltageL3Complex() + ",";
				
		valueString += this.getVoltageReal() + ",";
		valueString += this.getVoltageComplex() + ",";
		valueString += this.getVoltageViolations() + ")";		
		return valueString;
	}
	
}
