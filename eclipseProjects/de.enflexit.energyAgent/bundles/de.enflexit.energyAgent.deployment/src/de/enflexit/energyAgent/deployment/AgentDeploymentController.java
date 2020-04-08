package de.enflexit.energyAgent.deployment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.settings.ComponentTypeSettings;
import org.awb.env.networkModel.settings.GeneralGraphSettings4MAS;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import agentgui.core.application.Application;
import agentgui.core.application.Language;
import agentgui.core.classLoadService.ClassLoadServiceUtility;
import agentgui.core.config.BundleProperties;
import agentgui.core.config.DeviceAgentDescription;
import agentgui.core.config.GlobalInfo.DeviceSystemExecutionMode;
import agentgui.core.config.GlobalInfo.EmbeddedSystemAgentVisualisation;
import agentgui.core.config.GlobalInfo.ExecutionMode;
import agentgui.core.plugin.PlugIn;
import agentgui.core.project.BundleFeatureMapper;
import agentgui.core.project.PlatformJadeConfig;
import agentgui.core.project.PlatformJadeConfig.MTP_Creation;
import agentgui.core.project.Project;
import agentgui.core.project.setup.SimulationSetup;
import agentgui.core.project.transfer.DefaultProjectExportController;
import agentgui.core.project.transfer.ProjectExportSettings;
import agentgui.core.update.repositoryModel.ProjectRepository;
import agentgui.core.update.repositoryModel.RepositoryEntry;
import agentgui.simulationService.environment.DisplaytEnvironmentModel;
import agentgui.simulationService.environment.EnvironmentModel;
import de.enflexit.common.featureEvaluation.FeatureInfo;
import de.enflexit.db.hibernate.gui.DatabaseSettings;
import de.enflexit.energyAgent.core.globalDataModel.deployment.AgentDeploymentInformation;
import de.enflexit.energyAgent.core.globalDataModel.deployment.AgentOperatingMode;
import de.enflexit.energyAgent.core.globalDataModel.deployment.DeploymentGroup;
import de.enflexit.energyAgent.core.globalDataModel.deployment.DeploymentSettings;
import de.enflexit.energyAgent.core.globalDataModel.deployment.SetupExtension;
import de.enflexit.energyAgent.core.globalDataModel.deployment.DeploymentSettings.DeploymentMode;
import hygrid.env.HyGridAbstractEnvironmentModel;
import hygrid.plugin.HyGridPlugIn;
import hygrid.plugin.HyGridProjectExportController;

