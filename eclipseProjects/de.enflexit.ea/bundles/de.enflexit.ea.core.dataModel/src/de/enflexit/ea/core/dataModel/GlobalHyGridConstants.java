package de.enflexit.ea.core.dataModel;

import energy.optionModel.FixedVariable;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * This class is intended to make constants that are required in different bundles globally available. 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public final class GlobalHyGridConstants {

	/**
	 * Private constructor to prevent instantiation
	 */
	private GlobalHyGridConstants() {}
	
	
	// ------------------------------------------------------------------------
	// --- AWB Domains from the GeneralGraphSettingsForMAS---------------------
	// ------------------------------------------------------------------------
	/** AWB domain or 'Sub Network' for coordination components */
	public static final String HYGRID_DOMAIN_Coordination = "Coordination";
	
	/** AWB domain or 'Sub Network' for electrical distribution grids */
	public static final String DEPRECATED_DOMAIN_ELECTRICITY_400V = "ElectricityThriPhase";
	/** AWB domain or 'Sub Network' for 10KV electricity networks */
	public static final String DEPRECATED_DOMAIN_ELECTRICITY_10KV = "Electricity 10kV";
	
	/** AWB domain or 'Sub Network' for natural gas networks */
	public static final String HYGRID_DOMAIN_NATURAL_GAS = "NaturalGas";
	
	/** AWB domain or 'Sub Network' for heat networks */
	public static final String HYGRID_DOMAIN_HEAT_NETWORK = "Heat";
	
	
	// ------------------------------------------
	// --- Default agent names ------------------
	// ------------------------------------------
	/** The local name of the blackboard agent. */
	public static String BLACKBOARD_AGENT_NAME = "BlackBoardAgent";  
	
	
	// ------------------------------------------
	// --- GlobalElectricityConstants -----------
	// ------------------------------------------
	
	public enum ElectricityNetworkType {
		TriPhaseNetwork,
		UniPhaseNetwork
	}
	
	/**
	 * The Class GlobalElectricityConstants.
	 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
	 */
	public static final class GlobalElectricityConstants {
		
		/**
		 * The enumeration GlobalTransformerMeasurements represents the globally known IO-variables of transformer.
		 * If an implemented transformer will influence the slack node voltage level for a power flow calculation, the
		 * produced system state (a {@link TechnicalSystemStateEvaluation}) should include the following enumeration
		 * parts depending on the use case (three or only one electrical phase considered). Use the {@link #name()}
		 * method of the enumeration to use a parts as variable name or variableID respectively.
		 * 
		 * @see FixedVariable#setVariableID(String)
		 */
		public enum GlobalTransformerMeasurements {
			lvVoltageRealAllPhases, 
			lvVoltageImagAllPhases,
			
			lvVoltageRealL1, 
			lvVoltageImagL1,
			lvVoltageRealL2, 
			lvVoltageImagL2,
			lvVoltageRealL3, 
			lvVoltageImagL3
		}
		
	}
	
	
	// ------------------------------------------
	// --- Conversation IDs ---------------------
	// ------------------------------------------
	/**
	 * Conversation ID for measurement subscriptions to SensorAgents
	 */
	public static final String CONVERSATION_ID_MEASUREMENT_SUBSCRIPTION = "MeasurementSubscription";
	/**
	 * Conversation ID for the subscription from LiveMonitoringAgents to the LiveMonitoringProxyAgent
	 */
	public static final String CONVERSATION_ID_LIVE_MONITORING_OPS = "LiveMonitoringOps";
	/**
	 * Conversation ID for the subscription from the LiveMonitoringProxyAgent to the field agents
	 */
	public static final String CONVERSATION_ID_LIVE_MONITORING_FIELD = "LiveMonitoringField";
	
}
