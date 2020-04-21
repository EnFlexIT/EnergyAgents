package de.enflexit.ea.core.ops.liveMonitoring;

import org.awb.env.networkModel.NetworkModel;

import agentgui.core.application.Application;
import agentgui.core.environment.EnvironmentController;
import agentgui.core.project.Project;
import agentgui.simulationService.SimulationService;
import agentgui.simulationService.SimulationServiceHelper;
import agentgui.simulationService.environment.EnvironmentModel;
import agentgui.simulationService.time.TimeModelDateBased;
import agentgui.simulationService.time.TimeModelPresent;
import agentgui.simulationService.transaction.DisplayAgentNotification;
import agentgui.simulationService.transaction.EnvironmentNotification;
import de.enflexit.ea.core.aggregation.AbstractAggregationHandler;
import de.enflexit.ea.core.aggregation.AggregationListener;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import de.enflexit.ea.core.dataModel.opsOntology.OpsOntology;
import de.enflexit.ea.core.ops.OpsController;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.domain.FIPANames;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;


// TODO: Auto-generated Javadoc
/**
 * The base Class of the LiveMonitoringAgent.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg - Essen
 */
public class LiveMonitoringAgent extends Agent implements AggregationListener {

	private static final long serialVersionUID = 4174941541489553938L;
	
	public static final String DEFAULT_LOCAL_NAME = "LiMonAg";

	private EnvironmentModel environmentModel;
	private NetworkModel networkModel;
	private HyGridAbstractEnvironmentModel hygridAbstractEnvironmentModel;
	
	private OpsController opsController;
	
	private AbstractAggregationHandler aggregationHandler;
	private AgentController displayAgentController;
	
	private LiveMonitoringSubscriptionInitiator liveMonitoringSubscriptionInitiator;
	
	private SimulationServiceHelper simHelper;
	
	private boolean debug = true;
	
