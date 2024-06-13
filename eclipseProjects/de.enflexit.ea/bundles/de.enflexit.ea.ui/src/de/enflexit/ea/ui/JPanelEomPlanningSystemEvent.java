package de.enflexit.ea.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import energy.planning.events.EomPlanningEvent.SystemEvent;
import java.awt.Color;

/**
 * The Class JPanelEomPlanningSystemEvent.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class JPanelEomPlanningSystemEvent extends JPanel {

	private static final long serialVersionUID = 2843932103355914204L;

	private SystemEvent systemEvent;
	private JLabel jLabelNetworkID;
	private JLabel jLabelEvent;
	private JScrollPane jScrollPane;
	private DefaultTableModel tableModel;
	private JTable jTableControlChanges;
	
	/**
	 * Instantiates a new eom planning system event.
	 *
	 * @param systemEvent the system event
	 */
	public JPanelEomPlanningSystemEvent(SystemEvent systemEvent) {
		this.initialize();
		this.setSystemEvent(systemEvent);
	}
	
	public void setSystemEvent(SystemEvent systemEvent) {
		this.systemEvent = systemEvent;
		this.updateView();
	}
	public SystemEvent getSystemEvent() {
		return systemEvent;
	}
	
	private void initialize() {
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		this.setLayout(gridBagLayout);
		GridBagConstraints gbc_jLabelNetworkID = new GridBagConstraints();
		gbc_jLabelNetworkID.gridwidth = 2;
		gbc_jLabelNetworkID.anchor = GridBagConstraints.WEST;
		gbc_jLabelNetworkID.insets = new Insets(3, 5, 0, 5);
		gbc_jLabelNetworkID.gridx = 0;
		gbc_jLabelNetworkID.gridy = 0;
		this.add(this.getJLabelNetworkID(), gbc_jLabelNetworkID);
		GridBagConstraints gbc_jLabelEvent = new GridBagConstraints();
		gbc_jLabelEvent.gridwidth = 2;
		gbc_jLabelEvent.anchor = GridBagConstraints.WEST;
		gbc_jLabelEvent.insets = new Insets(3, 5, 0, 5);
		gbc_jLabelEvent.gridx = 0;
		gbc_jLabelEvent.gridy = 1;
		this.add(this.getJLabelEvent(), gbc_jLabelEvent);
		GridBagConstraints gbc_jScrollPane = new GridBagConstraints();
		gbc_jScrollPane.gridwidth = 2;
		gbc_jScrollPane.fill = GridBagConstraints.BOTH;
		gbc_jScrollPane.gridx = 0;
		gbc_jScrollPane.gridy = 2;
		gbc_jScrollPane.insets = new Insets(2, 2, 2, 2);
		this.add(this.getJScrollPane(), gbc_jScrollPane);
		
		this.setMinimumSize(new Dimension(450, 120));
		this.setPreferredSize(new Dimension(450, 140));
	}
	private JLabel getJLabelNetworkID() {
		if (jLabelNetworkID == null) {
			jLabelNetworkID = new JLabel("NetworkID");
			jLabelNetworkID.setForeground(Color.BLUE);
			jLabelNetworkID.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelNetworkID;
	}
	private JLabel getJLabelEvent() {
		if (jLabelEvent == null) {
			jLabelEvent = new JLabel("EventDescription");
			jLabelEvent.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelEvent;
	}
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTableControlChanges());
		}
		return jScrollPane;
	}
	private DefaultTableModel getTableModel() {
		if (tableModel==null) {
			
			Vector<String> tbHeader = new Vector<>();
			tbHeader.add("Variable-ID");
			tbHeader.add("Description");
			tbHeader.add("Old Value");
			tbHeader.add("New Value");
			
			tableModel = new DefaultTableModel(null, tbHeader) {
				private static final long serialVersionUID = 3475791417901795845L;
				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			};
		}
		return tableModel;
	}
	private JTable getJTableControlChanges() {
		if (jTableControlChanges == null) {
			jTableControlChanges = new JTable(this.getTableModel()) {
				private static final long serialVersionUID = -838659945904950273L;
				@Override
				public TableCellRenderer getCellRenderer(int row, int column) {
					
					Object valueObject = getJTableControlChanges().getValueAt(row, column);
					if (valueObject==null || valueObject instanceof String) {
						return this.getDefaultRenderer(String.class);
					} else if (valueObject instanceof Boolean) {
						return this.getDefaultRenderer(Boolean.class);
					} else if (valueObject instanceof Integer) {
						return this.getDefaultRenderer(Integer.class);
					} else if (valueObject instanceof Double) {
						return this.getDefaultRenderer(Double.class);
					}
					return super.getCellRenderer(row, column);
				}
			};
			jTableControlChanges.setFillsViewportHeight(true);
			
			jTableControlChanges.setShowGrid(false);
			jTableControlChanges.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTableControlChanges.setRowHeight(20);
			
			jTableControlChanges.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jTableControlChanges.getTableHeader().setReorderingAllowed(false);
			jTableControlChanges.setAutoCreateRowSorter(true);
			
			TableColumnModel tcm =  jTableControlChanges.getTableHeader().getColumnModel();
			tcm.getColumn(0).setMaxWidth(120);
			
			tcm.getColumn(2).setMaxWidth(120);
			tcm.getColumn(3).setMaxWidth(120);
		}
		return jTableControlChanges;
	}
	
	/**
	 * Updates view.
	 */
	private void updateView() {
		
		SystemEvent sysEvt = this.getSystemEvent();
		
		this.getJLabelNetworkID().setText(sysEvt.getSystemDescription()==null ? "Aggregating System" : sysEvt.getSystemDescription());
		this.getJLabelEvent().setText(sysEvt.getEventDescription());
		
		// --- Clear table --------------------------------
		this.getTableModel().setRowCount(0);
		
		// --- Get variable ID's --------------------------
		boolean isUsePrevStateHashMap = false;
		List<String> variableIDList = new ArrayList<>(sysEvt.getNewStateHM().keySet());
		if (variableIDList.size()==0) {
			variableIDList = new ArrayList<>(sysEvt.getPrevStateHM().keySet());
			isUsePrevStateHashMap = true;
		}
		Collections.sort(variableIDList);

		// --- Add rows -----------------------------------
		for (String variableID : variableIDList) {
			
			Vector<Object> row = new Vector<>();
			row.add(variableID);
			row.add(sysEvt.getVariableDescription(variableID));
			if (isUsePrevStateHashMap==false) {
				// --- Regular case -----------------------
				row.add(sysEvt.getPrevStateHM().get(variableID));
				row.add(sysEvt.getNewStateHM().get(variableID));
			} else {
				// --- Initial case -----------------------
				// --- Regular case -----------------------
				row.add("-");
				row.add(sysEvt.getPrevStateHM().get(variableID));

			}
			this.getTableModel().addRow(row);
		}
		
	}
	
}
