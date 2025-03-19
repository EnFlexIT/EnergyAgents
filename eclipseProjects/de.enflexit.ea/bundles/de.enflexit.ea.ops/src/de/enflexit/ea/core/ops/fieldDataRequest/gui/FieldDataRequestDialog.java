package de.enflexit.ea.core.ops.fieldDataRequest.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import de.enflexit.common.swing.WindowSizeAndPostionController;
import de.enflexit.common.swing.WindowSizeAndPostionController.JDialogPosition;
import de.enflexit.eom.database.ScheduleListSelection.SystemStateRangeType;
import de.enflexit.eom.database.gui.ScheduleListSelectionRangePanel;
import de.enflexit.language.Language;

/**
 * A dialog for the configuration of field data requests to deployed agents.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class FieldDataRequestDialog extends JDialog implements ActionListener{

	private static final long serialVersionUID = 69938339269233471L;
	private FieldAgentSelectionPanel jPanelAgentSelection;
	private ScheduleListSelectionRangePanel jPanelRangeSelection;
	private JPanel jPanelButtons;
	private JButton jButtonRequest;
	private JButton jButtonCancel;
	
	private boolean canceled;
	
	/**
	 * Instantiates a new field data request dialog.
	 */
	public FieldDataRequestDialog(Window owner) {
		super(owner);
		this.initialize();
	}
	
	/**
	 * Initialize.
	 */
	private void initialize() {
		
		this.setTitle("Field Data Aggregation");
		
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				FieldDataRequestDialog.this.canceled = true;
				FieldDataRequestDialog.this.setVisible(false);
			}
			
		});
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		GridBagConstraints gbc_jPanelAgentSelection = new GridBagConstraints();
		gbc_jPanelAgentSelection.insets = new Insets(5, 5, 5, 5);
		gbc_jPanelAgentSelection.fill = GridBagConstraints.BOTH;
		gbc_jPanelAgentSelection.gridx = 0;
		gbc_jPanelAgentSelection.gridy = 0;
		getContentPane().add(getJPanelAgentSelection(), gbc_jPanelAgentSelection);
		GridBagConstraints gbc_jPanelScheduleDefinition = new GridBagConstraints();
		gbc_jPanelScheduleDefinition.anchor = GridBagConstraints.WEST;
		gbc_jPanelScheduleDefinition.insets = new Insets(5, 5, 5, 5);
		gbc_jPanelScheduleDefinition.fill = GridBagConstraints.VERTICAL;
		gbc_jPanelScheduleDefinition.gridx = 1;
		gbc_jPanelScheduleDefinition.gridy = 0;
		getContentPane().add(getJPanelRangeSelection(), gbc_jPanelScheduleDefinition);
		GridBagConstraints gbc_jPanelButtons = new GridBagConstraints();
		gbc_jPanelButtons.gridwidth = 2;
		gbc_jPanelButtons.insets = new Insets(10, 0, 10, 0);
		gbc_jPanelButtons.fill = GridBagConstraints.VERTICAL;
		gbc_jPanelButtons.gridx = 0;
		gbc_jPanelButtons.gridy = 1;
		getContentPane().add(getJPanelButtons(), gbc_jPanelButtons);
		
		this.setSize(800, 400);
		this.setModal(true);
		
		WindowSizeAndPostionController.setJDialogPositionOnScreen(this, JDialogPosition.ParentCenter);
	}
	
	/**
	 * Gets the j panel agent selection.
	 * @return the j panel agent selection
	 */
	private FieldAgentSelectionPanel getJPanelAgentSelection() {
		if (jPanelAgentSelection == null) {
			jPanelAgentSelection = new FieldAgentSelectionPanel();
		}
		return jPanelAgentSelection;
	}
	
	/**
	 * Gets the j panel schedule definition.
	 * @return the j panel schedule definition
	 */
	private ScheduleListSelectionRangePanel getJPanelRangeSelection() {
		if (jPanelRangeSelection == null) {
			jPanelRangeSelection = new ScheduleListSelectionRangePanel();
		}
		return jPanelRangeSelection;
	}
	
	/**
	 * Gets the j panel buttons.
	 * @return the j panel buttons
	 */
	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			GridBagLayout gbl_buttonsPanel = new GridBagLayout();
			gbl_buttonsPanel.columnWidths = new int[]{0, 0, 0};
			gbl_buttonsPanel.rowHeights = new int[]{0, 0};
			gbl_buttonsPanel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			gbl_buttonsPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			jPanelButtons.setLayout(gbl_buttonsPanel);
			GridBagConstraints gbc_jButtonOK = new GridBagConstraints();
			gbc_jButtonOK.insets = new Insets(0, 0, 0, 5);
			gbc_jButtonOK.gridx = 0;
			gbc_jButtonOK.gridy = 0;
			jPanelButtons.add(getJButtonRequest(), gbc_jButtonOK);
			GridBagConstraints gbc_jButtonCancel = new GridBagConstraints();
			gbc_jButtonCancel.gridx = 1;
			gbc_jButtonCancel.gridy = 0;
			jPanelButtons.add(getJButtonCancel(), gbc_jButtonCancel);
		}
		return jPanelButtons;
	}
	
	/**
	 * Gets the jButtonOK.
	 * @return the jButtonOK
	 */
	private JButton getJButtonRequest() {
		if (jButtonRequest == null) {
			jButtonRequest = new JButton(Language.translate("Send Request",Language.EN));
			jButtonRequest.setForeground(new Color(0, 153, 0));
			jButtonRequest.setFont(new Font("Dialog", Font.BOLD, 12));
			jButtonRequest.setMinimumSize(new Dimension(150, 28));
			jButtonRequest.setMaximumSize(new Dimension(150, 28));
			jButtonRequest.setPreferredSize(new Dimension(150, 28));
			jButtonRequest.addActionListener(this);
		}
		return jButtonRequest;
	}
	/**
	 * Gets the jButtonCancel.
	 * @return the jButtonCancel
	 */
	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton(Language.translate("Cancel",Language.EN));
			jButtonCancel.setFont(new Font("Dialog", Font.BOLD, 12));
			jButtonCancel.setForeground(new Color(153, 0, 0));
			jButtonCancel.setMinimumSize(new Dimension(150, 28));
			jButtonCancel.setMaximumSize(new Dimension(150, 28));
			jButtonCancel.setPreferredSize(new Dimension(150, 28));
			jButtonCancel.addActionListener(this);
		}
		return jButtonCancel;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource()==this.getJButtonRequest()) {
			if (this.isValidSelection()==true) {
				this.canceled = false;
				this.setVisible(false);
			} else {
				JOptionPane.showMessageDialog(this, "Please fix the following problems:\n" + this.getProblemsMessage(), "Invalid selection", JOptionPane.WARNING_MESSAGE);
			}
		} else if (ae.getSource()==this.getJButtonCancel()) {
			this.canceled = true;
			this.setVisible(false);
		}
	}

	/**
	 * @return the canceled
	 */
	public boolean isCanceled() {
		return canceled;
	}
	
	/**
	 * Gets the selected agent's IDs.
	 * @return the selected agent's IDs
	 */
	public List<String> getSelectedAgentIDs(){
		return this.getJPanelAgentSelection().getSelectedAgentIDs();
	}
	
	/**
	 * Gets the system state range type.
	 * @return the system state range type
	 */
	public SystemStateRangeType getSystemStateRangeType() {
		return this.getJPanelRangeSelection().getSystemStateRangeType();
	}
	
	/**
	 * Gets the number of states to load.
	 * @return the number of states to load
	 */
	public int getNumberOfStatesToLoad() {
		return this.getJPanelRangeSelection().getNumberOfStatesToLoad();
	}
	
	/**
	 * Gets the time range from.
	 * @return the time range from
	 */
	public long getTimeRangeFrom() {
		return this.getJPanelRangeSelection().getTimeRangeFrom();
	}
	
	/**
	 * Gets the time range to.
	 * @return the time range to
	 */
	public long getTimeRangeTo() {
		return this.getJPanelRangeSelection().getTimeRangeTo();
	}

	/**
	 * Checks if the current selection is valid.
	 * @return true, if is valid selection
	 */
	private boolean isValidSelection() {
		
		// --- Check if at least one agent is selected --------------
		int numOfAgents = this.getJPanelAgentSelection().getSelectedAgentIDs().size();
		boolean validSelection = (numOfAgents>0);
		
		// --- Additional range-related constraints -----------------
		switch(this.getSystemStateRangeType()) {
		case AllStates:
			// --- No additional constraints ------------------------
			break;
		case StartDateAndNumber:
			// --- No future dates, more than 0 states --------------
			long timeStamp = this.getTimeRangeFrom();
			int numberOfStates = this.getNumberOfStatesToLoad();
			validSelection &= (timeStamp<=System.currentTimeMillis() && numberOfStates>0);
			break;
		case TimeRange:
			// --- No future dates, start before end ----------------
			long timeStampFrom = this.getTimeRangeFrom();
			long timeStampTo = this.getTimeRangeTo();
			validSelection &= (timeStampTo<=System.currentTimeMillis() && timeStampFrom<timeStampTo);
			break;
		}
		
		return validSelection;
	}
	
	/**
	 * Gets the problems message.
	 * @return the problems message
	 */
	private String getProblemsMessage() {
		List<String> problems = new ArrayList<>();
		
		if (this.getJPanelAgentSelection().getSelectedAgentIDs().size()==0) {
			problems.add("- No agents selected");
		}
		
		switch(this.getSystemStateRangeType()) {
		case AllStates:
			break;
		case StartDateAndNumber:
			if (this.getTimeRangeFrom()>System.currentTimeMillis()) {
				problems.add("- The selected start date is in the future");
			}
			if (this.getNumberOfStatesToLoad()<=0) {
				problems.add("- The number of states is 0 or negative");
			}
			break;
		case TimeRange:
			if (this.getTimeRangeFrom()>this.getTimeRangeTo()) {
				problems.add("- The selected end date is in the future");
			}
			if (this.getTimeRangeTo()>System.currentTimeMillis()) {
				problems.add("- The selected start date after the end date");
			}
			break;
		}
		
		if (problems.isEmpty()==false) {
			return String.join("\n", problems);
		} else {
			return null;
		}
	}
}
