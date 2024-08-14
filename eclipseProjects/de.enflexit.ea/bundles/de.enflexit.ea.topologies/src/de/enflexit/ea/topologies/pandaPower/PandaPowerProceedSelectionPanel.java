package de.enflexit.ea.topologies.pandaPower;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JButton;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import energy.schedule.loading.ScheduleTimeRange;
import energy.schedule.loading.gui.ScheduleTimeRangeSelectionPanel;

public class PandaPowerProceedSelectionPanel extends JPanel {
	
	private static final long serialVersionUID = -5208144521402665174L;
	
	private boolean canceled;
	
	private JLabel jLabelExplain;
	private JButton jButtonProceed;
	private JButton jButtonCancel;
	private ScheduleTimeRangeSelectionPanel scheduleTimeRangeSelectionPanel;
	
	
	public PandaPowerProceedSelectionPanel() {
		this.initialize();
	}
	
	public boolean isCanceled() {
		return canceled;
	}
	
	private void initialize() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		GridBagConstraints gbc_jLabelExplain = new GridBagConstraints();
		gbc_jLabelExplain.anchor = GridBagConstraints.WEST;
		gbc_jLabelExplain.insets = new Insets(5, 10, 5, 5);
		gbc_jLabelExplain.gridx = 0;
		gbc_jLabelExplain.gridy = 0;
		add(getJLabelExplain(), gbc_jLabelExplain);
		GridBagConstraints gbc_scheduleTimeRangeSelectionPanel = new GridBagConstraints();
		gbc_scheduleTimeRangeSelectionPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_scheduleTimeRangeSelectionPanel.insets = new Insets(5, 10, 10, 5);
		gbc_scheduleTimeRangeSelectionPanel.gridx = 0;
		gbc_scheduleTimeRangeSelectionPanel.gridy = 1;
		add(getScheduleTimeRangeSelectionPanel(), gbc_scheduleTimeRangeSelectionPanel);
		GridBagConstraints gbc_jButtonCancel = new GridBagConstraints();
		gbc_jButtonCancel.anchor = GridBagConstraints.SOUTH;
		gbc_jButtonCancel.insets = new Insets(5, 10, 10, 5);
		gbc_jButtonCancel.gridx = 1;
		gbc_jButtonCancel.gridy = 1;
		add(getJButtonCancel(), gbc_jButtonCancel);
		GridBagConstraints gbc_jButtonProceed = new GridBagConstraints();
		gbc_jButtonProceed.anchor = GridBagConstraints.SOUTH;
		gbc_jButtonProceed.insets = new Insets(5, 10, 10, 10);
		gbc_jButtonProceed.gridx = 2;
		gbc_jButtonProceed.gridy = 1;
		add(getJButtonProceed(), gbc_jButtonProceed);
	}

	private JLabel getJLabelExplain() {
		if (jLabelExplain == null) {
			jLabelExplain = new JLabel("Please, select the node (" + PandaPowerFileStore.PANDA_Bus + ") or the load (" + PandaPowerFileStore.PANDA_Load + ") that is to be imported.");
			jLabelExplain.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelExplain;
	}
	
	private ScheduleTimeRangeSelectionPanel getScheduleTimeRangeSelectionPanel() {
		if (scheduleTimeRangeSelectionPanel == null) {
			scheduleTimeRangeSelectionPanel = new ScheduleTimeRangeSelectionPanel();
		}
		return scheduleTimeRangeSelectionPanel;
	}
	public ScheduleTimeRange getScheduleTimeRange() {
		return this.getScheduleTimeRangeSelectionPanel().getScheduleTimeRange();
	}
	public void setScheduleTimeRange(ScheduleTimeRange str) {
		this.getScheduleTimeRangeSelectionPanel().setScheduleTimeRange(str);
	}
	
	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton("Cancel");
			jButtonCancel.setFont(new Font("Dialog", Font.BOLD, 12));
			jButtonCancel.setForeground(new Color(183, 0, 0));
			jButtonCancel.setPreferredSize(new Dimension(90, 26));
			jButtonCancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					PandaPowerProceedSelectionPanel.this.canceled = true;
					PandaPowerProceedSelectionPanel.this.getJButtonProceed().doClick();
				}
			});
		}
		return jButtonCancel;
	}
	private JButton getJButtonProceed() {
		if (jButtonProceed == null) {
			jButtonProceed = new JButton("Proceed");
			jButtonProceed.setFont(new Font("Dialog", Font.BOLD, 12));
			jButtonProceed.setForeground(new Color (0, 183, 0));
			jButtonProceed.setPreferredSize(new Dimension(90, 26));
		}
		return jButtonProceed;
	}
	/**
	 * Can be used to register an action listener to JButton of the panel. 
	 * @param listener the action listener
	 */
	public void addActionListener(ActionListener listener) {
		this.getJButtonProceed().addActionListener(listener);
	}
	
}
