package de.enflexit.ea.deployment.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.controller.ui.BasicGraphGui;
import org.awb.env.networkModel.controller.ui.BasicGraphGuiJInternalFrame;
import org.awb.env.networkModel.controller.ui.BasicGraphGuiRootJSplitPane;
import org.awb.env.networkModel.controller.ui.BasicGraphGui.ToolBarType;

import agentgui.core.application.Application;
import agentgui.core.project.setup.SimulationSetup;
import agentgui.core.project.setup.SimulationSetupNotification;
import agentgui.core.project.setup.SimulationSetupNotification.SimNoteReason;
import de.enflexit.db.hibernate.gui.DatabaseSettingsPanel;
import de.enflexit.ea.core.globalDataModel.deployment.DeploymentGroup;
import de.enflexit.ea.core.globalDataModel.deployment.DeploymentSettings;
import de.enflexit.ea.core.globalDataModel.deployment.SetupExtension;
import de.enflexit.ea.core.globalDataModel.deployment.SetupExtensionListener;
import de.enflexit.ea.core.globalDataModel.deployment.SetupExtension.Changed;
import de.enflexit.ea.deployment.AgentDeployment;
import de.enflexit.ea.deployment.plugin.DeploymentPlugIn;

/**
 * This JInternlFrame displays a list of deployed agents and allows to edit it
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public class DeploymentGroupsInternalFrame extends BasicGraphGuiJInternalFrame implements ActionListener, TreeSelectionListener, Observer, SetupExtensionListener {

	private static final long serialVersionUID = -1133420845509253437L;
	
	private static final String TITLE = "Deployed Agents";
	private static final int initialWidth = 900;
	private static final int initialHeight = 500;
	
	private JPanel contentPanel;
	
	private JSplitPane splitPane;
	
	private JScrollPane scrollPane;
	private JToolBar jToolBar;
	private JButton jButtonAdd;
	private JButton jButtonRemove;
	private JButton jButtonUpdate;
	private JButton jButtonRedeploy;
	private JButton jButtonApply;
	private JButton jButtonReset;
	private DefaultMutableTreeNode deploymentGroupsTreeRoot;
	private JTree deploymentGroupsTree;
	
	private JTabbedPane deploymentSettingsTabbedPane;
	private DeploymentSettingsConfigurationPanel deploymentSettingsConfigurationPanel;
	private DatabaseSettingsPanel databaseSettingsPanel;
	
	private SetupExtension setupExtension;
	
	private boolean isEnvironmentEditor;
	
	private DefaultMutableTreeNode selectedGroupNode;
	private DeploymentGroup selectedGroup;
	private boolean settingsChanged;
	
	private SettingsChangeListener settingsChangeListener;
	
	private boolean disableChangeListener = false;
	
	/**
	 * Constructor
	 * 
	 * @param initialEntries Initial list of deployed agents
	 * @param graphController The graph controller
	 * @param isEnvironmentEditor If true, the frame is used in the environment editor, i.e. the list can be edited
	 */
	public DeploymentGroupsInternalFrame(SetupExtension setupExtension, GraphEnvironmentController graphController, boolean isEnvironmentEditor){
		super(graphController);
		this.isEnvironmentEditor = isEnvironmentEditor;
		this.graphController = graphController;
		this.setSetupExtension(setupExtension);
		this.initialize();
	}
	/** 
	 * Initialize GUI components 
	 **/
	private void initialize(){

		this.setTitle(TITLE);
		
		this.setResizable(true);
		this.setClosable(true);
		this.setMaximizable(false);
		this.setIconifiable(false);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		this.setSize(initialWidth, initialHeight);
		this.setContentPane(this.getContentPanel());
		
		this.addInternalFrameListener(new InternalFrameAdapter() {

			@Override
			public void internalFrameClosing(InternalFrameEvent e) {
				// --- Check for unsaved changes --------------------
				if (DeploymentGroupsInternalFrame.this.settingsChanged==true) {
					// --- Ask the user what to do ------------------
					if (DeploymentGroupsInternalFrame.this.handleUnsavedChanges()==true) {
						// --- Only hide the dialog if an option != cancel was selected
						DeploymentGroupsInternalFrame.this.setVisible(false);
					}
				} else {
					DeploymentGroupsInternalFrame.this.setVisible(false);
				}
			}
		});
		
		Application.getProjectFocused().addObserver(this);
	}
	
	/**
	 * Gets the content panel.
	 * @return the content panel
	 */
	private JPanel getContentPanel() {
		if (contentPanel==null) {
			contentPanel = new JPanel(new BorderLayout());
			contentPanel.add(this.getJToolBar(), BorderLayout.NORTH);
			contentPanel.add(this.getSplitPane(), BorderLayout.CENTER);
		}
		return contentPanel;
	}
	
	/**
	 * Gets the split pane.
	 * @return the split pane
	 */
	private JSplitPane getSplitPane() {
		if (splitPane==null) {
			splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			splitPane.setDividerLocation(300);
			splitPane.setLeftComponent(this.getScrollPane());
			splitPane.setRightComponent(this.getDeploymentSettingsTabbedPane());
		}
		return splitPane;
	}
	
	/**
	 * Gets the deployment settings tabbed pane.
	 * @return the deployment settings tabbed pane
	 */
	private JTabbedPane getDeploymentSettingsTabbedPane() {
		if (deploymentSettingsTabbedPane==null) {
			deploymentSettingsTabbedPane = new JTabbedPane();
			deploymentSettingsTabbedPane.add("General Settings", this.getDeploymentSettingsConfigurationPanel());
			deploymentSettingsTabbedPane.add("Database Settings", this.getDatabaseSettingsPanel());
		}
		return deploymentSettingsTabbedPane;
	}
	
	/**
	 * Gets the deployment settings configuration panel.
	 * @return the deployment settings configuration panel
	 */
	private DeploymentSettingsConfigurationPanel getDeploymentSettingsConfigurationPanel() {
		if (deploymentSettingsConfigurationPanel==null) {
			deploymentSettingsConfigurationPanel = new DeploymentSettingsConfigurationPanel(null);
			deploymentSettingsConfigurationPanel.addActionListenerToSelectionComponents(this.getSettingsChangeListener());
			deploymentSettingsConfigurationPanel.addDocumentListenerToTextFields(this.getSettingsChangeListener());
		}
		return deploymentSettingsConfigurationPanel;
	}
	
	/**
	 * Gets the database settings panel.
	 * @return the database settings panel
	 */
	private DatabaseSettingsPanel getDatabaseSettingsPanel() {
		if (databaseSettingsPanel==null) {
			databaseSettingsPanel = new DatabaseSettingsPanel(null);
			databaseSettingsPanel.addDocumentListenerToTextFields(this.getSettingsChangeListener());
		}
		return databaseSettingsPanel;
	}
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.controller.BasicGraphGuiJInternalFrame#isRemindAsLastOpenedEditor()
	 */
	@Override
	protected boolean isRemindAsLastOpenedEditor() {
		return false;
	}
	
	
	/**
	 * Gets the deployment groups tree.
	 * @return the deployment groups tree
	 */
	private JTree getDeploymentGroupsTree() {
		if (deploymentGroupsTree==null) {
			deploymentGroupsTree = new JTree(this.getDeploymentGroupsTreeRoot());
			deploymentGroupsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			deploymentGroupsTree.setCellRenderer(new DeploymentGroupTreeCellRenderEditor(this, this.graphController));
			deploymentGroupsTree.setCellEditor(new DeploymentGroupTreeCellRenderEditor(this, this.graphController));
			deploymentGroupsTree.addTreeSelectionListener(this);
			deploymentGroupsTree.setEditable(true);
		}
		return deploymentGroupsTree;
	}
	/**
	 * Gets the deployment groups tree root.
	 * @return the deployment groups tree root
	 */
	private DefaultMutableTreeNode getDeploymentGroupsTreeRoot() {
		if (deploymentGroupsTreeRoot==null) {
			String currentSetup = Application.getProjectFocused().getSimulationSetupCurrent();
			deploymentGroupsTreeRoot = new DefaultMutableTreeNode(currentSetup);
			this.rebuildDeploymentGroupsTreeModel();
		}
		return deploymentGroupsTreeRoot;
	}
	
	/**
	 * Rebuilds the tree model of the {@link DeploymentGroup}s, keeps the existing root
	 */
	private void rebuildDeploymentGroupsTreeModel() {
		this.getDeploymentGroupsTreeRoot().setUserObject(Application.getProjectFocused().getSimulationSetupCurrent());
		this.getDeploymentGroupsTreeRoot().removeAllChildren();
		
		Vector<DeploymentGroup> deploymentGroups = new Vector<DeploymentGroup>(this.setupExtension.getDeploymentGroups().values());
		for (int i=0; i<deploymentGroups.size(); i++) {
			DefaultMutableTreeNode groupNode = this.builtGroupNode(deploymentGroups.get(i));
			this.getDeploymentGroupsTreeRoot().add(groupNode);
		}
	}
	
	
	private boolean isDeploymentGroup(String nodeObject) {
		return this.getSetupExtension().getDeploymentGroups().containsKey(nodeObject);
	}
	
	/**
	 * Built a tree node for a {@link DeploymentGroup}.
	 * @param deploymentGroup the deployment group
	 * @return the default mutable tree node
	 */
	private DefaultMutableTreeNode builtGroupNode(DeploymentGroup deploymentGroup) {
		DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(deploymentGroup.getGroupID());
		List<String> agentIDs = new ArrayList<>(deploymentGroup.getAgentsHashMap().keySet());
		for (int i=0; i<agentIDs.size(); i++) {
			DefaultMutableTreeNode agentNode = new DefaultMutableTreeNode(agentIDs.get(i));
			groupNode.add(agentNode);
		}
		return groupNode;
	}
	
	/**
	 * Gets the scroll pane.
	 * @return the scroll pane
	 */
	private JScrollPane getScrollPane(){
		if (this.scrollPane==null) {
			this.scrollPane = new JScrollPane(this.getDeploymentGroupsTree());
		}
		return this.scrollPane;
	}

	/**
	 * Gets the j tool bar.
	 * @return the j tool bar
	 */
	private JToolBar getJToolBar() {
		if (jToolBar==null) {
			jToolBar = new JToolBar();
			jToolBar.setFloatable(false);
			jToolBar.add(getJButtonAdd());
			jToolBar.add(getJButtonRemove());
			jToolBar.addSeparator();
			jToolBar.add(getJButtonUpdate());
			jToolBar.add(getJButtonRedeploy());
			jToolBar.addSeparator();
			jToolBar.add(getjButtonApply());
			jToolBar.add(getjButtonReset());
		}
		return jToolBar;
	}
	
	/**
	 * Gets the j button add.
	 * @return the j button add
	 */
	private JButton getJButtonAdd() {
		if (jButtonAdd == null) {
			jButtonAdd = new JButton();
			jButtonAdd.setIcon(new ImageIcon(DeploymentGroupsInternalFrame.class.getResource(DeploymentPlugIn.ICONS_PATH + "ListPlus.png")));
			jButtonAdd.setToolTipText("Add agent");
			jButtonAdd.addActionListener(this);
			jButtonAdd.setEnabled(this.isEnvironmentEditor);
		}
		return jButtonAdd;
	}
	
	/**
	 * Gets the j button remove.
	 * @return the j button remove
	 */
	private JButton getJButtonRemove() {
		if (jButtonRemove == null) {
			jButtonRemove = new JButton();
			jButtonRemove.setIcon(new ImageIcon(DeploymentGroupsInternalFrame.class.getResource(DeploymentPlugIn.ICONS_PATH + "ListMinus.png")));
			jButtonRemove.setToolTipText("Remove agent");
			jButtonRemove.addActionListener(this);
			jButtonRemove.setEnabled(false);
		}
		return jButtonRemove;
	}
	
	
	/**
	 * Gets the j button update.
	 * @return the j button update
	 */
	private JButton getJButtonUpdate() {
		if (jButtonUpdate == null) {
			jButtonUpdate = new JButton();
			jButtonUpdate.setIcon(new ImageIcon(DeploymentGroupsInternalFrame.class.getResource(DeploymentPlugIn.ICONS_PATH + "update.png")));
			jButtonUpdate.setToolTipText("Create a project update for the selected groups");
			jButtonUpdate.addActionListener(this);
			jButtonUpdate.setEnabled(this.isEnvironmentEditor);
		}
		return jButtonUpdate;
	}
	
	/**
	 * Gets the j button redeploy.
	 * @return the j button redeploy
	 */
	private JButton getJButtonRedeploy() {
		if (jButtonRedeploy == null) {
			jButtonRedeploy = new JButton();
			jButtonRedeploy.setIcon(new ImageIcon(DeploymentGroupsInternalFrame.class.getResource(DeploymentPlugIn.ICONS_PATH + "box.png")));
			jButtonRedeploy.setToolTipText("Create a full deployment for the selected groups");
			jButtonRedeploy.addActionListener(this);
			jButtonRedeploy.setEnabled(this.isEnvironmentEditor);
		}
		return jButtonRedeploy;
	}
	
	
	private JButton getjButtonApply() {
		if (jButtonApply==null) {
			jButtonApply = new JButton();
			jButtonApply.setIcon(new ImageIcon(DeploymentGroupsInternalFrame.class.getResource(DeploymentPlugIn.ICONS_PATH + "apply.png")));
			jButtonApply.setToolTipText("Apply changes to the deployment settings");
			jButtonApply.addActionListener(this);
			jButtonApply.setEnabled(false);
		}
		return jButtonApply;
	}
	private JButton getjButtonReset() {
		if (jButtonReset==null) {
			jButtonReset = new JButton();
			jButtonReset.setIcon(new ImageIcon(DeploymentGroupsInternalFrame.class.getResource(DeploymentPlugIn.ICONS_PATH + "reset.png")));
			jButtonReset.setToolTipText("Reset changes to the deployment settings");
			jButtonReset.addActionListener(this);
			jButtonReset.setEnabled(false);
		}
		return jButtonReset;
	}
	/**
	 * Sets the deployment button state.
	 */
	private void setDeploymentButtonsState() {
		// --- Update and redeployment only makes sense if there is at least one group ------------
		int numOfGroups = this.getSetupExtension().getDeploymentGroups().size();
		this.getJButtonUpdate().setEnabled(numOfGroups>0);
		this.getJButtonRedeploy().setEnabled(numOfGroups>0);
	}

	/**
	 * Show on desktop.
	 */
	public void showOnDesktop() {
		this.setFramePosition();
		this.registerAtDesktopAndSetVisible();
	}
	
	/**
	 * Sets the position of this {@link JInternalFrame} in the bottom left of the {@link BasicGraphGui}
	 */
	public void setFramePosition() {
		
		BasicGraphGuiRootJSplitPane splitPane = this.graphControllerGUI.getBasicGraphGuiRootJSplitPane();

		int dividerLocation = splitPane.getJSplitPaneRoot().getDividerLocation() + splitPane.getJSplitPaneRoot().getDividerSize();
		JToolBar toolBarEdit = splitPane.getBasicGraphGui().getJToolBar(ToolBarType.EditControl);
		JToolBar toolBarView = splitPane.getBasicGraphGui().getJToolBar(ToolBarType.ViewControl);
		int iFramePositionX = dividerLocation;
		if(toolBarEdit != null){
			iFramePositionX += toolBarEdit.getWidth();
		}
		if(toolBarView != null){
			iFramePositionX += toolBarView.getWidth();
		}
		int iFramePositionY = splitPane.getBasicGraphGui().getHeight() - this.getHeight();
		this.setLocation(iFramePositionX, iFramePositionY);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		
		if (ae.getSource()==this.getJButtonAdd()) {
			this.addNewDeploymentGroup();
		} else if (ae.getSource()==this.getJButtonRemove()) {
			this.removeDeploymentGroupOrAgent();
		} else if (ae.getSource()==this.getJButtonUpdate()) {
			this.updateDeploymentGroups();
		} else if (ae.getSource()==this.getJButtonRedeploy()) {
			this.redeployDeploymentGroups();
		} else if (ae.getSource()==this.getjButtonApply()) {
			this.applyChanges();
		} else if (ae.getSource()==this.getjButtonReset()) {
			this.resetChanges();
		}
	}
	
	/**
	 * Creates a new deployment group.
	 */
	private void addNewDeploymentGroup() {
		AgentDeployment agentDeployment = new AgentDeployment();
		agentDeployment.deploySelectedAgents(new ArrayList<>());
		Application.getProjectFocused().setUnsaved(true);
	}
	
	/**
	 * Removes an element (group or agent) from the deployment group tree.
	 */
	private void removeDeploymentGroupOrAgent() {
		if (this.getDeploymentGroupsTree().getSelectionPath()!=null) {
			DefaultMutableTreeNode selectedTreeNode = (DefaultMutableTreeNode) this.getDeploymentGroupsTree().getSelectionPath().getLastPathComponent();
			
			if (selectedTreeNode!=null) {
				if (this.isDeploymentGroup((String) selectedTreeNode.getUserObject())) {
					// --- Remove a whole group -------------------
					this.removeDeploymentGroup(selectedTreeNode);
				} else {
					// --- Select a single agent ------------------
					this.removeAgent(selectedTreeNode);
				}
				DefaultTreeModel model = (DefaultTreeModel) this.getDeploymentGroupsTree().getModel();
				model.removeNodeFromParent(selectedTreeNode);
				this.setDeploymentButtonsState();
				
			}
		}
	}
	
	/**
	 * Removes a whole group from the deployment group tree.
	 * @param groupNode the group's tree node
	 */
	private void removeDeploymentGroup(DefaultMutableTreeNode groupNode) {
		String groupID = (String) groupNode.getUserObject();
		this.getSetupExtension().getDeploymentGroups().remove(groupID);
		Application.getProjectFocused().setUnsaved(true);
	}
	
	/**
	 * Removes a single agent from the deployment group tree.
	 * @param agentNode the agent's tree node
	 */
	private void removeAgent(DefaultMutableTreeNode agentNode) {
		
		// --- Determine the agent's ID -----------------------------
		String agentID = (String) agentNode.getUserObject();
		
		// --- Determine the agent's deployment  group --------------
		DefaultMutableTreeNode groupNode = (DefaultMutableTreeNode) agentNode.getParent();
		String groupID = (String) groupNode.getUserObject();
		DeploymentGroup parentGroup = this.getSetupExtension().getDeploymentGroups().get(groupID);
		
		// --- Remove the agent from the group's agent list ---------
		parentGroup.getAgentsHashMap().remove(agentID);
		Application.getProjectFocused().setUnsaved(true);
		
	}
	
	/**
	 * Update the selected deployment groups.
	 */
	private void updateDeploymentGroups() {

		List<DeploymentGroup> deploymentGroups = this.getSelectedDeploymentGroups();
		if (deploymentGroups.size()>0) {
			// --- Start the actual deployment --------------------------
			AgentDeployment agentDeployment = new AgentDeployment();
			agentDeployment.updateDeploymentGroups(deploymentGroups);
		}
	}
	
	/**
	 * Redeploy the selected deployment groups.
	 */
	private void redeployDeploymentGroups() {
		List<DeploymentGroup> deploymentGroups = this.getSelectedDeploymentGroups();
		if (deploymentGroups.size()>0) {
			// --- Start the actual deployment --------------------------
			AgentDeployment agentDeployment = new AgentDeployment();
			agentDeployment.redeployDeploymentGroups(deploymentGroups);
		}
	}
	
	/**
	 * Determines which deployment groups are currently selected
	 * @return the selected deployment groups
	 */
	private List<DeploymentGroup> getSelectedDeploymentGroups(){
		DefaultMutableTreeNode selectedTreeNode = null;
		if (this.getDeploymentGroupsTree().getSelectionPath()!=null) {
			selectedTreeNode = (DefaultMutableTreeNode) this.getDeploymentGroupsTree().getSelectionPath().getLastPathComponent();
		}
		
		// --- Determine which groups should be updated -------------
		List<DeploymentGroup> deploymentGroups = new ArrayList<>(); 
		if (selectedTreeNode==null || selectedTreeNode.isRoot()) {
			// --- No selection or root, update all groups ----------
			deploymentGroups.addAll(this.getSetupExtension().getDeploymentGroups().values()); 
		} else {
			//TODO Allow multiple selections
			// --- Update a single group ----------------------------
			String groupID;
			if (selectedTreeNode.isLeaf()) {
				// --- Agent selected - update the parent group -----
				groupID = (String) ((DefaultMutableTreeNode)selectedTreeNode.getParent()).getUserObject();
			} else {
				// --- Group selected -------------------------------
				groupID = (String) ((DefaultMutableTreeNode)selectedTreeNode).getUserObject();
			}
			deploymentGroups.add(this.getSetupExtension().getDeploymentGroups().get(groupID));
		}
		return deploymentGroups;
	}

	
	/* (non-Javadoc)
	 * @see hygrid.deployment.dataModel.SetupExtensionListener#setSetupExtensionChanged(hygrid.deployment.dataModel.SetupExtension.Changed)
	 */
	@Override
	public void setSetupExtensionChanged(Changed reason) {
		
		switch (reason) {
		case DEPLOYED_AGENTS_LIST:
			// --- The list in the setup extensions was changed, refresh the list model ---------------
			this.rebuildDeploymentGroupsTreeModel();
			((DefaultTreeModel)this.getDeploymentGroupsTree().getModel()).reload();
			
			// --- Set the state of the update and redeployment buttons -----------
			this.setDeploymentButtonsState();
			break;

		default:
			break;
		}
		
	}
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable observable, Object observableObject) {
		
		if (observableObject instanceof SimulationSetupNotification){
			// --- Check the type of SimulationSetupNotification ----
			SimulationSetupNotification ssn = (SimulationSetupNotification) observableObject;
			if (ssn.getUpdateReason()==SimNoteReason.SIMULATION_SETUP_LOAD) {
				// --- Get the new instance of SetupExtension -------
				SimulationSetup currentSetup = Application.getProjectFocused().getSimulationSetups().getCurrSimSetup();
				SetupExtension newSetupExtension = (SetupExtension) currentSetup.getUserRuntimeObject();
				this.setSetupExtension(newSetupExtension);
			}
		}
	}
	
	/**
	 * Gets the setup extension.
	 * @return the setup extension
	 */
	protected SetupExtension getSetupExtension() {
		return setupExtension;
	}
	/**
	 * Sets the setup extension.
	 * @param setupExtension the new setup extension
	 */
	public void setSetupExtension(SetupExtension setupExtension){
		
		// --- Remove observer from the old instance---------------------------
		if (this.setupExtension!=null) {
			this.setupExtension.removeSetupExtensionListener(this);
		}

		// -- Set the new instance and attach the observer --------------------
		this.setupExtension = setupExtension;
		this.setupExtension.addSetupExtensionListener(this);
		
		// --- Rebuild the tree model -----------------------------------------
		this.rebuildDeploymentGroupsTreeModel();
		((DefaultTreeModel)this.getDeploymentGroupsTree().getModel()).reload();
		
		// --- Set the state of the update and redeployment buttons -----------
		this.setDeploymentButtonsState();
		
	}
	@Override
	public void valueChanged(TreeSelectionEvent tse) {
		// --- Get the node for the currently selected group ------------------
		DefaultMutableTreeNode newSelectedGroupNode = (DefaultMutableTreeNode) this.getDeploymentGroupsTree().getLastSelectedPathComponent();
		if (newSelectedGroupNode!=null) {
			
			if (newSelectedGroupNode.isLeaf()) {
				newSelectedGroupNode = (DefaultMutableTreeNode) newSelectedGroupNode.getParent();
			}
			
			// --- If the selection has changed, show the new settings ------------
			if (newSelectedGroupNode!=this.selectedGroupNode) {
				
				// --- Check for unsaved changes ----------------------------------
				if (this.settingsChanged==true) {
					// --- Ask the user how to handle unsaved changes -------------
					if (this.handleUnsavedChanges()==false) {
						// --- Canceled by the user - return to the previous selection, no further processing
						this.getDeploymentGroupsTree().setSelectionPath(tse.getOldLeadSelectionPath());
						return;
					}
				}
				
				// --- Initializes the GUI components with the selected group's settings
				this.disableChangeListener = true;
				this.selectedGroup = this.getSetupExtension().getDeploymentGroups().get(newSelectedGroupNode.getUserObject());
				if (selectedGroup!=null) {
					DeploymentSettings settingsForVisualization = this.selectedGroup.getDeploymentSettings().getCopy();
					this.getDeploymentSettingsConfigurationPanel().setDeploymentSettings(settingsForVisualization);
					this.getDatabaseSettingsPanel().setDatabaseSettings(settingsForVisualization.getDatabaseSettings());
				} else {
					this.getDeploymentSettingsConfigurationPanel().setDeploymentSettings(null);
					this.getDatabaseSettingsPanel().setDatabaseSettings(null);
				}
				this.selectedGroupNode = newSelectedGroupNode;
				this.setGroupSettingsChanged(false);
				this.disableChangeListener = false;
				
				this.getJButtonRemove().setEnabled(newSelectedGroupNode!=this.getDeploymentGroupsTreeRoot());
			}
		} else {
			// --- No tree node selected, clear the inputs ----------
			this.getDeploymentSettingsConfigurationPanel().setDeploymentSettings(null);
			this.getDatabaseSettingsPanel().setDatabaseSettings(null);
			this.setGroupSettingsChanged(false);
			this.selectedGroupNode = null;
			
			// --- Disable remove button if nothing is selected -----
			this.getJButtonRemove().setEnabled(false);
		}
	}
	
	/**
	 * Ask the user how to handle unsaved changes.
	 * @return false if cancel was selected, true otherwise
	 */
	private boolean handleUnsavedChanges() {
		int dialogReturnValue = this.showUnsavedChangesDialog();
		if (dialogReturnValue==JOptionPane.YES_OPTION) {
			this.applyChanges();
		} else if (dialogReturnValue==JOptionPane.NO_OPTION) {
			this.resetChanges();
		}
		return (dialogReturnValue!=JOptionPane.CANCEL_OPTION);
	}
	
	/**
	 * Apply changes to the deployment settings.
	 */
	private void applyChanges() {
		DeploymentSettings newSettings = this.getDeploymentSettingsConfigurationPanel().getDeploymentSettings();
		newSettings.setDatabaseSettings(this.getDatabaseSettingsPanel().getDatabaseSettings());
		this.selectedGroup.setDeploymentSettings(newSettings);
		this.setGroupSettingsChanged(false);
		Application.getProjectFocused().setUnsaved(true);
	}
	
	/**
	 * Reset changes to the deployment settings.
	 */
	private void resetChanges() {
		// --- Restore the original values ----------------
		DeploymentSettings settingsForVisualization = this.selectedGroup.getDeploymentSettings().getCopy();
		this.getDeploymentSettingsConfigurationPanel().setDeploymentSettings(settingsForVisualization);
		this.getDatabaseSettingsPanel().setDatabaseSettings(settingsForVisualization.getDatabaseSettings());
		this.setGroupSettingsChanged(false);
	}
	
	/**
	 * Show a dialog to ask the user how to handle unsaved changes.
	 * @return the user's choice (yes=apply, no=reset, cancel=cancel)
	 */
	private int showUnsavedChangesDialog() {
		String title = "Apply changes?";
		String message = "Your changes to the deployment settings for group " + this.selectedGroup.getGroupID() + " have not been applied yet.";
		String[] options = new String[3];
		options[0] = "Apply";
		options[1] = "Reset";
		options[2] = "Cancel";
		int returnValue = JOptionPane.showOptionDialog(this, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
		return returnValue;
	}
	
	/**
	 * Gets the settings change listener.
	 * @return the settings change listener
	 */
	private SettingsChangeListener getSettingsChangeListener() {
		if (settingsChangeListener==null) {
			settingsChangeListener = new SettingsChangeListener();
		}
		return settingsChangeListener;
	}

	/**
	 * Sets the settings changed.
	 * @param settingsChanged the new settings changed
	 */
	private void setGroupSettingsChanged(boolean settingsChanged) {
		this.settingsChanged = settingsChanged;
		this.getjButtonApply().setEnabled(settingsChanged);
		this.getjButtonReset().setEnabled(settingsChanged);
	}
	

	/**
	 * Simple listener to detect changes in the configured deployment settings 
	 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
	 */
	private class SettingsChangeListener implements ActionListener, DocumentListener{

		@Override
		public void insertUpdate(DocumentEvent e) {
			this.checkForChanges();			
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			this.checkForChanges();			
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			this.checkForChanges();			
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			this.checkForChanges();			
		}
		
		/**
		 * Check for changes in the configured deployment settings.
		 */
		private void checkForChanges() {
			if (DeploymentGroupsInternalFrame.this.disableChangeListener==false) {
				if (DeploymentGroupsInternalFrame.this.settingsChanged==false) {
					DeploymentSettings deploymentSettingsFromForm = DeploymentGroupsInternalFrame.this.getDeploymentSettingsConfigurationPanel().getDeploymentSettingsFromForm();
					if (DeploymentGroupsInternalFrame.this.selectedGroup.getDeploymentSettings().equals(deploymentSettingsFromForm)==false) {
						DeploymentGroupsInternalFrame.this.setGroupSettingsChanged(true);
					}
				}
			}
		}
		
	}
	
}