	/* (non-Javadoc)
	 * @see jade.core.Agent#setup()
	 */
	@Override
	protected void setup() {
		
		// --- Get the OpsController ----------------------
		Object[] args = this.getArguments();
		if (args!=null && args[0]!=null && args[0] instanceof OpsController) {
			this.opsController = (OpsController) args[0]; 
		}
		
		// --- Register ontology and codec ----------------
		this.getContentManager().registerLanguage(new SLCodec());
		this.getContentManager().registerOntology(OpsOntology.getInstance());
		
		// --- Start message receive behaviour ------------
		MessageReceiveBehaviour messageReceiveBehaviour = new MessageReceiveBehaviour(this);
		// --- Ignore protocol-related messages -----------
		messageReceiveBehaviour.addMessageTemplateToIgnoreList(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST));
		messageReceiveBehaviour.addMessageTemplateToIgnoreList(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE));
		this.addBehaviour(messageReceiveBehaviour);
		
		// --- Send a request to the CEA to start the LiveMonitoringProxyAgent -----
		this.addBehaviour(new LiveMonitoringRequestInitiator(this));
		if (this.debug==true) {
			System.out.println(this.getLocalName() + ": Live data request sent to " + this.getCeaAid());
		}
	}
	/* (non-Javadoc)
	 * @see jade.core.Agent#takeDown()
	 */
	@Override
	protected void takeDown() {
		this.getAggregationHandler().terminate();
		this.stopDisplayAgent();
		this.cancelSubscription();
	}

	/**
	 * Returns the aggregation display behaviour.
	 * @return the display behaviour
	 */
	protected AbstractAggregationHandler getAggregationHandler() {
		if (aggregationHandler==null) {
			aggregationHandler = new AggregationHandler(this.getNetworkModel(), false, this.getClass().getSimpleName());
			aggregationHandler.addAggregationListener(this);
		}
		return aggregationHandler;
	}
	
	/* (non-Javadoc)
	 * @see hygrid.aggregation.AggregationListener#networkCalculationDone()
	 */
	@Override
	public void networkCalculationDone() {
		// --- Nothing to do here (I think...) ------------
	}
	/* (non-Javadoc)
	 * @see hygrid.aggregation.AggregationListener#sendDisplayAgentNotification(agentgui.simulationService.transaction.DisplayAgentNotification)
	 */
	@Override
	public void sendDisplayAgentNotification(DisplayAgentNotification displayNotification) {
		try {
			EnvironmentNotification environmentNotification = new EnvironmentNotification(this.getAID(), true, displayNotification);
			this.getSimHelper().displayAgentNotification(environmentNotification);
		} catch (ServiceException e) {
			System.err.println(this.getLocalName() + ": Error accessing the simulation service!");
		}
	}
	
	/**
	 * Gets the {@link SimulationServiceHelper}
	 * @return the SimulationServiceHelper
	 * @throws ServiceException 
	 */
	private SimulationServiceHelper getSimHelper() throws ServiceException {
		if (simHelper==null) {
			this.simHelper = (SimulationServiceHelper) getHelper(SimulationService.NAME);
			this.simHelper.setManagerAgent(this.getAID());
		}
		return simHelper;
	}
	/**
	 * Returns the {@link NetworkModel} from the projects .
	 * @return the network model
	 */
	public NetworkModel getNetworkModel() {
		if (networkModel==null) {
			EnvironmentModel envModel = this.getEnvironmentModel();
			if (envModel!=null) {
				networkModel = (NetworkModel) envModel.getDisplayEnvironment(); 
			}
		}
		return networkModel;
	}
	
	/**
	 * Return the current {@link HyGridAbstractEnvironmentModel} form the projects setup.
	 * @return the hygrid abstract environment model
	 */
	public HyGridAbstractEnvironmentModel getHygridAbstractEnvironmentModel() {
		if (hygridAbstractEnvironmentModel==null) {
			EnvironmentModel envModel = this.getEnvironmentModel();
			if (envModel!=null) {
				hygridAbstractEnvironmentModel = (HyGridAbstractEnvironmentModel) envModel.getAbstractEnvironment(); 
			}
		}
		return hygridAbstractEnvironmentModel;
	}
	
	/**
	 * Returns the environment model from the projects {@link EnvironmentController}.
	 * @return the environment model
	 */
	public EnvironmentModel getEnvironmentModel() {
		if (environmentModel==null) {
			// --- Get a copy of the setups environment --- 
			Project project = this.getProject();
			if (project!=null) {
				EnvironmentController envController = project.getEnvironmentController();
				if (envController!=null) {
					environmentModel = envController.getEnvironmentModel().getCopy();
					TimeModelDateBased oldTimeModel = (TimeModelDateBased) environmentModel.getTimeModel();
					
					// --- Set the time model for real time visualization -----
					TimeModelPresent timeModelPresent = new TimeModelPresent();
					timeModelPresent.setTimeFormat(oldTimeModel.getTimeFormat());
					environmentModel.setTimeModel(timeModelPresent);
					
				}
			}
		}
		return environmentModel;
	}
	
	/**
	 * Gets the project.
	 * @return the project
	 */
	public Project getProject() {
		return Application.getProjectFocused();
	}
	
	/**
	 * Gets the AID of the CEA.
	 * @return the cea aid
	 */
	public AID getCeaAid() {
		return this.opsController.getCeaAID();
	}
	
	/**
	 * Start the subscription.
	 */
	protected void startSubscription(AID proxyAgentAID) {
		this.liveMonitoringSubscriptionInitiator = new LiveMonitoringSubscriptionInitiator(this, proxyAgentAID);
		this.addBehaviour(liveMonitoringSubscriptionInitiator);
	}
	
	/**
	 * Cancel the subscription.
	 */
	protected void cancelSubscription() {
		if (this.liveMonitoringSubscriptionInitiator!=null) {
			this.liveMonitoringSubscriptionInitiator.cancel();
		}
	}
	
	/**
	 * Gets the display agent controller.
	 * @return the display agent controller
	 */
	protected AgentController getDisplayAgentController() {
		return displayAgentController;
	}
	
	/**
	 * Sets the display agent controller.
	 * @param displayAgentController the new display agent controller
	 */
	protected void setDisplayAgentController(AgentController displayAgentController) {
		this.displayAgentController = displayAgentController;
	}
	
	/**
	 * Stops the display agent.
	 */
	private void stopDisplayAgent() {
		if (this.getDisplayAgentController()!=null) {
			try {
				this.getDisplayAgentController().kill();
			} catch (StaleProxyException e) {
				System.err.println("[" + this.getClass().getSimpleName() + "] Error stopping the display agent!");
			}
		}
	}

}
