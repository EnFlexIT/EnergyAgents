package hygrid.env.adapter;

import java.util.Vector;

import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter4Ontology;
import org.awb.env.networkModel.controller.GraphEnvironmentController;

import agentgui.ontology.AgentGUI_BaseOntology;
import agentgui.ontology.TimeSeriesChart;
import de.enflexit.eom.awb.adapter.EomDataModelAdapterOntology;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandlerOntology;
import hygrid.globalDataModel.ontology.HyGridOntology;
import hygrid.globalDataModel.ontology.Sensor;
import jade.content.onto.Ontology;

/**
 * The Class SensorDataModelAdapter defines the data model 
 * for a sensor / mBox within the defined {@link NetworkModel}.
 * In this special case an {@link NetworkComponentAdapter4Ontology} was
 * extended that enables to use ontology instances as a data model.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class SensorDataModelAdapter extends EomDataModelAdapterOntology {

	private Vector<Class<? extends Ontology>> ontologyBaseClasses = null;
	private String[] ontologyClassReferences = null;
	
	private SensorDataModelStorageHandler storageHandler;

	
	/**
	 * The constructor of this class
	 * @param graphController the current graph controller
	 */
	public SensorDataModelAdapter(GraphEnvironmentController graphController) {
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
			this.ontologyClassReferences[0] = Sensor.class.getName();
			this.ontologyClassReferences[1] = TimeSeriesChart.class.getName();
		}
		return this.ontologyClassReferences;
	}

	
	/* (non-Javadoc)
	 * @see de.enflexit.eom.awb.EomDataModelAdapterOntology#getOntologyClassReferencesToExchangeWithEOM()
	 */
	@Override
	public Vector<Class<?>> getOntologyClassReferencesToExchangeWithEOM() {
		return new Vector<>();
	}

	/* (non-Javadoc)
	 * @see de.enflexit.eom.awb.EomDataModelAdapterOntology#getTabOrder()
	 */
	@Override
	public TabOrder getTabOrder() {
		return TabOrder.OntologyModelFirst;
	}
	
	
	/* (non-Javadoc)
	 * @see de.enflexit.eom.awb.EomDataModelAdapterOntology#getDataModel()
	 */
	@Override
	public Object getDataModel() {
		return SensorDataModelStorageHandler.convertToObjectArray(super.getDataModel());
	}
	/* (non-Javadoc)
	 * @see de.enflexit.eom.awb.EomDataModelAdapterOntology#setDataModel(java.lang.Object)
	 */
	@Override
	public void setDataModel(Object dataModel) {
		super.setDataModel(SensorDataModelStorageHandler.convertToTreeMap(dataModel));
	}

	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.adapter.NetworkComponentAdapter4Ontology#getDataModelStorageHandler()
	 */
	@Override
	public EomDataModelStorageHandlerOntology getDataModelStorageHandler() {
		if (storageHandler==null) {
			storageHandler = new SensorDataModelStorageHandler(this);
		}
		return storageHandler;
	}
	
}
