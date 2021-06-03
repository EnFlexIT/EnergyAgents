package de.enflexit.ea.core.blackboard;

import java.util.Vector;

import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import agentgui.simulationService.SimulationService;
import agentgui.simulationService.behaviour.SimulationServiceBehaviour;
import agentgui.simulationService.transaction.EnvironmentNotification;
import de.enflexit.ea.core.aggregation.AbstractAggregationHandler;
import de.enflexit.ea.core.aggregation.AbstractSubBlackboardModel;
import de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration;
import de.enflexit.ea.core.blackboard.Blackboard.BlackboardWorkingThread;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import de.enflexit.ea.core.dataModel.absEnvModel.SimulationStatus.STATE;
import de.enflexit.ea.core.dataModel.blackboard.AbstractBlackboardAnswer;
import de.enflexit.ea.core.dataModel.blackboard.BlackboardRequest;
import de.enflexit.ea.core.dataModel.blackboard.EmptyBlackboardAnswer;
import de.enflexit.ea.core.dataModel.blackboard.MultipleBlackboardAnswer;
import de.enflexit.ea.core.dataModel.blackboard.SingleRequestSpecifier;
import de.enflexit.ea.core.dataModel.blackboard.SingleRequestSpecifier.RequestType;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.ThreadedBehaviourFactory;

