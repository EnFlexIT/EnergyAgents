package de.enflexit.ea.core.dataModel;

import java.util.TreeMap;
import java.util.Vector;

import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import de.enflexit.ea.core.dataModel.ontology.TransformerNodeProperties;

/**
 * The Class TransformerHelper provides some static help methods for {@link NetworkComponent}s
 * that represent a transformer.
 * 
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class TransformerHelper {

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
			return TransformerHelper.isTransformer(netComp);
		}
		return isTransformerNode;
	}
	/**
	 * Checks if the specified {@link NetworkComponent} is a transformer.
	 *
	 * @param networkComponentType the network component type
	 * @return true, if is transformer
	 * 
	 * @see NetworkComponent#getType()
	 */
	public static boolean isTransformer(NetworkComponent networkComponent) {
		if (networkComponent!=null) {
			return TransformerHelper.isTransformer(networkComponent.getType());
		}
		return false; 
	}
	/**
	 * Checks if the specified type of a {@link NetworkComponent} is a transformer.
	 *
	 * @param networkComponentType the network component type
	 * @return true, if is transformer
	 * 
	 * @see NetworkComponent#getType()
	 */
	public static boolean isTransformer(String networkComponentType) {
		String netCompTypeCheck = networkComponentType.toLowerCase();
		if (netCompTypeCheck.contains("transformer") || netCompTypeCheck.contains("trafo") || netCompTypeCheck.contains("transformercontrollable") || netCompTypeCheck.contains("slacknode")) {
			return true;
		}
		return false;
	}
	
	
	// ------------------------------------------------------------------------
	// --- From here handling of multiple Transformer NetworkComponents -------
	// ------------------------------------------------------------------------
	/**
	 * Based on the specified vector of NetworkComponents, returns the corresponding vector of {@link TransformerComponent}a.
	 *
	 * @param networkModel the current network model
	 * @param domain the domain
	 * @param searchVector the vector of NetworkComponets to search for Transformer
	 * @return the transformer components
	 */
	public static Vector<TransformerComponent> getTransformerComponents(NetworkModel networkModel, String domain, Vector<NetworkComponent> searchVector) {
		
		Vector<TransformerComponent> transComps = new Vector<>();
		
		// --- Find the Transformer within the NetworkComponents -------------- 
		Vector<NetworkComponent> transformerVector = TransformerHelper.getTransformerNetworkComponents(searchVector);
		for (NetworkComponent netComp : transformerVector) {
			// --- Get corresponding information ------------------------------
			GraphNode graphNode = networkModel.getGraphNodeFromDistributionNode(netComp);
			Vector<String> graphNodeDomains = TransformerHelper.getTransformerDomains(graphNode);
			
			TransformerNodeProperties transNodeProps = TransformerHelper.getTransformerNodeProperties(graphNode, domain);
			Float ratedVoltage = TransformerHelper.getRatedVoltage(transNodeProps);
			
			// --- Get the opposite node information --------------------------
			TransformerNodeProperties oppositeTransNodeProps = null;
			Float oppositeRatedVoltage = 0.0f; 
			if (graphNodeDomains.size()>1) {
				graphNodeDomains.remove(domain);
				String oppositeDomain = graphNodeDomains.get(0);
				oppositeTransNodeProps = TransformerHelper.getTransformerNodeProperties(graphNode, oppositeDomain);
				oppositeRatedVoltage = TransformerHelper.getRatedVoltage(oppositeTransNodeProps);
			}
			
			// --- Define and add TransformerComponent ------------------------ 
			transComps.add(new TransformerComponent(netComp, graphNode, transNodeProps, ratedVoltage, oppositeTransNodeProps, oppositeRatedVoltage));
		}
		return transComps;
	}
	
	/**
	 * Based on the specified vector, returns the filtered vector of transformer network components.
	 *
	 * @param searchVector the search vector
	 * @return the transformer network components
	 */
	public static Vector<NetworkComponent> getTransformerNetworkComponents(Vector<NetworkComponent> searchVector) {
		
		Vector<NetworkComponent> transformerVector = new Vector<>(); 
		if (searchVector!=null && searchVector.size()>0)  {
			for (NetworkComponent netComp : searchVector) {
				if (TransformerHelper.isTransformer(netComp)==true) {
					transformerVector.add(netComp);
				}
			}
		}
		return transformerVector;
	}
	
	/**
	 * Returns the rated voltage if configured in the specified {@link GraphNode}.
	 *
	 * @param graphNode the graph node
	 * @param domain the domain
	 * @return the rated voltage
	 */
	public static Float getRatedVoltage(GraphNode graphNode, String domain) {
		return TransformerHelper.getRatedVoltage(TransformerHelper.getTransformerNodeProperties(graphNode, domain));
	}
	/**
	 * Returns the rated voltage if configured in the specified {@link GraphNode}.
	 *
	 * @param graphNode the graph node
	 * @param domain the domain
	 * @return the rated voltage
	 */
	public static Float getRatedVoltage(TransformerNodeProperties nodeProperties) {
		if (nodeProperties!=null && nodeProperties.getRatedVoltage()!=null) {
			return nodeProperties.getRatedVoltage().getValue();
		}
		return null;
	}
	
	/**
	 * Returns domains that are available in GraphNode data model.
	 *
	 * @param graphNode the graph node
	 * @return the transformer domains
	 */
	public static Vector<String> getTransformerDomains(GraphNode graphNode) {
		
		if (graphNode==null) return null;
		
		Vector<String> domainVector = new Vector<>();
		if (graphNode.getDataModel()!=null) {
			// --- Get the object array of the data model -----------
			Object dataModel = graphNode.getDataModel();
			if (dataModel instanceof TreeMap<?,?>) {
				// --- BundlingNetworkComponentAdapter4DataModel ----
				@SuppressWarnings("unchecked")
				TreeMap<String, Object> dmTreeMap = (TreeMap<String, Object>) dataModel;
				domainVector = new Vector<>(dmTreeMap.keySet());
			}
		}
		return domainVector;
	}
	
	
	/**
	 * Returns the TransformerNodeProperties if configured in the specified {@link GraphNode}.
	 *
	 * @param graphNode the graph node
	 * @param domain the domain
	 * @return the rated voltage
	 */
	public static TransformerNodeProperties getTransformerNodeProperties(GraphNode graphNode, String domain) {
		
		if (graphNode==null || domain==null) return null;
		
		if (graphNode.getDataModel()!=null) {
			// --- Get the object array of the data model -----------
			Object dataModel = graphNode.getDataModel();
			Object[] dataModelArray = null;
			if (dataModel!=null && dataModel.getClass().isArray()) {
				// --- Regular NetworkComponentAdapter4DataModel ----
				dataModelArray = (Object[]) dataModel;
				
			} else if (dataModel instanceof TreeMap<?,?>) {
				// --- BundlingNetworkComponentAdapter4DataModel ----
				@SuppressWarnings("unchecked")
				TreeMap<String, Object> dmTreeMap = (TreeMap<String, Object>) dataModel;
				dataModel = dmTreeMap.get(domain);
				if (dataModel!=null && dataModel.getClass().isArray()) {
					dataModelArray = (Object[]) dataModel;
				}
				
			}
			
			if (dataModelArray!=null && dataModelArray.length>0) {
				// --- Get TransformerNodeProperties ----------------
				if (dataModelArray[0] instanceof TransformerNodeProperties) {
					TransformerNodeProperties nodeProperties = (TransformerNodeProperties) dataModelArray[0];
					if (nodeProperties.getRatedVoltage()!=null) {
						return nodeProperties;
					} else {
						System.err.println("==> ToDo: [" + TransformerHelper.class.getSimpleName() + "] Data model incomplete: No rated voltage specified for tranformer node " + graphNode.getId());
					}
					
				} else if (dataModelArray[0]==null){
					System.err.println("==> ToDo: [" + TransformerHelper.class.getSimpleName() + "] Data model incomplete: No properties instance set for tranformer node " + graphNode.getId());
				} else {
					System.err.println("==> ToDo: [" + TransformerHelper.class.getSimpleName() + "] Found unknow GraphNode data type '" + dataModelArray[0].getClass().getName() + "'!");
				}
				
			}
		}
		return null;
	}
	
	/**
	 * Checks if the rated voltage levels are equal for all {@link TransformerComponent}s.
	 *
	 * @param transCompsToCheck the TransformerComponents to check
	 * @return true, if is equal rated voltage
	 */
	public static boolean isEqualRatedVoltage(Vector<TransformerComponent> transCompsToCheck) {
		
		if (transCompsToCheck==null) return false;
		if (transCompsToCheck.size()==0) return true;
		
		float refRatedVoltage = transCompsToCheck.get(0).getRatedVoltage();
		for (TransformerComponent transComp :  transCompsToCheck) {
			if (transComp.getRatedVoltage()!=refRatedVoltage) return false;
		}
		return true;
	}
	
	/**
	 * Returns all rated voltages as string.
	 *
	 * @param transCompsToCheck the TransformerComponents to check
	 * @return the rated voltages as string
	 */
	public static String getRatedVoltagesAsString(Vector<TransformerComponent> transCompsToCheck) {
		
		if (transCompsToCheck==null || transCompsToCheck.size()==0) return null;
		
		String desc = "";
		for (TransformerComponent transComp :  transCompsToCheck) {
			String singleDesc = "NetComp: " + transComp.getNetworkComponent().getId() + ": " + transComp.getRatedVoltage() + " V";
			if (desc.isBlank()==false) singleDesc = ", " + singleDesc;
			desc += singleDesc;
		}
		return desc;
	}
	
	
	
}
