package de.enflexit.ea.lib.powerFlowCalculation.parameter;

public class NodeAssignment {

	private int nLocalNodeNumber;
	private int nGlobalNodeNumber;
	
	
	public int getnLocalNodeNumber() {
		return nLocalNodeNumber;
	}
	public void setnLocalNodeNumber(int nLocalNodeNumber) {
		this.nLocalNodeNumber = nLocalNodeNumber;
	}
	public int getnGlobalNodeNumber() {
		return nGlobalNodeNumber;
	}
	public void setnGlobalNodeNumber(int nGlobalNodeNumber) {
		this.nGlobalNodeNumber = nGlobalNodeNumber;
	}
}
