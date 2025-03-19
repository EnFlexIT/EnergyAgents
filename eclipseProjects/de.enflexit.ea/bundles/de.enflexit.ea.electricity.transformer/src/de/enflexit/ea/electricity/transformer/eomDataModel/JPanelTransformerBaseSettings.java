package de.enflexit.ea.electricity.transformer.eomDataModel;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import de.enflexit.awb.core.Application;
import de.enflexit.common.csv.CsvDataController;
import de.enflexit.common.swing.KeyAdapter4Numbers;
import de.enflexit.ea.electricity.transformer.TransformerBundleHelper;
import de.enflexit.ea.electricity.transformer.TransformerDataModel;
import de.enflexit.ea.electricity.transformer.TransformerDataModel.TapSide;

/**
 * The Class TransformerStaticModelSettings.
 * 
 * @author Christian Derksen - SOFTEC - University Duisburg-Essen
 */
public class JPanelTransformerBaseSettings extends JPanel implements ActionListener{

	private static final long serialVersionUID = 8027574099572403096L;

	private JDialogTransformerDataModel transformerDialog;
	
	private CsvDataController csvController;
	
	
	private JPanel jPanelTransformerSelection;
	private DefaultComboBoxModel<String> comboBoxModelTransformer;
	private JComboBox<String> jComboBoxTransformerSelection;
	private JButton jButtonSelectCSVFile;

	private JSeparator jSeparatorAfterSelection;

	private JLabel jLabelLibID;
	private JTextField jTextFieldLibraryID;
	
	private JLabel jLabelRatedPower;
	private JTextField jTextFieldRatedPower;
	private JLabel jLabelUpperVoltage;
	private JTextField jTextFieldUpperVoltage;
	private JLabel jLabelLowerVoltage;
	private JTextField jTextFieldLowerVoltage;
	private JCheckBox jCheckBoxUpperVoltageTriPhase;
	private JCheckBox jCheckBoxLowerVoltageTriPhase;
	private DefaultComboBoxModel<TapSide> comboModelSlackNodeSide;
	private JLabel jLabelSlackNodeSide;
	private JComboBox<TapSide> jComboBoxSlackNodeSide;
	
	private JLabel jLabelPhaseShift;
	private JTextField jTextFieldPhaseShift;
	private JLabel jLabelShortCircuitImpedance;
	private JTextField jTextFieldShortCircuitImpedance;
	private JLabel jLabelCopperLosses;
	private JTextField jTextFieldCopperLosses;
	private JLabel jLabelIronLosses;
	private JTextField jTextFieldIronLosses;
	private JLabel jLabelImpedance;
	private JTextField jTextFieldIdleImpedance;
	
	private JSeparator jSeparatorBeforeTap;
	private JCheckBox jCheckBoxTapable;
	private JLabel jLabelTapSide;
	private DefaultComboBoxModel<TapSide> comboModelTapSide;
	private JComboBox<TapSide> jComboBoxTapSide;
	private JLabel jLabelVoltageDeltaPerStep;
	private JTextField jTextFieldVoltageDeltaPerStep;
	private JLabel jLabelPhaseDeltaPerStep;
	private JTextField jTextFieldPhaseDeltaPerStep;
	private JLabel jLabelTapNeutral;
	private JTextField jTextFieldTapNeutral;
	private JLabel jLabelTapMinimum;
	private JTextField jTextFieldTapMinimum;
	private JLabel jLabelTapMaximum;
	private JTextField jTextFieldTapMaximum;
	
