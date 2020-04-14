package de.enflexit.ea.core.ops.fieldDataRequest.gui;

import javax.swing.JPanel;

import hygrid.plugin.HyGridPlugIn;
import java.awt.GridBagLayout;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import java.awt.GridBagConstraints;
import javax.swing.JList;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.enflexit.ea.core.globalDataModel.deployment.AgentDeploymentInformation;
import de.enflexit.ea.core.globalDataModel.deployment.SetupExtension;

/**
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class FieldAgentSelectionPanel extends JPanel {

	private static final long serialVersionUID = 575837324615962258L;
	private JCheckBox jCheckBoxSelectAll;
	private JList<AgentDeploymentInformation> jListSelectAgents;
	
	private DefaultListModel<AgentDeploymentInformation> deployedAgentsListModel;
	private JScrollPane scrollPane;
	
	private boolean pauseListSelectionListener = false;
	
	private List<String> excludeComponentTypes;

	/**
	 * Instantiates a new field agent selection panel.
	 */
	public FieldAgentSelectionPanel() {
		this.intialize();
	}

	/**
	 * Intialize.
	 */
	private void intialize() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		GridBagConstraints gbc_jCheckBoxSelectAll = new GridBagConstraints();
		gbc_jCheckBoxSelectAll.insets = new Insets(10, 10, 5, 0);
		gbc_jCheckBoxSelectAll.anchor = GridBagConstraints.WEST;
		gbc_jCheckBoxSelectAll.gridx = 0;
		gbc_jCheckBoxSelectAll.gridy = 0;
		add(getJCheckBoxSelectAll(), gbc_jCheckBoxSelectAll);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(5, 10, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		add(getScrollPane(), gbc_scrollPane);
		
		// --- By default, select all agents --------------
		this.setAllSelected(true);
	}
	
	
	/**
	 * Gets the j check box select all.
	 * @return the j check box select all
	 */
	private JCheckBox getJCheckBoxSelectAll() {
		if (jCheckBoxSelectAll == null) {
			jCheckBoxSelectAll = new JCheckBox("Select all deployed agents");
			jCheckBoxSelectAll.setFont(new Font("Dialog", Font.PLAIN, 12));
			jCheckBoxSelectAll.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					pauseListSelectionListener = true;
					setAllSelected(getJCheckBoxSelectAll().isSelected());
					pauseListSelectionListener = false;
				}
			});
			jCheckBoxSelectAll.setSelected(true);
		}
		return jCheckBoxSelectAll;
	}
	
	/**
	 * Gets the j list select agents.
	 * @return the j list select agents
	 */
	private JList<AgentDeploymentInformation> getJListAgentSelection() {
		if (jListSelectAgents == null) {
			jListSelectAgents = new JList<AgentDeploymentInformation>();
			jListSelectAgents.setModel(this.getDeployedAgentsListModel());
			jListSelectAgents.setCellRenderer(new CheckBoxListCellRenderer());
			jListSelectAgents.setSelectionModel(new DefaultListSelectionModel() {
				private static final long serialVersionUID = 8444856337414000172L;
				@Override
				public void setSelectionInterval(int index0, int index1) {
					if (super.isSelectedIndex(index0)) {
						super.removeSelectionInterval(index0, index1);
					} else {
						super.addSelectionInterval(index0, index1);
					}
				}
			});
			jListSelectAgents.addListSelectionListener(new ListSelectionListener() {
				
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (pauseListSelectionListener==false) {
						getJCheckBoxSelectAll().setSelected(isAllSelected());
					}
				}
			});
		}
		return jListSelectAgents;
	}
	
	/**
	 * Checks if the whole lsit is selected
	 * @return true, if is all selected
	 */
	private boolean isAllSelected() {
		for (int i=0; i<this.getJListAgentSelection().getModel().getSize(); i++) {
			if (this.getJListAgentSelection().isSelectedIndex(i)==false) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Sets the selection state for the whole list
	 * @param selectionState the new all selected
	 */
	private void setAllSelected(boolean selectionState) {
		if (selectionState==true) {
			this.getJListAgentSelection().setSelectionInterval(0, this.getDeployedAgentsListModel().size()-1);
		} else {
			this.getJListAgentSelection().clearSelection();
		}
	}

	/**
	 * List of component types that should not be included in the selection list
	 * @return the exclude component types
	 */
	private List<String> getExcludeComponentTypes() {
		if (excludeComponentTypes==null) {
			excludeComponentTypes = new ArrayList<>();
			excludeComponentTypes.add("CEA");	// Exclude the CEA by default
		}
		return excludeComponentTypes;
	}

	/**
	 * Gets the deployed agents list model.
	 * @return the deployed agents list model
	 */
	private DefaultListModel<AgentDeploymentInformation> getDeployedAgentsListModel() {
		if (deployedAgentsListModel==null) {
			deployedAgentsListModel = new DefaultListModel<AgentDeploymentInformation>();
			
			List<AgentDeploymentInformation> deployedAgentsList = this.getDeployedAgents();
			if (deployedAgentsList.size()>0) {
				Collections.sort(deployedAgentsList);
				for (int i=0; i<deployedAgentsList.size(); i++) {
					AgentDeploymentInformation agentInfo = deployedAgentsList.get(i);
					if (this.getExcludeComponentTypes().contains(agentInfo.getComponentType())==false) {
						deployedAgentsListModel.addElement(deployedAgentsList.get(i));
					}
				}
			}
			
			
		}
		return deployedAgentsListModel;
	}
	
	/**
	 * Gets the deployed agents.
	 * @return the deployed agents
	 */
	private List<AgentDeploymentInformation> getDeployedAgents(){
		List<AgentDeploymentInformation> deployedAgentsList = null;
		
		HyGridPlugIn plugin = HyGridPlugIn.getInstanceForCurrentProject();
		if (plugin!=null) {
			SetupExtension setEx = plugin.getSetupExtension();
			if (setEx!=null) {
				deployedAgentsList = setEx.getDeploymentGroupsHelper().getAllDeployedAgents();
			}
		}
		
		return deployedAgentsList;
	}
	
	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
			scrollPane.setViewportView(getJListAgentSelection());
		}
		return scrollPane;
	}
	
	/**
	 * {@link ListCellRenderer} implementation based on {@link JCheckBox}.
	 *
	 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
	 */
	private class CheckBoxListCellRenderer extends JCheckBox implements ListCellRenderer<AgentDeploymentInformation> {

		private static final long serialVersionUID = 6526933726761540148L;

		/* (non-Javadoc)
		 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
		 */
		@Override
		public Component getListCellRendererComponent(JList<? extends AgentDeploymentInformation> list, AgentDeploymentInformation value, int index, boolean isSelected, boolean cellHasFocus) {
			String displayText = value.getAgentID() + " - " + value.getComponentType();
			this.setText(displayText);
			this.setSelected(isSelected);
			this.setEnabled(list.isEnabled());
			return this;
		}

	}
	
	/**
	 * Gets the IDs of the selected agents.
	 * @return the IDs of the selected agents
	 */
	public List<String> getSelectedAgentIDs(){
		List<String> selectedAgentIDs = new ArrayList<>();
		for(int i=0; i<this.getDeployedAgentsListModel().size(); i++) {
			if (this.getJListAgentSelection().isSelectedIndex(i)) {
				AgentDeploymentInformation adi = this.getDeployedAgentsListModel().getElementAt(i);
				selectedAgentIDs.add(adi.getAgentID());
			}
		}
		return selectedAgentIDs;
	}
	
	/**
	 * Adds the list selection listener.
	 * @param listener the listener
	 */
	public void addListSelectionListener(ListSelectionListener listener) {
		this.getJListAgentSelection().addListSelectionListener(listener);
	}
	
	/**
	 * Removes the list selection listener.
	 * @param listener the listener
	 */
	public void removeListSelectionListener(ListSelectionListener listener) {
		this.getJListAgentSelection().removeListSelectionListener(listener);
	} 

}
