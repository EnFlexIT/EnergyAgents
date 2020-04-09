package de.enflexit.ea.lib.powerFlowCalculation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CSVImport {

	private File fileToImport;
	
	private static double[][] myArray;

	
	/**
	 * Instantiates a new CSV import.
	 * @param fileToImport the file to import
	 */
	public CSVImport(File fileToImport) {
		this.fileToImport = fileToImport;
		this.run();
	}
	/**
	 * Instantiates a new CSV import.
	 * @param pathname the pathname
	 */
	public CSVImport(String pathname) {
		File fileImp = new File(pathname);
		if (fileImp.exists()==true) {
			this.fileToImport = fileImp;
			this.run();
		}
	}
	
	/**
	 * @return the csv array
	 */
	public double[][] getCsvArray() {
		return myArray;
	}
	/**
	 * @param csvArray the csv array to set
	 */
	public void setCsvArray(double[][] csvArray) {
		CSVImport.myArray = csvArray;
	}
	
	private void run() {

		BufferedReader br1 = null;
		BufferedReader br1n = null;

		String line = "";
		String cvsSplitBy = ";";

		try {
			
			// --- Determine number of columns and nodes ------------
			Integer nColumns = null;
			int nNodes = 0;
			br1 = new BufferedReader(new FileReader(this.fileToImport));
			while ((line = br1.readLine()) != null) {
				if (nColumns==null) {
					nColumns = line.split(";").length;
				}
				nNodes++;
			}
			this.setCsvArray(new double[nNodes][nColumns]);

			// ---- Import 
			nNodes = 0;
			br1n = new BufferedReader(new FileReader(this.fileToImport));
			while ((line = br1n.readLine()) != null) {
				// use comma as separator
				String[] NodeSetup = line.split(cvsSplitBy);
				for (int x = 0; x < NodeSetup.length; x++) {
					this.getCsvArray()[nNodes][x] = Double.parseDouble(NodeSetup[x]);
				}
				nNodes++;
			} // --- end while ----
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br1 != null) {
				try {
					br1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	

}
