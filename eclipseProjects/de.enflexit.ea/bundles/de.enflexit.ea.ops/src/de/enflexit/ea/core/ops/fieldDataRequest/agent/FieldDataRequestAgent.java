package de.enflexit.ea.core.ops.fieldDataRequest.agent;

import de.enflexit.ea.core.dataModel.opsOntology.FieldDataRequest;
import de.enflexit.ea.core.dataModel.opsOntology.OpsOntology;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.core.Agent;

/**
 * This agent will initiate a request for field data and process the results.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class FieldDataRequestAgent extends Agent {
	
	private static final long serialVersionUID = -6487016359325708001L;
	
	public static final String DEFAULT_AGENT_NAME = "FiDaReAg";
	
	private InternalDataModel internalDataModel;
	
	/* (non-Javadoc)
	 * @see jade.core.Agent#setup()
	 */
	@Override
	protected void setup() {
		
		Object[] arguments = this.getArguments();
		for (int i=0; i<arguments.length; i++) {
			if (arguments[i] instanceof FieldDataRequest) {
				this.getInternalDataModel().setDataRequest((FieldDataRequest) arguments[i]);
			}else if(arguments[i] instanceof AID) {
				this.getInternalDataModel().setCeaAID((AID) arguments[i]);
			}
		}
		
		this.getContentManager().registerLanguage(new SLCodec());
		this.getContentManager().registerOntology(OpsOntology.getInstance());
		
		this.addBehaviour(new HandleRepliesBehaviour());
		this.addBehaviour(new SendRequestBehaviour());
		
	}
	
	/**
	 * Gets the internal data model.
	 * @return the internal data model
	 */
	public InternalDataModel getInternalDataModel() {
		if (internalDataModel==null) {
			internalDataModel = new InternalDataModel();
		}
		return internalDataModel;
	}

}
