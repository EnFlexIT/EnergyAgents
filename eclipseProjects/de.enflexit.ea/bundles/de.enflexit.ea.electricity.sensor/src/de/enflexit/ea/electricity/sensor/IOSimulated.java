package de.enflexit.ea.electricity.sensor;

import java.util.HashSet;
import java.util.Vector;

import org.awb.env.networkModel.GraphElement;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import agentgui.simulationService.transaction.EnvironmentNotification;
import de.enflexit.ea.core.AbstractIOSimulated;
import de.enflexit.ea.core.dataModel.GlobalHyGridConstants;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.ExecutionDataBase;
import de.enflexit.ea.core.dataModel.blackboard.AbstractBlackboardAnswer;
import de.enflexit.ea.core.dataModel.blackboard.BlackboardRequest;
import de.enflexit.ea.core.dataModel.blackboard.MultipleBlackboardAnswer;
import de.enflexit.ea.core.dataModel.blackboard.SingleRequestSpecifier;
import de.enflexit.ea.core.dataModel.blackboard.SingleRequestSpecifier.RequestType;
import de.enflexit.ea.core.dataModel.ontology.CableState;
import de.enflexit.ea.core.dataModel.ontology.ElectricalNodeState;
import de.enflexit.ea.electricity.blackboard.CurrentLevelAnswer;
import de.enflexit.ea.electricity.blackboard.ElectricityRequestObjective;
import de.enflexit.ea.electricity.blackboard.VoltageLevelAnswer;
import jade.core.AID;