/**
 * This BlackboardAgent manages the requests for the current situation of the  
 * simulation that are handled within the {@link Blackboard}.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class BlackboardAgent extends Agent {

	private static final long serialVersionUID = -1677309747788934181L;

	private Blackboard blackboard;
	
	private ThreadedBehaviourFactory tbf;
	private SimServiceBehaviour simServiceBehaviour;

	
	/* (non-Javadoc)
	 * @see jade.core.Agent#setup()
	 */
	@Override
	protected void setup() {
		
		Object[] startArguments = this.getArguments();
		if (startArguments!=null) {
			this.blackboard = (Blackboard) startArguments[0];;
		}
		
		this.tbf = new ThreadedBehaviourFactory();
		this.simServiceBehaviour = new SimServiceBehaviour(this);
		this.addBehaviour(tbf.wrap(this.simServiceBehaviour));
	}
	
	/* (non-Javadoc)
	 * @see jade.core.Agent#takeDown()
	 */
	@Override
	protected void takeDown() {
		this.simServiceBehaviour.setExit(true);
		this.tbf.getThread(this.simServiceBehaviour).interrupt();
		this.removeBehaviour(this.simServiceBehaviour);
		this.simServiceBehaviour=null;
	}
	
	/**
	 * Return the instance of the blackboard.
	 * @return the blackboard
	 */
	public Blackboard getBlackboard() {
		if (blackboard==null) {
			blackboard = Blackboard.getInstance();
		}
		return blackboard;
	}
	
	/**
	 * The Class SimServiceBehaviour connects this agent to the {@link SimulationService}.
	 */
	private class SimServiceBehaviour extends SimulationServiceBehaviour {

		private static final long serialVersionUID = 3060575302036731885L;

		private Vector<BlackboardRequest> bbRequestVector;
		private boolean exit;
		
		/**
		 * Instantiates a new simulation service behaviour.
		 * @param agent the agent
		 */
		public SimServiceBehaviour(Agent agent) {
			super(agent, true);
		}
		/* (non-Javadoc)
		 * @see agentgui.simulationService.sensoring.ServiceSensorInterface#setPauseSimulation(boolean)
		 */
		@Override
		public void setPauseSimulation(boolean isPauseSimulation) { }
		/* (non-Javadoc)
		 * @see agentgui.simulationService.behaviour.SimulationServiceBehaviour#setMigration(jade.core.Location)
		 */
		@Override
		public void setMigration(Location newLocation) { }
		/* (non-Javadoc)
		 * @see agentgui.simulationService.behaviour.SimulationServiceBehaviour#onEnvironmentStimulus()
		 */
		@Override
		public void onEnvironmentStimulus() { }

		/* (non-Javadoc)
		 * @see agentgui.simulationService.behaviour.SimulationServiceBehaviour#onEnvironmentNotification(agentgui.simulationService.transaction.EnvironmentNotification)
		 */
		@Override
		protected EnvironmentNotification onEnvironmentNotification(EnvironmentNotification notification) {
			
			if (notification.getNotification()!=null && notification.getNotification() instanceof BlackboardRequest) {
				
				BlackboardRequest bbRequest = (BlackboardRequest) notification.getNotification(); 
				if (bbRequest.getRequestType()==RequestType.SubscriptionRequest) {
					// --- Add the request to the vector of requests ----------
					this.getBlackboardRequestVector().add(bbRequest);
				}
				this.answerBlackboardRequest(bbRequest);
			}
			return super.onEnvironmentNotification(notification);
		}
		
		/* (non-Javadoc)
		 * @see agentgui.simulationService.behaviour.SimulationServiceBehaviour#action()
		 */
		@Override
		public void action() {
			
			Blackboard bBoard = BlackboardAgent.this.getBlackboard();
			try {
				// --- Wait for the next restart call -------------------------
				synchronized (bBoard.getWakeUpTrigger()) {
					bBoard.getWakeUpTrigger().wait();	
				}
				
			} catch (IllegalMonitorStateException imse) {
				// imse.printStackTrace();
			} catch (InterruptedException ie) {
				// ie.printStackTrace();
			}
			
			if (this.isExit()==false && bBoard.isDoTerminate()==false && bBoard.isAgentNotificationsEnabled()==true) {
				// --- Execute to answer the BlackboardRequestVector ----------
				this.answerBlackboardRequestVector();
				// --- Restart this behaviour --------------------------------- 
				super.reset();
			}

			// --- Set this working thread to be finalized -------------------- 
			bBoard.setBlackboardWorkingThreadFinalized(BlackboardWorkingThread.BlackboardAgent);
		}
		
		/**
		 * Sets the behaviour to exit.
		 * @param exit the new exit
		 */
		public void setExit(boolean exit) {
			this.exit = exit;
		}
		/**
		 * Checks if this behaviour is to exit.
		 * @return true, if is exit
		 */
		public boolean isExit() {
			return exit;
		}
		
		/**
		 * Gets the blackboard request vector.
		 * @return the blackboard request vector
		 */
		private Vector<BlackboardRequest> getBlackboardRequestVector() {
			if (bbRequestVector==null) {
				bbRequestVector = new Vector<BlackboardRequest>();
			}
			return bbRequestVector;
		}
		/**
		 * Answers to all BlackboardRequestthat are stored in the {@link #bbRequestVector}.
		 */
		private void answerBlackboardRequestVector() {
			// --- Make a copy of the current vector in order -----------
			// --- to avoid concurrent exceptions ----------------------- 
			Vector<BlackboardRequest> bbRequestVectorCopy = new Vector<BlackboardRequest>(this.getBlackboardRequestVector());
			for (int i = 0; i < bbRequestVectorCopy.size(); i++) {
				this.answerBlackboardRequest(bbRequestVectorCopy.get(i));
			}
		}
		
		/**
		 * Answers to the specified BlackboardRequest.
		 * @param bbRequest the BlackboardRequest
		 */
		private void answerBlackboardRequest(BlackboardRequest bbRequest) {

			try {
				// ----------------------------------------------------------------------
				// --- Empty or Null request? -------------------------------------------
				// ----------------------------------------------------------------------
				if (bbRequest==null) return;

				// ----------------------------------------------------------------------
				// --- Check if we're at the begin or the end of a simulation ----------- 
				// ----------------------------------------------------------------------
				AbstractAggregationHandler agHandler = BlackboardAgent.this.getBlackboard().getAggregationHandler();
				HyGridAbstractEnvironmentModel hyGridAbsModel = agHandler.getHyGridAbstractEnvironmentModel();
				if (hyGridAbsModel!=null) {
					STATE simState = hyGridAbsModel.getSimulationStatus().getState();
					if (simState!=null) {
						if (simState!=STATE.B_ExecuteSimuation) return;
					}
				}
				
				// ----------------------------------------------------------------------
				// --- Define result vector and answer the requests ---------------------
				// ----------------------------------------------------------------------
				Vector<AbstractBlackboardAnswer> answerVector = new Vector<AbstractBlackboardAnswer>();
				Vector<SingleRequestSpecifier> requestVector = bbRequest.getRequestSpecifierVector();
				for (int i=0; i<requestVector.size(); i++) {
					
					AbstractBlackboardAnswer requestAnswer = null;
					SingleRequestSpecifier singleRequest = requestVector.get(i);

					// --- Answer general requests here ---------------------------------
					if (singleRequest.getRequestObjective() instanceof GeneralRequestObjective) {
						requestAnswer = this.getGeneralBlackboardAnswer(singleRequest);
						answerVector.add(requestAnswer);
						
					} else {
						// --- Delegate specific requests to the sub model --------------
						for (int j = 0; j < agHandler.getSubNetworkConfigurations().size(); j++) {
							AbstractSubNetworkConfiguration subConfig = agHandler.getSubNetworkConfigurations().get(j);
							AbstractSubBlackboardModel subBlackboardModel = subConfig.getSubBlackboardModel();
							if (subBlackboardModel!=null) {
								AbstractBlackboardAnswer subAnswer = subBlackboardModel.getBlackboardRequestAnswer(singleRequest);
								if (subAnswer!=null) {
									subAnswer.setSubConfigurationID(subConfig.getID());
									subAnswer.setSubConfigurationDomain(subConfig.getDomain());
									answerVector.add(subAnswer);
								}
							}
						}
					}
				}
				
				// ----------------------------------------------------------------------
				// --- Prepare answer type ----------------------------------------------
				// ----------------------------------------------------------------------
				AbstractBlackboardAnswer finalAnswer = null;
				if (answerVector.size()==0) {
					finalAnswer = new EmptyBlackboardAnswer();
				} else if (answerVector.size()==1) {
					finalAnswer = answerVector.get(0);
				} else {
					finalAnswer = new MultipleBlackboardAnswer(answerVector); 
				}
				
				// ----------------------------------------------------------------------
				// --- Finally, send notification that can be used as I/O-input ---------
				// ----------------------------------------------------------------------
				if (finalAnswer!=null) {
					this.sendAgentNotification(bbRequest.getRequester(), finalAnswer);
				}
					
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		/**
		 * Returns the blackboard answer for the specified request.
		 * @param singleRequest the single request 
		 * @return the blackboard answer
		 */
		private AbstractBlackboardAnswer getGeneralBlackboardAnswer(SingleRequestSpecifier singleRequest) {
		
			AbstractBlackboardAnswer answer = null;

			// --- Check if it is a eneral network-related request ------------
			if (singleRequest.getRequestObjective() instanceof GeneralRequestObjective) {
				GeneralRequestObjective requestObjective = (GeneralRequestObjective) singleRequest.getRequestObjective();
				NetworkModel networkModel = BlackboardAgent.this.getBlackboard().getNetworkModel();
				
				switch (requestObjective) {
				case NetworkModel:
					answer = new NetworkModelAnswer(networkModel);
					break;
				case GraphNodeDataModel:
					GraphNode node = (GraphNode) networkModel.getGraphElement(singleRequest.getIdentifier());
					answer = new GraphNodeAnswer(singleRequest.getIdentifier(), node.getDataModel());
					break;
				case NetworkComponentDataModel:
					NetworkComponent netComp = networkModel.getNetworkComponent(singleRequest.getIdentifier());
					answer = new GraphNodeAnswer(singleRequest.getIdentifier(), netComp.getDataModel());
					break;
				}
			}
			return answer;
		}
		
	} // end sub class 
	
	
}
