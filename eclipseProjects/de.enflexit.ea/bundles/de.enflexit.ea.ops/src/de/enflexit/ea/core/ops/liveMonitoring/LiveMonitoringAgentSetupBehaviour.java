package de.enflexit.ea.core.ops.liveMonitoring;

import java.util.ArrayList;

import org.awb.env.networkModel.visualisation.DisplayAgent;

import de.enflexit.ea.core.aggregation.AbstractAggregationHandler;
import de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.ExecutionDataBase;
import energy.optionModel.EvaluationSettings;
import energy.optionModel.TechnicalSystemStateTime;
import jade.core.behaviours.OneShotBehaviour;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

/**
 * The Class LiveMonitoringAgentSetupBehaviour starts all required elements
 * to enable a live monitoring for field agents.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg - Essen
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class LiveMonitoringAgentSetupBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = -5272188906733844199L;
	private static final long UPDATE_PERIOD = 5000; 

	private LiveMonitoringAgent liveMonitoringAgent;
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		
		// --- Start the Aggregation of the current NetworkModel ---- 
		this.prepareAggregationHandler();
		
		// --- Start the display agent ------------------------------
		this.startDisplayAgent();
		
		// --- Start the periodical visualization updates -----------
		this.startVisualizationUpdateBehaviour();
		
	}
	
	/**
	 * Prepare the aggregation handler.
	 */
	private void prepareAggregationHandler() {
		AbstractAggregationHandler aggregationHandler = this.getLiveMonitoringAgent().getAggregationHandler();
		// --- Set the network calculation to sensor data mode ---------------- 
		aggregationHandler.setExecutionDataBase(ExecutionDataBase.SensorData);
		
		// --- Set the start time to now ---------------------------------------
		long startTime = System.currentTimeMillis();
		ArrayList<AbstractSubNetworkConfiguration> subConfigs = aggregationHandler.getSubNetworkConfigurations();
		for (int i=0; i<subConfigs.size(); i++) {
			EvaluationSettings evaluationSettings = subConfigs.get(i).getNetworkCalculationStrategy().getEvaluationSettings();
			TechnicalSystemStateTime firstState = evaluationSettings.getEvaluationStateList().get(0);
			firstState.setGlobalTime(startTime);
		}
	}
	
	/**
	 * Gets the live monitoring agent.
	 * @return the live monitoring agent
	 */
	private LiveMonitoringAgent getLiveMonitoringAgent() {
		if (liveMonitoringAgent==null) {
			liveMonitoringAgent = (LiveMonitoringAgent) this.myAgent;
		}
		return liveMonitoringAgent;
	}
	
	/**
	 * Starts the display agent.
	 */
	private void startDisplayAgent() {
		
		try {
			DisplayAgent da = new DisplayAgent();
			Object[] startArgs = new Object[2];
			startArgs[0] = this.getLiveMonitoringAgent().getProject().getVisualizationTab4SetupExecution();
			startArgs[1] = this.getLiveMonitoringAgent().getEnvironmentModel();
			da.setArguments(startArgs);
			AgentController agentController = myAgent.getContainerController().acceptNewAgent("DisplayAgent", da);
			agentController.start();
			
			this.getLiveMonitoringAgent().setDisplayAgentController(agentController);
			
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Start visualization update behaviour.
	 */
	private void startVisualizationUpdateBehaviour() {
		myAgent.addBehaviour(new LiveMonitoringVisualizationUpdateBehaviour(this.getLiveMonitoringAgent(), UPDATE_PERIOD));
	}
	
	
}
