package de.enflexit.ea.core.dataModel.blackboard;

import java.util.Vector;

import agentgui.simulationService.SimulationService;
import agentgui.simulationService.behaviour.SimulationServiceBehaviour;
import agentgui.simulationService.transaction.EnvironmentNotification;
import de.enflexit.ea.core.dataModel.blackboard.RequestSpecifier.RequestType;
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
				if (bbRequest.getRequestType()==RequestType.ChangeRequest) {
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
			
			try {
				// --- Wait for the next restart call -------------------------
				synchronized (BlackboardAgent.this.getBlackboard().getNotificationTrigger()) {
					BlackboardAgent.this.getBlackboard().getNotificationTrigger().wait();	
				}
				
			} catch (IllegalMonitorStateException imse) {
				// imse.printStackTrace();
			} catch (InterruptedException ie) {
				// ie.printStackTrace();
			}
			
			if (this.isExit()==false && BlackboardAgent.this.getBlackboard().isDoTerminate()==false) {
				// --- Execute to answer the BlackboardRequestVector ----------
				this.answerBlackboardRequestVector();
				// --- Restart this behaviour --------------------------------- 
				super.reset();
			}
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
			for (BlackboardRequest bbRequest : bbRequestVectorCopy) {
				this.answerBlackboardRequest(bbRequest);
			}
		}
		
		/**
		 * Answers to the specified BlackboardRequest.
		 * @param bbRequest the BlackboardRequest
		 */
		private void answerBlackboardRequest(BlackboardRequest bbRequest) {

			try {
				
				// --- Get the answer from the corresponding domain blackboard ----------
				DomainBlackboard subBlackboard = BlackboardAgent.this.getBlackboard().getDomainBlackboard(bbRequest.getDomainIdentifier());
				AbstractBlackboardAnswer answer = subBlackboard.processBlackboardRequest(bbRequest);
					
				// --- Finally, send notification that can be used as I/O-input ---------
				if (answer!=null) {
					this.sendAgentNotification(bbRequest.getRequester(), answer);
				}
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
	} // end sub class 
	
	
}
