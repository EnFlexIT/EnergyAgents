package de.enflexit.energyAgent.lib.powerFlowCalculation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import Jama.Matrix;

public class PowerFlowCalculation extends AbstractPowerFlowCalculation {

	private boolean debugPrintVectorSizes = false;
	
	private boolean errorShown = false;
	
	private double[][] dX;

	/**
	 * Does the power flow calculation by using the specified active and reactive
	 * power.
	 * 
	 * @param nodePowerPairs the HashMap of nodeID to {@link ActiveReactivePowerPair}
	 */
	public boolean calculate() {
		try {
			// --- Print vector size information ------------------------
			this.debugPrintVectorInformation();
			
			// --- Check if all nodal powers are given
			if (!this.checkNumberOfNodalPowers()) return false;
			
			
			// --- Do the the network calculation -----------------------
			return this.calculatePF();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	

	private boolean checkNumberOfNodalPowers() {
		
		if (this.getNodalPowerReal().size() == this.getnNumNodes() && this.getNodalPowerImag().size() == this.getnNumNodes()) {
			this.errorShown = false;	// Reset error reminder
			return true;
		} else {

			// --- Show error message only once ---------------------
			if (errorShown==false) {
				int missingActivePowers = this.getnNumNodes() - this.getNodalPowerReal().size();
				int missingReactivePowers = this.getnNumNodes() - this.getNodalPowerImag().size();
				
				System.err.println("Error in electrical PowerFlowCalculation: Not Enough Nodal Powers are given for a valid Calculation with a slack voltage of " + (int) this.getdSlackNodeVoltage() + "V.");
				System.err.println(missingActivePowers + " active powers and " + missingReactivePowers + " reactive powers are missing for a total of " + this.getnNumNodes() + " Nodes.");
				
				this.errorShown = true;
			}
			
			
			return false;
		}
	}


	/**
	 * Prints the debugPrintVectorSizes information.
	 */
	private void debugPrintVectorInformation() {

		if (debugPrintVectorSizes==false) return;
		
		String vectorSizes = "=> Vector Sizes: ";
//		vectorSizes += " " + this.debugGetVectorSizeDescription("YKK_real", this.YKK_real);
//		vectorSizes += " " + this.debugGetVectorSizeDescription("YKK_imag", this.YKK_imag);
		
		vectorSizes += " " + this.debugGetVectorSizeDescription("nodalVoltageReal", this.getNodalVoltageReal());
		vectorSizes += " " + this.debugGetVectorSizeDescription("nodalVoltageImag", this.getNodalVoltageImag());
		vectorSizes += " " + this.debugGetVectorSizeDescription("nodalVoltageAbs", this.getNodalVoltageAbs());
		
		vectorSizes += " " + this.debugGetVectorSizeDescription("nodalCurrentReal", this.getNodalCurrentReal());
		vectorSizes += " " + this.debugGetVectorSizeDescription("nodalCurrentImag", this.getNodalCurrentImag());
		

		vectorSizes += " " + this.debugGetVectorSizeDescription("NodalPowerReal", this.getNodalPowerReal());
		vectorSizes += " " + this.debugGetVectorSizeDescription("NodalPowerImag", this.getNodalPowerImag());

		vectorSizes += " " + this.debugGetVectorSizeDescription("branchPowerReal", this.getBranchPowerReal());
		vectorSizes += " " + this.debugGetVectorSizeDescription("branchPowerImag", this.getBranchPowerImag());
		
		vectorSizes += " " + this.debugGetVectorSizeDescription("branchCurrentReal", this.getBranchCurrentReal());
		vectorSizes += " " + this.debugGetVectorSizeDescription("branchCurrentImag", this.getBranchCurrentImag());
		vectorSizes += " " + this.debugGetVectorSizeDescription("branchCurrentAbs", this.getBranchCurrentAbs());
		vectorSizes += " " + this.debugGetHashMapSizeDescription("branchUtilization", this.getBranchUtilizationHashMap());

		vectorSizes += " " + this.debugGetVectorSizeDescription("powerTransformerReal", this.getPowerOfTransformer());

		vectorSizes += " " + this.debugGetVectorSizeDescription("cosPhi", this.getNodalCosPhi());
		
		System.out.println(vectorSizes);
		
	}
	/**
	 * Debug vector size.
	 *
	 * @param checkVector the check vector
	 * @return the string
	 */
	private String debugGetVectorSizeDescription(String vectorName, Vector<?> checkVector) {
		
		String vectorSizeDescription = vectorName + ": ";
		
		// --- Get the size of the Vector -----------------------
		if (checkVector==null) {
			vectorSizeDescription += "Null"; 
		} else {
			vectorSizeDescription +=  String.format("%03d", checkVector.size());
			// --- Check the size of the sub vector -----------------
			if (checkVector.size()>0) {
				if (checkVector.get(0)!=null) {
					vectorSizeDescription += " (Null)";
				} else if (checkVector.get(0) instanceof Vector<?>) {
					Vector<?> subVector = (Vector<?>) checkVector.get(0);
					vectorSizeDescription += " (" + String.format("%03d", subVector.size()) + ")";
				}
			}
		}
		return vectorSizeDescription;
	}
	
	/**
	 * Debug hash map size.
	 *
	 * @param checkHashMap the check vector
	 * @return the string
	 */
	private String debugGetHashMapSizeDescription(String hashMapName, HashMap<?,?> checkHashMap) {
		
		String hashMapSizeDescription = hashMapName + ": ";
		
		// --- Get the size of the HashMap -----------------------
		if (checkHashMap==null) {
			hashMapSizeDescription += "Null"; 
		} else {
			hashMapSizeDescription +=  String.format("%03d", checkHashMap.size());
			// --- Check the size of the sub hash map -----------------
			if (checkHashMap.size()>0) {
				if (checkHashMap.get(0)!=null) {
					hashMapSizeDescription += " (Null)";
				} else if (checkHashMap.get(0) instanceof HashMap<?,?>) {
					HashMap<?,?> subHashMap = (HashMap<?,?>) checkHashMap.get(0);
					hashMapSizeDescription += " (" + String.format("%03d", subHashMap.size()) + ")";
				}
			}
		}
		return hashMapSizeDescription;
	}
	
	/**
	 * Does the power flow calculation with the previously defined
	 * {@link PowerFlowParameter}.
	 *
	 * @param uK_real
	 * @param uK_imag
	 * @param YKK_real
	 * @param YKK_imag
	 * @param qK
	 * @param pK
	 * @param x
	 * @param nNumNodes
	 * @param nNumBranches
	 * @return the double[][] uK
	 */
	private boolean calculatePF() {
		// H,L,M,N to build Jacobian matrix J
		double[][] H = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] L = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] N = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] M = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] J = new double[2 * this.getnNumNodes()][2 * this.getnNumNodes()];
		
		// deltay und deltax for linear equation
		double[][] deltay = new double[2 * this.getnNumNodes()][1];
		// state vector
		double[][] deltax = new double[2 * this.getnNumNodes()][1];
		// part of H,L,M,N
		double[][] qN = new double[this.getnNumNodes()][1];
		double[][] pN = new double[this.getnNumNodes()][1];
		
		// nodal voltage vector
		Vector<Double> uK_real = new Vector<Double>();
		Vector<Double> uK_imag = new Vector<Double>();
		double[][] x = new double[2 * this.getnNumNodes()][1];
		boolean IterationProcess = true;
		double IterationLimit = 0.0001;
		int IterationCounter = 0;
		int IterationCounterLimit = 10;
		Vector<Double> uK_new_real= new Vector<Double>();
		Vector<Double> uK_new_imag= new Vector<Double>();
		
