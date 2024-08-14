package de.enflexit.ea.core.configuration.eom.systems.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;

import de.enflexit.common.FileHelper;
import de.enflexit.common.swing.TableCellColorHelper;
import de.enflexit.ea.core.configuration.eom.BundleHelper;
import de.enflexit.ea.core.configuration.eom.systems.EomSystem;
import de.enflexit.ea.core.configuration.eom.systems.SystemConfiguration;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler;
import de.enflexit.eom.database.DatabaseStorageHandler_ScheduleList;
import energy.EomControllerStorageSettings;
import energy.OptionModelController;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystem;
import energy.optionModel.TechnicalSystemGroup;
import energy.schedule.ScheduleController;
import energygroup.GroupController;;

/**
 * The Class EomSystemCellEditorModelSelection.
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class EomSystemCellEditorModelSelection extends AbstractCellEditor implements TableCellEditor, ActionListener {
	
	private static final long serialVersionUID = -8965685551115502359L;
	
	private SystemConfiguration systemConfiguration;
	
	private JTable table;
	private EomSystem eomSystem;
	private JButton jButtonSelectModel;
	
    /**
     * Instantiates a new eom system cell editor model selection.
     */
    public EomSystemCellEditorModelSelection(SystemConfiguration systemConfiguration) {
    	this.systemConfiguration = systemConfiguration;
        this.addCellEditorListener(new CellEditorListener() {
			/* (non-Javadoc)
			 * @see javax.swing.event.CellEditorListener#editingStopped(javax.swing.event.ChangeEvent)
			 */
			@Override
			public void editingStopped(ChangeEvent e) {

			}
			/* (non-Javadoc)
			 * @see javax.swing.event.CellEditorListener#editingCanceled(javax.swing.event.ChangeEvent)
			 */
			@Override
			public void editingCanceled(ChangeEvent e) {
				
			}
		});
    }

    /**
     * Returns the JTextField to edit the ID.
     * @return the j text field
     */
    private JButton getJButtonSelectModel() {
    	if (jButtonSelectModel==null) {
    		jButtonSelectModel = new JButton();
    		jButtonSelectModel.setBorder(new EmptyBorder(1, 1, 1, 1));
    		jButtonSelectModel.addActionListener(this);
    	}
    	return jButtonSelectModel;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
     */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

    	this.table = table;
    	this.eomSystem = (EomSystem) value;
    	
    	this.getJButtonSelectModel().setText(this.eomSystem.getStorageInfo());
    		
		TableCellColorHelper.setTableCellRendererColors(jButtonSelectModel, row, isSelected, Color.white);
        return this.getJButtonSelectModel();
    }
	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	@Override
	public Object getCellEditorValue() {
		return this.eomSystem;
	}

	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		
		Object model = null;
		try {
			switch (this.eomSystem.getEomModelType()) {
			case TechnicalSystem:
				model = this.loadTechnicalSystem();
				break;
			case ScheduleList:
				model = this.loadScheduleList();
				break;
			case TechnicalSystemGroup:
				model = this.loadTechnicalSystemGroup();
				break;
			}
			
		} catch (Exception ex) {
			System.err.println("[" + this.getClass().getSimpleName() + "] Error while trying to load EOM model " + this.eomSystem.getEomModelType() + ":");
			ex.printStackTrace();
		}
		
		if (model!=null) {
			this.eomSystem.setDataModel(model);
			this.systemConfiguration.save();
		}
		this.fireEditingStopped();
		this.table.repaint();
		
	}
	
	/**
	 * Loads a TechnicalSystem.
	 * @return the technical system
	 */
	private TechnicalSystem loadTechnicalSystem() {
		
		OptionModelController omc = new OptionModelController();
		EomControllerStorageSettings storageSettings = omc.getEomControllerStorageSettings();
		TechnicalSystem ts = null; 
		
		// --- Load with selected method --------------------------------------
		switch (this.eomSystem.getStorageLocation()) {
		case File:
			omc.loadTechnicalSystem(null, this.table);
			if (storageSettings.size()>0) {
				ts = omc.getTechnicalSystem();
				File file = storageSettings.getCurrentFile();
				if (this.isInProjectDirectory(file)==true) {
					// --- Save storage settings ------------------------------
					this.eomSystem.getDataModelStorageSettings().put(EomDataModelStorageHandler.EOM_SETTING_EOM_FILE_LOCATION, this.getRelativeProjectPath(file));
				} else {
					ts = null;
				}
			}
			break;

		case BundleLocation:
			omc.loadTechnicalSystemFromBundle(null, null, this.table, false);
			if (storageSettings.size()>0) {
				ts = omc.getTechnicalSystem();
				String symbolicBundleName  = storageSettings.getCurrentBundleModelSymbolicBundleName();
				String bundleFileReference = storageSettings.getCurrentBundleModelFileReference();
				
				this.eomSystem.getDataModelStorageSettings().put(EomDataModelStorageHandler.EOM_SETTING_BUNDLE_MODEL_SYMBOLIC_BUNDLE_NAME, symbolicBundleName);
				this.eomSystem.getDataModelStorageSettings().put(EomDataModelStorageHandler.EOM_SETTING_BUNDLE_MODEL_FILE_REFERENCE, bundleFileReference);
						
			}
			break;
		default:
			break;
		}
		return ts;
	}
	
	/**
	 * Loads a ScheduleList.
	 * @return the schedule list
	 */
	private ScheduleList loadScheduleList() {
		
		ScheduleController sc = new ScheduleController();
		EomControllerStorageSettings storageSettings = sc.getEomControllerStorageSettings();
		ScheduleList sl = null;
		switch (this.eomSystem.getStorageLocation()) {
		case File:
			sc.loadScheduleList(null, this.table);
			if (storageSettings.size()>0) {
				sl = sc.getScheduleList();
				File file = storageSettings.getCurrentFile();
				if (this.isInProjectDirectory(file)==true) {
					// --- Save storage settings ------------------------------
					this.eomSystem.getDataModelStorageSettings().put(EomDataModelStorageHandler.EOM_SETTING_EOM_FILE_LOCATION, this.getRelativeProjectPath(file));
				} else {
					sl = null;
				}
			}
			break;
		case Database:
			DatabaseStorageHandler_ScheduleList dbshsl = new DatabaseStorageHandler_ScheduleList();
			sl = dbshsl.loadScheduleList(sc, null, this.table);
			if (storageSettings.size()>0) {
				sl = sc.getScheduleList();
				this.eomSystem.getDataModelStorageSettings().put(EomDataModelStorageHandler.EOM_SETTING_DATABASE_ID, storageSettings.getCurrentDatabaseID().toString());
			}
			break;
		default:
			break;
		}
		return sl;
	}
	
	/**
	 * Loads a TechnicalSystemGroup.
	 * @return the technical system group
	 */
	private TechnicalSystemGroup loadTechnicalSystemGroup() {
		
		GroupController gc = new GroupController();
		EomControllerStorageSettings storageSettings = gc.getEomControllerStorageSettings();
		TechnicalSystemGroup tsg = null;
		switch (this.eomSystem.getStorageLocation()) {
		case File:
			gc.loadOptionModelGroup(null, this.table);
			if (storageSettings.size()>0) {
				tsg = gc.getTechnicalSystemGroup();
				File file = storageSettings.getCurrentFile();
				if (this.isInProjectDirectory(file)==true) {
					// --- Save storage settings ------------------------------
					this.eomSystem.getDataModelStorageSettings().put(EomDataModelStorageHandler.EOM_SETTING_EOM_FILE_LOCATION, this.getRelativeProjectPath(file));
				} else {
					tsg = null;
				}
			}
			break;
		default:
			break;
		}
		return tsg;
	}
	
	/**
	 * Checks if the specified file is a file in the current project.
	 *
	 * @param fileToCheck the file to check
	 * @return true, if is current project file
	 */
	private boolean isInProjectDirectory(File fileToCheck) {
		
		if (fileToCheck==null || fileToCheck.exists()==false || fileToCheck.isDirectory()==true || FileHelper.isInDirectory(BundleHelper.getProjectDirectory(), fileToCheck)==false) {
			String title = "Wrong source directory of the model!";
			String message = "The specified file is not part of the current project directory!";
			JOptionPane.showMessageDialog(this.table, message, title, JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	/**
	 * Returns the relative project path of the specified file.
	 *
	 * @param fileToUse the file to use
	 * @return the relative project path
	 */
	private String getRelativeProjectPath(File fileToUse) {
		return FileHelper.getRelativePath(BundleHelper.getProjectDirectory(), fileToUse);
	}
	
}