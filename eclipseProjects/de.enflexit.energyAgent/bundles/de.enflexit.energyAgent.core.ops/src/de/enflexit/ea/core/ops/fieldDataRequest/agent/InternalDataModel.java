package de.enflexit.ea.core.ops.fieldDataRequest.agent;

import java.util.ArrayList;
import java.util.List;

import hygrid.ops.ontology.FieldDataRequest;
import jade.core.AID;

/**
 * The internal data model of the {@link FieldDataRequestAgent}
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class InternalDataModel {
	
	private AID ceaAID;
	private FieldDataRequest dataRequest;
	
	private List<String> pendingReplies;
	
	/**
	 * Gets the cea AID.
	 * @return the cea AID
	 */
	public AID getCeaAID() {
		return ceaAID;
	}
	
	/**
	 * Sets the cea AID.
	 * @param ceaAID the new cea AID
	 */
	public void setCeaAID(AID ceaAID) {
		this.ceaAID = ceaAID;
	}
	
	/**
	 * Gets the data request.
	 * @return the data request
	 */
	public FieldDataRequest getDataRequest() {
		return dataRequest;
	}
	
	/**
	 * Sets the data request.
	 * @param dataRequest the new data request
	 */
	public void setDataRequest(FieldDataRequest dataRequest) {
		this.dataRequest = dataRequest;
	}

	/**
	 * Gets the pending replies.
	 * @return the pending replies
	 */
	protected List<String> getPendingReplies() {
		if (pendingReplies==null) {
			pendingReplies = new ArrayList<>();
		}
		return pendingReplies;
	}
	
	
	
	
}
