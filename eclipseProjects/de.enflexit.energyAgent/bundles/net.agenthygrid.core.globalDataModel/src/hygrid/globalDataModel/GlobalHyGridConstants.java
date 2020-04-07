package hygrid.globalDataModel;

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
	public static final String HYGRID_DOMAIN_ELECTRICITY_400V = "ElectricityThriPhase";
	/** AWB domain or 'Sub Network' for 10KV electricity networks */
	public static final String HYGRID_DOMAIN_ELECTRICITY_10KV = "Electricity 10kV";
	
	/** AWB domain or 'Sub Network' for natural gass networks */
	public static final String HYGRID_DOMAIN_NATURAL_GAS = "NaturalGas";
	
	/** AWB domain or 'Sub Network' for heat networks */
	public static final String HYGRID_DOMAIN_HEAT_NETWORK = "Heat";
	
	
	// ------------------------------------------
	// --- Default agent names ------------------
	// ------------------------------------------
	/** The local name of the blackboard agent. */
	public static String BLACKBOARD_AGENT_NAME = "BlackBoardAgent";  
	
	
	
	
	// ------------------------------------------
	// --- Conversation IDs ---------------------
	// ------------------------------------------
	/**
	 * Conversation ID for measurement subscriptions to SensorAgents
	 */
	public static final String CONVERSATION_ID_MEASUREMENT_SUBSCRIPTION = "MeasurementSubscription";
	
	public static final String CONVERSATION_ID_REF_PV_SUBSCRIPTION = "RefPvSubscription";
	/**
	 * Conversation ID for the subscription from LiveMonitoringAgents to the LiveMonitoringProxyAgent
	 */
	public static final String CONVERSATION_ID_LIVE_MONITORING_OPS = "LiveMonitoringOps";
	/**
	 * Conversation ID for the subscription from the LiveMonitoringProxyAgent to the field agents
	 */
	public static final String CONVERSATION_ID_LIVE_MONITORING_FIELD = "LiveMonitoringField";
	
}
