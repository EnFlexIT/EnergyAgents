package de.enflexit.ea.core.blackboard.db.dataModel;

import java.util.Calendar;

public class NodeResult  extends AbstractStateResult {

	private static final long serialVersionUID = 1151410790396704632L;
	
	private int idScenarioResult;	
	private String idNode;	
	private Calendar timestamp;
	
	private double voltageReal;
	private double voltageComplex;
	private double voltageViolations;

	
	public int getIdScenarioResult() {
		return idScenarioResult;
	}
	public void setIdScenarioResult(int idScenarioResult) {
		this.idScenarioResult = idScenarioResult;
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
		
		if (nrComp.getIdScenarioResult()!=this.getIdScenarioResult()) return false;
		
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
		
		String hashCodeString = "" + this.getIdScenarioResult();
		
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
		valueString += this.getIdScenarioResult() + ",";
		valueString += "'" + this.getIdNode() + "',";
		valueString += "'" + AbstractStateResult.getTimeStampAsSQLString(this.getTimestamp()) + "',";
		
		valueString += this.getVoltageReal() + ",";
		valueString += this.getVoltageComplex() + ",";
		valueString += this.getVoltageViolations() + ")";		
		return valueString;
	}
	
}
