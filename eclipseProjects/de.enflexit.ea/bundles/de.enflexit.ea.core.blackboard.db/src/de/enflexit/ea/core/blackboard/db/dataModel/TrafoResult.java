package de.enflexit.ea.core.blackboard.db.dataModel;

import java.util.Calendar;

public class TrafoResult  extends AbstractStateResult {

	private static final long serialVersionUID = 1151410790396704632L;
	
	private int idExecution;	
	private String idTrafo;	
	private Calendar timestamp;
	
	private double hvVoltageL1Real;
	private double hvVoltageL1Imag;
	private double hvVoltageL1Abs;
	private double hvVoltageL2Real;
	private double hvVoltageL2Imag;
	private double hvVoltageL2Abs;
	private double hvVoltageL3Real;
	private double hvVoltageL3Imag;
	private double hvVoltageL3Abs;
	
	private double hvCurrentL1;
	private double hvCurrentL2;
	private double hvCurrentL3;
	
	private double hvCosPhiL1;
	private double hvCosPhiL2;
	private double hvCosPhiL3;
	
	private double hvPowerP1;
	private double hvPowerQ1;
	private double hvPowerP2;
	private double hvPowerQ2;
	private double hvPowerP3;
	private double hvPowerQ3;
	
	
	private double lvVoltageL1Real;
	private double lvVoltageL1Imag;
	private double lvVoltageL1Abs;
	private double lvVoltageL2Real;
	private double lvVoltageL2Imag;
	private double lvVoltageL2Abs;
	private double lvVoltageL3Real;
	private double lvVoltageL3Imag;
	private double lvVoltageL3Abs;
	
	private double lvCurrentL1;
	private double lvCurrentL2;
	private double lvCurrentL3;
	
	private double lvCosPhiL1;
	private double lvCosPhiL2;
	private double lvCosPhiL3;
	
	private double lvPowerP1;
	private double lvPowerQ1;
	private double lvPowerP2;
	private double lvPowerQ2;
	private double lvPowerP3;
	private double lvPowerQ3;
	
	
	private double voltageReal;
	private double voltageImag;
	private double voltageViolations;	
	private double residualLoadP;
	private double residualLoadQ;	
	private double trafoUtilization;	
	private double trafoLossesP;
	private double trafoLossesQ;
	
	private int tapPos;
	
	
	public int getIdExecution() {
		return idExecution;
	}
	public void setIdExecution(int idExecution) {
		this.idExecution = idExecution;
	}
	
	public String getIdTrafo() {
		return idTrafo;
	}
	public void setIdTrafo(String idTrafo) {
		this.idTrafo = idTrafo;
	}
	
