package de.enflexit.energyAgent.lib.powerFlowCalculation;

public class MeasuredBranchCurrent {
	private int nFromNode;
	private int nToNode;
	private String sensorName; 
	private double current;
	private String nFromNodeComponentName;
	private String nToNodeComponentName;
	
	public String getnFromNodeComponentName() {
		return nFromNodeComponentName;
	}
	public void setnFromNodeComponentName(String nFromNodeComponentName) {
		this.nFromNodeComponentName = nFromNodeComponentName;
	}
	public String getnToNodeComponentName() {
		return nToNodeComponentName;
	}
	public void setnToNodeComponentName(String nToNodeComponentName) {
		this.nToNodeComponentName = nToNodeComponentName;
	}
	public int getnFromNode() {
		return nFromNode;
	}
	public void setnFromNode(int nFromNode) {
		this.nFromNode = nFromNode;
	}
	public int getnToNode() {
		return nToNode;
	}
	public void setnToNode(int nToNode) {
		this.nToNode = nToNode;
	}
	public String getSensorName() {
		return sensorName;
	}
	public void setSensorName(String sensorName) {
		this.sensorName = sensorName;
	}
	public double getCurrent() {
		return current;
	}
	public void setCurrent(double current) {
		this.current = current;
	}

}
