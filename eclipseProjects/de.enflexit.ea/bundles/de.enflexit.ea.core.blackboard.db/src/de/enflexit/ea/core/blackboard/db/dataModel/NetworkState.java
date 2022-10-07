package de.enflexit.ea.core.blackboard.db.dataModel;

import java.util.Calendar;
import java.util.List;

public class NetworkState {
	
	private Calendar stateTime;

	private List<NodeResult> nodeResultList;
	private List<EdgeResult> edgeResultList; 
	private List<TrafoResult> trafoResultList;

	
	public Calendar getStateTime() {
		return stateTime;
	}
	public void setStateTime(Calendar stateTime) {
		this.stateTime = stateTime;
	}

	
	public List<NodeResult> getNodeResultList() {
		return nodeResultList;
	}
	public void setNodeResultList(List<NodeResult> nodeResultList) {
		this.nodeResultList = nodeResultList;
	}

	public List<EdgeResult> getEdgeResultList() {
		return edgeResultList;
	}
	public void setEdgeResultList(List<EdgeResult> edgeResultList) {
		this.edgeResultList = edgeResultList;
	}

	public List<TrafoResult> getTrafoResultList() {
		return trafoResultList;
	}
	public void setTrafoResultList(List<TrafoResult> trafoResultList) {
		this.trafoResultList = trafoResultList;
	}
	
	
}
