package de.enflexit.ea.core.fieldDataProxyAgent;

import java.util.ArrayList;
import java.util.List;

import de.enflexit.ea.core.dataModel.opsOntology.OpsOntology;
import de.enflexit.ea.core.dataModel.opsOntology.ScheduleRangeDefinition;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.core.Agent;

/**
 * This agent is responsible for handling requests for field agent data sent by
 * the FieldDataRequestAgent (see the net.agenthygrid.ops bundle). It is started
 * by the CentralExecutiveAgent when receiving a request for field data, and will 
 * send the requests to the individual agents and pass the results to the requester.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class FieldDataProxyAgent extends Agent {

	private static final long serialVersionUID = -501351185980810347L;
	
	public static final String DEFAULT_AGENT_NAME = "DBProxy";
	
	private ScheduleRangeDefinition requestedRange;
	private List<AID> requestedAgentsAIDs;
	private AID requester;
	
	private List<String> waitingForResponse;

	
	/* (non-Javadoc)
	 * @see jade.core.Agent#setup()
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void setup() {

		// --- Process start arguments ------------------------------
		Object[] arguments = this.getArguments();
		for (int i=0; i<arguments.length; i++) {
			if (arguments[i] instanceof ScheduleRangeDefinition) {
				this.requestedRange = (ScheduleRangeDefinition) arguments[i];
			} else if (arguments[i] instanceof List<?>) {
				this.requestedAgentsAIDs = (List<AID>) arguments[i];
			} else if (arguments[i] instanceof AID) {
				this.requester = (AID) arguments[i];
			}
		}
		
		// --- Check if all required arguments are given ------------
		if (this.requestedRange==null || this.requestedAgentsAIDs==null || this.requester==null) {
			System.err.println(this.getLocalName() + ": Missing required arguments, terminating...");
			this.doDelete();
		}
		
		this.getContentManager().registerLanguage(new SLCodec());
		this.getContentManager().registerOntology(OpsOntology.getInstance());
		
		this.addBehaviour(new HandleRepliesBehaviour(this.requester));
		this.addBehaviour(new SendRequestsBehaviour(requestedRange, requestedAgentsAIDs));
	}

	/**
	 * Gets the requester.
	 * @return the requester
	 */
	public AID getRequester() {
		return requester;
	}

	/**
	 * Gets the waiting for response.
	 * @return the waiting for response
	 */
	public List<String> getWaitingForResponse() {
		if (waitingForResponse==null) {
			waitingForResponse = new ArrayList<String>();
		}
		return waitingForResponse;
	}
	

}
