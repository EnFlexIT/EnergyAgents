package hygrid.env.adapter;

import java.util.Vector;

import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter4Ontology;
import org.awb.env.networkModel.controller.GraphEnvironmentController;

import agentgui.ontology.AgentGUI_BaseOntology;
import agentgui.ontology.TimeSeriesChart;
import hygrid.globalDataModel.ontology.ElectricalNodeProperties;
import hygrid.globalDataModel.ontology.HyGridOntology;
import hygrid.globalDataModel.ontology.TransformerNodeProperties;
import hygrid.globalDataModel.ontology.TriPhaseCableState;
import jade.content.onto.Ontology;

/**
 * The Class ElectricalTriNodeDataModelAdapter defines the data model 
 * for the three phase nodes within the defined {@link NetworkModel}.
 * In this special case an {@link NetworkComponentAdapter4Ontology} was
 * extended that enables to use ontology instances as a data model.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class ElectricalTriNodeDataModelAdapter extends NetworkComponentAdapter4Ontology {

	private Vector<Class<? extends Ontology>> ontologyBaseClasses = null;
	private String[] ontologyClassReferences = null;
	
	/**
	 * The constructor of this class
	 * @param graphController the current graph controller
	 */
	public ElectricalTriNodeDataModelAdapter(GraphEnvironmentController graphController) {
		super(graphController);
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.helper.NetworkComponentAdapter4Ontology#getOntologyBaseClasses()
	 */
	@Override
	public Vector<Class<? extends Ontology>> getOntologyBaseClasses() {
		if (this.ontologyBaseClasses==null) {
			this.ontologyBaseClasses = new Vector<Class<? extends Ontology>>();
			this.ontologyBaseClasses.add(HyGridOntology.class);
			this.ontologyBaseClasses.add(AgentGUI_BaseOntology.class);
		}
		return this.ontologyBaseClasses;
	}
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.helper.NetworkComponentAdapter4Ontology#getOntologyClassReferences()
	 */
	@Override
	public String[] getOntologyClassReferences() {
		if (this.ontologyClassReferences==null) {
			this.ontologyClassReferences = new String[2];
			if (this.isTransformerNode()==true) {
				this.ontologyClassReferences[0] = TransformerNodeProperties.class.getName();
			} else {
				this.ontologyClassReferences[0] = ElectricalNodeProperties.class.getName();
			}
			this.ontologyClassReferences[1] = TriPhaseCableState.class.getName();
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
	
}
