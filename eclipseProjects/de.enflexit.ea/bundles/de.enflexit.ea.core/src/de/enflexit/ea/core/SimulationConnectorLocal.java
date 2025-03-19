package de.enflexit.ea.core;

import java.util.Vector;

import de.enflexit.awb.simulation.SimulationService;
import de.enflexit.awb.simulation.SimulationServiceHelper;
import de.enflexit.awb.simulation.environment.EnvironmentModel;
import de.enflexit.awb.simulation.sensoring.ServiceSensor;
import de.enflexit.awb.simulation.sensoring.ServiceSensorInterface;
import de.enflexit.awb.simulation.transaction.DisplayAgentNotification;
import de.enflexit.awb.simulation.transaction.EnvironmentNotification;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import de.enflexit.ea.core.dataModel.absEnvModel.SimulationStatus.STATE_CONFIRMATION;
import de.enflexit.ea.core.dataModel.deployment.SetupExtension;
import de.enflexit.ea.core.testbed.proxy.ProxyAgent;
import energy.optionModel.TechnicalSystemStateEvaluation;
import jade.core.AID;
import jade.core.Location;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;

/**
 * The Class SimulationConnectorLocal is used for the pure simulation environment.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public class SimulationConnectorLocal implements SimulationConnector, ServiceSensorInterface {
	
	private static final String PROXY_AGENT_CLASS_NAME = ProxyAgent.class.getName();

	private AbstractEnergyAgent myAgent;
	private AbstractIOSimulated ioSimulated;
	private TechnicalSystemStateEvaluation tsseLastTransferred;
	
	private ServiceSensor mySensor;
	
	private CyclicNotificationHandler notificationHandler;
	private Vector<EnvironmentNotification> notifications = new Vector<EnvironmentNotification>();

	
	/**
	 * Instantiates a new simulation connector.
	 *
	 * @param agent the agent
	 * @param ioSimulated the current IO-simulated
	 */
	public SimulationConnectorLocal(AbstractEnergyAgent agent, AbstractIOSimulated ioSimulated) {
		this.myAgent = agent;
		this.ioSimulated = ioSimulated;
		// --- Add sensor and notification handler ------------------
		this.sensorPlugIn();
		this.addNotificationHandler();
	}
	
	/**
	 * This Method plugs IN the service sensor.
	 */
	protected void sensorPlugIn() {
		// --- Start the ServiceSensor ------------------------------
		this.mySensor = new ServiceSensor(this);
		// --- Register the sensor to the SimulationService ---------
		try {
			SimulationServiceHelper simHelper = (SimulationServiceHelper) this.myAgent.getHelper(SimulationService.NAME);
			simHelper.sensorPlugIn(mySensor);	
				
		} catch (ServiceException ae) {
			System.err.println("Agent '" + this.myAgent.getLocalName() + "': Could not plugin simulated sensor!");
//			se.printStackTrace();
		}
	}
	
	/**
	 * This Method plugs OUT the service sensor.
	 */
	protected void sensorPlugOut() {
		// --- plug-out the Sensor ----------------------------------
		try {
			SimulationServiceHelper simHelper = (SimulationServiceHelper) this.myAgent.getHelper(SimulationService.NAME);
			simHelper.sensorPlugOut(mySensor);	
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		mySensor = null;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.SimulationConnectorInterface#waitForSimulatedEnvironmentAndStart()
	 */
	@Override
	public void pickEnvironmentModelAndStart() {
		
		Runnable startUp = new Runnable() {
			@Override
			public void run() {

				try {
					// --- Wait for a simulation manager to be found ----------
					SimulationServiceHelper simHelper = (SimulationServiceHelper) myAgent.getHelper(SimulationService.NAME);
					while (ioSimulated.done()==false && simHelper.getManagerAgent()==null) {
						Thread.sleep(100);
					}
					
					// --- Wait for the environment model ---------------------
					while (ioSimulated.done()==false && simHelper.getEnvironmentModel()==null) {
						Thread.sleep(100);
					}
					if (ioSimulated.done()==true) return;
					
					// --- Inform manager about initialization ----------------
					SimulationConnectorLocal.this.sendManagerNotification(STATE_CONFIRMATION.Initialized);
					
					// --- Get the actual environment model -------------------
					EnvironmentModel envModel = simHelper.getEnvironmentModel();
					
					// --- Check if this simulated agent has to die -----------
					HyGridAbstractEnvironmentModel hygridModel = (HyGridAbstractEnvironmentModel) envModel.getAbstractEnvironment();
					if (hygridModel!=null && hygridModel.getSetupExtension()!=null) {

						SetupExtension setupExtension = hygridModel.getSetupExtension();
						// --- Check if this agent is deployed ----------------
						boolean isAgentDepolyed = setupExtension.getDeploymentGroupsHelper().isAgentDeployed(myAgent.getAID());
						// --- Check if the deployment is activated ----------- 
						boolean isDeploymentActivated = setupExtension.getDeploymentGroupsHelper().isDeploymentActivated(myAgent.getAID());
						if (isAgentDepolyed==true && isDeploymentActivated==true) {
							// --- Terminate local agent and start proxy ------ 
							Thread proxyStarterThread = new AgentStarterThread(myAgent.getAID().getLocalName(), PROXY_AGENT_CLASS_NAME, myAgent.getContainerController());
							proxyStarterThread.start();
							
							sensorPlugOut();
							myAgent.doDelete();
							return;
						}
					}
					
					// --- Start the IO simulated -----------------------------
					ioSimulated.initialize(envModel);
					
				} catch (ServiceException se) {
					se.printStackTrace();
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
				
			}
		};
		// --- Execute start up in an own thread ----------
		Thread startUpThread = new Thread(startUp, this.myAgent.getLocalName() + "_Start");
		startUpThread.start();
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.SimulationConnector#getLastTechnicalSystemStateEvaluationTransferred()
	 */
	@Override
	public TechnicalSystemStateEvaluation getLastTechnicalSystemStateEvaluationTransferred() {
		return this.tsseLastTransferred;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.SimulationConnectorInterface#sendManagerNotification(java.lang.Object)
	 */
	@Override
	public boolean sendManagerNotification(Object notification) {
		
		if (notification instanceof TechnicalSystemStateEvaluation) this.tsseLastTransferred = (TechnicalSystemStateEvaluation) notification;
		
		boolean send = false;
		EnvironmentNotification myNotification = new EnvironmentNotification(this.myAgent.getAID(), false, notification);
		try {
			SimulationServiceHelper simHelper = (SimulationServiceHelper) this.myAgent.getHelper(SimulationService.NAME);
			send = simHelper.notifyManagerAgent(myNotification);	
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		return send;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.SimulationConnectorInterface#sendAgentNotification(jade.core.AID, java.lang.Object)
	 */
	public boolean sendAgentNotification(AID receiverAID, Object notification) {
		boolean send = false;
		EnvironmentNotification myNotification = new EnvironmentNotification(this.myAgent.getAID(), false, notification);
		try {
			SimulationServiceHelper simHelper = (SimulationServiceHelper) this.myAgent.getHelper(SimulationService.NAME);
			send = simHelper.notifySensorAgent(receiverAID, myNotification);
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		return send;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.SimulationConnectorInterface#sendDisplayAgentNotification(agentgui.simulationService.transaction.DisplayAgentNotification)
	 */
	public void sendDisplayAgentNotification(DisplayAgentNotification displayAgentNotification) {
		try {
			EnvironmentNotification notification = new EnvironmentNotification(getAID(), true, displayAgentNotification);
			SimulationServiceHelper simHelper = (SimulationServiceHelper) this.myAgent.getHelper(SimulationService.NAME);
			simHelper.displayAgentNotification(notification);
		} catch (ServiceException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.SimulationConnectorInterface#setMyStimulusAnswer(java.lang.Object)
	 */
	public void setMyStimulusAnswer(Object myNextState) {
		try {
			SimulationServiceHelper simHelper = (SimulationServiceHelper) this.myAgent.getHelper(SimulationService.NAME);
			simHelper.setEnvironmentInstanceNextPart(this.myAgent.getAID(), myNextState);
		} catch (ServiceException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see agentgui.simulationService.sensoring.ServiceSensorInterface#setMigration(jade.core.Location)
	 */
	@Override
	public void setMigration(Location newLocation) {
		// --- Prepare agent to migrate ---------
	}
	/* (non-Javadoc)
	 * @see agentgui.simulationService.sensoring.ServiceSensorInterface#setPauseSimulation(boolean)
	 */
	@Override
	public void setPauseSimulation(boolean isPauseSimulation) {
		this.ioSimulated.setPauseSimulation(isPauseSimulation);
	}

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.SimulationConnectorInterface#setEnvironmentModel(agentgui.simulationService.environment.EnvironmentModel, boolean)
	 */
	@Override
	public void setEnvironmentModel(EnvironmentModel envModel, boolean aSynchron) {
		this.ioSimulated.setEnvironmentModel(envModel);
		if (aSynchron==true) {
			this.myAgent.addBehaviour(new ServiceStimulus());	
		} else {
			this.onEnvironmentStimulusIntern();	
		}		
	}
	
		
	/**
	 * This Behaviour is used to stimulate the agent from the outside 
	 * in a asynchronous way.
	 * 	 
	 * @author Christian Derksen - DAWIS - ICB - University of Duisburg - Essen
	 */
	private class ServiceStimulus extends OneShotBehaviour {
		private static final long serialVersionUID = 1441989543791055996L;
		@Override
		public void action() {
			onEnvironmentStimulusIntern();
		}
	}
	/**
	 * This method is internally called if a stimulus from the outside reached this agent.
	 */
	private void onEnvironmentStimulusIntern() {
		this.ioSimulated.onEnvironmentStimulus();
	}
	
	/* (non-Javadoc)
	 * @see agentgui.simulationService.sensoring.ServiceSensorInterface#setNotification(agentgui.simulationService.transaction.EnvironmentNotification)
	 */
	public void setNotification(EnvironmentNotification notification) {
		// --- place the notification into the notification vector -------
		synchronized (this.notifications) {
			this.notifications.add(notification);	
		}
		// --- restart the CyclicNotificationHandler ---------------------
		if (this.notificationHandler==null) {
			this.addNotificationHandler();
		} else {
			this.notificationHandler.restart();	
		}
			
	}
	/**
	 * This method adds the CyclicNotificationHandler to this agent.
	 */
	private void addNotificationHandler() {
		if (this.notificationHandler==null) {
			this.notificationHandler = new CyclicNotificationHandler();	
		}		
		this.myAgent.addBehaviour(this.notificationHandler);
	}
	/**
	 * This method removes the CyclicNotificationHandler from this agent.
	 */
	private void removeNotificationHandler() {
		if (this.notificationHandler!=null) {
			this.myAgent.removeBehaviour(this.notificationHandler);	
		}
	}

	/**
	 * This CyclicBehaviour is used in order to act on the incoming notifications.
	 * @author Christian Derksen - DAWIS - ICB - University of Duisburg - Essen
	 */
	private class CyclicNotificationHandler extends CyclicBehaviour {
		
		private static final long serialVersionUID = 4638681927192305608L;
		
		/* (non-Javadoc)
		 * @see jade.core.behaviours.Behaviour#action()
		 */
		@Override
		public void action() {
			
			EnvironmentNotification notification = null;
			boolean removeFirstElement = false;
			boolean moveAsLastElement = false;
			
			// --- Get the first element and work on it ------------------
			if (notifications.size()!=0) {
				notification = notifications.get(0);
				notification = ioSimulated.filterEnvironmentNotification(notification);
				if (notification.getProcessingInstruction().isDelete()) {
					removeFirstElement = true;	
					moveAsLastElement = false;
					
				} else if (notification.getProcessingInstruction().isBlock()) {
					removeFirstElement = false;
					moveAsLastElement = false;
					this.block(notification.getProcessingInstruction().getBlockPeriod());
					
				} 
				if (notification.getProcessingInstruction().isMoveLast()) {
					removeFirstElement = false;
					moveAsLastElement = true;
				}
			}
			
			// --- remove this element and control the notifications -----
			synchronized (notifications) {
				if (removeFirstElement==true) {
					notifications.remove(0);
				}
				if (moveAsLastElement==true) {
					if (notifications.size()>1) {
						notifications.remove(0);
						notifications.add(notification);
					} else {
						this.block(notification.getProcessingInstruction().getBlockPeriod());	
					}
				}
				if (notification!=null) {
					notification.resetProcessingInstruction();	
				}
				if (notifications.size()==0) {
					block();
				}
			}
			
		}
	}

	/* (non-Javadoc)
	 * @see agentgui.simulationService.sensoring.ServiceSensorInterface#doDelete()
	 */
	@Override
	public void doDelete() {
		this.ioSimulated.setDone(true);
		this.removeNotificationHandler();
		this.sensorPlugOut();
	}

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.SimulationConnectorInterface#onEnd()
	 */
	@Override
	public void onEnd() {
		this.removeNotificationHandler();
		this.sensorPlugOut();
	}

	/* (non-Javadoc)
	 * @see agentgui.simulationService.sensoring.ServiceSensorInterface#getAID()
	 */
	@Override
	public AID getAID() {
		return this.myAgent.getAID();
	}
	
}
