package de.enflexit.ea.electricity.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.awb.env.networkModel.GraphElement;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import de.enflexit.ea.core.dataModel.TransformerHelper;
import de.enflexit.ea.core.dataModel.ontology.TransformerNodeProperties;
import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationMessage;
import de.enflexit.ea.core.validation.HyGridValidationMessage.MessageType;
import de.enflexit.ea.electricity.transformer.TransformerCalculation;
import de.enflexit.ea.electricity.transformer.TransformerDataModel;
import de.enflexit.ea.electricity.transformer.TransformerDataModel.TapSide;
import energy.optionModel.TechnicalSystem;

/**
 * The Class TransformerVoltageLevelChecks.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class TransformerVoltageLevelChecks extends HyGridValidationAdapter {
	
	private HashMap<String, TransformerInformationCollector> transformerInformationHashMap;

	/**
	 * Gets the transformer information hash map.
	 * @return the transformer information hash map
	 */
	private HashMap<String, TransformerInformationCollector> getTransformerInformationHashMap() {
		if (transformerInformationHashMap==null) {
			transformerInformationHashMap = new HashMap<>();
		}
		return transformerInformationHashMap;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.validation.HyGridValidationAdapter#validateNetworkModel(org.awb.env.networkModel.NetworkModel)
	 */
	@Override
	public HyGridValidationMessage validateNetworkModel(NetworkModel networkModel) {
		
		// -------------------------------------------------------------------- 
		// --- This method basically collects the required information --------
		// --- for the actual checks from the NetworkModel. -------------------
		// --- The EOM information will be considered later in the method ----- 
		// --- validateEomTechnicalSystem(NetworkComponent, TechnicalSystem) --
		// --------------------------------------------------------------------
		
		// --- Clear previous information -------------------------------------
		this.getTransformerInformationHashMap().clear();
		
		// --- Filter for all Transformers configured ------------------------- 
		List<NetworkComponent> transFormerNetComps = new ArrayList<>();
		for (NetworkComponent netComp : networkModel.getNetworkComponentVectorSorted()) {
			if (TransformerHelper.isTransformer(netComp)==true) transFormerNetComps.add(netComp); 
		}
		if (transFormerNetComps.size()==0) return null;

		
		// --- Get the transformer GraphNodes and further information ---------
		for (NetworkComponent netComp : transFormerNetComps) {

			TechnicalSystem ts = null;
			if (netComp.getDataModel() instanceof TechnicalSystem) {
				ts = (TechnicalSystem) netComp.getDataModel();
			}
			
			// --- Get the Domain of the NetworkComponent ---------------------
			String domain = networkModel.getDomain(netComp);
			
			// --- Get the GraphNode of the transformer -----------------------
			Vector<GraphElement> graphElements =  networkModel.getGraphElementsFromNetworkComponent(netComp);
			if (graphElements==null || graphElements.size()!=1 || !(graphElements.get(0) instanceof GraphNode)) continue; 
			GraphNode graphNode = (GraphNode) graphElements.get(0);
			
			// ----------------------------------------------------------------
			// --- Get the TransformerNodeProperties --------------------------
			// ----------------------------------------------------------------
			TransformerNodeProperties tnp1 = null;
			TransformerNodeProperties tnp2 = null;
			
			Object graphNodeDataModel = graphNode.getDataModel();
			if (graphNodeDataModel instanceof TreeMap<?, ?>) {
				@SuppressWarnings("unchecked")
				TreeMap<String, Object> dmNode = (TreeMap<String, Object>) graphNodeDataModel;
				for (String dmDomain : dmNode.keySet()) {
					Object dmNodeSingleDomain = dmNode.get(dmDomain);
					if (dmNodeSingleDomain.getClass().isArray()==true) {
						Object[] dmArrayNodeSingleDomain = (Object[]) dmNodeSingleDomain;
						// --- If this goes wrong, it might be, that there is further domain  
						try {
							if (tnp1==null) {
								tnp1 = (TransformerNodeProperties) dmArrayNodeSingleDomain[0];
								continue;
							}
							if (tnp2==null) {
								tnp2 = (TransformerNodeProperties) dmArrayNodeSingleDomain[0];
								continue;
							}
						} catch (Exception ex) {
							//ex.printStackTrace();
						}
					}
				}
				
			} else {
				// --- Found a single, regular NetworkComponentAdapter --------
				if (graphNodeDataModel!=null && graphNodeDataModel.getClass().isArray()==true) {
					Object[] dmArrayNodeSingleDomain = (Object[]) graphNodeDataModel;
					tnp1 = (TransformerNodeProperties) dmArrayNodeSingleDomain[0];
				}
				
			}
			
			// ----------------------------------------------------------------
			// --- Create TransformerInformationCollector instance ------------
			// ----------------------------------------------------------------
			TransformerInformationCollector transInfoCollector = new TransformerInformationCollector();
			transInfoCollector.setNetworkComponent(netComp);
			transInfoCollector.setDomain(domain);
			transInfoCollector.setGraphNode(graphNode);
			transInfoCollector.setTransformerNodeProperties(tnp1, tnp2);
			transInfoCollector.setTechnicalSystem(ts);
			
			// --- Put to local HashMap ---------------------------------------
			this.getTransformerInformationHashMap().put(netComp.getId(), transInfoCollector);
		}
		return null;
	}
	
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.validation.HyGridValidationAdapter#validateEomTechnicalSystem(org.awb.env.networkModel.NetworkComponent, energy.optionModel.TechnicalSystem)
	 */
	@Override
	public HyGridValidationMessage validateEomTechnicalSystem(NetworkComponent netComp, TechnicalSystem ts) {
		
		TransformerInformationCollector tric = this.getTransformerInformationHashMap().get(netComp.getId());
		if (tric==null) return null;
		
		// ------------------------------------------------------------------------------
		// --- Do checks & produce validation messages ----------------------------------
		// ------------------------------------------------------------------------------		
		String description = "";
		description = this.appendToDescription(description, tric.checkGraphNodeVoltageLevel());
		description = this.appendToDescription(description, tric.checkTransformerDataModel());
		description = this.appendToDescription(description, tric.checkTransformerDataModelCombinedWithGraphNodeModels());
		
		if (description.isEmpty()==false) {
			String message = "Configuration errors for transformer '" + tric.getNetworkComponent().getId() + "' (Domain: " + tric.getDomain() + ", GraphNode: " + tric.getGraphNode().getId() + " )";
			return new HyGridValidationMessage(message, MessageType.Error, description);
		}
		return null;
	}
	
	/**
	 * Method to append new check results to the specified description.
	 *
	 * @param description the current description
	 * @param additionalDescription the additional description
	 * @return the new resulting description 
	 */
	private String appendToDescription(String description, String additionalDescription) {
		
		if (additionalDescription==null || additionalDescription.isBlank()==true) return description;
		
		if (description==null || description.isBlank()==true) {
			description = "- " + additionalDescription;
		} else {
			description += "\n- " + additionalDescription; 
		}
		return description;
	}
	
	/**
	 * The Class TransformerInformationCollector.
	 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
	 */
	private class TransformerInformationCollector {
		
		private NetworkComponent networkComponent;
		private String domain;
		
		private GraphNode graphNode;
		
		private TransformerNodeProperties uvTransformerNodeProperties;
		private TransformerNodeProperties lvTransformerNodeProperties;
		private Double uvNodeVoltageLevel;
		private Double lvNodeVoltageLevel;
		
		private TechnicalSystem technicalSystem;
		private TransformerDataModel transformerDataModel;
		

		public NetworkComponent getNetworkComponent() {
			return networkComponent;
		}
		public void setNetworkComponent(NetworkComponent networkComponent) {
			this.networkComponent = networkComponent;
		}
		
		public String getDomain() {
			return domain;
		}
		public void setDomain(String domain) {
			this.domain = domain;
		}
		
		public GraphNode getGraphNode() {
			return graphNode;
		}
		public void setGraphNode(GraphNode graphNode) {
			this.graphNode = graphNode;
		}
		
		/**
		 * Sets the transformer node properties to the collector.
		 *
		 * @param tnp1 the TransformerNodeProperties no. 1
		 * @param tnp2 the TransformerNodeProperties no. 2
		 */
		public void setTransformerNodeProperties(TransformerNodeProperties tnp1, TransformerNodeProperties tnp2) {

			if (tnp1==null && tnp2==null) return;

			// --- Evaluate TransformerNodeProperties -------------------------
			if (tnp2==null) {
				this.setLvTransformerNodeProperties(tnp1);
				return;
			}
			
			// --- Both properties are available ------------------------------
			Double tnp1VoltageLevel = this.getRatedVoltage(tnp1);
			Double tnp2VoltageLevel = this.getRatedVoltage(tnp2);
			if (tnp1VoltageLevel < tnp2VoltageLevel) {
				this.setLvTransformerNodeProperties(tnp1);
				this.setUvTransformerNodeProperties(tnp2);
			} else {
				this.setLvTransformerNodeProperties(tnp2);
				this.setUvTransformerNodeProperties(tnp1);
			}
		}
		/**
		 * Returns the rated voltage of the specified TransformerNodeProperties.
		 *
		 * @param tnp the TransformerNodeProperties
		 * @return the rated voltage
		 */
		private double getRatedVoltage(TransformerNodeProperties tnp) {
			if (tnp==null || tnp.getRatedVoltage()==null) return 0;
			return tnp.getRatedVoltage().getValue();
		}
		
		
		public TransformerNodeProperties getUvTransformerNodeProperties() {
			return uvTransformerNodeProperties;
		}
		public void setUvTransformerNodeProperties(TransformerNodeProperties uvTransformerNodeProperties) {
			this.uvTransformerNodeProperties = uvTransformerNodeProperties;
			if (this.uvTransformerNodeProperties!=null) {
				this.setUvNodeVoltageLevel(this.getRatedVoltage(this.uvTransformerNodeProperties));
			}
		}
		
		public Double getUvNodeVoltageLevel() {
			return uvNodeVoltageLevel;
		}
		public void setUvNodeVoltageLevel(Double uvVoltageLevel) {
			this.uvNodeVoltageLevel = uvVoltageLevel;
		}

		
		public TransformerNodeProperties getLvTransformerNodeProperties() {
			return lvTransformerNodeProperties;
		}
		public void setLvTransformerNodeProperties(TransformerNodeProperties lvTransformerNodeProperties) {
			this.lvTransformerNodeProperties = lvTransformerNodeProperties;
			if (this.lvTransformerNodeProperties!=null) {
				this.setLvNodeVoltageLevel(this.getRatedVoltage(this.lvTransformerNodeProperties));
			}
			
		}
		public Double getLvNodeVoltageLevel() {
			return lvNodeVoltageLevel;
		}
		public void setLvNodeVoltageLevel(Double lvVoltageLevel) {
			this.lvNodeVoltageLevel = lvVoltageLevel;
		}

		
		public TechnicalSystem getTechnicalSystem() {
			return technicalSystem;
		}
		public void setTechnicalSystem(TechnicalSystem technicalSystem) {
			this.technicalSystem = technicalSystem;
			if (this.technicalSystem==null) return;
			this.setTransformerDataModel(TransformerCalculation.getTransformerDataModelFromTechnicalSystem(this.technicalSystem));
		}
		
		public TransformerDataModel getTransformerDataModel() {
			return transformerDataModel;
		}
		public void setTransformerDataModel(TransformerDataModel transformerDataModel) {
			this.transformerDataModel = transformerDataModel;
		}

		
		// --------------------------------------------------------------------
		// --- From here, all detail checks are available ---------------------
		// --------------------------------------------------------------------		
		/**
		 * Checks the graph node voltage level.
		 * @return the string
		 */
		public String checkGraphNodeVoltageLevel() {
			
			boolean isDebug = false;
			List<String> errMsgList = new ArrayList<>();
			String configHint = "\n   (see 'Edit Vertex Properties' in the NetworkComponent's context menue)";
			
			if (isDebug==true || this.getLvTransformerNodeProperties()==null) {
				errMsgList.add("An instance of TransformerNodeProperties for the low voltage level is missing." + configHint);
			}
			if (isDebug==true ||this.getUvTransformerNodeProperties()==null) {
				errMsgList.add("An instance of TransformerNodeProperties for the high voltage level is missing." + configHint);
			}
			if (isDebug==true ||this.getLvNodeVoltageLevel()==null) {
				errMsgList.add("No voltage level could be found for the low voltage side." + configHint);
			}
			if (isDebug==true ||this.getUvNodeVoltageLevel()==null) {
				errMsgList.add("No voltage level could be found for the high voltage side." + configHint);
			}
			
			if (isDebug==true || (this.getLvNodeVoltageLevel()!=null && this.getUvNodeVoltageLevel()!=null)) {
				if (isDebug==true ||this.getLvNodeVoltageLevel().equals(this.getUvNodeVoltageLevel())==true) {
					errMsgList.add("The high and the low voltage levels are equal." + configHint);
				}
			}
			return String.join("\n- ", errMsgList);
		}
		
		/**
		 * Checks the transformer data model.
		 * @return the error message or null
		 */
		public String checkTransformerDataModel() {
			
			boolean isDebug = false;
			List<String> errMsgList = new ArrayList<>();
			String configHint = "\n   (see 'Edit Properties' for '" + this.getNetworkComponent().getType() + ": " + this.getNetworkComponent().getId() + "' in the NetworkComponent's context menue)";
			
			if (isDebug==true || this.getTechnicalSystem()==null) {
				errMsgList.add("No TechnicalSystem definition was found." + configHint);
			}
			if (isDebug==true || this.getTransformerDataModel()==null) {
				errMsgList.add("No TransformerDataModel was found." + configHint);
			}
			
			if (isDebug==true || this.getTransformerDataModel()!=null) {

				double lowerVoltageLevel = this.getTransformerDataModel().getLowerVoltage_vmLV() * 1000.0;
				double upperVoltageLevel = this.getTransformerDataModel().getUpperVoltage_vmHV() * 1000.0;
				TapSide slackNodeSide = this.getTransformerDataModel().getSlackNodeSide();
				double slackNodeVoltageLevel = this.getTransformerDataModel().getSlackNodeVoltageLevel();
				
				// --- Voltage level checks -------------------------
				if (isDebug==true || lowerVoltageLevel==0) {
					errMsgList.add("The lower voltage level in the TransfomerDataModel is 0." + configHint);
				}
				if (isDebug==true || upperVoltageLevel==0) {
					errMsgList.add("The upper voltage level in the TransfomerDataModel is 0." + configHint);
				}
				if (isDebug==true || lowerVoltageLevel==upperVoltageLevel) {
					errMsgList.add("The lower voltage level and the upper voltage level in the TransfomerDataModel is equal." + configHint);
				}
				if (isDebug==true || lowerVoltageLevel>upperVoltageLevel) {
					errMsgList.add("The lower voltage level is higher than the lower voltage level in the TransfomerDataModel." + configHint);
				}
				
				// --- SlackNode Checks -----------------------------
				switch (slackNodeSide) {
				case LowVoltageSide:
					if (isDebug==true || slackNodeVoltageLevel!=lowerVoltageLevel) {
						errMsgList.add("The configured slack node voltage level is different to the defined low voltage level of the TransfomerDataModel." + configHint);
					}
					break;

				case HighVoltageSide:
					if (isDebug==true || slackNodeVoltageLevel!=upperVoltageLevel) {
						errMsgList.add("The configured slack node voltage level is different to the defined high voltage level of the TransfomerDataModel." + configHint);
					}
					break;
				}
								
			}
			return String.join("\n- ", errMsgList);
		}
		
		/**
		 * Check transformer data model combined with graph node models.
		 * @return the error message or null
		 */
		public String checkTransformerDataModelCombinedWithGraphNodeModels() {
			
			boolean isDebug = false;
			List<String> errMsgList = new ArrayList<>();
			String configHint = "\n   (see 'Edit Vertex Properties' or 'Edit Properties' for '" + this.getNetworkComponent().getType() + ": " + this.getNetworkComponent().getId() + "' in the NetworkComponent's context menue)";
			
			// --- If nothing is available simply return here -----------------
			if (this.getLvNodeVoltageLevel()==null) return null;
			if (this.getUvNodeVoltageLevel()==null) return null;
			if (this.getTransformerDataModel()==null) return null;

			
			double lowerVoltageLevel = this.getTransformerDataModel().getLowerVoltage_vmLV() * 1000.0;
			double upperVoltageLevel = this.getTransformerDataModel().getUpperVoltage_vmHV() * 1000.0;
			if (isDebug==true || this.getLvNodeVoltageLevel()!=lowerVoltageLevel) {
				errMsgList.add("The configured low voltage levels in Graph Node and TransfomerDataModel are NOT equal (" + this.getLvNodeVoltageLevel() + " vs. " + lowerVoltageLevel + ")." + configHint);
			}
			if (isDebug==true || this.getUvNodeVoltageLevel()!=upperVoltageLevel) {
				errMsgList.add("The configured high voltage levels in Graph Node and TransfomerDataModel are NOT equal (" + this.getUvNodeVoltageLevel() + " vs. " + upperVoltageLevel + ")." + configHint);
			}
			return String.join("\n- ", errMsgList);
		}
	} // end sub class 
	
}
