package de.enflexit.energyAgent.lib.powerFlowCalculation.parameter;

public class BranchParams {

	private int nFromNode;
	private int nToNode;
	private double dLength=0;
	private double dR=0;
	private double dX=0;
	private double dC=0;
	private double dG=0;
	
	private double dMaxCurrent;
	
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
	public double getdLength() {
		return dLength;
	}
	public void setdLength(double dLength) {
		this.dLength = dLength;
	}
	public double getdR() {
		return dR;
	}
	public void setdR(double dR) {
		this.dR = dR;
	}
	public double getdX() {
		return dX;
	}
	public void setdX(double dX) {
		this.dX = dX;
	}
	public double getdMaxCurrent() {
		return dMaxCurrent;
	}
	public void setdMaxCurrent(double dMaxCurrent) {
		this.dMaxCurrent = dMaxCurrent;
	}
	public double getdC() {
		return dC;
	}
	public void setdC(double dC) {
		this.dC = dC;
	}
	public double getdG() {
		return dG;
	}
	public void setdG(double dG) {
		this.dG = dG;
	}
}
