package de.enflexit.ea.electricity.transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.awb.env.networkModel.GraphEdge;
import org.awb.env.networkModel.GraphElement;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.settings.ComponentTypeSettings;
import org.awb.env.networkModel.settings.GeneralGraphSettings4MAS;

import de.enflexit.awb.core.Application;
import de.enflexit.awb.core.project.Project;
import de.enflexit.ea.core.dataModel.GlobalHyGridConstants.ElectricityNetworkType;
import de.enflexit.ea.core.dataModel.ontology.CableState;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseCableState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseCableState;
import de.enflexit.ea.electricity.ElectricityDomainIdentification;
import de.enflexit.ea.electricity.NetworkModelToCsvMapper;
import de.enflexit.ea.electricity.aggregation.taskThreading.ElectricitySubNetworkGraph.SubNetworkGraphNode;
import edu.uci.ics.jung.graph.util.Pair;
import energy.domain.DefaultDomainModelElectricity.Phase;

/**
 * The Class TransformerTotalCurrentCalculation.
 */
public class TransformerTotalCurrentCalculation {
	
	public static final String [] DEFAULT_NEIGHBOUR_COMPOENNT_TYPES = {"Breaker", "Cable", "Sensor"};
	public static final String NEIGHBOUR_COMPONENT_TYPES_PROPERTY_KEY = "transformer.neighbourComponentTypes";
	

	private SubNetworkGraphNode subNetworkGraphNode;
	
	private ElectricityNetworkType electricityNetworkType;
	private List<String> transformerLvCable;
	private HashMap<String, Float> cableDirectionFactorHashMap;
	
	private TreeMap<Phase, Double> totalCurrentReal;
	private TreeMap<Phase, Double> totalCurrentImag;
	
