package de.enflexit.energyAgent.deployment.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.controller.ui.NetworkComponentTablePanel;

import agentgui.core.application.Language;
import agentgui.core.config.InstallationPackageFinder;
import agentgui.core.config.InstallationPackageFinder.InstallationPackageDescription;
import agentgui.core.project.Project;
import de.enflexit.common.crypto.KeyStoreController;
import de.enflexit.common.crypto.TrustStoreController;
import de.enflexit.common.swing.AwbBasicTabbedPaneUI;
import de.enflexit.db.hibernate.gui.DatabaseSettingsPanel;
import hygrid.deployment.dataModel.AgentDeploymentInformation;
import hygrid.deployment.dataModel.AgentOperatingMode;
import hygrid.deployment.dataModel.DeploymentGroup;
import hygrid.deployment.dataModel.DeploymentSettings;
import hygrid.deployment.dataModel.DeploymentSettings.DeploymentMode;
import hygrid.deployment.dataModel.SetupExtension;
import hygrid.env.HyGridAbstractEnvironmentModel;
import hygrid.plugin.HyGridPlugIn;

/**
 * AgentDeploymentDialog represents a Dialog that is used to enter necessary informations for the 
 * deployment of an agent.
 * 
 * @author Mohamed Amine JEDIDI <mohamedamine_jedidi@outlook.com>
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public class AgentDeploymentDialog extends JDialog implements ActionListener, ListSelectionListener, TableModelListener {

	private static final long serialVersionUID = 5679554823280587395L;
	
	private static final int TAB_INDEX_DEPLOYED_AGENTS = 0;
	private static final int TAB_INDEX_DATABASE_SETTINGS = 1;
	
	private static final String INSTALLATION_PACKAGE_WARNING_TITLE = Language.translate("Keine Installationspakete gefunden!");
	private static final String INSTALLATION_PACKAGE_WARNING_MESSAGE = Language.translate("Es wurden keine Installationspakete gefunden! Bitte überprüfen Sie ob in den Optionen das korrekte Verzeichnis eingestellt ist!");
	
	private KeyStoreController keyStoreController;
	private TrustStoreController trustStoreController;
	private JPanel contentPanel;
	
	private DeploymentSettingsConfigurationPanel deploymentSettingsConfigurationPanel;
	private JPanel deploymentSettingsConfigurationPanelTest;
	
	private JTabbedPane tabbedPane;
	private DeployedAgentsTablePanel deployedAgentsTablePanel;
	private JScrollPane jScrollPaneDatabaseSettingsPanel;
	private DatabaseSettingsPanel databaseSettingsPanel;

	private JPanel buttonsPanel;

	private JButton jButtonOK;
	private JButton jButtonCancel;
	
	private Project project;
	private DeploymentSettings deploymentSettings;
	private List<NetworkComponent> componentsToDeploy;
	
	private boolean canceled = false;
	private File targetDirectory;
	private JPanel networkComponentSelectionPanel;
	private JLabel jLabelSelectComponents;
	private NetworkComponentTablePanel networkComponentTablePanel;
	private JSeparator separator;
	
	
	/**
	 * Instantiates a new AgentDeploymentDialog.
	 * Just included for WindowBuilder compatibility, use the other constructor!
	 */
	@Deprecated
	public AgentDeploymentDialog() {
		this.initialize();
	}
	/**
	 * Instantiates a new AgentDeploymentDialog
	 * @param AgentID The ID of the agent to be deployed
	 * @param project The current project
	 */
	public AgentDeploymentDialog(List<NetworkComponent> components, Project project) {
		this(components, project, null);
	}
	
	/**
	 * Instantiates a new agent deployment dialog.
	 * @param components the components
	 * @param project the project
	 * @param deploymentSettings the deployment settings. If null, the default settings from the project will be used
	 */
	public AgentDeploymentDialog(List<NetworkComponent> components, Project project, DeploymentSettings deploymentSettings) {
		this.componentsToDeploy = components;
		this.project = project;
		if (deploymentSettings==null) {
			this.deploymentSettings = this.getDefaultDeploymentSettingsFromProject().getCopy();
		} else {
			this.deploymentSettings = deploymentSettings.getCopy();
		}
		this.initialize();
	}
	
	/**
	 * Return the current {@link GraphEnvironmentController}.
	 * @return the graph controller
	 */
	private GraphEnvironmentController getGraphEnvironmentController() {
		return (GraphEnvironmentController) this.project.getEnvironmentController();
	}
	/**
	 * Gets the default deployment settings from the project
	 * @param project The project
	 * @return The default deployment settings
	 */
	private DeploymentSettings getDefaultDeploymentSettingsFromProject() {
		HyGridAbstractEnvironmentModel abstractDM = (HyGridAbstractEnvironmentModel) this.project.getUserRuntimeObject();
		DeploymentSettings deploymentSettings = abstractDM.getDeploymentSettingsModel();
		deploymentSettings.setDeploymentMode(DeploymentMode.FULL);
		return deploymentSettings;
	}
	
	/**
	 * Initialize.
	 */
	public void initialize() {
		
		if (this.checkForInstallationPackages()==false) {
			JOptionPane.showMessageDialog(this, INSTALLATION_PACKAGE_WARNING_MESSAGE, INSTALLATION_PACKAGE_WARNING_TITLE, JOptionPane.WARNING_MESSAGE);
		}
		
		this.setContentPane(this.getContentPanel());
		this.setTitle("Agent Deployment");
		
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// --- Cancel the dialog if the close button is clicked -------
				AgentDeploymentDialog.this.cancelDialog();
			}
		});

		this.setSize(900, 800);
		this.setLocationRelativeTo(null);
		this.setModal(true);
	}
	
	/**
	 * Checks if installation packages are available.
	 * @return true, if installation packages are available
	 */
	private boolean checkForInstallationPackages() {
		InstallationPackageFinder installationPackageFinder = new InstallationPackageFinder();
		Vector<InstallationPackageDescription> installationPackages = installationPackageFinder.getInstallationPackageVector();
		return installationPackages.size()>0;
	}
	/**
	 * Gets the content panel.
	 * @return the content panel
	 */
	private JPanel getContentPanel() {
		if (contentPanel == null) {
			
			contentPanel = new JPanel();
			
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{0, 0, 0};
			gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
			gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0};
			gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
			contentPanel.setLayout(gridBagLayout);
			
			GridBagConstraints gbc_DeploymentSettingsConfigurationPanel = new GridBagConstraints();
			gbc_DeploymentSettingsConfigurationPanel.fill = GridBagConstraints.HORIZONTAL;
			gbc_DeploymentSettingsConfigurationPanel.gridx = 0;
			gbc_DeploymentSettingsConfigurationPanel.gridy = 0;
			contentPanel.add(getDeploymentSettingsConfigurationPanel(), gbc_DeploymentSettingsConfigurationPanel);
			GridBagConstraints gbc_separator = new GridBagConstraints();
			gbc_separator.fill = GridBagConstraints.VERTICAL;
			gbc_separator.insets = new Insets(10, 5, 3, 0);
			gbc_separator.gridx = 1;
			gbc_separator.gridy = 0;
			contentPanel.add(getSeparator(), gbc_separator);
			GridBagConstraints gbc_networkComponentSelectionPanel = new GridBagConstraints();
			gbc_networkComponentSelectionPanel.insets = new Insets(10, 0, 0, 5);
			gbc_networkComponentSelectionPanel.fill = GridBagConstraints.BOTH;
			gbc_networkComponentSelectionPanel.gridx = 2;
			gbc_networkComponentSelectionPanel.gridy = 0;
			contentPanel.add(getNetworkComponentSelectionPanel(), gbc_networkComponentSelectionPanel);
			
			GridBagConstraints gbc_TabbedPane = new GridBagConstraints();
			gbc_TabbedPane.gridwidth = 3;
			gbc_TabbedPane.insets = new Insets(10, 10, 10, 10);
			gbc_TabbedPane.fill = GridBagConstraints.BOTH;
			gbc_TabbedPane.gridx = 0;
			gbc_TabbedPane.gridy = 1;
			contentPanel.add(getTabbedPane(), gbc_TabbedPane);
			
			GridBagConstraints gbc_buttonsPanel = new GridBagConstraints();
			gbc_buttonsPanel.gridwidth = 3;
			gbc_buttonsPanel.insets = new Insets(0, 0, 10, 0);
			gbc_buttonsPanel.anchor = GridBagConstraints.SOUTH;
			gbc_buttonsPanel.gridx = 0;
			gbc_buttonsPanel.gridy = 2;
			contentPanel.add(getButtonsPanel(), gbc_buttonsPanel);

		}
		return contentPanel;
	}
	
	/**
	 * Gets the panel.
	 * @return the panel
	 */
	private JPanel getButtonsPanel() {
		if (buttonsPanel == null) {
			buttonsPanel = new JPanel();
			GridBagLayout gbl_buttonsPanel = new GridBagLayout();
			gbl_buttonsPanel.columnWidths = new int[]{0, 0, 0};
			gbl_buttonsPanel.rowHeights = new int[]{0, 0};
			gbl_buttonsPanel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			gbl_buttonsPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			buttonsPanel.setLayout(gbl_buttonsPanel);
			GridBagConstraints gbc_jButtonOK = new GridBagConstraints();
			gbc_jButtonOK.insets = new Insets(0, 0, 0, 5);
			gbc_jButtonOK.gridx = 0;
			gbc_jButtonOK.gridy = 0;
			buttonsPanel.add(getJButtonOK(), gbc_jButtonOK);
			GridBagConstraints gbc_jButtonCancel = new GridBagConstraints();
			gbc_jButtonCancel.gridx = 1;
			gbc_jButtonCancel.gridy = 0;
			buttonsPanel.add(getJButtonCancel(), gbc_jButtonCancel);
		}
		return buttonsPanel;
	}
	
	
	/**
	 * Gets the deployment settings configuration panel.
	 * @return the deployment settings configuration panel
	 */
	private DeploymentSettingsConfigurationPanel getDeploymentSettingsConfigurationPanel() {
		if (deploymentSettingsConfigurationPanel == null) {
			deploymentSettingsConfigurationPanel = new DeploymentSettingsConfigurationPanel(this.deploymentSettings);
		}
		return deploymentSettingsConfigurationPanel;
	}
	@SuppressWarnings("unused")
	private JPanel getDeploymentSettingsConfigurationPanelTest() {
		if (deploymentSettingsConfigurationPanelTest==null) {
			deploymentSettingsConfigurationPanelTest = new JPanel();
			deploymentSettingsConfigurationPanelTest.setPreferredSize(new Dimension(600, 300));
			deploymentSettingsConfigurationPanelTest.setMinimumSize(new Dimension(600, 300));
			deploymentSettingsConfigurationPanelTest.setMaximumSize(new Dimension(600, 300));
		}
		return deploymentSettingsConfigurationPanelTest;
	}
	/**
	 * Gets the agents and certificates tabbed pane.
	 * @return the agents and certificates tabbed pane
	 */
	private JTabbedPane getTabbedPane() {
		if (tabbedPane==null) {
			tabbedPane = new JTabbedPane();
			tabbedPane.setUI(new AwbBasicTabbedPaneUI());
			tabbedPane.setFont(new Font("Dialog", Font.BOLD, 13));
			
			tabbedPane.addTab("Deployed Agents", this.getDeployedAgentsTablePanel());
			tabbedPane.setToolTipTextAt(TAB_INDEX_DEPLOYED_AGENTS, Language.translate("Configure the agents for deployment", Language.EN));
			
			tabbedPane.addTab("Database Settings", this.getJScrollPaneDatabaseSettingsPanel());
			tabbedPane.setToolTipTextAt(TAB_INDEX_DATABASE_SETTINGS, Language.translate("Configure the database settings", Language.EN));
		}
		return tabbedPane;
	}
	
	/**
	 * Gets the deployed agents panel.
	 * @return the deployed agents panel
	 */
	private DeployedAgentsTablePanel getDeployedAgentsTablePanel() {
		if (deployedAgentsTablePanel==null) {
			deployedAgentsTablePanel = new DeployedAgentsTablePanel(this.getGraphEnvironmentController());
			AgentOperatingMode operatingMode = deploymentSettings.getDefaultAgentOperatingMode();
			if (operatingMode==null) {
				operatingMode = DeploymentSettings.DEFAULT_OPERATING_MODE;
			}
			this.deployedAgentsTablePanel.addTableModelListener(this);
			this.deployedAgentsTablePanel.addActionListener(this);
			this.deployedAgentsTablePanel.addAllAgents(this.componentsToDeploy, operatingMode);
		}
		return deployedAgentsTablePanel;
	}
	
	/**
	 * Gets the j scroll pane database settings panel.
	 * @return the j scroll pane database settings panel
	 */
	private JScrollPane getJScrollPaneDatabaseSettingsPanel() {
		if (jScrollPaneDatabaseSettingsPanel==null) {
			jScrollPaneDatabaseSettingsPanel = new JScrollPane();
			jScrollPaneDatabaseSettingsPanel.setViewportView(this.getDatabaseSettingsPanel());
		}
		return jScrollPaneDatabaseSettingsPanel;
	}
	/**
	 * Gets the database settings panel.
	 * @return the database settings panel
	 */
	private DatabaseSettingsPanel getDatabaseSettingsPanel() {
		if (databaseSettingsPanel==null) {
			databaseSettingsPanel = new DatabaseSettingsPanel(this.deploymentSettings.getDatabaseSettings());
		}
		return databaseSettingsPanel;
	}
	
	/**
	 * Gets the jButtonOK.
	 * @return the jButtonOK
	 */
	private JButton getJButtonOK() {
		if (jButtonOK == null) {
			jButtonOK = new JButton(Language.translate("  OK  ",Language.EN));
			jButtonOK.setForeground(new Color(0, 153, 0));
			jButtonOK.setFont(new Font("Dialog", Font.BOLD, 12));
			jButtonOK.setMinimumSize(new Dimension(100, 28));
			jButtonOK.setMaximumSize(new Dimension(100, 28));
			jButtonOK.setPreferredSize(new Dimension(100, 28));
			jButtonOK.addActionListener(this);
		}
		return jButtonOK;
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
			jButtonCancel.setMinimumSize(new Dimension(100, 28));
			jButtonCancel.setMaximumSize(new Dimension(100, 28));
			jButtonCancel.setPreferredSize(new Dimension(100, 28));
			jButtonCancel.addActionListener(this);
		}
		return jButtonCancel;
	}
	/**
	 * This method initializes TrustStoreController.
	 */
	protected TrustStoreController getTrustStoreController() {
		if (trustStoreController == null) {
			trustStoreController = new TrustStoreController(this);
		}
		return trustStoreController;
	}
	/**
	 * This method initializes KeyStoreController.
	 */
	protected KeyStoreController getKeyStoreController() {
		if (keyStoreController == null) {
			keyStoreController = new KeyStoreController(this);
		}
		return keyStoreController;
	}
    
    /**
     * Checks the user inputs before deploying an agent, shows a message if something is wrong
     * @param selectedMtp HTTP or HTTPS?
     * @return All requirements met?
     */
    private boolean checkUserInputs(){
    	
    	// ---------------------------------------------------------
    	// --- Check if all required fields have been filled out ---
    	// ---------------------------------------------------------
    	
    	// --- Fields required for both HTTP and HTTPS
    	boolean allRequirementsMet = this.getDeploymentSettingsConfigurationPanel().checkForRequiredInputs();
    	
    	// --- Something missing, show a message and return -----
    	if (allRequirementsMet==false) {
    		
			String msg = Language.translate("You must fill out all required fields!",Language.EN);
			String title = Language.translate("Required fields",Language.EN);
			JOptionPane.showMessageDialog(this, msg, title, JOptionPane.WARNING_MESSAGE);
			//TODO Highlight missing inputs
			return false;
    	
    	}
    	
    	
    	// --- If this point is reached, everything is fine
		return true;
    }

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		
		if (ae.getSource() == this.getJButtonOK()) {
			// --- Dialog confirmed ---------------------------------
			this.confirmDialog();
		} else if(ae.getSource()==getJButtonCancel()){
			// --- Dialog canceled ----------------------------------
			this.cancelDialog();
		} else if (ae.getSource()==this.getDeployedAgentsTablePanel().getJButtonAdd()) {
			// --- Add selected network components to the component table -----
			Vector<NetworkComponent> componentsForDeployment = this.getNetworkComponentTablePanel().getSelectedNetworkComponents();
			AgentOperatingMode operatingMode = this.getDeploymentSettingsConfigurationPanel().getSelectedOperatingMode();
			this.getDeployedAgentsTablePanel().addAllAgents(componentsForDeployment, operatingMode);
		}
	}
	
	
	/**
	 * Confirm the dialog.
	 */
	private void confirmDialog() {
		
		// --- Check the user inputs ---------
		if (this.checkUserInputs() == true){
			
			// --- Make sure all changes are applied ---------
			this.getDeploymentSettingsConfigurationPanel().setFormToDataModel();
			this.deploymentSettings.setDatabaseSettings(this.getDatabaseSettingsPanel().getDatabaseSettings());
			
			this.canceled = false;
			this.setVisible(false);
		}
	}
	
	
	/**
	 * Cancel the dialog.
	 */
	private void cancelDialog() {
		// --- Canceled - close dialog without deploying -----
		this.canceled = true;
		this.setVisible(false);
	}
	
	/**
	 * Visitor object to recursively copy directories.
	 * Based on Bill Bejeck's example code at http://codingjunkie.net/java-7-copy-move/
	 *
	 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
	 *
	 */
	public class CopyDirectoryVisitor extends SimpleFileVisitor<Path>{
		private Path fromPath;
	    private Path toPath;
	    private StandardCopyOption copyOption;
	    
	    private List<Path> skipList;
	    
	    /**
    	 * Instantiates a new copy directory visitor.
    	 *
    	 * @param fromPath the from path
    	 * @param toPath the to path
    	 * @param copyOption the copy option
    	 */
    	public CopyDirectoryVisitor(Path fromPath, Path toPath, StandardCopyOption copyOption) {
	        this.fromPath = fromPath;
	        this.toPath = toPath;
	        this.copyOption = copyOption;
	    }

	    /**
    	 * Instantiates a new copy directory visitor.
    	 *
    	 * @param fromPath the from path
    	 * @param toPath the to path
    	 */
    	public CopyDirectoryVisitor(Path fromPath, Path toPath) {
	        this(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
	    }
	    
	    /**
    	 * Instantiates a new copy directory visitor.
    	 *
    	 * @param fromPath the from path
    	 * @param toPath the to path
    	 * @param skipList the skip list
    	 */
    	public CopyDirectoryVisitor(Path fromPath, Path toPath, String[] skipList) {
	    	this(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
	    	this.skipList = new ArrayList<Path>();
	    	if(skipList != null){
		    	for(int i=0; i<skipList.length; i++){
		    		Path pathToSkip = new File(skipList[i]).toPath();
		    		this.skipList.add(pathToSkip);
		    	}
	    	}
	    }
	    
    	/* (non-Javadoc)
	     * @see java.nio.file.SimpleFileVisitor#preVisitDirectory(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
	     */
	    @Override
	    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    		
    		// --- Skip the folders contains 
	    	if(this.skipList != null){
		    	for(Path pathToSkip : skipList){
		    		if(pathToSkip.equals(dir)){
		    			return FileVisitResult.SKIP_SUBTREE;
		    		}
		    	}
	    	}
	        Path targetPath = toPath.resolve(fromPath.relativize(dir));
	        if(!Files.exists(targetPath)){
	            Files.createDirectory(targetPath);
	        }
	        return FileVisitResult.CONTINUE;
	    }

	    /* (non-Javadoc)
    	 * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
    	 */
    	@Override
	    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	    	try{
//	    		System.out.println("Copying file " + file);
	    		Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption);
	    	}catch(Exception ex){
	    		System.err.println("Error copying file " + file);
	    	}
	        return FileVisitResult.CONTINUE;
	    }
	    
	}
	
	/**
	 * Visitor object to recursively delete directories.
	 * Based on example code by Bill Bejeck at http://codingjunkie.net/java-7-copy-move/
	 *
	 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
	 *
	 */
	public class DeleteDirectoryVisitor  extends SimpleFileVisitor<Path> {

	    @Override
	    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	        Files.delete(file);
	        return FileVisitResult.CONTINUE;
	    }

	    @Override
	    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
	        if(exc == null){
	            Files.delete(dir);
	            return FileVisitResult.CONTINUE;
	        }
	        throw exc;
	    }
	}
	
	/**
	 * Gets the deployment settings.
	 *
	 * @return the deployment settings
	 */
	public DeploymentSettings getDeploymentSettings() {
		return this.deploymentSettings;
	}
	
	/**
	 * Gets the previously configured deployment group.
	 * @return the deployment group
	 */
	public DeploymentGroup getDeploymentGroup() {
		DeploymentGroup deploymentGroup = new DeploymentGroup();
		deploymentGroup.setDeploymentSettings(this.getDeploymentSettings());
		deploymentGroup.setGroupID(this.getDeploymentSettings().getProjectTag());
		deploymentGroup.addAllAgents(this.getAgentDeploymentInformation());
		return deploymentGroup;
	}
	
	/**
	 * Checks if is canceled.
	 *
	 * @return true, if is canceled
	 */
	public boolean isCanceled() {
		return canceled;
	}
	
	/**
	 * Gets the target directory.
	 * @return the target directory
	 */
	public File getTargetDirectory() {
		return targetDirectory;
	}
	
	/**
	 * Gets the agent deployment information.
	 * @return the agent deployment information
	 */
	public List<AgentDeploymentInformation> getAgentDeploymentInformation() {
		return this.getDeployedAgentsTablePanel().getAgentDeploymentInformation();
	}
	
	/**
	 * Gets the network component selection panel.
	 * @return the network component selection panel
	 */
	private JPanel getNetworkComponentSelectionPanel() {
		if (networkComponentSelectionPanel == null) {
			networkComponentSelectionPanel = new JPanel();
			GridBagLayout gbl_networkComponentSelectionPanel = new GridBagLayout();
			gbl_networkComponentSelectionPanel.columnWidths = new int[]{396, 0};
			gbl_networkComponentSelectionPanel.rowHeights = new int[]{16, 349, 0};
			gbl_networkComponentSelectionPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_networkComponentSelectionPanel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			networkComponentSelectionPanel.setLayout(gbl_networkComponentSelectionPanel);
			GridBagConstraints gbc_jLabelSelectComponents = new GridBagConstraints();
			gbc_jLabelSelectComponents.anchor = GridBagConstraints.NORTH;
			gbc_jLabelSelectComponents.fill = GridBagConstraints.HORIZONTAL;
			gbc_jLabelSelectComponents.insets = new Insets(0, 15, 5, 0);
			gbc_jLabelSelectComponents.gridx = 0;
			gbc_jLabelSelectComponents.gridy = 0;
			networkComponentSelectionPanel.add(getJLabelSelectComponents(), gbc_jLabelSelectComponents);
			GridBagConstraints gbc_networkComponentTablePanel = new GridBagConstraints();
			gbc_networkComponentTablePanel.fill = GridBagConstraints.BOTH;
			gbc_networkComponentTablePanel.gridx = 0;
			gbc_networkComponentTablePanel.gridy = 1;
			networkComponentSelectionPanel.add(this.getNetworkComponentTablePanel(), gbc_networkComponentTablePanel);
		}
		return networkComponentSelectionPanel;
	}
	
	/**
	 * Gets the j label select components.
	 * @return the j label select components
	 */
	private JLabel getJLabelSelectComponents() {
		if (jLabelSelectComponents == null) {
			jLabelSelectComponents = new JLabel("Select Network Components for Deployment");
			jLabelSelectComponents.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelSelectComponents;
	}
	
	/**
	 * Gets the network component table panel.
	 * @return the network component table panel
	 */
	private NetworkComponentTablePanel getNetworkComponentTablePanel() {
		if (networkComponentTablePanel == null) {
			networkComponentTablePanel = new NetworkComponentTablePanel(this.getGraphEnvironmentController(), false, true);
			networkComponentTablePanel.reLoadNetworkComponents();
			networkComponentTablePanel.addListSelectionListener(this);
			this.updateExcludeList();
		}
		return networkComponentTablePanel;
	}
	
	/**
	 * Gets the setup extension.
	 * @return the setup extension
	 */
	private SetupExtension getSetupExtension() {
		SetupExtension setupExtension = null;
		HyGridPlugIn plugin = HyGridPlugIn.getInstanceForCurrentProject();
		if (plugin!=null) {
			setupExtension = plugin.getSetupExtension();
		}
		return setupExtension;
	}
	
	/**
	 * Updates the exclude list for the network components table, containing currently deployed agent's IDs
	 */
	protected void updateExcludeList() {
		this.updateExcludeList(new Vector<>());
	}
	
	/**
	 * Clear the selection.
	 */
	protected void clearSelection() {
		this.getNetworkComponentTablePanel().clearSelection();
	}
	
	/**
	 * Updates the exclude list for the network components table, containing currently 
	 * deployed agent's IDs and optionally additional IDs.
	 * @param addIDs the additional IDs
	 */
	protected void updateExcludeList(Vector<String> addIDs) {

		Vector<String> excludeList = new Vector<String>(this.getSetupExtension().getDeploymentGroupsHelper().getDeployedAgentIDs());
		for (int i=0; i<addIDs.size(); i++) {
			excludeList.add(addIDs.get(i));
		}
		this.getNetworkComponentTablePanel().setExcludeList(excludeList);
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent lse) {
		
		// --- Set add button state according to component selection ---------- 
		ListSelectionModel lsm = (ListSelectionModel) lse.getSource();
		boolean addButtonEnabled = !lsm.isSelectionEmpty();
		this.getDeployedAgentsTablePanel().getJButtonAdd().setEnabled(addButtonEnabled);
	}
	
	/**
	 * Gets the separator.
	 * @return the separator
	 */
	private JSeparator getSeparator() {
		if (separator == null) {
			separator = new JSeparator();
			separator.setOrientation(SwingConstants.VERTICAL);
		}
		return separator;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
	 */
	@Override
	public void tableChanged(TableModelEvent tme) {
		if (tme.getSource()==this.getDeployedAgentsTablePanel().getJTableDeployedAgents().getModel()) {
			if (tme.getType()==TableModelEvent.INSERT || tme.getType()==TableModelEvent.DELETE) {
				Vector<String> componentsSelectedForDeployment = this.getDeployedAgentsTablePanel().getSelectedComponentIDs();
				this.updateExcludeList(componentsSelectedForDeployment);
				if (tme.getType()==TableModelEvent.INSERT) {
					this.getNetworkComponentTablePanel().clearSelection();
				}
			}
		}
	}
}
