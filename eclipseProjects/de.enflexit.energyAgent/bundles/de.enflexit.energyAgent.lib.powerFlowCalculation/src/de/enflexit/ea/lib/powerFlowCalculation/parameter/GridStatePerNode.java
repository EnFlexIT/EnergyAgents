package de.enflexit.ea.lib.powerFlowCalculation.parameter;

import java.util.Vector;

public class GridStatePerNode {
	
	private int nLocalNode;
	private String globalName;
	// --- Vector of nodal Voltage
	private Vector<Double> vNodalVoltageReal = new Vector<Double>();
	private Vector<Double> vNodalVoltageImag = new Vector<Double>();
	private Vector<Double> vNodalVoltageAbs = new Vector<Double>();
	
	private Vector<Double> vNodalPowerReal = new Vector<Double>();
	private Vector<Double> vNodalPowerImag = new Vector<Double>();
	private Vector<Double> vNodalPowerApparent = new Vector<Double>();
	
	private long timeStamp;
	/**
	 * @return the nNode
	 */
	public int getnNode() {
		return nLocalNode;
	}
	/**
	 * @param nNode the nNode to set
	 */
	public void setnNode(int nNode) {
		this.nLocalNode = nNode;
	}
	/**
	 * @return the vNodalVoltageReal
	 */
	/**
	 * @return the vNodalVoltageReal
	 */
	public Vector<Double> getvNodalVoltageReal() {
		return vNodalVoltageReal;
	}
	/**
	 * @param vNodalVoltageReal the vNodalVoltageReal to set
	 */
	public void setvNodalVoltageReal(Vector<Double> vNodalVoltageReal) {
		this.vNodalVoltageReal = vNodalVoltageReal;
	}
	/**
	 * @return the vNodalVoltageImag
	 */
	public Vector<Double> getvNodalVoltageImag() {
		return vNodalVoltageImag;
	}
	/**
	 * @param vNodalVoltageImag the vNodalVoltageImag to set
	 */
	public void setvNodalVoltageImag(Vector<Double> vNodalVoltageImag) {
		this.vNodalVoltageImag = vNodalVoltageImag;
	}
	/**
	 * @return the vNodalVoltageAbs
	 */
	public Vector<Double> getvNodalVoltageAbs() {
		return vNodalVoltageAbs;
	}
	/**
	 * @param vNodalVoltageAbs the vNodalVoltageAbs to set
	 */
	public void setvNodalVoltageAbs(Vector<Double> vNodalVoltageAbs) {
		this.vNodalVoltageAbs = vNodalVoltageAbs;
	}
	/**
	 * @return the vNodalPowerReal
	 */
	public Vector<Double> getvNodalPowerReal() {
		return vNodalPowerReal;
	}
	/**
	 * @param vNodalPowerReal the vNodalPowerReal to set
	 */
	public void setvNodalPowerReal(Vector<Double> vNodalPowerReal) {
		this.vNodalPowerReal = vNodalPowerReal;
	}
	/**
	 * @return the vNodalPowerImag
	 */
	public Vector<Double> getvNodalPowerImag() {
		return vNodalPowerImag;
	}
	/**
	 * @param vNodalPowerImag the vNodalPowerImag to set
	 */
	public void setvNodalPowerImag(Vector<Double> vNodalPowerImag) {
		this.vNodalPowerImag = vNodalPowerImag;
	}
	/**
	 * @return the vNodalPowerApparent
	 */
	public Vector<Double> getvNodalPowerApparent() {
		return vNodalPowerApparent;
	}
	/**
	 * @param vNodalPowerApparent the vNodalPowerApparent to set
	 */
	public void setvNodalPowerApparent(Vector<Double> vNodalPowerApparent) {
		this.vNodalPowerApparent = vNodalPowerApparent;
	}
	public long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String getGlobalName() {
		return globalName;
	}
	public void setGlobalName(String globalName) {
		this.globalName = globalName;
	}

}