	/**
	 * Instantiates a new total current calculation for the transformer of this network .
	 * @param subNetworkGraphNode the sub network graph node
	 */
	public TransformerTotalCurrentCalculation(SubNetworkGraphNode subNetworkGraphNode) {
		this.subNetworkGraphNode = subNetworkGraphNode;
		this.initialize();
	}
	/**
	 * Returns the list of low voltage cable to the transformer.
	 * @return the LV transformer cable
	 */
	private List<String> getTransformerLvCable() {
		if (transformerLvCable==null) {
			transformerLvCable = new ArrayList<>();
		}
		return transformerLvCable;
	}
	/**
	 * Returns the cable direction factor hash that serves as reminder for corresponding multiplier value (1 or -1 for each Cable).
	 * @return the cable direction factor hash
	 */
	private HashMap<String, Float> getCableDirectionFactorHashMap() {
		if (cableDirectionFactorHashMap==null) {
			cableDirectionFactorHashMap = new HashMap<String, Float>();
		}
		return cableDirectionFactorHashMap;
	}
	/**
	 * Initializes the total current calculation.
	 */
	private void initialize() {
		
		NetworkModel networkModel = this.subNetworkGraphNode.getSubNetworkConfiguration().getAggregationHandler().getNetworkModel();
		String domain = this.subNetworkGraphNode.getSubNetworkConfiguration().getDomain();
		this.electricityNetworkType = ElectricityDomainIdentification.getElectricityNetworkType(domain);
		
		NetworkModelToCsvMapper csvMapper = this.subNetworkGraphNode.getNetworkCalculationStrategy().getNetworkModelToCsvMapper();
		String transNetCompID = csvMapper.getSlackNodeVector().get(0).getNetworkComponentID();
		
		NetworkComponent netComp = networkModel.getNetworkComponent(transNetCompID);
		GraphNode transformerGraphNode = networkModel.getGraphNodeFromDistributionNode(netComp);
		
		Vector<NetworkComponent> lvNeighbours = this.getConnectedNetworkComponentsOfElectricalDomain(networkModel, netComp, domain);
		if (lvNeighbours!=null && lvNeighbours.size()>0) {
			// --- Define allowed types of NetworkComponents -------- 
			List<String> typesToConsider = getNeighbourComponentTypes();
			// --- Filter for cables and sensors --------------------
			for (NetworkComponent lvNetComp : lvNeighbours) {
				for (String allowedTypeKeyWord : typesToConsider) {
					if (lvNetComp.getType().toLowerCase().contains(allowedTypeKeyWord)) {
						// --- Remind this component ----------------
						this.getTransformerLvCable().add(lvNetComp.getId());
						// --- Remind the cable direction factor ----
						this.remindCableDirectionFactor(networkModel, transformerGraphNode, lvNetComp.getId());
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Returns the connected NetworkComponent that belong to the specified domain.<br>
	 * <b><i>See available local constants also!</i></b>
	 *
	 * @param networkModel the network model
	 * @param domain the domain specifier
	 * @return the network components connected to the transformer in the specified domain
	 */
	private Vector<NetworkComponent> getConnectedNetworkComponentsOfElectricalDomain(NetworkModel networkModel, NetworkComponent netComp, String domain) {
	
		String errMsg = null;
		
		// --- Check domain -------------------------------
		if (domain==null || domain.isEmpty()==true) {
			errMsg = "No domain was specified for the request of connected domain NetworkComponents!";
			System.err.println("[" + this.getClass().getName() + "] " + errMsg);
			return null;
		}
		// --- Unknown / illegal domain? ------------------
		if (ElectricityDomainIdentification.isElectricityDomain(domain)==false) {
			errMsg = "Unknown or illegal domain '" + domain + "' for the request of connected electrical domain NetworkComponents!";
			System.err.println("[" + this.getClass().getName() + "] " + errMsg);
			return null;
		}
		
		// --- Get connected components first -------------
		Vector<NetworkComponent> connNetComps = networkModel.getNeighbourNetworkComponents(netComp);
		if (connNetComps==null) {
			errMsg = "No NetworkComponents connected to Transformer '" + netComp.getId() + "' could be found!";
			System.err.println("[" + this.getClass().getName() + "] " + errMsg);
			return null;
		}

		
		// --- Define result list -------------------------
		Vector<NetworkComponent> netCompsFound = new Vector<NetworkComponent>();
		GeneralGraphSettings4MAS graphSettings = networkModel.getGeneralGraphSettings4MAS();
		for (int i = 0; i < connNetComps.size(); i++) {
			
			NetworkComponent netCompCheck = connNetComps.get(i);
			ComponentTypeSettings cts = graphSettings.getCurrentCTS().get(netCompCheck.getType());
			if (cts.getDomain().equals(domain)==true) {
				// --- Add to result Vector ---------------
				netCompsFound.add(netCompCheck);
			}
		}
		
		// --- Prepare return value -----------------------
		if (netCompsFound.size()==0) return null;
		return netCompsFound;
	}
	
	/**
	 * Reminds the cable direction multiplication factor for the specified cable
	 * in the local {@link #cableDirectionFactorHashMap}.
	 *
	 * @param networkModel the network model
	 * @param transformerGraphNode the transformer graph node
	 * @param cableID the cable ID
	 */
	private void remindCableDirectionFactor(NetworkModel networkModel, GraphNode transformerGraphNode, String cableID) {
		
		// --- Get the GraphEdge from the cableID -------------------------
		GraphElement graphElement = networkModel.getGraphElement(cableID);
		if (graphElement instanceof GraphEdge) {
			// --- Get start and end node from the NetworkModel graph -----
			GraphEdge grapEdge = (GraphEdge) graphElement;
			Pair<GraphNode> edgeNodes = networkModel.getGraph().getEndpoints(grapEdge);
			GraphNode nodeFrom = edgeNodes.getFirst();
			boolean isStartingFromTransformer = nodeFrom.getId().equals(transformerGraphNode.getId());

			float cdfNew = 0;
			if (isStartingFromTransformer==true) {
				cdfNew = 1;
			} else {
				cdfNew = -1;
			}
			// --- Remind this value --------------------------------------
			this.getCableDirectionFactorHashMap().put(cableID, cdfNew);
		}
	}
	
	/**
	 * Returns the total current real.
	 * @return the total current real
	 */
	public TreeMap<Phase, Double> getTotalCurrentReal() {
		if (totalCurrentReal == null) {
			totalCurrentReal = new TreeMap<Phase, Double>();
		}
		return totalCurrentReal;
	}
	/**
	 * Returns the total current imag.
	 * @return the total current imag
	 */
	public TreeMap<Phase, Double> getTotalCurrentImag() {
		if (totalCurrentImag == null) {
			totalCurrentImag = new TreeMap<Phase, Double>();
		}
		return totalCurrentImag;
	}
	
	/**
	 * Returns the current cable states as tree map.
	 * @return the cable state tree map
	 */
	private TreeMap<String, CableState> getCableStateTreeMap() {
		
		TreeMap<String, CableState> cableStateTreeMap = new TreeMap<>();
		for (String cableID : this.getTransformerLvCable()) {
			CableState cableState = this.subNetworkGraphNode.getNetworkCalculationStrategy().getCableStates().get(cableID);
			cableStateTreeMap.put(cableID, cableState);
		}
		return cableStateTreeMap;
	}
	
	/**
	 * Based on the available information, calculates the real and imaginary total current.
	 * @return the current instance of the TransformerTotalCurrentCalculation
	 */
	public TransformerTotalCurrentCalculation calculate() {

		// --- Reset current result variables -------------
		this.totalCurrentReal = null;
		this.totalCurrentImag = null;
		
		TreeMap<String, CableState> cableStateTreeMap = this.getCableStateTreeMap();
		if (cableStateTreeMap==null  || cableStateTreeMap.size()==0) return this;
		
		// --- Iterate over Cable States ------------------ 
		List<String> cableIDs = new ArrayList<>(cableStateTreeMap.keySet()); 
		
		if (this.electricityNetworkType==ElectricityNetworkType.UniPhaseNetwork) {
			// --------------------------------------------
			// --- UniPhase handling ---------------------- 
			// --------------------------------------------
			double totalCurrentReal = 0;
			double totalCurrentImag = 0;
			
			for (int i = 0; i < cableIDs.size(); i++) {
				
				String cableID = cableIDs.get(i);
				float cdf = this.getCableDirectionFactorHashMap().get(cableID);
				
				// --- Case separation single or three phase --
				CableState cableState = cableStateTreeMap.get(cableID);
				if (cableState instanceof UniPhaseCableState) {
					UniPhaseCableState upCableState  = (UniPhaseCableState) cableState; 
					
					double currentReal = upCableState.getCurrentReal().getValue() * cdf;
					double currentImag = upCableState.getCurrentImag().getValue() * cdf;
					
					totalCurrentReal += currentReal;
					totalCurrentImag += currentImag;
				}
			}
			
			// --- Save total currents as real and imaginary
			this.getTotalCurrentReal().put(Phase.AllPhases, totalCurrentReal);
			this.getTotalCurrentImag().put(Phase.AllPhases, totalCurrentImag);
			
		} else {
			// --------------------------------------------
			// --- ThriPhase handling ---------------------
			// --------------------------------------------
			double totalCurrentReal_L1 = 0;
			double totalCurrentImag_L1 = 0;
			double totalCurrentReal_L2 = 0;
			double totalCurrentImag_L2 = 0;
			double totalCurrentReal_L3 = 0;
			double totalCurrentImag_L3 = 0;
			
			for (int i = 0; i < cableIDs.size(); i++) {
				
				String cableID = cableIDs.get(i);
				float cdf = this.getCableDirectionFactorHashMap().get(cableID);
				
				// --- Case separation single or three phase --
				CableState cableState = cableStateTreeMap.get(cableID);
				if (cableState instanceof TriPhaseCableState) {
					TriPhaseCableState tpCableState  = (TriPhaseCableState) cableState;
					
					double currentReal_L1 = tpCableState.getPhase1().getCurrentReal().getValue() * cdf;
					double currentReal_L2 = tpCableState.getPhase2().getCurrentReal().getValue() * cdf;
					double currentReal_L3 = tpCableState.getPhase3().getCurrentReal().getValue() * cdf;
					double currentImag_L1 = tpCableState.getPhase1().getCurrentImag().getValue() * cdf;
					double currentImag_L2 = tpCableState.getPhase2().getCurrentImag().getValue() * cdf;
					double currentImag_L3 = tpCableState.getPhase3().getCurrentImag().getValue() * cdf;
					
					totalCurrentReal_L1 += currentReal_L1;
					totalCurrentReal_L2 += currentReal_L2;
					totalCurrentReal_L3 += currentReal_L3;
					totalCurrentImag_L1 += currentImag_L1;
					totalCurrentImag_L2 += currentImag_L2;
					totalCurrentImag_L3 += currentImag_L3;
					
				}
			}
			
			// --- Save total currents as real and imaginary
			this.getTotalCurrentReal().put(Phase.L1, totalCurrentReal_L1);
			this.getTotalCurrentReal().put(Phase.L2, totalCurrentReal_L2);
			this.getTotalCurrentReal().put(Phase.L3, totalCurrentReal_L3);
			
			this.getTotalCurrentImag().put(Phase.L1, totalCurrentImag_L1);
			this.getTotalCurrentImag().put(Phase.L2, totalCurrentImag_L2);
			this.getTotalCurrentImag().put(Phase.L3, totalCurrentImag_L3);
		}
		return this;
	}
	
	/**
	 * Gets the component types that should be considered as possible neighbor components of transformers.
	 * @return the neighbor component types
	 */
	public static ArrayList<String> getNeighbourComponentTypes(){
		String[] componentTypes = null;
		
		// ---  Get the component types from the project properties -----------
		Project project = Application.getProjectFocused();
		if (project!=null) {
			String propertiesEntry = project.getProperties().getStringValue(NEIGHBOUR_COMPONENT_TYPES_PROPERTY_KEY);
			if (propertiesEntry!=null && propertiesEntry.isBlank()==false) {
				componentTypes = propertiesEntry.split(",");
			}
		}
		
		// --- If not successful, use the defaults  ---------------------------
		if (componentTypes==null) {
			componentTypes = DEFAULT_NEIGHBOUR_COMPOENNT_TYPES;
		}
		
		// --- Convert to a list of lower case strings ------------------------
		ArrayList<String> neighbourComponentTypes = new ArrayList<>();
		for (String componentType : componentTypes) {
			neighbourComponentTypes.add(componentType.toLowerCase());
		}
		return neighbourComponentTypes;
	}
	
}
