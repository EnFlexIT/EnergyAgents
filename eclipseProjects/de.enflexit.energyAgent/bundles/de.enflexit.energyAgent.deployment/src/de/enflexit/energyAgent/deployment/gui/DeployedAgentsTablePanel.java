package de.enflexit.energyAgent.deployment.gui;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JButton;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.controller.GraphEnvironmentController;

import agentgui.core.config.GlobalInfo;
import de.enflexit.energyAgent.core.globalDataModel.deployment.AgentDeploymentInformation;
import de.enflexit.energyAgent.core.globalDataModel.deployment.AgentOperatingMode;

/**
 * A table to display and edit deployed agents. 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class DeployedAgentsTablePanel extends JPanel implements ActionListener, TableModelListener{
	
	private static final long serialVersionUID = -2798105029407170990L;

	private static final int COLUMN_INDEX_AGENT_ID = 0;
	private static final int COLUMN_INDEX_OPERATING_MODE = 3;
	
	private GraphEnvironmentController graphController;
	
	private JButton jButtonAdd;
	private JButton jButtonRemove;
	private JScrollPane jScrollPaneAgents;
	private JTable jTableDeployedAgents;
	private DefaultTableModel tableModel;
	
	private HashMap<String, AgentDeploymentInformation> agentDeploymentInformation;

	/**
	 * Instantiates a new deployed agents table.
	 * @param graphController the graph controller
	 */
	public DeployedAgentsTablePanel(GraphEnvironmentController graphController) {
		this.graphController = graphController;
		this.initialize();
	}
	
	/**
	 * Initialize.
	 */
	private void initialize() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		GridBagConstraints gbc_jButtonAdd = new GridBagConstraints();
		gbc_jButtonAdd.fill = GridBagConstraints.BOTH;
		gbc_jButtonAdd.insets = new Insets(10, 0, 5, 5);
		gbc_jButtonAdd.gridx = 1;
		gbc_jButtonAdd.gridy = 0;
		add(getJButtonAdd(), gbc_jButtonAdd);
		GridBagConstraints gbc_jButtonRemove = new GridBagConstraints();
		gbc_jButtonRemove.insets = new Insets(10, 0, 5, 5);
		gbc_jButtonRemove.fill = GridBagConstraints.BOTH;
		gbc_jButtonRemove.gridx = 2;
		gbc_jButtonRemove.gridy = 0;
		add(getJButtonRemove(), gbc_jButtonRemove);
		GridBagConstraints gbc_jScrollPaneAgents = new GridBagConstraints();
		gbc_jScrollPaneAgents.gridwidth = 3;
		gbc_jScrollPaneAgents.insets = new Insets(0, 10, 5, 5);
		gbc_jScrollPaneAgents.fill = GridBagConstraints.BOTH;
		gbc_jScrollPaneAgents.gridx = 0;
		gbc_jScrollPaneAgents.gridy = 1;
		add(getJScrollPaneAgents(), gbc_jScrollPaneAgents);
	}

	/**
	 * Gets the j button add.
	 * @return the j button add
	 */
	protected JButton getJButtonAdd() {
		if (jButtonAdd == null) {
			jButtonAdd = new JButton("");
			jButtonAdd.setIcon(GlobalInfo.getInternalImageIcon("ListPlus.png"));
			jButtonAdd.setPreferredSize(new Dimension(26, 26));
			jButtonAdd.setSize(new Dimension(26, 26));
			jButtonAdd.addActionListener(this);
			jButtonAdd.setEnabled(false);
		}
		return jButtonAdd;
	}
	
	/**
	 * Gets the j button remove.
	 * @return the j button remove
	 */
	protected JButton getJButtonRemove() {
		if (jButtonRemove == null) {
			jButtonRemove = new JButton("");
			jButtonRemove.setIcon(GlobalInfo.getInternalImageIcon("ListMinus.png"));
			jButtonRemove.setPreferredSize(new Dimension(26, 26));
			jButtonRemove.setSize(new Dimension(26, 26));
			jButtonRemove.addActionListener(this);
		}
		return jButtonRemove;
	}
	
	/**
	 * Gets the j scroll pane agents.
	 * @return the j scroll pane agents
	 */
	private JScrollPane getJScrollPaneAgents() {
		if (jScrollPaneAgents == null) {
			jScrollPaneAgents = new JScrollPane();
			jScrollPaneAgents.setViewportView(getJTableDeployedAgents());
		}
		return jScrollPaneAgents;
	}
	
	/**
	 * Gets the j table agents.
	 * @return the j table agents
	 */
	protected JTable getJTableDeployedAgents() {
		if (jTableDeployedAgents == null) {
			jTableDeployedAgents = new JTable(this.getTableModel());
			jTableDeployedAgents.setFillsViewportHeight(true);
			jTableDeployedAgents.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTableDeployedAgents.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			jTableDeployedAgents.getTableHeader().setReorderingAllowed(false);
			jTableDeployedAgents.getColumnModel().getColumn(COLUMN_INDEX_OPERATING_MODE).setCellEditor(new AgentOperatingModeTableCellEditor());
			jTableDeployedAgents.setRowSorter(this.getRowSorter());
		}
		return jTableDeployedAgents;
	}

	/**
	 * Gets the table model.
	 * @return the table model
	 */
	private DefaultTableModel getTableModel() {
		if (tableModel == null) {
			Vector<String> header = new Vector<String>();
			header.add("Agent Name");
			header.add("Agent Class");
			header.add("Component Type");
			header.add("Operating Mode");
			tableModel = new DefaultTableModel(null, header) {

				private static final long serialVersionUID = -5590043891675310191L;
				
				@Override
				public boolean isCellEditable(int row, int column) {
					if (column==COLUMN_INDEX_OPERATING_MODE) {
						// --- Only the operating mode can be changed here --------------
						return true;
					}
					
					// --- Name and type are defined by the environment model -----------
					return false;
				}
				
			};
			
			tableModel.addTableModelListener(this);
		}
		return tableModel;
	}
	
	/**
	 * Creates a table row sorter that sorts by the numeric parts of the IDs.
	 * @return the table row sorter
	 */
	private TableRowSorter<TableModel> getRowSorter() {
		TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(this.getTableModel());
		rowSorter.setComparator(COLUMN_INDEX_AGENT_ID, new StringCompatratorForComponentIDs());
		
		List<SortKey> sortKeys = new Vector<>();
		sortKeys.add(new SortKey(COLUMN_INDEX_AGENT_ID, SortOrder.ASCENDING));
		rowSorter.setSortKeys(sortKeys);
		rowSorter.sort();
		
		return rowSorter;
	}
	
	/**
	 * Adds a list of agents to the table, using the default operating mode for all agents.
	 * @param networkComponents the network components
	 * @param defaultOperatingMode the default operating mode
	 */
	protected void addAllAgents(List<NetworkComponent> networkComponents, AgentOperatingMode defaultOperatingMode) {
		for (int i=0; i<networkComponents.size(); i++) {
			NetworkComponent netComp = networkComponents.get(i);
			this.addAgent(netComp, defaultOperatingMode);
		}
	}
	
	/**
	 * Adds the agent to list.
	 *
	 * @param networkComponent the network component
	 * @param agentClassName the agent class name
	 * @param operatingMode the operating mode
	 */
	protected void addAgent(NetworkComponent networkComponent, AgentOperatingMode operatingMode) {
		String agentClassName = this.graphController.getNetworkModel().getAgentClassName(networkComponent);
		this.addAgent(new AgentDeploymentInformation(networkComponent, agentClassName, operatingMode));
	}
	
	/**
	 * Adds the agent to list.
	 * @param deployedAgent the deployed agent
	 */
	protected void addAgent(AgentDeploymentInformation deployedAgent) {
		// --- Add the agent to the internal list -------------------
		this.getAgentDeploymentInformationInternal().put(deployedAgent.getAgentID(), deployedAgent);
		
		// --- Create and add a table row for the agent -------------
		Vector<Object> rowData = new Vector<>();
		rowData.add(deployedAgent.getAgentID());
		rowData.add(deployedAgent.getAgentClassName());
		rowData.add(deployedAgent.getComponentType());
		rowData.add(deployedAgent.getAgentOperatingMode());
		this.getTableModel().addRow(rowData);
	}
	
	/**
	 * Removes the agent from list.
	 * @param rowIndex the row index
	 */
	protected void removeAgentFromList(int rowIndex) {
		String id = (String) this.getJTableDeployedAgents().getValueAt(rowIndex, COLUMN_INDEX_AGENT_ID);
		this.getAgentDeploymentInformationInternal().remove(id);
		this.getTableModel().removeRow(rowIndex);
	}

	/**
	 * Gets the deployed agents hash map.
	 * @return the deployed agents hash map
	 */
	private HashMap<String, AgentDeploymentInformation> getAgentDeploymentInformationInternal() {
		if (agentDeploymentInformation==null) {
			agentDeploymentInformation = new HashMap<>();
		}
		return agentDeploymentInformation;
	}
	
	public List<AgentDeploymentInformation> getAgentDeploymentInformation(){
		return new ArrayList<>(this.getAgentDeploymentInformationInternal().values());
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// --- jButtonAdd is handled by the parent AgentDeploymentDialog ------
		if (e.getSource()==this.getJButtonRemove()) {
			// --- Remove the selected agents from the table ---------
			if (this.getJTableDeployedAgents().getSelectedRowCount()>0) {
				int[] selectedRows = this.getJTableDeployedAgents().getSelectedRows();
				// --- Start with the last selected row to prevent problems with changing indices
				for (int i=selectedRows.length-1; i>=0; i--) {
					int rowIndexModel = this.getJTableDeployedAgents().convertRowIndexToModel(selectedRows[i]);
					String agentID = (String) this.getTableModel().getValueAt(rowIndexModel, COLUMN_INDEX_AGENT_ID);
					this.getAgentDeploymentInformationInternal().remove(agentID);
					this.getTableModel().removeRow(rowIndexModel);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
	 */
	@Override
	public void tableChanged(TableModelEvent tme) {
		if (tme.getSource() == this.getTableModel()) {
			if (tme.getType() == TableModelEvent.UPDATE && tme.getColumn()==COLUMN_INDEX_OPERATING_MODE) {
				// --- The AgentOperatingMode was changed -----------
				int rowIndex = tme.getFirstRow();
				String agentID = (String) this.getTableModel().getValueAt(rowIndex, COLUMN_INDEX_AGENT_ID);
				AgentOperatingMode agentOperatingMode = (AgentOperatingMode) this.getTableModel().getValueAt(rowIndex, COLUMN_INDEX_OPERATING_MODE);
				AgentDeploymentInformation agentDefinition = this.getAgentDeploymentInformationInternal().get(agentID);
				agentDefinition.setAgentOperatingMode(agentOperatingMode);
			}
		}
	}
	
	/**
	 * Gets the selected component IDs.
	 * @return the selected component IDs
	 */
	protected Vector<String> getSelectedComponentIDs(){
		Vector<String> selectedComponentIDs = new Vector<>();
		for (int i=0; i<this.getTableModel().getRowCount(); i++) {
			selectedComponentIDs.add((String) this.getTableModel().getValueAt(i, 0));
		}
		return selectedComponentIDs;
	}
	
	/**
	 * Adds a table model listener to the JTable.
	 * @param tableModelListener the table model listener
	 */
	protected void addTableModelListener(TableModelListener tableModelListener) {
		this.getJTableDeployedAgents().getModel().addTableModelListener(tableModelListener);
	}
	
	/**
	 * Adds an external action listener to the JButtons.
	 * @param actionListener the action listener
	 */
	protected void addActionListener(ActionListener actionListener) {
		this.getJButtonAdd().addActionListener(actionListener);
		this.getJButtonRemove().addActionListener(actionListener);
	}
	
	/**
	 * Removes an external action listener from the JButtons.
	 * @param actionListener the action listener
	 */
	protected void removeActionListener(ActionListener actionListener) {
		this.getJButtonAdd().removeActionListener(actionListener);
		this.getJButtonRemove().removeActionListener(actionListener);
	}
	
	/**
	 * Removes table model listener to the JTable.
	 * @param tableModelListener the table model listener
	 */
	protected void removeTableModelListener(TableModelListener tableModelListener) {
		this.getJTableDeployedAgents().getModel().removeTableModelListener(tableModelListener);
	}
}
