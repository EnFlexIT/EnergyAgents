package de.enflexit.ea.ui;

import java.beans.PropertyChangeEvent;

/**
 * The Class SwingUiDataCollector.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class SwingUiDataCollector extends PropertyChangeEvent {

	private static final long serialVersionUID = -5412757945792127846L;
	
	private Object collectedData;
	
	public SwingUiDataCollector(Object source, String propertyName, Object oldValue, Object newValue) {
		super(source, propertyName, oldValue, newValue);
	}

	/**
	 * Returns the collected data.
	 * @return the collected data
	 */
	public Object getCollectedData() {
		return collectedData;
	}
	/**
	 * Can be used to set the data to collected.
	 * @param collectedData the new collected data
	 */
	public void setCollectedData(Object collectedData) {
		this.collectedData = collectedData;
	}
	
}
