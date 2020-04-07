package de.enflexit.energyAgent.core.centralExecutiveAgent.behaviour;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

import agentgui.core.update.MirrorTool;
import agentgui.core.update.MirrorTool.MirrorToolsJob;
import agentgui.core.update.MirrorTool.P2DownloadType;
import de.enflexit.energyAgent.core.centralExecutiveAgent.CentralExecutiveAgent;
import agentgui.core.update.MirrorToolListener;
import hygrid.globalDataModel.cea.CeaConfigModel;
import jade.core.behaviours.WakerBehaviour;

/**
 * The Class RepositoryMirrorBehaviour.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class RepositoryMirrorBehaviour extends WakerBehaviour implements MirrorToolListener {

	private static final long serialVersionUID = -7488509213368577104L;

	private CentralExecutiveAgent cea;
	private HashMap<MirrorToolsJob, Boolean> mirrorJobFeedback;
	
	/**
	 * Instantiates a new repository mirror behaviour.
	 *
	 * @param cea the instance of the {@link CentralExecutiveAgent}
	 * @param wakeUpTime the wake up time
	 */
	public RepositoryMirrorBehaviour(CentralExecutiveAgent cea, Date wakeUpTime) {
		super(cea, wakeUpTime);
		this.cea = cea;
	}
	/* (non-Javadoc)
	 * @see jade.core.behaviours.WakerBehaviour#onWake()
	 */
	@Override
	protected void onWake() {
		this.doMirrorJobs();
	}
	/**
	 * Does the mirror jobs.
	 */
	private void doMirrorJobs() {
		
		try {
			
			if (this.cea.isExecutedInSimulation()==true) {
				System.out.println("[" + this.cea.getClass().getSimpleName() + "] Repository mirroring was canceled because of current simulation environment.");
				return;
			}
			
			CeaConfigModel configModel = this.cea.getInternalDataModel().getCeaConfigModel();
			if (configModel!=null) {
				
				// --- 0. Reset the mirror jobs done ----------------
				this.getMirrorJobFeedback().clear();
				
				// --- 1. Do P2 mirror job --------------------------
				String p2SourceLocation = configModel.getMirrorSourceP2Repository();
				String p2DestinLocation = null;
				if (configModel.getMirrorDestinationP2Repository()!=null) {
					p2DestinLocation = new File(configModel.getMirrorDestinationP2Repository()).getAbsolutePath();
				}
				// --- Start the mirror process ---------------------
				if (this.isAccessibleSourceURL(p2SourceLocation) && this.isAccessibleDestinationDirectory(p2DestinLocation)==true) {
					MirrorTool.mirrorP2Repository(P2DownloadType.MetaData, p2SourceLocation, p2DestinLocation, this);
					MirrorTool.mirrorP2Repository(P2DownloadType.Artifacts, p2SourceLocation, p2DestinLocation, this);
				}
				
				// --- 2. Do mirror job for the project repository --
				String projectSourceLocation = configModel.getMirrorSourceProjectRepository();
				String projectDestinLocation = null;
				if (configModel.getMirrorDestinationProjectRepository()!=null) {
					projectDestinLocation = new File(configModel.getMirrorDestinationProjectRepository()).getAbsolutePath();
				}
				// --- Start the mirror process ---------------------
				if (this.isAccessibleSourceURL(projectSourceLocation) && this.isAccessibleDestinationDirectory(projectDestinLocation)==true) {
					MirrorTool.mirrorProjectRepository(projectSourceLocation, projectDestinLocation, this);
				}
			}
			
		} catch (Exception ex) {
			System.err.println("[" + this.cea.getLocalName() + "] Error while starting mirror jobs:");
			ex.printStackTrace();
			
		} finally {
			// --- Start a new RepositoryMirrorBehaviour ------------
			this.cea.startNewRepositoryMirrorBehaviour();
			
		}
	}
	
	/**
	 * Checks if the URL source is accessible.
	 *
	 * @param urlString the URL string
	 * @return true, if is accessible URL source
	 */
	private boolean isAccessibleSourceURL(String urlString) {

		boolean accessible = this.isUsableLocationPath(urlString);
		if (accessible==false) {
			return accessible;
		}
		
		try {
			// --- Check the URL String -------------------
			URL url = new URL(urlString);
			url.toURI();
			accessible = true;

			// --- Can be elaborated, if required --------- 
			//HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			//huc.setRequestMethod("HEAD");
			//int responseCode = huc.getResponseCode();

			//if (responseCode != 404) {
			//	System.out.println("GOOD");
			//	accessible = true;
			//} else {
			//	System.out.println("BAD");
			//	accessible = false;
			//}
			
		} catch (URISyntaxException | IOException ex) {
			accessible = false;
			ex.printStackTrace();
		}
		return accessible;
	}
	/**
	 * Checks if is accessible destination directory.
	 * @return true, if is accessible destination directory
	 */
	private boolean isAccessibleDestinationDirectory(String destinationDirectoryPath) {
		
		boolean accessible = this.isUsableLocationPath(destinationDirectoryPath);
		if (accessible==false) {
			return accessible;
		}

		// --- Check if the directory can directly be accessed ------
		File destinDir = new File(destinationDirectoryPath);
		if (destinDir.exists()==true) return true;
		
		// --- Directory is not directly available ------------------
		try {
			accessible = destinDir.mkdirs();
			if (accessible==true) {
				System.out.println("[" + this.cea.getLocalName() + "] Created directory '" +   destinDir.getAbsolutePath() + "'");
			}
			
		} catch (SecurityException se) {
			accessible = false;
			se.printStackTrace();
		}
		if (accessible==false) {
			System.err.println("[" + this.cea.getLocalName() + "] Could not access or create directory '" +   destinDir.getAbsolutePath() + "'. Please, check your configurations.");
		}
		return accessible; 
	}
	/**
	 * Checks if the specified location is usable as location value (basically null or empty check).
	 *
	 * @param locationPath the location
	 * @return true, if is a usable location path
	 */
	private boolean isUsableLocationPath(String locationPath) {
		if (locationPath==null || locationPath.isEmpty()==true) {
			return false;
		}
		return true; 
	}
	
	/**
	 * Return the mirror job feedback of this behaviour. With the maximum size of 3, it will
	 * be indicated that all jobs are done. Check the value of the HashMap to check if the
	 * jobs were successfully finalized.
	 * 
	 * @return the mirror job feedback
	 */
	public HashMap<MirrorToolsJob, Boolean> getMirrorJobFeedback() {
		if (mirrorJobFeedback==null) {
			mirrorJobFeedback = new HashMap<>();
		}
		return mirrorJobFeedback;
	}
	/**
	 * Return if the mirror jobs are done.
	 * @return true, if is mirror jobs are done
	 */
	public boolean isMirrorJobsAreDone() {
		return this.getMirrorJobFeedback().size()==3;
	}
	/* (non-Javadoc)
	 * @see agentgui.core.update.MirrorToolListener#onMirroringFinaliized(agentgui.core.update.MirrorTool.MirrorToolsJob, boolean)
	 */
	@Override
	public void onMirroringFinaliized(MirrorToolsJob job, boolean successful) {
		this.getMirrorJobFeedback().put(job, successful);
		if (this.isMirrorJobsAreDone()==true) {
			// --- Start a new RepositoryMirrorBehaviour ------------
			this.cea.startNewRepositoryMirrorBehaviour();
		}
	}
	
}
