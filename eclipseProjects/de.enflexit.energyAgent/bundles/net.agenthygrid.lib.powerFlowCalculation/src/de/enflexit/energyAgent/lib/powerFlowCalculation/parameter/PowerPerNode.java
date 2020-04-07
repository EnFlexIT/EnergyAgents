package de.enflexit.energyAgent.lib.powerFlowCalculation.parameter;

public class PowerPerNode {
	private int nNode;
	private Complex complexPowerPerPhase;
	
	public PowerPerNode(int nNode, Complex complexPowerPerPhase){
		this.nNode=nNode;
		this.complexPowerPerPhase=complexPowerPerPhase;
	}
	public PowerPerNode(){
		this.nNode=0;
		this.complexPowerPerPhase= new Complex(0,0);
	}
	public int getnNode() {
		return nNode;
	}
	public void setnNode(int nNode) {
		this.nNode = nNode;
	}
	public Complex getComplexPowerPerPhase() {
		return complexPowerPerPhase;
	}
	public void setComplexPowerPerPhase(Complex complexPowerPerPhase) {
		this.complexPowerPerPhase = complexPowerPerPhase;
	}
}
