package de.enflexit.ea.core.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentFactory;

import agentgui.core.application.Application;
import agentgui.core.environment.EnvironmentController;
import agentgui.core.project.Project;
import agentgui.simulationService.environment.AbstractEnvironmentModel;
import de.enflexit.ea.core.validation.HyGridValidationMessage.MessageType;
import energy.GlobalInfo;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystem;
import energy.optionModel.TechnicalSystemGroup;
import hygrid.env.HyGridAbstractEnvironmentModel;

/**
 * The Class HyGridValidationProcess does the actual check of the HyGrid setup.
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class HyGridValidationProcess {

	private enum ListenerInformation {
		Executed,
		MessageReceived,
		Finalized
	}
	
	private enum SingleConfigurationEntity {
		Project,
		Setup,
		NetworkModel,
		HyGridAbstractEnvironmentModel
	}
	
	private boolean isAddTestMessages = false;
	
	private List<HyGridValidationProcessListener> validationListener;
	private List<HyGridValidationMessage> hygridValidationMessages;

	
	/**
	 * Validates the current setup in an individual thread.
	 */
	public void validateCurrentSetupInThread() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				HyGridValidationProcess.this.validateCurrentSetup();
			}
		}, this.getClass().getSimpleName()).start();
	}
	/**
	 * Validates the current setup in the current thread.
	 */
	public void validateCurrentSetup() {
		
		// --- Reset old messages -------------------------
		this.setHygridValidationMessages(null);
		
		// --- Check to access the project ----------------
		if (this.getProject()==null) {
			this.addMessage("Could not access any project !", MessageType.Error);
			return;
		}
		// --- Check to access the graphController --------
		if (this.getGraphController()==null) {
			this.addMessage("Could not access the projects GraphEnvironementController!", MessageType.Error);
			return;
		}

		// --- Execute the specific validation checks -----
		this.doHyGridChecks();
	}
	
	/**
	 * Does the HyGrid checks.
	 */
	private void doHyGridChecks() {
		
		long checkTimeStart = System.currentTimeMillis();
		
		// --- Inform listener about the execution ------------------
		this.informHyGridValidationProcessListener(ListenerInformation.Executed, null);
		
		// --- Get registered services ------------------------------
		List<HyGridValidationService> serviceList = this.getValidationServiceList();
		if (serviceList.size()==0) {
			this.addMessage("No " + HyGridValidationService.class.getSimpleName() + " was registered. - Exit validation process!", MessageType.Information);
			return;
		}
		// --- Get validation checks --------------------------------		
		List<HyGridValidationAdapter> validationList = this.getValidationList(serviceList);
		if (validationList.size()==0) {
			this.addMessage("No validation checks were registered to the " + this.getClass().getSimpleName() + ". - Exit validation process!", MessageType.Information);
			return;
		}
		
		// --- Print validation start message ----------------------- 
		String nServiceText  = serviceList.size() + " service";
		if (serviceList.size()>1) nServiceText += "s";
		String nValidationText = validationList.size() + " validation check";
		if (validationList.size()>1) nValidationText += "s";
		this.addMessage("Execute HyGrid-Checks with " + nServiceText + " and " + nValidationText + " ... ", MessageType.Information);


		// ----------------------------------------------------------
		// --- Add test messages ------------------------------------
		// ----------------------------------------------------------
		this.addTestMessages();
		
		// ----------------------------------------------------------
		// --- Do the single entity checks --------------------------
		// ----------------------------------------------------------
		this.checkSingleConfigurationEntity(validationList, SingleConfigurationEntity.Project);
		this.checkSingleConfigurationEntity(validationList, SingleConfigurationEntity.Setup);
		this.checkSingleConfigurationEntity(validationList, SingleConfigurationEntity.NetworkModel);
		this.checkSingleConfigurationEntity(validationList, SingleConfigurationEntity.HyGridAbstractEnvironmentModel);
		
		// ----------------------------------------------------------
		// --- Validate the configured EOM models -------------------
		// ----------------------------------------------------------
		this.checkNetworkComponents(validationList);
		
		
		// --- Set the finalized message ----------------------------
		long durationMS = System.currentTimeMillis() - checkTimeStart;
		double durationS = (double)durationMS / 1000.0;
		double durationMin = (double)durationS / 60.0;
		
		String durationText = null;
		if (durationMS<=1000) {
			durationText = durationMS + " ms";
		} else {
			if (durationS<=300) {
				durationText = GlobalInfo.round(durationS, 2) + " s";
			} else {
				durationText = GlobalInfo.round(durationMin , 2) + " min";
			}
		}
		this.addMessage("Done! - Finalized in " + durationText + ".", MessageType.Information);

		// --- Inform listener about the finalization ---------------
		this.informHyGridValidationProcessListener(ListenerInformation.Finalized, null);

	}
	
	/**
	 * Checks the current {@link NetworkComponent}s.
	 * @param validationList the validation list
	 */
	private void checkNetworkComponents(List<HyGridValidationAdapter> validationList) {
		
		NetworkModel networkModel = this.getGraphController().getNetworkModel();
		
		Vector<NetworkComponent> netCompList = networkModel.getNetworkComponentVectorSorted();
		for (int i = 0; i < netCompList.size(); i++) {
			// --- Check each NetworkComponent ----------------------
			NetworkComponent netComp = netCompList.get(i);
			TechnicalSystem ts = null;
			TechnicalSystemGroup tsg = null;
			ScheduleList sl = null;
			
			// --- Run over the list of validation classes ---------- 
			for (int v = 0; v < validationList.size(); v++) {
				
				HyGridValidationAdapter validator = validationList.get(v);
				String validatorClassName = validator.getClass().getName();
				try {
					// --- Call the validation methods --------------
					HyGridValidationMessage message = validator.validateNetworkComponent(netComp);
					this.addMessage(message, validator);

					// -- Check the data model of the component -------------
					Object dataModel = netComp.getDataModel();
					if (dataModel instanceof TechnicalSystem) {
						ts = (TechnicalSystem) dataModel;
					} else if (dataModel instanceof TechnicalSystemGroup) {
						tsg = (TechnicalSystemGroup) dataModel;
					} else if (dataModel instanceof ScheduleList) {
						sl = (ScheduleList)dataModel;
					}
					
					// --- Call validation methods for EOM model? ---
					if (ts!=null) {
						message = validator.validateEomTechnicalSystem(netComp, ts);
						this.addMessage(message, validator);
					}
					if (tsg!=null) {
						message = validator.validateEomTechnicalSystemGroup(netComp, tsg);
						this.addMessage(message, validator);
					}
					if (sl!=null) {
						message = validator.validateEomScheduleList(netComp, sl);
						this.addMessage(message, validator);
					}
					
				} catch (Exception ex) {
					// --- Create a message for the exception -----------
					HyGridValidationMessage errMessage = new HyGridValidationMessage("Exception in validation class '" + validatorClassName + "'", MessageType.Error);
					errMessage.setDescription(ex.getMessage());
					this.addMessage(errMessage);
					ex.printStackTrace();				
				}
			} // end inner for
		} // end outer for
	}
	
	/**
	 * Check single configuration entity.
	 *
	 * @param validationList the validation list
	 * @param configEntity the actual single configuration entity
	 */
	private void checkSingleConfigurationEntity(List<HyGridValidationAdapter> validationList, SingleConfigurationEntity configEntity) {
		
		for (int i = 0; i < validationList.size(); i++) {
			
			HyGridValidationAdapter validator = validationList.get(i);
			String validatorClassName = validator.getClass().getName();
			try {
				// --- Call the validation method -------------------
				HyGridValidationMessage message = validator.validateProject(this.getProject());
				switch (configEntity) {
				case Project:
					message = validator.validateProject(this.getProject());
					break;
				case Setup:
					message = validator.validateSetup(this.getProject().getSimulationSetups().getCurrSimSetup());
					break;
				case NetworkModel:
					message = validator.validateNetworkModel(this.getGraphController().getNetworkModel());
					break;
				case HyGridAbstractEnvironmentModel:
					message = validator.validateHyGridAbstractEnvironmentModel(this.getHyGridAbstractEnvironmentModel());
					break;
				}
				this.addMessage(message, validator);
				
			} catch (Exception ex) {
				// --- Create a message for the exception -----------
				HyGridValidationMessage errMessage = new HyGridValidationMessage("Exception in validation class '" + validatorClassName + "' while checking the " + configEntity.name() + ".", MessageType.Error);
				errMessage.setDescription(ex.getMessage());
				this.addMessage(errMessage);
				ex.printStackTrace();				
			}
		}
	}
	
	/**
	 * Returns the list of validation checks that were provided by the services.
	 *
	 * @param serviceList the service list
	 * @return the validator list
	 */
	private List<HyGridValidationAdapter> getValidationList(List<HyGridValidationService> serviceList) {
		
		List<HyGridValidationAdapter> validatorList = new ArrayList<>();
		
		// --- Walk through the list of specified services --------------------
		for (int i = 0; i < serviceList.size(); i++) {
			// --- Get the checks from the registered service -----------------
			HyGridValidationService validatorService = serviceList.get(i);
			ArrayList<HyGridValidationAdapter> serviceValidatorList = validatorService.getHyGridValidationChecks(this.isHeadlessOperation());
			if (serviceValidatorList==null) continue;
			
			for (int j = 0; j < serviceValidatorList.size(); j++) {
				// --- Add single validator to result list -------------------- 
				HyGridValidationAdapter validationAdapter = serviceValidatorList.get(j);
				// --- Set the required project instances to the adapter --
				validationAdapter.setProject(this.getProject());
				validationAdapter.setSetup(this.getProject().getSimulationSetups().getCurrSimSetup());
				validationAdapter.setGraphController(this.getGraphController());
				validationAdapter.setNetworkModel(this.getGraphController().getNetworkModel());
				validationAdapter.setHyGridAbstractEnvironmentModel(this.getHyGridAbstractEnvironmentModel());	
				validatorList.add(validationAdapter);
			}
		}
		return validatorList;
	}
	/**
	 * Returns the validation service list.
	 * @return the validation service list
	 */
	private List<HyGridValidationService> getValidationServiceList() {
		
		List<HyGridValidationService> serviceList = new ArrayList<>();
		
		try {
			// --- Search for the registered validation services -------------- 
			BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
			String serviceFilter = "(" + Constants.OBJECTCLASS + "=" + HyGridValidationService.class.getName() + ")";
			ServiceReference<?>[] serviceReferences = bundleContext.getAllServiceReferences(null, serviceFilter);
			if (serviceReferences!=null) {
				for (int i = 0; i < serviceReferences.length; i++) {
					@SuppressWarnings("unchecked")
					ServiceReference<ComponentFactory<HyGridValidationService>> serviceRef = (ServiceReference<ComponentFactory<HyGridValidationService>>) serviceReferences[i];
					try {
						// --- get the service and add to result list ---------
						HyGridValidationService service = (HyGridValidationService)bundleContext.getService(serviceRef);
						if (serviceList!=null && serviceList.contains(service)==false) {
							serviceList.add(service);	
						}
						
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				
			} else {
				System.err.println("[" + this.getClass().getSimpleName() + "] : Could not find any " + HyGridValidationService.class.getSimpleName() + ".");
			}
		
		} catch (InvalidSyntaxException isEx) {
			isEx.printStackTrace();
		}
		return serviceList;
	}
	
	// ------------------------------------------------------------------------
	// --- From here, access to required runtime instances will be available --  
	// ------------------------------------------------------------------------
	/**
	 * Returns the current project.
	 * @return the project
	 */
	private Project getProject() {
		return Application.getProjectFocused();
	}
	/**
	 * Returns the current {@link GraphEnvironmentController}.
	 * @return the graph controller
	 */
	private GraphEnvironmentController getGraphController() {
		GraphEnvironmentController graphController = null; 	
		Project project = this.getProject(); 
		if (project!=null) {
			EnvironmentController envController = project.getEnvironmentController();
			if (envController instanceof GraphEnvironmentController) {
				graphController = (GraphEnvironmentController) envController;		
			}
		}
		return graphController;
	}
	/**
	 * Returns the current HyGridAbstractEnvironmentModel.
	 * @return the HyGridAbstractEnvironmentModel
	 */
	private HyGridAbstractEnvironmentModel getHyGridAbstractEnvironmentModel() {
		HyGridAbstractEnvironmentModel hygAbsEnvModel=null;
		GraphEnvironmentController graphController = this.getGraphController();
		if (graphController!=null) {
			AbstractEnvironmentModel absEnvModel = graphController.getAbstractEnvironmentModel();
			if (absEnvModel instanceof HyGridAbstractEnvironmentModel) {
				hygAbsEnvModel = (HyGridAbstractEnvironmentModel)absEnvModel;	
			} else {
				if (absEnvModel!=null) {
					this.addMessage("The current abstract environment model is not of type '" + HyGridAbstractEnvironmentModel.class.getSimpleName() + "'!", MessageType.Error);
				}
			}	
		}
		return hygAbsEnvModel;
	}
	/**
	 * Checks if is headless operation.
	 * @return true, if is headless operation
	 */
	private boolean isHeadlessOperation() {
		return Application.isOperatingHeadless();
	}

	// ------------------------------------------------------------------------
	// --- From here, handling of HyGridValidationProcessListener -------------  
	// ------------------------------------------------------------------------
	/**
	 * Gets the hy grid validation process listener.
	 * @return the hy grid validation process listener
	 */
	private List<HyGridValidationProcessListener> getHyGridValidationProcessListener() {
		if (validationListener==null) {
			validationListener = new ArrayList<>();
		}
		return validationListener;
	}
	/**
	 * Adds the specified HyGridValidationProcessListener.
	 * @param listener the HyGridValidationProcessListener to add
	 */
	public void addHyGridValidationProcessListener(HyGridValidationProcessListener listener) {
		if (listener==null) return;
		if (this.getHyGridValidationProcessListener().contains(listener)==true) return;
		this.getHyGridValidationProcessListener().add(listener);
	}
	/**
	 * Removes the specified HyGridValidationProcessListener.
	 * @param listener the HyGridValidationProcessListener to remove
	 */
	public void removeHyGridValidationProcessListener(HyGridValidationProcessListener listener) {
		if (listener==null) return;
		if (this.getHyGridValidationProcessListener().contains(listener)==false) return;
		this.getHyGridValidationProcessListener().remove(listener);
	}
	/**
	 * Inform HyGridValidationProcessListener about the specified event.
	 *
	 * @param info the kind of information to send to the listener
	 * @param message the message
	 */
	private void informHyGridValidationProcessListener(ListenerInformation info, HyGridValidationMessage message) {
		
		for (int i = 0; i < this.getHyGridValidationProcessListener().size(); i++) {
			HyGridValidationProcessListener listener = this.getHyGridValidationProcessListener().get(i);
			switch (info) {
			case Executed:
				listener.processExecuted();
				break;
			case MessageReceived:
				listener.messageReceived(message);
				break;
			case Finalized:
				listener.processFinalized();
				break;
			}
		}
	}
	
	// ------------------------------------------------------------------------
	// --- From here, handling of HyGridValidationMessage ---------------------  
	// ------------------------------------------------------------------------
	/**
	 * Returns the DefaultListModel with the errors found.
	 * @return the DefaultListModel with errors found.
	 */
	public List<HyGridValidationMessage> getHygridValidationMessages() {
		if (this.hygridValidationMessages==null) {
			this.hygridValidationMessages = new ArrayList<HyGridValidationMessage>();
		}
		return this.hygridValidationMessages;
	}
	/**
	 * Sets the list model messages.
	 * @param newDefaultListModel the new list model messages
	 */
	private void setHygridValidationMessages(ArrayList<HyGridValidationMessage> newDefaultListModel) {
		this.hygridValidationMessages = newDefaultListModel;
	}
	/**
	 * Adds a new {@link HyGridValidationMessage} to the local stack.
	 * @param newMessage the new message
	 * @param messageType the message type
	 */
	private void addMessage(String newMessage, MessageType messageType) {
		if (newMessage!=null) {
			this.addMessage(new HyGridValidationMessage(newMessage, messageType));
		}
	}
	/**
	 * Adds the specified {@link HyGridValidationMessage} to the local stack.
	 * @param newValidationMessage the new validation message
	 */
	private void addMessage(HyGridValidationMessage newValidationMessage) {
		if (newValidationMessage!=null) {
			this.getHygridValidationMessages().add(newValidationMessage);
			this.informHyGridValidationProcessListener(ListenerInformation.MessageReceived, newValidationMessage);
		}
	}
	/**
	 * Adds the specified {@link HyGridValidationMessage} to the local stack.
	 *
	 * @param newValidationMessage the new validation message
	 * @param validator the validator instance that produced the message
	 */
	private void addMessage(HyGridValidationMessage newValidationMessage, HyGridValidationAdapter validator) {
		if (newValidationMessage!=null) {
			newValidationMessage.setFoundByClass(validator.getClass().getName());
			this.getHygridValidationMessages().add(newValidationMessage);
			this.informHyGridValidationProcessListener(ListenerInformation.MessageReceived, newValidationMessage);
		}
	}
	
	/**
	 * Adds the test messages.
	 */
	private void addTestMessages() {
		
		if (isAddTestMessages==false) return;
		
		String description = "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		
		HyGridValidationMessage message = new HyGridValidationMessage("Test Information 1", MessageType.Information);
		message.setDescription(description);
		this.addMessage(message);

		this.addMessage("Test Information 2", MessageType.Information);
		
		this.addMessage("Test Warning 1", MessageType.Warning);
		this.addMessage("Test Warning 2", MessageType.Warning);
		
		this.addMessage("Test Error 1", MessageType.Error);
		this.addMessage("Test Error 2", MessageType.Error);
		
		this.addMessage("Test Information 3", MessageType.Information);
		this.addMessage("Test Warning 3", MessageType.Warning);
		this.addMessage("Test Error 3", MessageType.Error);
	}
}
