package de.enflexit.ea.electricity.transformer.validation;

import java.util.List;
import java.util.TreeMap;

import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.ea.core.dataModel.GlobalHyGridConstants.ElectricityNetworkType;
import de.enflexit.ea.core.dataModel.ontology.TransformerNodeProperties;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.UnitValue;
import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationMessage;
import de.enflexit.ea.core.validation.HyGridValidationMessage.MessageType;
import de.enflexit.ea.electricity.ElectricityDomainIdentification;
import energy.optionModel.TechnicalSystem;

/**
 * The Class ValidateTransformer that checks the transformer data model.
 */
public class ValidateTransformer extends HyGridValidationAdapter {

	private String netCompID;
	private String domain;
	
	/* (non-Javadoc)
	 * @see net.agenthygrid.core.validation.HyGridValidationAdapter#validateNetworkComponent(org.awb.env.networkModel.NetworkComponent)
	 */
	@Override
	public HyGridValidationMessage validateNetworkComponent(NetworkComponent netComp) {

		if (netComp.getType().startsWith("Transform")==true) {
			
			this.netCompID = netComp.getId();
			
			// --- Check if a data model is specified ---------------
			Object dmNetComp = netComp.getDataModel();
			if (dmNetComp==null) {
				return new HyGridValidationMessage("No data model was specified for Transformer '" + netCompID + "'!", MessageType.Error);
			} 
			
			// --- Check the GraphNode data model -------------------
			GraphNode graphNode = this.getNetworkModel().getGraphNodeFromDistributionNode(netComp);
			Object dmGraphNode = graphNode.getDataModel();
			if (dmGraphNode==null) {
				return new HyGridValidationMessage("No data model was specified for the vertex properties of Transformer '" + netCompID + "'!", MessageType.Error);
			}
			
			// --- Get a working data model -------------------------
			TreeMap<?, ?> dmTreeMap = null;
			Object[] dmArray = null;
			if (dmGraphNode instanceof TreeMap<?, ?>) {
				dmTreeMap = (TreeMap<?, ?>) graphNode.getDataModel(); 
			} else if (dmGraphNode.getClass().isArray()) {
				dmArray = (Object[]) graphNode.getDataModel(); 
			}
			
			HyGridValidationMessage validationMessage = null;
			List<String> domainList = this.getNetworkModel().getDomain(graphNode);
			if (domainList.size()==1) {
				// --- Is data model array ---------------------------
				this.domain = domainList.get(0);
				validationMessage = this.validateDataModelArray(dmArray);
				if (validationMessage!=null) return validationMessage;
				
			} else {
				// --- Is TreeMap ? ---------------------------------
				if (dmTreeMap==null) {
					return new HyGridValidationMessage("The vertex properties for Transformer '" + netCompID + "' are misconfigured!", MessageType.Error);
				}
				// --- Iterate over domains -------------------------
				for (int i = 0; i < domainList.size(); i++) {
					this.domain = domainList.get(i);
					dmArray = (Object[]) dmTreeMap.get(domain);
					validationMessage = this.validateDataModelArray(dmArray);
					if (validationMessage!=null) return validationMessage;
				}
			}
			
		}
		return null;
	}

