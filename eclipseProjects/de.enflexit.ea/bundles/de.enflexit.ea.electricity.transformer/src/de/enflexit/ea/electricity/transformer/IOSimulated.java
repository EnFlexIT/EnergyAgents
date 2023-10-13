package de.enflexit.ea.electricity.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import agentgui.simulationService.transaction.EnvironmentNotification;
import de.enflexit.ea.core.AbstractIOSimulated;
import de.enflexit.ea.core.dataModel.GlobalHyGridConstants;
import de.enflexit.ea.core.dataModel.blackboard.AbstractBlackboardAnswer;
import de.enflexit.ea.core.dataModel.blackboard.BlackboardRequest;
import de.enflexit.ea.core.dataModel.blackboard.MultipleBlackboardAnswer;
import de.enflexit.ea.core.dataModel.blackboard.SingleRequestSpecifier;
import de.enflexit.ea.core.dataModel.blackboard.SingleRequestSpecifier.RequestType;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseElectricalNodeState;
import de.enflexit.ea.electricity.blackboard.CurrentLevelAnswer;
import de.enflexit.ea.electricity.blackboard.ElectricityRequestObjective;
import de.enflexit.ea.electricity.blackboard.VoltageLevelAnswer;
import de.enflexit.ea.electricity.transformer.TransformerDataModel.TransformerSystemVariable;
import energy.FixedVariableList;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.optionModel.FixedDouble;
import jade.core.AID;


