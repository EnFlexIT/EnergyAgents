package de.enflexit.ea.core.validation.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Icon;
import javax.swing.JButton;

import org.agentgui.gui.swing.MainWindowExtension;
import org.awb.env.networkModel.controller.GraphEnvironmentController;

import agentgui.core.application.Application;
import agentgui.core.application.ApplicationListener;
import agentgui.core.project.Project;
import agentgui.core.project.setup.SimulationSetupNotification;
import de.enflexit.ea.core.validation.BundleHelper;
import de.enflexit.ea.core.validation.HyGridValidationMessage;
import de.enflexit.ea.core.validation.HyGridValidationProcess;
import de.enflexit.ea.core.validation.HyGridValidationProcessListener;
import de.enflexit.ea.core.validation.HyGridValidationMessage.MessageType;

/**
 * The Class HyGridValidator.
 */
public class HyGridValidator extends MainWindowExtension implements ApplicationListener, Observer {

	private JButton jButtonHyGridValidator;
	
	private HyGridValidationProcess hyGridValidationProcess;
	private HyGridValidatorDialog hyGridValidatorDialog;
	
	private MessageType lastMessageType;
	
	/**
	 * Instantiates the validator.
	 */
	public HyGridValidator() {	}

	/* (non-Javadoc)
	 * @see org.agentgui.gui.swing.MainWindowExtension#initialize()
	 */
	@Override
	public void initialize() {
		Application.addApplicationListener(this);
		this.addToolbarComponent(this.getJButtonHyGridValidator(), null, SeparatorPosition.SeparatorAfter);
	}

