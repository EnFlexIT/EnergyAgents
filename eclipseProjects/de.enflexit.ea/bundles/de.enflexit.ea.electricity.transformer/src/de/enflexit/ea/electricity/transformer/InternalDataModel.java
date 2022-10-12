package de.enflexit.ea.electricity.transformer;

import java.io.File;
import java.util.TreeMap;
import java.util.Vector;

import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.helper.NeighborhoodSearch;
import org.awb.env.networkModel.settings.ComponentTypeSettings;
import org.awb.env.networkModel.settings.GeneralGraphSettings4MAS;

import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.AbstractInternalDataModel;
import de.enflexit.ea.core.dataModel.GlobalHyGridConstants;
import de.enflexit.ea.core.dataModel.ontology.CableState;
import de.enflexit.ea.core.dataModel.ontology.ElectricalMeasurement;
import de.enflexit.ea.core.dataModel.phoneBook.EnergyAgentPhoneBookEntry;
import de.enflexit.ea.electricity.transformer.eomDataModel.TransformerDataModel;
import de.enflexit.jade.phonebook.PhoneBook;
import energy.FixedVariableList;
import energy.helper.NumberHelper;
import energy.optionModel.SystemVariableDefinitionStaticModel;

/**
 * This class represents the internal data model of the transformer
 * 
 * @author  Marcel Ludwig - EVT - University of Wuppertal (BUW)
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class InternalDataModel extends AbstractInternalDataModel<EnergyAgentPhoneBookEntry> {

	private static final long serialVersionUID = 3913554312467337020L;

	public static String DOMAIN_ELECTRICITY_400V = GlobalHyGridConstants.HYGRID_DOMAIN_ELECTRICITY_400V;
	public static String DOMAIN_ELECTRICITY_10KV = GlobalHyGridConstants.HYGRID_DOMAIN_ELECTRICITY_10KV;
	
	private GraphNode myGraphNode;
	private TransformerDataModel transformerDataModel;
	
	private Vector<NetworkComponent> connectedNetworkComponents; 
	private TreeMap<String, CableState> cableStateTreeMap;
	
	
	private String idSensorToSubscribeTo;
	private ElectricalMeasurement sensorMeasurement;

	private FixedVariableList setpointsToSystem;
	
	private double requestedVoltageAdjustment = 0;
//	private Vector<AID> aidsOfDistrictAgents = null;
	private int nDistrictAgentsForCNP;
	
	private int acceptedVoltageAdjustment = 0;
	
	private int transformerStep = 0;
	private double possibleVoltageAdjustment = 0;
	
	private boolean newSetPointFromCNP = false;
	
	
	/**
	 * Instantiates a new internal data model.
	 * @param agent the agent
	 */
	public InternalDataModel(AbstractEnergyAgent agent) {
		super(agent);
	}
	
	/**
	 * Returns the {@link GraphNode} of the local component.
	 * @return the local graph node
	 */
	public GraphNode getGraphNode() {
		if (myGraphNode==null) {
			myGraphNode = this.getNetworkModel().getGraphNodeFromDistributionNode(this.getNetworkComponent());
		}
		return myGraphNode;
	}
	/**
	 * Returns the transformer data model.
	 * @return the transformer data model
	 */
	public TransformerDataModel getTransformerDataModel() {
		if (transformerDataModel==null) {
			SystemVariableDefinitionStaticModel sysVarDefStaticModel = (SystemVariableDefinitionStaticModel) this.getOptionModelController().getSystemVariableDefinition(this.getOptionModelController().getTechnicalSystem().getSystemVariables(), "StaticParameters");
			transformerDataModel = (TransformerDataModel) this.getOptionModelController().getStaticModelInstance(sysVarDefStaticModel);
		}
		return transformerDataModel;
	}
	
	// --------------------------------------------------------------------------------------------
	// --- From here, search methods for connected NetworkComponents (cables and sensors) ---------
	// --------------------------------------------------------------------------------------------
	/**
	 * Returns all NetworkComponent's that are connected to the transformer.
	 * @return the transformer connections
	 */
	public Vector<NetworkComponent> getConnectedNetworkComponents() {
		if (connectedNetworkComponents==null) {
			if (this.getNetworkModel()!=null && this.getNetworkComponent()!=null) {
				// --- Found everything required -------------------- 
				connectedNetworkComponents = this.getNetworkModel().getNeighbourNetworkComponents(this.getNetworkComponent());
			} else {
				// --- Something is missing -------------------------
				String errMsg = "Unknown Error!";
				if (this.getNetworkModel()==null) {
					errMsg = "No NetworkModel instance could be found for Transformer '" + this.energyAgent.getLocalName() + "'!";
				} else if (this.getNetworkComponent()==null) {
					errMsg = "No NetworkComponent instance could be found for Transformer '" + this.energyAgent.getLocalName() + "'!";
				}
				System.err.println("[" + this.energyAgent.getClass().getName() + "] " + errMsg);
			}
		}
		return connectedNetworkComponents;
	}
	/**
	 * Returns the connected NetworkComponent that belong to the specified domain.<br>
	 * <b><i>See available local constants also!</i></b>
	 *
	 * @param domain the domain specifier
	 * @return the network components connected to the transformer in the specified domain
	 * 
	 * @see #DOMAIN_ELECTRICITY_400V
	 * @see #DOMAIN_ELECTRICITY_10KV
	 */
	public Vector<NetworkComponent> getConnectedNetworkComponentsOfElectricalDomain(String domain) {
	
		String errMsg = null;
		
		// --- Check domain -------------------------------
		if (domain==null || domain.isEmpty()==true) {
			errMsg = "No domain was specified for the request of connected domain NetworkComponents!";
			System.err.println("[" + this.energyAgent.getClass().getName() + "] " + errMsg);
			return null;
		}
		// --- Unknown / illegal domain? ------------------
		if (domain.equals(DOMAIN_ELECTRICITY_400V)==false && domain.equals(DOMAIN_ELECTRICITY_10KV)==false) {
			errMsg = "Unknown or illegal domain '" + domain + "' for the request of connected electrical domain NetworkComponents!";
			System.err.println("[" + this.energyAgent.getClass().getName() + "] " + errMsg);
			return null;
		}
		
		// --- Get connected components first -------------
		Vector<NetworkComponent> connNetComps = this.getConnectedNetworkComponents();
		if (connNetComps==null) {
			errMsg = "No NetworkComponents connected to Transformer '" + this.energyAgent.getLocalName() + "' could be found!";
			System.err.println("[" + this.energyAgent.getClass().getName() + "] " + errMsg);
			return null;
		}

		
		// --- Define result list -------------------------
		Vector<NetworkComponent> netCompsFound = new Vector<NetworkComponent>();
		GeneralGraphSettings4MAS graphSettings = this.getNetworkModel().getGeneralGraphSettings4MAS();
		for (int i = 0; i < connNetComps.size(); i++) {
			
			NetworkComponent netComp = connNetComps.get(i);
			ComponentTypeSettings cts = graphSettings.getCurrentCTS().get(netComp.getType());
			if (cts.getDomain().equals(domain)==true) {
				// --- Add to result Vector ---------------
				netCompsFound.add(netComp);
			}
		}
		
		// --- Prepare return value -----------------------
		if (netCompsFound.size()==0) return null;
		return netCompsFound;
	}
	
	
	// --------------------------------------------------------------------------------------------
	// --- From here, handling of connected low voltage NetworkComponents (cables and sensors) ----
	// --------------------------------------------------------------------------------------------
	/**
	 * Gets the cable state tree map.
	 * @return the cable state tree map
	 */
	public TreeMap<String, CableState> getLowVoltageCableStateTreeMap() {
		if (cableStateTreeMap==null) {
			cableStateTreeMap = new TreeMap<String, CableState>();
		}
		return cableStateTreeMap;
	}
	/**
	 * Updates the current level for the specified cable.
	 *
	 * @param id the id of the cable
	 * @param cableState the cable state
	 */
	public void updateLowVoltageCurrentLevel(String id, CableState cableState) {
		this.getLowVoltageCableStateTreeMap().put(id, cableState);
	}
	/**
	 * Execute total current calculation.
	 * @return the total current calculation
	 */
	public TotalCurrentCalculation executeTotalCurrentCalculation() {
		return new TotalCurrentCalculation(this, this.getLowVoltageCableStateTreeMap());
	}
	
	
	// --------------------------------------------------------------------------------------------
	// --- From here, handling of the previously used sensor measurement -------------------------- 
	// --------------------------------------------------------------------------------------------
	/**
	 * Return the sensor ID to subscribe to.
	 * @return the sensor ID to subscribe to
	 */
	public String getIDSensorToSubscribeTo() {
		if (idSensorToSubscribeTo==null) {
			
			String msgPrefix = this.energyAgent.getClass().getSimpleName() + " " + this.energyAgent.getAID().getLocalName();
			
			// --- Search for sensors? ----------------------------------------
			NeighborhoodSearch nSearch = new NeighborhoodSearch(this.getNetworkModel(), this.getNetworkComponent());
			Vector<NetworkComponent> sensors = nSearch.getNextNeighborNetworkComponent("Sensor");
			if (sensors.size()==0) {
				System.err.println(msgPrefix + ": Could not find any sensor to subscribe to!");
				return null;
				
			} else if (sensors.size()==1) {
				// --- Found exactly one sensor -------------------------------
				idSensorToSubscribeTo = sensors.get(0).getId();
				
			} else {
				// --- Found more than one sensor -----------------------------
				int min = 0;
				int max = sensors.size()-1;
				int sensorSelected = NumberHelper.getRandomInteger(min, max);
				
				String randomSelection = "(Index-Range: " + min + "-" + max + ", Selection: " + sensorSelected + ")";
				
				NetworkComponent netCompSensor = sensors.get(sensorSelected);
				idSensorToSubscribeTo = netCompSensor.getId();
				System.out.println(msgPrefix + ": Found " + sensors.size() + " sensors in the neighborhood. - Randomly choosed Sensor " + netCompSensor.getId() + " " + randomSelection +"!");
				
			}
		}
		return idSensorToSubscribeTo;
	}
	
	/**
	 * Gets the sensor measurement.
	 * @return the sensor measurement
	 */
	public ElectricalMeasurement getSensorMeasurement() {
		return sensorMeasurement;
	}
	/**
	 * Sets the sensor measurement.
	 * @param sensorMeasurement the new sensor measurement
	 */
	public void setSensorMeasurement(ElectricalMeasurement sensorMeasurement) {
		this.sensorMeasurement = sensorMeasurement;
	}

	
	public FixedVariableList getSetpointsToSystem() {
		return setpointsToSystem;
	}
	public void setSetpointsToSystem(FixedVariableList setpointsToSystem) {
		this.setpointsToSystem = setpointsToSystem;
	}


	public double getRequestedVoltageAdjustment() {
		return requestedVoltageAdjustment;
	}
	public void setRequestedVoltageAdjustment(double requestedVoltageAdjustment) {
		this.requestedVoltageAdjustment = requestedVoltageAdjustment;
	}


