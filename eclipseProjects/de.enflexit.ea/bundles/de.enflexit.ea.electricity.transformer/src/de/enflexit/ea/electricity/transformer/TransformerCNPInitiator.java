package de.enflexit.ea.electricity.transformer;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import de.enflexit.ea.core.dataModel.ontology.FlexibilityOffer;
import java.util.Vector;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;


/**
 * 
 * The Class DistrictCNPInitiator.
 * 
 * This class initiates the Contract Net Protocol.
 * It is based on jade.proto.ContractNetInitiator
 * 
 *  @author Marcel Ludwig
 */
public class TransformerCNPInitiator extends ContractNetInitiator {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5584197052550972523L;
	
	/** The number of actors used in the CNP. */
	
	private Vector<ACLMessage> cfps; 
	
	private boolean debug = true;
	
	
	/**
	 * Instantiates a new district CNP initiator.
	 *
	 * @param a the a
	 * @param vCFPMessage the Vector with all Call for Proposals
	 */
	public TransformerCNPInitiator(Agent a, Vector<ACLMessage> vCFPMessage) {
		super(a, vCFPMessage.get(0));
		this.myAgent = a;
//		this.myViolation = violation;
		this.cfps = vCFPMessage;

	}

	
	
	@Override
	protected Vector<ACLMessage> prepareCfps(ACLMessage cfp) {
		Vector<ACLMessage> cfps = new Vector<ACLMessage>();
		cfps = this.cfps;
		return cfps;
	}
	
	protected void handleAllResponses(Vector responses, Vector acceptances) {
		// --- evaluate acceptance of Distric Agents
		TransformerAgent transformerAgent = (TransformerAgent)this.myAgent;
		
		// --- Get number of involved district Agents 
		int nDistrictAgentsForCNP= transformerAgent.getInternalDataModel().getnDistrictAgentsForCNP();
		int acceptedVoltageAdjustment =0;
		
		//Vector in which all received proposals (TSSEs) are stored
		HashMap<String, ACLMessage > acceptanceMap = new HashMap<>();
		//check if all receivers responded
		
		if (responses.size() == 0) {
			System.err.println("Warning: no responses during cnp received! "+ this.getAgent().getLocalName());
			return;
		}else if (responses.size() <= nDistrictAgentsForCNP) {
			if (debug) System.out.println("Warning: some actors did not respond to cfp: " + (nDistrictAgentsForCNP - responses.size()) + " districtAgent: " + this.getAgent().getLocalName() );
		}
		
		
		// --- Add number of accepted District Agents
		for ( int i=0; i < responses.size(); i++) {
			
			ACLMessage message = (ACLMessage)responses.get(i);
			
			//check if it is a Accept of proposal message
			if (message.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
				acceptedVoltageAdjustment++;
				//if its a proposal, extract the Vector with TSSEs and put it in the map
			}else {
				System.out.println("District Agent " + message.getSender().getLocalName() + " refused voltage adjustment, content: ");
			
			}
		}
		
		// --- Check number of accepted District Agents
		if (acceptedVoltageAdjustment >= nDistrictAgentsForCNP) {
			// all district agents accept voltage adjustment
			
			// --- New Setpoint --> Transformer Control Strategy (End of CNP)
			transformerAgent.getInternalDataModel().setNewSetPointFromCNP(true);
			
		}else {
			System.err.println("Not every District Agents accept voltage adjustment! "+ this.getAgent().getLocalName());
			return;
		}
		if (debug) {
			String debugLine = new String();
			for (ACLMessage reply : (Vector<ACLMessage>)acceptances) {
				debugLine += ((AID)reply.getAllReceiver().next()).getLocalName();
				debugLine += ", ";
			}
			System.out.println("replies send to: " + debugLine);
		}
	}
	
	


//	/* (non-Javadoc)
//	 * @see jade.proto.ContractNetInitiator#handlePropose(jade.lang.acl.ACLMessage, java.util.Vector)
//	 */
//	@SuppressWarnings("unchecked")
//	@Override
//	protected void handlePropose(ACLMessage propose, Vector acceptances) {
//		//Vector in which all received proposals (TSSEs) are stored
//		HashMap<String, ACLMessage > proposalMap = new HashMap<>();
//		
//		if (propose.getPerformative() == ACLMessage.PROPOSE) {
//			//if its a proposal, extract the Vector with TSSEs and put it in the map
//			proposalMap.put(propose.getSender().getLocalName(), propose);
//			if (debug) System.out.println("actor " + propose.getSender().getLocalName() + " send proposal");
//
//		}else {
//			if (debug) System.out.println("actor " + propose.getSender().getLocalName() + " refused cfp, content: " + propose.getContent());
//		}
//		
//		// --- evaluate proposals
//		if (proposalMap.size() > 0) {
//			 this.evaluateProposals(proposalMap, acceptances);
//			// --- check if each proposal has a reply message
//			addRejectAnswers(acceptances, proposalMap);
//		}else {
//			System.err.println("no proposals during cnp received! "+ this.getAgent().getLocalName());
//			return;
//		}
//		if (debug) {
//			String debugLine = new String();
//			for (ACLMessage reply : (Vector<ACLMessage>)acceptances) {
//				debugLine += ((AID)reply.getAllReceiver().next()).getLocalName();
//				debugLine += ", ";
//			}
//			System.out.println("replie send to: " + debugLine);
//		}
//	}


	/**
	 * Adds the reject answers.
	 * 
	 * this method checks, if all received proposals
	 *
	 * @param acceptances the acceptances
	 * @param proposalMap the proposal map
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addRejectAnswers(Vector acceptances, HashMap<String, ACLMessage> proposalMap) {
		if (acceptances.size() < proposalMap.size()) {
			for (ACLMessage proposal : proposalMap.values()) {
				boolean hasAnswer = false;
				for (ACLMessage acceptance : (Vector<ACLMessage>)acceptances) {
					Iterator<AID> receiverIter = acceptance.getAllIntendedReceiver();
					while (receiverIter.hasNext()) {
						AID aid = receiverIter.next();
						if (aid == proposal.getSender()) {
							hasAnswer = true;
						}
					}
				}
				if (!hasAnswer) {
					// --- if this proposal has no answer, crate reject proposal message
					ACLMessage rejectMessage = proposal.createReply();
					rejectMessage.setPerformative(ACLMessage.REJECT_PROPOSAL);
					rejectMessage.setContent("no TSSE was chosen, thus proposal was rejected");
					acceptances.add(rejectMessage);
				}
			}
		}
	}
	

}
