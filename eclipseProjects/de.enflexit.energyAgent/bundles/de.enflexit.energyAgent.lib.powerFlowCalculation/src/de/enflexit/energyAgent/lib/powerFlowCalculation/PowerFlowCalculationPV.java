package de.enflexit.energyAgent.lib.powerFlowCalculation;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import Jama.Matrix;
import de.enflexit.energyAgent.lib.powerFlowCalculation.PVNodeParameters;
import de.enflexit.energyAgent.lib.powerFlowCalculation.PowerFlowParameter;

/**
 ***************************************************************************
 * @author Marcel Ludwig - EVT - University of Wuppertal (BUW) 02.03.2018
 *         Description: This Class calculates the powerFlow of an Low-Voltage
 *         Grid
 ***************************************************************************
 * 
 */

public class PowerFlowCalculationPV extends AbstractPowerFlowCalculation{

	private PowerFlowParameter powerFlowParameter;
	private boolean bResult;
	
	private Vector<Vector<Double>> sJReal;
	private Vector<Vector<Double>> sJImag;
	
	private Vector<Double> qN;
	private Vector<Double> pN;
	private double[][] dX;
	private double[][] dJ;
	private Vector<Vector<Double>> vH;
	private Vector<Vector<Double>> vL;
	private Vector<Vector<Double>> vM;
	private Vector<Vector<Double>> vN;
	private Vector<Double> vDeltay;
	
	/**
	 * Constructor for PowerFlow Calculation.
	 *
	 * @param powerFlowParameter
	 *            the power flow parameter
	 */
	public PowerFlowCalculationPV() {

	}
	
	

