package de.enflexit.ea.core.awbIntegration.plugin.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import agentgui.core.application.Application;
import agentgui.core.application.Language;
import agentgui.core.config.GlobalInfo;
import agentgui.core.gui.projectwindow.simsetup.TimeModelController;
import agentgui.core.project.Project;
import agentgui.simulationService.time.TimeModelContinuous;
import agentgui.simulationService.time.TimeModelDateBased;
import agentgui.simulationService.time.TimeModelDiscrete;
import agentgui.simulationService.time.TimeUnit;
import agentgui.simulationService.time.TimeUnitVector;
import de.enflexit.common.Observable;
import de.enflexit.common.Observer;
import de.enflexit.common.ServiceFinder;
import de.enflexit.common.classLoadService.BaseClassLoadServiceUtility;
import de.enflexit.common.classSelection.ClassSelectionDialog;
import de.enflexit.common.swing.KeyAdapter4Numbers;
import de.enflexit.db.hibernate.gui.DatabaseSelectionPanel;
import de.enflexit.ea.core.awbIntegration.plugin.AWBIntegrationPlugIn;
import de.enflexit.ea.core.dataModel.absEnvModel.DisplayUpdateConfiguration;
import de.enflexit.ea.core.dataModel.absEnvModel.DisplayUpdateConfiguration.UpdateMechanism;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.ExecutionDataBase;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.SnapshotDecisionLocation;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.StateTransmission;
import de.enflexit.ea.core.dataModel.graphLayout.AbstractGraphElementLayoutSettingsPanel;
import de.enflexit.ea.core.dataModel.graphLayout.GraphElementLayoutService;
import energy.optionModel.ScheduleLengthRestriction;
import energy.schedule.ScheduleTransformerKeyValueConfiguration;
import energy.schedule.ScheduleTransformerKeyValueConfiguration.DeltaMechanism;
import energy.schedule.gui.ScheduleLengthRestrictionListener;
import energy.schedule.gui.ScheduleLengthRestrictionPanel;

