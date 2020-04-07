package hygrid.env.adapter.uniPhase;

import java.util.Vector;

import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter4Ontology;
import org.awb.env.networkModel.controller.GraphEnvironmentController;

import agentgui.ontology.AgentGUI_BaseOntology;
import agentgui.ontology.TimeSeriesChart;
import hygrid.globalDataModel.ontology.HyGridOntology;
import hygrid.globalDataModel.ontology.SensorProperties;
import hygrid.globalDataModel.ontology.UniPhaseSensorState;
import jade.content.onto.Ontology;

/**
 * This class defines the data model for a uni-phase sensor within the defined {@link NetworkModel},
 * which consists of two ontology objects describing its static and dynamic properties.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class UniPhaseSensorDataModelAdapter extends NetworkComponentAdapter4Ontology {
	
	private Vector<Class<? extends Ontology>> ontologyBaseClasses = null;
	private String[] ontologyClassReferences = null;

	/**
	 * Instantiates a new UniPhaseSensorDataModelAdapter.
	 *
	 * @param graphController the graph controller
	 */
	public UniPhaseSensorDataModelAdapter(GraphEnvironmentController graphController) {
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
			this.ontologyClassReferences[0] = SensorProperties.class.getName();
			this.ontologyClassReferences[1] = UniPhaseSensorState.class.getName();
			this.ontologyClassReferences[2] = TimeSeriesChart.class.getName();			
		}
		return this.ontologyClassReferences;
	}

}
