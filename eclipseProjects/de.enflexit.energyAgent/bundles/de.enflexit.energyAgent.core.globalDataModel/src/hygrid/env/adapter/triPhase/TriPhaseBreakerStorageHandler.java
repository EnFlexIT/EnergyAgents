package hygrid.env.adapter.triPhase;

import java.util.Vector;

import org.awb.env.networkModel.adapter.NetworkComponentAdapter4Ontology;
import org.awb.env.networkModel.adapter.dataModel.DataModelStorageHandlerOntology;

import agentgui.ontology.TimeSeriesChart;
import agentgui.ontology.TimeSeriesChartSettings;
import hygrid.globalDataModel.ontology.Cable;
import hygrid.globalDataModel.ontology.CableWithBreaker;
import hygrid.globalDataModel.ontology.CableWithBreakerProperties;
import hygrid.globalDataModel.ontology.HyGridOntology;
import hygrid.globalDataModel.ontology.TriPhaseCableState;

public class TriPhaseBreakerStorageHandler extends DataModelStorageHandlerOntology {
	
	private static final String newOntoXmlStringStart = "<" + CableWithBreakerProperties.class.getSimpleName();
	private static final String oldOntoClassReference = Cable.class.getName();
	
	private boolean debug = false;

	public TriPhaseBreakerStorageHandler(NetworkComponentAdapter4Ontology ontologyAdapter, String partModelID) {
		super(ontologyAdapter, partModelID);
	}

	@Override
	protected Object[] createInstancesFromOntologyXmlVector(Vector<String> ontologyXmlVector) {
		
		String firstXmlString = ontologyXmlVector.get(0);
		if (firstXmlString!=null && firstXmlString.startsWith(newOntoXmlStringStart)==false) {
			Object[] instances = new Object[this.ontologyAdapter.getOntologyClassReferences().length];
			
			if (this.debug==true) {
				System.out.println("[" + this.getClass().getSimpleName() + "] Data model for component " + this.ontologyAdapter.getNetworkComponent().getId() + " contains a CableWithBreaker instance, converting to CableWithBreakerProperties and CableState");
			}
			
			// --- Old style data model -> convert ----------------------------
			CableWithBreaker oldCableWithBreaker = (CableWithBreaker) this.getInstanceOfXML(firstXmlString, oldOntoClassReference, HyGridOntology.getInstance());
			CableWithBreakerProperties newCableWithBreaker = this.getBreakerPropertiesFromBreaker(oldCableWithBreaker);
			instances[0] = newCableWithBreaker;
			
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
	 * Converts a {@link CableWithBreaker} instance to a {@link CableWithBreakerProperties} instance.
	 *
	 * @param breaker the cable
	 * @return the cable properties
	 */
	private CableWithBreakerProperties getBreakerPropertiesFromBreaker(CableWithBreaker breaker) {
		CableWithBreakerProperties breakerProperties = new CableWithBreakerProperties();
		breakerProperties.setDim(breaker.getDim());
		breakerProperties.setDin(breaker.getDin());
		breakerProperties.setLength(breaker.getLength());
		breakerProperties.setLinearCapacitance(breaker.getLinearCapacitance());
		breakerProperties.setLinearConductance(breaker.getLinearConductance());
		breakerProperties.setLinearReactance(breaker.getLinearReactance());
		breakerProperties.setLinearResistance(breaker.getLinearResistance());
		breakerProperties.setMaxCurrent(breaker.getMaxCurrent());
		breakerProperties.setBreakerBegin(breaker.getBreakerBegin());
		breakerProperties.setBreakerEnd(breaker.getBreakerEnd());
		return breakerProperties;
	}

}
