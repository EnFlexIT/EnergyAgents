package de.enflexit.ea.core.awbIntegration.plugin.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import agentgui.core.application.Language;
import agentgui.core.gui.projectwindow.simsetup.TimeModelController;
import agentgui.core.project.Project;
import agentgui.simulationService.time.TimeModelContinuous;
import agentgui.simulationService.time.TimeModelDateBased;
import agentgui.simulationService.time.TimeModelDiscrete;
import agentgui.simulationService.time.TimeUnit;
import agentgui.simulationService.time.TimeUnitVector;
import de.enflexit.common.ServiceFinder;
import de.enflexit.common.swing.AwbBasicTabbedPaneUI;
import de.enflexit.common.swing.KeyAdapter4Numbers;
import de.enflexit.ea.core.awbIntegration.plugin.AWBIntegrationPlugIn;
import de.enflexit.ea.core.dataModel.absEnvModel.DisplayUpdateConfiguration;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import de.enflexit.ea.core.dataModel.absEnvModel.DisplayUpdateConfiguration.UpdateMechanism;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.ExecutionDataBase;
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

	private JPanel jPanelMain;
	
	private JLabel jLabelHeaderPowerTransmission;
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
	
	private JLabel jLabelScheduleLenghtRestriction;
	private ScheduleLengthRestrictionPanel jPanelScheduleLengthRestriction;
	private JLabel jLabelSimulationType;
	private JRadioButton jRadioButtonExecutionBasedOnPowerFlow;
	private JRadioButton jRadioButtonExecutionBasedOnSensorData;
	
	private JTabbedPane graphElementLayoutSettingsConfigurationPanels;

	
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
		HyGridAbstractEnvironmentModel abstractDM = this.getHyGridAbstractEnvironmentModel();
		
		// --- Set simulation interval length ---
		this.loadSimulationIntervalLength(abstractDM);
		this.loadNetworkCalculationIntervalLength(abstractDM);
		
		// --- Set the simulation data base -----
		switch (abstractDM.getExecutionDataBase()) {
		case NodePowerFlows:
			this.getJRadioButtonExecutionBasedOnPowerFlow().setSelected(true);
			this.getJRadioButtonExecutionBasedOnSensorData().setSelected(false);
			break;
		case SensorData:
			this.getJRadioButtonExecutionBasedOnPowerFlow().setSelected(false);
			this.getJRadioButtonExecutionBasedOnSensorData().setSelected(true);
			break;
		}
		
		// --- Set energy transmission ----------
		ScheduleTransformerKeyValueConfiguration etc = abstractDM.getEnergyTransmissionConfiguration();
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
		DisplayUpdateConfiguration duc = abstractDM.getDisplayUpdateConfiguration();
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

		this.setTimeExplanation(this.getJTextFieldTransLiveBit(), jLabelTransLiveBit);
		
		// --- Set ScheduleLenghtDescription ----
		this.getJPanelScheduleLengthRestriction().setScheduleLengthRestriction(abstractDM.getScheduleLengthRestriction());
		
		this.actOnActionsOrDocumentChanges = true;
		this.enableControls();
	}
	
	/**
	 * Load form data to the data model.
	 */
	private void loadFormToDataModel() {
		
		HyGridAbstractEnvironmentModel abstractDM = this.getHyGridAbstractEnvironmentModel();
		
		// --- Save Simulation Interval length ------------
		this.saveSimulationIntervalLength(abstractDM);
		this.saveNetworkCalculationIntervalLength(abstractDM);
		
		// --- Save the simulation execution base --------
		if (this.getJRadioButtonExecutionBasedOnPowerFlow().isSelected()) {
			abstractDM.setExecutionDataBase(ExecutionDataBase.NodePowerFlows);
		} else if (this.getJRadioButtonExecutionBasedOnSensorData().isSelected()) {
			abstractDM.setExecutionDataBase(ExecutionDataBase.SensorData);
		}
		
		// --- Save energy transmission -------------------
		ScheduleTransformerKeyValueConfiguration etc = abstractDM.getEnergyTransmissionConfiguration();
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
		DisplayUpdateConfiguration duc = abstractDM.getDisplayUpdateConfiguration();
		if (this.getJRadioButtonDisplayEnabled().isSelected()) {
			duc.setUpdateMechanism(UpdateMechanism.EnableUpdates);	
		} else if (this.getJRadioButtonDisplayDisable().isSelected()) {
			duc.setUpdateMechanism(UpdateMechanism.DisableUpdates);
		}
		
		// --- Set ScheduleLenghtDescription ----
		abstractDM.setScheduleLengthRestriction(this.getJPanelScheduleLengthRestriction().getScheduleLengthRestriction());
		
		// --- Set the current project to be unsaved ------
		this.currProject.setUnsaved(true);
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
		
		jLabelTimeModelSelection.setText("Simulation Time Model");
		jRadioButtonTimeModelDiscrete.setText(Language.translate(jRadioButtonTimeModelDiscrete.getText(), Language.EN));
		jRadioButtonTimeModelContinuous.setText(Language.translate(jRadioButtonTimeModelContinuous.getText(), Language.EN));
		
		jLabelInterval.setText(Language.translate(jLabelInterval.getText(), Language.EN));
		jLabelNetCalcInterval.setText(Language.translate(jLabelNetCalcInterval.getText(), Language.EN));
		
		jLabelHeaderPowerTransmission.setText(Language.translate(jLabelHeaderPowerTransmission.getText(), Language.EN));
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
		this.setSize(650, 796);
		this.setViewportView(this.getJPanelMain());
		this.setBorder(new EmptyBorder(0, 0, 0, 0));
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
			gridBagLayout.columnWidths = new int[]{133, 0, 250, 0};
			gridBagLayout.rowHeights = new int[]{16, 25, 0, 0, 0, 0, 0, 16, 26, 26, 26, 16, 26, 0, 0, 0, 0};
			gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			jPanelMain.setLayout(gridBagLayout);
			jPanelMain.setSize(550, 893);
			
			GridBagConstraints gbc_jLabelTimeModelSelection = new GridBagConstraints();
			gbc_jLabelTimeModelSelection.gridwidth = 3;
			gbc_jLabelTimeModelSelection.anchor = GridBagConstraints.WEST;
			gbc_jLabelTimeModelSelection.insets = new Insets(10, 10, 2, 0);
			gbc_jLabelTimeModelSelection.gridx = 0;
			gbc_jLabelTimeModelSelection.gridy = 0;
			jPanelMain.add(getJLabelTimeModelSelection(), gbc_jLabelTimeModelSelection);
			
			GridBagConstraints gbc_jRadioButtonTimeModelDiscrete = new GridBagConstraints();
			gbc_jRadioButtonTimeModelDiscrete.gridwidth = 2;
			gbc_jRadioButtonTimeModelDiscrete.anchor = GridBagConstraints.WEST;
			gbc_jRadioButtonTimeModelDiscrete.insets = new Insets(0, 10, 5, 5);
			gbc_jRadioButtonTimeModelDiscrete.gridx = 0;
			gbc_jRadioButtonTimeModelDiscrete.gridy = 1;
			jPanelMain.add(getJRadioButtonTimeModelDiscrete(), gbc_jRadioButtonTimeModelDiscrete);
			GridBagConstraints gbc_jLabelInterval = new GridBagConstraints();
			gbc_jLabelInterval.anchor = GridBagConstraints.WEST;
			gbc_jLabelInterval.insets = new Insets(0, 32, 5, 5);
			gbc_jLabelInterval.gridx = 0;
			gbc_jLabelInterval.gridy = 2;
			jPanelMain.add(getJLabelInterval(), gbc_jLabelInterval);
			
			GridBagConstraints gbc_jTextFieldWidthValue = new GridBagConstraints();
			gbc_jTextFieldWidthValue.anchor = GridBagConstraints.WEST;
			gbc_jTextFieldWidthValue.insets = new Insets(0, 0, 5, 5);
			gbc_jTextFieldWidthValue.gridx = 1;
			gbc_jTextFieldWidthValue.gridy = 2;
			jPanelMain.add(getJTextFieldIntervalWidthValue(), gbc_jTextFieldWidthValue);
			GridBagConstraints gbc_jComboBoxWidthUnit = new GridBagConstraints();
			gbc_jComboBoxWidthUnit.anchor = GridBagConstraints.WEST;
			gbc_jComboBoxWidthUnit.insets = new Insets(0, 0, 5, 0);
			gbc_jComboBoxWidthUnit.gridx = 2;
			gbc_jComboBoxWidthUnit.gridy = 2;
			jPanelMain.add(getJComboBoxIntervalWidthUnit(), gbc_jComboBoxWidthUnit);
			GridBagConstraints gbc_jRadioButtonTimeModelContinous = new GridBagConstraints();
			gbc_jRadioButtonTimeModelContinous.gridwidth = 2;
			gbc_jRadioButtonTimeModelContinous.anchor = GridBagConstraints.WEST;
			gbc_jRadioButtonTimeModelContinous.insets = new Insets(5, 10, 5, 5);
			gbc_jRadioButtonTimeModelContinous.gridx = 0;
			gbc_jRadioButtonTimeModelContinous.gridy = 3;
			jPanelMain.add(getJRadioButtonTimeModelContinuous(), gbc_jRadioButtonTimeModelContinous);
			GridBagConstraints gbc_jLabelNetCalcInterval = new GridBagConstraints();
			gbc_jLabelNetCalcInterval.anchor = GridBagConstraints.WEST;
			gbc_jLabelNetCalcInterval.insets = new Insets(0, 32, 5, 5);
			gbc_jLabelNetCalcInterval.gridx = 0;
			gbc_jLabelNetCalcInterval.gridy = 4;
			jPanelMain.add(getJLabelNetCalcInterval(), gbc_jLabelNetCalcInterval);
			GridBagConstraints gbc_jTextFieldNetCalcInterval = new GridBagConstraints();
			gbc_jTextFieldNetCalcInterval.anchor = GridBagConstraints.WEST;
			gbc_jTextFieldNetCalcInterval.insets = new Insets(0, 0, 5, 5);
			gbc_jTextFieldNetCalcInterval.gridx = 1;
			gbc_jTextFieldNetCalcInterval.gridy = 4;
			jPanelMain.add(getJTextFieldNetCalcInterval(), gbc_jTextFieldNetCalcInterval);
			GridBagConstraints gbc_jComboBoxNetCalcIntervalWidthUnit = new GridBagConstraints();
			gbc_jComboBoxNetCalcIntervalWidthUnit.anchor = GridBagConstraints.WEST;
			gbc_jComboBoxNetCalcIntervalWidthUnit.insets = new Insets(0, 0, 5, 0);
			gbc_jComboBoxNetCalcIntervalWidthUnit.gridx = 2;
			gbc_jComboBoxNetCalcIntervalWidthUnit.gridy = 4;
			jPanelMain.add(getJComboBoxNetCalcIntervalWidthUnit(), gbc_jComboBoxNetCalcIntervalWidthUnit);

			GridBagConstraints gbc_jLabelSimulationType = new GridBagConstraints();
			gbc_jLabelSimulationType.gridwidth = 2;
			gbc_jLabelSimulationType.insets = new Insets(10, 10, 2, 0);
			gbc_jLabelSimulationType.anchor = GridBagConstraints.WEST;
			gbc_jLabelSimulationType.gridx = 0;
			gbc_jLabelSimulationType.gridy = 5;
			jPanelMain.add(getJLabelSimulationType(), gbc_jLabelSimulationType);
			GridBagConstraints gbc_jRadioButtonSimulationBasedOnPowerFlow = new GridBagConstraints();
			gbc_jRadioButtonSimulationBasedOnPowerFlow.anchor = GridBagConstraints.WEST;
			gbc_jRadioButtonSimulationBasedOnPowerFlow.insets = new Insets(0, 10, 0, 0);
			gbc_jRadioButtonSimulationBasedOnPowerFlow.gridx = 0;
			gbc_jRadioButtonSimulationBasedOnPowerFlow.gridy = 6;
			jPanelMain.add(getJRadioButtonExecutionBasedOnPowerFlow(), gbc_jRadioButtonSimulationBasedOnPowerFlow);
			GridBagConstraints gbc_jRadioButtonSimulationBasedOnSensorData = new GridBagConstraints();
			gbc_jRadioButtonSimulationBasedOnSensorData.gridwidth = 2;
			gbc_jRadioButtonSimulationBasedOnSensorData.insets = new Insets(0, 10, 0, 0);
			gbc_jRadioButtonSimulationBasedOnSensorData.anchor = GridBagConstraints.WEST;
			gbc_jRadioButtonSimulationBasedOnSensorData.gridx = 1;
			gbc_jRadioButtonSimulationBasedOnSensorData.gridy = 6;
			jPanelMain.add(getJRadioButtonExecutionBasedOnSensorData(), gbc_jRadioButtonSimulationBasedOnSensorData);

			jLabelHeaderPowerTransmission = new JLabel();
			jLabelHeaderPowerTransmission.setText("Transmission of power signals");
			jLabelHeaderPowerTransmission.setFont(new Font("Dialog", Font.BOLD, 13));
			GridBagConstraints gbc_jLabelHeaderPowerTransmission = new GridBagConstraints();
			gbc_jLabelHeaderPowerTransmission.gridwidth = 3;
			gbc_jLabelHeaderPowerTransmission.anchor = GridBagConstraints.WEST;
			gbc_jLabelHeaderPowerTransmission.insets = new Insets(10, 10, 2, 0);
			gbc_jLabelHeaderPowerTransmission.gridx = 0;
			gbc_jLabelHeaderPowerTransmission.gridy = 7;
			jPanelMain.add(jLabelHeaderPowerTransmission, gbc_jLabelHeaderPowerTransmission);
			
			GridBagConstraints gbc_jRadioButtonTransByAbsolute = new GridBagConstraints();
			gbc_jRadioButtonTransByAbsolute.anchor = GridBagConstraints.WEST;
			gbc_jRadioButtonTransByAbsolute.insets = new Insets(0, 10, 5, 5);
			gbc_jRadioButtonTransByAbsolute.gridx = 0;
			gbc_jRadioButtonTransByAbsolute.gridy = 8;
			jPanelMain.add(getJRadioButtonTransByAbsolute(), gbc_jRadioButtonTransByAbsolute);
			
			GridBagConstraints gbc_jTextFieldTransWatt = new GridBagConstraints();
			gbc_jTextFieldTransWatt.anchor = GridBagConstraints.WEST;
			gbc_jTextFieldTransWatt.insets = new Insets(0, 0, 5, 5);
			gbc_jTextFieldTransWatt.gridx = 1;
			gbc_jTextFieldTransWatt.gridy = 8;
			jPanelMain.add(getJTextFieldTransWatt(), gbc_jTextFieldTransWatt);
			
			jLabelTransWatt = new JLabel();
			jLabelTransWatt.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabelTransWatt.setText(" Watt");
			GridBagConstraints gbc_jLabelTransWatt = new GridBagConstraints();
			gbc_jLabelTransWatt.anchor = GridBagConstraints.WEST;
			gbc_jLabelTransWatt.insets = new Insets(0, 0, 5, 0);
			gbc_jLabelTransWatt.gridx = 2;
			gbc_jLabelTransWatt.gridy = 8;
			jPanelMain.add(jLabelTransWatt, gbc_jLabelTransWatt);
			
			GridBagConstraints gbc_jRadioButtonTransByPercent = new GridBagConstraints();
			gbc_jRadioButtonTransByPercent.anchor = GridBagConstraints.WEST;
			gbc_jRadioButtonTransByPercent.insets = new Insets(0, 10, 5, 5);
			gbc_jRadioButtonTransByPercent.gridx = 0;
			gbc_jRadioButtonTransByPercent.gridy = 9;
			jPanelMain.add(getJRadioButtonTransByPercent(), gbc_jRadioButtonTransByPercent);
			
			GridBagConstraints gbc_jTextFieldTransPercent = new GridBagConstraints();
			gbc_jTextFieldTransPercent.anchor = GridBagConstraints.WEST;
			gbc_jTextFieldTransPercent.insets = new Insets(0, 0, 5, 5);
			gbc_jTextFieldTransPercent.gridx = 1;
			gbc_jTextFieldTransPercent.gridy = 9;
			jPanelMain.add(getJTextFieldTransPercent(), gbc_jTextFieldTransPercent);
			
			jLabelTransPercent = new JLabel();
			jLabelTransPercent.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabelTransPercent.setText(" % ");
			GridBagConstraints gbc_jLabelTransPercent = new GridBagConstraints();
			gbc_jLabelTransPercent.anchor = GridBagConstraints.WEST;
			gbc_jLabelTransPercent.insets = new Insets(0, 0, 5, 0);
			gbc_jLabelTransPercent.gridx = 2;
			gbc_jLabelTransPercent.gridy = 9;
			jPanelMain.add(jLabelTransPercent, gbc_jLabelTransPercent);
			
			jLabelTransHeaderLiveBit = new JLabel();
			jLabelTransHeaderLiveBit.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabelTransHeaderLiveBit.setText("Time after a signal must be sent at least");
			GridBagConstraints gbc_jLabelTransHeaderLiveBit = new GridBagConstraints();
			gbc_jLabelTransHeaderLiveBit.anchor = GridBagConstraints.WEST;
			gbc_jLabelTransHeaderLiveBit.insets = new Insets(0, 10, 5, 5);
			gbc_jLabelTransHeaderLiveBit.gridx = 0;
			gbc_jLabelTransHeaderLiveBit.gridy = 10;
			jPanelMain.add(jLabelTransHeaderLiveBit, gbc_jLabelTransHeaderLiveBit);
			
			GridBagConstraints gbc_jTextFieldTransLiveBit = new GridBagConstraints();
			gbc_jTextFieldTransLiveBit.anchor = GridBagConstraints.WEST;
			gbc_jTextFieldTransLiveBit.insets = new Insets(0, 0, 5, 5);
			gbc_jTextFieldTransLiveBit.gridx = 1;
			gbc_jTextFieldTransLiveBit.gridy = 10;
			jPanelMain.add(getJTextFieldTransLiveBit(), gbc_jTextFieldTransLiveBit);
			
			jLabelTransLiveBit = new JLabel();
			jLabelTransLiveBit.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabelTransLiveBit.setText("Milliseconds");
			GridBagConstraints gbc_jLabelTransLiveBit = new GridBagConstraints();
			gbc_jLabelTransLiveBit.anchor = GridBagConstraints.WEST;
			gbc_jLabelTransLiveBit.insets = new Insets(0, 0, 5, 0);
			gbc_jLabelTransLiveBit.gridx = 2;
			gbc_jLabelTransLiveBit.gridy = 10;
			jPanelMain.add(jLabelTransLiveBit, gbc_jLabelTransLiveBit);
			
			jLabelHeaderVisualization = new JLabel();
			jLabelHeaderVisualization.setText("Visualisation Updates");
			jLabelHeaderVisualization.setFont(new Font("Dialog", Font.BOLD, 13));
			GridBagConstraints gbc_jLabelHeaderVisualization = new GridBagConstraints();
			gbc_jLabelHeaderVisualization.gridwidth = 3;
			gbc_jLabelHeaderVisualization.anchor = GridBagConstraints.WEST;
			gbc_jLabelHeaderVisualization.insets = new Insets(10, 10, 2, 0);
			gbc_jLabelHeaderVisualization.gridx = 0;
			gbc_jLabelHeaderVisualization.gridy = 11;
			jPanelMain.add(jLabelHeaderVisualization, gbc_jLabelHeaderVisualization);
			
			GridBagConstraints gbc_jRadioButtonDisplayChanges = new GridBagConstraints();
			gbc_jRadioButtonDisplayChanges.anchor = GridBagConstraints.WEST;
			gbc_jRadioButtonDisplayChanges.insets = new Insets(0, 10, 5, 5);
			gbc_jRadioButtonDisplayChanges.gridx = 0;
			gbc_jRadioButtonDisplayChanges.gridy = 12;
			jPanelMain.add(getJRadioButtonDisplayEnabled(), gbc_jRadioButtonDisplayChanges);
			
			GridBagConstraints gbc_jRadioButtonDisplayDisable = new GridBagConstraints();
			gbc_jRadioButtonDisplayDisable.anchor = GridBagConstraints.WEST;
			gbc_jRadioButtonDisplayDisable.insets = new Insets(0, 0, 5, 5);
			gbc_jRadioButtonDisplayDisable.gridx = 1;
			gbc_jRadioButtonDisplayDisable.gridy = 12;
			jPanelMain.add(getJRadioButtonDisplayDisable(), gbc_jRadioButtonDisplayDisable);
			GridBagConstraints gbc_jPanelColorSettings = new GridBagConstraints();
			gbc_jPanelColorSettings.insets = new Insets(10, 10, 5, 0);
			gbc_jPanelColorSettings.anchor = GridBagConstraints.NORTH;
			gbc_jPanelColorSettings.gridwidth = 3;
			gbc_jPanelColorSettings.fill = GridBagConstraints.HORIZONTAL;
			gbc_jPanelColorSettings.gridx = 0;
			gbc_jPanelColorSettings.gridy = 13;
//			jPanelMain.add(getJPanelColorSettings(), gbc_jPanelColorSettings);
			jPanelMain.add(getGraphElementLayoutSettingsConfigurationPanels(), gbc_jPanelColorSettings);
			GridBagConstraints gbc_jLabelScheduleLenghtRestriction = new GridBagConstraints();
			gbc_jLabelScheduleLenghtRestriction.anchor = GridBagConstraints.WEST;
			gbc_jLabelScheduleLenghtRestriction.insets = new Insets(0, 10, 0, 5);
			gbc_jLabelScheduleLenghtRestriction.gridx = 0;
			gbc_jLabelScheduleLenghtRestriction.gridy = 14;
			jPanelMain.add(getJLabelScheduleLenghtRestriction(), gbc_jLabelScheduleLenghtRestriction);
			GridBagConstraints gbc_jPanelScheduleLengthRestriction = new GridBagConstraints();
			gbc_jPanelScheduleLengthRestriction.gridwidth = 2;
			gbc_jPanelScheduleLengthRestriction.insets = new Insets(0, 10, 0, 5);
			gbc_jPanelScheduleLengthRestriction.fill = GridBagConstraints.BOTH;
			gbc_jPanelScheduleLengthRestriction.gridx = 0;
			gbc_jPanelScheduleLengthRestriction.gridy = 15;
			jPanelMain.add(getJPanelScheduleLengthRestriction(), gbc_jPanelScheduleLengthRestriction);
			
		}
		return jPanelMain;
	}
	
	/**
	 * Gets the j label time model selection.
	 * @return the j label time model selection
	 */
	private JLabel getJLabelTimeModelSelection() {
		if (jLabelTimeModelSelection == null) {
			jLabelTimeModelSelection = new JLabel("Switch Simulation Time Model");
			jLabelTimeModelSelection.setFont(new Font("Dialog", Font.BOLD, 13));
		}
		return jLabelTimeModelSelection;
	}
	/**
	 * Gets the JRadioButton time model discrete.
	 * @return the JRadioButton time model discrete
	 */
	private JRadioButton getJRadioButtonTimeModelDiscrete() {
		if (jRadioButtonTimeModelDiscrete == null) {
			jRadioButtonTimeModelDiscrete = new JRadioButton("Discrete Time Model");
			jRadioButtonTimeModelDiscrete.setFont(new Font("Dialog", Font.BOLD, 12));
			jRadioButtonTimeModelDiscrete.addActionListener(this);
		}
		return jRadioButtonTimeModelDiscrete;
	}
	/**
	 * Gets the JLabel interval.
	 * @return the JLabel interval
	 */
	private JLabel getJLabelInterval() {
		if (jLabelInterval == null) {
			jLabelInterval = new JLabel("Time between two simulation steps");
			jLabelInterval.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return jLabelInterval;
	}
	/**
	 * Gets the JTextField width value.
	 * @return the JTextField width value
	 */
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
	
	/**
	 * Gets the JRadioButton time model continuous.
	 * @return the JRadioButtonn time model continuous
	 */
	private JRadioButton getJRadioButtonTimeModelContinuous() {
		if (jRadioButtonTimeModelContinuous == null) {
			jRadioButtonTimeModelContinuous = new JRadioButton("Continious Time Model");
			jRadioButtonTimeModelContinuous.setFont(new Font("Dialog", Font.BOLD, 12));
			jRadioButtonTimeModelContinuous.addActionListener(this);
		}
		return jRadioButtonTimeModelContinuous;
	}
	private JLabel getJLabelNetCalcInterval() {
		if (jLabelNetCalcInterval == null) {
			jLabelNetCalcInterval = new JLabel("Network Calculation Interval");
			jLabelNetCalcInterval.setFont(new Font("Dialog", Font.PLAIN, 12));
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
	
	private JLabel getJLabelSimulationType() {
		if (jLabelSimulationType == null) {
			jLabelSimulationType = new JLabel();
			jLabelSimulationType.setText("Execute Simulation based on ...");
			jLabelSimulationType.setFont(new Font("Dialog", Font.BOLD, 13));
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
	
	/**
	 * This method initializes jRadioButtonTransByTime	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButtonTransByAbsolute() {
		if (jRadioButtonTransByAbsolute == null) {
			jRadioButtonTransByAbsolute = new JRadioButton();
			jRadioButtonTransByAbsolute.setFont(new Font("Dialog", Font.PLAIN, 12));
			jRadioButtonTransByAbsolute.setText("Depending on watts (absolute)");
			jRadioButtonTransByAbsolute.addActionListener(this);
		}
		return jRadioButtonTransByAbsolute;
	}
	/**
	 * This method initializes jRadioButtonTransByPercent	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButtonTransByPercent() {
		if (jRadioButtonTransByPercent == null) {
			jRadioButtonTransByPercent = new JRadioButton();
			jRadioButtonTransByPercent.setFont(new Font("Dialog", Font.PLAIN, 12));
			jRadioButtonTransByPercent.setText("Depending on delta watts (percent)");
			jRadioButtonTransByPercent.addActionListener(this);
		}
		return jRadioButtonTransByPercent;
	}
	/**
	 * This method initializes jTextFieldTransWatt	
	 * @return javax.swing.JTextField	
	 */
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
	/**
	 * This method initializes jTextFieldTransPercent	
	 * @return javax.swing.JTextField	
	 */
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
	/**
	 * This method initializes jTextFieldTransLiveBit	
	 * @return javax.swing.JTextField	
	 */
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
	/**
	 * This method initializes jRadioButtonDisplayChanges	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButtonDisplayEnabled() {
		if (jRadioButtonDisplayEnabled == null) {
			jRadioButtonDisplayEnabled = new JRadioButton();
			jRadioButtonDisplayEnabled.setFont(new Font("Dialog", Font.PLAIN, 12));
			jRadioButtonDisplayEnabled.setText("Enable");
			jRadioButtonDisplayEnabled.addActionListener(this);
		}
		return jRadioButtonDisplayEnabled;
	}
	/**
	 * This method initializes jRadioButtonDisplayDisable	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButtonDisplayDisable() {
		if (jRadioButtonDisplayDisable == null) {
			jRadioButtonDisplayDisable = new JRadioButton();
			jRadioButtonDisplayDisable.setFont(new Font("Dialog", Font.PLAIN, 12));
			jRadioButtonDisplayDisable.setText("Disable");
			jRadioButtonDisplayDisable.addActionListener(this);
		}
		return jRadioButtonDisplayDisable;
	}
	
	/**
	 * Gets the graph element layout settings configuration panels.
	 * @return the graph element layout settings configuration panels
	 */
	private JTabbedPane getGraphElementLayoutSettingsConfigurationPanels() {
		if (graphElementLayoutSettingsConfigurationPanels==null) {
			graphElementLayoutSettingsConfigurationPanels = new JTabbedPane();
			graphElementLayoutSettingsConfigurationPanels.setUI(new AwbBasicTabbedPaneUI());
			List<GraphElementLayoutService> layoutServices = ServiceFinder.findServices(GraphElementLayoutService.class);
			for (GraphElementLayoutService layoutService : layoutServices) {
				AbstractGraphElementLayoutSettingsPanel layoutSettingsPanel = layoutService.getGraphElementLayoutSettingPanel(this.currProject, layoutService.getDomain());
				layoutSettingsPanel.setGraphElementLayoutSettingsToVisualization();
				graphElementLayoutSettingsConfigurationPanels.addTab(" " + layoutService.getDomain() + " ", layoutSettingsPanel);
			}
			
		}
		return graphElementLayoutSettingsConfigurationPanels;
	}
	
	private JLabel getJLabelScheduleLenghtRestriction() {
		if (jLabelScheduleLenghtRestriction == null) {
			jLabelScheduleLenghtRestriction = new JLabel("Real Time Settings");
			jLabelScheduleLenghtRestriction.setFont(new Font("Dialog", Font.BOLD, 13));
		}
		return jLabelScheduleLenghtRestriction;
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
			this.loadDataModelToForm();
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
				this.setTimeExplanation(this.getJTextFieldTransLiveBit(), jLabelTransLiveBit);
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
			timModelNew.setTimeFormat(timeModelOld.getTimeFormat());
			// --- Save the new time model settings -----------
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
			step = new Long(0);
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
			step = new Long(0);
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

		this.getJLabelInterval().setEnabled(isTimeModelDiscrete);
		this.getJTextFieldIntervalWidthValue().setEnabled(isTimeModelDiscrete);
		this.getJComboBoxIntervalWidthUnit().setEnabled(isTimeModelDiscrete);
		
		this.getJLabelNetCalcInterval().setEnabled(! isTimeModelDiscrete);
		this.getJTextFieldNetCalcInterval().setEnabled(! isTimeModelDiscrete);
		this.getJComboBoxNetCalcIntervalWidthUnit().setEnabled(! isTimeModelDiscrete);
		
		this.jLabelHeaderPowerTransmission.setEnabled(! isTimeModelDiscrete);
		this.getJRadioButtonTransByAbsolute().setEnabled(! isTimeModelDiscrete);
		this.getJRadioButtonTransByPercent().setEnabled(! isTimeModelDiscrete);
		
		this.jLabelTransWatt.setEnabled(! isTimeModelDiscrete);
		this.getJTextFieldTransWatt().setEnabled(! isTimeModelDiscrete);
		this.jLabelTransWatt.setEnabled(! isTimeModelDiscrete);
		
		this.jLabelTransPercent.setEnabled(! isTimeModelDiscrete);
		this.getJTextFieldTransPercent().setEnabled(! isTimeModelDiscrete);
		this.jLabelTransPercent.setEnabled(! isTimeModelDiscrete);
		
		this.jLabelTransHeaderLiveBit.setEnabled(! isTimeModelDiscrete);
		this.getJTextFieldTransLiveBit().setEnabled(! isTimeModelDiscrete);
		this.jLabelTransLiveBit.setEnabled(! isTimeModelDiscrete);
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
				
			} else if (ae.getSource()==this.getJRadioButtonTimeModelContinuous()) {
				this.switchTimeModel(TimeModelContinuous.class.getName());
				this.enableControls();
				
			} else if (ae.getSource()==this.getJRadioButtonExecutionBasedOnPowerFlow()) {
				this.loadFormToDataModel();
			} else if (ae.getSource()==this.getJRadioButtonExecutionBasedOnSensorData()) {
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
				
			} else if (ae.getSource()==this.getGraphElementLayoutSettingsConfigurationPanels()) {
				this.loadFormToDataModel();
			}
			
		}
	}
	
}  
