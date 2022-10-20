package de.enflexit.ea.core;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import agentgui.simulationService.environment.EnvironmentModel;
import agentgui.simulationService.time.TimeModelContinuous;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import de.enflexit.ea.core.dataModel.absEnvModel.SimulationStatus.STATE;
import de.enflexit.ea.core.dataModel.absEnvModel.SimulationStatus.STATE_CONFIRMATION;
import de.enflexit.ea.core.dataModel.phonebook.EnergyAgentPhoneBookEntry;
import de.enflexit.ea.core.monitoring.MonitoringListenerForSimulation;
import jade.core.AID;
import jade.lang.acl.MessageTemplate;


/**
 * {@link SimulationConnector} implementation for testbed agents with an IOReal behaviour
 *
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public class SimulationConnectorRemoteForIOReal extends SimulationConnectorRemote  {

	private AbstractInternalDataModel<? extends EnergyAgentPhoneBookEntry> internalDataModel;
	
	/**
	 * Instantiates a new simulation connector remote for IO real.
	 * @param myAgent The agent
	 * @param agentSpecifier The central agent AID
	 */
	public SimulationConnectorRemoteForIOReal(AbstractEnergyAgent myAgent) {
		super(myAgent, null);
		this.internalDataModel = myAgent.getInternalDataModel();
		this.pickEnvironmentModelAndStart();
	}

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.SimulationConnectorRemote#onRegistrationSuccess(jade.core.AID)
	 */
	@Override
	public void onRegistrationSuccess(AID proxyAgentAID) {
		
		this.proxyAgentAID = proxyAgentAID;
		
		// --- Set message templates for both message receive behaviours ------
		this.getRemoteAgentMessageReceiveBehaviour().setMessageTemplate(MessageTemplate.MatchSender(proxyAgentAID));
		
		MessageTemplate messagesFromProxyAgent = MessageTemplate.MatchSender(proxyAgentAID);
		myAgent.getDefaultMessageReceiveBehaviour().addMessageTemplateToIgnoreList(messagesFromProxyAgent);
		
		// --- If the network model is not available, request it from the CEA ---------- 
		if(this.environmentModel == null){
			this.environmentModel = this.requestEnvironmentModel();
		}
		
		// --- Ready to start, send notification to the ProxyAgent
		if (this.environmentModel!=null) {
			this.setEnvironmentModel(environmentModel, false);
			this.sendManagerNotification(STATE_CONFIRMATION.Done);
		} else {
			System.out.println(myAgent.getLocalName() + ": No environment model");
		}
		
	}

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.SimulationConnectorRemote#setEnvironmentModel(agentgui.simulationService.environment.EnvironmentModel, boolean)
	 */
	@Override
	public void setEnvironmentModel(EnvironmentModel envModel, boolean aSynchron) {
		
		NetworkModel networkModel = (NetworkModel) this.environmentModel.getDisplayEnvironment();
		this.internalDataModel.setNetworkModel(networkModel, false);
		NetworkComponent networkComponent = networkModel.getNetworkComponent(this.myAgent.getLocalName()).getCopy();
		this.internalDataModel.setNetworkComponent(networkComponent);

		// --- If switching to state B_ExecuteSimuation, set the start time for the IOReal -------------
		HyGridAbstractEnvironmentModel hygridModel = (HyGridAbstractEnvironmentModel) envModel.getAbstractEnvironment();
		if (hygridModel.getSimulationStatus().getState()==STATE.B_ExecuteSimuation) {

			// --- Set time offset for the IOBehaviour --------------
			TimeModelContinuous timeModel = (TimeModelContinuous) this.environmentModel.getTimeModel();
			long simulationStartTime = timeModel.getTimeStart();
			long timeOffset = System.currentTimeMillis() - simulationStartTime;
			
			myAgent.setTimeOffsetForSimulation(timeOffset);
			
			MonitoringListenerForSimulation monitoringListener = new MonitoringListenerForSimulation(myAgent);
			myAgent.getMonitoringBehaviourRT().addMonitoringListener(monitoringListener);
			
			// --- Check if the control strategy RT has to be started ---------
			switch (myAgent.getInternalDataModel().getTypeOfControlledSystem()) {
			case TechnicalSystem:
				if (myAgent.getInternalDataModel().getOptionModelController().getEvaluationStrategyRT()!=null) {
					// --- Add a real time control, if configured -------------
					myAgent.startControlBehaviourRT();
				}
				break;

			case TechnicalSystemGroup:
				if (myAgent.getInternalDataModel().getGroupController().getGroupOptionModelController().getEvaluationStrategyRT()!=null) {
						// --- Add a real time control, if configured ---------
					myAgent.startControlBehaviourRT();
				}
				break;
				
			case None:
				break;
			}
			
		}
		
	}

}
