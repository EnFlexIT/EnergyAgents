package de.enflexit.ea.electricity.sensor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import de.enflexit.ea.core.EnergyAgentIO;
import de.enflexit.common.Observable;
import de.enflexit.common.Observer;
import de.enflexit.ea.core.AbstractInternalDataModel.CHANGED;
import de.enflexit.ea.core.dataModel.ontology.HyGridOntology;
import jade.content.Concept;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * The Class SensorSendingBehaviour.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public abstract class SensorSendingBehaviour extends CyclicBehaviour implements Observer {

	private boolean debug = false; 
	protected SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	
	private static final long serialVersionUID = 4378325659431942685L;

	protected InternalDataModel internalDataModel;
	protected EnergyAgentIO energyAgentIO;
	
	protected AID aidReceiver;
	protected Ontology ontology = HyGridOntology.getInstance();
	protected Codec codec = new SLCodec();
	
	
	/**
	 * Instantiates a new sensor sending behaviour.
	 *
	 * @param mySensorAgent the my sensor agent
	 * @param internalDataMode I/O-interface
	 */
	public SensorSendingBehaviour(Agent mySensorAgent, InternalDataModel internalDataModel, EnergyAgentIO ioInterface) {
		super(mySensorAgent);
		this.internalDataModel = internalDataModel;
		this.internalDataModel.addObserver(this);
		this.energyAgentIO = ioInterface;
	}
	
	/**
	 * This method sends a message to the GSE agent determined.
	 *
	 * @param agentAction the agent action
	 * @return true, if successful
	 */
	protected boolean sendMessage2MainServer(Concept agentAction) {
		
		if (this.getAidReceiver(null)==null) return false;
		
		try {
			// --- Definition of an 'Action' --------------
			Action act = new Action();
			act.setActor(this.myAgent.getAID());
			act.setAction(agentAction);

			// --- Define ACL message ---------------------
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setSender(this.myAgent.getAID());
			msg.addReceiver(this.getAidReceiver(null));
			msg.setLanguage(codec.getName());
			msg.setOntology(ontology.getName());
			
			if (debug==true) {
				System.out.println(this.sdf.format(new Date(this.energyAgentIO.getTime())) + ": "+this.myAgent.getAID().getLocalName()+" sends PowerSensorData to GSE agent");
			}

			// --- Send ------------------------------------
			this.myAgent.getContentManager().registerLanguage(codec);
			this.myAgent.getContentManager().registerOntology(ontology);
			this.myAgent.getContentManager().fillContent(msg, act);
			this.myAgent.send(msg);			
			return true;

		} catch (CodecException e) {
			e.printStackTrace();
			return false;
		} catch (OntologyException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	/**
	 * Gets the aid receiver.
	 *
	 * @param networkModel the network model
	 * @return the aid receiver
	 */
	public AID getAidReceiver(NetworkModel networkModel) {
		if (aidReceiver==null && networkModel!=null) {
			// --- Get Vector of networkComponents from the NetworkModel ------
			Vector<NetworkComponent> netComps = networkModel.getNetworkComponentVectorSorted();
			for (NetworkComponent netComp : netComps) {
				// --- Check the type of the NetworkComponent -----------------
				String agentClassName = networkModel.getAgentClassName(netComp); 
				if (netComp.getType().equals("GridStateEstimator")==true && agentClassName!=null) {
					aidReceiver = new AID(netComp.getId(), AID.ISLOCALNAME); // --- To change for the real / testbed application ---
					break;
				}
			}
		}
		return aidReceiver;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable observable, Object updateObject) {
		if (updateObject==CHANGED.NETWORK_MODEL) {
			this.getAidReceiver(this.internalDataModel.getNetworkModel());
		} else if (updateObject==CHANGED.MEASUREMENTS_FROM_SYSTEM) {
			this.restart();
		}
	}

	
}