//	public Vector<AID> getAidsOfDistrictAgents() {
//		return aidsOfDistrictAgents;
//	}
//	public void setAidsOfDistrictAgents(Vector<AID> aidsOfDistrictAgents) {
//		this.aidsOfDistrictAgents = aidsOfDistrictAgents;
//	}
	
	
	public int getnDistrictAgentsForCNP() {
		return nDistrictAgentsForCNP;
	}
	public void setnDistrictAgentsForCNP(int nDistrictAgentsForCNP) {
		this.nDistrictAgentsForCNP = nDistrictAgentsForCNP;
	}

	
	public int getAcceptedVoltageAdjustment() {
		return acceptedVoltageAdjustment;
	}
	public void setAcceptedVoltageAdjustment(int acceptedVoltageAdjustment) {
		this.acceptedVoltageAdjustment = acceptedVoltageAdjustment;
	}


	public int getTransformerStep() {
		return transformerStep;
	}
	public void setTransformerStep(int transformerStep) {
		this.transformerStep = transformerStep;
	}


	public double getPossibleVoltageAdjustment() {
		return possibleVoltageAdjustment;
	}
	public void setPossibleVoltageAdjustment(double possibleVoltageAdjustment) {
		this.possibleVoltageAdjustment = possibleVoltageAdjustment;
	}


	public boolean isNewSetPointFromCNP() {
		return newSetPointFromCNP;
	}
	public void setNewSetPointFromCNP(boolean newSetPointFromCNP) {
		this.newSetPointFromCNP = newSetPointFromCNP;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.AbstractInternalDataModel#loadPhoneBookFromFile(java.io.File)
	 */
	@Override
	protected PhoneBook<EnergyAgentPhoneBookEntry> loadPhoneBookFromFile(File phoneBookFile) {
		return PhoneBook.loadPhoneBook(phoneBookFile, EnergyAgentPhoneBookEntry.class);
	}

}
