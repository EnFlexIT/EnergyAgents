package de.enflexit.ea.electricity.transformer;

import de.enflexit.ea.core.dataModel.ontology.ElectricalMeasurement;
import jade.content.Concept;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;
import jade.proto.SubscriptionResponder;

/**
 * {@link SubscriptionResponder} implementation for 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class MeasurementSubscriptionInitiator extends SubscriptionInitiator {

	private static final long serialVersionUID = -5929806575226131254L;
	
	private boolean debug = false;
	

	public MeasurementSubscriptionInitiator(Agent a, ACLMessage msg) {
		super(a, msg);
	}

	/* (non-Javadoc)
	 * @see jade.proto.SubscriptionInitiator#handleInform(jade.lang.acl.ACLMessage)
	 */
	@Override
	protected void handleInform(ACLMessage inform) {

		if (inform != null) {
			try {
				
				// --- Extract message contents -------------------------------
				Action action = (Action) myAgent.getContentManager().extractContent(inform);
				Concept concept = action.getAction();
				
				// --- Store new measurements in the internal data model ------
				if (concept instanceof ElectricalMeasurement) {
					ElectricalMeasurement newMeasurement = (ElectricalMeasurement) concept;
					((TransformerAgent)myAgent).getInternalDataModel().setSensorMeasurement(newMeasurement);
					
					if (this.debug==true) {
						System.out.println(myAgent.getLocalName() + " : Received a new measurement from " + inform.getSender().getName());
					}
				}
				
			} catch (UngroundedException e1) {
				e1.printStackTrace();
			} catch (CodecException e1) {
				e1.printStackTrace();
			} catch (OntologyException e1) {
				e1.printStackTrace();
			}
		}
	}

	
}
