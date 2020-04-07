package de.enflexit.energyAgent.lib.powerFlowCalculation;

public class PVNodeParameters {
	private int nPVNode;
	private double dVoltageOfPVNode;
	private String networkComponent;
	
	public PVNodeParameters() {
		// TODO Auto-generated constructor stub
	}
	
	public PVNodeParameters(int nPVNode, double dVoltageOfPVNode, String networkComponent){
		this.setnPVNode(nPVNode);
		this.setdVoltageOfPVNode(dVoltageOfPVNode);
		this.setNetworkComponent(networkComponent);
	}
	
	public String getNetworkComponentName() {
		return networkComponent;
	}

	public void setNetworkComponent(String networkComponent) {
		this.networkComponent = networkComponent;
	}

	public int getnPVNode() {
		return nPVNode;
	}
	public void setnPVNode(int nPVNode) {
		this.nPVNode = nPVNode;
	}
	public double getdVoltageOfPVNode() {
		return dVoltageOfPVNode;
	}
	public void setdVoltageOfPVNode(double dVoltageOfPVNode) {
		this.dVoltageOfPVNode = dVoltageOfPVNode;
	}
	

}