	/**
	 * Returns the JButton for the HyGrid validation.
	 * @return the j button hy grid validator
	 */
	private JButton getJButtonHyGridValidator() {
		if (jButtonHyGridValidator == null) {
			jButtonHyGridValidator = new JButton();
			jButtonHyGridValidator.setToolTipText("Validate Agent.HyGrid-Setup ...");
			jButtonHyGridValidator.setSize(new Dimension(26, 26));
			jButtonHyGridValidator.setPreferredSize(new Dimension(26, 26));
			jButtonHyGridValidator.setIcon(BundleHelper.getImageIcon("ValidationGreen.png"));
			jButtonHyGridValidator.setEnabled(false);
			jButtonHyGridValidator.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					HyGridValidator.this.checkHyGridSetup(true);
				}
			});
		}
		return jButtonHyGridValidator;
	}
	/**
	 * Checks the current HyGrid setup.
	 */
	public void checkHyGridSetup() {
		this.checkHyGridSetup(false);
	}
	/**
	 * Checks the current HyGrid setup.
	 * @param set true, if the GUI has to be displayed
	 */
	public void checkHyGridSetup(boolean isSetVisible) {
		this.getHyGridValidatorDialog().setVisible(isSetVisible);
		this.getHyGridValidationProcess().validateCurrentSetupInThread();
	}
	
	/**
	 * Returns the HyGridValidatorDialog.
	 * @return the HyGridValidatorDialog
	 */
	private HyGridValidatorDialog getHyGridValidatorDialog() {
		if (hyGridValidatorDialog==null ) {
			Frame owner = Application.getMainWindow();
			hyGridValidatorDialog = new HyGridValidatorDialog(owner, this.getHyGridValidationProcess());
		}
		return hyGridValidatorDialog;
	}
	/**
	 * Return the HyGridValidationProcess.
	 * @return the HyGrid validation process
	 */
	private HyGridValidationProcess getHyGridValidationProcess() {
		if (hyGridValidationProcess==null) {
			hyGridValidationProcess = new HyGridValidationProcess();
			hyGridValidationProcess.addHyGridValidationProcessListener(new HyGridValidationProcessListener() {
				@Override
				public void processExecuted() {
					HyGridValidator.this.setJButtonHyGridValidatorColor(MessageType.Information, true);
				}
				@Override
				public void messageReceived(HyGridValidationMessage message) {
					HyGridValidator.this.setJButtonHyGridValidatorColor(message.getMessageType(), false);
				}
				@Override
				public void processFinalized() {
				}
			});
		}
		return hyGridValidationProcess;
	}
	
	/**
	 * Sets the button image color.
	 * @param newMessageType the new message type
	 */
	private void setJButtonHyGridValidatorColor(MessageType newMessageType, boolean overwriteLastType) {
		
		// --- Substitute last type? ------------
		if (overwriteLastType==true) {
			this.lastMessageType = newMessageType;
			
		} else {
			switch (this.lastMessageType) {
			case Information:
				if (newMessageType==MessageType.Warning || newMessageType==MessageType.Error) {
					this.lastMessageType = newMessageType;
				}
				break;
				
			case Warning:
				if (newMessageType==MessageType.Error) {
					this.lastMessageType = newMessageType;
				}
				break;
				
			case Error:
				break;
				
			default:
				this.lastMessageType = newMessageType;
				break;
			}
		}
		
		// --- Set the image icon ---------------
		Icon coloredIcon = null;
		switch (this.lastMessageType) {
		case Information:
			coloredIcon = BundleHelper.getImageIcon("ValidationGreen.png");
			break;
		case Warning:
			coloredIcon = BundleHelper.getImageIcon("ValidationYellow.png");
			break;
		case Error:
			coloredIcon = BundleHelper.getImageIcon("ValidationRed.png");
			break;
		}
		this.getJButtonHyGridValidator().setIcon(coloredIcon);
	}
	

	// ------------------------------------------------------------------------
	// --- From here the application and project listener is implemented ------ 
	// ------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see agentgui.core.application.ApplicationListener#onApplicationEvent(agentgui.core.application.ApplicationListener.ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		
		Project project = null;
		switch (event.getApplicationEvent()) {
		case ApplicationEvent.PROJECT_LOADED:
			if (event.getEventObject()!=null) {
				project = (Project) event.getEventObject();
				if (this.isHyGridProject(project)==true) {
					project.addObserver(this);
				}
			}
			break;

		case ApplicationEvent.PROJECT_CLOSED:
			if (event.getEventObject()!=null) {
				project = (Project) event.getEventObject();
				project.deleteObserver(this);
				this.getJButtonHyGridValidator().setEnabled(false);
			}
			break;
			
		case ApplicationEvent.PROJECT_FOCUSED:
			if (event.getEventObject()!=null) {
				project = (Project) event.getEventObject();
				boolean isHyGridProject = this.isHyGridProject(project);
				this.getJButtonHyGridValidator().setEnabled(isHyGridProject);
				if (isHyGridProject==true) {
					this.checkHyGridSetup();
				}
				
			} else {
				this.getJButtonHyGridValidator().setEnabled(false);
			}
			break;
		} 
		
	}
	/**
	 * Checks if is the specified project is a HyGrid project.
	 *
	 * @param project the project
	 * @return true, if is HyGrid project
	 */
	private boolean isHyGridProject(Project project) {
		if (project==null || project.getEnvironmentController()==null || !(project.getEnvironmentController() instanceof GraphEnvironmentController)) {
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable observable, Object updateObject) {
		
		// --------------------------------------------------------------------
		// --- Will be get updates from an observed project -------------------
		// --------------------------------------------------------------------
		
		if (updateObject instanceof String) {
			// --- Changes in the project -----------------			
			String projectNotification = (String) updateObject;
			switch (projectNotification) {
			case Project.PREPARE_FOR_SAVING:
				this.checkHyGridSetup();
				break;

			default:
				break;
			}
			
		} else if (updateObject instanceof SimulationSetupNotification) {
			// --- Changes in the setup -------------------
			SimulationSetupNotification setupNotification = (SimulationSetupNotification) updateObject;
			switch (setupNotification.getUpdateReason()) {
			case SIMULATION_SETUP_LOAD:
				this.checkHyGridSetup();
				break;

			default:
				break;
			}
		}
		
	}
	
}
