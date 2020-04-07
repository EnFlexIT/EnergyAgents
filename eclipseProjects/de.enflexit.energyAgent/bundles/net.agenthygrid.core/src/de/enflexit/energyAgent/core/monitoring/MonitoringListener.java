package de.enflexit.energyAgent.core.monitoring;

import java.util.EventListener;

/**
 * This interface has to be implemented to listen to {@link MonitoringEvent}s.
 *
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 *
 */
public interface MonitoringListener extends EventListener {
	
	/**
	 * This method is triggered when a {@link MonitoringEvent} occurs.
	 * @param monitoringEvent The monitoring event
	 */
	public void onMonitoringEvent(MonitoringEvent monitoringEvent);
}
