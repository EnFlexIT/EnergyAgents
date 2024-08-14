package de.enflexit.ea.core.awbIntegration.plugin.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import de.enflexit.common.swing.WindowSizeAndPostionController;
import de.enflexit.common.swing.WindowSizeAndPostionController.JDialogPosition;
import energy.optionModel.gui.components.DateTimeWidget;

/**
 * Configuration dialog for simulation data export.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 *
 */
public class SimulationDataExportConfigurationDialog extends JDialog implements ActionListener{

	private static final long serialVersionUID = -6574654513109763875L;
	private DateTimeWidget dateTimeWidget;
	private JPanel jPanelButtons;
	private JButton jButtonOK;
	private JButton jButtonCancel;
	private Date initialDate = null;
	
	private boolean canceled = false;
	
	/**
	 * Constructor - Just for the use of the WindowBuilder
	 */
	@Deprecated
	public SimulationDataExportConfigurationDialog(){
		this.initialize();
	}
	
	/**
	 * Constructor
	 * @param initialTimestamp The time selection widget will be initialized with this timestamp
	 */
	public SimulationDataExportConfigurationDialog(long initialTimestamp){
		this.initialDate = new Date(initialTimestamp);
		this.initialize();
	}
	
	/**
	 * Initialize GUI components
	 */
	private void initialize(){
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		GridBagConstraints gbc_dateTimeWidget = new GridBagConstraints();
		gbc_dateTimeWidget.fill = GridBagConstraints.HORIZONTAL;
		gbc_dateTimeWidget.insets = new Insets(5, 5, 5, 0);
		gbc_dateTimeWidget.gridx = 0;
		gbc_dateTimeWidget.gridy = 0;
		getContentPane().add(getDateTimeWidget(), gbc_dateTimeWidget);
		
		GridBagConstraints gbc_jPanelButtons = new GridBagConstraints();
		gbc_jPanelButtons.fill = GridBagConstraints.BOTH;
		gbc_jPanelButtons.gridx = 0;
		gbc_jPanelButtons.gridy = 1;
		getContentPane().add(getJPanelButtons(), gbc_jPanelButtons);
		
		this.setTitle("Data Export - single step");
		this.setModal(true);
		this.pack();
		WindowSizeAndPostionController.setJDialogPositionOnScreen(this, JDialogPosition.ParentCenter);

		this.setVisible(true);
	}
	
	
	/**
	 * Gets the date time widget.
	 * @return the date time widget
	 */
	private DateTimeWidget getDateTimeWidget() {
		if(dateTimeWidget == null){
			
			// Initialize with today 00:00:00
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			dateTimeWidget = new DateTimeWidget(cal.getTime());
			
			if(initialDate != null){
				dateTimeWidget.setDate(initialDate);
			}
			
		}
		return dateTimeWidget;
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
	 * @return Has the import been canceled?
	 */
	public boolean isCanceled(){
		return this.canceled;
	}
	
	/**
	 * Gets the timestamp for the selected date
	 * @return The timestamo for the selected date
	 */
	public long getSelectedTimestamp(){
		return this.getDateTimeWidget().getDate().getTime();
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == this.getJButtonOK()){
			
			// --- Close dialog ------------
			this.canceled = false;
			this.setVisible(false);
			
		} else if(ae.getSource() == getJButtonCancel()) {
			
			// --- Close dialog -----------
			this.canceled = true;
			this.setVisible(false);
		}
	}

}