/**
 * The Class IOSimulated is used to simulate measurements from an energy conversion 
 * process, if the current project setup is used for simulations.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class IOSimulated extends AbstractIOSimulated {

	private static final long serialVersionUID = 3659353239575036308L;
	
	/**
	 * Instantiates a the simulated IO interface of the {@link TransformerAgent}.
	 * @param agent the instance of the TransformerAgent
	 */
	public IOSimulated(TransformerAgent agent) {
		super(agent);
		this.setTechnicalSystemStateFromRealTimeControlBehaviourToEnvironmentModel(false);
	}
	
	/**
	 * Processes single blackboard answers.
	 * @param aBlackAnswer the blackboard answer
	 */
	private void processBlackboardAnswer(AbstractBlackboardAnswer aBlackAnswer) {
		
		// --- Get the transformers internal data model -----------------------
		InternalDataModel intDM = (InternalDataModel) this.getInternalDataModel();

		if (aBlackAnswer instanceof CurrentLevelAnswer) {
			// --- CurrentLevelAnswer -----------------------------------------
			CurrentLevelAnswer cla = (CurrentLevelAnswer) aBlackAnswer;
			if (cla!=null) {
				intDM.updateLowVoltageCurrentLevel(cla.getIdentifier(), cla.getCableState());
			}
		
		} else if (aBlackAnswer instanceof VoltageLevelAnswer) {
			// --- A voltage level answer -------------------------------------
			double uReal = 0;
			double uImag = 0;
			VoltageLevelAnswer vla = (VoltageLevelAnswer) aBlackAnswer;
			if (vla.getElectricalNodeState() instanceof TriPhaseElectricalNodeState) {
				// --- Three phase flow ---------------------------------------
				TriPhaseElectricalNodeState tpNodeState = (TriPhaseElectricalNodeState) vla.getElectricalNodeState(); 
				if (tpNodeState.getL1()!=null) {
					uReal = tpNodeState.getL1().getVoltageRealNotNull().getValue() * Math.sqrt(3.0);
					uImag = tpNodeState.getL1().getVoltageImagNotNull().getValue() * Math.sqrt(3.0);
				}
				
			} else if (vla.getElectricalNodeState() instanceof UniPhaseElectricalNodeState) {
				// --- Single Phase flow --------------------------------------
				UniPhaseElectricalNodeState upNodeState = (UniPhaseElectricalNodeState) vla.getElectricalNodeState();
				uReal = upNodeState.getVoltageRealNotNull().getValue();
				uImag = upNodeState.getVoltageImagNotNull().getValue();
			}
			
			// --- Convert into 'measurement from system' ---------------------
			FixedDouble fdVoltageReal = new FixedDouble();
			fdVoltageReal.setVariableID(TransformerSystemVariable.cnVoltageReal.name());
			fdVoltageReal.setValue(uReal);
			this.getMeasurementsFromSystem().addOrEdit(fdVoltageReal);
			
			FixedDouble fdVoltageImag = new FixedDouble();
			fdVoltageImag.setVariableID(TransformerSystemVariable.cnVoltageImag.name());
			fdVoltageImag.setValue(uImag);
			this.getMeasurementsFromSystem().addOrEdit(fdVoltageImag);
			
		} else {
			// --- Unknown type of blackboard request -------------------------
			System.out.println("[" + this.getEnergyAgent().getClass().getSimpleName() + "] Unknow blackboard information of type " + aBlackAnswer.getClass().getName());
		}
	}

	/* (non-Javadoc)
	 * @see agentgui.simulationService.behaviour.SimulationServiceBehaviour#onEnvironmentNotification(agentgui.simulationService.transaction.EnvironmentNotification)
	 */
	@Override
	protected EnvironmentNotification onEnvironmentNotification(EnvironmentNotification notification) {
		
		// --- Get the transformers internal data model -----------------------
		InternalDataModel intDM = (InternalDataModel) this.getInternalDataModel();
		
		// --------------------------------------------------------------------
		// --- Evaluate notification ------------------------------------------
		Object noteContent = notification.getNotification(); 
		if (noteContent instanceof AbstractBlackboardAnswer) {
			// --- Check type of BlackboardAnswer -----------------------------
			AbstractBlackboardAnswer aBlackAnswer = (AbstractBlackboardAnswer) noteContent;
			if (notification.getNotification() instanceof MultipleBlackboardAnswer) {
				// --- MultipleBlackboardAnswers ------------------------------
				MultipleBlackboardAnswer muBlaAnswer = (MultipleBlackboardAnswer) notification.getNotification();
				for (int i = 0; i < muBlaAnswer.getAnswerVector().size(); i++) {
					this.processBlackboardAnswer(muBlaAnswer.getAnswerVector().get(i));
				}
				
			} else {
				// --- Single BlackboardAnswers -------------------------------
				this.processBlackboardAnswer(aBlackAnswer);
			}
		}
		
		// --------------------------------------------------------------------
		// --- Execute total calculation --------------------------------------
		TotalCurrentCalculation tcc = intDM.executeTotalCurrentCalculation();
		
		// --------------------------------------------------------------------
		// --- Update agents IO measurements ----------------------------------
		FixedVariableList measurements = this.getMeasurementsFromSystem();

		if (tcc.isLowerVoltage_ThriPhase()==false) {
			// --- Uni phase current ------------------------------------------
			FixedDouble fdLvTotalCurrentReal = new FixedDouble();
			fdLvTotalCurrentReal.setVariableID(TransformerSystemVariable.lvTotaCurrentRealAllPhases.name());
			fdLvTotalCurrentReal.setValue(tcc.getTotalCurrentReal().get(Phase.AllPhases));
			measurements.addOrEdit(fdLvTotalCurrentReal);
			
			FixedDouble fdLvTotalCurrentImag = new FixedDouble();
			fdLvTotalCurrentImag.setVariableID(TransformerSystemVariable.lvTotaCurrentImagAllPhases.name());
			fdLvTotalCurrentImag.setValue(tcc.getTotalCurrentImag().get(Phase.AllPhases));
			measurements.addOrEdit(fdLvTotalCurrentImag);
			
		} else {
			// --- Tree phase current -----------------------------------------
			FixedDouble fdLvTotalCurrentRealL1 = new FixedDouble();
			fdLvTotalCurrentRealL1.setVariableID(TransformerSystemVariable.lvTotaCurrentRealL1.name());
			fdLvTotalCurrentRealL1.setValue(tcc.getTotalCurrentReal().get(Phase.L1));
			measurements.addOrEdit(fdLvTotalCurrentRealL1);
			
			FixedDouble fdLvTotalCurrentRealL2 = new FixedDouble();
			fdLvTotalCurrentRealL2.setVariableID(TransformerSystemVariable.lvTotaCurrentRealL2.name());
			fdLvTotalCurrentRealL2.setValue(tcc.getTotalCurrentReal().get(Phase.L2));
			measurements.addOrEdit(fdLvTotalCurrentRealL2);
			
			FixedDouble fdLvTotalCurrentRealL3 = new FixedDouble();
			fdLvTotalCurrentRealL3.setVariableID(TransformerSystemVariable.lvTotaCurrentRealL3.name());
			fdLvTotalCurrentRealL3.setValue(tcc.getTotalCurrentReal().get(Phase.L3));
			measurements.addOrEdit(fdLvTotalCurrentRealL3);
			
			
			FixedDouble fdLvTotalCurrentImagL1 = new FixedDouble();
			fdLvTotalCurrentImagL1.setVariableID(TransformerSystemVariable.lvTotaCurrentImagL1.name());
			fdLvTotalCurrentImagL1.setValue(tcc.getTotalCurrentImag().get(Phase.L1));
			measurements.addOrEdit(fdLvTotalCurrentImagL1);
			
			FixedDouble fdLvTotalCurrentImagL2 = new FixedDouble();
			fdLvTotalCurrentImagL2.setVariableID(TransformerSystemVariable.lvTotaCurrentImagL2.name());
			fdLvTotalCurrentImagL2.setValue(tcc.getTotalCurrentImag().get(Phase.L2));
			measurements.addOrEdit(fdLvTotalCurrentImagL2);
			
			FixedDouble fdLvTotalCurrentImagL3 = new FixedDouble();
			fdLvTotalCurrentImagL3.setVariableID(TransformerSystemVariable.lvTotaCurrentImagL3.name());
			fdLvTotalCurrentImagL3.setValue(tcc.getTotalCurrentImag().get(Phase.L3));
			measurements.addOrEdit(fdLvTotalCurrentImagL3);
		}
		
		this.commitMeasurementsToAgent();
		
		return super.onEnvironmentNotification(notification);
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.AbstractIOSimulated#commitMeasurementsToAgentsManually()
	 */
	@Override
	protected boolean commitMeasurementsToAgentsManually() {
		return true;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.AbstractIOSimulated#prepareForSimulation(org.awb.env.networkModel.helper.NetworkModel)
	 */
	@Override
	protected void prepareForSimulation(NetworkModel networkModel) {

		// --- Get internal data model of Transformer --------------- 
		InternalDataModel intDM = (InternalDataModel) this.getEnergyAgent().getInternalDataModel();
		Vector<SingleRequestSpecifier> requestSpecifierVector = new Vector<SingleRequestSpecifier>();
		
		// ----------------------------------------------------------
		// --- Check for neighbor components ------------------------
		String domain = networkModel.getDomain(intDM.getNetworkComponent());
		Vector<NetworkComponent> lvNeighbours = intDM.getConnectedNetworkComponentsOfElectricalDomain(domain);
		if (lvNeighbours!=null && lvNeighbours.size()>0) {
			// --- Define allowed types of NetworkComponents -------- 
			List<String> allowedTypeKeyWords = new ArrayList<String>();
			allowedTypeKeyWords.add("Cable".toLowerCase());
			allowedTypeKeyWords.add("Sensor".toLowerCase());
			// --- Filter for cables and sensors --------------------
			for (NetworkComponent lvNetComp : lvNeighbours) {
				for (String allowedTypeKeyWord : allowedTypeKeyWords) {
					if (lvNetComp.getType().toLowerCase().contains(allowedTypeKeyWord)) {
						// --- Prepare BlackboardRequest ----------------
						requestSpecifierVector.add(new SingleRequestSpecifier(ElectricityRequestObjective.CurrentLevels, lvNetComp.getId()));
						break;
					}
				}
			}
		}

		// ----------------------------------------------------------
		// --- Check if a voltage level request is required ---------
		if (intDM.getTransformerDataModel().isControlBasedOnNodeVoltage()==true) {
			String controlNodeID = intDM.getTransformerDataModel().getControlNodeID();
			if (controlNodeID!=null && controlNodeID.isEmpty()==false) {
				SingleRequestSpecifier voltageLevelRequest = new SingleRequestSpecifier(ElectricityRequestObjective.VoltageLevels, intDM.getTransformerDataModel().getControlNodeID());
				requestSpecifierVector.add(voltageLevelRequest);
			}
		}
		
		// ----------------------------------------------------------
		// --- Prepare BlackboardRequest ----------------------------
		if (requestSpecifierVector.size()>0) {
			// --- Define concluding BlackboardRequest -------------- 
			BlackboardRequest bbRequest = new BlackboardRequest(this.getEnergyAgent().getAID(), RequestType.SubscriptionRequest, requestSpecifierVector);
			// --- Send to blackboard agent -------------------------
			AID bbAgent = new AID(GlobalHyGridConstants.BLACKBOARD_AGENT_NAME, AID.ISLOCALNAME);
			this.sendAgentNotification(bbAgent, bbRequest);
		}
	}
	
}