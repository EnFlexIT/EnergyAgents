package de.enflexit.energyAgent.lib.powerFlowEstimation.decentralEstimation;

import java.util.Vector;

import jampack.Inv;
import jampack.JampackException;
import jampack.Zmat;

public class BuildSensitivity {
	private double[][] Sensreal;
	private double[][] Sensimag;

	public void calculateSensitivity(Vector<Vector<Double>> dYKKreal, Vector<Vector<Double>> dYKKimag, int nSlackNode, int nNumNodes)
			throws JampackException {
		Sensreal = new double[nNumNodes][nNumNodes];
		Sensimag = new double[nNumNodes][nNumNodes];
		double[][] dYKKRealred = new double[nNumNodes][nNumNodes];
		double[][] dYKKImagred = new double[nNumNodes][nNumNodes];
		for(int a=0;a<nNumNodes;a++){
			for(int b=0;b<nNumNodes;b++){
				dYKKRealred[a][b] = dYKKreal.get(a).get(b); 
				dYKKImagred[a][b] = dYKKimag.get(a).get(b);
			}
		}
		double TrafoImpedanz = 0.04 * ((400 / Math.sqrt(3)) * (400 / Math.sqrt(3))) / (250 * 1000);
		double TrafoAdmittanz = 1 / TrafoImpedanz;
		dYKKRealred[nSlackNode - 1][nSlackNode - 1] = dYKKreal.get(nSlackNode - 1).get(nSlackNode - 1) + TrafoAdmittanz;

		Zmat YKKcomplex = new Zmat(dYKKRealred, dYKKImagred);
		Zmat Sensitivity = new Zmat(nNumNodes, nNumNodes);
		Sensitivity = Inv.o(YKKcomplex);
		

		for (int i = 0; i < nNumNodes; i++) {
			for (int j = 0; j < nNumNodes; j++) {
				this.Sensreal[i][j] = Sensitivity.get(i + 1, j + 1).re;
				this.Sensimag[i][j] = Sensitivity.get(i + 1, j + 1).im;
			}
		}
	}

	public double[][] getSensreal() {
		return Sensreal;
	}

	public void setSensreal(double[][] sensreal) {
		Sensreal = sensreal;
	}

	public double[][] getSensimag() {
		return Sensimag;
	}

	public void setSensimag(double[][] sensimag) {
		Sensimag = sensimag;
	}

}
