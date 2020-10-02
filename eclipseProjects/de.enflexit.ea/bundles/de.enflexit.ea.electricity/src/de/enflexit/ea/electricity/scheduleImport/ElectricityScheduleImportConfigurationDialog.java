package de.enflexit.ea.electricity.scheduleImport;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import javax.swing.JButton;
import javax.swing.JDialog;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import energy.helper.UnitConverter;
import energy.optionModel.gui.components.DateTimeWidget;
import energy.optionModel.gui.components.TimeUnitComboBox;
import javax.swing.JSpinner;

/**
 * Dialog for configuring a load profile CSV import
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public class ElectricityScheduleImportConfigurationDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	private static final double INITIAL_COS_PHI = 0.95;
	
	// Swing components
	private JLabel jLabelStartTime;
	private DateTimeWidget startDateTimeWidget;
	private JLabel jLabelTimeStepLength;
	private JTextField jTextFieldTimeStepLength;
	private TimeUnitComboBox comboBoxTimeStepUnit;
	private JPanel jPanelButtons;
	private JButton jButtonOK;
	private JButton jButtonCancel;
	
	private AbstractElectricalNetworkScheduleImporter csvImporter;
	
	private boolean canceled;
	private JLabel jLabelCosPhi;
	private JSpinner jSpinnerCosPhi;
	/**
	 * Constructor - Just for the use of the WindowBuilder
	 */
	@Deprecated
	public ElectricityScheduleImportConfigurationDialog() {
		this.initialize();
	}
	/**
	 * Constructor
	 * @param csvImporter The {@link LoadProfileCsvImporter} instance for this dialog
	 */
	public ElectricityScheduleImportConfigurationDialog(AbstractElectricalNetworkScheduleImporter csvImporter){
		this.csvImporter = csvImporter;
		this.initialize();
	}
	
	private void initialize(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		GridBagConstraints gbc_jLabelStartTime = new GridBagConstraints();
		gbc_jLabelStartTime.anchor = GridBagConstraints.EAST;
		gbc_jLabelStartTime.insets = new Insets(5, 5, 5, 5);
		gbc_jLabelStartTime.gridx = 0;
		gbc_jLabelStartTime.gridy = 0;
		getContentPane().add(getJLabelStartTime(), gbc_jLabelStartTime);
		GridBagConstraints gbc_startDateTimeSelector = new GridBagConstraints();
		gbc_startDateTimeSelector.gridwidth = 2;
		gbc_startDateTimeSelector.anchor = GridBagConstraints.WEST;
		gbc_startDateTimeSelector.insets = new Insets(5, 5, 5, 5);
		gbc_startDateTimeSelector.gridx = 1;
		gbc_startDateTimeSelector.gridy = 0;
		getContentPane().add(getStartDateTimeWidget(), gbc_startDateTimeSelector);
		GridBagConstraints gbc_jLabelStepSize = new GridBagConstraints();
		gbc_jLabelStepSize.anchor = GridBagConstraints.EAST;
		gbc_jLabelStepSize.insets = new Insets(5, 5, 5, 5);
		gbc_jLabelStepSize.gridx = 0;
		gbc_jLabelStepSize.gridy = 1;
		getContentPane().add(getJLabelTimeStepLength(), gbc_jLabelStepSize);
		GridBagConstraints gbc_jTextFieldTimeStepLength = new GridBagConstraints();
		gbc_jTextFieldTimeStepLength.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldTimeStepLength.insets = new Insets(5, 5, 5, 5);
		gbc_jTextFieldTimeStepLength.gridx = 1;
		gbc_jTextFieldTimeStepLength.gridy = 1;
		getContentPane().add(getJTextFieldTimeStepLength(), gbc_jTextFieldTimeStepLength);
		GridBagConstraints gbc_comboBoxTimeStepUnit = new GridBagConstraints();
		gbc_comboBoxTimeStepUnit.anchor = GridBagConstraints.WEST;
		gbc_comboBoxTimeStepUnit.insets = new Insets(5, 5, 5, 5);
		gbc_comboBoxTimeStepUnit.gridx = 2;
		gbc_comboBoxTimeStepUnit.gridy = 1;
		getContentPane().add(getComboBoxTimeStepUnit(), gbc_comboBoxTimeStepUnit);
		GridBagConstraints gbc_jLabelCosPhi = new GridBagConstraints();
		gbc_jLabelCosPhi.anchor = GridBagConstraints.EAST;
		gbc_jLabelCosPhi.insets = new Insets(5, 5, 5, 5);
		gbc_jLabelCosPhi.gridx = 0;
		gbc_jLabelCosPhi.gridy = 2;
		getContentPane().add(getJLabelCosPhi(), gbc_jLabelCosPhi);
		GridBagConstraints gbc_jSpinnerCosPhi = new GridBagConstraints();
		gbc_jSpinnerCosPhi.fill = GridBagConstraints.HORIZONTAL;
		gbc_jSpinnerCosPhi.insets = new Insets(5, 5, 5, 5);
		gbc_jSpinnerCosPhi.gridx = 1;
		gbc_jSpinnerCosPhi.gridy = 2;
		getContentPane().add(getJSpinnerCosPhi(), gbc_jSpinnerCosPhi);
		GridBagConstraints gbc_jPanelButtons = new GridBagConstraints();
		gbc_jPanelButtons.insets = new Insets(0, 0, 0, 5);
		gbc_jPanelButtons.gridwidth = 3;
		gbc_jPanelButtons.fill = GridBagConstraints.BOTH;
		gbc_jPanelButtons.gridx = 0;
		gbc_jPanelButtons.gridy = 3;
		getContentPane().add(getJPanelButtons(), gbc_jPanelButtons);
		
		this.pack();
		this.setLocationRelativeTo(null);
		
		this.setTitle("Load Profile Import Settings");
		this.setModal(true);
		this.setVisible(true);
	}
	private JLabel getJLabelStartTime() {
		if (jLabelStartTime == null) {
			jLabelStartTime = new JLabel("Start Time:");
		}
		return jLabelStartTime;
	}
	private DateTimeWidget getStartDateTimeWidget() {
		if(startDateTimeWidget == null){
			
			// Initialize with today 00:00:00
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			startDateTimeWidget = new DateTimeWidget();
			
			startDateTimeWidget = new DateTimeWidget(cal.getTime());
		}
		return startDateTimeWidget;
	}


	private JLabel getJLabelTimeStepLength() {
		if (jLabelTimeStepLength == null) {
			jLabelTimeStepLength = new JLabel("Step Size:");
		}
		return jLabelTimeStepLength;
	}
	private JTextField getJTextFieldTimeStepLength() {
		if (jTextFieldTimeStepLength == null) {
			jTextFieldTimeStepLength = new JTextField();
			jTextFieldTimeStepLength.setColumns(10);
			jTextFieldTimeStepLength.setText("1");
		}
		return jTextFieldTimeStepLength;
	}
	private TimeUnitComboBox getComboBoxTimeStepUnit() {
		if(comboBoxTimeStepUnit == null){
			comboBoxTimeStepUnit = new TimeUnitComboBox();
			comboBoxTimeStepUnit.setSelectedIndex(2);
		}
		return comboBoxTimeStepUnit;
	}

	/**
	 * Gets a {@link JPanel} containing {@link JButton}s for OK and Cancel
	 * @return A {@link JPanel} containing {@link JButton}s for OK and Cancel
	 */
	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			jPanelButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 10));
			jPanelButtons.add(getJButtonOK());
			jPanelButtons.add(getJButtonCancel());
		}
		return jPanelButtons;
	}

	/**
	 * Gets the OK button
	 * @return The OK button
	 */
	private JButton getJButtonOK() {
		if (jButtonOK == null) {
			jButtonOK = new JButton("OK");
			jButtonOK.setFont(new Font("Dialog", Font.BOLD, 12));
			jButtonOK.setForeground(new Color(0, 153, 0));
			jButtonOK.setPreferredSize(new Dimension(100, 26));
			jButtonOK.setMaximumSize(jButtonOK.getPreferredSize());
			jButtonOK.setMinimumSize(jButtonOK.getPreferredSize());
			jButtonOK.addActionListener(this);
		}
		return jButtonOK;
	}
	/**
	 * Gets the cancel button.
	 * @return The cancel button
	 */
	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton("Cancel");
			jButtonCancel.setFont(new Font("Dialog", Font.BOLD, 12));
			jButtonCancel.setForeground(new Color(153, 0, 0));
			jButtonCancel.setPreferredSize(new Dimension(100, 26));
			jButtonCancel.setMinimumSize(jButtonCancel.getPreferredSize());
			jButtonCancel.setMaximumSize(jButtonCancel.getPreferredSize());
			jButtonCancel.addActionListener(this);
		}
		return jButtonCancel;
	}
	
	/**
	 * Checks if the import has been canceled
	 * @return Canceled?
	 */
	public boolean isCanceled() {
		return canceled;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		
		if(ae.getSource() == this.getJButtonOK()){
			
			csvImporter.setStartDateTime(this.getStartDateTimeWidget().getDate());
			int durationValue = Integer.parseInt(this.getJTextFieldTimeStepLength().getText());
			long durationInMillis = UnitConverter.convertDurationToMilliseconds(durationValue, this.getComboBoxTimeStepUnit().getTimeUnit());
			csvImporter.setStateDurationMillis(durationInMillis);
			csvImporter.setCosPhi((double) this.getJSpinnerCosPhi().getValue());
			
			this.canceled = false;
			this.setVisible(false);
			
		}else if(ae.getSource() == getJButtonCancel()){
			
			this.canceled = true;
			this.setVisible(false);
			
		}
	}

	/**
	 * Gets the j label cos phi.
	 * @return the j label cos phi
	 */
	private JLabel getJLabelCosPhi() {
		if (jLabelCosPhi == null) {
			jLabelCosPhi = new JLabel("cos(Phi):");
		}
		return jLabelCosPhi;
	}
	
	/**
	 * Gets the j spinner cos phi.
	 * @return the j spinner cos phi
	 */
	private JSpinner getJSpinnerCosPhi() {
		if (jSpinnerCosPhi == null) {
			jSpinnerCosPhi = new JSpinner();
			jSpinnerCosPhi.setModel(new SpinnerNumberModel(INITIAL_COS_PHI, 0, 1, 0.01));
			
			if (this.csvImporter instanceof MediumVoltageGridScheduleImporter) {
				jSpinnerCosPhi.setEnabled(false);
				jSpinnerCosPhi.setToolTipText("CosPhi is not required for medium voltage grids");
			}
		}
		return jSpinnerCosPhi;
	}
}
