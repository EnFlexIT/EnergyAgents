package de.enflexit.ea.core.awbIntegration.adapter.uniPhase;

import java.util.Vector;

import org.awb.env.networkModel.adapter.NetworkComponentAdapter4Ontology;
import org.awb.env.networkModel.controller.GraphEnvironmentController;

import agentgui.ontology.AgentGUI_BaseOntology;
import agentgui.ontology.TimeSeriesChart;
import de.enflexit.ea.core.dataModel.ontology.CableWithBreakerProperties;
import de.enflexit.ea.core.dataModel.ontology.HyGridOntology;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseCableState;
import jade.content.onto.Ontology;

public class UniPhaseBreakerDataModelAdapter extends NetworkComponentAdapter4Ontology {
	
	private Vector<Class<? extends Ontology>> ontologyBaseClasses = null;
	private String[] ontologyClassReferences = null;

	public UniPhaseBreakerDataModelAdapter(GraphEnvironmentController graphController) {
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
			this.ontologyClassReferences[0] = CableWithBreakerProperties.class.getName();
			this.ontologyClassReferences[1] = UniPhaseCableState.class.getName();
			this.ontologyClassReferences[2] = TimeSeriesChart.class.getName();			
		}
		return this.ontologyClassReferences;
	}

}
