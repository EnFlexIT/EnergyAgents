package de.enflexit.ea.lib.powerFlowCalculation.parameter;

public class NodeAssociationParams {

	private int nNode;
	private boolean isLoadNode;
	private double dPowerAbs;
	private double dActivePower;
	private double dReactivePower;
	
	
	public int getnNode() {
		return nNode;
	}
	public void setnNode(int nNode) {
		this.nNode = nNode;
	}
	public boolean isLoadNode() {
		return isLoadNode;
	}
	public void setLoadNode(boolean isLoadNode) {
		this.isLoadNode = isLoadNode;
	}
	public double getPower() {
		return dPowerAbs;
	}
	public void setPower(double power) {
		dPowerAbs = power;
	}
	public double getdPowerAbs() {
		return dPowerAbs;
	}
	public void setdPowerAbs(double dPowerAbs) {
		this.dPowerAbs = dPowerAbs;
	}
	public double getdActivePower() {
		return dActivePower;
	}
	public void setdActivePower(double dActivePower) {
		this.dActivePower = dActivePower;
	}
	public double getdReactivePower() {
		return dReactivePower;
	}
	public void setdReactivePower(double dReactivePower) {
		this.dReactivePower = dReactivePower;
	}
	
}