	private KeyAdapter4Numbers keyAdapterForDouble;
	private KeyAdapter4Numbers keyAdapterForInteger;
	private JLabel jLabelSlackNodeVoltageLevel;
	private JTextField jTextFieldSlackNodeVoltageLevel;
	
	
	/**
	 * Instantiates a new battery static model dialog.
	 *
	 * @param owner the owner
	 * @param staticModel the static model
	 */
	public JPanelTransformerBaseSettings(JDialogTransformerDataModel transformerDialog) {
		this.transformerDialog = transformerDialog;
		this.initialize();
		this.loadDataModelToDialog();
	}
	private void initialize(){
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		this.setLayout(gridBagLayout);
		
		GridBagConstraints gbc_jPanelTransformerSelection = new GridBagConstraints();
		gbc_jPanelTransformerSelection.insets = new Insets(10, 10, 0, 10);
		gbc_jPanelTransformerSelection.gridwidth = 4;
		gbc_jPanelTransformerSelection.fill = GridBagConstraints.BOTH;
		gbc_jPanelTransformerSelection.gridx = 0;
		gbc_jPanelTransformerSelection.gridy = 0;
		this.add(getJPanelTransformerSelection(), gbc_jPanelTransformerSelection);
		GridBagConstraints gbc_jSeparatorAfterSelection = new GridBagConstraints();
		gbc_jSeparatorAfterSelection.fill = GridBagConstraints.HORIZONTAL;
		gbc_jSeparatorAfterSelection.gridwidth = 4;
		gbc_jSeparatorAfterSelection.insets = new Insets(15, 10, 10, 10);
		gbc_jSeparatorAfterSelection.gridx = 0;
		gbc_jSeparatorAfterSelection.gridy = 1;
		this.add(getJSeparatorAfterSelection(), gbc_jSeparatorAfterSelection);
		GridBagConstraints gbc_jLabelLibID = new GridBagConstraints();
		gbc_jLabelLibID.insets = new Insets(5, 10, 0, 0);
		gbc_jLabelLibID.anchor = GridBagConstraints.WEST;
		gbc_jLabelLibID.gridx = 0;
		gbc_jLabelLibID.gridy = 2;
		this.add(getJLabelLibID(), gbc_jLabelLibID);
		GridBagConstraints gbc_jTextFieldLibraryID = new GridBagConstraints();
		gbc_jTextFieldLibraryID.gridwidth = 3;
		gbc_jTextFieldLibraryID.insets = new Insets(5, 5, 0, 10);
		gbc_jTextFieldLibraryID.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldLibraryID.gridx = 1;
		gbc_jTextFieldLibraryID.gridy = 2;
		this.add(getJTextFieldLibraryID(), gbc_jTextFieldLibraryID);
		
		GridBagConstraints gbc_jLabelRatedPower = new GridBagConstraints();
		gbc_jLabelRatedPower.insets = new Insets(5, 10, 0, 0);
		gbc_jLabelRatedPower.anchor = GridBagConstraints.WEST;
		gbc_jLabelRatedPower.gridx = 0;
		gbc_jLabelRatedPower.gridy = 3;
		this.add(getJLabelRatedPower(), gbc_jLabelRatedPower);
		GridBagConstraints gbc_txtRatedPower = new GridBagConstraints();
		gbc_txtRatedPower.insets = new Insets(5, 5, 0, 0);
		gbc_txtRatedPower.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtRatedPower.gridx = 1;
		gbc_txtRatedPower.gridy = 3;
		this.add(getJTextFieldRatedPower(), gbc_txtRatedPower);
		GridBagConstraints gbc_jLabelUpperVoltage = new GridBagConstraints();
		gbc_jLabelUpperVoltage.anchor = GridBagConstraints.WEST;
		gbc_jLabelUpperVoltage.insets = new Insets(5, 10, 0, 0);
		gbc_jLabelUpperVoltage.gridx = 0;
		gbc_jLabelUpperVoltage.gridy = 4;
		this.add(getJLabelUpperVoltage(), gbc_jLabelUpperVoltage);
		GridBagConstraints gbc_jTextFieldUpperVoltage = new GridBagConstraints();
		gbc_jTextFieldUpperVoltage.insets = new Insets(5, 5, 0, 0);
		gbc_jTextFieldUpperVoltage.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldUpperVoltage.gridx = 1;
		gbc_jTextFieldUpperVoltage.gridy = 4;
		this.add(getJTextFieldUpperVoltage(), gbc_jTextFieldUpperVoltage);
		GridBagConstraints gbc_jLabelLowerVoltage = new GridBagConstraints();
		gbc_jLabelLowerVoltage.anchor = GridBagConstraints.WEST;
		gbc_jLabelLowerVoltage.insets = new Insets(5, 15, 0, 0);
		gbc_jLabelLowerVoltage.gridx = 2;
		gbc_jLabelLowerVoltage.gridy = 4;
		this.add(getJLabelLowerVoltage(), gbc_jLabelLowerVoltage);
		GridBagConstraints gbc_jTextFiledLowerVoltage = new GridBagConstraints();
		gbc_jTextFiledLowerVoltage.insets = new Insets(5, 5, 0, 10);
		gbc_jTextFiledLowerVoltage.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFiledLowerVoltage.gridx = 3;
		gbc_jTextFiledLowerVoltage.gridy = 4;
		this.add(getJTextFieldLowerVoltage(), gbc_jTextFiledLowerVoltage);
		GridBagConstraints gbc_jCheckBoxUpperVoltageTriPhase = new GridBagConstraints();
		gbc_jCheckBoxUpperVoltageTriPhase.insets = new Insets(0, 5, 5, 0);
		gbc_jCheckBoxUpperVoltageTriPhase.anchor = GridBagConstraints.WEST;
		gbc_jCheckBoxUpperVoltageTriPhase.gridx = 1;
		gbc_jCheckBoxUpperVoltageTriPhase.gridy = 5;
		this.add(getJCheckBoxUpperVoltageTriPhase(), gbc_jCheckBoxUpperVoltageTriPhase);
		GridBagConstraints gbc_jCheckBoxLowerVoltageTriPhase = new GridBagConstraints();
		gbc_jCheckBoxLowerVoltageTriPhase.insets = new Insets(0, 5, 5, 0);
		gbc_jCheckBoxLowerVoltageTriPhase.anchor = GridBagConstraints.WEST;
		gbc_jCheckBoxLowerVoltageTriPhase.gridx = 3;
		gbc_jCheckBoxLowerVoltageTriPhase.gridy = 5;
		this.add(getJCheckBoxLowerVoltageTriPhase(), gbc_jCheckBoxLowerVoltageTriPhase);
		GridBagConstraints gbc_jLabelSlackNodeSide = new GridBagConstraints();
		gbc_jLabelSlackNodeSide.insets = new Insets(0, 10, 5, 0);
		gbc_jLabelSlackNodeSide.anchor = GridBagConstraints.WEST;
		gbc_jLabelSlackNodeSide.gridx = 0;
		gbc_jLabelSlackNodeSide.gridy = 6;
		this.add(getJLabelSlackNodeSide(), gbc_jLabelSlackNodeSide);
		GridBagConstraints gbc_jComboBoxSlackNodeSide = new GridBagConstraints();
		gbc_jComboBoxSlackNodeSide.insets = new Insets(0, 5, 5, 0);
		gbc_jComboBoxSlackNodeSide.fill = GridBagConstraints.HORIZONTAL;
		gbc_jComboBoxSlackNodeSide.gridx = 1;
		gbc_jComboBoxSlackNodeSide.gridy = 6;
		this.add(getJComboBoxSlackNodeSide(), gbc_jComboBoxSlackNodeSide);
		GridBagConstraints gbc_jLabelSlackNodeVoltageLevel = new GridBagConstraints();
		gbc_jLabelSlackNodeVoltageLevel.anchor = GridBagConstraints.WEST;
		gbc_jLabelSlackNodeVoltageLevel.insets = new Insets(0, 15, 5, 0);
		gbc_jLabelSlackNodeVoltageLevel.gridx = 2;
		gbc_jLabelSlackNodeVoltageLevel.gridy = 6;
		this.add(getJLabelSlackNodeVoltageLevel(), gbc_jLabelSlackNodeVoltageLevel);
		GridBagConstraints gbc_jTextFieldVoltageLevel = new GridBagConstraints();
		gbc_jTextFieldVoltageLevel.insets = new Insets(0, 5, 5, 10);
		gbc_jTextFieldVoltageLevel.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldVoltageLevel.gridx = 3;
		gbc_jTextFieldVoltageLevel.gridy = 6;
		this.add(getJTextFieldSlackNodeVoltageLevel(), gbc_jTextFieldVoltageLevel);
		GridBagConstraints gbc_jLabelPhaseShift = new GridBagConstraints();
		gbc_jLabelPhaseShift.anchor = GridBagConstraints.WEST;
		gbc_jLabelPhaseShift.insets = new Insets(5, 10, 0, 0);
		gbc_jLabelPhaseShift.gridx = 0;
		gbc_jLabelPhaseShift.gridy = 7;
		this.add(getJLabelPhaseShift(), gbc_jLabelPhaseShift);
		GridBagConstraints gbc_jTextFieldPhaseShift = new GridBagConstraints();
		gbc_jTextFieldPhaseShift.insets = new Insets(5, 5, 0, 0);
		gbc_jTextFieldPhaseShift.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldPhaseShift.gridx = 1;
		gbc_jTextFieldPhaseShift.gridy = 7;
		this.add(getJTextFieldPhaseShift(), gbc_jTextFieldPhaseShift);
		GridBagConstraints gbc_jLabelShortCircuitImpedance = new GridBagConstraints();
		gbc_jLabelShortCircuitImpedance.anchor = GridBagConstraints.WEST;
		gbc_jLabelShortCircuitImpedance.insets = new Insets(5, 15, 0, 0);
		gbc_jLabelShortCircuitImpedance.gridx = 2;
		gbc_jLabelShortCircuitImpedance.gridy = 7;
		this.add(getJLabelShortCircuitImpedance(), gbc_jLabelShortCircuitImpedance);
		GridBagConstraints gbc_jTextFieldShortCircuitImpedance = new GridBagConstraints();
		gbc_jTextFieldShortCircuitImpedance.insets = new Insets(5, 5, 0, 10);
		gbc_jTextFieldShortCircuitImpedance.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldShortCircuitImpedance.gridx = 3;
		gbc_jTextFieldShortCircuitImpedance.gridy = 7;
		this.add(getJTextFieldShortCircuitImpedance(), gbc_jTextFieldShortCircuitImpedance);
		GridBagConstraints gbc_jLabelCopperLosses = new GridBagConstraints();
		gbc_jLabelCopperLosses.anchor = GridBagConstraints.WEST;
		gbc_jLabelCopperLosses.insets = new Insets(5, 10, 0, 0);
		gbc_jLabelCopperLosses.gridx = 0;
		gbc_jLabelCopperLosses.gridy = 8;
		this.add(getJLabelCopperLosses(), gbc_jLabelCopperLosses);
		GridBagConstraints gbc_jTextFieldCopperLosses = new GridBagConstraints();
		gbc_jTextFieldCopperLosses.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldCopperLosses.insets = new Insets(5, 5, 0, 0);
		gbc_jTextFieldCopperLosses.gridx = 1;
		gbc_jTextFieldCopperLosses.gridy = 8;
		this.add(getJTextFieldCopperLosses(), gbc_jTextFieldCopperLosses);
		GridBagConstraints gbc_jLabelIronLosses = new GridBagConstraints();
		gbc_jLabelIronLosses.anchor = GridBagConstraints.WEST;
		gbc_jLabelIronLosses.insets = new Insets(5, 15, 0, 0);
		gbc_jLabelIronLosses.gridx = 2;
		gbc_jLabelIronLosses.gridy = 8;
		this.add(getJLabelIronLosses(), gbc_jLabelIronLosses);
		GridBagConstraints gbc_jTextFieldIronLosses = new GridBagConstraints();
		gbc_jTextFieldIronLosses.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldIronLosses.insets = new Insets(5, 5, 0, 10);
		gbc_jTextFieldIronLosses.gridx = 3;
		gbc_jTextFieldIronLosses.gridy = 8;
		this.add(getJTextFieldIronLosses(), gbc_jTextFieldIronLosses);
		
		GridBagConstraints gbc_lblImpedance = new GridBagConstraints();
		gbc_lblImpedance.insets = new Insets(5, 10, 0, 0);
		gbc_lblImpedance.anchor = GridBagConstraints.WEST;
		gbc_lblImpedance.gridx = 0;
		gbc_lblImpedance.gridy = 9;
		this.add(getLblImpedance(), gbc_lblImpedance);
		GridBagConstraints gbc_txtImpedance = new GridBagConstraints();
		gbc_txtImpedance.insets = new Insets(5, 5, 0, 0);
		gbc_txtImpedance.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtImpedance.gridx = 1;
		gbc_txtImpedance.gridy = 9;
		this.add(getJTextFieldIdleImpedance(), gbc_txtImpedance);
		GridBagConstraints gbc_jSeparatorBeforeTap = new GridBagConstraints();
		gbc_jSeparatorBeforeTap.fill = GridBagConstraints.HORIZONTAL;
		gbc_jSeparatorBeforeTap.gridwidth = 4;
		gbc_jSeparatorBeforeTap.insets = new Insets(15, 10, 10, 10);
		gbc_jSeparatorBeforeTap.gridx = 0;
		gbc_jSeparatorBeforeTap.gridy = 10;
		this.add(getJSeparatorBeforeTap(), gbc_jSeparatorBeforeTap);
		GridBagConstraints gbc_jCheckBoxTapable = new GridBagConstraints();
		gbc_jCheckBoxTapable.gridwidth = 2;
		gbc_jCheckBoxTapable.anchor = GridBagConstraints.WEST;
		gbc_jCheckBoxTapable.insets = new Insets(5, 10, 0, 0);
		gbc_jCheckBoxTapable.gridx = 0;
		gbc_jCheckBoxTapable.gridy = 11;
		this.add(getJCheckBoxTapable(), gbc_jCheckBoxTapable);
		GridBagConstraints gbc_jLabelTapSide = new GridBagConstraints();
		gbc_jLabelTapSide.insets = new Insets(5, 15, 0, 0);
		gbc_jLabelTapSide.anchor = GridBagConstraints.WEST;
		gbc_jLabelTapSide.gridx = 2;
		gbc_jLabelTapSide.gridy = 11;
		this.add(getJLabelTapSide(), gbc_jLabelTapSide);
		GridBagConstraints gbc_jComboBoxTapSide = new GridBagConstraints();
		gbc_jComboBoxTapSide.fill = GridBagConstraints.HORIZONTAL;
		gbc_jComboBoxTapSide.insets = new Insets(5, 5, 0, 10);
		gbc_jComboBoxTapSide.gridx = 3;
		gbc_jComboBoxTapSide.gridy = 11;
		this.add(getJComboBoxTapSide(), gbc_jComboBoxTapSide);
		GridBagConstraints gbc_jLabelVoltageDeltaPerStep = new GridBagConstraints();
		gbc_jLabelVoltageDeltaPerStep.anchor = GridBagConstraints.WEST;
		gbc_jLabelVoltageDeltaPerStep.insets = new Insets(5, 10, 0, 0);
		gbc_jLabelVoltageDeltaPerStep.gridx = 0;
		gbc_jLabelVoltageDeltaPerStep.gridy = 12;
		this.add(getJLabelVoltageDeltaPerStep(), gbc_jLabelVoltageDeltaPerStep);
		GridBagConstraints gbc_jTextFieldVoltageDeltaPerStep = new GridBagConstraints();
		gbc_jTextFieldVoltageDeltaPerStep.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldVoltageDeltaPerStep.insets = new Insets(5, 5, 0, 0);
		gbc_jTextFieldVoltageDeltaPerStep.gridx = 1;
		gbc_jTextFieldVoltageDeltaPerStep.gridy = 12;
		this.add(getJTextFieldVoltageDeltaPerStep(), gbc_jTextFieldVoltageDeltaPerStep);
		GridBagConstraints gbc_jLabelPhaseDeltaPerStep = new GridBagConstraints();
		gbc_jLabelPhaseDeltaPerStep.anchor = GridBagConstraints.WEST;
		gbc_jLabelPhaseDeltaPerStep.insets = new Insets(5, 15, 0, 0);
		gbc_jLabelPhaseDeltaPerStep.gridx = 2;
		gbc_jLabelPhaseDeltaPerStep.gridy = 12;
		this.add(getJLabelPhaseDeltaPerStep(), gbc_jLabelPhaseDeltaPerStep);
		GridBagConstraints gbc_jTextFieldPhaseDeltaPerStep = new GridBagConstraints();
		gbc_jTextFieldPhaseDeltaPerStep.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldPhaseDeltaPerStep.insets = new Insets(5, 5, 0, 10);
		gbc_jTextFieldPhaseDeltaPerStep.gridx = 3;
		gbc_jTextFieldPhaseDeltaPerStep.gridy = 12;
		this.add(getJTextFieldPhaseDeltaPerStep(), gbc_jTextFieldPhaseDeltaPerStep);
		GridBagConstraints gbc_jLabelTapNeutral = new GridBagConstraints();
		gbc_jLabelTapNeutral.anchor = GridBagConstraints.WEST;
		gbc_jLabelTapNeutral.insets = new Insets(5, 10, 0, 0);
		gbc_jLabelTapNeutral.gridx = 0;
		gbc_jLabelTapNeutral.gridy = 13;
		this.add(getJLabelTapNeutral(), gbc_jLabelTapNeutral);
		GridBagConstraints gbc_jTextFieldTapNeutral = new GridBagConstraints();
		gbc_jTextFieldTapNeutral.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldTapNeutral.insets = new Insets(5, 5, 0, 0);
		gbc_jTextFieldTapNeutral.gridx = 1;
		gbc_jTextFieldTapNeutral.gridy = 13;
		this.add(getJTextFieldTapNeutral(), gbc_jTextFieldTapNeutral);
		GridBagConstraints gbc_jLabelTapMinimum = new GridBagConstraints();
		gbc_jLabelTapMinimum.anchor = GridBagConstraints.WEST;
		gbc_jLabelTapMinimum.insets = new Insets(5, 10, 0, 0);
		gbc_jLabelTapMinimum.gridx = 0;
		gbc_jLabelTapMinimum.gridy = 14;
		this.add(getJLabelTapMinimum(), gbc_jLabelTapMinimum);
		GridBagConstraints gbc_jTextFieldTapMinimum = new GridBagConstraints();
		gbc_jTextFieldTapMinimum.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldTapMinimum.insets = new Insets(5, 5, 0, 0);
		gbc_jTextFieldTapMinimum.gridx = 1;
		gbc_jTextFieldTapMinimum.gridy = 14;
		this.add(getJTextFieldTapMinimum(), gbc_jTextFieldTapMinimum);
		GridBagConstraints gbc_jLabelTapMaximum = new GridBagConstraints();
		gbc_jLabelTapMaximum.anchor = GridBagConstraints.WEST;
		gbc_jLabelTapMaximum.insets = new Insets(5, 15, 0, 0);
		gbc_jLabelTapMaximum.gridx = 2;
		gbc_jLabelTapMaximum.gridy = 14;
		this.add(getJLabelTapMaximum(), gbc_jLabelTapMaximum);
		GridBagConstraints gbc_jTextFieldTapMaximum = new GridBagConstraints();
		gbc_jTextFieldTapMaximum.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldTapMaximum.insets = new Insets(5, 5, 0, 10);
		gbc_jTextFieldTapMaximum.gridx = 3;
		gbc_jTextFieldTapMaximum.gridy = 14;
		this.add(getJTextFieldTapMaximum(), gbc_jTextFieldTapMaximum);
		
	}
	
