package de.enflexit.ea.core.monitoring;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import de.enflexit.ea.core.dataModel.DirectoryHelper;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class MonitoringFileHandler can be used to continuously write objects into the specified file.
 * In turn, these objects (instances of {@link TechnicalSystemStateEvaluation}) can also be read.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class MonitoringFileHandler {

	private File monitoringFile;
	
	/**
	 * Instantiates a new monitoring file writer.
	 * @param monitoringFile the monitoring file
	 */
	public MonitoringFileHandler(File monitoringFile) {
		this.setMonitoringFile(monitoringFile);
	}
	
	/**
	 * Gets the monitoring file.
	 * @return the monitoring file
	 */
	public File getMonitoringFile() {
		return monitoringFile;
	}
	/**
	 * Sets the monitoring file.
	 * @param monitoringFile the new monitoring file
	 */
	public void setMonitoringFile(File monitoringFile) {
		this.monitoringFile = monitoringFile;
	}
	
	
	/**
	 * Writes the specified object to the file.
	 * @param objectToWrite the object to write
	 */
	public boolean writeObject(Object objectToWrite) {
		
		if (objectToWrite==null) return false;
		
		boolean success = false;
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {

			if (DirectoryHelper.isAvailableDirectory(this.getMonitoringFile())==false) {
				throw new IOException("The destination directory '" + this.getMonitoringFile().getAbsolutePath() + "' could not be found or be created!");
			}
			
			if (this.getMonitoringFile().exists()==false) {
				fos = new FileOutputStream(this.getMonitoringFile());
				oos = new ObjectOutputStream(fos);
			} else {
				fos = new FileOutputStream(this.getMonitoringFile(), true);
				oos = new AppendableObjectOutputStream(fos);
			}
	        oos.writeObject(objectToWrite);
	        oos.flush();
	        success = true;
		
		} catch (IOException ioEx ){
			ioEx.printStackTrace();
		} finally {
			try {
				if (oos!=null) oos.close();
			} catch (IOException ioEx) {
				ioEx.printStackTrace();
			}
			try {
				if (fos!=null) fos.close();
			} catch (IOException ioEx) {
				ioEx.printStackTrace();
			}
		}
		return success;
	}
	
	
	/**
	 * Reads the monitoring objects {@link TechnicalSystemStateEvaluation} from the specified file.
	 * @return the list of {@link TechnicalSystemStateEvaluation}
	 */
	public List<TechnicalSystemStateEvaluation> readObjects() {
		
		ArrayList<TechnicalSystemStateEvaluation> tsseList = new ArrayList<>();

		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			
			if (this.getMonitoringFile().exists()==true) {
				// --- Open states from file ------------------------
				fis = new FileInputStream(this.getMonitoringFile());
				ois = new ObjectInputStream(fis);

				TechnicalSystemStateEvaluation tempLogInfo = null;
				while ((tempLogInfo=(TechnicalSystemStateEvaluation)ois.readObject())!=null) {
					tsseList.add(tempLogInfo);
				}
				
			} else {
				System.err.println("[" + this.getClass().getSimpleName() + "] The file '" + this.getMonitoringFile().getAbsolutePath() +  "' does not exists!");
			}
			
		} catch (IOException  | ClassNotFoundException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (ois!=null ) ois.close();
			} catch (IOException ioEx) {
				ioEx.printStackTrace();
			}
			try {
				if (fis!=null) fis.close();
			} catch (IOException ioEx) {
				ioEx.printStackTrace();
			}
		}
		return tsseList;
	}
	
	
	/**
	 * The Class AppendableObjectOutputStream allows to append objects 
	 * to files that already exists.
	 */
	private class AppendableObjectOutputStream extends ObjectOutputStream {

		/**
		 * Instantiates a new appendable object output stream.
		 *
		 * @param out the OutputStream
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public AppendableObjectOutputStream(OutputStream out) throws IOException {
			super(out);
		}
		/* (non-Javadoc)
		 * @see java.io.ObjectOutputStream#writeStreamHeader()
		 */
		@Override
		protected void writeStreamHeader() throws IOException {
			this.reset();
		}
	}
	
}
