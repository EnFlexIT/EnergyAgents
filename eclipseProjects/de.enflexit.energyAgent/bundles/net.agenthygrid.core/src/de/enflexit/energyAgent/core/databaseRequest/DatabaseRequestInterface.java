package de.enflexit.energyAgent.core.databaseRequest;

import java.util.HashMap;

import de.enflexit.energyAgent.core.AbstractEnergyAgent;
import hygrid.ops.ontology.FieldDataReply;
import hygrid.ops.ontology.FieldDataRequest;
import jade.core.Agent;

/**
 * Singleton class to handle database requests from the agents. Not much functionality here,
 * the main purpose of this class is to limit&control access to the {@link DatabaseRequestThread}. 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class DatabaseRequestInterface {
	
	private static DatabaseRequestInterface thisInstance;
	private DatabaseRequestThread requestThread;
	
	private HashMap<String, Agent> waitingAgents;
	
	/**
	 * Singleton -> private constructor. Use getInstance() instead.
	 */
	private DatabaseRequestInterface() {}
	
	/**
	 * Gets the single instance of DatabaseRequestInterface.
	 * @return single instance of DatabaseRequestInterface
	 */
	public synchronized static DatabaseRequestInterface getInstance() {
		if (thisInstance==null){
			thisInstance = new DatabaseRequestInterface();
		}
		return thisInstance;
	}
	
	/**
	 * Gets the request thread.
	 * @return the request thread
	 */
	private DatabaseRequestThread getRequestThread() {
		if (requestThread==null) {
			requestThread  = new DatabaseRequestThread(this);
			requestThread.start();
		}
		return requestThread;
	}

	/**
	 * Request field data.
	 * @param agent the requesting agent
	 * @param request the request
	 */
	public void requestFieldData(AbstractEnergyAgent agent, FieldDataRequest request) {
		this.getWaitingAgents().put(agent.getLocalName(), agent);
		this.getRequestThread().addRequestToQueue(request);
	}
	
	/**
	 * Gets the waiting agents.
	 * @return the waiting agents
	 */
	private HashMap<String, Agent> getWaitingAgents() {
		if (waitingAgents==null) {
			waitingAgents = new HashMap<>();
		}
		return waitingAgents;
	}

	/**
	 * Send reply to agent.
	 * @param reply the reply
	 */
	protected void sendReplyToAgent(FieldDataReply reply) {
		Agent waitingAgent = this.getWaitingAgents().get(reply.getAgentID());
		if (waitingAgent!=null) {
			try {
				waitingAgent.putO2AObject(reply, false);
				if (reply.getMoreComming()==false) {
					thisInstance.getWaitingAgents().remove(reply.getAgentID());
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("[" + this.getClass().getSimpleName() + "] Agent " + reply.getAgentID() + " not in the waiting list!");
		}
	}

}
