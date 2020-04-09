package de.enflexit.ea.core.globalDataModel.blackboard;

/**
 * The Class GraphNodeAnswer represents an extended {@link AbstractBlackoardAnswer}.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class GraphNodeAnswer extends AbstractBlackoardAnswer {

	private static final long serialVersionUID = -679778118583726548L;

	private String identifier;
	private Object graphNodeDataModel;

	
	/**
	 * Instantiates a GraphNodeAnswer for a {@link BlackboardRequest}.
	 *
	 * @param identifier the identifier
	 * @param networkComponentDataModel the network component data model
	 */
	public GraphNodeAnswer(String identifier, Object networkComponentDataModel) {
		this.identifier = identifier;
		this.graphNodeDataModel = networkComponentDataModel;
	}
	
	/**
	 * Gets the identifier of the NetworkComponent.
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
	/**
	 * Sets the identifier of the NetworkComponent.
	 * @param identifier the new identifier
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	/**
	 * Gets the graph node data model.
	 * @return the graph node data model
	 */
	public Object getGraphNodeDataModel() {
		return graphNodeDataModel;
	}
	/**
	 * Sets the graph node data model.
	 * @param graphNodeDataModel the new graph node data model
	 */
	public void setGraphNodeDataModel(Object graphNodeDataModel) {
		this.graphNodeDataModel = graphNodeDataModel;
	}
	
}