	/**
	 * This method calculates the powerflow.
	 */
	public boolean calculate() {

		this.setbIterationProcess(true);
		this.setdIterationLimit(0.00001);
		this.setnIterationCounter(0);
		this.setnIterationCounterLimit(5);
		this.setSucessfullPFC(true);
		this.bResult=true;
		
		//Delete slackNodes
		for(int i=0;i<this.getvPVNodes().size();i++) {
			if(this.getvPVNodes().get(i).getnPVNode()==this.powerFlowParameter.getnSlackNode())
				getvNodesToDelete().add(this.getvPVNodes().get(i).getnPVNode());
		}
		
		// Building Array which includes the Columns and Rows to Delete;
//		this.setvNodesToDelete(new Vector<>());
//		for (int a = 0; a < this.getvPVNodes().size(); a++) {
//			getvNodesToDelete().add(this.getvPVNodes().get(a).getnPVNode());
//		}

		// Sort-Algorithm to Sort nNodesToDelete;
		Collections.sort(this.getvNodesToDelete());
		
		Collections.sort(this.getvPVNodes(), new Comparator<PVNodeParameters>() {
            @Override
            public int compare(PVNodeParameters pvNodeParam1, PVNodeParameters pvNodeParam2) {
                Integer pvNode1 = pvNodeParam1.getnPVNode();
                Integer pvNode2 = pvNodeParam2.getnPVNode();
                return pvNode1.compareTo(pvNode2);
            }
        });
		
		this.dX = this.powerFlowParameter.getdX();
		
		Vector<Double> nodalVoltageReal = new Vector<>();
		
		for(int i=0;i<this.getnNumNodes();i++) {
			nodalVoltageReal.add(this.getPowerFlowParameter().getdSlackVoltage());
		}
		
		// --- Set PV Nodes in dX
		for(int i=0;i<this.getvPVNodes().size();i++) {
			this.dX[this.getvPVNodes().get(i).getnPVNode()-1+this.getnNumNodes()][0]=this.getvPVNodes().get(i).getdVoltageOfPVNode();
			nodalVoltageReal.set(this.getvPVNodes().get(i).getnPVNode()-1, this.getvPVNodes().get(i).getdVoltageOfPVNode());
		}
		
		this.setNodalVoltageReal(nodalVoltageReal);
		Vector<Double > temp = new Vector<>();
		for(int i=0;i<this.getnNumNodes();i++) {
			temp.add(0.0);
		}
		
		this.setNodalVoltageImag(temp);
		
		// Newton-Raphson with PV
		while (this.isbIterationProcess()) {
			int nIterationCounter = this.getnIterationCounter() + 1;
			this.setnIterationCounter(nIterationCounter);
			
			// Calculation of BranchPower
			bResult = calculateSJ();
			calculatePnQn();

			if (bResult) {

				// Building Jacobian-Elements H,N,M,L
				bResult &= calculation_vH();
				bResult &= calculation_vN();
				bResult &= calculation_vM();
				bResult &= calculation_vL();

				// --- Changing of H, N, M, L

				changeMatrices();

				// Build Jacobian Matrix
				dJ = buildJacobianMatrix();

				// Calculation of deltay
				calculateDeltay();

				// Changes of Deltay
				int b = 0;
//				vDeltay = vDeleteRowOfVector(vDeltay, this.getnSlackNode() - 1);
				for (int a = 0; a < this.getvNodesToDelete().size(); a++) {
					vDeltay = vDeleteRowOfVector(vDeltay, this.getnNumNodes() + getvNodesToDelete().get(a) - 1 - 1 - b);
					b++;
				}

				// Solution of equatation
				double[][] dDeltay = VectorToDoubleVectorConverter(vDeltay);
				double[][] dDeltax = new double[2 * this.getnNumNodes()][1];
				try {
					dDeltax = solver(dJ, dDeltay);
				} catch (java.lang.RuntimeException e) {
					Vector<Double> nodalVoltageAbs = new Vector<Double>(this.getnNumNodes(), 0);
					for (int i = 0; i < this.getnNumNodes(); i++) {
						nodalVoltageAbs.add(0.0);
					}
					this.setNodalVoltageAbs(nodalVoltageAbs);
					bResult = false;
//					System.err.println("Problem in Power Flow Calculation: Matrix is singular");
					break;
				}

				// Building complete deltax Vector
				double[][] dDeltaxFull = new double[2 * this.getnNumNodes()][1];
				int i;
				Vector<Integer> nodesToInsert = new Vector<Integer>();
//				nodesToInsert.addElement(this.getnSlackNode() + this.getnNumNodes()- 1);
				for (i = 0; i < getvNodesToDelete().size(); i++) {
					nodesToInsert.addElement(getvNodesToDelete().get(i) + this.getnNumNodes() - 1);
				}
				Collections.sort(nodesToInsert);
				int c = 0;
				int d = 0;
				for (i = 0; i < 2 * this.getnNumNodes(); i++) {

					if (i == nodesToInsert.get(d)) {
						dDeltaxFull[i][0] = 0;
						if (d < nodesToInsert.size() - 1) {
							d++;
						}

					} else {
						dDeltaxFull[i][0] = dDeltax[c][0];
						c++;
					}

				}
				// Updating of Node-Voltage-Vector x
				calculation_x(dDeltaxFull);

				refreshNodeVoltage();
				// Checking, if powerflow calculation is ready or not
				powerFlowReady(dDeltax);

			} else {
//				System.out.println("Error in PFC.");
//				bResult = false;
			}


		}
		// --- Do PFC without PV Nodes, if there are problems
		if(this.getnIterationCounter()>=getnIterationCounterLimit()) {
			bResult = true;
			Vector<Double> vNodalVoltageReal = this.powerFlowParameter.getNodalVoltageReal();
			Vector<Double> vNodalVoltageImag = this.powerFlowParameter.getNodalVoltageImag();
			for(int i=0; i<this.getvPVNodes().size();i++) {
				vNodalVoltageReal.set(this.getvPVNodes().get(i).getnPVNode()-1, this.getvPVNodes().get(i).getdVoltageOfPVNode());
			}
			
			PowerFlowCalculation pfc = new PowerFlowCalculation();
			this.getPowerFlowParameter().setNodalPowerReal(this.getNodalPowerReal());
			this.getPowerFlowParameter().setNodalPowerImag(this.getNodalPowerImag());
			pfc.setPowerFlowParameter(this.getPowerFlowParameter());
			
			pfc.calculate();
			
			vNodalVoltageReal=pfc.getNodalVoltageReal();
			vNodalVoltageImag=pfc.getNodalVoltageImag();
			
			this.setNodalVoltageReal(vNodalVoltageReal);
			this.setNodalVoltageImag(vNodalVoltageImag);
		}
		
		// Preparation of Results
		calculateNodalCurrent();
		calculateBranchCurrent();
		calculateBranchPower();
		calculateCosPhi();
		
		calculateUtilization();
		this.setNodalVoltageAbs(vCalcluateAbs(this.getNodalVoltageReal(), this.getNodalVoltageImag()));
		this.setSucessfullPFC(true);
		return bResult;
	}

