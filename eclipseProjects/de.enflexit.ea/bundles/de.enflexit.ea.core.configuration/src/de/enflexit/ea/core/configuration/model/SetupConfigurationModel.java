package de.enflexit.ea.core.configuration.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.awb.env.networkModel.controller.GraphEnvironmentController;

import agentgui.core.application.Application;
import agentgui.core.project.Project;
import agentgui.core.project.setup.SimulationSetup;
import agentgui.core.project.setup.SimulationSetupNotification;
import de.enflexit.common.Observable;
import de.enflexit.common.Observer;
import de.enflexit.ea.core.configuration.SetupConfigurationAttributeService;
import de.enflexit.ea.core.configuration.SetupConfigurationService;
import de.enflexit.ea.core.configuration.model.components.ConfigurableComponent;

/**
 * The Class SetupConfigurationModel.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class SetupConfigurationModel implements Observer {

	public static final String PROPERTY_MODEL_UI_MESSAGE = "SetupConfigurationModel-UI-Message";
	public static final String PROPERTY_MODEL_CREATED = "SetupConfigurationModel-Created";
	
	private Project project;
	private SimulationSetup setup;
	
	private Vector<SetupConfigurationAttributeService> attributeServiceVector;
	private ConfigurableComponentVector configurableComponents;
	private DefaultTableModel configTableModel;
	
	private PropertyChangeSupport pcSupport;
	
	/**
	 * Instantiates a new setup configuration model.
	 */
	public SetupConfigurationModel() {	
		this(null, null);
	}
	/**
	 * Instantiates a new setup configuration model.
	 *
	 * @param project the project
	 * @param setup the setup
	 */
	public SetupConfigurationModel(Project project, SimulationSetup setup) {
		this.project = project;
		this.setup = setup;
		this.initialize();
	}
	/**
	 * Initializes the current model.
	 */
	private void initialize() {
		Project project = this.getProject();
		if (project!=null) {
			this.getProject().addObserver(this);
		}
		this.reCreateConfigurationTableModel();
	}
	/**
	 * Disposes the current model.
	 */
	public void dispose() {
		Project project = this.getProject();
		if (project!=null) {
			this.getProject().deleteObserver(this);
		}
	}
	
	/**
     * Returns the current project instance.
     * @return the project
     */
    public Project getProject() {
    	if (project==null) {
    		project = Application.getProjectFocused();
    	}
    	return project;
    }
	/* (non-Javadoc)
	 * @see de.enflexit.common.Observer#update(de.enflexit.common.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable observable, Object updateObject) {
		
		if (updateObject instanceof SimulationSetupNotification) {
			
			SimulationSetupNotification sscn = (SimulationSetupNotification) updateObject;
			switch (sscn.getUpdateReason()) {
			case SIMULATION_SETUP_COPY:
				this.setSetup(this.getProject().getSimulationSetups().getCurrSimSetup());
				break;
			case SIMULATION_SETUP_LOAD:
				this.clearTableModel();
				break;
			case SIMULATION_SETUP_DETAILS_LOADED:
				this.setSetup(this.getProject().getSimulationSetups().getCurrSimSetup());
				break;
			default:
				break;
			}
		}
	}
	
	 /**
     * Return the current projects {@link SimulationSetup}.
     * @return the setup
     */
    public SimulationSetup getSetup() {
    	if (this.setup==null) {
    		Project project = this.getProject();
    		if (project!=null) {
    			setup = this.getProject().getSimulationSetups().getCurrSimSetup();
    		}
    	}
    	return setup;
    }
	/**
	 * Will be invoked, if a new setup is selected.
	 * @param newSetup the new {@link SimulationSetup}
	 */
    public void setSetup(SimulationSetup newSetup) {
		if (newSetup!=this.setup) {
			this.setup = newSetup;
			// --- Forward the new setup information ------
			this.reCreateConfigurationTableModel();
		}
	}
    
	/**
	 * Returns the current graph controller.
	 * @return the graph controller
	 */
	public GraphEnvironmentController getGraphController() {
		if (this.getProject()!=null && this.getProject().getEnvironmentController() instanceof GraphEnvironmentController) {
			return (GraphEnvironmentController) this.getProject().getEnvironmentController();
		}
		return null;
	}
		
	// ------------------------------------------------------------------------
	// --- PropertyChangeSupport and -event handling -------------------------- 
	// ------------------------------------------------------------------------	
	/**
	 * Returns the local property change support.
	 * @return the property change support
	 */
	private PropertyChangeSupport getPropertyChangeSupport() {
		if (pcSupport==null) {
			pcSupport = new PropertyChangeSupport(this);
		}
		return pcSupport;
	}
	/**
	 * Adds the specified property change listener.
	 * @param listener the listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.getPropertyChangeSupport().addPropertyChangeListener(listener);
    }
    /**
     * Removes the specified property change listener.
     * @param listener the listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.getPropertyChangeSupport().removePropertyChangeListener(listener);
    }
  
    /**
     * Sets the UI message by using the local property change support.
     * @param message the new UI message
     */
    public void setUIMessage(String message) {
    	this.getPropertyChangeSupport().firePropertyChange(new PropertyChangeEvent(this, PROPERTY_MODEL_UI_MESSAGE, null, message));
    }
    
	
	// ------------------------------------------------------------------------
	// --- From here, methods to create and fill the local table model -------- 
	// ------------------------------------------------------------------------	
    /**
     * Returns the vector of configurable components.
     * @return the configurable component vector
     */
    public ConfigurableComponentVector getConfigurableComponentVector() {
    	if (configurableComponents==null) {
    		configurableComponents = new ConfigurableComponentVector(this.getGraphController());
    	}
    	return configurableComponents;
    }
    /**
	 * Returns the list of SetupConfigurationAttributeService currently defined by the {@link SetupConfigurationService}s.
	 * @return the setup configuration service attribute list
	 */
    private List<SetupConfigurationAttributeService> getSetupConfigurationAttributeServiceList() {
    	return this.getConfigurableComponentVector().getSetupConfigurationAttributeServiceList();
    }
    
	/**
	 * Returns - based on the currently available configuration services -  the column vector of the table model.
	 * @return the column vector
	 */
	public Vector<SetupConfigurationAttributeService> getColumnVector() {
		if (attributeServiceVector==null) {
			attributeServiceVector = new Vector<>();
			attributeServiceVector.add(new DescriptionColumn("Description"));
			// --- Get all registered service attributes ----------------------
			attributeServiceVector.addAll(this.getSetupConfigurationAttributeServiceList());
		}
		return attributeServiceVector;
	}
	
	/**
	 * Returns the table data as data vector.
	 * @return the data vector
	 */
	private Vector<Vector<?>> getDataVector() {
		
		Vector<Vector<?>> dataVector = new Vector<>();
		
		Vector<ConfigurableComponent> confCompVector = this.getConfigurableComponentVector();
		for (ConfigurableComponent confComp : confCompVector) {
			if (confComp.getRelevantSetupConfigurationAttributeServiceList().size()>0) {
				dataVector.add(this.createRowVector(confComp));
			}
		}
		return dataVector;
	}

	/**
	 * Creates a data row for the specified {@link ConfigurableComponent}.
	 *
	 * @param confComp the ConfigurableComponent
	 * @return the vector
	 */
	private Vector<?> createRowVector(ConfigurableComponent confComp) {
		
		Vector<Object> dataRow = new Vector<>();
		dataRow.add(confComp); // --- Description -------------------
		
		for (SetupConfigurationAttributeService attributeService : this.getSetupConfigurationAttributeServiceList()) {
			
			if (confComp.getRelevantSetupConfigurationAttributeServiceList().contains(attributeService)==true) {
				// --- Set the current value of that attribute ------   
				dataRow.add(attributeService.getValue(confComp));
				
			} else {
				// --- Not relevant - set an indicator value --------
				dataRow.add(null);
			}
		}
		return dataRow;
	}
	
	/**
	 * Returns the configuration table model.
	 * @return the configuration table model
	 */
	public DefaultTableModel getConfigurationTableModel() {
		if (configTableModel==null) {
			configTableModel = new DefaultTableModel(this.getDataVector(), this.getColumnVector()) {
				private static final long serialVersionUID = -8104668585340458305L;
				
				/* (non-Javadoc)
				 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
				 */
				@Override
				public boolean isCellEditable(int row, int column) {
					// --- First column is read only ----------------
					if (column==0) {
						return false;
					}

					// --- Check if something is to configure -------
					SetupConfigurationModel scm = SetupConfigurationModel.this;
					ConfigurableComponent confComponent = (ConfigurableComponent) scm.getConfigurationTableModel().getValueAt(row, 0);
					SetupConfigurationAttributeService attributeService = scm.getColumnVector().get(column);
					return confComponent.getRelevantSetupConfigurationAttributeServiceList().contains(attributeService);
				}
				/* (non-Javadoc)
				 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
				 */
				@Override
				public Class<?> getColumnClass(int columnIndex) {
					
					Class<?> colClass = null;
					switch (columnIndex) {
					case 0:
						colClass = String.class;
						break;

					default:
						// --- Get type from column vector ----------
						SetupConfigurationAttributeService attributeService = (SetupConfigurationAttributeService) this.columnIdentifiers.get(columnIndex);
						colClass = attributeService.getSetupConfigurationAttribute().getType();
						break;
					}

					// --- Fallback solution ------------------------
					if (colClass==null) {
						colClass = super.getColumnClass(columnIndex);;
					}
					return colClass;
				}
				
			};
			this.getPropertyChangeSupport().firePropertyChange(new PropertyChangeEvent(this, PROPERTY_MODEL_CREATED, null, configTableModel));
		}
		return configTableModel;
	}
	
	/**
	 * Clears the table model.
	 */
	private void clearTableModel() {
		if (this.configTableModel!=null) {
			this.configTableModel.setRowCount(0);
			this.configTableModel.setColumnCount(0);
		}
	}
	
	
	/**
	 * Recreates the configuration table model in a dedicated thread.
	 */
	public void reCreateConfigurationTableModelInThread() {
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				SetupConfigurationModel.this.reCreateConfigurationTableModel();
			}
		}
		, "Setup Configuration Model - Reading").start();
	}
	
	/**
	 * (Re) Creates the configuration table model.
	 * @return the default table model
	 */
	public DefaultTableModel reCreateConfigurationTableModel() {
		
		// --- Reset locally stored data --------------------------------------
		if (this.configTableModel!=null) {
			this.clearTableModel();
			this.configTableModel = null;
			this.attributeServiceVector = null;
			this.configurableComponents = null;
		}
		
		// --- Create a new table model ---------------------------------------
		return this.getConfigurationTableModel();
	}
	
	/**
	 * Sets the configuration to setup.
	 */
	public void setConfigurationToSetup() {
		new ConfigurationToSetupWriter(this).start();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		int nTableRows   = this.getConfigurationTableModel().getRowCount();
		int nTableColums = this.getConfigurationTableModel().getColumnCount();
		
		int nConfigValues = 0;
		for (int row = 0; row < this.getConfigurationTableModel().getRowCount(); row++) {
			for (int col = 0; col < this.getConfigurationTableModel().getColumnCount(); col++) {
				if (this.getConfigurationTableModel().isCellEditable(row, col)==true) nConfigValues++;
			}
		}
		
		String description = "";
		description += nTableRows + " components ";
		description += ", " + (nTableColums-1) + " attributes ";
		description += " => " + nConfigValues + " configurable values";
		return description;
	}
	
}