	private JPanel getJPanelTransformerSelection() {
		if (jPanelTransformerSelection == null) {
			jPanelTransformerSelection = new JPanel();
			GridBagLayout gbl_jPanelTransformerSelection = new GridBagLayout();
			gbl_jPanelTransformerSelection.columnWidths = new int[]{0, 0, 0};
			gbl_jPanelTransformerSelection.rowHeights = new int[]{0, 0};
			gbl_jPanelTransformerSelection.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
			gbl_jPanelTransformerSelection.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			jPanelTransformerSelection.setLayout(gbl_jPanelTransformerSelection);
			GridBagConstraints gbc_jComboBoxTransformerSelection = new GridBagConstraints();
			gbc_jComboBoxTransformerSelection.fill = GridBagConstraints.HORIZONTAL;
			gbc_jComboBoxTransformerSelection.gridx = 0;
			gbc_jComboBoxTransformerSelection.gridy = 0;
			jPanelTransformerSelection.add(getJComboBoxTransformerSelection(), gbc_jComboBoxTransformerSelection);
			GridBagConstraints gbc_jButtonSelectCSVFile = new GridBagConstraints();
			gbc_jButtonSelectCSVFile.insets = new Insets(0, 5, 0, 0);
			gbc_jButtonSelectCSVFile.gridx = 1;
			gbc_jButtonSelectCSVFile.gridy = 0;
			jPanelTransformerSelection.add(getJButtonSelectCSVFile(), gbc_jButtonSelectCSVFile);
		}
		return jPanelTransformerSelection;
	}

