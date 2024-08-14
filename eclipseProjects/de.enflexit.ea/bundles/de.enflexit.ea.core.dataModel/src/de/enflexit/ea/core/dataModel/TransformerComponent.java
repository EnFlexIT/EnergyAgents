package de.enflexit.ea.core.dataModel;

import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.ea.core.dataModel.ontology.TransformerNodeProperties;

/**
 * The Class TransformerComponent.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class TransformerComponent {

	private NetworkComponent networkComponent;
	private GraphNode graphNode;
	private TransformerNodeProperties transformerNodeProperties;
	private float ratedVoltage;
	
	private TransformerNodeProperties oppositeTransformerNodeProperties;
	private float oppositeRatedVoltage;
	
	
	/**
	 * Instantiates a new transformer component.
	 */
	public TransformerComponent() {	}
	
	/**
	 * Instantiates a new transformer component.
	 *
	 * @param networkComponent the network component
	 * @param graphNode the graph node
	 * @param transformerNodeProperties the transformer node properties
	 * @param ratedVoltage the rated voltage
	 * @param oppositeTransformerNodeProperties the opposite transformer node properties
	 * @param oppositeRatedVoltage the opposite rated voltage
	 */
	public TransformerComponent(NetworkComponent networkComponent, GraphNode graphNode, TransformerNodeProperties transformerNodeProperties, float ratedVoltage, TransformerNodeProperties oppositeTransformerNodeProperties, float oppositeRatedVoltage) {
		this.networkComponent = networkComponent;
		this.graphNode = graphNode;
		this.transformerNodeProperties = transformerNodeProperties;
		this.ratedVoltage = ratedVoltage;
		this.oppositeTransformerNodeProperties = oppositeTransformerNodeProperties;
		this.oppositeRatedVoltage = oppositeRatedVoltage;
	}
	
	/**
	 * Gets the network component.
	 * @return the network component
	 */
	public NetworkComponent getNetworkComponent() {
		return networkComponent;
	}
	/**
	 * Sets the network component.
	 * @param networkComponent the new network component
	 */
	public void setNetworkComponent(NetworkComponent networkComponent) {
		this.networkComponent = networkComponent;
	}
	
	/**
	 * Gets the graph node.
	 * @return the graph node
	 */
	public GraphNode getGraphNode() {
		return graphNode;
	}
	/**
	 * Sets the graph node.
	 * @param graphNode the new graph node
	 */
	public void setGraphNode(GraphNode graphNode) {
		this.graphNode = graphNode;
	}
	
	/**
	 * Gets the transformer node properties.
	 * @return the transformer node properties
	 */
	public TransformerNodeProperties getTransformerNodeProperties() {
		return transformerNodeProperties;
	}
	/**
	 * Sets the transformer node properties.
	 * @param transformerNodeProperties the new transformer node properties
	 */
	public void setTransformerNodeProperties(TransformerNodeProperties transformerNodeProperties) {
		this.transformerNodeProperties = transformerNodeProperties;
	}
	
	/**
	 * Gets the rated voltage.
	 * @return the rated voltage
	 */
	public float getRatedVoltage() {
		return ratedVoltage;
	}
	/**
	 * Sets the rated voltage.
	 * @param ratedVoltage the new rated voltage
	 */
	public void setRatedVoltage(float ratedVoltage) {
		this.ratedVoltage = ratedVoltage;
	}
	
	/**
	 * Gets the opposite transformer node properties.
	 * @return the opposite transformer node properties
	 */
	public TransformerNodeProperties getOppositeTransformerNodeProperties() {
		return oppositeTransformerNodeProperties;
	}
	/**
	 * Sets the opposite transformer node properties.
	 * @param oppositeTransformerNodeProperties the new opposite transformer node properties
	 */
	public void setOppositeTransformerNodeProperties(TransformerNodeProperties oppositeTransformerNodeProperties) {
		this.oppositeTransformerNodeProperties = oppositeTransformerNodeProperties;
	}

	/**
	 * Gets the opposite rated voltage.
	 * @return the opposite rated voltage
	 */
	public float getOppositeRatedVoltage() {
		return oppositeRatedVoltage;
	}
	/**
	 * Sets the opposite rated voltage.
	 * @param oppositeRatedVoltage the new opposite rated voltage
	 */
	public void setOppositeRatedVoltage(float oppositeRatedVoltage) {
		this.oppositeRatedVoltage = oppositeRatedVoltage;
	}
	
}
