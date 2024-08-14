package de.enflexit.ea.core.eomStateStream;

import java.util.concurrent.ConcurrentHashMap;

import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler;

/**
 * The Class EomModelLoader provides a central static class to load an EOM model
 * that is located in a NetworkComponent.
 * The idea is to coordinate model loading since a competitive, concurrent 
 * loading of different threads will slow down the overall load processes.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class EomModelLoader {

	private static boolean isDoThreadCoordination = false;
		
	private static final int threadsRunningMax = 50;
	private static Integer threadsExecuted = 0;

	private static ConcurrentHashMap<Thread, Object> threadHashMap;
	
	
	/**
	 * Will try to loads an EOM model for the specified {@link NetworkComponent}.
	 * @return the EOM model object or <code>null</code>
	 */
	public static Object loadEomModel(NetworkComponent netComp) {
		
		try {
			// ----------------------------------------------------------------
			// --- Do thread coordination? ------------------------------------
			// ----------------------------------------------------------------
			if (isDoThreadCoordination==true) {

				Thread thread = Thread.currentThread();
				Object threadMonitor = new Object();
				
				// --- Check if parking is required ---------------------------
				boolean isDoPark = false;
				synchronized (threadsExecuted) {
					if (threadsExecuted>=threadsRunningMax) {
						isDoPark = true;
					} else {
						threadsExecuted++;
					}
				}
				
				// --- Park the thread ----------------------------------------
				if (isDoPark==true) {
					// --- Let the thread wait --------------------------------
					EomModelLoader.parkThread(thread, threadMonitor);
					// - - Thread was restarted, counter increased! - - - - - -
				}
			}
			// ----------------------------------------------------------------
			
			
			// ----------------------------------------------------------------
			// --- Try to load from storage settings --------------------------
			// ----------------------------------------------------------------
			EomDataModelStorageHandler eomStroageHandler = new EomDataModelStorageHandler(null);
			return eomStroageHandler.loadDataModel(netComp);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (isDoThreadCoordination==true) {
				synchronized (threadsExecuted) {
					threadsExecuted--;
				}
				EomModelLoader.resumeNextThreads();
			}
		}
		return null;
	}
	
	/**
	 * Returns the HashMap of threads that are attempting to load an EOM model.
	 * @return the thread vector
	 */
	public static ConcurrentHashMap<Thread, Object> getThreadHashMap() {
		if (threadHashMap==null) {
			threadHashMap = new ConcurrentHashMap<>();
		}
		return threadHashMap;
	}
	/**
	 * Parks the specified thread.
	 *
	 * @param thread the thread
	 * @param threadMonitor the thread monitor
	 */
	private static void parkThread(Thread thread, Object threadMonitor) {
		try {
			// --- Remind thread and monitor for later reactivation -----------
			getThreadHashMap().put(thread, threadMonitor);
			// --- Suspend thread ---------------------------------------------
			synchronized (threadMonitor) {
				threadMonitor.wait();
			}
		} catch (InterruptedException intEx) {
			intEx.printStackTrace();
		}
	}
	/**
	 * Resumes the parked threads so that the maximum number of thread are running concurrently.
	 */
	private static void resumeNextThreads() {
		
		synchronized (threadsExecuted) {
			while (getThreadHashMap().size()>0 & threadsExecuted < threadsRunningMax) {
				Thread thread = getThreadHashMap().keys().nextElement();
				if (thread!=null) {
					Object threadMonitor = getThreadHashMap().get(thread);
					if (threadMonitor!=null) {
						getThreadHashMap().remove(thread);
						synchronized (threadMonitor) {
							threadMonitor.notify();
						}
						threadsExecuted++;
					}
				}
			}
		}
	}
	
}
