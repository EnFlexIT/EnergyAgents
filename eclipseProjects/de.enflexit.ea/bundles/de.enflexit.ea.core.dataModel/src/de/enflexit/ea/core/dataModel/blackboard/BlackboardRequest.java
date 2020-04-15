package de.enflexit.ea.core.dataModel.blackboard;

import java.io.Serializable;
import java.util.Vector;

import de.enflexit.ea.core.dataModel.blackboard.RequestSpecifier.RequestObjective;
import de.enflexit.ea.core.dataModel.blackboard.RequestSpecifier.RequestType;
import jade.core.AID;

/**
 * The class BlackboardRequest can be used in order to request information 
 * about the current state of the simulation that are controlled by the 
 * SimulationManager. Send this request to the current instance of 
 * the {@link BlackboardAgent} that will reply it. 
 * 
 * @see Blackboard
 * @see BlackboardAgent
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class BlackboardRequest implements Serializable {

	private static final long serialVersionUID = -7051645509894663641L;

	private AID requester;
	private RequestType requestType;
	private Vector<RequestSpecifier> requestSpecifierVector;
	
	
	/**
	 * Instantiates a new blackboard request.
	 *
	 * @param requester the requester
	 * @param requestType the request type
	 * @param objective the objective
	 * @param identifier the identifier of the component
	 */
	public BlackboardRequest(AID requester, RequestType requestType, RequestObjective objective, String identifier) {
		this.requester = requester;
		this.requestType = requestType;
		this.getRequestSpecifierVector().add(new RequestSpecifier(objective, identifier));
	}
	/**
	 * Instantiates a new blackboard request.
	 *
	 * @param requester the requester
	 * @param requestType the request type
	 * @param requestSpecifier the request specifier
	 */
	public BlackboardRequest(AID requester, RequestType requestType, RequestSpecifier requestSpecifier) {
		this.requester = requester;
		this.requestType = requestType;
		this.getRequestSpecifierVector().add(requestSpecifier);
	}
	/**
	 * Instantiates a new blackboard request.
	 *
	 * @param requester the requester
	 * @param requestType the request type
	 * @param requestSpecifierVector the request specifier vector
	 */
	public BlackboardRequest(AID requester, RequestType requestType, Vector<RequestSpecifier> requestSpecifierVector) {
		this.requester = requester;
		this.requestType = requestType;
		this.getRequestSpecifierVector().addAll(requestSpecifierVector);
	}
	
	/**
	 * Returns the requester.
	 * @return the requester
	 */
	public AID getRequester() {
		return requester;
	}
	/**
	 * Sets the requester.
	 * @param requester the requester to set
	 */
	public void setRequester(AID requester) {
		this.requester = requester;
	}

	/**
	 * Returns the request type.
	 * @return the requestType
	 */
	public RequestType getRequestType() {
		return requestType;
	}
	/**
	 * Sets the request type.
	 * @param requestType the requestType to set
	 */
	public void setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}

	/**
	 * Gets the request specifier vector.
	 * @return the request specifier vector
	 */
	public Vector<RequestSpecifier> getRequestSpecifierVector() {
		if (requestSpecifierVector==null) {
			requestSpecifierVector = new Vector<RequestSpecifier>();
		}
		return requestSpecifierVector;
	}
	
}
