package hygrid.env.adapter.triPhase;

import java.util.Vector;

import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter4Ontology;
import org.awb.env.networkModel.adapter.dataModel.AbstractDataModelStorageHandler;
import org.awb.env.networkModel.controller.GraphEnvironmentController;

import agentgui.ontology.AgentGUI_BaseOntology;
import agentgui.ontology.TimeSeriesChart;
import hygrid.globalDataModel.ontology.HyGridOntology;
import hygrid.globalDataModel.ontology.TransformerNodeProperties;
import hygrid.globalDataModel.ontology.TriPhaseElectricalNodeState;
import hygrid.globalDataModel.ontology.TriPhaseElectricalTransformerState;
import hygrid.env.adapter.NetworkComponentHelper;
import hygrid.globalDataModel.ontology.ElectricalNodeProperties;
import jade.content.onto.Ontology;

/**
 * This class defines the data model for a tri-phase electrical node within the defined {@link NetworkModel},
 * which consists of a group of ontology objects describing its static and dynamic properties.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class TriPhaseElectricalNodeDataModelAdapter extends NetworkComponentAdapter4Ontology {
	
	private Vector<Class<? extends Ontology>> ontologyBaseClasses = null;
	private String[] ontologyClassReferences = null;

	private TriPhaseElectricalNodeStorageHandler storageHandler;

	/**
	 * Instantiates a new TriPhaseElectricalNodeDataModelAdapter.
	 *
	 * @param graphController the graph controller
	 */
	public TriPhaseElectricalNodeDataModelAdapter(GraphEnvironmentController graphController) {
		super(graphController);
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.adapter.NetworkComponentAdapter4Ontology#getOntologyBaseClasses()
	 */
	@Override
	public Vector<Class<? extends Ontology>> getOntologyBaseClasses() {
		if (this.ontologyBaseClasses==null) {
			this.ontologyBaseClasses = new Vector<Class<? extends Ontology>>();
			this.ontologyBaseClasses.add(HyGridOntology.class);
			this.ontologyBaseClasses.add(HyGridOntology.class);
			this.ontologyBaseClasses.add(AgentGUI_BaseOntology.class);
		}
		return this.ontologyBaseClasses;
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.adapter.NetworkComponentAdapter4Ontology#getOntologyClassReferences()
	 */
	@Override
	public String[] getOntologyClassReferences() {
		if (this.ontologyClassReferences==null) {
			this.ontologyClassReferences = new String[3];
			if (this.isTransformerNode()==true) {
				this.ontologyClassReferences[0] = TransformerNodeProperties.class.getName();
			} else {
				this.ontologyClassReferences[0] = ElectricalNodeProperties.class.getName();
			}
			if (this.isTransformerNode()==true) {
				this.ontologyClassReferences[1] = TriPhaseElectricalTransformerState.class.getName();
			} else {
				this.ontologyClassReferences[1] = TriPhaseElectricalNodeState.class.getName();
			}
			this.ontologyClassReferences[2] = TimeSeriesChart.class.getName();			
		}
		return this.ontologyClassReferences;
	}
	/**
	 * Checks if the current GraphNode belongs to a transformer.
	 * @return true, if is transformer node
	 */
	private boolean isTransformerNode() {
		return NetworkComponentHelper.isTransformer(this.getGraphNode(), this.graphController.getNetworkModel());
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.adapter.NetworkComponentAdapter4Ontology#getDataModelStorageHandler()
	 */
	@Override
	protected AbstractDataModelStorageHandler getDataModelStorageHandler() {
		if (this.storageHandler==null) {
			this.storageHandler = new TriPhaseElectricalNodeStorageHandler(this, this.getPartModelID());
		}
		return this.storageHandler;
	}

	

}
