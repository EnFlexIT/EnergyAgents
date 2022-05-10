package de.enflexit.ea.electricity.transformer.eomDataModel;

import java.util.Vector;

import javax.swing.table.TableCellRenderer;

import org.awb.env.networkModel.GraphElement;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.controller.ui.NetworkComponentTablePanel;
import org.awb.env.networkModel.controller.ui.NetworkComponentTableService;

/**
 * The Class NetworkComponentTableAliasColumn defines the alias column in the {@link NetworkComponentTablePanel}
 * of the graph and network model environment.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg - Essen
 */
public class GraphNodeIDColumn implements NetworkComponentTableService {

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.controller.ui.NetworkComponentTableService#getColumnHeader()
	 */
	@Override
	public String getColumnHeader() {
		return "GraphNode-ID";
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.controller.ui.NetworkComponentTableService#getCellValue(org.awb.env.networkModel.NetworkModel, org.awb.env.networkModel.NetworkComponent)
	 */
	@Override
	public String getCellValue(NetworkModel networkModel, NetworkComponent networkComponent) {
		return getGraphNodeID(networkModel, networkComponent);
	}
	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.controller.ui.NetworkComponentTableService#getWidth()
	 */
	@Override
	public Integer getWidth() {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.controller.ui.NetworkComponentTableService#getMinWidth()
	 */
	@Override
	public Integer getMinWidth() {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.controller.ui.NetworkComponentTableService#getMaxWidth()
	 */
	@Override
	public Integer getMaxWidth() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.controller.ui.NetworkComponentTableService#getTableCellRenderer()
	 */
	@Override
	public TableCellRenderer getTableCellRenderer() {
		return null;
	}

	/**
	 * Returns the graph node ID for the specified NetworkComponent if the component consists of a single {@link GraphNode}.
	 *
	 * @param networkModel the network model
	 * @param networkComponent the network component
	 * @return the graph node ID
	 */
	public static String getGraphNodeID(NetworkModel networkModel, NetworkComponent networkComponent) {
		
		if (networkModel==null || networkComponent==null) return null;
		
		Vector<GraphElement> graphElementVector = networkModel.getGraphElementsFromNetworkComponent(networkComponent);
		boolean isGraphNode = graphElementVector.size()==1 && graphElementVector.get(0) instanceof GraphNode;
		if (isGraphNode==true) {
			return graphElementVector.get(0).getId();
		}
		return null;
	}
	/**
	 * Returns the NetworkComponent of the specified GraphNode-ID.
	 *
	 * @param networkModel the network model
	 * @param graphNodeID the graph node ID
	 * @return the network component
	 */
	public static NetworkComponent getNetworkComponent(NetworkModel networkModel, String graphNodeID) {
		
		if (networkModel==null || graphNodeID==null || graphNodeID.isEmpty()==true) return null;
		
		GraphElement graphElement = networkModel.getGraphElement(graphNodeID);
		if (graphElement!=null && graphElement instanceof GraphNode) {
			return networkModel.getDistributionNode((GraphNode) graphElement);
		}
		return null;
	}
}
