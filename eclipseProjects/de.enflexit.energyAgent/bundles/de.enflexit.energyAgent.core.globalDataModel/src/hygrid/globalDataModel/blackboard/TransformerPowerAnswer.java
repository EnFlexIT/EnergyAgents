package hygrid.globalDataModel.blackboard;

import energy.optionModel.TechnicalSystemState;

/**
 * The Class TransformerPowerAnswer represents an extended {@link AbstractBlackoardAnswer}.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class TransformerPowerAnswer extends AbstractBlackoardAnswer {

	private static final long serialVersionUID = -679778118583726548L;

	// --- Constant tri phase values --------------------------------
	public static final String TransformerInterface_L1_P = "L1 P";
	public static final String TransformerInterface_L1_Q = "L1 Q";
	public static final String TransformerInterface_L2_P = "L2 P";
	public static final String TransformerInterface_L2_Q = "L2 Q";
	public static final String TransformerInterface_L3_P = "L3 P";
	public static final String TransformerInterface_L3_Q = "L3 Q";
	
	// --- Constant uni phase values --------------------------------
	public static final String TransformerInterface_P = "P";
	public static final String TransformerInterface_Q = "Q";
	
	private String identifier;
	private TechnicalSystemState technicalSystemState;

	
	/**
	 * Instantiates a TransformerPowerAnswer for a {@link BlackboardRequest}.
	 *
	 * @param identifier the identifier
	 * @param technicalSystemState the technical system state
	 */
	public TransformerPowerAnswer(String identifier, TechnicalSystemState technicalSystemState) {
		this.identifier = identifier;
		this.setTechnicalSystemState(technicalSystemState);
	}
	
	/**
	 * Gets the identifier of the NetworkComponent.
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
	/**
	 * Sets the identifier of the NetworkComponent.
	 * @param identifier the new identifier
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Returns the {@link TechnicalSystemState} that contains the power information.
	 * @return the technical system state
	 * @see TechnicalSystemState#getUsageOfInterfaces()
	 */
	public TechnicalSystemState getTechnicalSystemState() {
		return technicalSystemState;
	}
	/**
	 * Sets the technical system state.
	 * @param technicalSystemState the new technical system state
	 */
	public void setTechnicalSystemState(TechnicalSystemState technicalSystemState) {
		this.technicalSystemState = technicalSystemState;
	}
	
}
