package de.enflexit.ea.deployment;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.osgi.framework.Version;

import agentgui.core.application.Application;
import agentgui.core.application.Language;
import agentgui.core.config.BundleProperties;
import agentgui.core.project.Project;
import agentgui.core.project.transfer.ProjectExportSettings;
import agentgui.core.project.transfer.ProjectExportSettingsController;
import agentgui.core.update.ProjectRepositoryExport;
import de.enflexit.ea.core.dataModel.deployment.DeploymentGroup;
import de.enflexit.ea.core.dataModel.deployment.DeploymentSettings;
import de.enflexit.ea.core.dataModel.deployment.DeploymentSettings.DeploymentMode;
import de.enflexit.ea.deployment.gui.AgentDeploymentDialog;

/**
 * This class is responsible for managing the deployment of energy agents, i.e. collecting 
 * all required data and triggering the actual deployment process.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class AgentDeployment implements DeploymentListener {
	
	public enum DeploymentState {
		RUNNING, SUCCESSFUL, FAILED
	}
	
	private Project project;
	private AgentDeploymentController deploymentController;
	
	private HashMap<String, DeploymentState> currentDeployments;
	
	private Path repositoryLocation;
	
	/**
	 * Gets the repository location.
	 * @return the repository location
	 */
	private Path getRepositoryLocation() {
		if (repositoryLocation==null) {
			String repositoryPathString = Application.getGlobalInfo().getStringFromConfiguration(BundleProperties.DEF_LOCAL_PROJECT_REPOSITORY, null);
			repositoryLocation = new File(repositoryPathString).toPath();
		}
		return repositoryLocation;
	}


	/**
	 * Gets the project.
	 * @return the project
	 */
	private Project getProject() {
		if (project==null) {
			project = Application.getProjectFocused();
		}
		return project;
	}
	
	/**
	 * Returns the current {@link NetworkModel}.
	 * @return the network mode
	 */
	private NetworkModel getNetworkModel() {
		Project project = this.getProject();
		if (project!=null) {
			if (project.getEnvironmentController() instanceof GraphEnvironmentController) {
				GraphEnvironmentController graphController = (GraphEnvironmentController) project.getEnvironmentController();
				return graphController.getNetworkModel();
			}
		}
		return null;
	}
	
	/**
	 * Gets the deployment controller.
	 * @return the deployment controller
	 */
	private AgentDeploymentController getDeploymentController() {
		if (deploymentController == null) {
			deploymentController = new AgentDeploymentController(this.getProject());
		}
		return deploymentController;
	}


	/**
	 * Deploy selected agents.
	 * @param networkComponents the network components
	 */
	public void deploySelectedAgents(List<NetworkComponent> networkComponents) {
		
		List<NetworkComponent> activeComponents = this.removePassiveComponents(networkComponents);
		AgentDeploymentDialog deploymentDialog = new AgentDeploymentDialog(activeComponents, this.getProject());
		deploymentDialog.setVisible(true);

		if (deploymentDialog.isCanceled()==false) {
			DeploymentGroup deploymentGroup = deploymentDialog.getDeploymentGroup();
			deploymentGroup.setProjectVersion(this.getProject().getVersion().toString());
			ProjectExportSettings exportSettings = this.buildProjectExportSettings(deploymentGroup.getDeploymentSettings());
			
			File targetFile = null;
			if (deploymentGroup.getDeploymentSettings().getDeploymentMode()==DeploymentMode.FULL) {
				targetFile = this.showTargetFileSelectionDialog(deploymentGroup, exportSettings);
			} else {
				targetFile = this.getRepositoryTargetFile(deploymentGroup, exportSettings);
			}
			
			if (targetFile!=null) {
				exportSettings.setTargetFile(targetFile);
				AgentDeploymentController deploymentController = new AgentDeploymentController(this.getProject());
				deploymentController.deploySelectedAgents(deploymentGroup, exportSettings);
			}
			
		}
	}
	
	/**
	 * Removes all passive components from a list of {@link NetworkComponent}s.
	 * @param componentsList the components list
	 * @return the filtered list
	 */
	private List<NetworkComponent> removePassiveComponents(List<NetworkComponent> componentsList) {
		
		List<NetworkComponent> activeComponents = new ArrayList<>();
		// --- Remove all components with no assigned agent class from the list ---------
		for (int i=0; i<componentsList.size(); i++) {
			NetworkComponent networkComponent = componentsList.get(i);
			String agentClassName = this.getNetworkModel().getAgentClassName(networkComponent);
			if (agentClassName!=null) {
				activeComponents.add(networkComponent);
			}
		}
		return activeComponents;
	}
	
	
	/**
	 * Builds the project export settings.
	 * @param targetFolder the target directory
	 * @return the project export settings
	 */
	private ProjectExportSettings buildProjectExportSettings(DeploymentSettings deploymentSettings) {
		
		ProjectExportSettingsController exportSettingsController = new ProjectExportSettingsController(this.getProject(), this.getDeploymentController());
		
		exportSettingsController.addDefaultsToExcludeList();

		if (deploymentSettings.getDeploymentMode()==DeploymentMode.FULL) {
			// --- Export to file - full deployment including application -----
			exportSettingsController.includeInstallationPackage(deploymentSettings.getTargetOperatingSystem());
		} else {
			// --- Export to repository - update deployment , project only ----
			exportSettingsController.excludeInstallationPackage();
		}
		
		// --- Include only the current simulation setup ------------
		exportSettingsController.setIncludeAllSetups(false);
		exportSettingsController.includeSimulationSetup(this.getProject().getSimulationSetupCurrent());
		return exportSettingsController.getProjectExportSettings();
	}
	
	/**
	 * Shows a {@link JFileChooser} to select an export target file
	 * @return The export target file, null if canceled
	 */
	private File showTargetFileSelectionDialog(DeploymentGroup deploymentGroup, ProjectExportSettings exportSettings) {
		
		String initialFileName = this.getProposedFileName(deploymentGroup, exportSettings);
		Path initialFilePath = Application.getGlobalInfo().getLastSelectedFolder().toPath().resolve(initialFileName);
		
		int fcResult;
		JFileChooser fileChooser = this.getJFileChooserForFiles(initialFilePath.toFile());

		// --- Repeat until either a non-existing file is selected, overwriting an existing file is permitted, or the dialog is canceled ---
		do {
			fcResult = fileChooser.showSaveDialog(null);
			if (fcResult == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				
				// --- Append the right file suffix, if missing ---------------
				String path = selectedFile.getPath();
				String fileExtension = ((FileNameExtensionFilter) fileChooser.getFileFilter()).getExtensions()[0];
				if (path.endsWith(fileExtension)==false) {
					selectedFile = new File(path.concat('.' + fileExtension));
				}
				Application.getGlobalInfo().setLastSelectedFolder(selectedFile.getParentFile());
				if (selectedFile.exists() == false) {

					// --- File does not exist, return it ---------------------
					return selectedFile;

				} else {

					// --- File exists, ask for permission to overwrite -----
					String optionTitle = selectedFile.getName() + ": " + Language.translate("Datei überschreiben?");
					String optionMsg = Language.translate("Die Datei existiert bereits. Wollen Sie diese Datei überschreiben?");
					if (JOptionPane.showConfirmDialog(null, optionMsg, optionTitle, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						// --- Permitted ----------
						return selectedFile;
					}
				}
			}

		} while (fcResult != JFileChooser.CANCEL_OPTION);

		// --- The dialog was canceled ------------
		return null;
	}
	
	/**
	 * Creates and initialized a {@link JFileChooser} for selecting the export target
	 * @return the {@link JFileChooser}
	 */
	public JFileChooser getJFileChooserForFiles(File initialFile) {

		// --- Create and initialize the JFileChooser -------------------------
		JFileChooser jFileChooser = new JFileChooser();
		jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jFileChooser.setMultiSelectionEnabled(false);
		jFileChooser.setAcceptAllFileFilterUsed(false);
		jFileChooser.setSelectedFile(initialFile);

		// --- Remember the suffix --------------------------------------------
		String fileName = initialFile.getName();
		
		// --- Add file filters -----------------------------------------------
		List<FileNameExtensionFilter> filtersList = this.getFileNameExtensionFilters();
		for (FileNameExtensionFilter filter : filtersList) {
			jFileChooser.addChoosableFileFilter(filter);
			
			// --- Set the selected filter according to the suffix ------------
			if (fileName.endsWith(filter.getExtensions()[0])) {
				jFileChooser.setFileFilter(filter);
			}
		}

		return jFileChooser;
	}
	
	/**
	 * Gets the j file chooser for folders.
	 * @param initialFolder the initial folder
	 * @return the j file chooser for folders
	 */
	public JFileChooser getJFileChooserForFolders(File initialFolder) {
		JFileChooser jFileChooser = new JFileChooser();
		jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jFileChooser.setMultiSelectionEnabled(false);
		jFileChooser.setCurrentDirectory(initialFolder);
		return jFileChooser;
	}
	
	/**
	 * Gets the proposed file name.
	 * @param deploymentGroup the deployment group
	 * @return the proposed file name
	 */
	private String getProposedFileName(DeploymentGroup deploymentGroup, ProjectExportSettings exportSettings) {
		return this.getProposedFileName(deploymentGroup, this.getNewVersion(), exportSettings);
	}
	
	/**
	 * Gets the proposed file name.
	 * @param deploymentGroup the deployment group
	 * @param version the version
	 * @return the proposed file name
	 */
	private String getProposedFileName(DeploymentGroup deploymentGroup, Version version, ProjectExportSettings exportSettings) {

		StringBuffer fileName = new StringBuffer(Application.getProjectFocused().getProjectFolder());
		fileName.append("_" + deploymentGroup.getDeploymentSettings().getProjectTag());
		fileName.append("_" + version.toString());
		
		if (exportSettings.isIncludeInstallationPackage()==true) {
			// --- Installation package included, choose OS-specific suffix -------------
			if (exportSettings.getInstallationPackage().isForWindows()==true) {
				fileName.append(".zip");
			} else {
				fileName.append(".tar.gz");
			}
		} else {
			// --- Installation package not included, use Agent.GUI suffix --------------
			fileName.append("." + Application.getGlobalInfo().getFileEndProjectZip());
		}
		String fileNameFinal = fileName.toString().replaceAll("\\s+", "_");
		return fileNameFinal;
	}
	

	/**
	 * Provides a list of {@link FileNameExtensionFilter}s for a {@link JFileChooser} dialog. Can be overridden by
	 * subclasses to customize the list of available filters.
	 * @return The list of {@link FileNameExtensionFilter}s
	 */
	private List<FileNameExtensionFilter> getFileNameExtensionFilters() {
		List<FileNameExtensionFilter> filtersList = new ArrayList<FileNameExtensionFilter>();
		// --- Prepare file type filters -----------------------------
		String projectFileSuffix = Application.getGlobalInfo().getFileEndProjectZip();
		FileNameExtensionFilter projectFileFilter = new FileNameExtensionFilter(Language.translate("Agent.GUI Projekt-Datei") + " (*." + projectFileSuffix + ")", projectFileSuffix);
		filtersList.add(projectFileFilter);
		FileNameExtensionFilter zipFileFilter = new FileNameExtensionFilter(Language.translate("Zip-Datei") + " (*.zip)", "zip");
		filtersList.add(zipFileFilter);
		FileNameExtensionFilter tarGzFileFilter = new FileNameExtensionFilter(Language.translate("Tar.gz-Datei") + " (*.tar.gz)", "tar.gz");
		filtersList.add(tarGzFileFilter);
		return filtersList;
	}
	
	/**
	 * Update deployment groups.
	 * @param deploymentGroups the deployment groups
	 */
	public void updateDeploymentGroups(List<DeploymentGroup> deploymentGroups) {
		Version newVersion = this.getNewVersion();
		
		for (int i=0; i<deploymentGroups.size(); i++) {
			// --- Configure the deployment group -------------------
			DeploymentGroup deploymentGroup = deploymentGroups.get(i);
			deploymentGroup.getDeploymentSettings().setDeploymentMode(DeploymentMode.UPDATE);
			deploymentGroup.setProjectVersion(newVersion.toString());

			// --- Generate the export settings ---------------------
			ProjectExportSettings exportSettings = this.buildProjectExportSettings(deploymentGroup.getDeploymentSettings());
			exportSettings.setTargetFile(this.getRepositoryTargetFile(deploymentGroup, newVersion, exportSettings));
			
			// --- Perform the actual deploymet ---------------------
			AgentDeploymentController deploymentController = new AgentDeploymentController(this.getProject());
			deploymentController.addDeploymentListener(this);
			deploymentController.deploySelectedAgents(deploymentGroup, exportSettings, false);
			this.getCurrentDeployments().put(deploymentGroup.getGroupID(), DeploymentState.RUNNING);
		}
	}
	
	/**
	 * Redeploy deployment groups.
	 * @param deploymentGroups the deployment groups
	 */
	public void redeployDeploymentGroups(List<DeploymentGroup> deploymentGroups) {
		JFileChooser fileChooser = this.getJFileChooserForFolders(Application.getGlobalInfo().getLastSelectedFolder());
		if (fileChooser.showSaveDialog(null)==JFileChooser.APPROVE_OPTION) {
			Application.getGlobalInfo().setLastSelectedFolder(fileChooser.getSelectedFile());
			Path targetPath = fileChooser.getSelectedFile().toPath();
			Version newVersion = this.getNewVersion();
			
			for (int i=0; i<deploymentGroups.size(); i++) {
				// --- Configure the deployment group ---------------
				DeploymentGroup deploymentGroup = deploymentGroups.get(i);
				deploymentGroup.getDeploymentSettings().setDeploymentMode(DeploymentMode.FULL);
				deploymentGroup.setProjectVersion(newVersion.toString());
				
				// --- Generate the export settings -----------------
				ProjectExportSettings exportSettings = this.buildProjectExportSettings(deploymentGroup.getDeploymentSettings());
				// --- Generate the target file name -----------------
				String targetFileName = this.getProposedFileName(deploymentGroup, newVersion, exportSettings);
				File targetFile = targetPath.resolve(targetFileName).toFile();
				exportSettings.setTargetFile(targetFile);
				
				// --- Perform the actual deployment ----------------
				AgentDeploymentController deploymentController = new AgentDeploymentController(this.getProject());
				deploymentController.addDeploymentListener(this);
				deploymentController.deploySelectedAgents(deploymentGroup, exportSettings, false);
				this.getCurrentDeployments().put(deploymentGroup.getGroupID(), DeploymentState.RUNNING);
			}
			
		}
	}
	
	/**
	 * Gets the repository file for the specified deployment group, creating a new version number.
	 * @param deploymentGroup the deployment group
	 * @return the repository target file
	 */
	private File getRepositoryTargetFile(DeploymentGroup deploymentGroup, ProjectExportSettings exportSettings) {
		return this.getRepositoryTargetFile(deploymentGroup, this.getNewVersion(), exportSettings);
	}
	
	/**
	 * Gets the repository file for the specified deployment group and version.
	 * @param deploymentGroup the deployment group
	 * @param version the version
	 * @return the repository target file
	 */
	private File getRepositoryTargetFile(DeploymentGroup deploymentGroup, Version version, ProjectExportSettings exportSettings) {
		String proposedFileName = this.getProposedFileName(deploymentGroup, version, exportSettings);
		return  this.getRepositoryLocation().resolve(proposedFileName).toFile(); 
	}
	
	/**
	 * Creates a new version, based on the project's version but with a new qualifier 
	 * @return the new version
	 */
	private Version getNewVersion() {
		Version projectVersion = Application.getProjectFocused().getVersion();
		String deploymentQualifier = ProjectRepositoryExport.getVersionQualifierForTimeStamp(System.currentTimeMillis());
		Version deploymentVersion = new Version(projectVersion.getMajor(), projectVersion.getMinor(), projectVersion.getMicro(), deploymentQualifier);
		return deploymentVersion;
	}

	/**
	 * Gets the current deployments.
	 * @return the current deployments
	 */
	private HashMap<String, DeploymentState> getCurrentDeployments() {
		if (currentDeployments==null) {
			currentDeployments = new HashMap<>();
		}
		return currentDeployments;
	}

	/* (non-Javadoc)
	 * @see hygrid.deployment.DeploymentListener#deploymentSuccessful(java.lang.String)
	 */
	@Override
	public void deploymentSuccessful(String groupID) {
		this.getCurrentDeployments().put(groupID, DeploymentState.SUCCESSFUL);
		this.checkIfAllDone();
	}

	/* (non-Javadoc)
	 * @see hygrid.deployment.DeploymentListener#deploymentFailed(java.lang.String)
	 */
	@Override
	public void deploymentFailed(String groupID) {
		this.getCurrentDeployments().put(groupID, DeploymentState.FAILED);
		this.checkIfAllDone();
	}
	
	/**
	 * Checks if all deployments are done.
	 * @return true, if done
	 */
	private boolean allDeploymentsDone() {
		Set<DeploymentState> currentStates = new HashSet<>(this.getCurrentDeployments().values());
		return (currentStates.contains(DeploymentState.RUNNING)==false);
	}
	
	/**
	 * Checks if all deployments were successful.
	 * @return true, if successful
	 */
	private boolean allDeploymentsSuccessFull() {
		Set<DeploymentState> currentStates = new HashSet<>(this.getCurrentDeployments().values());
		return (this.allDeploymentsDone() && currentStates.contains(DeploymentState.FAILED)==false);
	}
	
	/**
	 * Gets the failed deployments.
	 * @return the failed deployments
	 */
	private List<String> getFailedDeployments(){
		List<String> allGroups = new ArrayList<String>(this.getCurrentDeployments().keySet());
		List<String> failedGroups = new ArrayList<>();
		
		for (int i=0; i<allGroups.size(); i++) {
			String group = allGroups.get(i);
			if (this.getCurrentDeployments().get(group)== DeploymentState.FAILED) {
				failedGroups.add(group);
			}
		}
		return failedGroups;
	}
	
	/**
	 * Check if all deployments are done.
	 */
	private void checkIfAllDone() {
		if (this.allDeploymentsDone()==true) {
			if (this.allDeploymentsSuccessFull()==true) {
				JOptionPane.showMessageDialog(Application.getMainWindow(), Language.translate("Alle ausgewählten Gruppen wurden erfolgreich deployt!"), Language.translate("Deployment erfolgreich"), JOptionPane.INFORMATION_MESSAGE);
			} else {
				List<String> failedDeployments = this.getFailedDeployments();
				String errorMessage = Language.translate("Das Deployment folgender Gruppen ist fehlgeschlagen") + ": " + String.join(", ", failedDeployments);
				JOptionPane.showMessageDialog(Application.getMainWindow(), errorMessage, Language.translate("Deployment Fehlgeschlagen"), JOptionPane.ERROR_MESSAGE);
			}
			this.getCurrentDeployments().clear();
		}
	}
	

}
