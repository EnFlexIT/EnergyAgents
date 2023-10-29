package de.enflexit.ea.core.configuration.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.enflexit.ea.core.configuration.BundleHelper;
import de.enflexit.ea.core.configuration.model.SetupConfigurationModel;
import de.enflexit.ea.core.configuration.persistence.SetupConfigurationFileReader;
import de.enflexit.ea.core.configuration.persistence.SetupConfigurationFileWriter;
import energy.GlobalInfo;

/**
 * The Class SetupConfigurationToolBar.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class SetupConfigurationToolBar extends JToolBar implements ActionListener {

	private static final long serialVersionUID = -4020142646497272018L;

	private SetupConfigurationModel configModel;

	private JButton jButtonLoadFromSetup;
	private JButton jButtonSaveToSetup;
	
	private JButton jButtonSaveToFile;
	private JButton jButtonLoadFromFile;

	private JLabel jLabelInfo;
	private Timer infoTimer;
	
	/**
	 * Instantiates a new setup configuration tool bar.
	 *
	 * @param configModel the config model
	 */
	public SetupConfigurationToolBar(SetupConfigurationModel configModel) {
		this.configModel = configModel;
		this.initialize();
	}
	/**
	 * Initializes this toolbar.
	 */
	private void initialize() {

		this.setFloatable(false);
		this.setRollover(true);
		this.setBorder(BorderFactory.createEmptyBorder());
		
		this.add(this.getJButtonSaveToFile());
		this.add(this.getJButtonLoadFromFile());
		this.addSeparator();
		
		this.add(this.getJButtonLoadFromSetup());
		this.add(this.getJButtonSaveToSetup());
		this.addSeparator();
		
		this.add(this.getJLabelInfo());
		this.setDefaultInfoText();
		this.addSeparator();
	}
	
	
	/**
	 * Returns the JButton to load settings from the current setup .
	 * @return the j button load from setup
	 */
	private JButton getJButtonLoadFromSetup() {
		if (jButtonLoadFromSetup==null) {
			jButtonLoadFromSetup = new JButton();
			jButtonLoadFromSetup.setIcon(BundleHelper.getImageIcon("Table_In.png"));
			jButtonLoadFromSetup.setToolTipText("Load setup values to table ...");
			jButtonLoadFromSetup.setPreferredSize(new Dimension(26, 26));
			jButtonLoadFromSetup.addActionListener(this);
		}
		return jButtonLoadFromSetup;
	}
	/**
	 * Returns the JButton to load settings from the current setup .
	 * @return the j button load from setup
	 */
	private JButton getJButtonSaveToSetup() {
		if (jButtonSaveToSetup==null) {
			jButtonSaveToSetup = new JButton();
			jButtonSaveToSetup.setIcon(BundleHelper.getImageIcon("Table_Out.png"));
			jButtonSaveToSetup.setToolTipText("Save table values to setup ...");
			jButtonSaveToSetup.setPreferredSize(new Dimension(26, 26));
			jButtonSaveToSetup.addActionListener(this);
		}
		return jButtonSaveToSetup;
	}
	
	/**
	 * Returns the JButton to load settings from the current setup .
	 * @return the j button load from setup
	 */
	private JButton getJButtonSaveToFile() {
		if (jButtonSaveToFile==null) {
			jButtonSaveToFile = new JButton();
			jButtonSaveToFile.setIcon(BundleHelper.getImageIcon("MBsaveAs.png"));
			jButtonSaveToFile.setToolTipText("Save table data to CSV file ...");
			jButtonSaveToFile.setPreferredSize(new Dimension(26, 26));
			jButtonSaveToFile.addActionListener(this);
		}
		return jButtonSaveToFile;
	}
	/**
	 * Returns the JButton to load settings from the current setup .
	 * @return the j button load from setup
	 */
	private JButton getJButtonLoadFromFile() {
		if (jButtonLoadFromFile==null) {
			jButtonLoadFromFile = new JButton();
			jButtonLoadFromFile.setIcon(BundleHelper.getImageIcon("MBopen.png"));
			jButtonLoadFromFile.setToolTipText("Load CSV data to table ...");
			jButtonLoadFromFile.setPreferredSize(new Dimension(26, 26));
			jButtonLoadFromFile.addActionListener(this);
		}
		return jButtonLoadFromFile;
	}

	/**
	 * Returns the JLabel for the user information.
	 * @return the j label info
	 */
	private JLabel getJLabelInfo() {
		if (jLabelInfo==null) {
			jLabelInfo = new JLabel();
			jLabelInfo.setFont(new Font("Dialog", Font.BOLD, 12));
			jLabelInfo.setForeground(new Color(38, 127, 0));
		}
		return jLabelInfo;
	}
	/**
	 * Returns the local swing {@link Timer}
	 * @return the timer
	 */
	private Timer getTimer() {
		if (infoTimer==null) {
			infoTimer = new Timer(1000 * 3, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					SetupConfigurationToolBar.this.setDefaultInfoText();
				}
			});
			infoTimer.setRepeats(false);
		}
		return infoTimer;
	}
	/**
	 * Sets the default info text.
	 */
	private void setDefaultInfoText() {
		this.getJLabelInfo().setText("  " + this.configModel.toString() + "  ");
	}
	/**
	 * Sets the info text.
	 * @param infoText the new info text
	 */
	public void setInfoText(String infoText) {
		
		if (infoText==null || infoText.isBlank()==true) return;
		
		this.getJLabelInfo().setText("  " + infoText + "  ");
		this.getTimer().restart();
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {

		if (ae.getSource()==this.getJButtonLoadFromSetup()) {
			// --- Load service values from the current setup -----------------
			this.configModel.reCreateConfigurationTableModelInThread();
			
		} else if (ae.getSource()==this.getJButtonSaveToSetup()) {
			// --- Save service values to the current setup --------------------
			this.configModel.setConfigurationToSetup();
			
		} else if (ae.getSource()==this.getJButtonSaveToFile()) {
			// --- Save table data to CSV file --------------------------------
			File fileSelection = this.getFileSelection(true);
			if (fileSelection!=null) {
				new SetupConfigurationFileWriter(this.configModel).write(fileSelection);
				this.setInfoText("Exportetd table data to file '" + fileSelection.getName() + "'.");
			}
			
		} else if (ae.getSource()==this.getJButtonLoadFromFile()) {
			// --- Loads CSV data to the table --------------------------------
			File fileSelection = this.getFileSelection(false);
			if (fileSelection!=null) {
				new SetupConfigurationFileReader(this.configModel).read(fileSelection);
				this.setInfoText("Imported file '" + fileSelection.getName() + "' to table data.");
			}
		}
	}
	
	/**
	 * Returns the file selection after the user was requested.
	 *
	 * @param isSaveAction the is save action
	 * @return the file selection
	 */
	private File getFileSelection(boolean isSaveAction) {
		
		String dialogTitle = "Save table data to CSV file";
		String approveButtonText = "Save";
		if (isSaveAction==false) {
			approveButtonText = "Load";
			dialogTitle = "Load CSV file to table";
		}
		
		FileNameExtensionFilter fnFilter = new FileNameExtensionFilter("CSV File", "csv");
		
		// --- Create file choose instance ------------
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(GlobalInfo.getLastSelectedDirectory());
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(fnFilter);
		fileChooser.setFileFilter(fnFilter);
		fileChooser.setDialogTitle(dialogTitle);
		
		// --- Use predefined file selection? --------- 
		File fileSelected = BundleHelper.getLastSelectedFile();
		if (fileSelected!=null) {
			File dirSelected = fileSelected.getParentFile();
			if (dirSelected.exists()==false) {
				dirSelected.mkdirs();
			}
			fileChooser.setCurrentDirectory(dirSelected);
			fileChooser.setSelectedFile(fileSelected);	
		}
		int ret = fileChooser.showDialog(this.getParent(), approveButtonText);
		// - - - - - - - - - - - - - - - - - - - - - - - - 
		
		if (ret == JFileChooser.APPROVE_OPTION) {
			fileSelected = fileChooser.getSelectedFile();
			String fileExtension = null;
			if (fileChooser.getFileFilter()==fnFilter) {
				fileExtension = "." + "csv";
			}
			if (fileSelected.getAbsolutePath().toLowerCase().endsWith(fileExtension)==false) {
				fileSelected = new File(fileSelected.getAbsoluteFile() + fileExtension);	
			}
			BundleHelper.setLastSelectedFile(fileSelected);
			
		} else {
			fileSelected = null;
		}
		return fileSelected;
	}
	
}