/**
 * The Class HyGridSettingsTab represents a tab that is a {@link JScrollPane} and that is 
 * added by the {@link AWBIntegrationPlugIn}.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class HyGridSettingsTab extends JScrollPane implements Observer, ActionListener, DocumentListener {

	private static final long serialVersionUID = -1983567619404123877L;

	private Project currProject;
	private boolean isPauseObserver;
	
	private JPanel jPanelMain;
	
	private JRadioButton jRadioButtonPowerTransmissionReduced;
	private JLabel jLabelTransWatt;
	private JLabel jLabelTransPercent;
	private JLabel jLabelTransHeaderLiveBit;
	private JLabel jLabelTransLiveBit;
	private JRadioButton jRadioButtonTransByAbsolute;
	private JRadioButton jRadioButtonTransByPercent;
	private JTextField jTextFieldTransWatt;
	private JTextField jTextFieldTransPercent;
	private JTextField jTextFieldTransLiveBit;
	
	private JLabel jLabelHeaderVisualization;
	private JRadioButton jRadioButtonDisplayEnabled;
	private JRadioButton jRadioButtonDisplayDisable;

	private KeyAdapter4Numbers keyListenerLongInteger;
	private KeyAdapter4Numbers keyListenerFloat;
	private boolean actOnActionsOrDocumentChanges = true;
	private JLabel jLabelTimeModelSelection;
	private JRadioButton jRadioButtonTimeModelDiscrete;
	private JRadioButton jRadioButtonTimeModelContinuous;
	
	private JLabel jLabelInterval;
	private JTextField jTextFieldIntervalWidthValue;
	private JComboBox<TimeUnit> jComboBoxIntervalWidthUnit;

	private JLabel jLabelNetCalcInterval;
	private JTextField jTextFieldNetCalcInterval;
	private JComboBox<TimeUnit> jComboBoxNetCalcIntervalWidthUnit;
	
	private JLabel jLabelDateHandling;
	private ScheduleLengthRestrictionPanel jPanelScheduleLengthRestriction;
	private JLabel jLabelSimulationType;
	private JRadioButton jRadioButtonExecutionBasedOnPowerFlow;
	private JRadioButton jRadioButtonExecutionBasedOnSensorData;
	
	private JTabbedPane jTabbedPaneColorSettings;
	private JCheckBox jCheckBoxSnapshotSimulation;
	private JPanel jPanelDiscreteSettings;
	private JPanel jPanelContinousSettings;
	private JSeparator jSeparatorAfterTimeModelSelection;
	private JPanel jPanelDataHandling;
	private JSeparator jSeparatorAfterDataHandling;
	private JPanel jPanelVisualizationSettings;
	private JLabel jLabelHeaderColorAtRuntime;
	private JLabel jLabelHeaderVisualizationSettings;
	private JRadioButton jRadioButtonSnapshotDecentral;
	private JRadioButton jRadioButtonSnapshotCentral;
	private JTextField jTextFieldCentralDecisionClass;
	private JButton jButtonCentralDecisionClass;
	private JPanel jPanelSnapshotCentralDecision;
	private JRadioButton jRadioButtonPowerTransmissionAsDefined;
	private JLabel jLabelHeaderRuntimeDataSettings;
	private JSeparator jSeparatorAfterRuntimeDataHandling;

	private JPanel jPanelRuntimeDataSettings;
	private JCheckBox jCheckboxStoreToDatabase;
	private JCheckBox jCheckboxStoreToDedicatedDatabase;
	private DatabaseSelectionPanel databaseSelectionPanel;
	private JLabel jLabelDatabasePrefix;
	private JTextField jTextFieldDatabasePrefix;

	
	/**
	 * This is the default constructor
	 */
	public HyGridSettingsTab(Project project) {
		super();

		this.currProject=project;
		this.currProject.addObserver(this);
		
		this.initialize();
		this.setTranslation();
		this.loadDataModelToForm();
	}

	/**
	 * Returns the {@link HyGridAbstractEnvironmentModel}.
	 * @return the HyGrid abstract environment model
	 */
	private HyGridAbstractEnvironmentModel getHyGridAbstractEnvironmentModel() {
		return (HyGridAbstractEnvironmentModel) this.currProject.getUserRuntimeObject();
	}
	
	/**
	 * Load data model to form.
	 */
	private void loadDataModelToForm() {
		
		this.actOnActionsOrDocumentChanges = false;
		
		// --------------------------------------
		// --- Set the current time model -------
		// --------------------------------------
		String timeModelClass = this.currProject.getTimeModelClass();
		if (timeModelClass==null || timeModelClass.trim().equals("") || timeModelClass.equals(TimeModelDiscrete.class.getName())==true) {
			// --- Select discrete time model ---
			this.getJRadioButtonTimeModelDiscrete().setSelected(true);
			this.getJRadioButtonTimeModelContinuous().setSelected(false);
		} else if (timeModelClass.equals(TimeModelContinuous.class.getName())) {
			// --- Select continuous time model -
			this.getJRadioButtonTimeModelDiscrete().setSelected(false);
			this.getJRadioButtonTimeModelContinuous().setSelected(true);
		}
		
		// --------------------------------------
		// --- Read the HyGrid abstract model ---
		// --------------------------------------		
		HyGridAbstractEnvironmentModel hyGridDM = this.getHyGridAbstractEnvironmentModel();
		
		// --- Set simulation interval length ---
		this.loadNetworkCalculationIntervalLength(hyGridDM);
		this.loadSimulationIntervalLength(hyGridDM);
		
		if (this.currProject.getTimeModelClass()!=null && this.currProject.getTimeModelClass().equals(TimeModelDiscrete.class.getName())==true) {
			this.getJCheckBoxSnapshotSimulation().setSelected(hyGridDM.isDiscreteSnapshotSimulation());
			if (hyGridDM.getSnapshotDecisionLocation()==null || hyGridDM.getSnapshotDecisionLocation()==SnapshotDecisionLocation.Decentral) {
				this.getJRadioButtonSnapshotDecentral().setSelected(true);
				this.getJRadioButtonSnapshotCentral().setSelected(false);
				this.getJTextFieldCentralDecisionClass().setText(hyGridDM.getSnapshotCentralDecisionClass());
				this.getJTextFieldCentralDecisionClass().setToolTipText(hyGridDM.getSnapshotCentralDecisionClass());
			} else {
				this.getJRadioButtonSnapshotDecentral().setSelected(false);
				this.getJRadioButtonSnapshotCentral().setSelected(true);
				this.getJTextFieldCentralDecisionClass().setText(hyGridDM.getSnapshotCentralDecisionClass());
				this.getJTextFieldCentralDecisionClass().setToolTipText(hyGridDM.getSnapshotCentralDecisionClass());
			}
		} else {
			this.getJCheckBoxSnapshotSimulation().setSelected(false);
			this.getJRadioButtonSnapshotDecentral().setSelected(true);
			this.getJRadioButtonSnapshotCentral().setSelected(false);
			this.getJTextFieldCentralDecisionClass().setText(null);
			this.getJTextFieldCentralDecisionClass().setToolTipText(null);
		}
		
		// --- Set the StateTransmission --------
		this.getJRadioButtonPowerTransmissionAsDefined().setSelected(hyGridDM.getStateTransmission()==StateTransmission.AsDefined);
		this.getJRadioButtonPowerTransmissionReduced().setSelected(hyGridDM.getStateTransmission()==StateTransmission.Reduced);
		
		// --- Set the simulation data base -----
		switch (hyGridDM.getExecutionDataBase()) {
		case NodePowerFlows:
			this.getJRadioButtonExecutionBasedOnPowerFlow().setSelected(true);
			this.getJRadioButtonExecutionBasedOnSensorData().setSelected(false);
			break;
		case SensorData:
			this.getJRadioButtonExecutionBasedOnPowerFlow().setSelected(false);
			this.getJRadioButtonExecutionBasedOnSensorData().setSelected(true);
			break;
		}
		
		// --- Database settings ----------------
		this.getJCheckboxStoreToDatabase().setSelected(hyGridDM.isSaveRuntimeInformationToDatabase());
		this.getJCheckboxStoreToDedicatedDatabase().setSelected(hyGridDM.isSaveRuntimeInformationToDedicatedDatabase());
		this.getJTextFieldDatabasePrefix().setText(hyGridDM.getDedicatedDatabasePrefix());
		this.getDatabaseSelectionPanel().setFactoryIDsSelected(hyGridDM.getFactoryIDList());
		
		// --- Set energy transmission ----------
		ScheduleTransformerKeyValueConfiguration etc = hyGridDM.getEnergyTransmissionConfiguration();
		switch (etc.getDeltaMechanism()) {
		case Absolute:
			this.getJRadioButtonTransByAbsolute().setSelected(true);
			this.getJRadioButtonTransByPercent().setSelected(false);
			break;
		case Percentage:
			this.getJRadioButtonTransByAbsolute().setSelected(false);
			this.getJRadioButtonTransByPercent().setSelected(true);
			break;
		}
		this.getJTextFieldTransWatt().setText(((Integer)etc.getDeltaAbsoluteWatt()).toString());
		this.getJTextFieldTransPercent().setText(((Float)etc.getDeltaPercentage()).toString());
		this.getJTextFieldTransLiveBit().setText(((Long)etc.getTimePeriodForLiveBit()).toString());
		
		// --- Set display updates --------------
		DisplayUpdateConfiguration duc = hyGridDM.getDisplayUpdateConfiguration();
		switch (duc.getUpdateMechanism()) {
		case EnableUpdates:
			this.getJRadioButtonDisplayEnabled().setSelected(true);
			this.getJRadioButtonDisplayDisable().setSelected(false);
			break;
		case DisableUpdates:
			this.getJRadioButtonDisplayEnabled().setSelected(false);
			this.getJRadioButtonDisplayDisable().setSelected(true);
			break;
		}

		this.setTimeExplanation(this.getJTextFieldTransLiveBit(), getJLabelTransLiveBit());
		
		// --- Set ScheduleLenghtDescription ----
		this.getJPanelScheduleLengthRestriction().setScheduleLengthRestriction(hyGridDM.getScheduleLengthRestriction());
		
		this.actOnActionsOrDocumentChanges = true;
		this.enableControls();
	}
	
	/**
	 * Load form data to the data model.
	 */
	private void loadFormToDataModel() {
		
		HyGridAbstractEnvironmentModel hyGridDM = this.getHyGridAbstractEnvironmentModel();
		HyGridAbstractEnvironmentModel hyGridDMOld = hyGridDM.getCopy(); 
				
		// --- Save Simulation Interval length ------------
		this.saveNetworkCalculationIntervalLength(hyGridDM);
		this.saveSimulationIntervalLength(hyGridDM);

		// --- Set State Transmission ---------------------
		if (this.getJRadioButtonPowerTransmissionAsDefined().isSelected()==true) {
			hyGridDM.setStateTransmission(StateTransmission.AsDefined);
		} else if (this.getJRadioButtonPowerTransmissionReduced().isSelected()==true) {
			hyGridDM.setStateTransmission(StateTransmission.Reduced);
		}
		
		// --- Values for snapshot simulations ------------
		String centralDecisionClass = this.getJTextFieldCentralDecisionClass().getText();
		if (centralDecisionClass.isEmpty()==true) centralDecisionClass = null;
		
		hyGridDM.setDiscreteSnapshotSimulation(this.getJCheckBoxSnapshotSimulation().isSelected());
		if (this.getJRadioButtonSnapshotDecentral().isSelected()) {
			hyGridDM.setSnapshotDecisionLocation(SnapshotDecisionLocation.Decentral);
			hyGridDM.setSnapshotCentralDecisionClass(null);
		} else if (this.getJRadioButtonSnapshotCentral().isSelected()) {
			hyGridDM.setSnapshotDecisionLocation(SnapshotDecisionLocation.Central);
			hyGridDM.setSnapshotCentralDecisionClass(centralDecisionClass);
		}
		
		// --- Save the simulation execution base --------
		if (this.getJRadioButtonExecutionBasedOnPowerFlow().isSelected()) {
			hyGridDM.setExecutionDataBase(ExecutionDataBase.NodePowerFlows);
		} else if (this.getJRadioButtonExecutionBasedOnSensorData().isSelected()) {
			hyGridDM.setExecutionDataBase(ExecutionDataBase.SensorData);
		}
		
		// --- Database settings --------------------------
		hyGridDM.setSaveRuntimeInformationToDatabase(this.getJCheckboxStoreToDatabase().isSelected());
		hyGridDM.setSaveRuntimeInformationToDedicatedDatabase(this.getJCheckboxStoreToDedicatedDatabase().isSelected());
		hyGridDM.setDedicatedDatabasePrefix(this.getJTextFieldDatabasePrefix().getText());
		hyGridDM.setFactoryIDList(this.getDatabaseSelectionPanel().getFactoryIDsSelected());
		
		// --- Save energy transmission -------------------
		ScheduleTransformerKeyValueConfiguration etc = hyGridDM.getEnergyTransmissionConfiguration();
		if (this.getJRadioButtonTransByAbsolute().isSelected()) {
			etc.setDeltaMechanism(DeltaMechanism.Absolute);
		} else if (this.getJRadioButtonTransByPercent().isSelected()) {
			etc.setDeltaMechanism(DeltaMechanism.Percentage);
		}
		
		String deltaAbsoluteWattText = this.getJTextFieldTransWatt().getText();
		int deltaAbsoluteWatt = 0;
		if (deltaAbsoluteWattText!=null && deltaAbsoluteWattText.equals("")==false) {
			deltaAbsoluteWatt = Integer.parseInt(deltaAbsoluteWattText);
		}
		etc.setDeltaAbsoluteWatt(deltaAbsoluteWatt);
		
		String deltaPercentText = this.getJTextFieldTransPercent().getText();
		float deltaPercent = 1f;
		if (deltaPercentText!=null && deltaPercentText.equals("")==false) {
			try {
				deltaPercent = Float.parseFloat(deltaPercentText);	
			} catch (Exception ex) {
				deltaPercent = 1f;
			}
		}
		etc.setDeltaPercentage(deltaPercent);
		
		String timePeriodForLiveBitText = this.getJTextFieldTransLiveBit().getText();
		long timePeriodForLiveBit = 0;
		if (timePeriodForLiveBitText!=null && timePeriodForLiveBitText.equals("")==false) {
			timePeriodForLiveBit = Long.parseLong(timePeriodForLiveBitText);
		}
		etc.setTimePeriodForLiveBit(timePeriodForLiveBit);
		
		
		// --- Set display updates ------------------------
		DisplayUpdateConfiguration duc = hyGridDM.getDisplayUpdateConfiguration();
		if (this.getJRadioButtonDisplayEnabled().isSelected()) {
			duc.setUpdateMechanism(UpdateMechanism.EnableUpdates);	
		} else if (this.getJRadioButtonDisplayDisable().isSelected()) {
			duc.setUpdateMechanism(UpdateMechanism.DisableUpdates);
		}
		
		// --- Set ScheduleLenghtDescription --------------
		hyGridDM.setScheduleLengthRestriction(this.getJPanelScheduleLengthRestriction().getScheduleLengthRestriction());
		
		// --- Check if we have changes -------------------
		if (hyGridDM.equals(hyGridDMOld)==false) {
			this.isPauseObserver = true;
			this.currProject.setUserRuntimeObject(hyGridDM);
			this.isPauseObserver = false;
		}
	}
	

	/**
	 * Sets the time explanation.
	 */
	private void setTimeExplanation(JTextField sourceJTextField, JLabel destinJLabel) {
	
		String timePeriodText = sourceJTextField.getText();
		long timePeriod = 0;
		if (timePeriodText!=null && timePeriodText.equals("")==false) {
			timePeriod = Long.parseLong(timePeriodText);
		}

		double timeSeconds = (double)timePeriod / 1000.0;
		timeSeconds = Math.round(timeSeconds*1000.0) / 1000.0;

		double timeMinutes = ((((double)timePeriod / 1000) / 60.0));
		timeMinutes = Math.round(timeMinutes * 100.0) / 100.0;

		// --- Prepare Text -----------
		String explanation = timeSeconds + " s - " + timeMinutes + " Min";
		String oldText = destinJLabel.getText();
		String newText = null;
			
		int cutPos = oldText.indexOf("(");
		if (cutPos>=0) {
			oldText = oldText.substring(0, cutPos).trim();	
		}
		newText = oldText + " (" + explanation + ")";
		destinJLabel.setText(newText);
		
	}
	
	/**
	 * Sets the translations of the content.
	 */
	private void setTranslation() {
		
		jLabelTimeModelSelection.setText(Language.translate(this.getJLabelTimeModelSelection().getText(), Language.EN));
		jRadioButtonTimeModelDiscrete.setText(Language.translate(this.getJRadioButtonTimeModelDiscrete().getText(), Language.EN));
		jRadioButtonTimeModelContinuous.setText(Language.translate(this.getJRadioButtonTimeModelContinuous().getText(), Language.EN));
		
		jLabelInterval.setText(Language.translate(jLabelInterval.getText(), Language.EN));
		jLabelNetCalcInterval.setText(Language.translate(jLabelNetCalcInterval.getText(), Language.EN));
		jRadioButtonTransByAbsolute.setText(Language.translate(jRadioButtonTransByAbsolute.getText(), Language.EN));
		jRadioButtonTransByPercent.setText(Language.translate(jRadioButtonTransByPercent.getText(), Language.EN));
		jLabelTransLiveBit.setText(Language.translate(jLabelTransLiveBit.getText(), Language.EN));
		jLabelTransHeaderLiveBit.setText(Language.translate(jLabelTransHeaderLiveBit.getText(), Language.EN));
		
		jLabelHeaderVisualization.setText(Language.translate(jLabelHeaderVisualization.getText(), Language.EN));
		jRadioButtonDisplayEnabled.setText(Language.translate(jRadioButtonDisplayEnabled.getText(), Language.EN));
		jRadioButtonDisplayDisable.setText(Language.translate(jRadioButtonDisplayDisable.getText(), Language.EN));

	}
	
	/**
	 * This method initializes this
	 * @return void
	 */
	private void initialize() {
		
		this.setSize(1050, 750);
		this.setBorder(new EmptyBorder(0, 0, 0, 0));
		this.setViewportView(this.getJPanelMain());
	}

	/**
	 * Gets the main JPanel.
	 * @return the main JPanel
	 */
	private JPanel getJPanelMain() {
		if (jPanelMain==null) {
			jPanelMain = new JPanel();
			
			ButtonGroup bgTimeModel = new ButtonGroup();
			bgTimeModel.add(this.getJRadioButtonTimeModelDiscrete());
			bgTimeModel.add(this.getJRadioButtonTimeModelContinuous());
			
			ButtonGroup bgStateTrasnsmission = new ButtonGroup();
			bgStateTrasnsmission.add(this.getJRadioButtonPowerTransmissionAsDefined());
			bgStateTrasnsmission.add(this.getJRadioButtonPowerTransmissionReduced());
			
			ButtonGroup bgDiscreteSnapshot = new ButtonGroup();
			bgDiscreteSnapshot.add(this.getJRadioButtonSnapshotDecentral());
			bgDiscreteSnapshot.add(this.getJRadioButtonSnapshotCentral());
			
			ButtonGroup bgSimBaseOn = new ButtonGroup();
			bgSimBaseOn.add(this.getJRadioButtonExecutionBasedOnPowerFlow());
			bgSimBaseOn.add(this.getJRadioButtonExecutionBasedOnSensorData());
			
			ButtonGroup bgTrans = new ButtonGroup();
			bgTrans.add(this.getJRadioButtonTransByAbsolute());
			bgTrans.add(this.getJRadioButtonTransByPercent());
			
			ButtonGroup bgDisplay = new ButtonGroup();
			bgDisplay.add(this.getJRadioButtonDisplayEnabled());
			bgDisplay.add(this.getJRadioButtonDisplayDisable());
			
			
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{133, 0, 0};
			gridBagLayout.rowHeights = new int[]{16, 25, 0, 0, 26, 26, 0, 0, 0, 0, 0, 0};
			gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			jPanelMain.setLayout(gridBagLayout);
			jPanelMain.setSize(550, 893);
			
			GridBagConstraints gbc_jLabelTimeModelSelection = new GridBagConstraints();
			gbc_jLabelTimeModelSelection.anchor = GridBagConstraints.WEST;
			gbc_jLabelTimeModelSelection.insets = new Insets(10, 10, 2, 0);
			gbc_jLabelTimeModelSelection.gridx = 0;
			gbc_jLabelTimeModelSelection.gridy = 0;
			jPanelMain.add(getJLabelTimeModelSelection(), gbc_jLabelTimeModelSelection);
			GridBagConstraints gbc_jPanelContinousSettings = new GridBagConstraints();
			gbc_jPanelContinousSettings.anchor = GridBagConstraints.WEST;
			gbc_jPanelContinousSettings.insets = new Insets(5, 10, 0, 0);
			gbc_jPanelContinousSettings.fill = GridBagConstraints.VERTICAL;
			gbc_jPanelContinousSettings.gridx = 0;
			gbc_jPanelContinousSettings.gridy = 1;
			jPanelMain.add(getJPanelContinousSettings(), gbc_jPanelContinousSettings);
			GridBagConstraints gbc_jPanelDiscreteSettings = new GridBagConstraints();
			gbc_jPanelDiscreteSettings.insets = new Insets(5, 10, 0, 10);
			gbc_jPanelDiscreteSettings.fill = GridBagConstraints.BOTH;
			gbc_jPanelDiscreteSettings.gridx = 1;
			gbc_jPanelDiscreteSettings.gridy = 1;
			jPanelMain.add(getJPanelDiscreteSettings(), gbc_jPanelDiscreteSettings);
			GridBagConstraints gbc_jSeparatorAfterTimeModelSelection = new GridBagConstraints();
			gbc_jSeparatorAfterTimeModelSelection.insets = new Insets(15, 10, 10, 10);
			gbc_jSeparatorAfterTimeModelSelection.gridwidth = 2;
			gbc_jSeparatorAfterTimeModelSelection.fill = GridBagConstraints.HORIZONTAL;
			gbc_jSeparatorAfterTimeModelSelection.gridx = 0;
			gbc_jSeparatorAfterTimeModelSelection.gridy = 2;
			jPanelMain.add(getJSeparatorAfterTimeModelSelection(), gbc_jSeparatorAfterTimeModelSelection);
			GridBagConstraints gbc_jLabelDateHandling = new GridBagConstraints();
			gbc_jLabelDateHandling.anchor = GridBagConstraints.WEST;
			gbc_jLabelDateHandling.insets = new Insets(0, 10, 0, 5);
			gbc_jLabelDateHandling.gridx = 0;
			gbc_jLabelDateHandling.gridy = 3;
			jPanelMain.add(getJLabelDateHandling(), gbc_jLabelDateHandling);
			GridBagConstraints gbc_jPanelDataHandling = new GridBagConstraints();
			gbc_jPanelDataHandling.insets = new Insets(5, 10, 0, 10);
			gbc_jPanelDataHandling.fill = GridBagConstraints.BOTH;
			gbc_jPanelDataHandling.gridx = 0;
			gbc_jPanelDataHandling.gridy = 4;
			jPanelMain.add(getJPanelDataHandling(), gbc_jPanelDataHandling);
			GridBagConstraints gbc_jPanelScheduleLengthRestriction = new GridBagConstraints();
			gbc_jPanelScheduleLengthRestriction.insets = new Insets(0, 10, 0, 10);
			gbc_jPanelScheduleLengthRestriction.anchor = GridBagConstraints.NORTHWEST;
			gbc_jPanelScheduleLengthRestriction.gridx = 1;
			gbc_jPanelScheduleLengthRestriction.gridy = 4;
			jPanelMain.add(getJPanelScheduleLengthRestriction(), gbc_jPanelScheduleLengthRestriction);
			GridBagConstraints gbc_jSeparatorAfterDataHandling = new GridBagConstraints();
			gbc_jSeparatorAfterDataHandling.gridwidth = 2;
			gbc_jSeparatorAfterDataHandling.insets = new Insets(15, 10, 10, 10);
			gbc_jSeparatorAfterDataHandling.fill = GridBagConstraints.HORIZONTAL;
			gbc_jSeparatorAfterDataHandling.gridx = 0;
			gbc_jSeparatorAfterDataHandling.gridy = 5;
			jPanelMain.add(getJSeparatorAfterDataHandling(), gbc_jSeparatorAfterDataHandling);
			GridBagConstraints gbc_jLabelHeaderRuntimeDataSettings = new GridBagConstraints();
			gbc_jLabelHeaderRuntimeDataSettings.insets = new Insets(0, 10, 0, 0);
			gbc_jLabelHeaderRuntimeDataSettings.anchor = GridBagConstraints.WEST;
			gbc_jLabelHeaderRuntimeDataSettings.gridx = 0;
			gbc_jLabelHeaderRuntimeDataSettings.gridy = 6;
			jPanelMain.add(getJLabelHeaderRuntimeDataSettings(), gbc_jLabelHeaderRuntimeDataSettings);
			GridBagConstraints gbc_jPanelRuntimeDataSettings = new GridBagConstraints();
			gbc_jPanelRuntimeDataSettings.insets = new Insets(5, 10, 0, 10);
			gbc_jPanelRuntimeDataSettings.fill = GridBagConstraints.BOTH;
			gbc_jPanelRuntimeDataSettings.gridx = 0;
			gbc_jPanelRuntimeDataSettings.gridy = 7;
			jPanelMain.add(getJPanelRuntimeDataSettings(), gbc_jPanelRuntimeDataSettings);
			GridBagConstraints gbc_databaseSelectionPanel = new GridBagConstraints();
			gbc_databaseSelectionPanel.insets = new Insets(0, 10, 0, 10);
			gbc_databaseSelectionPanel.fill = GridBagConstraints.BOTH;
			gbc_databaseSelectionPanel.gridx = 1;
			gbc_databaseSelectionPanel.gridy = 7;
			jPanelMain.add(getDatabaseSelectionPanel(), gbc_databaseSelectionPanel);
			GridBagConstraints gbc_jSeparatorAfterRuntimeDataHandling = new GridBagConstraints();
			gbc_jSeparatorAfterRuntimeDataHandling.insets = new Insets(15, 10, 10, 10);
			gbc_jSeparatorAfterRuntimeDataHandling.fill = GridBagConstraints.HORIZONTAL;
			gbc_jSeparatorAfterRuntimeDataHandling.gridwidth = 2;
			gbc_jSeparatorAfterRuntimeDataHandling.gridx = 0;
			gbc_jSeparatorAfterRuntimeDataHandling.gridy = 8;
			jPanelMain.add(getJSeparatorAfterRuntimeDataHandling(), gbc_jSeparatorAfterRuntimeDataHandling);
			GridBagConstraints gbc_jLabelHeaderVisualizationSettings = new GridBagConstraints();
			gbc_jLabelHeaderVisualizationSettings.insets = new Insets(0, 10, 0, 0);
			gbc_jLabelHeaderVisualizationSettings.anchor = GridBagConstraints.WEST;
			gbc_jLabelHeaderVisualizationSettings.gridx = 0;
			gbc_jLabelHeaderVisualizationSettings.gridy = 9;
			jPanelMain.add(getJLabelHeaderVisualizationSettings(), gbc_jLabelHeaderVisualizationSettings);
			GridBagConstraints gbc_jPanelVisualizationSettings = new GridBagConstraints();
			gbc_jPanelVisualizationSettings.gridwidth = 2;
			gbc_jPanelVisualizationSettings.insets = new Insets(5, 10, 0, 10);
			gbc_jPanelVisualizationSettings.fill = GridBagConstraints.BOTH;
			gbc_jPanelVisualizationSettings.gridx = 0;
			gbc_jPanelVisualizationSettings.gridy = 10;
			jPanelMain.add(getJPanelVisualizationSettings(), gbc_jPanelVisualizationSettings);
			
		}
		return jPanelMain;
	}
	private JLabel getJLabelTimeModelSelection() {
		if (jLabelTimeModelSelection == null) {
			jLabelTimeModelSelection = new JLabel("Simulation Time Model");
			jLabelTimeModelSelection.setFont(new Font("Dialog", Font.BOLD, 13));
		}
		return jLabelTimeModelSelection;
	}
	private JPanel getJPanelContinousSettings() {
		if (jPanelContinousSettings == null) {
			jPanelContinousSettings = new JPanel();
			
			GridBagLayout gbl_jPanelContinousSettings = new GridBagLayout();
			gbl_jPanelContinousSettings.columnWidths = new int[]{0, 0, 0, 0};
			gbl_jPanelContinousSettings.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
			gbl_jPanelContinousSettings.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
			gbl_jPanelContinousSettings.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};

			GridBagConstraints gbc_jRadioButtonTimeModelContinuous = new GridBagConstraints();
			gbc_jRadioButtonTimeModelContinuous.anchor = GridBagConstraints.WEST;
			gbc_jRadioButtonTimeModelContinuous.gridx = 0;
			gbc_jRadioButtonTimeModelContinuous.gridy = 0;
			
			GridBagConstraints gbc_jLabelNetCalcInterval = new GridBagConstraints();
			gbc_jLabelNetCalcInterval.insets = new Insets(5, 0, 0, 0);
			gbc_jLabelNetCalcInterval.anchor = GridBagConstraints.WEST;
			gbc_jLabelNetCalcInterval.gridx = 0;
			gbc_jLabelNetCalcInterval.gridy = 1;
			GridBagConstraints gbc_jTextFieldNetCalcInterval = new GridBagConstraints();
			gbc_jTextFieldNetCalcInterval.insets = new Insets(5, 5, 0, 0);
			gbc_jTextFieldNetCalcInterval.anchor = GridBagConstraints.WEST;
			gbc_jTextFieldNetCalcInterval.gridx = 1;
			gbc_jTextFieldNetCalcInterval.gridy = 1;
			
			GridBagConstraints gbc_jRadioButtonPowerTransmissionAsDefined = new GridBagConstraints();
			gbc_jRadioButtonPowerTransmissionAsDefined.anchor = GridBagConstraints.WEST;
			gbc_jRadioButtonPowerTransmissionAsDefined.gridwidth = 3;
			gbc_jRadioButtonPowerTransmissionAsDefined.insets = new Insets(10, 0, 0, 0);
			gbc_jRadioButtonPowerTransmissionAsDefined.gridx = 0;
			gbc_jRadioButtonPowerTransmissionAsDefined.gridy = 2;
			
			GridBagConstraints gbc_jComboBoxNetCalcIntervalWidthUnit = new GridBagConstraints();
			gbc_jComboBoxNetCalcIntervalWidthUnit.insets = new Insets(5, 0, 0, 0);
			gbc_jComboBoxNetCalcIntervalWidthUnit.anchor = GridBagConstraints.WEST;
			gbc_jComboBoxNetCalcIntervalWidthUnit.gridx = 2;
			gbc_jComboBoxNetCalcIntervalWidthUnit.gridy = 1;
			GridBagConstraints gbc_jRadioButtonTransByAbsolute = new GridBagConstraints();
			gbc_jRadioButtonTransByAbsolute.fill = GridBagConstraints.HORIZONTAL;
			gbc_jRadioButtonTransByAbsolute.insets = new Insets(0, 20, 0, 0);
			GridBagConstraints gbc_jRadioButtonPowerTransmissionReduced = new GridBagConstraints();
			gbc_jRadioButtonPowerTransmissionReduced.insets = new Insets(5, 0, 0, 0);
			gbc_jRadioButtonPowerTransmissionReduced.gridwidth = 3;
			gbc_jRadioButtonPowerTransmissionReduced.anchor = GridBagConstraints.WEST;
			gbc_jRadioButtonPowerTransmissionReduced.gridx = 0;
			gbc_jRadioButtonPowerTransmissionReduced.gridy = 3;
			gbc_jRadioButtonTransByAbsolute.gridx = 0;
			gbc_jRadioButtonTransByAbsolute.gridy = 4;
			GridBagConstraints gbc_jTextFieldTransWatt = new GridBagConstraints();
			gbc_jTextFieldTransWatt.insets = new Insets(0, 5, 0, 0);
			gbc_jTextFieldTransWatt.anchor = GridBagConstraints.WEST;
			gbc_jTextFieldTransWatt.gridx = 1;
			gbc_jTextFieldTransWatt.gridy = 4;
			GridBagConstraints gbc_jLabelTransWatt = new GridBagConstraints();
			gbc_jLabelTransWatt.anchor = GridBagConstraints.WEST;
			gbc_jLabelTransWatt.gridx = 2;
			gbc_jLabelTransWatt.gridy = 4;

			GridBagConstraints gbc_jRadioButtonTransByPercent = new GridBagConstraints();
			gbc_jRadioButtonTransByPercent.fill = GridBagConstraints.HORIZONTAL;
			gbc_jRadioButtonTransByPercent.insets = new Insets(0, 20, 0, 0);
			gbc_jRadioButtonTransByPercent.gridx = 0;
			gbc_jRadioButtonTransByPercent.gridy = 5;
			GridBagConstraints gbc_jTextFieldTransPercent = new GridBagConstraints();
			gbc_jTextFieldTransPercent.insets = new Insets(0, 5, 0, 0);
			gbc_jTextFieldTransPercent.anchor = GridBagConstraints.WEST;
			gbc_jTextFieldTransPercent.gridx = 1;
			gbc_jTextFieldTransPercent.gridy = 5;
			GridBagConstraints gbc_jLabelTransPercent = new GridBagConstraints();
			gbc_jLabelTransPercent.anchor = GridBagConstraints.WEST;
			gbc_jLabelTransPercent.gridx = 2;
			gbc_jLabelTransPercent.gridy = 5;
			
			GridBagConstraints gbc_jLabelTransHeaderLiveBit = new GridBagConstraints();
			gbc_jLabelTransHeaderLiveBit.insets = new Insets(0, 20, 0, 0);
			gbc_jLabelTransHeaderLiveBit.anchor = GridBagConstraints.WEST;
			gbc_jLabelTransHeaderLiveBit.gridx = 0;
			gbc_jLabelTransHeaderLiveBit.gridy = 6;
			GridBagConstraints gbc_jTextFieldTransLiveBit = new GridBagConstraints();
			gbc_jTextFieldTransLiveBit.insets = new Insets(0, 5, 0, 0);
			gbc_jTextFieldTransLiveBit.anchor = GridBagConstraints.WEST;
			gbc_jTextFieldTransLiveBit.gridx = 1;
			gbc_jTextFieldTransLiveBit.gridy = 6;
			GridBagConstraints gbc_jLabelTransLiveBit = new GridBagConstraints();
			gbc_jLabelTransLiveBit.anchor = GridBagConstraints.WEST;
			gbc_jLabelTransLiveBit.gridx = 2;
			gbc_jLabelTransLiveBit.gridy = 6;

			jPanelContinousSettings.setLayout(gbl_jPanelContinousSettings);
			jPanelContinousSettings.add(getJRadioButtonTimeModelContinuous(), gbc_jRadioButtonTimeModelContinuous);
			jPanelContinousSettings.add(getJLabelNetCalcInterval(), gbc_jLabelNetCalcInterval);
			jPanelContinousSettings.add(getJTextFieldNetCalcInterval(), gbc_jTextFieldNetCalcInterval);
			jPanelContinousSettings.add(getJComboBoxNetCalcIntervalWidthUnit(), gbc_jComboBoxNetCalcIntervalWidthUnit);
			jPanelContinousSettings.add(getJRadioButtonPowerTransmissionAsDefined(), gbc_jRadioButtonPowerTransmissionAsDefined);
			jPanelContinousSettings.add(getJRadioButtonPowerTransmissionReduced(), gbc_jRadioButtonPowerTransmissionReduced);
			jPanelContinousSettings.add(getJRadioButtonTransByAbsolute(), gbc_jRadioButtonTransByAbsolute);
			jPanelContinousSettings.add(getJTextFieldTransWatt(), gbc_jTextFieldTransWatt);
			jPanelContinousSettings.add(getJLabelLabelTransWatt(), gbc_jLabelTransWatt);
			jPanelContinousSettings.add(getJRadioButtonTransByPercent(), gbc_jRadioButtonTransByPercent);
			jPanelContinousSettings.add(getJTextFieldTransPercent(), gbc_jTextFieldTransPercent);
			jPanelContinousSettings.add(getJLabelTransPercent(), gbc_jLabelTransPercent);
			jPanelContinousSettings.add(getJLabelTransHeaderLiveBit(), gbc_jLabelTransHeaderLiveBit);
			jPanelContinousSettings.add(getJTextFieldTransLiveBit(), gbc_jTextFieldTransLiveBit);
			jPanelContinousSettings.add(getJLabelTransLiveBit(), gbc_jLabelTransLiveBit);
			
		}
		return jPanelContinousSettings;
	}	
	private JRadioButton getJRadioButtonTimeModelContinuous() {
		if (jRadioButtonTimeModelContinuous == null) {
			jRadioButtonTimeModelContinuous = new JRadioButton("Continuous Time Model");
			jRadioButtonTimeModelContinuous.setFont(new Font("Dialog", Font.BOLD, 13));
			jRadioButtonTimeModelContinuous.addActionListener(this);
		}
		return jRadioButtonTimeModelContinuous;
	}
	private JLabel getJLabelNetCalcInterval() {
		if (jLabelNetCalcInterval == null) {
			jLabelNetCalcInterval = new JLabel("Network Calculation Interval");
			jLabelNetCalcInterval.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelNetCalcInterval;
	}
	private JTextField getJTextFieldNetCalcInterval() {
		if (jTextFieldNetCalcInterval == null) {
			jTextFieldNetCalcInterval = new JTextField();
			jTextFieldNetCalcInterval.setText("0");
			jTextFieldNetCalcInterval.setPreferredSize(new Dimension(100, 26));
			jTextFieldNetCalcInterval.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextFieldNetCalcInterval.addKeyListener(new KeyAdapter4Numbers(false));
			jTextFieldNetCalcInterval.getDocument().addDocumentListener(this);
		}
		return jTextFieldNetCalcInterval;
	}
	private JComboBox<TimeUnit> getJComboBoxNetCalcIntervalWidthUnit() {
		if (jComboBoxNetCalcIntervalWidthUnit == null) {
			jComboBoxNetCalcIntervalWidthUnit = new JComboBox<TimeUnit>(new DefaultComboBoxModel<TimeUnit>(new TimeUnitVector()));
			jComboBoxNetCalcIntervalWidthUnit.setSelectedIndex(0);
			jComboBoxNetCalcIntervalWidthUnit.setPreferredSize(new Dimension(120, 26));
			jComboBoxNetCalcIntervalWidthUnit.setFont(new Font("Dialog", Font.PLAIN, 12));
			jComboBoxNetCalcIntervalWidthUnit.setEnabled(false);
			jComboBoxNetCalcIntervalWidthUnit.addActionListener(this);
		}
		return jComboBoxNetCalcIntervalWidthUnit;
	}
	
	private JRadioButton getJRadioButtonPowerTransmissionAsDefined() {
		if (jRadioButtonPowerTransmissionAsDefined == null) {
			jRadioButtonPowerTransmissionAsDefined = new JRadioButton();
			jRadioButtonPowerTransmissionAsDefined.setText("State-Transmission of power signals as defined");
			jRadioButtonPowerTransmissionAsDefined.setFont(new Font("Dialog", Font.BOLD, 13));
			jRadioButtonPowerTransmissionAsDefined.setEnabled(true);
			jRadioButtonPowerTransmissionAsDefined.addActionListener(this);
		}
		return jRadioButtonPowerTransmissionAsDefined;
	}
	private JRadioButton getJRadioButtonPowerTransmissionReduced() {
		if (jRadioButtonPowerTransmissionReduced==null) {
			jRadioButtonPowerTransmissionReduced = new JRadioButton();
			jRadioButtonPowerTransmissionReduced.setText("Reduced State-Transmission of power signals ...");
			jRadioButtonPowerTransmissionReduced.setFont(new Font("Dialog", Font.BOLD, 13));
			jRadioButtonPowerTransmissionReduced.addActionListener(this);
		}
		return jRadioButtonPowerTransmissionReduced;
	}
	private JRadioButton getJRadioButtonTransByAbsolute() {
		if (jRadioButtonTransByAbsolute == null) {
			jRadioButtonTransByAbsolute = new JRadioButton();
			jRadioButtonTransByAbsolute.setFont(new Font("Dialog", Font.PLAIN, 12));
			jRadioButtonTransByAbsolute.setText("... depending on watts (absolute)");
			jRadioButtonTransByAbsolute.addActionListener(this);
		}
		return jRadioButtonTransByAbsolute;
	}
	private JTextField getJTextFieldTransWatt() {
		if (jTextFieldTransWatt == null) {
			jTextFieldTransWatt = new JTextField();
			jTextFieldTransWatt.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextFieldTransWatt.setPreferredSize(new Dimension(100, 26));
			jTextFieldTransWatt.addKeyListener(this.getKeyAdapter4LongAndInteger());
			jTextFieldTransWatt.getDocument().addDocumentListener(this);
		}
		return jTextFieldTransWatt;
	}
	private JLabel getJLabelLabelTransWatt() {
		if (jLabelTransWatt==null) {
			jLabelTransWatt = new JLabel();
			jLabelTransWatt.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabelTransWatt.setText(" Watt");
		}
		return jLabelTransWatt;
	}
	private JRadioButton getJRadioButtonTransByPercent() {
		if (jRadioButtonTransByPercent == null) {
			jRadioButtonTransByPercent = new JRadioButton();
			jRadioButtonTransByPercent.setFont(new Font("Dialog", Font.PLAIN, 12));
			jRadioButtonTransByPercent.setText("... depending on delta watts (percent)");
			jRadioButtonTransByPercent.addActionListener(this);
		}
		return jRadioButtonTransByPercent;
	}
	private JTextField getJTextFieldTransPercent() {
		if (jTextFieldTransPercent == null) {
			jTextFieldTransPercent = new JTextField();
			jTextFieldTransPercent.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextFieldTransPercent.setPreferredSize(new Dimension(100, 26));
			jTextFieldTransPercent.addKeyListener(this.getKeyAdapter4Float());
			jTextFieldTransPercent.getDocument().addDocumentListener(this);
		}
		return jTextFieldTransPercent;
	}
	private JLabel getJLabelTransPercent() {
		if (jLabelTransPercent==null) {
			jLabelTransPercent = new JLabel();
			jLabelTransPercent.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabelTransPercent.setText(" % ");
		}
		return jLabelTransPercent;
	}
	private JLabel getJLabelTransHeaderLiveBit() {
		if (jLabelTransHeaderLiveBit==null) {
			jLabelTransHeaderLiveBit = new JLabel();
			jLabelTransHeaderLiveBit.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabelTransHeaderLiveBit.setText("Time after a signal must be sent at least");
		}
		return jLabelTransHeaderLiveBit;
	}
	private JTextField getJTextFieldTransLiveBit() {
		if (jTextFieldTransLiveBit == null) {
			jTextFieldTransLiveBit = new JTextField();
			jTextFieldTransLiveBit.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextFieldTransLiveBit.setPreferredSize(new Dimension(100, 26));
			jTextFieldTransLiveBit.addKeyListener(this.getKeyAdapter4LongAndInteger());
			jTextFieldTransLiveBit.getDocument().addDocumentListener(this);
		}
		return jTextFieldTransLiveBit;
	}
	private JLabel getJLabelTransLiveBit() {
		if (jLabelTransLiveBit==null) {
			jLabelTransLiveBit = new JLabel();
			jLabelTransLiveBit.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabelTransLiveBit.setText("Milliseconds");
		}
		return jLabelTransLiveBit;
	}
	
	
	private JPanel getJPanelDiscreteSettings() {
		if (jPanelDiscreteSettings == null) {
			jPanelDiscreteSettings = new JPanel();
			
			GridBagLayout gbl_jPanelDiscreteSettings = new GridBagLayout();
			gbl_jPanelDiscreteSettings.columnWidths = new int[]{0, 0, 0, 0};
			gbl_jPanelDiscreteSettings.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
			gbl_jPanelDiscreteSettings.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
			gbl_jPanelDiscreteSettings.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			
			GridBagConstraints gbc_jRadioButtonTimeModelDiscrete = new GridBagConstraints();
			gbc_jRadioButtonTimeModelDiscrete.anchor = GridBagConstraints.WEST;
			gbc_jRadioButtonTimeModelDiscrete.gridx = 0;
			gbc_jRadioButtonTimeModelDiscrete.gridy = 0;
			
			GridBagConstraints gbc_jLabelInterval = new GridBagConstraints();
			gbc_jLabelInterval.insets = new Insets(5, 0, 0, 0);
			gbc_jLabelInterval.anchor = GridBagConstraints.WEST;
			gbc_jLabelInterval.gridx = 0;
			gbc_jLabelInterval.gridy = 1;
			
			GridBagConstraints gbc_jTextFieldIntervalWidthValue = new GridBagConstraints();
			gbc_jTextFieldIntervalWidthValue.insets = new Insets(5, 5, 0, 0);
			gbc_jTextFieldIntervalWidthValue.anchor = GridBagConstraints.WEST;
			gbc_jTextFieldIntervalWidthValue.gridx = 1;
			gbc_jTextFieldIntervalWidthValue.gridy = 1;
			
			GridBagConstraints gbc_jComboBoxIntervalWidthUnit = new GridBagConstraints();
			gbc_jComboBoxIntervalWidthUnit.insets = new Insets(5, 0, 0, 0);
			gbc_jComboBoxIntervalWidthUnit.anchor = GridBagConstraints.WEST;
			gbc_jComboBoxIntervalWidthUnit.gridx = 2;
			gbc_jComboBoxIntervalWidthUnit.gridy = 1;
			
			GridBagConstraints gbc_jCheckBoxSnapshotSimulation = new GridBagConstraints();
			gbc_jCheckBoxSnapshotSimulation.insets = new Insets(10, 0, 0, 0);
			gbc_jCheckBoxSnapshotSimulation.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxSnapshotSimulation.gridx = 0;
			gbc_jCheckBoxSnapshotSimulation.gridy = 2;

			GridBagConstraints gbc_jRadioButtonSnapshotDecentral = new GridBagConstraints();
			gbc_jRadioButtonSnapshotDecentral.insets = new Insets(4, 0, 0, 0);
			gbc_jRadioButtonSnapshotDecentral.anchor = GridBagConstraints.NORTHWEST;
			gbc_jRadioButtonSnapshotDecentral.gridx = 0;
			gbc_jRadioButtonSnapshotDecentral.gridy = 3;

			GridBagConstraints gbc_jRadioButtonSnapshotCentral = new GridBagConstraints();
			gbc_jRadioButtonSnapshotCentral.gridwidth = 2;
			gbc_jRadioButtonSnapshotCentral.insets = new Insets(6, 0, 0, 0);
			gbc_jRadioButtonSnapshotCentral.anchor = GridBagConstraints.NORTHWEST;
			gbc_jRadioButtonSnapshotCentral.gridx = 0;
			gbc_jRadioButtonSnapshotCentral.gridy = 4;
			
			GridBagConstraints gbc_jPanelSnapshotCentralDecision = new GridBagConstraints();
			gbc_jPanelSnapshotCentralDecision.gridwidth = 3;
			gbc_jPanelSnapshotCentralDecision.insets = new Insets(4, 0, 0, 0);
			gbc_jPanelSnapshotCentralDecision.fill = GridBagConstraints.HORIZONTAL;
			gbc_jPanelSnapshotCentralDecision.gridx = 0;
			gbc_jPanelSnapshotCentralDecision.gridy = 5;

			
			jPanelDiscreteSettings.setLayout(gbl_jPanelDiscreteSettings);
			jPanelDiscreteSettings.add(getJRadioButtonTimeModelDiscrete(), gbc_jRadioButtonTimeModelDiscrete);
			jPanelDiscreteSettings.add(getJLabelInterval(), gbc_jLabelInterval);
			jPanelDiscreteSettings.add(getJTextFieldIntervalWidthValue(), gbc_jTextFieldIntervalWidthValue);
			jPanelDiscreteSettings.add(getJComboBoxIntervalWidthUnit(), gbc_jComboBoxIntervalWidthUnit);
			jPanelDiscreteSettings.add(getJCheckBoxSnapshotSimulation(), gbc_jCheckBoxSnapshotSimulation);
			jPanelDiscreteSettings.add(getJRadioButtonSnapshotDecentral(), gbc_jRadioButtonSnapshotDecentral);
			jPanelDiscreteSettings.add(getJRadioButtonSnapshotCentral(), gbc_jRadioButtonSnapshotCentral);
			jPanelDiscreteSettings.add(getJPanelSnapshotCentralDecision(), gbc_jPanelSnapshotCentralDecision);
		}
		return jPanelDiscreteSettings;
	}
	private JRadioButton getJRadioButtonTimeModelDiscrete() {
		if (jRadioButtonTimeModelDiscrete == null) {
			jRadioButtonTimeModelDiscrete = new JRadioButton("Discrete Time Model");
			jRadioButtonTimeModelDiscrete.setFont(new Font("Dialog", Font.BOLD, 13));
			jRadioButtonTimeModelDiscrete.addActionListener(this);
		}
		return jRadioButtonTimeModelDiscrete;
	}
	private JLabel getJLabelInterval() {
		if (jLabelInterval == null) {
			jLabelInterval = new JLabel("Time between two simulation steps");
			jLabelInterval.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return jLabelInterval;
	}
	private JTextField getJTextFieldIntervalWidthValue() {
		if (jTextFieldIntervalWidthValue == null) {
			jTextFieldIntervalWidthValue = new JTextField();
			jTextFieldIntervalWidthValue.setPreferredSize(new Dimension(100, 26));
			jTextFieldIntervalWidthValue.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextFieldIntervalWidthValue.addKeyListener(new KeyAdapter4Numbers(false));
			jTextFieldIntervalWidthValue.getDocument().addDocumentListener(this);
		}
		return jTextFieldIntervalWidthValue;
	}
	private JComboBox<TimeUnit> getJComboBoxIntervalWidthUnit() {
		if (jComboBoxIntervalWidthUnit == null) {
			jComboBoxIntervalWidthUnit = new JComboBox<TimeUnit>(new DefaultComboBoxModel<TimeUnit>(new TimeUnitVector()));
			jComboBoxIntervalWidthUnit.setPreferredSize(new Dimension(120, 26));
			jComboBoxIntervalWidthUnit.setFont(new Font("Dialog", Font.PLAIN, 12));
			jComboBoxIntervalWidthUnit.addActionListener(this); 
		}
		return jComboBoxIntervalWidthUnit;
	}
	private JCheckBox getJCheckBoxSnapshotSimulation() {
		if (jCheckBoxSnapshotSimulation == null) {
			jCheckBoxSnapshotSimulation = new JCheckBox("Do Snapshot Simulation ...");
			jCheckBoxSnapshotSimulation.setFont(new Font("Dialog", Font.BOLD, 12));
			jCheckBoxSnapshotSimulation.addActionListener(this);
		}
		return jCheckBoxSnapshotSimulation;
	}
	private JRadioButton getJRadioButtonSnapshotDecentral() {
		if (jRadioButtonSnapshotDecentral == null) {
			jRadioButtonSnapshotDecentral = new JRadioButton("... with decentral decision processes");
			jRadioButtonSnapshotDecentral.setFont(new Font("Dialog", Font.PLAIN, 12));
			jRadioButtonSnapshotDecentral.addActionListener(this);
		}
		return jRadioButtonSnapshotDecentral;
	}
	private JRadioButton getJRadioButtonSnapshotCentral() {
		if (jRadioButtonSnapshotCentral == null) {
			jRadioButtonSnapshotCentral = new JRadioButton("... with central decision processes using class:");
			jRadioButtonSnapshotCentral.setFont(new Font("Dialog", Font.PLAIN, 12));
			jRadioButtonSnapshotCentral.addActionListener(this);
		}
		return jRadioButtonSnapshotCentral;
	}
	
	private JPanel getJPanelSnapshotCentralDecision() {
		if (jPanelSnapshotCentralDecision == null) {
			jPanelSnapshotCentralDecision = new JPanel();

			GridBagLayout gbl_jPanelSnapshotCentralDecision = new GridBagLayout();
			gbl_jPanelSnapshotCentralDecision.columnWidths = new int[]{0, 0, 0};
			gbl_jPanelSnapshotCentralDecision.rowHeights = new int[]{0, 0};
			gbl_jPanelSnapshotCentralDecision.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
			gbl_jPanelSnapshotCentralDecision.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			
			GridBagConstraints gbc_jTextFieldCentralDecisionClass = new GridBagConstraints();
			gbc_jTextFieldCentralDecisionClass.fill = GridBagConstraints.HORIZONTAL;
			gbc_jTextFieldCentralDecisionClass.gridx = 0;
			gbc_jTextFieldCentralDecisionClass.gridy = 0;
			
			GridBagConstraints gbc_jButtonCentralDecisionClass = new GridBagConstraints();
			gbc_jButtonCentralDecisionClass.gridx = 1;
			gbc_jButtonCentralDecisionClass.gridy = 0;
			
			jPanelSnapshotCentralDecision.setLayout(gbl_jPanelSnapshotCentralDecision);
			jPanelSnapshotCentralDecision.add(getJTextFieldCentralDecisionClass(), gbc_jTextFieldCentralDecisionClass);
			jPanelSnapshotCentralDecision.add(getJButtonCentralDecisionClass(), gbc_jButtonCentralDecisionClass);
		}
		return jPanelSnapshotCentralDecision;
	}
	private JTextField getJTextFieldCentralDecisionClass() {
		if (jTextFieldCentralDecisionClass == null) {
			jTextFieldCentralDecisionClass = new JTextField();
			jTextFieldCentralDecisionClass.setHorizontalAlignment(SwingConstants.RIGHT);
			jTextFieldCentralDecisionClass.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextFieldCentralDecisionClass.setEditable(false);
		}
		return jTextFieldCentralDecisionClass;
	}
	private JButton getJButtonCentralDecisionClass() {
		if (jButtonCentralDecisionClass == null) {
			jButtonCentralDecisionClass = new JButton();
			jButtonCentralDecisionClass.setToolTipText("Klasse auswählen");
			jButtonCentralDecisionClass.setPreferredSize(new Dimension(45, 26));
			jButtonCentralDecisionClass.setIcon(GlobalInfo.getInternalImageIcon("Search.png"));
			jButtonCentralDecisionClass.addActionListener(this);
		}
		return jButtonCentralDecisionClass;
	}
	
	
	private JSeparator getJSeparatorAfterTimeModelSelection() {
		if (jSeparatorAfterTimeModelSelection == null) {
			jSeparatorAfterTimeModelSelection = new JSeparator();
		}
		return jSeparatorAfterTimeModelSelection;
	}
	
	
	private JLabel getJLabelDateHandling() {
		if (jLabelDateHandling == null) {
			jLabelDateHandling = new JLabel("Data Handling");
			jLabelDateHandling.setFont(new Font("Dialog", Font.BOLD, 13));
		}
		return jLabelDateHandling;
	}
	private JPanel getJPanelDataHandling() {
		if (jPanelDataHandling == null) {
			jPanelDataHandling = new JPanel();
			
			GridBagLayout gbl_jPanelDataHandling = new GridBagLayout();
			gbl_jPanelDataHandling.columnWidths = new int[]{0, 0, 0};
			gbl_jPanelDataHandling.rowHeights = new int[]{0, 0, 0};
			gbl_jPanelDataHandling.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			gbl_jPanelDataHandling.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			
			GridBagConstraints gbc_jLabelSimulationType = new GridBagConstraints();
			gbc_jLabelSimulationType.gridx = 0;
			gbc_jLabelSimulationType.gridy = 0;
			
			GridBagConstraints gbc_jRadioButtonExecutionBasedOnPowerFlow = new GridBagConstraints();
			gbc_jRadioButtonExecutionBasedOnPowerFlow.insets = new Insets(10, 0, 0, 0);
			gbc_jRadioButtonExecutionBasedOnPowerFlow.gridx = 0;
			gbc_jRadioButtonExecutionBasedOnPowerFlow.gridy = 1;
			
			GridBagConstraints gbc_jRadioButtonExecutionBasedOnSensorData = new GridBagConstraints();
			gbc_jRadioButtonExecutionBasedOnSensorData.insets = new Insets(10, 10, 0, 0);
			gbc_jRadioButtonExecutionBasedOnSensorData.gridx = 1;
			gbc_jRadioButtonExecutionBasedOnSensorData.gridy = 1;

			jPanelDataHandling.setLayout(gbl_jPanelDataHandling);
			jPanelDataHandling.add(getJLabelSimulationType(), gbc_jLabelSimulationType);
			jPanelDataHandling.add(getJRadioButtonExecutionBasedOnPowerFlow(), gbc_jRadioButtonExecutionBasedOnPowerFlow);
			jPanelDataHandling.add(getJRadioButtonExecutionBasedOnSensorData(), gbc_jRadioButtonExecutionBasedOnSensorData);
		}
		return jPanelDataHandling;
	}
	private JLabel getJLabelSimulationType() {
		if (jLabelSimulationType == null) {
			jLabelSimulationType = new JLabel();
			jLabelSimulationType.setText("Execute Simulation based on ...");
			jLabelSimulationType.setFont(new Font("Dialog", Font.BOLD, 12));
			jLabelSimulationType.setEnabled(true);
		}
		return jLabelSimulationType;
	}
	private JRadioButton getJRadioButtonExecutionBasedOnPowerFlow() {
		if (jRadioButtonExecutionBasedOnPowerFlow == null) {
			jRadioButtonExecutionBasedOnPowerFlow = new JRadioButton();
			jRadioButtonExecutionBasedOnPowerFlow.setText("... power flow at graph nodes");
			jRadioButtonExecutionBasedOnPowerFlow.setFont(new Font("Dialog", Font.PLAIN, 12));
			jRadioButtonExecutionBasedOnPowerFlow.addActionListener(this);
		}
		return jRadioButtonExecutionBasedOnPowerFlow;
	}
	private JRadioButton getJRadioButtonExecutionBasedOnSensorData() {
		if (jRadioButtonExecutionBasedOnSensorData == null) {
			jRadioButtonExecutionBasedOnSensorData = new JRadioButton();
			jRadioButtonExecutionBasedOnSensorData.setText("... sensor data");
			jRadioButtonExecutionBasedOnSensorData.setFont(new Font("Dialog", Font.PLAIN, 12));
			jRadioButtonExecutionBasedOnSensorData.addActionListener(this);
		}
		return jRadioButtonExecutionBasedOnSensorData;
	}
	
	
	private ScheduleLengthRestrictionPanel getJPanelScheduleLengthRestriction() {
		if (jPanelScheduleLengthRestriction == null) {
			jPanelScheduleLengthRestriction = new ScheduleLengthRestrictionPanel();
			jPanelScheduleLengthRestriction.addScheduleLengthRestrictionListener(new ScheduleLengthRestrictionListener() {
				@Override
				public void changedScheduleLengthRestriction(ScheduleLengthRestriction updatedScheduleLengthRestriction) {
					if (actOnActionsOrDocumentChanges==true) loadFormToDataModel();
				}
			});
		}
		return jPanelScheduleLengthRestriction;
	}
	
	
	private JSeparator getJSeparatorAfterDataHandling() {
		if (jSeparatorAfterDataHandling == null) {
			jSeparatorAfterDataHandling = new JSeparator();
		}
		return jSeparatorAfterDataHandling;
	}
	
	
	private JLabel getJLabelHeaderRuntimeDataSettings() {
		if (jLabelHeaderRuntimeDataSettings == null) {
			jLabelHeaderRuntimeDataSettings = new JLabel("Runtime Data");
			jLabelHeaderRuntimeDataSettings.setFont(new Font("Dialog", Font.BOLD, 13));
		}
		return jLabelHeaderRuntimeDataSettings;
	}
	private JPanel getJPanelRuntimeDataSettings() {
		if (jPanelRuntimeDataSettings == null) {
			jPanelRuntimeDataSettings = new JPanel();
			GridBagLayout gbl_jPanelRuntimeDataSettings = new GridBagLayout();
			gbl_jPanelRuntimeDataSettings.columnWidths = new int[]{0, 0};
			gbl_jPanelRuntimeDataSettings.rowHeights = new int[]{0, 0, 0, 0, 0};
			gbl_jPanelRuntimeDataSettings.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_jPanelRuntimeDataSettings.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			jPanelRuntimeDataSettings.setLayout(gbl_jPanelRuntimeDataSettings);
			GridBagConstraints gbc_jCheckboxStoreToDatabase = new GridBagConstraints();
			gbc_jCheckboxStoreToDatabase.anchor = GridBagConstraints.WEST;
			gbc_jCheckboxStoreToDatabase.gridx = 0;
			gbc_jCheckboxStoreToDatabase.gridy = 0;
			jPanelRuntimeDataSettings.add(getJCheckboxStoreToDatabase(), gbc_jCheckboxStoreToDatabase);
			GridBagConstraints gbc_jCheckboxtoreToRelocatedDatabase = new GridBagConstraints();
			gbc_jCheckboxtoreToRelocatedDatabase.insets = new Insets(10, 0, 0, 0);
			gbc_jCheckboxtoreToRelocatedDatabase.anchor = GridBagConstraints.WEST;
			gbc_jCheckboxtoreToRelocatedDatabase.gridx = 0;
			gbc_jCheckboxtoreToRelocatedDatabase.gridy = 1;
			jPanelRuntimeDataSettings.add(getJCheckboxStoreToDedicatedDatabase(), gbc_jCheckboxtoreToRelocatedDatabase);
			GridBagConstraints gbc_jLabelDatabasePrefix = new GridBagConstraints();
			gbc_jLabelDatabasePrefix.anchor = GridBagConstraints.WEST;
			gbc_jLabelDatabasePrefix.insets = new Insets(5, 22, 0, 0);
			gbc_jLabelDatabasePrefix.gridx = 0;
			gbc_jLabelDatabasePrefix.gridy = 2;
			jPanelRuntimeDataSettings.add(getJLabelDatabasePrefix(), gbc_jLabelDatabasePrefix);
			GridBagConstraints gbc_jTextFieldDatabasePrefix = new GridBagConstraints();
			gbc_jTextFieldDatabasePrefix.anchor = GridBagConstraints.WEST;
			gbc_jTextFieldDatabasePrefix.insets = new Insets(0, 20, 0, 0);
			gbc_jTextFieldDatabasePrefix.gridx = 0;
			gbc_jTextFieldDatabasePrefix.gridy = 3;
			jPanelRuntimeDataSettings.add(getJTextFieldDatabasePrefix(), gbc_jTextFieldDatabasePrefix);

		}
		return jPanelRuntimeDataSettings;
	}
	private JCheckBox getJCheckboxStoreToDatabase() {
		if (jCheckboxStoreToDatabase == null) {
			jCheckboxStoreToDatabase = new JCheckBox("Store runtime data to database");
			jCheckboxStoreToDatabase.setFont(new Font("Dialog", Font.BOLD, 12));
			jCheckboxStoreToDatabase.addActionListener(this);
		}
		return jCheckboxStoreToDatabase;
	}
	private JCheckBox getJCheckboxStoreToDedicatedDatabase() {
		if (jCheckboxStoreToDedicatedDatabase == null) {
			jCheckboxStoreToDedicatedDatabase = new JCheckBox("Store runtime data of selected database connections to a dedicated database");
			jCheckboxStoreToDedicatedDatabase.setFont(new Font("Dialog", Font.BOLD, 12));
			jCheckboxStoreToDedicatedDatabase.setVerticalAlignment(SwingConstants.TOP);
			jCheckboxStoreToDedicatedDatabase.addActionListener(this);
		}
		return jCheckboxStoreToDedicatedDatabase;
	}
	private JLabel getJLabelDatabasePrefix() {
		if (jLabelDatabasePrefix == null) {
			jLabelDatabasePrefix = new JLabel("Prefix for the dedicated database (will automatically be extended)");
			jLabelDatabasePrefix.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelDatabasePrefix;
	}
	private JTextField getJTextFieldDatabasePrefix() {
		if (jTextFieldDatabasePrefix == null) {
			jTextFieldDatabasePrefix = new JTextField();
			jTextFieldDatabasePrefix.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextFieldDatabasePrefix.setPreferredSize(new Dimension(400, 26));
			jTextFieldDatabasePrefix.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent de) {
					this.updateHyGridDataModel();
				}
				@Override
				public void insertUpdate(DocumentEvent de) {
					this.updateHyGridDataModel();
				}
				@Override
				public void changedUpdate(DocumentEvent de) {
					this.updateHyGridDataModel();
				}
				private void updateHyGridDataModel() {
					if (HyGridSettingsTab.this.actOnActionsOrDocumentChanges==true) {
						HyGridSettingsTab.this.loadFormToDataModel();	
					}
				}
			});
		}
		return jTextFieldDatabasePrefix;
	}
	
	private DatabaseSelectionPanel getDatabaseSelectionPanel() {
		if (databaseSelectionPanel == null) {
			databaseSelectionPanel = new DatabaseSelectionPanel();
			databaseSelectionPanel.setPreferredSize(new Dimension(350, 150));
			databaseSelectionPanel.addActionListener(this);
		}
		return databaseSelectionPanel;
	}
	
	private JSeparator getJSeparatorAfterRuntimeDataHandling() {
		if (jSeparatorAfterRuntimeDataHandling == null) {
			jSeparatorAfterRuntimeDataHandling = new JSeparator();
		}
		return jSeparatorAfterRuntimeDataHandling;
	}
	
	
	private JLabel getJLabelHeaderVisualizationSettings() {
		if (jLabelHeaderVisualizationSettings == null) {
			jLabelHeaderVisualizationSettings = new JLabel("Visualization Settings");
			jLabelHeaderVisualizationSettings.setFont(new Font("Dialog", Font.BOLD, 13));
		}
		return jLabelHeaderVisualizationSettings;
	}
	private JPanel getJPanelVisualizationSettings() {
		if (jPanelVisualizationSettings == null) {
			jPanelVisualizationSettings = new JPanel();
			
			GridBagLayout gbl_jPanelVisualizationSettings = new GridBagLayout();
			gbl_jPanelVisualizationSettings.columnWidths = new int[]{0, 0, 0, 0};
			gbl_jPanelVisualizationSettings.rowHeights = new int[]{0, 0, 0, 200, 0};
			gbl_jPanelVisualizationSettings.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
			gbl_jPanelVisualizationSettings.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			
			GridBagConstraints gbc_jLabelHeaderVisualization = new GridBagConstraints();
			gbc_jLabelHeaderVisualization.anchor = GridBagConstraints.WEST;
			gbc_jLabelHeaderVisualization.gridx = 0;
			gbc_jLabelHeaderVisualization.gridy = 0;

			GridBagConstraints gbc_jRadioButtonDisplayEnabled = new GridBagConstraints();
			gbc_jRadioButtonDisplayEnabled.anchor = GridBagConstraints.WEST;
			gbc_jRadioButtonDisplayEnabled.gridx = 0;
			gbc_jRadioButtonDisplayEnabled.gridy = 1;
			
			GridBagConstraints gbc_jRadioButtonDisplayDisable = new GridBagConstraints();
			gbc_jRadioButtonDisplayDisable.gridx = 1;
			gbc_jRadioButtonDisplayDisable.gridy = 1;
			
			GridBagConstraints gbc_jLabelHeaderColorAtRuntime = new GridBagConstraints();
			gbc_jLabelHeaderColorAtRuntime.anchor = GridBagConstraints.WEST;
			gbc_jLabelHeaderColorAtRuntime.insets = new Insets(10, 0, 0, 0);
			gbc_jLabelHeaderColorAtRuntime.gridx = 0;
			gbc_jLabelHeaderColorAtRuntime.gridy = 2;
			
			GridBagConstraints gbc_jTabbedPaneColorSettings = new GridBagConstraints();
			gbc_jTabbedPaneColorSettings.anchor = GridBagConstraints.NORTH;
			gbc_jTabbedPaneColorSettings.insets = new Insets(5, 0, 0, 0);
			gbc_jTabbedPaneColorSettings.gridwidth = 3;
			gbc_jTabbedPaneColorSettings.gridx = 0;
			gbc_jTabbedPaneColorSettings.gridy = 3;
			
			jPanelVisualizationSettings.setLayout(gbl_jPanelVisualizationSettings);
			jPanelVisualizationSettings.add(getJLabelHeaderVisualization(), gbc_jLabelHeaderVisualization);
			jPanelVisualizationSettings.add(getJRadioButtonDisplayEnabled(), gbc_jRadioButtonDisplayEnabled);
			jPanelVisualizationSettings.add(getJRadioButtonDisplayDisable(), gbc_jRadioButtonDisplayDisable);
			jPanelVisualizationSettings.add(getJLabelHeaderColorAtRuntime(), gbc_jLabelHeaderColorAtRuntime);
			jPanelVisualizationSettings.add(getJTabbedPaneColorSettings(), gbc_jTabbedPaneColorSettings);
		}
		return jPanelVisualizationSettings;
	}
	private JLabel getJLabelHeaderVisualization() {
		if (jLabelHeaderVisualization==null) {
			jLabelHeaderVisualization = new JLabel();
			jLabelHeaderVisualization.setText("Runtime Visualization Updates");
			jLabelHeaderVisualization.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelHeaderVisualization;
	}
	private JRadioButton getJRadioButtonDisplayEnabled() {
		if (jRadioButtonDisplayEnabled == null) {
			jRadioButtonDisplayEnabled = new JRadioButton();
			jRadioButtonDisplayEnabled.setFont(new Font("Dialog", Font.PLAIN, 12));
			jRadioButtonDisplayEnabled.setText("Enable");
			jRadioButtonDisplayEnabled.addActionListener(this);
		}
		return jRadioButtonDisplayEnabled;
	}
	private JRadioButton getJRadioButtonDisplayDisable() {
		if (jRadioButtonDisplayDisable == null) {
			jRadioButtonDisplayDisable = new JRadioButton();
			jRadioButtonDisplayDisable.setFont(new Font("Dialog", Font.PLAIN, 12));
			jRadioButtonDisplayDisable.setText("Disable");
			jRadioButtonDisplayDisable.addActionListener(this);
		}
		return jRadioButtonDisplayDisable;
	}
	private JLabel getJLabelHeaderColorAtRuntime() {
		if (jLabelHeaderColorAtRuntime == null) {
			jLabelHeaderColorAtRuntime = new JLabel();
			jLabelHeaderColorAtRuntime.setText("Runtime Color Settings");
			jLabelHeaderColorAtRuntime.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelHeaderColorAtRuntime;
	}
	private JTabbedPane getJTabbedPaneColorSettings() {
		if (jTabbedPaneColorSettings==null) {
			jTabbedPaneColorSettings = new JTabbedPane();
			List<GraphElementLayoutService> layoutServiceList = ServiceFinder.findServices(GraphElementLayoutService.class);
			for (int i = 0; i < layoutServiceList.size(); i++) {
				GraphElementLayoutService layoutService = layoutServiceList.get(i);
				for (String domain : layoutService.getDomainList(this.currProject)) {
					AbstractGraphElementLayoutSettingsPanel layoutSettingsPanel = layoutService.getGraphElementLayoutSettingPanel(this.currProject, domain);
					layoutSettingsPanel.setGraphElementLayoutSettingsToVisualization();
					jTabbedPaneColorSettings.addTab(" " + domain + " ", layoutSettingsPanel);
				}
			}
		}
		return jTabbedPaneColorSettings;
	}
	
	
	/**
	 * Returns a key adapter that just allows numbers.
	 * @return the key adapter4 numbers
	 */
	private KeyAdapter4Numbers getKeyAdapter4LongAndInteger() {
		if (keyListenerLongInteger==null) {
			keyListenerLongInteger = new KeyAdapter4Numbers(false);
		}
		return keyListenerLongInteger;
	}
	/**
	 * Returns a key adapter that just allows numbers.
	 * @return the key adapter4 numbers
	 */
	private KeyAdapter4Numbers getKeyAdapter4Float() {
		if (keyListenerFloat==null) {
			keyListenerFloat = new KeyAdapter4Numbers(true);
		}
		return keyListenerFloat;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable observable, Object updateObject) {
		if (updateObject.equals(Project.CHANGED_UserRuntimeObject)) {
			if (this.isPauseObserver==false) this.loadDataModelToForm();
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
	 */
	@Override
	public void insertUpdate(DocumentEvent de) {
		this.setJTextFieldChanged(de);	
	}
	/* (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
	 */
	@Override
	public void removeUpdate(DocumentEvent de) {
		this.setJTextFieldChanged(de);
	}
	/* (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
	 */
	@Override
	public void changedUpdate(DocumentEvent de) {
		this.setJTextFieldChanged(de);
	}
	
	/**
	 * Reacts on changes inside a JTextField.
	 * @param de the new j text field changed
	 */
	private void setJTextFieldChanged(DocumentEvent de) {
		if (this.actOnActionsOrDocumentChanges==true) {
			this.loadFormToDataModel();	
			if (de.getDocument()==this.getJTextFieldTransLiveBit().getDocument()) {
				this.setTimeExplanation(this.getJTextFieldTransLiveBit(), getJLabelTransLiveBit());
			}
		}
	}
	
	/**
	 * Switches the time model class .
	 * @param timeModelClass the time model class
	 */
	private void switchTimeModel(String timeModelClass) {
		
		TimeModelController tmc = this.currProject.getTimeModelController();
		TimeModelDateBased timeModelOld = null;
		
		if (tmc.getTimeModel() instanceof TimeModelDateBased) {
			// --- Remind source configuration ------------ 
			timeModelOld = (TimeModelDateBased) tmc.getTimeModel();
		}
		
		// --- Set the new time model type ---------------- 
		this.currProject.setTimeModelClass(timeModelClass);
		
		if (timeModelOld!=null) {
			// ---- Set old settings to new time model ----   
			TimeModelDateBased timModelNew = (TimeModelDateBased) tmc.getTimeModel();
			// --- Transfer old settings to the new ---
			timModelNew.setTimeStart(timeModelOld.getTimeStart());
			timModelNew.setTimeStop(timeModelOld.getTimeStop());
			timModelNew.setZoneId(timeModelOld.getZoneId());
			timModelNew.setTimeFormat(timeModelOld.getTimeFormat());
			// --- Save the new time model settings -------
			tmc.saveTimeModelToSimulationSetup();
		}
	}
	
	/**
	 * Saves the simulation interval length.
	 * @param abstractDM the abstract data model
	 */
	private void saveSimulationIntervalLength(HyGridAbstractEnvironmentModel abstractDM) {
		
		// --- Get the current index of the unit list ---------------
		ComboBoxModel<TimeUnit> cbm = this.getJComboBoxIntervalWidthUnit().getModel();
		TimeUnit timeUnit = (TimeUnit) cbm.getSelectedItem();
		int indexSelected = 0;
		for (int i = 0; i < cbm.getSize(); i++) {
			if (cbm.getElementAt(i)==timeUnit) {
				indexSelected = i;
				break;
			}
		}
		
		// --- Getting step width -----------------------------------
		String stepString = this.getJTextFieldIntervalWidthValue().getText().trim();
		long step;
		long stepInUnit;
		if (stepString==null || stepString.equals("")) {
			step = Long.valueOf(0);
		} else {
			stepInUnit = Long.parseLong(stepString);
			step = stepInUnit * timeUnit.getFactorToMilliseconds();
		}
		
		abstractDM.setSimulationIntervalLength(step);
		abstractDM.setSimulationIntervalUnitIndex(indexSelected);
	}
	
	/**
	 * Load simulation interval length.
	 * @param abstractDM the abstract network model
	 */
	private void loadSimulationIntervalLength(HyGridAbstractEnvironmentModel abstractDM) {

		long step = abstractDM.getSimulationIntervalLength();
		int unitSelection = abstractDM.getSimulationIntervalUnitIndex();
		TimeUnit timeUnit = (TimeUnit) this.getJComboBoxIntervalWidthUnit().getModel().getElementAt(unitSelection);
		Long stepInUnit = step / timeUnit.getFactorToMilliseconds();
		
		this.getJTextFieldIntervalWidthValue().setText(stepInUnit.toString());
		this.getJComboBoxIntervalWidthUnit().setSelectedIndex(unitSelection);
	}
	
	/**
	 * Saves the simulation interval length.
	 * @param abstractDM the abstract data model
	 */
	private void saveNetworkCalculationIntervalLength(HyGridAbstractEnvironmentModel abstractDM) {
		
		// --- Get the current index of the unit list ---------------
		ComboBoxModel<TimeUnit> cbm = this.getJComboBoxNetCalcIntervalWidthUnit().getModel();
		TimeUnit timeUnit = (TimeUnit) cbm.getSelectedItem();
		int indexSelected = 0;
		for (int i = 0; i < cbm.getSize(); i++) {
			if (cbm.getElementAt(i)==timeUnit) {
				indexSelected = i;
				break;
			}
		}
		
		// --- Getting step width -----------------------------------
		String stepString = this.getJTextFieldNetCalcInterval().getText().trim();
		long step;
		long stepInUnit;
		if (stepString==null || stepString.equals("")) {
			step = Long.valueOf(0);
		} else {
			stepInUnit = Long.parseLong(stepString);
			step = stepInUnit * timeUnit.getFactorToMilliseconds();
		}
		
		abstractDM.setNetworkCalculationIntervalLength(step);
		abstractDM.setNetworkCalculationIntervalUnitIndex(indexSelected);
	}
	/**
	 * Load network calculation interval length.
	 * @param abstractDM the abstract network model
	 */
	private void loadNetworkCalculationIntervalLength(HyGridAbstractEnvironmentModel abstractDM) {

		long step = abstractDM.getNetworkCalculationIntervalLength();
		int unitSelection = abstractDM.getNetworkCalculationIntervalUnitIndex();
		TimeUnit timeUnit = (TimeUnit) this.getJComboBoxNetCalcIntervalWidthUnit().getModel().getElementAt(unitSelection);
		Long stepInUnit = step / timeUnit.getFactorToMilliseconds();
		
		this.getJTextFieldNetCalcInterval().setText(stepInUnit.toString());
		this.getJComboBoxNetCalcIntervalWidthUnit().setSelectedIndex(unitSelection);
	}

	
	/**
	 * Enables the controls according to the current settings.
	 */
	private void enableControls() {
		
		boolean isTimeModelDiscrete = this.getJRadioButtonTimeModelDiscrete().isSelected();
		boolean isStateTransmissionReduced = this.getJRadioButtonPowerTransmissionReduced().isSelected();
		boolean isSnapshotSimulation = this.getJCheckBoxSnapshotSimulation().isSelected();
		boolean isSnapshotSimulationCentralDecision = this.getJRadioButtonSnapshotCentral().isSelected();
		
		this.getJLabelInterval().setEnabled(isTimeModelDiscrete);
		this.getJTextFieldIntervalWidthValue().setEnabled(isTimeModelDiscrete);
		this.getJComboBoxIntervalWidthUnit().setEnabled(isTimeModelDiscrete);
		
		this.getJCheckBoxSnapshotSimulation().setEnabled(isTimeModelDiscrete);
		if (isTimeModelDiscrete==false) this.getJCheckBoxSnapshotSimulation().setSelected(false);
		this.getJRadioButtonSnapshotDecentral().setEnabled(isTimeModelDiscrete & isSnapshotSimulation);
		this.getJRadioButtonSnapshotCentral().setEnabled(isTimeModelDiscrete & isSnapshotSimulation);
		this.getJTextFieldCentralDecisionClass().setEnabled(isTimeModelDiscrete & isSnapshotSimulation & isSnapshotSimulationCentralDecision);
		this.getJButtonCentralDecisionClass().setEnabled(isTimeModelDiscrete & isSnapshotSimulation & isSnapshotSimulationCentralDecision);
		
		this.getJLabelNetCalcInterval().setEnabled(! isTimeModelDiscrete);
		this.getJTextFieldNetCalcInterval().setEnabled(! isTimeModelDiscrete);
		this.getJComboBoxNetCalcIntervalWidthUnit().setEnabled(! isTimeModelDiscrete);
		
		this.getJRadioButtonPowerTransmissionAsDefined().setEnabled(! isTimeModelDiscrete);
		this.getJRadioButtonPowerTransmissionReduced().setEnabled(! isTimeModelDiscrete);
		this.getJRadioButtonTransByAbsolute().setEnabled(! isTimeModelDiscrete & isStateTransmissionReduced);
		this.getJRadioButtonTransByPercent().setEnabled(! isTimeModelDiscrete & isStateTransmissionReduced);
		
		this.getJLabelLabelTransWatt().setEnabled(! isTimeModelDiscrete & isStateTransmissionReduced);
		this.getJTextFieldTransWatt().setEnabled(! isTimeModelDiscrete & isStateTransmissionReduced);
		
		this.getJLabelTransPercent().setEnabled(! isTimeModelDiscrete & isStateTransmissionReduced);
		this.getJTextFieldTransPercent().setEnabled(! isTimeModelDiscrete & isStateTransmissionReduced);
		
		this.getJLabelTransHeaderLiveBit().setEnabled(! isTimeModelDiscrete & isStateTransmissionReduced);
		this.getJTextFieldTransLiveBit().setEnabled(! isTimeModelDiscrete & isStateTransmissionReduced);
		this.getJLabelTransLiveBit().setEnabled(! isTimeModelDiscrete & isStateTransmissionReduced);
		
		this.enableDatabaseControls();
	}
	
	/**
	 * Enable / disables the database setting controls.
	 */
	private void enableDatabaseControls() {
		
		boolean saveToDatabase = this.getJCheckboxStoreToDatabase().isSelected();
		boolean saveToDedicatedDatabase = this.getJCheckboxStoreToDedicatedDatabase().isSelected();
		
		this.getJCheckboxStoreToDedicatedDatabase().setEnabled(saveToDatabase);

		this.getJLabelDatabasePrefix().setEnabled(saveToDatabase & saveToDedicatedDatabase);
		this.getJTextFieldDatabasePrefix().setEnabled(saveToDatabase & saveToDedicatedDatabase);
		this.getDatabaseSelectionPanel().setEnabled(saveToDatabase & saveToDedicatedDatabase);
	}
	
	
	/**
	 * Sets the central decision class for snapshot simulations.
	 */
	private void setSnapshotCentralDecisionClass() {
		
		// ----------------------------------------------------------
		// --- Try to get base class for central decisions ----------
		// ----------------------------------------------------------
		// --- This UNUSUAL way was select to overcome --------------
		// --- cyclic bundle dependencies ---------------------------
		// ----------------------------------------------------------		
		Class<?> search4Class = null;
		try {
			search4Class = BaseClassLoadServiceUtility.forName("de.enflexit.ea.core.simulation.decisionControl.AbstractCentralDecisionProcess");
		} catch (ClassNotFoundException | NoClassDefFoundError cnEx) {
			cnEx.printStackTrace();
		}
		
		// --- Will hopefully never happen! -------------------------
		if (search4Class==null) {
			String title = "Critical internal error!";
			String msg = "[" + this.getClass().getSimpleName() + "] The class reference for the AbstractCentralDecisionProcess is incorrect!\nPlease, inform the developers about this error!";
			System.err.println(msg);
			JOptionPane.showMessageDialog(Application.getMainWindow(), msg, title, JOptionPane.ERROR_MESSAGE, null);
			return;
		}
		
		// --- Open the ClassSelectionDialog ----------------------
		String   search4CurrentValue = this.getJTextFieldCentralDecisionClass().getText();
		String   search4DefaultValue = null;
		String   search4Description = "Select the central decision process class to apply.";
		
		ClassSelectionDialog cs = new ClassSelectionDialog(Application.getMainWindow(), search4Class, search4CurrentValue, search4DefaultValue, search4Description, true);
		cs.setVisible(true);
		// --- act in the dialog ... ------------------------------
		if (cs.isCanceled()==true) return;
		
		// --------------------------------------------------------
		// --- Class was selected. Proceed it ---------------------
		String classSelected = cs.getClassSelected();
		if (classSelected.isEmpty()==true) classSelected =null;

		cs.dispose();
		cs = null;
		// --------------------------------------------------------
		
		this.getJTextFieldCentralDecisionClass().setText(classSelected);
		this.getJTextFieldCentralDecisionClass().setToolTipText(classSelected);
		this.loadFormToDataModel();
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		
		if (this.actOnActionsOrDocumentChanges==true) {
			
			if (ae.getSource()==this.getJRadioButtonTimeModelDiscrete()) {
				this.switchTimeModel(TimeModelDiscrete.class.getName());
				this.enableControls();
				this.loadFormToDataModel();
			} else if (ae.getSource()==this.getJRadioButtonTimeModelContinuous()) {
				this.switchTimeModel(TimeModelContinuous.class.getName());
				this.enableControls();
				this.loadFormToDataModel();
				
			} else if (ae.getSource()==this.getJRadioButtonPowerTransmissionAsDefined()) {
				this.enableControls();
				this.loadFormToDataModel();
			} else if (ae.getSource()==this.getJRadioButtonPowerTransmissionReduced()) {
				this.enableControls();
				this.loadFormToDataModel();
				
			} else if (ae.getSource()==this.getJCheckBoxSnapshotSimulation()) {
				this.loadFormToDataModel();
				this.enableControls();
			} else if (ae.getSource()==this.getJRadioButtonSnapshotDecentral()) {
				this.loadFormToDataModel();
				this.enableControls();
			} else if (ae.getSource()==this.getJRadioButtonSnapshotCentral()) {
				this.loadFormToDataModel();
				this.enableControls();
			} else if (ae.getSource()==this.getJButtonCentralDecisionClass()) {
				this.setSnapshotCentralDecisionClass();
				
			} else if (ae.getSource()==this.getJRadioButtonExecutionBasedOnPowerFlow()) {
				this.loadFormToDataModel();
			} else if (ae.getSource()==this.getJRadioButtonExecutionBasedOnSensorData()) {
				this.loadFormToDataModel();
				
			} else if (ae.getSource()==this.getJCheckboxStoreToDatabase()) {
				this.enableDatabaseControls();
				this.loadFormToDataModel();
			} else if (ae.getSource()==this.getJCheckboxStoreToDedicatedDatabase()) {
				this.enableDatabaseControls();
				this.loadFormToDataModel();
			} else if (ae.getSource()==this.getDatabaseSelectionPanel()) {
				this.loadFormToDataModel();
				
			} else if (ae.getSource()==this.getJComboBoxIntervalWidthUnit()) {
				this.loadFormToDataModel();
			} else if (ae.getSource()==this.getJComboBoxNetCalcIntervalWidthUnit()) {
				this.loadFormToDataModel();
				
			} else if (ae.getSource()==this.getJRadioButtonTransByAbsolute()) {
				this.loadFormToDataModel();
			} else if (ae.getSource()==this.getJRadioButtonTransByPercent()) {
				this.loadFormToDataModel();
			
			} else if (ae.getSource()==this.getJRadioButtonDisplayEnabled()) {
				this.loadFormToDataModel();
			} else if (ae.getSource()==this.getJRadioButtonDisplayDisable()) {
				this.loadFormToDataModel();
				
			} else if (ae.getSource()==this.getJTabbedPaneColorSettings()) {
				this.loadFormToDataModel();
			}
			
		}
	}

}  