	/**
	 * Validates the specified data model array.
	 *
	 * @param netCompID the net comp ID
	 * @param dmArray the dm array
	 * @param domain the domain
	 * @return the hy grid validation message
	 */
	private HyGridValidationMessage validateDataModelArray(Object[] dmArray) {
		
		if (dmArray==null) {
			return new HyGridValidationMessage("The vertex properties for Transformer '" + this.netCompID + "' are misconfigured!", MessageType.Error);
		}
		
		HyGridValidationMessage validationMessage = this.validateTypeInDataModelArray(dmArray);
		if (validationMessage!=null) return validationMessage;
		
		return this.validateSlotInDataModelArray(dmArray);
	}
	/**
	 * Validates the actual type in data model array.
	 *
	 * @param dmArray the dm array
	 * @return the hy grid validation message
	 */
	private HyGridValidationMessage validateTypeInDataModelArray(Object[] dmArray) {

		if (dmArray[1]==null) {
			return new HyGridValidationMessage("The vertex properties for the Transformer '" + this.netCompID + "' are not configured!", MessageType.Error);
		}
		
		// --- Get type of electrical network -----------------------
		ElectricityNetworkType elNetType = ElectricityDomainIdentification.getElectricityNetworkType(this.domain);
		if (elNetType==null) {
			// --- Not configured for electrical networks ----------- 
			return new HyGridValidationMessage("Wrong type in vertex properties for Transformer '" + this.netCompID + "'!", MessageType.Error);
		}
		
		switch (elNetType) {
		case TriPhaseNetwork:
			if (TriPhaseElectricalNodeState.class.isInstance(dmArray[1])==false) {
				return new HyGridValidationMessage("Wrong type in vertex properties for Transformer '" + this.netCompID + "'!", MessageType.Error);
			}
			break;

		case UniPhaseNetwork:
			if (UniPhaseElectricalNodeState.class.isInstance(dmArray[1])==false) {
				return new HyGridValidationMessage("Wrong type in vertex properties for Transformer '" + this.netCompID + "'!", MessageType.Error);
			}
			break;
		}
		return null;
	}
	
	/**
	 * Validate rated voltage.
	 *
	 * @param netCompID the net comp ID
	 * @param dmArray the dm array
	 * @param domain the domain
	 * @return the hy grid validation message
	 */
	private HyGridValidationMessage validateSlotInDataModelArray(Object[] dmArray) {
	
		// --- Check data model slot ---------------------- 
		Object dataModelSlot = dmArray[0];
		if (dataModelSlot==null) {
			return new HyGridValidationMessage("The array data model value for '" + this.domain + "' in the vertex properties of Transformer '" + this.netCompID + "' is null!", MessageType.Error);
		}
		
		// --- Check the rated voltage --------------------
		TransformerNodeProperties transformerProperties = (TransformerNodeProperties) dataModelSlot;
		UnitValue ratedVoltage = transformerProperties.getRatedVoltage();
		//TODO check if rated voltage is really a static property, remove switch if so
//		switch (domain) {
//		case GlobalHyGridConstants.HYGRID_DOMAIN_ELECTRICAL_DISTRIBUTION_GRID:
//			TriPhaseElectricalTransformerState tStateTriPhase = (TriPhaseElectricalTransformerState) dataModelSlot; 
//			ratedVoltage = tStateTriPhase.getRatedVoltage();
//			break;
//
//		case GlobalHyGridConstants.HYGRID_DOMAIN_ELECTRICITY_10KV:
//			UniPhaseElectricalTransformerState tStateUniPhase = (UniPhaseElectricalTransformerState) dataModelSlot;
//			ratedVoltage = tStateUniPhase.getRatedVoltage();
//			break;
//		}
		return this.validateRatedVoltage(ratedVoltage);
	}
	
	/**
	 * Validate rated voltage.
	 *
	 * @param ratedVoltage the rated voltage
	 * @return the hy grid validation message
	 */
	private HyGridValidationMessage validateRatedVoltage(UnitValue ratedVoltage) {
		
		if (ratedVoltage==null) {
			return new HyGridValidationMessage("No rated voltage was specified for '" + this.domain + "' in the vertex properties for Transformer '" + this.netCompID + "'!", MessageType.Error);
		}
		
		if (ratedVoltage.getValue()==0) {
			return new HyGridValidationMessage("The rated voltage for '" + this.domain + "' in the vertex properties for Transformer '" + this.netCompID + "' is zero!", MessageType.Error);
		}
		
		if (ratedVoltage.getUnit()==null || ratedVoltage.getUnit().isEmpty()==true) {
			return new HyGridValidationMessage("The rated voltage unit for '" + this.domain + "' in the vertex properties for Transformer '" + this.netCompID + "' is null!", MessageType.Error);
		}
		return null;
	}
	
	
	/* (non-Javadoc)
	 * @see net.agenthygrid.core.validation.HyGridValidationAdapter#validateEomTechnicalSystem(org.awb.env.networkModel.NetworkComponent, energy.optionModel.TechnicalSystem)
	 */
	@Override
	public HyGridValidationMessage validateEomTechnicalSystem(NetworkComponent netComp, TechnicalSystem ts) {

		
		
		return null;
	}
}
