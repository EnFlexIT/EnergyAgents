package de.enflexit.ea.core.awbIntegration.adapter;

import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import de.enflexit.ea.core.dataModel.GlobalHyGridConstants;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseElectricalNodeState;

/**
 * The Class NetworkComponentHelper provides some static help methods for {@link NetworkComponent}s.
 */
public class NetworkComponentHelper {

	/**
	 * Answers the question if the specified {@link GraphNode} is a Transformer.
	 *
	 * @param graphNode the graph node
	 * @param networkModel the network model
	 * @return true, if is transformer
	 */
	public static boolean isTransformer(GraphNode graphNode, NetworkModel networkModel) {
	
		boolean isTransformerNode = false;
		NetworkComponent netComp = networkModel.getDistributionNode(graphNode);
		if (netComp!=null) {
			if (netComp.getType().startsWith("Transformer")==true) {
				return true;
			}
		}
		return isTransformerNode;
	}

	/**
	 * Return the actual GraphNode data model class for the specified domain.
	 *
	 * @param domain the domain
	 * @param isTransformer the is transformer
	 * @return the graph node data model class for domain
	 */
	public static Class<?> getGraphNodeDataModelClassForDomain(String domain, boolean isTransformer) {
		
		//TODO check if state or properties is required
		Class<?> clazz = null;
//		if (isTransformer==true) {
//			// --- Classes for transformer --------------------------
//			switch (domain) {
//			case GlobalHyGridConstants.HYGRID_DOMAIN_ELECTRICAL_DISTRIBUTION_GRID:
//				clazz = TriPhaseElectricalTransformerState.class;
//				break;
//				
//			case GlobalHyGridConstants.HYGRID_DOMAIN_ELECTRICITY_10KV:
//				clazz = UniPhaseElectricalTransformerState.class;
//				break;
//				
//			default:
//				break;
//			}
//			
//		} else {
			// --- Data model types for everything else ------------- 
			switch (domain) {
			case GlobalHyGridConstants.HYGRID_DOMAIN_ELECTRICITY_400V:
				clazz = TriPhaseElectricalNodeState.class;
				break;
				
			case GlobalHyGridConstants.HYGRID_DOMAIN_ELECTRICITY_10KV:
				clazz = UniPhaseElectricalNodeState.class;
				break;
				
			default:
				break;
			}
//		}
		return clazz;
	}
	
	
}