	/**
	 * This method changes the H,L,M,N matrices.
	 */
	private void changeMatrices() {
		// Changes for H
		vH = vDeleteColumn(vH, this.getnSlackNode() - 1);
		vH = vDeleteRow(vH, this.getnSlackNode() - 1);

		// Changes for N
		int b = 0;
		vN = vDeleteRow(vN, this.getnSlackNode() - 1);
		for (int a = 0; a < getvNodesToDelete().size(); a++) {
			vN = vDeleteColumn(vN, getvNodesToDelete().get(a) - 1 - b);

			b++;
		}

		// Changes for M
		b = 0;
		vM = vDeleteColumn(vM, this.getnSlackNode() - 1);
		for (int a = 0; a < getvNodesToDelete().size(); a++) {
			vM = vDeleteRow(vM, getvNodesToDelete().get(a) - 1 - b);
			b++;
		}

		// Changes for L
		b = 0;
		for (int a = 0; a < getvNodesToDelete().size(); a++) {
			vL = (vDeleteColumn(vL, getvNodesToDelete().get(a) - 1 - b));
			vL = vDeleteRow(vL, getvNodesToDelete().get(a) - 1 - b);
			b++;
		}
	}

	// iN=YKK*(uK(b)-uK(a))
	/**
	 * Calculate branch current.
	 */
	// iN=(A+jB)*(C+jD-E-jF)=(A*C-A*E-B*D+B*F)+j(BC-BE+AD-AF)
	private void calculateBranchCurrent() {
		Vector<Vector<Double>> branchCurrentReal = new Vector<Vector<Double>>();
		Vector<Vector<Double>> branchCurrentImag = new Vector<Vector<Double>>();
		Vector<Vector<Double>> branchCurrentAbs = new Vector<Vector<Double>>();
		double A, B, deltaReal, deltaImag;
		double[][] dIn_real = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] dIn_imag = new double[this.getnNumNodes()][this.getnNumNodes()];

		for (int a = 0; a < this.getnNumNodes(); a++) {
			for (int b = 0; b < this.getnNumNodes(); b++) {
				if (a != b) {
					A = powerFlowParameter.getYkkReal().get(a).get(b);
					B = powerFlowParameter.getYkkImag().get(a).get(b);
					deltaReal = this.getNodalVoltageReal().get(b) - this.getNodalVoltageReal().get(a); // uK_real
					deltaImag = this.getNodalVoltageImag().get(b) - this.getNodalVoltageImag().get(a); // uK_imag
					dIn_real[a][b] = A * deltaReal - B * deltaImag;
					dIn_imag[a][b] = B * deltaReal + A * deltaImag;
				}
			}
		}

