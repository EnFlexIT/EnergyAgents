package de.enflexit.ea.ui;

import java.beans.PropertyChangeEvent;

/**
 * The Class SwingUiDataCollector.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class SwingUiFocusEvent extends PropertyChangeEvent {

	private static final long serialVersionUID = -5412757945792127846L;
	
	private SwingUiFocusDescription focusDescription;
	
	/**
	 * Instantiates a new swing ui focus event.
	 *
	 * @param source the source
	 * @param propertyName the property name
	 * @param oldValue the old value
	 * @param newValue the new value
	 * @param focusDescription the focus description
	 */
	public SwingUiFocusEvent(Object source, String propertyName, Object oldValue, Object newValue, SwingUiFocusDescription focusDescription) {
		super(source, propertyName, oldValue, newValue);
		this.setFocusDescription(focusDescription);
	}
	public SwingUiFocusDescription getFocusDescription() {
		return focusDescription;
	}
	public void setFocusDescription(SwingUiFocusDescription focusDescription) {
		this.focusDescription = focusDescription;
	}
	
}