/**
 * The Class IOSimulated is used to simulate measurements from an energy conversion 
 * process, if the current project setup is used for simulations.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public abstract class IOSimulated extends AbstractIOSimulated {

	private static final long serialVersionUID = 3659353219575016108L;
	
	private ElectricalNodeState nodeStateAnswer;
	private CableState edgeStateAnswer;
	
	private boolean debug = false;
	
	/**
	 * Instantiates a new simulated IO behaviour for the {@link AbstractSensorAgent}.
	 * @param agent the current {@link AbstractSensorAgent}
	 */
	public IOSimulated(AbstractSensorAgent agent) {
		super(agent);
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.AbstractIOSimulated#commitMeasurementsToAgentsManually()
	 */
	@Override
	protected boolean commitMeasurementsToAgentsManually() {
		if (this.getExecutionDataBase()==ExecutionDataBase.SensorData) {
			return false;
		} 
		return true;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.AbstractIOSimulated#prepareForSimulation(org.awb.env.networkModel.helper.NetworkModel)
	 */
	public void prepareForSimulation(NetworkModel networkModel) {

		// --- Nothing to prepare in case of sensor data simulations ----------
		if (this.getExecutionDataBase()==ExecutionDataBase.SensorData) return;
		
		NetworkComponent netComp = networkModel.getNetworkComponent(this.myAgent.getLocalName());
		
		// --- Get the needed information for this request -------------------- 
		Vector<SingleRequestSpecifier> requestVector = new Vector<SingleRequestSpecifier>();
		
		SingleRequestSpecifier spec1 = new SingleRequestSpecifier(ElectricityRequestObjective.CurrentLevels, netComp.getId());
		requestVector.add(spec1);
		
		// --- Find the GraphNode that is to be used for the voltage level ----
		Vector<NetworkComponent> neighbours = networkModel.getNeighbourNetworkComponents(netComp);
		NetworkComponent relevantNeighbour = null;
		for (NetworkComponent neighbourComp : neighbours) {
			if (neighbourComp.getType().equals("Transformer")) {
				relevantNeighbour = neighbourComp;
				break;
			} else if (neighbourComp.getType().equals("CableCabinet")) {
				relevantNeighbour = neighbourComp;
				break;
			}
		}

		if (relevantNeighbour== null) return;
		HashSet<GraphElement> graphElements = networkModel.getGraphElementsOfNetworkComponent(relevantNeighbour, new GraphNode()); 
		if (graphElements==null || graphElements.size()==0) return;
		GraphNode node = (GraphNode) graphElements.iterator().next();
		
		SingleRequestSpecifier spec2 = new SingleRequestSpecifier(ElectricityRequestObjective.VoltageLevels, node.getId());
		requestVector.add(spec2);
		
		// --- Prepare BlackboardRequest ----------------------------
		AID bbAgent = new AID(GlobalHyGridConstants.BLACKBOARD_AGENT_NAME, AID.ISLOCALNAME);
		BlackboardRequest bbRequest = new BlackboardRequest(this.myAgent.getAID(), RequestType.SubscriptionRequest, requestVector);
		this.sendAgentNotification(bbAgent, bbRequest);
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.AbstractIOSimulated#onEnvironmentNotification(agentgui.simulationService.transaction.EnvironmentNotification)
	 */
	@Override
	protected EnvironmentNotification onEnvironmentNotification(EnvironmentNotification notification) {
		if (notification.getNotification()!=null) {
			
			if (notification.getNotification() instanceof MultipleBlackboardAnswer) {
				
				// --- Extract and store the answers ----------------------------------------
				MultipleBlackboardAnswer mba = (MultipleBlackboardAnswer) notification.getNotification();
				for (int i=0; i<mba.getAnswerVector().size(); i++) {
					AbstractBlackboardAnswer aba = mba.getAnswerVector().get(i);
					if (aba instanceof VoltageLevelAnswer) {
						if (this.debug==true){
							System.out.println(myAgent.getClass().getSimpleName() + " " + myAgent.getLocalName() + ": VoltageLevelAnswer received");
						}
						this.processVoltageLevelAnswer((VoltageLevelAnswer) aba);
					} else if (aba instanceof CurrentLevelAnswer) {
						if (this.debug==true){
							System.out.println(myAgent.getClass().getSimpleName() + " " + myAgent.getLocalName() + ": CurrentLevelAnswer received");
						}
						this.processCurrentLevelAnswer((CurrentLevelAnswer) aba);
					}
				}
			}
			
			// --- If both answers are available, commit the measurements -----
			if (this.nodeStateAnswer!=null && this.edgeStateAnswer!=null) {
				
				if (this.debug==true){
					System.out.println(myAgent.getClass().getSimpleName() + " " + myAgent.getLocalName() + ": Both answers received, commiting measurements");
				}
				this.commitMeasurement();
				
				// --- Reset the answer fields --------------------------------
				this.nodeStateAnswer = null;
				this.edgeStateAnswer = null;
			}
		}
		return super.onEnvironmentNotification(notification);
	}
	
	/**
	 * Process a voltage level answer.
	 * @param vla the voltage level answer
	 */
	protected abstract void processVoltageLevelAnswer(VoltageLevelAnswer vla);
	
	/**
	 * Process a current level answer.
	 * @param cla the current level answer
	 */
	protected abstract void processCurrentLevelAnswer(CurrentLevelAnswer cla);
	
	
	/**
	 * Sets the node state answer.
	 * @param nodeStateAnswer the new node state answer
	 */
	protected void setNodeStateAnswer(ElectricalNodeState nodeStateAnswer) {
		this.nodeStateAnswer = nodeStateAnswer;
	}

	/**
	 * Gets the node state answer.
	 * @return the node state answer
	 */
	protected ElectricalNodeState getNodeStateAnswer() {
		return nodeStateAnswer;
	}

	/**
	 * Sets the edge state answer.
	 * @param edgeStateAnswer the new edge state answer
	 */
	protected void setEdgeStateAnswer(CableState edgeStateAnswer) {
		this.edgeStateAnswer = edgeStateAnswer;
	}

	/**
	 * Gets the edge state answer.
	 * @return the edge state answer
	 */
	protected CableState getEdgeStateAnswer() {
		return edgeStateAnswer;
	}

	/**
	 * Commits the measurements.
	 */
	protected abstract void commitMeasurement();

}