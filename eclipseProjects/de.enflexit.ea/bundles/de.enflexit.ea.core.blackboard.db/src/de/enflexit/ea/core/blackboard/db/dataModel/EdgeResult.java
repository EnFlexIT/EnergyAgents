package de.enflexit.ea.core.blackboard.db.dataModel;

import java.util.Calendar;

public class EdgeResult extends AbstractStateResult {

	private static final long serialVersionUID = 1151410790396704632L;
	
	private int idScenarioResult;	
	private String idEdge;	
	private Calendar timestamp;
	
	private double utilization;	
    private double lossesP;
    private double lossesQ;
	
    
    public int getIdScenarioResult() {
		return idScenarioResult;
	}
	public void setIdScenarioResult(int idScenarioResult) {
		this.idScenarioResult = idScenarioResult;
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
	
	
	public double getUtilization() {
		return utilization;
	}
	public void setUtilization(double utilization) {
		this.utilization = utilization;
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
	
    
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object compObejct) {
		
		if (compObejct==null || !(compObejct instanceof EdgeResult)) return false;
		EdgeResult erComp = (EdgeResult) compObejct;
		
		if (erComp.getIdScenarioResult()!=this.getIdScenarioResult()) return false;
		
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
		
		String hashCodeString = "" + this.getIdScenarioResult();
		
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
		valueString += this.getIdScenarioResult() + ",";
		valueString += "'" + this.getIdEdge() + "',";
		valueString += "'" + AbstractStateResult.getTimeStampAsSQLString(this.getTimestamp()) + "',";
		
		valueString += this.getUtilization() + ",";
		valueString += this.getLossesP() + ",";
		valueString += this.getLossesQ() + ")";		
		return valueString;
	}
	
}
