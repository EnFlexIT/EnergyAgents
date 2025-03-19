package de.enflexit.ea.core.awbIntegration.plugin;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import de.enflexit.awb.core.project.Project;
import de.enflexit.awb.core.project.transfer.DefaultProjectExportController;
import de.enflexit.awb.core.project.transfer.ProjectExportController;

/**
 * {@link ProjectExportController} implementation for the Agent.HyGrid project.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class EnergyAgentProjectExportController extends DefaultProjectExportController {
	
	public static final String RESSOURCES_SUBFOLDER = "ressources";
	public static final String SUBMODELS_SUBFOLDER = "subModels";
	
	
	/* (non-Javadoc)
	 * @see agentgui.core.project.transfer.DefaultProjectExportController#getDefaultExcludeList()
	 */
	@Override
	public ArrayList<Path> getDefaultExcludeList() {
		ArrayList<Path> defaultExcludeList = super.getDefaultExcludeList();
		defaultExcludeList.add(this.getProjectFolderPath().resolve(RESSOURCES_SUBFOLDER));
		defaultExcludeList.add(this.getProjectFolderPath().resolve(Project.DEFAULT_AGENT_WORKING_DIRECTORY));
		return defaultExcludeList;
	}
	
	/* (non-Javadoc)
	 * @see agentgui.core.project.transfer.DefaultProjectExportController#getAdditionalSetupFiles(java.lang.String)
	 */
	@Override
	public ArrayList<File> getAdditionalSetupFiles(String setupName) {
		
		// --- If there are sub-models for this setup, add them to the list of setup-related files 
		ArrayList<File> additionalSetupFiles = new ArrayList<>();
		Path subModelsFolderPath = new File(this.getProject().getProjectFolderFullPath()).toPath().resolve(SUBMODELS_SUBFOLDER);

		// --- Check if there is a submodels folder in the project ----------------------
		if (subModelsFolderPath.toFile().exists()==true) {
			File setupSubModelsFolder = subModelsFolderPath.resolve(setupName).toFile();
			// --- Check if there is a submodels folder for this setup ------------------
			if (setupSubModelsFolder.exists()==true) {
				
				// --- Add the folder an all contained files ----------------------------
				additionalSetupFiles.add(setupSubModelsFolder);
				additionalSetupFiles.addAll(Arrays.asList(setupSubModelsFolder.listFiles()));
				
			}
		}
		
		return additionalSetupFiles;
	}

}