		for (int a = 0; a < this.getnNumNodes(); a++) {
			Vector<Double> tempReal = new Vector<Double>();
			Vector<Double> tempImag = new Vector<Double>();
			for (int b = 0; b < this.getnNumNodes(); b++) {
				if(dIn_real[a][b]>200) {
					dIn_real[a][b]=60;
				}
				if(dIn_imag[a][b]>60) {
					dIn_imag[a][b]=20;
				}
				tempReal.add(dIn_real[a][b]);
				tempImag.add(dIn_imag[a][b]);

			}
			branchCurrentReal.add(tempReal);
			branchCurrentImag.add(tempImag);
			branchCurrentAbs.add(vCalcluateAbs(tempReal, tempImag));
		}
		this.setBranchCurrentReal(branchCurrentReal);
		this.setBranchCurrentImag(branchCurrentImag);
		this.setBranchCurrentAbs(branchCurrentAbs);
	}

	/**
	 * This method calculates the power of the Branch.
	 */
	private void calculateBranchPower() {
		double[][] A = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] B = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] C = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] D = new double[this.getnNumNodes()][this.getnNumNodes()];
		Vector<Vector<Double>> branchPowerReal = new Vector<Vector<Double>>();
		Vector<Vector<Double>> branchPowerImag = new Vector<Vector<Double>>();
		// A= diag(uK_imag) B=diag(uk_imag)
		for (int a = 0; a < this.getnNumNodes(); a++) {
			for (int b = 0; b < this.getnNumNodes(); b++) {
				if (a == b) {
					A[a][b] = this.getNodalVoltageReal().get(a);
					B[a][b] = this.getNodalVoltageImag().get(a);
				}
				C[a][b] = this.getBranchCurrentReal().get(a).get(b);
				D[a][b] = this.getBranchCurrentImag().get(a).get(b);
			}
		}

		// sN=3*diag(uK)*conj(iN)
		// sN= (A+jB)*(C+jD)* = (AC+BD)+j(BC-AD)
		for (int a = 0; a < this.getnNumNodes(); a++) {

			Vector<Double> tempReal = new Vector<Double>();
			Vector<Double> tempImag = new Vector<Double>();
			for (int b = 0; b < this.getnNumNodes(); b++) {

				double Term1 = 0;
				double Term2 = 0;
				for (int c = 0; c < this.getnNumNodes(); c++) {
					Term1 = Term1 + A[a][c] * C[c][b] + B[a][c] * D[c][b];
					Term2 = Term2 + B[a][c] * C[c][b] - A[a][c] * D[c][b];
				}
				tempReal.add(Term1);
				tempImag.add(Term2);
			}
			branchPowerReal.add(tempReal);
			branchPowerImag.addElement(tempImag);
		}
		this.setBranchPowerReal(branchPowerReal);
		this.setBranchPowerImag(branchPowerImag);
	}

	/**
	 * Calculate_utilization.
	 */
	public void calculateUtilization() {
		int fromNode, toNode;
		double abs = 0;
		double[][] matgriddata = this.getPowerFlowParameter().getdMatGridData();
		int branchNumberIndex = matgriddata[0].length-1;
		for (int a = 0; a < this.getnNumBranches(); a++) {
			fromNode = (int) matgriddata[a][0];
			toNode = (int) matgriddata[a][1];
			abs = Math.sqrt(this.getBranchCurrentReal().get(fromNode - 1).get(toNode - 1) * this.getBranchCurrentReal().get(fromNode - 1).get(toNode - 1) + this.getBranchCurrentImag().get(fromNode - 1).get(toNode - 1) * this.getBranchCurrentImag().get(fromNode - 1).get(toNode - 1));
			
			double utilization = (abs / (this.getPowerFlowParameter().getMaxCurrent().get(fromNode - 1).get(toNode - 1))) * 100;
			
			// --- Store in HashMap ---------------------------------
			int branchNumber = new Double(matgriddata[a][branchNumberIndex]).intValue();
			this.getBranchUtilizationHashMap().put(branchNumber, utilization);
		}
	}


	/**
	 * Calculate nodal current.
	 */
	// ik= AC-BD +j(BC+AD)
	private void calculateNodalCurrent() {
		Vector<Double> nodalCurrentReal = new Vector<Double>();
		Vector<Double> nodalCurrentImag = new Vector<Double>();
		double Zwischenergebnis_real = 0;
		double Zwischenergebnis_imag = 0;
		double A;
		double B;
		double C;
		double D;
		for (int a = 0; a < this.getnNumNodes(); a++) {
			for (int b = 0; b < this.getnNumNodes(); b++) {
				A = powerFlowParameter.getYkkReal().get(a).get(b);
				B = powerFlowParameter.getYkkImag().get(a).get(b);
				C = this.getNodalVoltageReal().get(b); // uK_real
				D = this.getNodalVoltageImag().get(b); // uK_imag
				Zwischenergebnis_real = Zwischenergebnis_real + A * C - B * D;
				Zwischenergebnis_imag = Zwischenergebnis_imag + B * C + A * D;
			}
			nodalCurrentReal.add(Zwischenergebnis_real);
			nodalCurrentImag.add(Zwischenergebnis_imag);

			Zwischenergebnis_real = 0;
			Zwischenergebnis_imag = 0;
		}
		
		this.setNodalCurrentReal(nodalCurrentReal);
		this.setNodalCurrentImag(nodalCurrentImag);
		this.setNodalCurrentAbs(this.vCalcluateAbs(nodalCurrentReal, nodalCurrentImag));
	}

	/**
	 * Calculates cos phi of each node.
	 */
	private void calculateCosPhi() {
		Vector<Double> nodalCosPhi = new Vector<Double>();
		for (int i = 0; i < this.getnNumNodes(); i++) {
			// cosPhi= P/S
			if (Math.abs(this.getNodalPowerReal().get(i)) == 0) {
				nodalCosPhi.add(1.0);
			} else {
				nodalCosPhi.add(Math.abs(this.getPowerFlowParameter().getNodalPowerReal().get(i)) / Math.sqrt(
						this.getPowerFlowParameter().getNodalPowerReal().get(i) * this.getPowerFlowParameter().getNodalPowerReal().get(i) + this.getPowerFlowParameter().getNodalPowerImag().get(i) * this.getPowerFlowParameter().getNodalPowerImag().get(i)));

			}
		}
		this.setNodalCosPhi(nodalCosPhi);
		
		double pN = 0;
		double qN = 0;
		
		int fromNode, toNode, branchNumber;
		for (int i = 0; i < this.getnNumBranches(); i++) {
			branchNumber = this.getBranchNumbersVector().get(i);
			fromNode = this.getBranchFromNodesHashMap().get(branchNumber);
			toNode = this.getBranchToNodesHashMap().get(branchNumber);
			
			pN = this.getBranchPowerReal().get(fromNode).get(toNode);
			qN = this.getBranchPowerImag().get(fromNode).get(toNode);
			
			// cosPhi= P/S
			if (Math.abs(pN) == 0) {
				this.getBranchCosPhiHashMap().put(branchNumber, 1.0);
			} else {
				this.getBranchCosPhiHashMap().put(branchNumber, Math.abs(pN) / Math.sqrt(pN * pN + qN * qN));
			}
		}
	}

	/**
	 * Refresh node voltage.
	 */
	private void refreshNodeVoltage() {
		Vector<Double> nodalVoltageRealIntern = new Vector<Double>();
		Vector<Double> nodalVoltageImagIntern = new Vector<Double>();
		double abs = 0;
		for (int a = 0; a < this.getnNumNodes(); a++) {
			abs = Math.sqrt(this.getNodalVoltageReal().get(a) * this.getNodalVoltageReal().get(a)+ this.getNodalVoltageImag().get(a) * this.getNodalVoltageImag().get(a));
			nodalVoltageRealIntern.add(abs * dX[a + this.getnNumNodes()][0] * Math.cos(dX[a][0]));
			nodalVoltageImagIntern.add(abs * dX[a + this.getnNumNodes()][0] * Math.sin(dX[a][0]));
		}
		this.setNodalVoltageReal(nodalVoltageRealIntern);
		this.setNodalVoltageImag(nodalVoltageImagIntern);
	}

	/**
	 * Calculation the dX Vector
	 *
	 * @param dDeltaxFull
	 *            the d deltax full
	 */
	private void calculation_x(double[][] dDeltaxFull) {

		double[][] dXtemp = new double[2 * this.getnNumNodes()][1];
		for (int a = 0; a < this.getnNumNodes(); a++) {
			dXtemp[a][0] = dX[a][0];
		}

		for (int a = 0; a < this.getnNumNodes(); a++) {
			dX[a][0] = dXtemp[a][0] + dDeltaxFull[a][0];
			dX[a + this.getnNumNodes()][0] = 1 + dDeltaxFull[a + this.getnNumNodes()][0];
		}
	}

	/**
	 * Power flow ready. Checks if powerflow is ready
	 * 
	 * @param dDeltax
	 *            the d deltax
	 */
	private void powerFlowReady(double[][] dDeltax) {
		double max = 0;
		int a;
		for (a = 0; a < dDeltax.length; a++) {
			if (max < Math.abs(dDeltax[a][0])) {
				max = Math.abs(dDeltax[a][0]);
			}
		}
		// Checking, if result is exact enough
		if (max < this.getdIterationLimit()) {
			//sucessfullPFC = false;// Algorithm ready
			this.setSucessfullPFC(false);
			this.setbIterationProcess(false);
		} else {
			//sucessfullPFC = true; // Algorithm not ready yet
			this.setSucessfullPFC(true); 
			this.setbIterationProcess(true);
		}
		// Checking, if iteration of powerflow are higher than IterationLimit
		if (this.getnIterationCounter() > this.getnIterationCounterLimit()) {
			//sucessfullPFC = false;// Algorithm ready
			this.setSucessfullPFC(false);
			this.setbIterationProcess(false);
		}
	}

	/**
	 * Vector to double vector converter.
	 *
	 * @param VectorArray
	 *            the vector array
	 * @return the double[][]
	 */
	private double[][] VectorToDoubleVectorConverter(Vector<Double> VectorArray) {
		double[][] doubleArray = new double[VectorArray.size()][1];
		for (int a = 0; a < VectorArray.size(); a++) {
			doubleArray[a][0] = VectorArray.get(a);
		}
		return doubleArray;
	}

	/**
	 * This method calculates the Abs
	 *
	 * @param real
	 *            the real
	 * @param imag
	 *            the imag
	 * @return the vector
	 */
	private Vector<Double> vCalcluateAbs(Vector<Double> real, Vector<Double> imag) {
		int nSize = real.size();
		Vector<Double> abs = new Vector<Double>();

		for (int i = 0; i < nSize; i++) {

			// --- Sign + or -, if real=+ --> +Abs
			if (real.get(i) > 0) {
				abs.add(Math.sqrt(real.get(i) * real.get(i) + imag.get(i) * imag.get(i)));
			} else {
				// --- if real=- --> -Abs
				abs.add(-1 * Math.sqrt(real.get(i) * real.get(i) + imag.get(i) * imag.get(i)));
			}
		}

		return abs;
	}

	/**
	 * This method solve the linear equation
	 *
	 * @param J
	 *            the j
	 * @param deltay
	 *            the deltay
	 * @return the double[][]
	 */
	private double[][] solver(double[][] J, double[][] deltay) {
		double[][] elems = J;
		Matrix Ab = new Matrix(elems);
		double[][] elems2 = deltay;
		Matrix bb = new Matrix(elems2);
		Matrix s = Ab.solve(bb);
		double[][] deltax = s.getArray();
		return deltax;
	}

	/**
	 * This method calculate the deltay matrice.
	 *
	 * @return true, if successful
	 */
	private boolean calculateDeltay() {
		vDeltay = new Vector<Double>(2 * this.getnNumNodes());
		int a;
		for (a = 0; a < 2 * this.getnNumNodes(); a++) {
			if (a < this.getnNumNodes()) {
				vDeltay.insertElementAt(-(pN.get(a) - this.getNodalPowerReal().get(a)), a);

			} else {
				vDeltay.insertElementAt(-(qN.get(a - this.getnNumNodes()) - this.getNodalPowerImag().get(a - this.getnNumNodes())), a);

			}
		}
		return true;
	}

	/**
	 * This method builds the jacobian matrix.
	 *
	 * @return the double[][]
	 */
	private double[][] buildJacobianMatrix() {

		int a, b;
		double[][] J = new double[2 * this.getnNumNodes() - getvNodesToDelete().size() - 1][2 * this.getnNumNodes() - getvNodesToDelete().size() - 1];

		for (a = 0; a < 2 * this.getnNumNodes() - getvNodesToDelete().size() - 1; a++) {
			if (a < this.getnNumNodes() - 1) {
				for (b = 0; b < 2 * this.getnNumNodes() - getvNodesToDelete().size() - 1; b++) {
					if (b < this.getnNumNodes() - 1) {
						J[a][b] = vH.elementAt(a).elementAt(b);
					} else {
						J[a][b] = vN.elementAt(a).elementAt(b - (this.getnNumNodes() - 1));
					}
				} // --- end for

			} else {
				for (b = 0; b < 2 * this.getnNumNodes() - getvNodesToDelete().size() - 1; b++) {
					if (b < this.getnNumNodes() - 1) {
						J[a][b] = vM.elementAt(a - (this.getnNumNodes() - 1)).elementAt(b);
					} else {
						J[a][b] = vL.elementAt(a - (this.getnNumNodes() - 1)).elementAt(b - (this.getnNumNodes() - 1));
					}
				} // end for

			}
		} // end for

		return J;
	}


	/**
	 * This method removes the selected column of the vectormatrix.
	 *
	 * @param vVectorToReduce
	 *            the vector to reduce
	 * @param nColumnToRemove
	 *            the column to remove
	 * @return the vector
	 */
	public Vector<Vector<Double>> vDeleteColumn(Vector<Vector<Double>> vVectorToReduce, int nColumnToRemove) {
		for (int a = 0; a < vVectorToReduce.size(); a++) {
			vVectorToReduce.elementAt(a).remove(nColumnToRemove);
		}
		return vVectorToReduce;
	}

	/**
	 * This method removes the selected row of the vectormatrix.
	 *
	 * @param vVectorToReduce
	 *            the vector to reduce
	 * @param nRowToRemove
	 *            the n row to remove
	 * @return the vector
	 */
	public Vector<Vector<Double>> vDeleteRow(Vector<Vector<Double>> vVectorToReduce, int nRowToRemove) {

		vVectorToReduce.remove(nRowToRemove);

		return vVectorToReduce;
	}

	/**
	 * This method deletes the row of vector.
	 *
	 * @param vVectorToReduce
	 *            the v vector to reduce
	 * @param nRowToRemove	 * @return the vector
	 */
	public Vector<Double> vDeleteRowOfVector(Vector<Double> vVectorToReduce, int nRowToRemove) {

		vVectorToReduce.remove(nRowToRemove);

		return vVectorToReduce;
	}

	/**
	 * This method calculates the Pn and Qn matrices
	 */
	private void calculatePnQn() {
		double sumReal;
		double sumImag;
		pN = new Vector<Double>();
		qN = new Vector<Double>();
		for (int a = 0; a < this.getnNumNodes(); a++) {
			sumReal = 0;
			sumImag = 0;
			for (int b = 0; b < this.getnNumNodes(); b++) {
				sumReal = sumReal + sJReal.get(a).get(b);
				sumImag = sumImag + sJImag.get(a).get(b);
			}
			pN.add(sumReal);
			qN.add(sumImag);
		}
	}

	/**
	 * Method calculates the H matrice
	 *
	 * @return true, if successful
	 */
	private boolean calculation_vH() {
		vH = new Vector<Vector<Double>>();
		for (int a = 0; a < this.getnNumNodes(); a++) {
			Vector<Double> vHtemp = new Vector<Double>();
			for (int b = 0; b < this.getnNumNodes(); b++) {
				if (a == b) {
					vHtemp.insertElementAt(sJImag.get(a).get(b) - qN.get(a), b);
				} else {
					vHtemp.insertElementAt(sJImag.get(a).get(b), b);
				}
			}
			vH.insertElementAt(vHtemp, a);
		}
		return true;
	}

	/**
	 * Method calculates the N matrice
	 *
	 * @return true, if successful
	 */
	private boolean calculation_vN() {
		vN = new Vector<Vector<Double>>();
		for (int a = 0; a < this.getnNumNodes(); a++) {
			Vector<Double> vNtemp = new Vector<Double>();
			for (int b = 0; b < this.getnNumNodes(); b++) {
				if (a == b) {
					vNtemp.insertElementAt(sJReal.get(a).get(b) + pN.get(a), b);
				} else {
					vNtemp.insertElementAt(sJReal.get(a).get(b), b);
				}
			}
			vN.insertElementAt(vNtemp, a);
		}
		return true;
	}

	/**
	 * Method calculates the M matrice
	 *
	 * @return true, if successful
	 */
	private boolean calculation_vM() {
		vM = new Vector<Vector<Double>>();
		for (int a = 0; a < this.getnNumNodes(); a++) {
			Vector<Double> vMtemp = new Vector<Double>();
			for (int b = 0; b < this.getnNumNodes(); b++) {
				if (a == b) {
					vMtemp.insertElementAt(-sJReal.get(a).get(b) + pN.get(a), b);
				} else {
					vMtemp.insertElementAt(-sJReal.get(a).get(b), b);
				}
			}
			vM.insertElementAt(vMtemp, a);
		}
		return true;
	}

	/**
	 * Method calculates the L matrice
	 *
	 * @return true, if successful
	 */
	private boolean calculation_vL() {
		vL = new Vector<Vector<Double>>();
		for (int a = 0; a < this.getnNumNodes(); a++) {
			Vector<Double> vLtemp = new Vector<Double>();
			for (int b = 0; b < this.getnNumNodes(); b++) {
				if (a == b) {
					vLtemp.insertElementAt(sJImag.get(a).get(b) + qN.get(a), b);
				} else {
					vLtemp.insertElementAt(sJImag.get(a).get(b), b);
				}
			}
			vL.insertElementAt(vLtemp, a);
		}
		return true;
	}

	/**
	 * Method calculates the Sj matrice
	 *
	 * @return true, if successful
	 */
	private boolean calculateSJ() {
		double[][] A = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] B = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] C = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] D = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] CEDF = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] DECF = new double[this.getnNumNodes()][this.getnNumNodes()];
		sJReal = new Vector<Vector<Double>>();
		sJImag = new Vector<Vector<Double>>();

		// A= diag(uK_imag) B=diag(uk_imag)
		for (int a = 0; a < this.getnNumNodes(); a++) {
			for (int b = 0; b < this.getnNumNodes(); b++) {
				if (a == b) {
					A[a][b] = this.getNodalVoltageReal().get(a);
					B[a][b] = this.getNodalVoltageImag().get(a);
				}
				C[a][b] = this.getYkkReal().get(a).get(b);
				D[a][b] = this.getYkkImag().get(a).get(b);
			}
		}

		// Construction of CEDF= C*E-DF and DECF= D*E+C*F
		for (int a = 0; a < this.getnNumNodes(); a++) {

			for (int b = 0; b < this.getnNumNodes(); b++) {
				double Term1 = 0;
				double Term2 = 0;
				for (int c = 0; c < this.getnNumNodes(); c++) {
					Term1 = Term1 + C[a][c] * A[c][b] - D[a][c] * B[c][b];
					Term2 = Term2 + D[a][c] * A[c][b] + C[a][c] * B[c][b];
				}
				CEDF[a][b] = Term1;
				DECF[a][b] = Term2;
			}
		}
		// Construction SJ_real=A*CEDF+ B*DECF and SJ_imag=(B*CEDF-A*DECF)
		for (int a = 0; a < this.getnNumNodes(); a++) {
			Vector<Double> tempReal = new Vector<Double>();
			Vector<Double> tempImag = new Vector<Double>();

			for (int b = 0; b < this.getnNumNodes(); b++) {
				double real = 0;
				double imag = 0;
				for (int c = 0; c < this.getnNumNodes(); c++) {
					real = real + A[a][c] * CEDF[c][b] + B[a][c] * DECF[c][b];
					imag = imag - A[a][c] * DECF[c][b] + B[a][c] * CEDF[c][b];
				}
				tempReal.add(real);
				tempImag.add(imag);

			}
			sJReal.add(tempReal);
			sJImag.add(tempImag);
		}

		return true;
	}

	public PowerFlowParameter getPowerFlowParameter() {
		return powerFlowParameter;
	}

	public void setPowerFlowParameter(PowerFlowParameter powerFlowParameter) {
		this.powerFlowParameter = powerFlowParameter;
	}


	@Override
	public boolean checkPreconditionsForCalculation() {
		return (this.getPowerFlowParameter().getvPVNodes()!=null && this.getPowerFlowParameter().getvPVNodes().size()>0);
	}

}
