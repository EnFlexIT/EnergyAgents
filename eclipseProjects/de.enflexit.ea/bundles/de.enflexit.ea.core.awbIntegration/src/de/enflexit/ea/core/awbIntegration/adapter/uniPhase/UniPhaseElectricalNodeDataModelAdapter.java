package de.enflexit.ea.core.awbIntegration.adapter.uniPhase;

import java.util.Vector;

import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter4Ontology;
import org.awb.env.networkModel.controller.GraphEnvironmentController;

import agentgui.ontology.AgentGUI_BaseOntology;
import agentgui.ontology.TimeSeriesChart;
import de.enflexit.ea.core.dataModel.TransformerHelper;
import de.enflexit.ea.core.dataModel.ontology.ElectricalNodeProperties;
import de.enflexit.ea.core.dataModel.ontology.HyGridOntology;
import de.enflexit.ea.core.dataModel.ontology.TransformerNodeProperties;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseElectricalNodeState;
import jade.content.onto.Ontology;

/**
 * This class defines the data model for a uni-phase electrical node within the defined {@link NetworkModel},
 * which consists of a group of ontology objects describing its static and dynamic properties.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class UniPhaseElectricalNodeDataModelAdapter extends NetworkComponentAdapter4Ontology {
	
	private Vector<Class<? extends Ontology>> ontologyBaseClasses = null;
	private String[] ontologyClassReferences = null;

	/**
	 * Instantiates a new UniPhaseElectricalNodeDataModelAdapter.
	 * @param graphController the graph controller
	 */
	public UniPhaseElectricalNodeDataModelAdapter(GraphEnvironmentController graphController) {
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
			this.ontologyClassReferences[1] = UniPhaseElectricalNodeState.class.getName();
			this.ontologyClassReferences[2] = TimeSeriesChart.class.getName();			
		}
		return this.ontologyClassReferences;
	}
	
	/**
	 * Checks if the current GraphNode belongs to a transformer.
	 * @return true, if is transformer node
	 */
	private boolean isTransformerNode() {
		return TransformerHelper.isTransformer(this.getGraphNode(), this.graphController.getNetworkModel());
	}
	
}