	public Calendar getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Calendar timestamp) {
		this.timestamp = timestamp;
	}
	
	
	public double getHvVoltageL1Real() {
		return hvVoltageL1Real;
	}
	public void setHvVoltageL1Real(double hvVoltageL1Real) {
		this.hvVoltageL1Real = hvVoltageL1Real;
	}
	public double getHvVoltageL1Imag() {
		return hvVoltageL1Imag;
	}
	public void setHvVoltageL1Imag(double hvVoltageL1Imag) {
		this.hvVoltageL1Imag = hvVoltageL1Imag;
	}
	public double getHvVoltageL1Abs() {
		return hvVoltageL1Abs;
	}
	public void setHvVoltageL1Abs(double hvVoltageL1Abs) {
		this.hvVoltageL1Abs = hvVoltageL1Abs;
	}

	
	public double getHvVoltageL2Real() {
		return hvVoltageL2Real;
	}
	public void setHvVoltageL2Real(double hvVoltageL2Real) {
		this.hvVoltageL2Real = hvVoltageL2Real;
	}
	public double getHvVoltageL2Imag() {
		return hvVoltageL2Imag;
	}
	public void setHvVoltageL2Imag(double hvVoltageL2Imag) {
		this.hvVoltageL2Imag = hvVoltageL2Imag;
	}
	public double getHvVoltageL2Abs() {
		return hvVoltageL2Abs;
	}
	public void setHvVoltageL2Abs(double hvVoltageL2Abs) {
		this.hvVoltageL2Abs = hvVoltageL2Abs;
	}
	
	
	public double getHvVoltageL3Real() {
		return hvVoltageL3Real;
	}
	public void setHvVoltageL3Real(double hvVoltageL3Real) {
		this.hvVoltageL3Real = hvVoltageL3Real;
	}
	public double getHvVoltageL3Imag() {
		return hvVoltageL3Imag;
	}
	public void setHvVoltageL3Imag(double hvVoltageL3Imag) {
		this.hvVoltageL3Imag = hvVoltageL3Imag;
	}
	public double getHvVoltageL3Abs() {
		return hvVoltageL3Abs;
	}
	public void setHvVoltageL3Abs(double hvVoltageL3Abs) {
		this.hvVoltageL3Abs = hvVoltageL3Abs;
	}
	
	
	public double getHvCurrentL1() {
		return hvCurrentL1;
	}
	public void setHvCurrentL1(double hvCurrentL1) {
		this.hvCurrentL1 = hvCurrentL1;
	}
	public double getHvCurrentL2() {
		return hvCurrentL2;
	}
	public void setHvCurrentL2(double hvCurrentL2) {
		this.hvCurrentL2 = hvCurrentL2;
	}
	public double getHvCurrentL3() {
		return hvCurrentL3;
	}
	public void setHvCurrentL3(double hvCurrentL3) {
		this.hvCurrentL3 = hvCurrentL3;
	}
	
	
	public double getHvCosPhiL1() {
		return hvCosPhiL1;
	}
	public void setHvCosPhiL1(double hvCosPhiL1) {
		this.hvCosPhiL1 = hvCosPhiL1;
	}
	public double getHvCosPhiL2() {
		return hvCosPhiL2;
	}
	public void setHvCosPhiL2(double hvCosPhiL2) {
		this.hvCosPhiL2 = hvCosPhiL2;
	}
	public double getHvCosPhiL3() {
		return hvCosPhiL3;
	}
	public void setHvCosPhiL3(double hvCosPhiL3) {
		this.hvCosPhiL3 = hvCosPhiL3;
	}
	
	
	public double getHvPowerP1() {
		return hvPowerP1;
	}
	public void setHvPowerP1(double hvPowerP1) {
		this.hvPowerP1 = hvPowerP1;
	}
	public double getHvPowerQ1() {
		return hvPowerQ1;
	}
	public void setHvPowerQ1(double hvPowerQ1) {
		this.hvPowerQ1 = hvPowerQ1;
	}
	public double getHvPowerP2() {
		return hvPowerP2;
	}
	public void setHvPowerP2(double hvPowerP2) {
		this.hvPowerP2 = hvPowerP2;
	}
	public double getHvPowerQ2() {
		return hvPowerQ2;
	}
	public void setHvPowerQ2(double hvPowerQ2) {
		this.hvPowerQ2 = hvPowerQ2;
	}
	public double getHvPowerP3() {
		return hvPowerP3;
	}
	public void setHvPowerP3(double hvPowerP3) {
		this.hvPowerP3 = hvPowerP3;
	}
	public double getHvPowerQ3() {
		return hvPowerQ3;
	}
	public void setHvPowerQ3(double hvPowerQ3) {
		this.hvPowerQ3 = hvPowerQ3;
	}
	
	
	
	public double getLvVoltageL1Real() {
		return lvVoltageL1Real;
	}
	public void setLvVoltageL1Real(double lvVoltageL1Real) {
		this.lvVoltageL1Real = lvVoltageL1Real;
	}
	public double getLvVoltageL1Imag() {
		return lvVoltageL1Imag;
	}
	public void setLvVoltageL1Imag(double lvVoltageL1Imag) {
		this.lvVoltageL1Imag = lvVoltageL1Imag;
	}
	public double getLvVoltageL1Abs() {
		return lvVoltageL1Abs;
	}
	public void setLvVoltageL1Abs(double lvVoltageL1Abs) {
		this.lvVoltageL1Abs = lvVoltageL1Abs;
	}
	
	
	public double getLvVoltageL2Real() {
		return lvVoltageL2Real;
	}
	public void setLvVoltageL2Real(double lvVoltageL2Real) {
		this.lvVoltageL2Real = lvVoltageL2Real;
	}
	public double getLvVoltageL2Imag() {
		return lvVoltageL2Imag;
	}
	public void setLvVoltageL2Imag(double lvVoltageL2Imag) {
		this.lvVoltageL2Imag = lvVoltageL2Imag;
	}
	public double getLvVoltageL2Abs() {
		return lvVoltageL2Abs;
	}
	public void setLvVoltageL2Abs(double lvVoltageL2Abs) {
		this.lvVoltageL2Abs = lvVoltageL2Abs;
	}
	
	
	public double getLvVoltageL3Real() {
		return lvVoltageL3Real;
	}
	public void setLvVoltageL3Real(double lvVoltageL3Real) {
		this.lvVoltageL3Real = lvVoltageL3Real;
	}
	public double getLvVoltageL3Imag() {
		return lvVoltageL3Imag;
	}
	public void setLvVoltageL3Imag(double lvVoltageL3Imag) {
		this.lvVoltageL3Imag = lvVoltageL3Imag;
	}
	public double getLvVoltageL3Abs() {
		return lvVoltageL3Abs;
	}
	public void setLvVoltageL3Abs(double lvVoltageL3Abs) {
		this.lvVoltageL3Abs = lvVoltageL3Abs;
	}
	
	
	public double getLvCurrentL1() {
		return lvCurrentL1;
	}
	public void setLvCurrentL1(double lvCurrentL1) {
		this.lvCurrentL1 = lvCurrentL1;
	}
	public double getLvCurrentL2() {
		return lvCurrentL2;
	}
	public void setLvCurrentL2(double lvCurrentL2) {
		this.lvCurrentL2 = lvCurrentL2;
	}
	public double getLvCurrentL3() {
		return lvCurrentL3;
	}
	public void setLvCurrentL3(double lvCurrentL3) {
		this.lvCurrentL3 = lvCurrentL3;
	}
	
	
	public double getLvCosPhiL1() {
		return lvCosPhiL1;
	}
	public void setLvCosPhiL1(double lvCosPhiL1) {
		this.lvCosPhiL1 = lvCosPhiL1;
	}
	public double getLvCosPhiL2() {
		return lvCosPhiL2;
	}
	public void setLvCosPhiL2(double lvCosPhiL2) {
		this.lvCosPhiL2 = lvCosPhiL2;
	}
	public double getLvCosPhiL3() {
		return lvCosPhiL3;
	}
	public void setLvCosPhiL3(double lvCosPhiL3) {
		this.lvCosPhiL3 = lvCosPhiL3;
	}
	
	
	public double getLvPowerP1() {
		return lvPowerP1;
	}
	public void setLvPowerP1(double lvPowerP1) {
		this.lvPowerP1 = lvPowerP1;
	}
	public double getLvPowerQ1() {
		return lvPowerQ1;
	}
	public void setLvPowerQ1(double lvPowerQ1) {
		this.lvPowerQ1 = lvPowerQ1;
	}
	public double getLvPowerP2() {
		return lvPowerP2;
	}
	public void setLvPowerP2(double lvPowerP2) {
		this.lvPowerP2 = lvPowerP2;
	}
	public double getLvPowerQ2() {
		return lvPowerQ2;
	}
	public void setLvPowerQ2(double lvPowerQ2) {
		this.lvPowerQ2 = lvPowerQ2;
	}
	public double getLvPowerP3() {
		return lvPowerP3;
	}
	public void setLvPowerP3(double lvPowerP3) {
		this.lvPowerP3 = lvPowerP3;
	}
	public double getLvPowerQ3() {
		return lvPowerQ3;
	}
	public void setLvPowerQ3(double lvPowerQ3) {
		this.lvPowerQ3 = lvPowerQ3;
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
	
	public double getVoltageViolations() {
		return voltageViolations;
	}
	public void setVoltageViolations(double voltageViolations) {
		this.voltageViolations = voltageViolations;
	}

	public double getResidualLoadP() {
		return residualLoadP;
	}
	public void setResidualLoadP(double residualLoadP) {
		this.residualLoadP = residualLoadP;
	}

	public double getResidualLoadQ() {
		return residualLoadQ;
	}
	public void setResidualLoadQ(double residualLoadQ) {
		this.residualLoadQ = residualLoadQ;
	}

	public double getTrafoUtilization() {
		return trafoUtilization;
	}
	public void setTrafoUtilization(double trafoUtilization) {
		this.trafoUtilization = trafoUtilization;
	}

	public double getTrafoLossesP() {
		return trafoLossesP;
	}
	public void setTrafoLossesP(double trafoLossesP) {
		this.trafoLossesP = trafoLossesP;
	}
	
	public double getTrafoLossesQ() {
		return trafoLossesQ;
	}
	public void setTrafoLossesQ(double trafoLossesQ) {
		this.trafoLossesQ = trafoLossesQ;
	}
	
	public int getTapPos() {
		return tapPos;
	}
	public void setTapPos(int tapPos) {
		this.tapPos = tapPos;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object compObejct) {
		
		if (compObejct==null || !(compObejct instanceof TrafoResult)) return false;
		TrafoResult trComp = (TrafoResult) compObejct;
		
		if (trComp.getIdExecution()!=this.getIdExecution()) return false;
		
		String idComp = trComp.getIdTrafo();
		String idLocal = this.getIdTrafo();
		if (idComp==null && idLocal==null) {
			// --- equals ---
		} else if ((idComp==null && idLocal!=null) || (idComp!=null && idLocal==null)) {
			return false;
		} else {
			if (idComp.equals(idLocal)==false) return false;
		}
		
		if (trComp.getTimestamp()==null && this.getTimestamp()==null) {
			// --- equals ---
		} else if ((trComp.getTimestamp()==null && this.getTimestamp()!=null) || (trComp.getTimestamp()!=null && this.getTimestamp()==null)) {
			return false;
		} else if (trComp.getTimestamp().equals(this.getTimestamp())==false) {
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
		
		if (this.getIdTrafo()==null) {
			hashCodeString += "null";
		} else {
			hashCodeString += this.getIdTrafo();
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
		valueString += "'" + this.getIdTrafo() + "',";
		valueString += "'" + AbstractStateResult.getTimeStampAsSQLString(this.getTimestamp()) + "',";
		
		
		valueString += this.getHvVoltageL1Real() + ",";
		valueString += this.getHvVoltageL1Imag() + ",";
		valueString += this.getHvVoltageL1Abs() + ",";
		
		valueString += this.getHvVoltageL2Real() + ",";
		valueString += this.getHvVoltageL2Imag() + ",";
		valueString += this.getHvVoltageL2Abs() + ",";
		
		valueString += this.getHvVoltageL3Real() + ",";
		valueString += this.getHvVoltageL3Imag() + ",";
		valueString += this.getHvVoltageL3Abs() + ",";
		
		valueString += this.getHvCurrentL1() + ",";
		valueString += this.getHvCurrentL2() + ",";
		valueString += this.getHvCurrentL3() + ",";

		valueString += this.getHvCosPhiL1() + ",";
		valueString += this.getHvCosPhiL2() + ",";
		valueString += this.getHvCosPhiL3() + ",";
		
		valueString += this.getHvPowerP1() + ",";
		valueString += this.getHvPowerQ1() + ",";

		valueString += this.getHvPowerP2() + ",";
		valueString += this.getHvPowerQ2() + ",";
		
		valueString += this.getHvPowerP3() + ",";
		valueString += this.getHvPowerQ3() + ",";
		
		
		
		valueString += this.getLvVoltageL1Real() + ",";
		valueString += this.getLvVoltageL1Imag() + ",";
		valueString += this.getLvVoltageL1Abs() + ",";
		
		valueString += this.getLvVoltageL2Real() + ",";
		valueString += this.getLvVoltageL2Imag() + ",";
		valueString += this.getLvVoltageL2Abs() + ",";
		
		valueString += this.getLvVoltageL3Real() + ",";
		valueString += this.getLvVoltageL3Imag() + ",";
		valueString += this.getLvVoltageL3Abs() + ",";
		
		valueString += this.getLvCurrentL1() + ",";
		valueString += this.getLvCurrentL2() + ",";
		valueString += this.getLvCurrentL3() + ",";

		valueString += this.getLvCosPhiL1() + ",";
		valueString += this.getLvCosPhiL2() + ",";
		valueString += this.getLvCosPhiL3() + ",";
		
		valueString += this.getLvPowerP1() + ",";
		valueString += this.getLvPowerQ1() + ",";

		valueString += this.getLvPowerP2() + ",";
		valueString += this.getLvPowerQ2() + ",";
		
		valueString += this.getLvPowerP3() + ",";
		valueString += this.getLvPowerQ3() + ",";
		
		
		
		valueString += this.getVoltageReal() + ",";
		valueString += this.getVoltageImag() + ",";
		valueString += this.getVoltageViolations() + ",";		
		
		valueString += this.getResidualLoadP() + ",";
		valueString += this.getResidualLoadQ() + ",";
		
		valueString += this.getTrafoUtilization() + ",";
		
		valueString += this.getTrafoLossesP() + ",";
		valueString += this.getTrafoLossesQ() + ",";
		
		valueString += this.getTapPos() + ")";

		return valueString;
	}
	
}
