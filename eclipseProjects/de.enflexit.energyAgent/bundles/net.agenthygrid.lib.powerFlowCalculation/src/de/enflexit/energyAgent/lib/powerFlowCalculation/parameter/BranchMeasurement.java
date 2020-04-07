package de.enflexit.energyAgent.lib.powerFlowCalculation.parameter;

import java.util.Vector;


public class BranchMeasurement {

	private double dSensorID;
	private int nFromNode;
	private int nToNode;
	private double dInfluenceOnAutarkicGrid;
	
	// TODO: Are these required variables?
//	private ElectricalMeasurement electricalMesaurement;
//	private NetworkComponent networkComponent;

	private Vector<Double> vVoltagePerPhase = new Vector<Double>(3, 0);
	private Vector<Double> vCurrentPerPhase = new Vector<Double>(3, 0);
	private Vector<Double> vCosPhiPerPhase = new Vector<Double>(3, 0);
	private Vector<Double> vPowerRealPerPhase = new Vector<Double>(3, 0);
	private Vector<Double> vPowerImagPerPhase = new Vector<Double>(3, 0);

	public double getdSensorID() {
		return dSensorID;
	}

	/**
	 * @return the vVoltagePerPhase
	 */
	public Vector<Double> getvVoltagePerPhase() {
		return vVoltagePerPhase;
	}

	/**
	 * @param vVoltagePerPhase
	 *            the vVoltagePerPhase to set
	 */
	public void setvVoltagePerPhase(Vector<Double> vVoltagePerPhase) {
		this.vVoltagePerPhase = vVoltagePerPhase;
	}

	/**
	 * @return the vCurrentPerPhase
	 */
	public Vector<Double> getvCurrentPerPhase() {
		return vCurrentPerPhase;
	}

	/**
	 * @param vCurrentPerPhase
	 *            the vCurrentPerPhase to set
	 */
	public void setvCurrentPerPhase(Vector<Double> vCurrentPerPhase) {
		this.vCurrentPerPhase = vCurrentPerPhase;
	}

	/**
	 * @return the vCosPhiPerPhase
	 */
	public Vector<Double> getvCosPhiPerPhase() {
		return vCosPhiPerPhase;
	}

	/**
	 * @param vCosPhiPerPhase
	 *            the vCosPhiPerPhase to set
	 */
	public void setvCosPhiPerPhase(Vector<Double> vCosPhiPerPhase) {
		this.vCosPhiPerPhase = vCosPhiPerPhase;
	}

	/**
	 * @return the vPowerRealPerPhase
	 */
	public Vector<Double> getvPowerRealPerPhase() {
		return vPowerRealPerPhase;
	}

	/**
	 * @param vPowerRealPerPhase
	 *            the vPowerRealPerPhase to set
	 */
	public void setvPowerRealPerPhase(Vector<Double> vPowerRealPerPhase) {
		this.vPowerRealPerPhase = vPowerRealPerPhase;
	}

	/**
	 * @return the vPowerImagPerPhase
	 */
	public Vector<Double> getvPowerImagPerPhase() {
		return vPowerImagPerPhase;
	}

	/**
	 * @param vPowerImagPerPhase
	 *            the vPowerImagPerPhase to set
	 */
	public void setvPowerImagPerPhase(Vector<Double> vPowerImagPerPhase) {
		this.vPowerImagPerPhase = vPowerImagPerPhase;
	}

	public void setdSensorID(double dSensorID) {
		this.dSensorID = dSensorID;
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

	public double getdInfluenceOnAutarkicGrid() {
		return dInfluenceOnAutarkicGrid;
	}

	public void setdInfluenceOnAutarkicGrid(double dInfluenceOnAutarkicGrid) {
		this.dInfluenceOnAutarkicGrid = dInfluenceOnAutarkicGrid;
	}

//	public NetworkComponent getNetworkComponent() {
//		return networkComponent;
//	}
//
//	public void setNetworkComponent(NetworkComponent networkComponent) {
//		this.networkComponent = networkComponent;
//	}
//
//	public ElectricalMeasurement getElectricalMesaurement() {
//		return electricalMesaurement;
//	}
//
//	public void setElectricalMesaurement(ElectricalMeasurement electricalMesaurement) {
//		this.electricalMesaurement = electricalMesaurement;
//	}

}
