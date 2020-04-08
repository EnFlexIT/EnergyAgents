package de.enflexit.energyAgent.deployment.gui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.controller.GraphEnvironmentController;

import hygrid.deployment.dataModel.AgentDeploymentInformation;
import hygrid.deployment.dataModel.DeploymentGroup;

/**
 * {@link TreeCellRenderer} implementation for visualizing the activation state of {@link DeploymentGroup}s
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class DeploymentGroupTreeCellRenderEditor extends DefaultTreeCellRenderer implements TreeCellEditor {

	private static final long serialVersionUID = -7375160340942107849L;
	
	private DeploymentGroupsInternalFrame deploymentGroupsInternalFrame;
	private GraphEnvironmentController graphController;
	
	private JTree tree;
	private DefaultMutableTreeNode treeNode;
	
	
	/**
	 * Instantiates a new deployment group tree cell renderer.
	 *
	 * @param deploymentGroupsInternalFrame the deployment groups internal frame
	 * @param graphController the graph controller
	 */
	public DeploymentGroupTreeCellRenderEditor(DeploymentGroupsInternalFrame deploymentGroupsInternalFrame, GraphEnvironmentController graphController) {
		this.deploymentGroupsInternalFrame = deploymentGroupsInternalFrame;
		this.graphController = graphController;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		this.tree = tree;
		this.treeNode = (DefaultMutableTreeNode) value;
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
		if (treeNode.isRoot()==true) {
			return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		} else {
			return this.getRendererPanel(tree, value, sel, expanded, leaf, row, hasFocus);
		}
	}
	
	/**
	 * Gets the renderer panel.
	 * @param tree the tree
	 * @param value the value
	 * @param sel the sel
	 * @param expanded the expanded
	 * @param leaf the leaf
	 * @param row the row
	 * @param hasFocus the has focus
	 * @return the renderer panel
	 */
	private JPanel getRendererPanel(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		
		JPanel rendererPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
		rendererPanel.setOpaque(false);
		
		// --- Add the check box for the current node ---------------
		JCheckBox checkBox = this.getActivationCheckBox((DefaultMutableTreeNode) value);
		rendererPanel.add(checkBox);
		
		// --- Add the description label for the current node -------
		JLabel displayLabel = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		if (this.graphController!=null) {
			// --- Get the network component ID ---------------------
			String netCompID = displayLabel.getText();
			NetworkComponent netComp = this.graphController.getNetworkModel().getNetworkComponent(netCompID);
			if (netComp!=null) {
				// --- Determine the operating mode -----------------
				String operatingMode = "?";
				DeploymentGroup dg = this.getDeploymentGroupForNode(this.treeNode);
				if (dg!=null) {
					AgentDeploymentInformation adi = dg.getAgentsHashMap().get(netCompID);
					if (adi!=null) {
						operatingMode = adi.getAgentOperatingMode().value();
					}
				}

				// --- Define the description text ------------------
				String newDisplayText = netCompID + " [" + netComp.getType() + "] - " + operatingMode;
				String newToolTipText = "Network Component: " + netCompID + ", Type: " + netComp.getType() + ", Operating Mode: " + operatingMode;
				displayLabel.setText(newDisplayText);
				// --- Define tool tip text -------------------------
				displayLabel.setToolTipText(newToolTipText);
				checkBox.setToolTipText(newToolTipText);
				rendererPanel.setToolTipText(newToolTipText);
			}
		}
		rendererPanel.add(displayLabel);
		return rendererPanel;
	}

	/**
	 * Gets the activation check box.
	 * @param currentTreeNode the current tree node
	 * @return the activation check box
	 */
	protected JCheckBox getActivationCheckBox(DefaultMutableTreeNode treeNode) {
		JCheckBox activationCheckBox = new DeploymentGroupTreeCheckBox(treeNode);
		activationCheckBox.setSelected(this.getNodeActivationState(treeNode));
		return activationCheckBox;
	}

	/**
	 * Gets the node activation state.
	 * @param treeNode the tree node
	 * @return the node activation state
	 */
	private boolean getNodeActivationState(DefaultMutableTreeNode treeNode) {
		
		boolean active = false;
		DeploymentGroup deploymentGroup = this.getDeploymentGroupForNode(treeNode);
		if (deploymentGroup!=null) {
			active = deploymentGroup.isActive();
		}
		return active;
	}
	
	/**
	 * Gets the corresponding group node for a tree node.
	 * @param treeNode the tree node
	 * @return the corresponding group node
	 */
	private DefaultMutableTreeNode getCorrespondingGroupNode(DefaultMutableTreeNode treeNode) {
		
		DefaultMutableTreeNode groupNode = null;
		if (treeNode.isLeaf()==true) {
			// --- Agent node, return the parent node ---------------
			groupNode = (DefaultMutableTreeNode) treeNode.getParent();
		} else if (treeNode.isRoot()==false) {
			// --- Group node, return the node itself ---------------
			groupNode = treeNode;
		}
		return groupNode;
	}
	
	/**
	 * Gets the deployment group for node.
	 *
	 * @param treeNode the tree node
	 * @return the deployment group for node
	 */
	private DeploymentGroup getDeploymentGroupForNode(DefaultMutableTreeNode treeNode) {
		
		String groupID = null;
		if (treeNode.isLeaf()==true) {
			DefaultMutableTreeNode groupNode = (DefaultMutableTreeNode) treeNode.getParent();
			groupID = (String) groupNode.getUserObject();
		} else if (treeNode.isRoot()==false){
			groupID = (String) treeNode.getUserObject();
		}
		return this.deploymentGroupsInternalFrame.getSetupExtension().getDeploymentGroups().get(groupID);
	}
	
	/**
	 * 
	 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
	 */
	private class DeploymentGroupTreeCheckBox extends JCheckBox{
		private static final long serialVersionUID = -3756141687439471727L;
		private DefaultMutableTreeNode treeNode;
		
		/**
		 * Instantiates a new deployment group tree check box.
		 * @param treeNode the tree node
		 */
		public DeploymentGroupTreeCheckBox(DefaultMutableTreeNode treeNode) {
			super();
			this.treeNode = treeNode;
			this.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					DeploymentGroupTreeCheckBox source = (DeploymentGroupTreeCheckBox) e.getSource();
					boolean selectionState = source.isSelected();
					DeploymentGroup deploymentGroup = getDeploymentGroupForNode(source.getTreeNode());
					deploymentGroup.setActive(selectionState);
					((DefaultTreeModel)tree.getModel()).reload(getCorrespondingGroupNode(source.getTreeNode()));
				}
			});
		}
		
		/**
		 * Gets the tree node.
		 * @return the tree node
		 */
		private DefaultMutableTreeNode getTreeNode() {
			return this.treeNode;
		}
		
	}

	// --------------------------------------------------------------
	// --- From here Cell editing methods ---------------------------
	// --------------------------------------------------------------
	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	@Override
	public Object getCellEditorValue() {
		return treeNode.getUserObject();
	}
	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeCellEditor#getTreeCellEditorComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int)
	 */
	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
		this.treeNode = (DefaultMutableTreeNode) value;
		return this.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, false);
	}
	@Override
	public boolean isCellEditable(EventObject anEvent) {
		return true;
	}
	@Override
	public boolean shouldSelectCell(EventObject anEvent) {
		return true;
	}
	@Override
	public boolean stopCellEditing() {
		return false;
	}
	@Override
	public void cancelCellEditing() { }
	@Override
	public void addCellEditorListener(CellEditorListener l) { }
	@Override
	public void removeCellEditorListener(CellEditorListener l) { }

}