//		dX = this.getPowerFlowParameter().getdX();
		double dSlackVoltage = this.getPowerFlowParameter().getdSlackVoltage();
		for(int i=0;i<this.getnNumNodes();i++) {
			uK_new_real.add(dSlackVoltage);
			uK_new_imag.add(0.0);
		}
		this.calculatex(uK_new_real, uK_new_imag);
		this.setNodalVoltageReal(uK_new_real);
		this.setNodalVoltageImag(uK_new_imag);
		
		// --- Loop for  PFC
		while (IterationProcess) {
			IterationCounter = IterationCounter + 1;
			calculation_SJ();
			pN = calculation_pN();
			qN = calculation_qN();
			H = calculation_H(qN);
			N = calculation_N(pN);
			M = calculation_M(pN);
			L = calculation_L(qN);
			J = calculation_J(H, N, L, M);
			deltay = calculation_deltay(pN, qN);

			double[][] elems = J;
			Matrix Ab = new Matrix(elems);
			double[][] elems2 = deltay;
			Matrix bb = new Matrix(elems2);
			Matrix s = Ab.solve(bb);
			deltax = s.getArray();
			uK_real = this.deepClone(this.getNodalVoltageReal());
			uK_imag = this.deepClone(this.getNodalVoltageImag());
			
			x= calculation_x(dX, deltax, this.getnNumNodes());
			dX=x;
			uK_new_real = refreshNodeVoltage_real(x, uK_real, uK_imag, this.getnNumNodes());
			uK_new_imag = refreshNodeVoltage_imag(x, uK_real, uK_imag, this.getnNumNodes());
			IterationProcess = false;
			IterationProcess = powerFlowReady(deltax, IterationLimit, this.getnNumNodes(), IterationCounter,IterationCounterLimit);
			this.setNodalVoltageReal(uK_new_real);
			this.setNodalVoltageImag(uK_new_imag);

		}
		
		// --- Refreshing nodal Voltage Abs
		Vector<Double> vNodalVoltageReal = uK_new_real;
		if(this.getvPVNodes()!=null) {
			for(int i=0; i<this.getvPVNodes().size();i++) {
				vNodalVoltageReal.set(this.getvPVNodes().get(i).getnPVNode()-1, this.getvPVNodes().get(i).getdVoltageOfPVNode());
			}
		}
		uK_new_real =vNodalVoltageReal;
		
		Vector<Double> nodalVoltageAbs = new Vector<Double>();
		for (int c = 0; c < this.getnNumNodes(); c++) {
			nodalVoltageAbs.add(Math.sqrt(uK_new_real.get(c) * uK_new_real.get(c) + uK_new_imag.get(c) * uK_new_imag.get(c)));
		}
		
		this.setNodalVoltageAbs(nodalVoltageAbs);
		this.setNodalVoltageReal(uK_new_real);
		this.setNodalVoltageImag(uK_new_imag);
		this.getPowerFlowParameter().setNodalVoltageReal(uK_new_real);
		this.getPowerFlowParameter().setNodalVoltageImag(uK_new_imag);
		this.calculateCosPhi();
		this.calculation_ik();
		this.calculation_sK();
		// --- Workaround --------
		if(this.getEstimatedBranchCurrents()!=null&&this.getEstimatedBranchCurrents().size()>0) {
			this.calculateBranchCurrents(this.getEstimatedBranchCurrents());}
		else {
			this.calculation_iN();
		}
		//_________
		this.calculation_sN();
		this.calculateCosPhiBranches();
		this.calculate_utilization();
		this.calculatePowerTransformer();
		return this.isSucessfullPFC();
	}
	/**
	 * Workaround for calculating branch Currents
	 */
	private void calculateBranchCurrents(HashMap<String,MeasuredBranchCurrent> estimatedBranchCurrents) {
		Vector<Vector<Double>> branchCurrentReal = new Vector<>();
		Vector<Vector<Double>> branchCurrentImag = new Vector<>();
		Vector<Vector<Double>> branchCurrentAbs  = new Vector<>();
		
		double[][] dIn_real = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] dIn_imag = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] dIn_abs = new double[this.getnNumNodes()][this.getnNumNodes()];
		
		if(estimatedBranchCurrents!=null&&estimatedBranchCurrents.size()>0) {
			ArrayList<String> keySet = new ArrayList<>(estimatedBranchCurrents.keySet());
			for(int i=0;i<keySet.size();i++) {
				String cableName = keySet.get(i);
				int nFromNode = estimatedBranchCurrents.get(cableName).getnFromNode();
				int nToNode = estimatedBranchCurrents.get(cableName).getnToNode();
				
				dIn_abs[nFromNode-1][nToNode-1]=estimatedBranchCurrents.get(cableName).getCurrent();
				dIn_abs[nToNode-1][nFromNode-1]=estimatedBranchCurrents.get(cableName).getCurrent();
				dIn_real[nFromNode-1][nToNode-1]=estimatedBranchCurrents.get(cableName).getCurrent();
				dIn_real[nToNode-1][nFromNode-1]=estimatedBranchCurrents.get(cableName).getCurrent();
				dIn_imag[nFromNode-1][nToNode-1]=0;
				dIn_imag[nToNode-1][nFromNode-1]=0;
				
			}
		}
		
		for (int a = 0; a < this.getnNumNodes(); a++) {
			Vector<Double> tempReal = new Vector<Double>();
			Vector<Double> tempImag = new Vector<Double>();
			Vector<Double> tempAbs = new Vector<Double>();
			for (int b = 0; b < this.getnNumNodes(); b++) {
				tempReal.add(dIn_real[a][b]);
				tempImag.add(dIn_imag[a][b]);
				tempAbs.add(dIn_abs[a][b]);

			}
			branchCurrentReal.add(tempReal);
			branchCurrentImag.add(tempImag);
			branchCurrentAbs.add(tempAbs);
		}
		
		
		this.setBranchCurrentReal(branchCurrentReal);
		this.setBranchCurrentImag(branchCurrentImag);
		this.setBranchCurrentAbs(branchCurrentAbs);
	}

	private void calculatex(Vector<Double> nodalVoltageReal, Vector<Double> nodalVoltageImag) {
		int nNumNodes = nodalVoltageReal.size();
		double[][] X = new double[2 * nNumNodes][1];
		int a;
		for (a = 0; a < nNumNodes; a++) {
			if (nodalVoltageReal.get(a) == 0) {
				X[a][0] = Math.PI;
			} else if (nodalVoltageImag.get(a) == 0) {
				X[a][0] = 0;
			} else {
				X[a][0] = Math.tan(nodalVoltageImag.get(a) / nodalVoltageReal.get(a));
			}
		}
		for (a = nNumNodes; a < 2 * nNumNodes; a++) {
			X[a][0] = Math.sqrt(nodalVoltageReal.get(a - nNumNodes) * nodalVoltageReal.get(a - nNumNodes) + nodalVoltageImag.get(a - nNumNodes) * nodalVoltageImag.get(a - nNumNodes));
		}
		this.dX=X;
	}
	

	/**
	 * This method calculates the cosPhi of a Branch cosPhi= P/S, P and S are the
	 * power of branch
	 */
	private void calculateCosPhiBranches() {
		
		double iNReal = 0;
		double iNAbs = 0;
		
		int fromNode, toNode, branchNumber;
		for (int i = 0; i < this.getnNumBranches(); i++) {
			branchNumber = this.getBranchNumbersVector().get(i);
			fromNode = this.getBranchFromNodesHashMap().get(branchNumber);
			toNode = this.getBranchToNodesHashMap().get(branchNumber);
			
			iNReal = this.getBranchCurrentReal().get(fromNode-1).get(toNode-1);
			iNAbs =  this.getBranchCurrentAbs().get(fromNode-1).get(toNode-1);
			// --- Checking, if apparent Power is zero
			if (iNAbs != 0) {
				getBranchCosPhiHashMap().put(branchNumber,Math.abs(iNReal) / Math.abs(iNAbs));
			} else {
				getBranchCosPhiHashMap().put(branchNumber,  1.0);
			}
		}
	}

	/**
	 * Calculates cos phi of each node
	 */
	private void calculateCosPhi() {
		Vector<Double> cosPhi= new Vector<Double>();
		//get numbe of nodes from powerflowParameter
		int numberOfNodes = this.getnNumNodes();
		//get number of powerflows, should be the same as numberOfNodes
		int numberOfPowerFlows = this.getNodalPowerReal().size();
		//check if both numbers are the same
		if (numberOfNodes > numberOfPowerFlows) {
			System.out.println("Error in PowerFlowCalculation.calculateCosPhi(): number of powerflows too small, schedules are missing in networkmodel!");
		}
		for (int i = 0; i < numberOfPowerFlows; i++) {
			
			double pK = this.getNodalPowerReal().get(i);
			double qK = this.getNodalPowerImag().get(i);
			// cosPhi= P/S
			if (Math.abs(pK) == 0) {
				cosPhi.add(1.0);
			} else {
				cosPhi.add(Math.abs(pK) / Math.sqrt(pK * pK + qK * qK));
			}
		}
		this.setNodalCosPhi(cosPhi);
	}

	private double[][] calculation_J(double[][] H, double[][] N, double[][] L, double[][] M) {

		int a, b;
		double[][] J = new double[2 * this.getnNumNodes()][2 * this.getnNumNodes()];

		for (a = 0; a < 2 * this.getnNumNodes(); a++) {
			if (a < this.getnNumNodes()) {
				for (b = 0; b < 2 * this.getnNumNodes(); b++) {
					if (b < this.getnNumNodes()) {
						J[a][b] = H[a][b];
					} else {
						J[a][b] = N[a][b - this.getnNumNodes()];
					}
				} // --- end for

			} else {
				for (b = 0; b < 2 * this.getnNumNodes(); b++) {
					if (b < this.getnNumNodes()) {
						J[a][b] = M[a - this.getnNumNodes()][b];
					} else {
						J[a][b] = L[a - this.getnNumNodes()][b - this.getnNumNodes()];
					}
				} // end for

			}
		} // end for

		for (a = 0; a < 2 * this.getnNumNodes(); a++) {
			J[a][this.getnSlackNode() - 1] = 0;
			J[this.getnSlackNode() - 1][a] = 0;
			J[a][this.getnNumNodes() + this.getnSlackNode() - 1] = 0;
			J[this.getnNumNodes() + this.getnSlackNode() - 1][a] = 0;
		}
		// Adjustment of slack  Node: Attention!
		J[this.getnSlackNode() - 1][this.getnSlackNode() - 1] = 1;
		J[this.getnNumNodes() + this.getnSlackNode() - 1][this.getnNumNodes() + this.getnSlackNode() - 1] = 1;
		return J;
	}

	private double[][] calculation_deltay(double[][] pN, double[][] qN) {

		//get number of nodes from powerflowparameter
		int numberOfNodes = this.getnNumNodes();
		//initialize deltaY matrix
		double[][] deltay = new double[2 * numberOfNodes][1];
		//get size of nodalpowerVector, should be the same as number of nodes
		int nodalPowerSize = this.getNodalPowerReal().size();
		//check if both numbers are equal
		if (numberOfNodes > nodalPowerSize) {
			System.out.println("Error in PowerFlowCalculation.calculation_deltay(): number of powerflows too small, schedules are missing in networkmodel!");
		}
		//iterate over number of nodes (2xtimes, one time for active power and one time for reactive power)
		for (int a = 0; a < 2 * numberOfNodes; a++) {
			//also check that index is not bigger than nodalPowerSize to prevent Exception
			if (a < numberOfNodes && a < nodalPowerSize) {
				deltay[a][0] = -(pN[a][0] + this.getNodalPowerReal().get(a));
			} else if (a >= numberOfNodes && (a - numberOfNodes) < nodalPowerSize){
				deltay[a][0] = -(qN[a - numberOfNodes][0] + this.getNodalPowerImag().get(a - numberOfNodes)); 
			}else {
				
			}
		}
		// Attention: Number of SlackNodes
		deltay[this.getnSlackNode() - 1][0] = 0;
		deltay[numberOfNodes + this.getnSlackNode() - 1][0] = 0;
		return deltay;
	}

	/**
	 * Calculation_ h.
	 *
	 * @param Sj_imag
	 *            the sj_imag
	 * @param qN
	 *            the q n
	 * @param nNumNodes
	 *            the n num nodes
	 * @return the double[][]
	 */
	private double[][] calculation_H(double[][] qN) {
		double[][] H = new double[this.getnNumNodes()][this.getnNumNodes()];
		for (int a = 0; a < this.getnNumNodes(); a++) {
			for (int b = 0; b < this.getnNumNodes(); b++) {
				if (a == b) {
					H[a][b] = this.getBranchPowerImag().get(a).get(b) - qN[a][0];
				} else {
					H[a][b] = this.getBranchPowerImag().get(a).get(b);
				}
			}
		}
		return H;
	}

	/**
	 * Calculation_ m.
	 *
	 * @param Sj_real
	 *            the sj_real
	 * @param pN
	 *            the p n
	 * @param nNumNodes
	 *            the n num nodes
	 * @return the double[][]
	 */
	private double[][] calculation_M(double[][] pN) {
		double[][] M = new double[this.getnNumNodes()][this.getnNumNodes()];
		for (int a = 0; a < this.getnNumNodes(); a++) {
			for (int b = 0; b < this.getnNumNodes(); b++) {
				if (a == b) {
					M[a][b] = -this.getBranchPowerReal().get(a).get(b) + pN[a][0];
				} else {
					M[a][b] = -this.getBranchPowerReal().get(a).get(b);
				}
			}
		}
		return M;
	}

	/**
	 * Calculation_ n.
	 *
	 * @param Sj_real
	 *            the sj_real
	 * @param pN
	 *            the p n
	 * @param pf
	 *            the pf
	 * @param pK
	 *            the p k
	 * @param nNumNodes
	 *            the n num nodes
	 * @return the double[][]
	 */
	private double[][] calculation_N(double[][] pN) {
		double[][] N = new double[this.getnNumNodes()][this.getnNumNodes()];
		for (int a = 0; a < this.getnNumNodes(); a++) {
			for (int b = 0; b < this.getnNumNodes(); b++) {
				if (a == b) {
					N[a][b] = this.getBranchPowerReal().get(a).get(b) + pN[a][0];
				} else {
					N[a][b] = this.getBranchPowerReal().get(a).get(b);
				}
			}
		}
		return N;
	}

	/**
	 * Calculation_ l.
	 *
	 * @param Sj_imag
	 *            the sj_imag
	 * @param qN
	 *            the q n
	 * @param qf
	 *            the qf
	 * @param qK
	 *            the q k
	 * @param nNumNodes
	 *            the n num nodes
	 * @return the double[][]
	 */
	private double[][] calculation_L(double[][] qN) {
		double[][] L = new double[this.getnNumNodes()][this.getnNumNodes()];
		for (int a = 0; a < this.getnNumNodes(); a++) {
			for (int b = 0; b < this.getnNumNodes(); b++) {
				if (a == b) {
					L[a][b] = this.getBranchPowerImag().get(a).get(b) + qN[a][0];
				} else {
					L[a][b] = this.getBranchPowerImag().get(a).get(b);
				}
			}
		}
		return L;
	}

	/**
	 * Calculation_p n.
	 *
	 * @param Sj_real
	 *            the sj_real
	 * @param nNumNodes
	 *            the n num nodes
	 * @return the double[][]
	 */
	private double[][] calculation_pN() {
		double[][] pN = new double[this.getnNumNodes()][1];
		for (int a = 0; a < this.getnNumNodes(); a++) {
			double sum = 0;
			for (int b = 0; b < this.getnNumNodes(); b++) {
				sum = sum + this.getBranchPowerReal().get(a).get(b);
			}
			pN[a][0] = sum;
		}
		return pN;
	}

	/**
	 * Calculation_q n.
	 *
	 * @param Sj_imag
	 *            the sj_imag
	 * @param nNumNodes
	 *            the n num nodes
	 * @return the double[][]
	 */
	private double[][] calculation_qN() {
		double[][] qN = new double[this.getnNumNodes()][1];
		for (int a = 0; a < this.getnNumNodes(); a++) {
			double sum = 0;
			for (int b = 0; b < this.getnNumNodes(); b++) {
				sum = sum + this.getBranchPowerImag().get(a).get(b);
			}
			qN[a][0] = sum;
		}
		return qN;
	}

	/**
	 * Refresh node voltage_real.
	 *
	 * @param x
	 *            the x
	 * @param uK_real
	 *            the u k_real
	 * @param uK_imag
	 *            the u k_imag
	 * @param nNumNodes
	 *            the n num nodes
	 * @return the double[][]
	 */
	private Vector<Double> refreshNodeVoltage_real(double[][] x, Vector<Double> uK_real, Vector<Double> uK_imag,int nNumNodes) {
		Vector<Double> uk_real_intern = new Vector<Double>();
		for (int a = 0; a < nNumNodes; a++) {
			uk_real_intern.add(Math.sqrt(uK_real.get(a) * uK_real.get(a) + uK_imag.get(a) * uK_imag.get(a))* x[a + nNumNodes][0] * Math.cos(x[a][0]));
		}
		return uk_real_intern;
	}

	/**
	 * Refresh node voltage_imag.
	 *
	 * @param x
	 *            the x
	 * @param uK_real
	 *            the u k_real
	 * @param uK_imag
	 *            the u k_imag
	 * @param nNumNodes
	 *            the n num nodes
	 * @return the double[][]
	 */
	private Vector<Double> refreshNodeVoltage_imag(double[][] x, Vector<Double> uk_real, Vector<Double> uK_imag,
			int nNumNodes) {
		Vector<Double> uK_imag_intern = new Vector<Double>();
		for (int a = 0; a < nNumNodes; a++) {
			uK_imag_intern.add(Math.sqrt((uk_real.get(a)) * (uk_real.get(a)) + (uK_imag.get(a)) * (uK_imag.get(a)))
					* x[a + nNumNodes][0] * Math.sin(x[a][0]));
		}
		return uK_imag_intern;
	}

	/**
	 * Calculation_x.
	 *
	 * @param x
	 *            the x
	 * @param deltax
	 *            the deltax
	 * @param nNumNodes
	 *            the n num nodes
	 * @return the double[][]
	 */
	private double[][] calculation_x(double[][] x, double[][] deltax, int nNumNodes) {
		for (int a = 0; a < nNumNodes; a++) {
			x[a][0] = x[a][0] + deltax[a][0];
			x[a + nNumNodes][0] = 1 + deltax[a + nNumNodes][0];
		}
		return x;
	}

	/**
	 * Power flow ready.
	 *
	 * @param deltax
	 *            the deltax
	 * @param IterationLimit
	 *            the iteration limit
	 * @param nNumNodes
	 *            the n num nodes
	 * @param IterationCounter
	 *            the iteration counter
	 * @param IterationCounterLimit
	 *            the iteration counter limit
	 * @return true, if successful
	 */
	private boolean powerFlowReady(double[][] deltax, double IterationLimit, int nNumNodes, int IterationCounter, int IterationCounterLimit) {
		double max = 0;
		boolean ready;
		int a;
		for (a = 0; a < 2 * nNumNodes; a++) {
			if (max < Math.abs(deltax[a][0])) {
				max = Math.abs(deltax[a][0]);
			}
		}
		// Check, if limit is valid
		if (max < IterationLimit) {
			ready = false; // Algorithm ready
			this.setSucessfullPFC(true);
		} else {
			ready = true; // Algorithm not ready yet
			this.setSucessfullPFC(false);
		}
		// If more than iterationCounterLimit iterations are done --> break
		if (IterationCounter > IterationCounterLimit) {
			this.setSucessfullPFC(false);
			ready = false;
		}
		return ready;
	}

	/**
	 * Calculation_ sj.
	 *
	 * @param uK_real
	 *            the u k_real
	 * @param uK_imag
	 *            the u k_imag
	 * @param YKK_real
	 *            the YK k_real
	 * @param YKK_imag
	 *            the YK k_imag
	 * @param nNumNodes
	 *            the n num nodes
	 */
	private void calculation_SJ() {

		double[][] A = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] B = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] C = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] D = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] CEDF = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] DECF = new double[this.getnNumNodes()][this.getnNumNodes()];
		Vector<Vector<Double>> branchPowerReal = new Vector<Vector<Double>>();
		Vector<Vector<Double>> branchPowerImag = new Vector<Vector<Double>>();

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
			branchPowerReal.add(tempReal);
			branchPowerImag.add(tempImag);
		}
		this.setBranchPowerReal(branchPowerReal);
		this.setBranchPowerImag(branchPowerImag);
	}

	/**
	 * Calculation_ik.
	 *
	 * @param YKK_real
	 *            the YK k_real
	 * @param YKK_imag
	 *            the YK k_imag
	 * @param nNumNodes
	 *            the n num nodes
	 */
	// ik= AC-BD +j(BC+AD)
	private void calculation_ik() {
		double tempReal = 0;
		double tempImag = 0;
		Vector<Double> nodalCurrentReal = new Vector<Double>();
		Vector<Double> nodalCurrentImag = new Vector<Double>();
		double A;
		double B;
		double C;
		double D;
		for (int a = 0; a < this.getnNumNodes(); a++) {
			for (int b = 0; b < this.getnNumNodes(); b++) {
				A = this.getPowerFlowParameter().getYkkReal().get(a).get(b);
				B = this.getPowerFlowParameter().getYkkImag().get(a).get(b);
				C = this.getNodalVoltageReal().get(b); // uK_real
				D = this.getNodalVoltageImag().get(b); // uK_imag
				tempReal = tempReal + A * C - B * D;
				tempImag = tempImag + B * C + A * D;
			}
			nodalCurrentReal.add(tempReal);
			nodalCurrentImag.add(tempImag);
			tempReal = 0;
			tempImag = 0;
		}
		
		this.setNodalCurrentReal(nodalCurrentReal);
		this.setNodalCurrentImag(nodalCurrentImag);
	}

	/**
	 * Calculation_s k.
	 */
	private void calculation_sK() {
		Vector<Double> sK_real = new Vector<Double>();
		Vector<Double> sK_imag = new Vector<Double>();
		double temp_real = 0;
		double temp_imag = 0;
		double[][] A = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] B = new double[this.getnNumNodes()][this.getnNumNodes()];
		double C = 0;
		double D = 0;
		for (int a = 0; a < this.getnNumNodes(); a++) {
			for (int b = 0; b < this.getnNumNodes(); b++) {
				A[a][a] = this.getNodalVoltageReal().get(a);
				B[a][a] = this.getNodalVoltageImag().get(a);
				C = this.getNodalCurrentReal().get(a);
				D = this.getNodalCurrentImag().get(a);
				temp_real = temp_real + A[a][b] * C + B[a][b] * D;
				temp_imag = temp_imag + B[a][b] * C - A[a][b] * D;
			}
			sK_real.add(temp_real); // pK
			sK_imag.add(temp_imag); // qK
			temp_real = 0;
			temp_imag = 0;
		}
	}

	/**
	 * Calculation_i n.
	 *
	 * @param YKK_real
	 *            the YK k_real
	 * @param YKK_imag
	 *            the YK k_imag
	 */
	// iN=YKK*(uK(b)-uK(a))
	// iN=(A+jB)*(C+jD-E+jF)=(A*C-A*E-B*D+B*F)+j(BC-BE+AD-AF)
	private void calculation_iN() {
		double A, B, C, D, E, F;
		double[][] dIn_real = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] dIn_imag = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] dIn_abs = new double[this.getnNumNodes()][this.getnNumNodes()];
		for (int a = 0; a < this.getnNumNodes(); a++) {
			for (int b = 0; b < this.getnNumNodes(); b++) {
				if (a != b) {
					A = this.getYkkReal().get(a).get(b);
					B = this.getYkkImag().get(a).get(b);
					C = this.getNodalVoltageReal().get(b); // uK_real
					D = this.getNodalVoltageImag().get(b); // uK_imag
					E = this.getNodalVoltageReal().get(a); // uK_real
					F = this.getNodalVoltageImag().get(a); // uK_imag
					dIn_real[a][b] = A * C - A * E - B * D + B * F;
					dIn_imag[a][b] = B * C - B * E + A * D - A * F;
					
					// Removed, since these Saturations are to low for medium voltage grids
//					// --- If current is to high--> saturate
//					if(dIn_real[a][b]>200)
//						dIn_real[a][b]=235.3;
//					if(dIn_imag[a][b]>200)
//						dIn_imag[a][b]=0;
						
					if(dIn_real[a][b]>0) {
						dIn_abs[a][b] = Math.sqrt(dIn_real[a][b] * dIn_real[a][b] + dIn_imag[a][b] * dIn_imag[a][b]);
					}
					else {
						dIn_abs[a][b] =-1* Math.sqrt(dIn_real[a][b] * dIn_real[a][b] + dIn_imag[a][b] * dIn_imag[a][b]);
					}
					
					
				
				}
			}
		}
		
		Vector<Vector<Double>> branchCurrentReal = new Vector<>();
		Vector<Vector<Double>> branchCurrentImag = new Vector<>();
		Vector<Vector<Double>> branchCurrentAbs  = new Vector<>();
		for (int a = 0; a < this.getnNumNodes(); a++) {
			Vector<Double> tempReal = new Vector<Double>();
			Vector<Double> tempImag = new Vector<Double>();
			Vector<Double> tempAbs = new Vector<Double>();
			for (int b = 0; b < this.getnNumNodes(); b++) {
				tempReal.add(dIn_real[a][b]);
				tempImag.add(dIn_imag[a][b]);
				tempAbs.add(dIn_abs[a][b]);

			}
			branchCurrentReal.add(tempReal);
			branchCurrentImag.add(tempImag);
			branchCurrentAbs.add(tempAbs);
		}
		this.setBranchCurrentReal(branchCurrentReal);
		this.setBranchCurrentImag(branchCurrentImag);
		this.setBranchCurrentAbs(branchCurrentAbs);
	}

	/**
	 * Calculation_s n.
	 *
	 * @param uK_real
	 *            the u k_real
	 */
	private void calculation_sN() {
		Vector<Vector<Double>> branchPowerReal = new Vector<Vector<Double>>();
		Vector<Vector<Double>> branchPowerImag = new Vector<Vector<Double>>();
		double[][] A = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] B = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] C = new double[this.getnNumNodes()][this.getnNumNodes()];
		double[][] D = new double[this.getnNumNodes()][this.getnNumNodes()];
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
			branchPowerImag.add(tempImag);
		}
		this.setBranchPowerReal(branchPowerReal);
		this.setBranchPowerImag(branchPowerImag);
	}

	/**
	 * Calculate_utilization.
	 */
	private void calculate_utilization() {
		int fromNode, toNode, branchNumber;
		double abs = 0;
		for (int a = 0; a < this.getnNumBranches(); a++) {
			branchNumber = this.getBranchNumbersVector().get(a);
			fromNode = this.getBranchFromNodesHashMap().get(branchNumber);
			toNode = this.getBranchToNodesHashMap().get(branchNumber);
			abs = Math.sqrt(this.getBranchCurrentReal().get(fromNode - 1).get(toNode - 1) * this.getBranchCurrentReal().get(fromNode - 1).get(toNode - 1) + this.getBranchCurrentImag().get(fromNode - 1).get(toNode - 1) * this.getBranchCurrentImag().get(fromNode - 1).get(toNode - 1));
			
			double utilization = (abs / (this.getPowerFlowParameter().getMaxCurrent().get(fromNode - 1).get(toNode - 1))) * 100;
			
			// --- Store in HashMap ---------------------------------
			this.getBranchUtilizationHashMap().put(branchNumber, utilization);
		}
		
	}

	/**
	 * Creates a deep copy of a 2d double array
	 * 
	 * @param arrayToClone
	 *            The array to be copied
	 * @return The copy
	 */
	private Vector<Double> deepClone(Vector<Double> arrayToClone) {
		Vector<Double> cloned = new Vector<Double>();
		for (int i = 0; i < arrayToClone.size(); i++) {
			cloned.add(arrayToClone.get(i));
		}
		return cloned;
	}

	/**
	 * Calculates the power transformer.
	 */
	private void calculatePowerTransformer() {
		double iSlackreal = 0;
		double iSlackimag = 0;
		double P = 0;
		double Q = 0;
		for (int i = 0; i < this.getnNumNodes(); i++) {
			iSlackreal = iSlackreal + this.getBranchCurrentReal().get(i).get(this.getnSlackNode() - 1);
			iSlackimag = iSlackimag + this.getBranchCurrentImag().get(i).get(this.getnSlackNode() - 1);
		}
		P = this.getdSlackNodeVoltage() * iSlackreal;
		Q = this.getdSlackNodeVoltage() * iSlackimag;
		
		Vector<Double> powerOfTransformer= new Vector<>();
		powerOfTransformer.add(P);
		powerOfTransformer.add(Q);
		this.setPowerOfTransformer(powerOfTransformer);
	}


	@Override
	public boolean checkPreconditionsForCalculation() {
		return true;
	}

}