	/**
	 * Gets the combo box model transformer.
	 * @return the combo box model transformer
	 */
	private DefaultComboBoxModel<String> getComboBoxModelTransformer() {
		if (comboBoxModelTransformer==null) {
			comboBoxModelTransformer = new DefaultComboBoxModel<String>();
			this.reFillComboBoxModelTransformer();
		}
		return comboBoxModelTransformer;
	}
	/**
	 * Fills the combo box model transformer.
	 */
	private void reFillComboBoxModelTransformer() {
		
		DefaultTableModel tableModel = this.getCSVDataController().getDataModel();
		if (tableModel==null) return;
		
		this.getComboBoxModelTransformer().removeAllElements();
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			String transformerID = (String) tableModel.getValueAt(i, 0);
			this.getComboBoxModelTransformer().addElement(transformerID);
		}
	}
	
	private JComboBox<String> getJComboBoxTransformerSelection() {
		if (jComboBoxTransformerSelection == null) {
			jComboBoxTransformerSelection = new JComboBox<String>(this.getComboBoxModelTransformer());
			jComboBoxTransformerSelection.setFont(new Font("Dialog", Font.PLAIN, 11));
			jComboBoxTransformerSelection.setPreferredSize(new Dimension(200, 26));
			jComboBoxTransformerSelection.setMaximumRowCount(15);
			jComboBoxTransformerSelection.addActionListener(this);
		}
		return jComboBoxTransformerSelection;
	}
	private JButton getJButtonSelectCSVFile() {
		if (jButtonSelectCSVFile == null) {
			jButtonSelectCSVFile = new JButton();
			jButtonSelectCSVFile.setPreferredSize(new Dimension(26, 26));
			jButtonSelectCSVFile.setIcon(TransformerBundleHelper.getImageIcon("MBopen.png"));
			jButtonSelectCSVFile.addActionListener(this);
			this.updateFileInfo();
		}
		return jButtonSelectCSVFile;
	}
	
	private JLabel getJLabelLibID() {
		if (jLabelLibID == null) {
			jLabelLibID = new JLabel("Library-ID");
			jLabelLibID.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jLabelLibID;
	}
	private JTextField getJTextFieldLibraryID() {
		if (jTextFieldLibraryID == null) {
			jTextFieldLibraryID = new JTextField();
			jTextFieldLibraryID.setPreferredSize(new Dimension(60, 24));
			jTextFieldLibraryID.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jTextFieldLibraryID;
	}
	
	public JLabel getJLabelRatedPower() {
		if (jLabelRatedPower == null) {
			jLabelRatedPower = new JLabel("Rated Power (sR) [MVA]");
			jLabelRatedPower.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jLabelRatedPower;
	}
	private JTextField getJTextFieldRatedPower() {
		if (jTextFieldRatedPower == null) {
			jTextFieldRatedPower = new JTextField();
			jTextFieldRatedPower.setPreferredSize(new Dimension(60, 24));
			jTextFieldRatedPower.setFont(new Font("Dialog", Font.PLAIN, 11));
			jTextFieldRatedPower.setText("0.0");
			jTextFieldRatedPower.addKeyListener(this.getKeyAdapterForDouble());
		}
		return jTextFieldRatedPower;
	}

	private JSeparator getJSeparatorAfterSelection() {
		if (jSeparatorAfterSelection == null) {
			jSeparatorAfterSelection = new JSeparator();
		}
		return jSeparatorAfterSelection;
	}
	
	private JLabel getJLabelUpperVoltage() {
		if (jLabelUpperVoltage == null) {
			jLabelUpperVoltage = new JLabel("Upper Voltage (vmHV) [kV]");
			jLabelUpperVoltage.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jLabelUpperVoltage;
	}
	private JTextField getJTextFieldUpperVoltage() {
		if (jTextFieldUpperVoltage == null) {
			jTextFieldUpperVoltage = new JTextField();
			jTextFieldUpperVoltage.setFont(new Font("Dialog", Font.PLAIN, 11));
			jTextFieldUpperVoltage.setText("0.0");
			jTextFieldUpperVoltage.setPreferredSize(new Dimension(60, 24));
			jTextFieldUpperVoltage.addKeyListener(this.getKeyAdapterForDouble());
		}
		return jTextFieldUpperVoltage;
	}
	
	private JLabel getJLabelLowerVoltage() {
		if (jLabelLowerVoltage == null) {
			jLabelLowerVoltage = new JLabel("Lower Voltage (vmLV) [kV]");
			jLabelLowerVoltage.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jLabelLowerVoltage;
	}
	private JTextField getJTextFieldLowerVoltage() {
		if (jTextFieldLowerVoltage == null) {
			jTextFieldLowerVoltage = new JTextField();
			jTextFieldLowerVoltage.setFont(new Font("Dialog", Font.PLAIN, 11));
			jTextFieldLowerVoltage.setText("0.0");
			jTextFieldLowerVoltage.setPreferredSize(new Dimension(60, 24));
			jTextFieldLowerVoltage.addKeyListener(this.getKeyAdapterForDouble());
		}
		return jTextFieldLowerVoltage;
	}
	
	private JCheckBox getJCheckBoxUpperVoltageTriPhase() {
		if (jCheckBoxUpperVoltageTriPhase == null) {
			jCheckBoxUpperVoltageTriPhase = new JCheckBox("3 Phases UV");
			jCheckBoxUpperVoltageTriPhase.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jCheckBoxUpperVoltageTriPhase;
	}
	private JCheckBox getJCheckBoxLowerVoltageTriPhase() {
		if (jCheckBoxLowerVoltageTriPhase == null) {
			jCheckBoxLowerVoltageTriPhase = new JCheckBox("3 Phases LV");
			jCheckBoxLowerVoltageTriPhase.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jCheckBoxLowerVoltageTriPhase;
	}
	
	private JLabel getJLabelSlackNodeSide() {
		if (jLabelSlackNodeSide == null) {
			jLabelSlackNodeSide = new JLabel("Slack Node Side");
			jLabelSlackNodeSide.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jLabelSlackNodeSide;
	}
	private DefaultComboBoxModel<TapSide> getComboModelSlackNodeSide() {
		if (comboModelSlackNodeSide==null) {
			comboModelSlackNodeSide = new DefaultComboBoxModel<TransformerDataModel.TapSide>();
			comboModelSlackNodeSide.addElement(TapSide.LowVoltageSide);
			comboModelSlackNodeSide.addElement(TapSide.HighVoltageSide);
		}
		return comboModelSlackNodeSide;
	}
	private JComboBox<TapSide> getJComboBoxSlackNodeSide() {
		if (jComboBoxSlackNodeSide == null) {
			jComboBoxSlackNodeSide = new JComboBox<TapSide>(this.getComboModelSlackNodeSide());
			jComboBoxSlackNodeSide.setSelectedIndex(0);
			jComboBoxSlackNodeSide.setPreferredSize(new Dimension(60, 24));
			jComboBoxSlackNodeSide.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jComboBoxSlackNodeSide;
	}
	
	private JLabel getJLabelSlackNodeVoltageLevel() {
		if (jLabelSlackNodeVoltageLevel == null) {
			jLabelSlackNodeVoltageLevel = new JLabel("Slack Node Voltage Level [V]");
			jLabelSlackNodeVoltageLevel.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jLabelSlackNodeVoltageLevel;
	}
	private JTextField getJTextFieldSlackNodeVoltageLevel() {
		if (jTextFieldSlackNodeVoltageLevel == null) {
			jTextFieldSlackNodeVoltageLevel = new JTextField();
			jTextFieldSlackNodeVoltageLevel.setText("0.0");
			jTextFieldSlackNodeVoltageLevel.setPreferredSize(new Dimension(60, 24));
			jTextFieldSlackNodeVoltageLevel.setFont(new Font("Dialog", Font.PLAIN, 11));
			jTextFieldSlackNodeVoltageLevel.addKeyListener(this.getKeyAdapterForDouble());
		}
		return jTextFieldSlackNodeVoltageLevel;
	}
	
	
	private JLabel getJLabelPhaseShift() {
		if (jLabelPhaseShift == null) {
			jLabelPhaseShift = new JLabel("Phase Shift (va0) [°]");
			jLabelPhaseShift.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jLabelPhaseShift;
	}
	private JTextField getJTextFieldPhaseShift() {
		if (jTextFieldPhaseShift == null) {
			jTextFieldPhaseShift = new JTextField();
			jTextFieldPhaseShift.setFont(new Font("Dialog", Font.PLAIN, 11));
			jTextFieldPhaseShift.setText("0.0");
			jTextFieldPhaseShift.setPreferredSize(new Dimension(60, 24));
			jTextFieldPhaseShift.addKeyListener(this.getKeyAdapterForDouble());
		}
		return jTextFieldPhaseShift;
	}
	
	private JLabel getJLabelShortCircuitImpedance() {
		if (jLabelShortCircuitImpedance == null) {
			jLabelShortCircuitImpedance = new JLabel("Short-Circuit Imp. (vmImp) [%]");
			jLabelShortCircuitImpedance.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jLabelShortCircuitImpedance;
	}
	private JTextField getJTextFieldShortCircuitImpedance() {
		if (jTextFieldShortCircuitImpedance == null) {
			jTextFieldShortCircuitImpedance = new JTextField();
			jTextFieldShortCircuitImpedance.setFont(new Font("Dialog", Font.PLAIN, 11));
			jTextFieldShortCircuitImpedance.setText("0.0");
			jTextFieldShortCircuitImpedance.setPreferredSize(new Dimension(60, 24));
			jTextFieldShortCircuitImpedance.addKeyListener(this.getKeyAdapterForDouble());
		}
		return jTextFieldShortCircuitImpedance;
	}
	
	private JLabel getJLabelCopperLosses() {
		if (jLabelCopperLosses == null) {
			jLabelCopperLosses = new JLabel("Copper Losses (pCu) [kW]");
			jLabelCopperLosses.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jLabelCopperLosses;
	}
	private JTextField getJTextFieldCopperLosses() {
		if (jTextFieldCopperLosses == null) {
			jTextFieldCopperLosses = new JTextField();
			jTextFieldCopperLosses.setFont(new Font("Dialog", Font.PLAIN, 11));
			jTextFieldCopperLosses.setText("0.0");
			jTextFieldCopperLosses.setPreferredSize(new Dimension(60, 24));
			jTextFieldCopperLosses.addKeyListener(this.getKeyAdapterForDouble());
		}
		return jTextFieldCopperLosses;
	}
	
	private JLabel getJLabelIronLosses() {
		if (jLabelIronLosses == null) {
			jLabelIronLosses = new JLabel("Iron Losses (pFe) [kW]");
			jLabelIronLosses.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jLabelIronLosses;
	}
	private JTextField getJTextFieldIronLosses() {
		if (jTextFieldIronLosses == null) {
			jTextFieldIronLosses = new JTextField();
			jTextFieldIronLosses.setFont(new Font("Dialog", Font.PLAIN, 11));
			jTextFieldIronLosses.setText("0.0");
			jTextFieldIronLosses.setPreferredSize(new Dimension(60, 24));
			jTextFieldIronLosses.addKeyListener(this.getKeyAdapterForDouble());
		}
		return jTextFieldIronLosses;
	}
	
	private JLabel getLblImpedance() {
		if (jLabelImpedance == null) {
			jLabelImpedance = new JLabel("Idle Impedance (iNoLoad) [%]");
			jLabelImpedance.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jLabelImpedance;
	}
	private JTextField getJTextFieldIdleImpedance() {
		if (jTextFieldIdleImpedance == null) {
			jTextFieldIdleImpedance = new JTextField();
			jTextFieldIdleImpedance.setPreferredSize(new Dimension(60, 24));
			jTextFieldIdleImpedance.setFont(new Font("Dialog", Font.PLAIN, 11));
			jTextFieldIdleImpedance.setText("0.0");
			jTextFieldIdleImpedance.addKeyListener(this.getKeyAdapterForDouble());
		}
		return jTextFieldIdleImpedance;
	}
	
	private JSeparator getJSeparatorBeforeTap() {
		if (jSeparatorBeforeTap == null) {
			jSeparatorBeforeTap = new JSeparator();
		}
		return jSeparatorBeforeTap;
	}

	private JCheckBox getJCheckBoxTapable() {
		if (jCheckBoxTapable == null) {
			jCheckBoxTapable = new JCheckBox("Tapable");
			jCheckBoxTapable.setFont(new Font("Dialog", Font.BOLD, 11));
			jCheckBoxTapable.addActionListener(this);
		}
		return jCheckBoxTapable;
	}
	
	private JLabel getJLabelTapSide() {
		if (jLabelTapSide == null) {
			jLabelTapSide = new JLabel("Tap Side");
			jLabelTapSide.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jLabelTapSide;
	}
	
	
	public DefaultComboBoxModel<TapSide> getComboModelTapSide() {
		if (comboModelTapSide==null) {
			comboModelTapSide = new DefaultComboBoxModel<TransformerDataModel.TapSide>();
			comboModelTapSide.addElement(TapSide.LowVoltageSide);
			comboModelTapSide.addElement(TapSide.HighVoltageSide);
		}
		return comboModelTapSide;
	}
	private JComboBox<TapSide> getJComboBoxTapSide() {
		if (jComboBoxTapSide == null) {
			jComboBoxTapSide = new JComboBox<TapSide>(this.getComboModelTapSide());
			jComboBoxTapSide.setPreferredSize(new Dimension(60, 24));
			jComboBoxTapSide.setFont(new Font("Dialog", Font.PLAIN, 11));
			jComboBoxTapSide.setSelectedIndex(0);
		}
		return jComboBoxTapSide;
	}
	
	private JLabel getJLabelVoltageDeltaPerStep() {
		if (jLabelVoltageDeltaPerStep == null) {
			jLabelVoltageDeltaPerStep = new JLabel("Voltage Delta / Tap (dVm) [%]");
			jLabelVoltageDeltaPerStep.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jLabelVoltageDeltaPerStep;
	}
	private JTextField getJTextFieldVoltageDeltaPerStep() {
		if (jTextFieldVoltageDeltaPerStep == null) {
			jTextFieldVoltageDeltaPerStep = new JTextField();
			jTextFieldVoltageDeltaPerStep.setFont(new Font("Dialog", Font.PLAIN, 11));
			jTextFieldVoltageDeltaPerStep.setText("0.0");
			jTextFieldVoltageDeltaPerStep.setPreferredSize(new Dimension(60, 24));
			jTextFieldVoltageDeltaPerStep.addKeyListener(this.getKeyAdapterForDouble());
		}
		return jTextFieldVoltageDeltaPerStep;
	}
	
	private JLabel getJLabelPhaseDeltaPerStep() {
		if (jLabelPhaseDeltaPerStep == null) {
			jLabelPhaseDeltaPerStep = new JLabel("Phase Delta per Tap (dVa)");
			jLabelPhaseDeltaPerStep.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jLabelPhaseDeltaPerStep;
	}
	private JTextField getJTextFieldPhaseDeltaPerStep() {
		if (jTextFieldPhaseDeltaPerStep == null) {
			jTextFieldPhaseDeltaPerStep = new JTextField();
			jTextFieldPhaseDeltaPerStep.setFont(new Font("Dialog", Font.PLAIN, 11));
			jTextFieldPhaseDeltaPerStep.setText("0.0");
			jTextFieldPhaseDeltaPerStep.setPreferredSize(new Dimension(60, 24));
			jTextFieldPhaseDeltaPerStep.addKeyListener(this.getKeyAdapterForDouble());
		}
		return jTextFieldPhaseDeltaPerStep;
	}
	
	private JLabel getJLabelTapNeutral() {
		if (jLabelTapNeutral == null) {
			jLabelTapNeutral = new JLabel("Tap - Neutral");
			jLabelTapNeutral.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jLabelTapNeutral;
	}
	private JTextField getJTextFieldTapNeutral() {
		if (jTextFieldTapNeutral == null) {
			jTextFieldTapNeutral = new JTextField();
			jTextFieldTapNeutral.setFont(new Font("Dialog", Font.PLAIN, 11));
			jTextFieldTapNeutral.setText("0");
			jTextFieldTapNeutral.setPreferredSize(new Dimension(60, 24));
			jTextFieldTapNeutral.addKeyListener(this.getKeyAdapterForInteger());
		}
		return jTextFieldTapNeutral;
	}
	
	private JLabel getJLabelTapMinimum() {
		if (jLabelTapMinimum == null) {
			jLabelTapMinimum = new JLabel("Tap - Minimum");
			jLabelTapMinimum.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jLabelTapMinimum;
	}
	private JTextField getJTextFieldTapMinimum() {
		if (jTextFieldTapMinimum == null) {
			jTextFieldTapMinimum = new JTextField();
			jTextFieldTapMinimum.setFont(new Font("Dialog", Font.PLAIN, 11));
			jTextFieldTapMinimum.setText("0");
			jTextFieldTapMinimum.setPreferredSize(new Dimension(60, 24));
			jTextFieldTapMinimum.addKeyListener(this.getKeyAdapterForInteger());
		}
		return jTextFieldTapMinimum;
	}
	
	private JLabel getJLabelTapMaximum() {
		if (jLabelTapMaximum == null) {
			jLabelTapMaximum = new JLabel("Tap - Maximum");
			jLabelTapMaximum.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jLabelTapMaximum;
	}
	private JTextField getJTextFieldTapMaximum() {
		if (jTextFieldTapMaximum == null) {
			jTextFieldTapMaximum = new JTextField();
			jTextFieldTapMaximum.setFont(new Font("Dialog", Font.PLAIN, 11));
			jTextFieldTapMaximum.setText("0");
			jTextFieldTapMaximum.setPreferredSize(new Dimension(60, 24));
			jTextFieldTapMaximum.addKeyListener(this.getKeyAdapterForInteger());
		}
		return jTextFieldTapMaximum;
	}
	
	private KeyAdapter4Numbers getKeyAdapterForDouble() {
		if (keyAdapterForDouble==null) {
			keyAdapterForDouble = new KeyAdapter4Numbers(true);
		}
		return keyAdapterForDouble;
	}
	private KeyAdapter4Numbers getKeyAdapterForInteger() {
		if (keyAdapterForInteger==null) {
			keyAdapterForInteger = new KeyAdapter4Numbers(false);
		}
		return keyAdapterForInteger;
	}

	
	public TransformerDataModel getTransformerDataModel() {
		if (transformerDialog!=null) {
			return transformerDialog.getTransformerDataModel();
		}
		return null;
	}
	public void setTransformerDataModel(TransformerDataModel transformerDataModel) {
		if (transformerDialog!=null) {
			this.transformerDialog.setTransformerDataModel(transformerDataModel);
		}
	}

	
	/**
	 * Load the data model to the dialog.
	 */
	public void loadDataModelToDialog(){
		
		if (this.getTransformerDataModel()==null) return;
		
		this.getJTextFieldLibraryID().setText(this.getTransformerDataModel().getLibraryID());
		
		this.getJTextFieldRatedPower().setText("" + this.getTransformerDataModel().getRatedPower_sR());
		this.getJTextFieldUpperVoltage().setText("" + this.getTransformerDataModel().getUpperVoltage_vmHV());
		this.getJTextFieldLowerVoltage().setText("" + this.getTransformerDataModel().getLowerVoltage_vmLV());
		
		this.getJCheckBoxUpperVoltageTriPhase().setSelected(this.getTransformerDataModel().isUpperVoltage_ThriPhase());
		this.getJCheckBoxLowerVoltageTriPhase().setSelected(this.getTransformerDataModel().isLowerVoltage_ThriPhase());
		
		this.getJComboBoxSlackNodeSide().setSelectedItem(this.getTransformerDataModel().getSlackNodeSide());
		this.getJTextFieldSlackNodeVoltageLevel().setText("" + this.getTransformerDataModel().getSlackNodeVoltageLevel());
		
		this.getJTextFieldPhaseShift().setText("" + this.getTransformerDataModel().getPhaseShift_va0());
		this.getJTextFieldShortCircuitImpedance().setText("" + this.getTransformerDataModel().getShortCircuitImpedance_vmImp());
		
		this.getJTextFieldCopperLosses().setText("" + this.getTransformerDataModel().getCopperLosses_pCu());
		this.getJTextFieldIronLosses().setText("" + this.getTransformerDataModel().getIronLosses_pFe());
		
		this.getJTextFieldIdleImpedance().setText("" + this.getTransformerDataModel().getIdleImpedance_iNoLoad());
		
		this.getJCheckBoxTapable().setSelected(this.getTransformerDataModel().isTapable());
		this.getJComboBoxTapSide().setSelectedItem(this.getTransformerDataModel().getTapSide());
		
		this.getJTextFieldVoltageDeltaPerStep().setText("" + this.getTransformerDataModel().getVoltageDeltaPerTap_dVm());
		this.getJTextFieldPhaseDeltaPerStep().setText("" + this.getTransformerDataModel().getPhaseShiftPerTap_dVa());
		
		this.getJTextFieldTapNeutral().setText("" + this.getTransformerDataModel().getTapNeutral());
		this.getJTextFieldTapMinimum().setText("" + this.getTransformerDataModel().getTapMinimum());
		this.getJTextFieldTapMaximum().setText("" + this.getTransformerDataModel().getTapMaximum());
		
		this.setTapEditEnabled();
	}
	/**
	 * Loads the dialog data to the data model.
	 */
	public void loadDialogDataToDataModel() {
		
		this.getTransformerDataModel().setLibraryID(this.getJTextFieldLibraryID().getText());
		
		this.getTransformerDataModel().setRatedPower_sR(this.transformerDialog.getDoubleValue(this.getJTextFieldRatedPower()));
		this.getTransformerDataModel().setUpperVoltage_vmHV(this.transformerDialog.getDoubleValue(this.getJTextFieldUpperVoltage()));
		this.getTransformerDataModel().setLowerVoltage_vmLV(this.transformerDialog.getDoubleValue(this.getJTextFieldLowerVoltage()));
		
		this.getTransformerDataModel().setUpperVoltage_ThriPhase(this.getJCheckBoxUpperVoltageTriPhase().isSelected());
		this.getTransformerDataModel().setLowerVoltage_ThriPhase(this.getJCheckBoxLowerVoltageTriPhase().isSelected());
		
		this.getTransformerDataModel().setSlackNodeSide((TapSide) this.getJComboBoxSlackNodeSide().getSelectedItem());
		this.getTransformerDataModel().setSlackNodeVoltageLevel(this.transformerDialog.getDoubleValue(this.getJTextFieldSlackNodeVoltageLevel()));
		
		this.getTransformerDataModel().setPhaseShift_va0(this.transformerDialog.getDoubleValue(this.getJTextFieldPhaseShift()));
		this.getTransformerDataModel().setShortCircuitImpedance_vmImp(this.transformerDialog.getDoubleValue(this.getJTextFieldShortCircuitImpedance()));
		
		this.getTransformerDataModel().setCopperLosses_pCu(this.transformerDialog.getDoubleValue(this.getJTextFieldCopperLosses()));
		this.getTransformerDataModel().setIronLosses_pFe(this.transformerDialog.getDoubleValue(this.getJTextFieldIronLosses()));
		
		this.getTransformerDataModel().setIdleImpedance_iNoLoad(this.transformerDialog.getDoubleValue(this.getJTextFieldIdleImpedance()));
		
		this.getTransformerDataModel().setTapable(this.getJCheckBoxTapable().isSelected());
		this.getTransformerDataModel().setTapSide((TapSide) this.getJComboBoxTapSide().getSelectedItem());
		
		this.getTransformerDataModel().setVoltageDeltaPerTap_dVm(this.transformerDialog.getDoubleValue(this.getJTextFieldVoltageDeltaPerStep()));
		this.getTransformerDataModel().setPhaseShiftPerTap_dVa(this.transformerDialog.getDoubleValue(this.getJTextFieldPhaseDeltaPerStep()));
		
		this.getTransformerDataModel().setTapNeutral(this.transformerDialog.getIntegerValue(this.getJTextFieldTapNeutral()));
		this.getTransformerDataModel().setTapMinimum(this.transformerDialog.getIntegerValue(this.getJTextFieldTapMinimum()));
		this.getTransformerDataModel().setTapMaximum(this.transformerDialog.getIntegerValue(this.getJTextFieldTapMaximum()));
		
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		
		if (ae.getSource()==this.getJButtonSelectCSVFile()) {
			// --- Select the csv file for transformer selection ----
			this.selectTransformerFile();
			
		} else if (ae.getSource()==this.getJComboBoxTransformerSelection()) {
			// --- Set the row from the library ---------------------
			String idSelection = (String) this.getJComboBoxTransformerSelection().getSelectedItem();
			this.setTransformerFromLibrary(idSelection);
			
		} else if (ae.getSource()==this.getJCheckBoxTapable()) {
			// --- Enable / disable Tap settings --------------------
			this.setTapEditEnabled();
			
		}
	}
	
	
	// ------------------------------------------------------------------------
	// --- From here library file handling ------------------------------------
	// ------------------------------------------------------------------------	
	/**
	 * Select transformer file.
	 */
	private void selectTransformerFile() {
		
		File fileSelected = this.getTransformerLibraryFile();
		File fileSelectedNew = this.selectFile(this, fileSelected);
		if (fileSelectedNew!=null && fileSelectedNew.exists()==true && fileSelectedNew.equals(fileSelected)==false) {
			this.saveTransformerLibraryFile(fileSelectedNew);
			this.loadCSVData(fileSelectedNew);
			this.updateFileInfo();
		}
	}
	/**
	 * Updates the current file info.
	 */
	private void updateFileInfo() {
		
		File tLibFile = this.getTransformerLibraryFile();
		if (tLibFile==null) {
			this.getJButtonSelectCSVFile().setToolTipText("<html>Select csv file ...</html>");
		} else {
			this.getJButtonSelectCSVFile().setToolTipText("<html>Select csv file ... <br><b>Current file: " + tLibFile.getAbsolutePath() + "</b></html>");
		}
	}
	/**
	 * Return the currently configured transformer file.
	 * @return the transformer library file
	 */
	private File getTransformerLibraryFile() {
		
		File tLibFile = null;
		
		String tSelectedProjectFileName = TransformerBundleHelper.getString(TransformerBundleHelper.PROP_TRANSFOMRER_SELECTION_FILE, null);
		if (tSelectedProjectFileName!=null && tSelectedProjectFileName.isEmpty()==false) {
			tLibFile = new File(tSelectedProjectFileName);
		}

		// --- Check if file exists -----------------------
		if (tLibFile!=null && tLibFile.exists()==false) {
			tLibFile = null;
		}
		return tLibFile;
	}
	/**
	 * Saves the specified transformer library file.
	 * @param tLibFile the file instance of the library
	 */
	private void saveTransformerLibraryFile(File tLibFile) {
		if (tLibFile!=null && tLibFile.exists()==true) {
			TransformerBundleHelper.putString(TransformerBundleHelper.PROP_TRANSFOMRER_SELECTION_FILE, tLibFile.getAbsolutePath());
		}
	}
	/**
	 * Select file to load a csv file.
	 *
	 * @param parentComponent the parent component
	 * @param currentFile the current file
	 * @return the selected file instance
	 */
	public File selectFile(Component parentComponent, File currentFile) {
		
		String fileSuffixCSV = "csv";
		FileFilter fileFilterCSV = new FileNameExtensionFilter("CSV File", fileSuffixCSV);
		
		
		File fileSelected = null;
		File dirSelected = null;
		if (currentFile==null || currentFile.exists()==false) {
			// --- Set directory to projects root -------------------
			if (Application.getProjectFocused()!=null) {
				dirSelected = new File(Application.getProjectFocused().getProjectFolderFullPath());
			}
		} else {
			// --- Get path and directory from current file ---------
			fileSelected = currentFile;
			dirSelected = currentFile.getParentFile();
		}
		
		// --- Create file choose instance ------------
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(fileFilterCSV);
		fileChooser.setFileFilter(fileFilterCSV);
		
		fileChooser.setCurrentDirectory(dirSelected);
		fileChooser.setSelectedFile(fileSelected);
		
		// --- Show file selection dialog -------------
		this.disableNewFolderButton(fileChooser);	
		int ret = fileChooser.showDialog(parentComponent, "Open Transformer Library File");
		// - - - - - - - - - - - - - - - - - - - - - -  
		
		if (ret == JFileChooser.APPROVE_OPTION) {
			fileSelected = fileChooser.getSelectedFile();
			String fileExtension = null;
			if (fileChooser.getFileFilter() == fileFilterCSV) {
				fileExtension = "." + fileSuffixCSV;
			}
			if (fileSelected.getAbsolutePath().toLowerCase().endsWith(fileExtension)==false) {
				fileSelected = new File(fileSelected.getAbsoluteFile() + fileExtension);	
			}
		}
		return fileSelected;
	}
	/**
	 * Disable new folder button.
	 * @param checkContainer the container to check
	 */
	private void disableNewFolderButton(Container checkContainer) {
		int len = checkContainer.getComponentCount();
		for (int i = 0; i < len; i++) {
			Component comp = checkContainer.getComponent(i);
			if (comp instanceof JButton) {
				JButton button = (JButton) comp;
				Icon icon = button.getIcon();
				if (icon!=null && icon==UIManager.getIcon("FileChooser.newFolderIcon")) {
					button.setEnabled(false);
					return;
				}
			} else if (comp instanceof Container) {
				disableNewFolderButton((Container) comp);
			}
		}
	}
	
	// ------------------------------------------------------------------------
	// --- From here csv controller handling ----------------------------------
	// ------------------------------------------------------------------------
	/**
	 * Returns the CSV data controller.
	 * @return the CSV data controller
	 */
	private CsvDataController getCSVDataController() {
		if (csvController==null) {
			csvController = new CsvDataController();
			csvController.setHeadline(true);
			csvController.setSeparator(";");
			// --- Load the transformer library file? ----- 
			File tLibFile = this.getTransformerLibraryFile();
			if (tLibFile!=null) {
				csvController.setFile(tLibFile);
				csvController.doImport();
			}
		}
		return csvController;
	}
	/**
	 * Loads the CSV data to the CsvDataController.
	 * @param fileToBeLoaded the file to be loaded
	 */
	private void loadCSVData(File fileToBeLoaded) {
		if (fileToBeLoaded!=null && fileToBeLoaded.exists()==true) {
			this.getCSVDataController().setFile(fileToBeLoaded);
			this.getCSVDataController().doImport();
			this.reFillComboBoxModelTransformer();
		}
	}
	
	/**
	 * Sets the transformer from library.
	 * @param idTransformer the id of the transformer of the library
	 */
	private void setTransformerFromLibrary(String idTransformer) {
		
		DefaultTableModel tableModel = this.getCSVDataController().getDataModel();
		if (tableModel==null) return;

		int rowIndexTransformer = this.getRowIndexOfTransformerSelection(idTransformer);
		if (rowIndexTransformer<0) return;
		
		boolean isTapable = true;
		
		for (int i = 0; i < tableModel.getColumnCount(); i++) {
			
			String colValue = (String) tableModel.getValueAt(rowIndexTransformer, i);
			
			// ------------------------------------------------------------------------------------------------------------------
			// Header from file: id;sR;vmHV;vmLV;va0;vmImp;pCu;pFe;iNoLoad;tapable;tapside;dVm;dVa;tapNeutr;tapMin;tapMax
			// Index from file:   0; 1;   2;   3;  4;    5;  6;  7;      8;      9;     10; 11; 12;      13;    14;    15
			// ------------------------------------------------------------------------------------------------------------------
			if (i==0) this.getJTextFieldLibraryID().setText(colValue);
			
			if (i==1) this.getJTextFieldRatedPower().setText(colValue);
			if (i==2) this.getJTextFieldUpperVoltage().setText(colValue);
			if (i==3) this.getJTextFieldLowerVoltage().setText(colValue);
			
			if (i==4) this.getJTextFieldPhaseShift().setText(colValue);
			if (i==5) this.getJTextFieldShortCircuitImpedance().setText(colValue);
			
			if (i==6) this.getJTextFieldCopperLosses().setText(colValue);
			if (i==7) this.getJTextFieldIronLosses().setText(colValue);
			
			if (i==8) this.getJTextFieldIdleImpedance().setText(colValue);
			
			if (i>8) {
				if (i==9) {
					isTapable = colValue.equals("1");
					this.getJCheckBoxTapable().setSelected(isTapable);
				}
				if (i==10) {
					TapSide tapSide = TapSide.LowVoltageSide;
					if (colValue.equals("HV")==true) {
						tapSide = TapSide.HighVoltageSide;
					}
					this.getJComboBoxTapSide().setSelectedItem(tapSide);
					this.setTapEditEnabled();
				}
				
				if (i==11) this.getJTextFieldVoltageDeltaPerStep().setText(colValue);
				if (i==12) this.getJTextFieldPhaseDeltaPerStep().setText(colValue);
				
				if (i==13) this.getJTextFieldTapNeutral().setText(colValue);
				if (i==14) this.getJTextFieldTapMinimum().setText(colValue);
				if (i==15) this.getJTextFieldTapMaximum().setText(colValue);
			}
			
		}
	}
	/**
	 * Return the row index of transformer selection.
	 *
	 * @param idTransformer the id transformer
	 * @return the row of transformer selection
	 */
	private int getRowIndexOfTransformerSelection(String idTransformer) {
		
		DefaultTableModel tableModel = this.getCSVDataController().getDataModel();
		if (tableModel==null) return -1;
		
		int matchRow = -1;
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			String transformerID = (String) tableModel.getValueAt(i, 0);
			if (transformerID.equals(idTransformer)) {
				matchRow = i;
				break;
			}
		}
		return matchRow;
	}
	
	
	/**
	 * Sets the tap edit enabled according to the value of the corresponding check box.
	 */
	private void setTapEditEnabled() {
		
		boolean isTapEnabled = this.getJCheckBoxTapable().isSelected();
		
		this.getJComboBoxTapSide().setEnabled(isTapEnabled);
		
		this.getJTextFieldVoltageDeltaPerStep().setEnabled(isTapEnabled);
		this.getJTextFieldPhaseDeltaPerStep().setEnabled(isTapEnabled);
		
		this.getJTextFieldTapNeutral().setEnabled(isTapEnabled);
		this.getJTextFieldTapMinimum().setEnabled(isTapEnabled);
		this.getJTextFieldTapMaximum().setEnabled(isTapEnabled);
	}
	
}
