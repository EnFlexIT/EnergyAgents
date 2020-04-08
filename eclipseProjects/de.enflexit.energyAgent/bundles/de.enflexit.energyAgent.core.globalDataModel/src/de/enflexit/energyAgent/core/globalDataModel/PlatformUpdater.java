package de.enflexit.energyAgent.core.globalDataModel;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import agentgui.core.application.Application;
import agentgui.core.project.Project;
import agentgui.core.update.AWBUpdater;
import de.enflexit.common.featureEvaluation.FeatureInfo;
import de.enflexit.common.p2.P2OperationsHandler;
import de.enflexit.energyAgent.core.globalDataModel.cea.CeaConfigModel;

/**
 * The Class PlatformUpdater provides the central runtime access method for energy agents 
 * to check for updates of features and bundles (p2 update check) and project repository
 * update. It is designed as singleton, in order to prevent concurrent use of the 
 * AWBUpdate or a projects update check. 
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class PlatformUpdater {

	private static PlatformUpdater thisInstance;
	
	public static boolean DEBUG_PLATFORM_UPDATE = false;
	
	private final long pausePeriodForSettingUpdate 		= 1000 * 60; // 1 minute
	private final long pausePeriodForAWBUpdateCheck 	= 1000 * 60; // 1 minute
	private final long pausePeriodForProjectUpdateCheck = 1000 * 60; // 1 minute
	
	private long lastSettingUpdate;
	private long lastAWBUpdateCheck;
	private long lastProjectUpdateCheck;
	
	
	/**
	 * Private constructor Instantiates a new platform updater.
	 */
	private PlatformUpdater() {	}
	/**
	 * Gets the single instance of PlatformUpdater.
	 * @return single instance of PlatformUpdater
	 */
	public static PlatformUpdater getInstance() {
		if (thisInstance==null) {
			thisInstance = new PlatformUpdater();
		}
		return thisInstance;
	}
	
	/**
	 * Returns the time stamp of the next mirror or update interval.
	 *
	 * @param minuteOffset the minute offset in relation to a full hour. Value may be between -59 to +59
	 * @param intervalHours the interval length in hours. A value of 0 will return the current date and time.
	 * @return the time stamp of next mirror or update interval
	 */
	public static Date getDateOfNextMirrorOrUpdateInterval(int minuteOffset, int intervalHours) {
		
		if (intervalHours==0) return new Date();
		
		// --- Check range of minute offset ------------------------- 
		while (minuteOffset>=60) {
			minuteOffset -=60;
		}
		while (minuteOffset<=-60) {
			minuteOffset +=60;
		}
		
		// --- Define start time at the begin of the day ------------
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR, 0);
		
		// --- Consider the minute offset----------------------------
		if (minuteOffset!=0) {
			calendar.add(Calendar.MINUTE, minuteOffset);
		}

		// --- Ensure to be in the future ---------------------------
		long currTime = System.currentTimeMillis();
		while (calendar.getTimeInMillis() < currTime) {
			calendar.add(Calendar.HOUR, intervalHours);
		}
		
		// --- Prepare return value ---------------------------------
		Date nextMirrorOrUpdateTime = calendar.getTime();
		if (DEBUG_PLATFORM_UPDATE==true) {
			System.out.println("Next mirror or update action at " + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS").format(nextMirrorOrUpdateTime));
		}
		return nextMirrorOrUpdateTime;
	}
	
	
	/**
	 * Updates the update settings according to the CeaConfigModel.
	 * @param ceaConfigModel the CeaConfigModel
	 */
	public synchronized void setUpdateSites(CeaConfigModel ceaConfigModel) {
		this.setUpdateSites(ceaConfigModel.getMirrorProviderURLP2Repository(), ceaConfigModel.getMirrorProviderURLProjectRepository());
	}
	/**
	 * Updates the update settings according to the CeaConfigModel.
	 *
	 * @param p2Repository the p 2 repository
	 * @param projectRepository the project repository
	 */
	public synchronized void setUpdateSites(String p2Repository, String projectRepository) {
		long currTime = System.currentTimeMillis();
		if (this.lastSettingUpdate==0 || currTime>=this.lastSettingUpdate + this.pausePeriodForSettingUpdate) {
			// --- Remind last execution time ---------------------------------
			this.lastSettingUpdate = currTime;
			// --- Some debug output ------------------------------------------
			if (DEBUG_PLATFORM_UPDATE==true) {
				System.out.println("[" + this.getClass().getSimpleName() + "] DEBUG: setUpdateSites(" + p2Repository + ", " + projectRepository + ") was called!");
			}
			// --- Check p2 update sites --------------------------------------
			this.setP2UpdateSite(p2Repository);
			// --- Check project repository in the current project ------------
			this.setProjectUpdateSite(projectRepository, p2Repository);
			// --- Set feature update-site to p2 ------------------------------
			
		}
	}
	/**
	 * Sets the p2 update site.
	 * @param p2Repository the new p 2 update site
	 */
	public void setP2UpdateSite(String p2Repository) {
		if (p2Repository!=null && p2Repository.isEmpty()==false) {
			try {
				URI repoURI = new URI(p2Repository);
				if (P2OperationsHandler.getInstance().isKnownRepository(repoURI)==false) {
					P2OperationsHandler.getInstance().addRepository(repoURI, "AWB Field Repository");
				}
			} catch (URISyntaxException uriEx) {
				uriEx.printStackTrace();
			} 
		}
	}
	
	/**
	 * Sets the project update site.
	 *
	 * @param projectRepository the new project update site
	 * @param p2Repository the ps repository
	 */
	public void setProjectUpdateSite(String projectRepository, String p2Repository) {
		if (projectRepository!=null && projectRepository.isEmpty()==false) {
			Project project = this.getCurrentProject();
			if (project!=null) {
				// --- Compare old and new settings -----------------
				String projectRepositoryOld = project.getUpdateSite();
				if (projectRepositoryOld==null || projectRepositoryOld.equals(projectRepository)==false) {
					project.setUpdateSite(projectRepository);
					project.save();	
				}
				// --- Check feature entries ------------------------
				if (p2Repository!=null && p2Repository.isEmpty()==false) {
					for (int i = 0; i < project.getProjectFeatures().size(); i++) {
						FeatureInfo fi =  project.getProjectFeatures().get(i);
						try {
							fi.setRepositoryURI(new URI(p2Repository));
						} catch (URISyntaxException uroEx) {
							uroEx.printStackTrace();
						}
					}
					project.save();
				}
			}
		}
	}
	
	
	/**
	 * Allows to execute the AWB update check once in a minute. This method will start 
	 * the {@link AWBUpdater} (in own thread) and will not wait until the update process
	 * is finalized.
	 * @param waitForFinalization the wait for finalization
	 * @see #executeAWBUpdateCheck(boolean)
	 */
	public synchronized void executeAWBUpdateCheck() {
		this.executeAWBUpdateCheck(false);
	}
	/**
	 * Allows to execute the AWB update check once in a minute.
	 * @param waitForFinalization the wait for finalization. Set <code>true</code> if you 
	 * would like to wait for the update process to be finalized
	 */
	public synchronized void executeAWBUpdateCheck(boolean waitForFinalization) {
		this.executeAWBUpdateCheck(waitForFinalization, false);
	}
	/**
	 * Allows to execute the AWB update check once in a minute.
	 *
	 * @param waitForFinalization the wait for finalization
	 * @param enforceUpdate the enforce update
	 */
	public synchronized void executeAWBUpdateCheck(boolean waitForFinalization, boolean enforceUpdate) {
		
		long currTime = System.currentTimeMillis();
		if (this.lastAWBUpdateCheck==0 || currTime>=this.lastAWBUpdateCheck + this.pausePeriodForAWBUpdateCheck) {
			// --- Remind last execution time -------------
			this.lastAWBUpdateCheck = currTime;
			// --- Do the feature / bundle update check ---
			AWBUpdater awbUpdater = new AWBUpdater(false, enforceUpdate);
			awbUpdater.start();
			if (waitForFinalization==true) {
				awbUpdater.waitForUpdate();
			}
		}
	}
	
	/**
	 * Allows to execute the project update check once in a minute.
	 */
	public synchronized void executeProjectUpdateCheck() {
		this.executeProjectUpdateCheck(false);
	}
	/**
	 * Allows to execute the project update check once in a minute.
	 * @param enforceUpdate ensures that the update will be executed (e.g. regardless of the date 
	 * of the last update check and the period between the last check is shorter than the standard 
	 * waiting time between update checks) 
	 */
	public synchronized void executeProjectUpdateCheck(boolean enforceUpdate) {
		
		long currTime = System.currentTimeMillis();
		if (this.lastProjectUpdateCheck==0 || currTime>=this.lastProjectUpdateCheck + this.pausePeriodForProjectUpdateCheck) {
			// --- Remind last execution time -------------
			this.lastProjectUpdateCheck = currTime;
			// --- Do the project update check ------------
			Project project = this.getCurrentProject();
			if (project!=null) {
				project.doProjectUpdate(false, enforceUpdate);
			}
		}
	}
	/**
	 * Returns the currently opened project from the application instance.
	 * @return the project
	 */
	private Project getCurrentProject() {
		return Application.getProjectFocused();
	}
	
}
