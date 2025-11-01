package de.enflexit.ea.core.validation.ui;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import de.enflexit.awb.baseUI.ToolBarGroup;
import de.enflexit.awb.baseUI.mainWindow.MainWindowExtension;
import de.enflexit.awb.core.Application;
import de.enflexit.awb.core.ApplicationListener;
import de.enflexit.ea.core.validation.BundleHelper;
import de.enflexit.ea.core.validation.HyGridValidationMessage;
import de.enflexit.ea.core.validation.HyGridValidationMessage.MessageType;
import de.enflexit.ea.core.validation.HyGridValidationProcess;
import de.enflexit.ea.core.validation.HyGridValidationProcessListener;

/**
 * The Class HyGridValidator represents an AWB {@link MainWindowExtension} and integrates
 * the {@link HyGridValidationProcess} and corresponding user interactions to an Energy Agent project.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class HyGridValidator extends MainWindowExtension implements ApplicationListener {

	private JButton jButtonHyGridValidator;
	
	private HyGridValidationProcessListener hyGridValidationProcessListener;
	private HyGridValidatorDialog hyGridValidatorDialog;
	
	private MessageType lastMessageType = MessageType.Information;
	
	/**
	 * Instantiates the HyGridValidator.
	 */
	public HyGridValidator() { }

	/* (non-Javadoc)
	 * @see org.agentgui.gui.swing.MainWindowExtension#initialize()
	 */
	@Override
	public void initialize() {
		// --- Add HyGridValidator as listener to validation process ----------
		this.getHyGridValidationProcess().addHyGridValidationProcessListener(this.getHyGridValidationProcessListener());
		// --- Add HyGridValidator as application listener --------------------
		Application.addApplicationListener(this);
		// --- Define the elements of this MainWindowExtension ----------------
		this.addToolbarComponent(this.getJButtonHyGridValidator(), 0, true, ToolBarGroup.MAS_Control);
	}

	/**
	 * Returns the JButton for the HyGrid validation.
	 * @return the j button hy grid validator
	 */
	private JButton getJButtonHyGridValidator() {
		if (jButtonHyGridValidator == null) {
			jButtonHyGridValidator = new JButton();
			jButtonHyGridValidator.setToolTipText("Validate Energy Agent - Setup ...");
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
	 * @param set true, if the visualization has to be displayed
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
			hyGridValidatorDialog = new HyGridValidatorDialog((Window)Application.getMainWindow(), this.getHyGridValidationProcess());
		}
		return hyGridValidatorDialog;
	}
	/**
	 * Return the singleton instance of the HyGridValidationProcess.
	 * @return the HyGrid validation process
	 */
	private HyGridValidationProcess getHyGridValidationProcess() {
		return HyGridValidationProcess.getInstance();
	}
	/**
	 * Gets the local HyGridValidationProcessListener.
	 * @return the HyGridValidationProcessListener
	 */
	private HyGridValidationProcessListener getHyGridValidationProcessListener() {
		if (hyGridValidationProcessListener==null) {
			hyGridValidationProcessListener = new HyGridValidationProcessListener() {
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
			};
		}
		return hyGridValidationProcessListener;
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
	// --- From here the application listener is implemented ------------------ 
	// ------------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see agentgui.core.application.ApplicationListener#onApplicationEvent(agentgui.core.application.ApplicationListener.ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		
		switch (event.getApplicationEvent()) {
		case ApplicationEvent.PROJECT_FOCUSED:
			// --- Happens earlier ------------------------
			this.getJButtonHyGridValidator().setEnabled(HyGridValidationProcess.isHyGridProject(event.getEventObject()));
			break;
		case ApplicationEvent.PROJECT_CLOSED:
			if (event.getEventObject()!=null) {
				this.getJButtonHyGridValidator().setEnabled(false);
			}
			break;
		} 
	}
	
}
