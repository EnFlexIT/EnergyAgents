package hygrid.env.adapter.triPhase;

import java.util.Vector;

import org.awb.env.networkModel.adapter.NetworkComponentAdapter4Ontology;
import org.awb.env.networkModel.adapter.dataModel.DataModelStorageHandlerOntology;

import agentgui.ontology.TimeSeriesChart;
import agentgui.ontology.TimeSeriesChartSettings;
import de.enflexit.ea.core.globalDataModel.ontology.Cable;
import de.enflexit.ea.core.globalDataModel.ontology.CableProperties;
import de.enflexit.ea.core.globalDataModel.ontology.HyGridOntology;
import de.enflexit.ea.core.globalDataModel.ontology.TriPhaseCableState;

/**
 * The Class TriPhaseCableDataModelStoragerHandler.
 */
public class TriPhaseCableStoragerHandler extends DataModelStorageHandlerOntology {

	private static final String newOntoXmlStringStart = "<" + CableProperties.class.getSimpleName();
	private static final String oldOntoClassReference = Cable.class.getName();
	
	private boolean debug = false;
	
	/**
	 * Instantiates a new TriPhaseCableDataModelStoragerHandler.
	 * @param ontologyAdapter the ontology adapter
	 */
	public TriPhaseCableStoragerHandler(NetworkComponentAdapter4Ontology ontologyAdapter, String partModelID) {
		super(ontologyAdapter, partModelID);
	}
	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.dataModel.DataModelStorageHandlerOntology#createInstancesFromOntologyXmlVector(java.util.Vector)
	 */
	@Override
	protected Object[] createInstancesFromOntologyXmlVector(Vector<String> ontologyXmlVector) {
		// --- Create instances -----------------------------------------------
				
		String firstXmlString = ontologyXmlVector.get(0);
		if (firstXmlString!=null && firstXmlString.startsWith(newOntoXmlStringStart)==false) {
			// --- Old style data model -> convert ----------------------------
			if (this.debug==true) {
				System.out.println("[" + this.getClass().getSimpleName() + "] Data model for component " + this.ontologyAdapter.getNetworkComponent().getId() + " contains a Cable instance, converting to CableProperties and CableState");
			}
			
			Object[] instances = new Object[this.ontologyAdapter.getOntologyClassReferences().length];
			
			Cable oldCable = (Cable) this.getInstanceOfXML(firstXmlString, oldOntoClassReference, HyGridOntology.getInstance());
			CableProperties newCable = this.getCablePropertiesFromCable(oldCable);
			instances[0] = newCable;
			
			instances[1] = new TriPhaseCableState();
			
			TimeSeriesChart tsc = new TimeSeriesChart();
			tsc.setTimeSeriesVisualisationSettings(new TimeSeriesChartSettings());
			instances[2] = tsc;
			
			this.setRequiresPersistenceUpdate(true);
			
			return instances;
			
		} else {
			return super.createInstancesFromOntologyXmlVector(ontologyXmlVector);
		}
	}

	/**
	 * Converts a {@link Cable} instance to a {@link CableProperties} instance.
	 *
	 * @param cable the cable
	 * @return the cable properties from cable
	 */
	private CableProperties getCablePropertiesFromCable(Cable cable) {
		CableProperties cableProperties = new CableProperties();
		cableProperties.setDim(cable.getDim());
		cableProperties.setDin(cable.getDin());
		cableProperties.setLength(cable.getLength());
		cableProperties.setLinearCapacitance(cable.getLinearCapacitance());
		cableProperties.setLinearConductance(cable.getLinearConductance());
		cableProperties.setLinearReactance(cable.getLinearReactance());
		cableProperties.getLinearReactance().setUnit("Ω\\km");
		cableProperties.setLinearResistance(cable.getLinearResistance());
		cableProperties.getLinearResistance().setUnit("Ω\\km");
		cableProperties.setMaxCurrent(cable.getMaxCurrent());
		return cableProperties;
	}
	
}
