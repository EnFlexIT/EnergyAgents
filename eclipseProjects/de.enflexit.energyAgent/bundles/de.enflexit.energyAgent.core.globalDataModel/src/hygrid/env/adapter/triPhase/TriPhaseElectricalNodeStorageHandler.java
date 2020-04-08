package hygrid.env.adapter.triPhase;

import java.util.Vector;

import org.awb.env.networkModel.adapter.NetworkComponentAdapter4Ontology;
import org.awb.env.networkModel.adapter.dataModel.DataModelStorageHandlerOntology;

import agentgui.core.application.Application;
import agentgui.ontology.TimeSeriesChart;
import agentgui.ontology.TimeSeriesChartSettings;
import hygrid.globalDataModel.ontology.ElectricalNodeProperties;
import hygrid.globalDataModel.ontology.TransformerNodeProperties;
import hygrid.globalDataModel.ontology.TriPhaseElectricalNodeState;
import hygrid.globalDataModel.ontology.UnitValue;

/**
 * StorageHandler for tri-phase electrical nodes, implementing data model conversion for regular nodes and transformer nodes.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class TriPhaseElectricalNodeStorageHandler extends DataModelStorageHandlerOntology {
	
	private static final String STATE_CLASS_STRING_NODE = "<TriPhaseElectricalNodeState";
	private static final String STATE_CLASS_STRING_TRANSFORMER = "<TriPhaseElectricalTransformerState";

	private boolean debug = false;
	
	public TriPhaseElectricalNodeStorageHandler(NetworkComponentAdapter4Ontology ontologyAdapter, String partModelID) {
		super(ontologyAdapter, partModelID);
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.dataModel.DataModelStorageHandlerOntology#createInstancesFromOntologyXmlVector(java.util.Vector)
	 */
	@Override
	protected Object[] createInstancesFromOntologyXmlVector(Vector<String> ontologyXmlVector) {
		String firstXmlString = ontologyXmlVector.get(0);
		
		if (firstXmlString!=null && (firstXmlString.startsWith(STATE_CLASS_STRING_NODE) || firstXmlString.startsWith(STATE_CLASS_STRING_TRANSFORMER))) {
			
			Object[] instances = new Object[3];
			
			// --- Old state only data model, convert to separate properties and state objects
			if (this.debug==true) {
				System.out.println("[" + this.getClass().getSimpleName() + "] Data model for graph node  " + this.ontologyAdapter.getGraphNode().getId() + " contains state only data model, converting to separate properties and state objects");
			}
			
			instances[0] = this.getNodePropertiesFromXmlString(firstXmlString);
			instances[1] = new TriPhaseElectricalNodeState();
			
			TimeSeriesChart tsc = new TimeSeriesChart();
			tsc.setTimeSeriesVisualisationSettings(new TimeSeriesChartSettings());
			instances[2] = tsc;
			
			this.ontologyAdapter.getGraphNode().setDataModelBase64(this.getXML64FromInstances(instances));
			Application.getProjectFocused().setUnsaved(true);
			return instances;
			
		} else {
			return super.createInstancesFromOntologyXmlVector(ontologyXmlVector);
		}
	}
	
	/**
	 * Gets the node properties from an xml string.
	 * @param xmlString the xml string
	 * @return the node properties
	 */
	private ElectricalNodeProperties getNodePropertiesFromXmlString(String xmlString) {
		ElectricalNodeProperties nodeProperties = null;
		
		if (xmlString.startsWith(STATE_CLASS_STRING_NODE)) {
			nodeProperties = new ElectricalNodeProperties();
		} else if (xmlString.startsWith(STATE_CLASS_STRING_TRANSFORMER)) {
			nodeProperties = new TransformerNodeProperties();
		}
		
		// --- Extract the attributes from the opening tag ---------- 
		String openingTag = xmlString.substring(0, xmlString.indexOf('>'));
		String[] parts = openingTag.split(" ");
		
		// --- Find and parse the attributes of interest ------------
		for (int i=1; i<parts.length; i++) {
			String attrString = parts[i];
			String attrValue = attrString.substring(attrString.indexOf("\"")+1, attrString.lastIndexOf("\""));
			if (attrString.startsWith("nominalPower")) {
				float nominalPower = Float.parseFloat(attrValue);
				nodeProperties.setNominalPower(new UnitValue(nominalPower, "W"));
			} else if (attrString.startsWith("isLoadNode")) {
				boolean isLoadNode = Boolean.parseBoolean(attrValue);
				nodeProperties.setIsLoadNode(isLoadNode);
			}
		}
		return nodeProperties;
	}

}
