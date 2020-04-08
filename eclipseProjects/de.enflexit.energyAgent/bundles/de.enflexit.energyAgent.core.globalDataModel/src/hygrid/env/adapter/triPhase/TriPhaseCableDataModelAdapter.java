package hygrid.env.adapter.triPhase;

import java.util.Vector;

import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter4Ontology;
import org.awb.env.networkModel.adapter.dataModel.AbstractDataModelStorageHandler;
import org.awb.env.networkModel.controller.GraphEnvironmentController;

import agentgui.ontology.AgentGUI_BaseOntology;
import agentgui.ontology.TimeSeriesChart;
import hygrid.globalDataModel.ontology.CableProperties;
import hygrid.globalDataModel.ontology.HyGridOntology;
import hygrid.globalDataModel.ontology.TriPhaseCableState;
import jade.content.onto.Ontology;

/**
 * This class defines the data model for a tri-phase cable within the defined {@link NetworkModel},
 * which consists of two ontology objects describing its static and dynamic properties.
 * To preserve backwards compatibility, this class provides a conversion function from the old
 * Cable class to the new CableProperties and TriPhaseCableState classes.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class TriPhaseCableDataModelAdapter extends NetworkComponentAdapter4Ontology {
	
	private Vector<Class<? extends Ontology>> ontologyBaseClasses = null;
	private String[] ontologyClassReferences = null;
	
	private TriPhaseCableStoragerHandler storageHandler;
	
	/**
	 * Instantiates a new TriPhaseCableDataModelAdapter.
	 * @param graphController the graph controller
	 */
	public TriPhaseCableDataModelAdapter(GraphEnvironmentController graphController) {
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
			this.ontologyClassReferences[0] = CableProperties.class.getName();
			this.ontologyClassReferences[1] = TriPhaseCableState.class.getName();
			this.ontologyClassReferences[2] = TimeSeriesChart.class.getName();			
		}
		return this.ontologyClassReferences;
	}
	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.adapter.NetworkComponentAdapter4Ontology#getDataModelStorageHandler()
	 */
	@Override
	protected AbstractDataModelStorageHandler getDataModelStorageHandler() {
		if (storageHandler==null) {
			storageHandler = new TriPhaseCableStoragerHandler(this, this.getPartModelID());
		}
		return storageHandler;
	}
	
}
