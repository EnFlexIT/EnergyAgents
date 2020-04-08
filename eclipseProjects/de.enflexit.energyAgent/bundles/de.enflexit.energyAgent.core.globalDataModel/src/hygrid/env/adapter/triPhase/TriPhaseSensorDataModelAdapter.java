package hygrid.env.adapter.triPhase;

import java.util.Vector;

import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter4Ontology;
import org.awb.env.networkModel.adapter.dataModel.AbstractDataModelStorageHandler;
import org.awb.env.networkModel.adapter.dataModel.DataModelStorageHandlerOntology;
import org.awb.env.networkModel.controller.GraphEnvironmentController;

import agentgui.ontology.AgentGUI_BaseOntology;
import agentgui.ontology.TimeSeriesChart;
import agentgui.ontology.TimeSeriesChartSettings;
import de.enflexit.energyAgent.core.globalDataModel.ontology.HyGridOntology;
import de.enflexit.energyAgent.core.globalDataModel.ontology.Sensor;
import de.enflexit.energyAgent.core.globalDataModel.ontology.SensorProperties;
import de.enflexit.energyAgent.core.globalDataModel.ontology.TriPhaseSensorState;
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
		
		TriPhaseSensorStorageHandlerOntology storageHandler;

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

		/* (non-Javadoc)
		 * @see org.awb.env.networkModel.adapter.NetworkComponentAdapter4Ontology#getDataModelStorageHandler()
		 */
		@Override
		protected AbstractDataModelStorageHandler getDataModelStorageHandler() {
			if (this.storageHandler==null) {
				this.storageHandler = new TriPhaseSensorStorageHandlerOntology(this, this.getPartModelID());
			}
			return this.storageHandler;
		}
		
	}
	
	/**
	 * A specialized StorageHandler for the ontology part, required for converting the old data model to the new structure. 
	 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
	 */
	private class TriPhaseSensorStorageHandlerOntology extends DataModelStorageHandlerOntology {
		
		private boolean debug = false;
		
		private final String newOntoXmlStringStart = "<SensorProperties";	// Final whitespace is important, as SensorProperties should not be matched!
		private final String oldOntoClassReference = Sensor.class.getName();

		public TriPhaseSensorStorageHandlerOntology(NetworkComponentAdapter4Ontology ontologyAdapter, String partModelID) {
			super(ontologyAdapter, partModelID);
		}
		
		/* (non-Javadoc)
		 * @see org.awb.env.networkModel.dataModel.DataModelStorageHandlerOntology#createInstancesFromOntologyXmlVector(java.util.Vector)
		 */
		@Override
		protected Object[] createInstancesFromOntologyXmlVector(Vector<String> ontologyXmlVector) {
			String firstXmlString = ontologyXmlVector.get(0);
			if (firstXmlString!=null && firstXmlString.startsWith(newOntoXmlStringStart)==false) {
				// --- Old style data model -> convert ------------------------
				if (this.debug==true) {
					System.out.println("[" + this.getClass().getSimpleName() + "] Data model for component " + TriPhaseSensorDataModelAdapter.this.getNetworkComponent().getId() + " contains a Sensor instance, converting to SensorProperties and SensorState");
				}
				
				Object[] instances = new Object[TriPhaseSensorDataModelAdapter.this.getOntologyClassReferences().length];
				
				Sensor oldSensor = (Sensor) this.getInstanceOfXML(firstXmlString, oldOntoClassReference, HyGridOntology.getInstance());
				SensorProperties newSensor = this.getSensorPropertiesFromSensor(oldSensor);
				instances[0] = newSensor;
				
				instances[1] = new TriPhaseSensorState();
				
				TimeSeriesChart tsc = new TimeSeriesChart();
				tsc.setTimeSeriesVisualisationSettings(new TimeSeriesChartSettings());
				instances[2] = tsc;
				
				return instances;
				
			} else {
				// --- New style data model already ---------------------------
				return super.createInstancesFromOntologyXmlVector(ontologyXmlVector);
			}
		}

		/**
		 * Converts a {@link Sensor} instance to a {@link SensorProperties} instance
		 * @param sensor the sensor
		 * @return the sensor properties
		 */
		private SensorProperties getSensorPropertiesFromSensor(Sensor sensor) {
			SensorProperties sensorProperties = new SensorProperties();
			sensorProperties.setDim(sensor.getDim());
			sensorProperties.setDin(sensor.getDin());
			sensorProperties.setLength(sensor.getLength());
			sensorProperties.setLinearCapacitance(sensor.getLinearCapacitance());
			sensorProperties.setLinearConductance(sensor.getLinearConductance());
			sensorProperties.setLinearReactance(sensor.getLinearReactance());
			sensorProperties.getLinearReactance().setUnit("Ω\\km");
			sensorProperties.setLinearResistance(sensor.getLinearResistance());
			sensorProperties.getLinearResistance().setUnit("Ω\\km");
			sensorProperties.setMaxCurrent(sensor.getMaxCurrent());
			sensorProperties.setSensorID(sensor.getSensorID());
			sensorProperties.setMeasureLocation(sensor.getMeasureLocation());
			return sensorProperties;
		}
	}
	
}
