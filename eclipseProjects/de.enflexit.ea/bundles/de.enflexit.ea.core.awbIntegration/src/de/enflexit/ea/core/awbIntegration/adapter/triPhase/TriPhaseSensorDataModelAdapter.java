package de.enflexit.ea.core.awbIntegration.adapter.triPhase;

import java.util.Vector;

import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter4Ontology;
import org.awb.env.networkModel.controller.GraphEnvironmentController;

import agentgui.ontology.AgentGUI_BaseOntology;
import agentgui.ontology.TimeSeriesChart;
import de.enflexit.ea.core.dataModel.ontology.HyGridOntology;
import de.enflexit.ea.core.dataModel.ontology.SensorProperties;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseSensorState;
import de.enflexit.eom.awb.adapter.EomDataModelAdapterOntology;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandlerOntology;
import jade.content.onto.Ontology;

/**
 *This class describes the data model for a uni-phase sensor within the defined {@link NetworkModel}.
 * Context menu interaction is delegated to the superclass, as it is identical for uni- and tri-phase sensors.
 * To preserve backwards compatibility, this class provides a conversion function from the old
 * Sensor class to the new SensorProperties and TriPhaseSensorState classes.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class TriPhaseSensorDataModelAdapter extends EomDataModelAdapterOntology {
	
	private Vector<Class<? extends Ontology>> ontologyBaseClasses = null;
	private String[] ontologyClassReferences = null;
	
	private TriPhaseSensorDataModelAdapterOntology dmAdapterOntology;
	
	private EomDataModelStorageHandlerOntology storageHandler;
	
	/**
	 * Instantiates a new TriPhaseSensorDataModelAdapter.
	 *
	 * @param graphController the graph controller
	 */
	public TriPhaseSensorDataModelAdapter(GraphEnvironmentController graphController) {
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
			this.ontologyClassReferences[1] = TriPhaseSensorState.class.getName();
			this.ontologyClassReferences[2] = TimeSeriesChart.class.getName();			
		}
		return this.ontologyClassReferences;
	}

	/* (non-Javadoc)
	 * @see energy.agentgui.EomDataModelAdapterOntology#getOntologyClassReferencesToExchangeWithEOM()
	 */
	@Override
	public Vector<Class<?>> getOntologyClassReferencesToExchangeWithEOM() {
		return new Vector<>();
	}

	/* (non-Javadoc)
	 * @see energy.agentgui.EomDataModelAdapterOntology#getTabOrder()
	 */
	@Override
	public TabOrder getTabOrder() {
		return TabOrder.OntologyModelFirst;
	}
	
	/* (non-Javadoc)
	 * @see energy.agentgui.EomDataModelAdapterOntology#getDataModel()
	 */
	@Override
	public Object getDataModel() {
		return TriPhaseSensorStorageHandler.convertToObjectArray(super.getDataModel());
	}
	/* (non-Javadoc)
	 * @see energy.agentgui.EomDataModelAdapterOntology#setDataModel(java.lang.Object)
	 */
	@Override
	public void setDataModel(Object dataModel) {
		super.setDataModel(TriPhaseSensorStorageHandler.convertToTreeMap(dataModel));
	}

	/* (non-Javadoc)
	 * @see energy.agentgui.EomDataModelAdapterOntology#getDataModelStorageHandler()
	 */
	@Override
	public EomDataModelStorageHandlerOntology getDataModelStorageHandler() {
		if (this.storageHandler==null) {
			this.storageHandler = new TriPhaseSensorStorageHandler(this);
		}
		return this.storageHandler;
	}
	
	/* (non-Javadoc)
	 * @see energy.agentgui.EomDataModelAdapterOntology#getNetworkComponentAdapter4Ontology()
	 */
	@Override
	protected NetworkComponentAdapter4Ontology getNetworkComponentAdapter4Ontology() {
		if (dmAdapterOntology==null) {
			dmAdapterOntology = new TriPhaseSensorDataModelAdapterOntology(this.getGraphEnvironmentController());
		}
		return dmAdapterOntology;
	}


	/**
	 * Specialized data model adapter, required to provide a specialized storage handler for data model conversion
	 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
	 */
	private class TriPhaseSensorDataModelAdapterOntology extends NetworkComponentAdapter4Ontology {
		
		/**
		 * Instantiates a new tri phase sensor data model adapter ontology.
		 *
		 * @param graphController the graph controller
		 */
		public TriPhaseSensorDataModelAdapterOntology(GraphEnvironmentController graphController) {
			super(graphController);
		}

		/* (non-Javadoc)
		 * @see org.awb.env.networkModel.adapter.NetworkComponentAdapter4Ontology#getOntologyBaseClasses()
		 */
		@Override
		public Vector<Class<? extends Ontology>> getOntologyBaseClasses() {
			return TriPhaseSensorDataModelAdapter.this.getOntologyBaseClasses();
		}

		/* (non-Javadoc)
		 * @see org.awb.env.networkModel.adapter.NetworkComponentAdapter4Ontology#getOntologyClassReferences()
		 */
		@Override
		public String[] getOntologyClassReferences() {
			return TriPhaseSensorDataModelAdapter.this.getOntologyClassReferences();
		}
		
	}
	
	
}
