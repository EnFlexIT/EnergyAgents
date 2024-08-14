package de.enflexit.ea.ui;

/**
 * The Class SwingUiFocusDescription.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class SwingUiFocusDescription {

	public enum FocusTo {
		Tab, 
		NextPlanningEvent
	}
	
	private FocusTo focusTo;
	private String argument; 
	
	/**
	 * Instantiates a new swing UI focus description.
	 *
	 * @param focusTo the focus to
	 * @param argument the argument
	 */
	public SwingUiFocusDescription(FocusTo focusTo, String argument) {
		this.focusTo = focusTo;
		this.argument = argument;
	}
	public FocusTo getFocusTo() {
		return focusTo;
	}
	public String getArgument() {
		return argument;
	}
}
