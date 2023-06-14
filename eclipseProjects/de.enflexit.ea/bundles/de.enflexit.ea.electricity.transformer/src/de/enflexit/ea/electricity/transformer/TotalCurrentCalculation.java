package de.enflexit.ea.electricity.transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.awb.env.networkModel.GraphEdge;
import org.awb.env.networkModel.GraphElement;
import org.awb.env.networkModel.GraphNode;

import de.enflexit.ea.core.dataModel.ontology.CableState;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseCableState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseCableState;
import edu.uci.ics.jung.graph.util.Pair;
import energy.domain.DefaultDomainModelElectricity.Phase;

/**
 * The Class TotalCurrentCalculation.
 */
public class TotalCurrentCalculation {

	private InternalDataModel internalDataModel;
	private TreeMap<String, CableState> cableStateTreeMap;
	
	private TreeMap<Phase, Double> totalCurrentReal;
	private TreeMap<Phase, Double> totalCurrentImag;
	
	private HashMap<String, Float> cableDirectionFactorHash;
	
	
	/**
	 * Instantiates a new total current calculation.
	 *
	 * @param internalDataModel the internal data model
	 * @param cableStateTreeMap the cable state tree map
	 */
	public TotalCurrentCalculation(InternalDataModel internalDataModel, TreeMap<String, CableState> cableStateTreeMap) {
		this.internalDataModel = internalDataModel;
		this.cableStateTreeMap = cableStateTreeMap;
		this.calculate();
	}
	
	/**
	 * Returns, if the lower voltage is provided in three phase .
	 * @return true, if is lower voltage three phase
	 */
	public boolean isLowerVoltage_ThriPhase() {
		return this.internalDataModel.getTransformerDataModel().isLowerVoltage_ThriPhase();
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
	 * Calculate.
	 */
	private void calculate() {
		
		if (this.cableStateTreeMap==null  || this.cableStateTreeMap.size()==0) return;
		
		// --- Iterate over Cable States ------------------ 
		List<String> cableIDs = new ArrayList<>(this.cableStateTreeMap.keySet()); 
		
		if (this.isLowerVoltage_ThriPhase()==false) {
			// --- Uni Phase handling
			double totalCurrentReal = 0;
			double totalCurrentImag = 0;
			
			for (int i = 0; i < cableIDs.size(); i++) {
				
				String cableID = cableIDs.get(i);
				float cdf = this.getCableDirectionFactor(cableID);
				
				// --- Case separation single or three phase --
				CableState cableState = this.cableStateTreeMap.get(cableID);
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
			// --- Thri Phase handling
			double totalCurrentReal_L1 = 0;
			double totalCurrentImag_L1 = 0;
			double totalCurrentReal_L2 = 0;
			double totalCurrentImag_L2 = 0;
			double totalCurrentReal_L3 = 0;
			double totalCurrentImag_L3 = 0;
			
			for (int i = 0; i < cableIDs.size(); i++) {
				
				String cableID = cableIDs.get(i);
				float cdf = this.getCableDirectionFactor(cableID);
				
				// --- Case separation single or three phase --
				CableState cableState = this.cableStateTreeMap.get(cableID);
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
	}
	
	/**
	 * Return the cable direction multiplication factor.
	 *
	 * @param cableID the cable ID
	 * @return the cable direction factor that is 1 or -1
	 */
	private float getCableDirectionFactor(String cableID) {
		
		Float cdf = this.getCableDirectionFactorHash().get(cableID);
		if (cdf==null) {
			// --- Get the GraphEdge from the cableID -------------------------
			GraphElement graphElement = this.internalDataModel.getNetworkModel().getGraphElement(cableID);
			if (graphElement instanceof GraphEdge) {
				// --- Get start and end node from the NetworkModel graph -----
				GraphEdge grapEdge = (GraphEdge) graphElement;
				Pair<GraphNode> edgeNodes = this.internalDataModel.getNetworkModel().getGraph().getEndpoints(grapEdge);
				GraphNode nodeFrom = edgeNodes.getFirst();
				boolean isStartingFromTransformer = nodeFrom.getId().equals(this.getLocalGraphNode().getId());

				float cdfNew = 0;
				if (isStartingFromTransformer==true) {
					cdfNew = 1;
				} else {
					cdfNew = -1;
				}
				// --- Remind this value --------------------------------------
				this.getCableDirectionFactorHash().put(cableID, cdfNew);
				return cdfNew;
			}
			
		} else {
			return cdf;
		}
		return 1;
	}
	
	/**
	 * Returns the graph node of the local component.
	 * @return the local graph node
	 */
	private GraphNode getLocalGraphNode() {
		return this.internalDataModel.getGraphNode();
	}
	/**
	 * Returns the cable direction factor hash that serves as reminder for corresponding multiplier value (1 or -1 for each Cable).
	 * @return the cable direction factor hash
	 */
	private HashMap<String, Float> getCableDirectionFactorHash() {
		if (cableDirectionFactorHash==null) {
			cableDirectionFactorHash = new HashMap<String, Float>();
		}
		return cableDirectionFactorHash;
	}
	
}