/**
 * {@link DefaultProjectExportController} implementation for deploying single energy agents to be executed in embedded system mode.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class AgentDeploymentController extends HyGridProjectExportController {
	
	private static final String RESOURCES_SUBFOLDER = "ressources";
	private static final String BUNDLES_SUBFOLDER = "plugins";
	private static final String SETUPS_SUBFOLDER = "setups";
	private static final String SETUP_ENVIRONMENTS_SUBFOLDER = "setupsEnv";
	private static final String GENERAL_GRAPH_SETTINGS_FILE_NAME = "~GeneralGraphSettings~.xml";

	private static final String PREFERENCES_SUBFOLDER = ".settings";
	private static final String PREFERENCES_FILE_SUFFIX = ".prefs";
	private static final String PREFERENCES_PATH_IN_ARCHIVE_WINDOWS_LINUX = "AgentWorkbench/configuration";
	private static final String PREFERENCES_PATH_IN_ARCHIVE_MAC = "agentgui.app/Contents/Eclipse/configuration";
	
	private static final String DATABASE_PREFERENCES_FILE_NAME = "de.enflexit.eom.database.prefs";

	private DeploymentGroup deploymentGroup;
	private ProjectExportSettings exportSettings;
	
	private File projectRepositoryDirectory;
	private ProjectRepository projectRepository;
	
	private NetworkModel networkModel;
	
	private Path preferencesFolderPath;
	
	private List<Bundle> bundlesToInclude;
	private List<String> missingBundlesList;
	
	private List<DeploymentListener> deploymentListeners;
	
	/**
	 * Instantiates a new agent deployment controller.
	 * @param project the project
	 */
	public AgentDeploymentController(Project project) {
		super();
		this.setProject(project);
	}
	
	private DeploymentSettings getDeploymentSettings() {
		return this.deploymentGroup.getDeploymentSettings();
	}
	
	protected ProjectExportSettings getExportSettings() {
		return this.exportSettings;
	}
	

	/* (non-Javadoc)
	 * @see agentgui.core.project.transfer.ProjectExportController#beforeZip()
	 */
	@Override
	protected boolean beforeZip() {
		
		// --- Determine the path for the config folder and create it --------------
		Path projectFolderPath = this.getTempFolderPath();
		Path setupFolderPath = projectFolderPath.resolve(SETUPS_SUBFOLDER);
		Path setupEnvironmentPath = projectFolderPath.resolve(SETUP_ENVIRONMENTS_SUBFOLDER);
		
		// --- Adjust the preferences -------------------------------------
		if (this.getExportSettings().isIncludeInstallationPackage()==true) {
			this.configureWorkbenchPreferences();
			this.exportDatabaseProperties();
		}

		// --- Adjust the project-specific settings -----------------
		this.modifyProjectSettings(projectFolderPath.toFile());
		this.modifySimulationSetup(setupFolderPath);
		
		// --- Remove unnecessary agent classes ---------------------
		GeneralGraphSettings4MAS generalGraphSettings = this.reduceGeneralGraphSettings();
		File generalGraphSettingsFile = projectFolderPath.resolve(SETUP_ENVIRONMENTS_SUBFOLDER).resolve(GENERAL_GRAPH_SETTINGS_FILE_NAME).toFile();
		GeneralGraphSettings4MAS.save(generalGraphSettingsFile, generalGraphSettings);
		
		// --- Remove unnecessary data models -----------------------
		NetworkModel networkModelForDeployment = this.removeOtherSystemsModels(this.deploymentGroup.getAgentIDs());
		GraphEnvironmentController gec = (GraphEnvironmentController) this.getProject().getEnvironmentController();
		String componentsFileName = gec.getFileXML().getName();
		File componentsFile = setupEnvironmentPath.resolve(componentsFileName).toFile();
		networkModelForDeployment.saveComponentsFile(componentsFile);
		
		return true;
	}
	

	/* (non-Javadoc)
	 * @see agentgui.core.project.transfer.ProjectExportController#afterZip(boolean)
	 */
	@Override
	protected void afterZip(boolean success) {
		// --- Show a feedback message to the user ------------------
		
		if (success == true) {
			System.out.println(this.getClass().getSimpleName() + ": Group " + this.deploymentGroup.getGroupID() + " export successful!");
			String messageTitle = Language.translate("Export erfolgreich");
			if (this.isShowUserDialogs()==true) {
				String messageContent = Language.translate("Gruppe") + " " + this.deploymentGroup.getGroupID() + " " + Language.translate("erfolgreich exportiert!");
				JOptionPane.showMessageDialog(Application.getMainWindow(), messageContent, messageTitle, JOptionPane.INFORMATION_MESSAGE);
			}
			
			// --- Notify listeners ---------------------------------
			this.notifySuccessful();
		} else {
			System.err.println(this.getClass().getSimpleName() + ": Group " + this.deploymentGroup.getGroupID() + " export failed!");
			if (this.isShowUserDialogs()==true) {
				String message = Language.translate("Gruppe") + " " + this.deploymentGroup.getGroupID() + " " + Language.translate("Export fehlgeschlagen");
				JOptionPane.showMessageDialog(Application.getMainWindow(), message, message, JOptionPane.ERROR_MESSAGE);
			}
			this.notifyFailed();
		}
	}

	/* (non-Javadoc)
	 * @see agentgui.core.project.transfer.ProjectExportController#getProposedFileName()
	 */
	@Override
	protected String getProposedFileName(Project project) {
		
		// --- Construct the full path --------------------
		StringBuffer fullPath = new StringBuffer();
		String folder = Application.getGlobalInfo().getLastSelectedFolderAsString();
		fullPath.append(folder);
		if (folder.endsWith(File.separator) == false) {
			fullPath.append(File.separator);
		}
		
		// --- Append the file name -----------------------
		fullPath.append(this.getDeploymentSettings().getProjectTag());
		fullPath.append(this.getFileSuffix());

		return fullPath.toString();
	}
	
	
	/* (non-Javadoc)
	 * @see agentgui.core.project.transfer.DefaultProjectExportController#getTempExportFolderPath()
	 */
	@Override
	protected Path getTempExportFolderPath() {
		if (tempFolderPath == null) {

			// --- Determine the path for the temporary export folder, based on the selected target file ----
			File targetFile = this.getExportSettings().getTargetFile();
			Path containingFolder = targetFile.getParentFile().toPath();
			String tempFolderName = this.deploymentGroup.getDeploymentSettings().getProjectTag() + TEMP_FOLDER_SUFFIX;
			tempFolderPath = containingFolder.resolve(tempFolderName);
			
			// --- Replace the temp folder name with the original name when adding files to the archive -----
			this.getArchiveFileHandler().addPathReplacement(tempFolderName, this.getProject().getProjectFolder());
			
		}
		return this.tempFolderPath;
	}

	/**
	 * Determine the file suffix based on the target OS
	 * @return the file suffix
	 */
	private String getFileSuffix() {
		// --- Determine the file suffix based on the target OS ---------------
		String suffix = null;
		if (this.getExportSettings().isIncludeInstallationPackage()==true) {
			// --- Full deployment, choose suffix for the target OS -----------
			if (this.getExportSettings().getInstallationPackage().isForWindows()) {
				suffix = ".zip";
			} else {
				suffix = ".tar.gz";
			}
		} else {
			// --- Project data only, use Agent.GUI suffix --------------------
			suffix = "." + Application.getGlobalInfo().getFileEndProjectZip();
		}
		return suffix;
	}
	
	/* (non-Javadoc)
	 * @see hygrid.plugin.HyGridProjectExportController#getFolderCopySkipList(java.nio.file.Path)
	 */
	@Override
	protected List<Path> getFolderCopySkipList(Path sourcePath) {
		
		// --- Add all non-required bundle files ------------------------------
		List<Path> skipList = this.getExportSettings().getProjectExportSettingsController().getFileExcludeList();
		skipList.addAll(this.getBundlesExcludeList());
		
		return skipList;
	}
	
	
	/* (non-Javadoc)
	 * @see hygrid.plugin.HyGridProjectExportController#getDefaultExcludeList()
	 */
	@Override
	public ArrayList<Path> getDefaultExcludeList() {
		ArrayList<Path> defaultExcludeList = super.getDefaultExcludeList();
		defaultExcludeList.add(this.getProjectFolderPath().resolve(RESOURCES_SUBFOLDER));
		defaultExcludeList.remove(this.getProjectFolderPath().resolve(Project.DEFAULT_SUB_FOLDER_SECURITY));
		return defaultExcludeList;
	}

	/**
	 * Gets the bundle files exclude list, i.e. all bundle files from the project that are not required by the deployed agents
	 * @return the bundles exclude list
	 */
	private List<Path> getBundlesExcludeList(){
		List<Path> bundlesExcludeList = new ArrayList<>();
		
		this.bundlesToInclude = this.getBundlesToInclude(new ArrayList<>(deploymentGroup.getAgentClassNames()));
		List<File> requiredBundleFiles = this.getBundleJarFilesToInclude(this.bundlesToInclude);
		Path bundlesFolderPath = this.getProjectFolderPath().resolve(BUNDLES_SUBFOLDER);
		
		if (requiredBundleFiles.size()==0) {
			// --- If no bundles are required, exclude the whole directory ----
			bundlesExcludeList.add(bundlesFolderPath);
		} else {
			// --- Otherwise, exclude all bundle files that are not in the required files list ----
			File[] bundleFiles = bundlesFolderPath.toFile().listFiles();
			for (int i=0; i<bundleFiles.length; i++) {
				File bundleFile = bundleFiles[i];
				if (requiredBundleFiles.contains(bundleFile)==false) {
					bundlesExcludeList.add(bundleFile.toPath());
				}
			}
		}
		
		return bundlesExcludeList;
	}
	
	/* (non-Javadoc)
	 * @see agentgui.core.project.transfer.ProjectExportController#buildFoldersToAddHasmap()
	 */
	@Override
	protected HashMap<File, String> buildFoldersToAddHasmap() {
		HashMap<File, String> foldersToAdd = super.buildFoldersToAddHasmap();
		if (this.getExportSettings().getInstallationPackage().isForMac() == true) {
			foldersToAdd.put(preferencesFolderPath.toFile(), PREFERENCES_PATH_IN_ARCHIVE_MAC);
		} else {
			foldersToAdd.put(preferencesFolderPath.toFile(), PREFERENCES_PATH_IN_ARCHIVE_WINDOWS_LINUX);
		}
		return foldersToAdd;
	}
	
	/**
	 * Deploy the agents from a {@link DeploymentGroup}.
	 * @param deploymentGroup the deployment group
	 */
	public void deploySelectedAgents(DeploymentGroup deploymentGroup, ProjectExportSettings exportSettings) {
		this.deploySelectedAgents(deploymentGroup, exportSettings, true);
	}

	/**
	 * Deploy the agents from a {@link DeploymentGroup}.
	 * @param deploymentGroup the deployment group
	 * @param showUserDialogs if true, progress and message dialogs are shown 
	 */
	public void deploySelectedAgents(DeploymentGroup deploymentGroup, ProjectExportSettings exportSettings, boolean showUserDialogs) {
		
		System.out.println(this.getClass().getSimpleName() + ": Deploying group " + deploymentGroup.getGroupID());
		
		this.deploymentGroup = deploymentGroup;
		this.exportSettings = exportSettings;
		
		// --- Add the deployment group to the setup ------------------
		this.getSetupExtension().addDeploymentGroup(deploymentGroup);
		deploymentGroup.setActive(true);
		
		// --- Check if all required bundles are available --------------------
		if (this.getMissingBundlesList().size()>0) {
			StringBuilder missingMessage = new StringBuilder();
			missingMessage.append("Some required bundles could not be found. Please export all required bundles to the project's plugin folder deploying an agent!");
			missingMessage.append("\n\nThe following bundles are missing:");
			for (int i=0; i<this.getMissingBundlesList().size(); i++) {
				missingMessage.append("\n    - " + this.getMissingBundlesList().get(i));
			}
			JOptionPane.showMessageDialog(Application.getMainWindow(), missingMessage.toString(), "Error: Required bundles not found!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		// --- Add a repository entry -------------------------------
		if (this.deploymentGroup.getDeploymentSettings().getDeploymentMode()==DeploymentMode.UPDATE) {
			RepositoryEntry repositoryEntry = new RepositoryEntry(this.getProject());
			repositoryEntry.setVersionTag(this.deploymentGroup.getDeploymentSettings().getProjectTag());
			repositoryEntry.setVersion(this.deploymentGroup.getProjectVersion());
			repositoryEntry.setFileName(this.getExportSettings().getTargetFile().getName());
			
			this.getProjectRepository().addRepositoryEntry(repositoryEntry);
			this.getProjectRepository().save(this.getProjectRepositoryDirectory());
		}
		
		// --- Deploy the agent to the selected location ------------
		this.exportProject(this.getProject(), this.getExportSettings(), showUserDialogs, true);
				
	}
	

	/**
	 * Returns the list of bundles that are required by the specified classes.
	 * @param classNameList the agent class name list
	 * @return the bundle jar files to include
	 */
	private List<Bundle> getBundlesToInclude(List<String> classNameList) {
		
		// --- Define a value set with unique values ----------------
		HashSet<Bundle> bundlesFoundHash = new HashSet<>();
		for (int i=0; i<classNameList.size(); i++) {
			String className = classNameList.get(i);
			List<Bundle> bundlesFound = this.getBundlesToInclude(className);
			if (bundlesFound!=null && bundlesFound.size()>0) {
				bundlesFoundHash.addAll(bundlesFound);
			}
		}
		// --- Convert the HashSet to ArrayList ---------------------
		return new ArrayList<Bundle>(bundlesFoundHash);
	}
	
	
	/**
	 * Gets the bundles to include.
	 * @param agentClassName the agent class name
	 * @return the bundles to include
	 */
	private List<Bundle> getBundlesToInclude(String agentClassName) {
		// --- Define reminder for required bundles ----------------- 
		List<Bundle> agentBundles = new ArrayList<>(); 
		
		// --- Get the source bundle of the agent class -------------
		Class<?> agentClass = null;
		try {
			agentClass = ClassLoadServiceUtility.getAgentClass(agentClassName);
			if (agentClass!=null) {
				// --- Get the bundle of the agent class ------------
				Bundle bundle = FrameworkUtil.getBundle(agentClass);	
				if (bundle!=null) {
					// --- Remind the agent bundle ------------------
					agentBundles.add(bundle);
					agentBundles.addAll(this.getRequiredProjectBundlesRecursively(bundle));
				}
			}
		
		} catch (NoClassDefFoundError | ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		
		if (agentBundles.size()==0) {
			System.err.println("[" + this.getClass().getSimpleName() + "]: Could not find the specific bundle(s) for the agent '" + agentClassName + "'. => If executing in IDE-environemt, please check your run configuration.");
		}
		
		return agentBundles;
	}
	
	/**
	 * Returns the bundle jar files to include.
	 *
	 * @param agentClassName the agent class name
	 * @return the bundle jar files to include
	 */
	private List<File> getBundleJarFilesToInclude(List<Bundle> agentBundles) {
		
		// --- Return the files of the project plugins found --------
		List<File> bundleJarsFound = new ArrayList<>();
		// --- Translate bundles into project bundle files ------ 
		for (int i=0; i<agentBundles.size(); i++) {
			Bundle bundle = agentBundles.get(i);
			File bundleFile = this.getProject().getProjectBundleLoader().getBundleFile(bundle);
			if (bundleFile!=null) {
				bundleJarsFound.add(bundleFile);
			} else {
				System.err.println("[" + this.getClass().getSimpleName() + "]: Could not find the file of the bundle '" + bundle.getSymbolicName() + "'.");
				this.getMissingBundlesList().add(bundle.getSymbolicName());
			}
		}
		return bundleJarsFound;
	}
	/**
	 * Recursively return the required bundles of the specified one.
	 *
	 * @param initialBundle the initial or agent bundle
	 * @return the required bundles recursively
	 */
	private List<Bundle> getRequiredProjectBundlesRecursively(Bundle initialBundle) {
		
		List<Bundle> requiredBundels = new ArrayList<>();
		
		// --- Get the list of required bundles ---------------------
		List<String> reqBundles = BundleFeatureMapper.getRequiredBundleIDs(initialBundle);
		for (int i = 0; i < reqBundles.size(); i++) {
			String requiredBundle = reqBundles.get(i);
			Bundle reqBundle = Platform.getBundle(requiredBundle);
			// --- Check if this is a project bundle ----------------
			if (this.getProject().getProjectBundleLoader().getBundleVector().contains(reqBundle)) {
				// --- Found a required bundle ----------------------
				requiredBundels.add(reqBundle);
				// --- check for further required bundles -----------
				requiredBundels.addAll(this.getRequiredProjectBundlesRecursively(reqBundle));
			}
		}
		return requiredBundels;
	}
	/**
	 * Gets the missing bundles list.
	 * @return the missing bundles list
	 */
	private List<String> getMissingBundlesList() {
		if (missingBundlesList==null) {
			missingBundlesList = new ArrayList<>();
		}
		return missingBundlesList;
	}
	

	/**
	 * Gets the network model.
	 * @return the network model
	 */
	private NetworkModel getNetworkModel() {
		if (networkModel == null) {
			EnvironmentModel environmentModel = this.getProject().getEnvironmentController().getEnvironmentModel();
			if (environmentModel != null) {
				DisplaytEnvironmentModel displayEnvironmentModel = environmentModel.getDisplayEnvironment();
				if (displayEnvironmentModel != null && displayEnvironmentModel instanceof NetworkModel) {
					networkModel = (NetworkModel) displayEnvironmentModel;
				}
			}
		}
		return networkModel;
	}

	/**
	 * Configure some preference settings for the testbed agent 
	 */
	private void configureWorkbenchPreferences() {

		try {
			String preferencesFileName = BundleProperties.PLUGIN_ID + PREFERENCES_FILE_SUFFIX;
			File preferencesFile = this.getPreferencesTempFolderPath().resolve(preferencesFileName).toFile();
			if (this.getPreferencesTempFolderPath().toFile().exists()==false) {
				Files.createDirectories(this.getPreferencesTempFolderPath());
			}

			TreeMap<String, String> preferencesTreeMap = this.buildPreferencesTreeMap();
			this.savePropertiesToFile(preferencesTreeMap, preferencesFile);

		} catch (IOException ioEx) {
			System.err.println("Error storing AgentWorkbench preferences in the export folder");
			ioEx.printStackTrace();
		}

	}
	
	/**
	 * Build a {@link TreeMap} containing some preference settings for the testbed agent
	 * @return the {@link TreeMap}
	 */
	private TreeMap<String, String> buildPreferencesTreeMap() {
		TreeMap<String, String> preferencesTreeMap = new TreeMap<>();
	
		// --- Values that have to be adjusted for the tested agent execution --------
		preferencesTreeMap.put(BundleProperties.DEF_RUNAS, ExecutionMode.DEVICE_SYSTEM.name());
		preferencesTreeMap.put(BundleProperties.DEF_DEVICE_SERVICE_PROJECT_FOLDER, this.getProject().getProjectFolder());
		preferencesTreeMap.put(BundleProperties.DEF_DEVICE_SERVICE_EXEC_AS, DeviceSystemExecutionMode.AGENT.name());
		preferencesTreeMap.put(BundleProperties.DEF_DEVICE_SERVICE_AGENT_NAME, this.getDeviceAgentsPropertyValue(this.getDeviceAgentDescriptions()));
		preferencesTreeMap.put(BundleProperties.DEF_DEVICE_SERVICE_VISUALIZATION, EmbeddedSystemAgentVisualisation.TRAY_ICON.name());
		preferencesTreeMap.put(BundleProperties.DEF_OWN_MTP_IP, PlatformJadeConfig.MTP_IP_AUTO_Config);
		preferencesTreeMap.put(BundleProperties.DEF_OWN_MTP_PORT, "" + this.getDeploymentSettings().getMtpPort());
		preferencesTreeMap.put(BundleProperties.DEF_OWN_MTP_PROTOCOL, this.getDeploymentSettings().getCentralAgentSpecifier().getMtpType());
		preferencesTreeMap.put(BundleProperties.DEF_OWN_MTP_CREATION, MTP_Creation.ConfiguredByJADE.name());
		preferencesTreeMap.put(BundleProperties.DEF_LOGGING_ENABLED, "true");
	
		// --- Values that can be taken from the running application ------------------
		IEclipsePreferences prefs = Application.getGlobalInfo().getBundleProperties().getEclipsePreferences();
		preferencesTreeMap.put(BundleProperties.DEF_MASTER_PORT, prefs.get(BundleProperties.DEF_MASTER_PORT, null));
		preferencesTreeMap.put(BundleProperties.DEF_MASTER_PORT4MTP, prefs.get(BundleProperties.DEF_MASTER_PORT4MTP, null));
		preferencesTreeMap.put(BundleProperties.DEF_MASTER_PROTOCOL, prefs.get(BundleProperties.DEF_MASTER_PROTOCOL, null));
	
		return preferencesTreeMap;
	}

	/**
	 * Write the preferences from the {@link TreeMap} to a file
	 * @param preferences the preferences
	 * @param file the file
	 * @throws IOException error saving the preferences
	 */
	private void savePropertiesToFile(TreeMap<String, String> preferences, File file) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			Set<String> keys = preferences.keySet();
			for (String key : keys) {
				String value = preferences.get(key);
				String prefFileEntry = key + "=" + value + "\n";
				fw.write(prefFileEntry);
			}
		} catch (IOException e) {
			System.err.println("Error writing properties to file " + file.getAbsolutePath());
			e.printStackTrace();
		} finally {
			if (fw!=null) {
				try {
					fw.close();
				} catch (IOException e) {
					System.err.println("Error closing FileWriter for file " + file.getAbsolutePath());
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Export the database properties.
	 */
	private void exportDatabaseProperties() {
		
		DatabaseSettings dbSettings = this.getDeploymentSettings().getDatabaseSettings();
		if (dbSettings!=null) {
			
			try {
				File dbPropertiesFile = this.getPreferencesTempFolderPath().resolve(DATABASE_PREFERENCES_FILE_NAME).toFile();
				if (this.getPreferencesTempFolderPath().toFile().exists()==false) {
					Files.createDirectories(this.getPreferencesTempFolderPath());
				}
			
				TreeMap<String, String> dbPropertiesTreeMap = this.buildDatabasePropertiesTreeMap(this.getDeploymentSettings().getDatabaseSettings());
				this.savePropertiesToFile(dbPropertiesTreeMap, dbPropertiesFile);
			
			} catch (IOException e) {
				System.err.println("Error storing database properties in the export folder");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Convert the {@link DatabaseSettings} to a {@link TreeMap}
	 * @param databaseSettings the database settings
	 * @return the tree map
	 */
	private TreeMap<String, String> buildDatabasePropertiesTreeMap(DatabaseSettings databaseSettings){
		TreeMap<String, String> dbPropertiesTreeMap = new TreeMap<>();
		dbPropertiesTreeMap.put("eclipse.preferences.version", "1");
		dbPropertiesTreeMap.put("eom.DatabaseSystem", databaseSettings.getDatabaseSystemName());
		
		Properties dbProperties = databaseSettings.getHibernateDatabaseSettings();
		Vector<Object> keys = new Vector<>(dbProperties.keySet());
		for (int i=0; i<keys.size(); i++) {
			String key = (String) keys.get(i);
			String value = dbProperties.getProperty(key);
			dbPropertiesTreeMap.put(key, value);
		}
		
		return dbPropertiesTreeMap;
	}
	
	/**
	 * Gets the preferences folder path.
	 * @return the preferences folder path
	 */
	private Path getPreferencesTempFolderPath() {
		if (preferencesFolderPath==null) {
			// --- Add the group ID to the temporary folder name to prevent conflicts when deploying multiple groups in parallel 
			String preferencesTempFolder = PREFERENCES_SUBFOLDER + "_" + this.deploymentGroup.getGroupID();
			
			// --- Replace the temporary folder name with the original name when adding it to the archive 
			this.getArchiveFileHandler().addPathReplacement(preferencesTempFolder, PREFERENCES_SUBFOLDER);
			
			// --- Resolve the actual temp folder path --------------
			Path exportFolderPath = this.getExportSettings().getTargetFile().getParentFile().toPath();
			preferencesFolderPath = exportFolderPath.resolve(preferencesTempFolder);
			
		}
		return preferencesFolderPath;
	}

	/**
	 * Gets the device agent descriptions for the deployed agents.
	 * @return the device agent descriptions
	 */
	private Vector<DeviceAgentDescription> getDeviceAgentDescriptions() {
		List<AgentDeploymentInformation> deployedAgents = new ArrayList<>(this.deploymentGroup.getAgentsHashMap().values());
		Vector<DeviceAgentDescription> deviceAgentDescriptions = new Vector<>();
		for (int i=0; i<deployedAgents.size(); i++) {
			AgentDeploymentInformation deploymentInfo = deployedAgents.get(i);
			DeviceAgentDescription deviceAgentDescription = new DeviceAgentDescription(deploymentInfo.getAgentID(), deploymentInfo.getAgentClassName());
			deviceAgentDescriptions.add(deviceAgentDescription);
		}
		return deviceAgentDescriptions;
	}
	
	/**
	 * Gets the device agents property value.
	 * @param deviceAgentVector the device agent vector
	 * @return the device agents property value
	 */
	private String getDeviceAgentsPropertyValue(Vector<DeviceAgentDescription> deviceAgentVector) {
		
		String propValue = "";
		for (int i = 0; i < deviceAgentVector.size(); i++) {
			
			String singleAgent = deviceAgentVector.get(i).toString();
			if (propValue==null || propValue.isEmpty()==true) {
				propValue = singleAgent;
			} else {
				propValue += ", " + singleAgent;
			}
		}
		return propValue;
	}

	/**
	 * Perform some necessary modifications of the project configuration
	 * @param projectFolder the project folder
	 */
	private void modifyProjectSettings(File projectFolder) {
		Project project = Project.load(projectFolder, false);
		
		// --- Configure jade communication settings ----------------
		project.getJadeConfiguration().setLocalPort(this.getDeploymentSettings().getJadePort());
		project.getJadeConfiguration().setLocalPortMTP(this.getDeploymentSettings().getMtpPort());
		if (this.getDeploymentSettings().isTargetSystemAutoIp()==true) {
			project.getJadeConfiguration().setMtpIpAddress(PlatformJadeConfig.MTP_IP_AUTO_Config);
		} else {
			project.getJadeConfiguration().setMtpIpAddress(this.getDeploymentSettings().getTargetSystemIpAddress());
		}
		
		// --- Adjust repository URLs -------------------------------
		if (this.getDeploymentSettings().isP2RepositoryEnabled()==true) {
			this.setProjectFeaturesRepositoryURL(project, this.getDeploymentSettings().getP2Repository());
		}
		if (this.getDeploymentSettings().isProjectRepositoryEnabled()==true) {
			project.setUpdateSite(this.getDeploymentSettings().getProjectRepository());
		}
		
		// --- Set the deployment settings --------------------------
		HyGridAbstractEnvironmentModel userDataModel = ((HyGridAbstractEnvironmentModel) this.getProject().getUserRuntimeObject()).getCopy();
		SetupExtension setEx = this.getSetupExtension();
		userDataModel.setSetupExtension(setEx);
		userDataModel.setDeploymentSettings(this.getDeploymentSettings());
		project.setUserRuntimeObject(userDataModel);
		project.setVersionTag(this.getDeploymentSettings().getProjectTag());
		project.setVersion(this.deploymentGroup.getProjectVersion());
		
		// --- Remove unnecessary project plug-ins ------------------
		this.reduceProjectPlugins(project);
		
		project.save(projectFolder, false, true);
	}
	
	private void setProjectFeaturesRepositoryURL(Project project, String uriString) {
		
		try {
			URI uri = new URI(uriString);
			
			Vector<FeatureInfo> projectFeatures = project.getProjectFeatures();
			for (int i=0; i<projectFeatures.size(); i++) {
				FeatureInfo feature = projectFeatures.get(i);
					feature.setRepositoryURI(uri);
			}
			
		} catch (URISyntaxException e) {
			System.err.println("[" + this.getClass().getSimpleName() + "] Error: Invalid URI syntax: " + uriString);
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Reduces the setup's {@link GeneralGraphSettings4MAS}. Agent and adapter classes for all
	 * component types but those of the deployed agents are removed
	 * @return the reduced general graph settings
	 */
	private GeneralGraphSettings4MAS reduceGeneralGraphSettings() {
		// --- Make a copy of the setup's ggs4mas instance --------------------
		GeneralGraphSettings4MAS reducedGeneralGraphSettings = this.getNetworkModel().getGeneralGraphSettings4MAS().getCopy();
		
		// --- Get lists of defined and deployed component types --------------
		Collection<String> deployedAgentsComponentTypes = this.deploymentGroup.getNetworkComponentTypes();
		List<String> graphSettingsComponentTypes = new ArrayList<>(reducedGeneralGraphSettings.getCurrentCTS().keySet());
		
		// --- Check all defined component types ------------------------------
		for (int i=0; i<graphSettingsComponentTypes.size(); i++) {
			
			// --- Remove classes if it is not in the list of the deployed component's types ------ 
			 String componentType = graphSettingsComponentTypes.get(i);
			 if (deployedAgentsComponentTypes.contains(componentType)==false) {
				 ComponentTypeSettings cts = reducedGeneralGraphSettings.getCurrentCTS().get(componentType);
				 if (cts.getAgentClass()!=null) {
					 cts.setAgentClass(null);
					 cts.setAdapterClass(null);
				 }
			 }
		 }
		
		 return reducedGeneralGraphSettings;
	}
	
	
	/**
	 * Saves the modified setup extension to the deployed simulation setup.
	 * @param setupsFolderPath the setups folder path
	 */
	private void modifySimulationSetup(Path setupsFolderPath) {
		// --- Derive the file path for the deployed simulation setup ---------
		String sourceSetupFilePath = this.getProject().getSimulationSetups().getCurrSimXMLFile();
		Path fileName = new File(sourceSetupFilePath).toPath().getFileName();
		File deployedSetupFile = setupsFolderPath.resolve(fileName).toFile();
		
		// --- Load the setup, set the setup extension  and save it --------------
		SimulationSetup deployedSetup = SimulationSetup.load(deployedSetupFile, true);
		deployedSetup.setUserRuntimeObject(this.getSetupExtension());
		deployedSetup.clearAgentDefaultListModels();
		deployedSetup.saveSetupFiles(deployedSetupFile, true);
	}
	
	/**
	 * Gets the {@link SetupExtension} for the current {@link SimulationSetup}.
	 * @return the setup extension
	 */
	private SetupExtension getSetupExtension() {
		HyGridPlugIn plugIn = HyGridPlugIn.getInstanceForCurrentProject();
		return plugIn.getSetupExtension();
	}
	
	/**
	 * Remove the detailed models for all systems but the currently deployed ones from the environment model
	 * @param environmentModel the environment model
	 * @param deployedSystemIDs the deployed system's IDs
	 * @return the modified environment model
	 */
	private NetworkModel removeOtherSystemsModels(Collection<String> deployedSystemIDs) {
		// --- Iterate over the network components -----------------
		NetworkModel networkModel = this.getNetworkModel().getCopy();
		Vector<NetworkComponent> networkComponents = networkModel.getNetworkComponentVectorSorted();
		for(int i=0; i<networkComponents.size(); i++){
			NetworkComponent networkComponent = networkComponents.get(i);
			// --- For all components but those to be deployed, remove the data models ----------------  
			if (deployedSystemIDs.contains(networkComponent.getId())==false) {
				// --- Remove only for active components ------------
				String agentClassName = this.getNetworkModel().getAgentClassName(networkComponent);
				if (agentClassName!=null) {
					networkComponent.setDataModel(null);
					networkComponent.setDataModelBase64(null);
				}
				
			} else {
				
				// --- For Sensors, remove the sensor schedule if deploying for RealSystem or TestbedReal
				if (networkComponent.getType().equals("Sensor")) {
					AgentOperatingMode opMode = this.deploymentGroup.getAgentsHashMap().get(networkComponent.getId()).getAgentOperatingMode();
					if (opMode==AgentOperatingMode.RealSystem || opMode==AgentOperatingMode.TestBedReal) {
						
						Object[] dataModelArray = (Object[]) networkComponent.getDataModel();
						if (dataModelArray.length==4 && dataModelArray[3]!=null) {
							dataModelArray[3] = 0;
						}
						
						Vector<String> dataModelBase64 = networkComponent.getDataModelBase64();
						if (dataModelBase64.size()==3) {
							dataModelBase64.remove(2);
						}
					}
					
				}
			}
		}
		return networkModel;
	}
	
	
	/**
	 * Reduce project plugins.
	 * @param projectForDeployment the project for deployment
	 */
	private void reduceProjectPlugins(Project projectForDeployment) {
		Vector<PlugIn> projectPlugIns = this.getProject().getPlugInsLoaded();
		for(int i=0; i<projectPlugIns.size(); i++) {
			PlugIn plugIn = projectPlugIns.get(i);
			Bundle pluginBundle = FrameworkUtil.getBundle(plugIn.getClass());
			if (this.isBundleIncluded(pluginBundle)==false) {
				projectForDeployment.removePlugInClassReference(plugIn.getClassReference());
			}
		}
	}
	
	
	/**
	 * Checks if is bundle included.
	 * @param bundle the bundle
	 * @return true, if is bundle included
	 */
	private boolean isBundleIncluded(Bundle bundle) {
		for(int i=0; i<this.bundlesToInclude.size(); i++) {
			if (bundlesToInclude.get(i).equals(bundle)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the project repository directory.
	 * @return the project repository directory
	 */
	private File getProjectRepositoryDirectory() {
		if (projectRepositoryDirectory==null) {
			String projectRepositoryPath = Application.getGlobalInfo().getStringFromConfiguration(BundleProperties.DEF_LOCAL_PROJECT_REPOSITORY, null);
			if (projectRepositoryPath==null || projectRepositoryPath.isEmpty()) {
				System.err.println(this.getClass().getName() + ": Error: Repository path not configured!");
			} else {
				projectRepositoryDirectory = new File(projectRepositoryPath);
			}
		}
		return projectRepositoryDirectory;
	}

	/**
	 * Gets the project repository.
	 * @return the project repository
	 */
	private ProjectRepository getProjectRepository() {
		if (projectRepository==null) {
			projectRepository = ProjectRepository.loadProjectRepository(this.getProjectRepositoryDirectory());
			if (projectRepository==null) {
				projectRepository = new ProjectRepository();
			}
		}
		return projectRepository;
	}

	private List<DeploymentListener> getDeploymentListeners() {
		if (deploymentListeners==null) {
			deploymentListeners = new ArrayList<>();
		}
		return deploymentListeners;
	}
	
	public void addDeploymentListener(DeploymentListener deploymentListener) {
		this.getDeploymentListeners().add(deploymentListener);
	}
	
	public void removeDeploymentListener(DeploymentListener deploymentListener) {
		this.getDeploymentListeners().remove(deploymentListener);
	}
	
	/**
	 * Notify successful.
	 */
	private void notifySuccessful() {
		if (this.getDeploymentListeners().size()>0) {
			for (int i=0; i<this.getDeploymentListeners().size(); i++) {
				this.getDeploymentListeners().get(i).deploymentSuccessful(this.deploymentGroup.getGroupID());
			}
		}
	}
	
	/**
	 * Notify failed.
	 */
	private void notifyFailed() {
		if (this.getDeploymentListeners().size()>0) {
			for (int i=0; i<this.getDeploymentListeners().size(); i++) {
				this.getDeploymentListeners().get(i).deploymentFailed(this.deploymentGroup.getGroupID());
			}
		}
	}

}
