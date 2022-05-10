package de.enflexit.ea.electricity.sensor;

import de.enflexit.common.Observable;
import de.enflexit.common.Observer;
import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.AbstractInternalDataModel;
import de.enflexit.ea.core.dataModel.ontology.ElectricalMeasurement;
import de.enflexit.ea.core.dataModel.ontology.HyGridOntology;
import de.enflexit.ea.core.monitoring.MonitoringBehaviourRT;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.lang.acl.ACLMessage;

// TODO: Auto-generated Javadoc
/**
 * Represents a SensorAgent that measures current and voltage.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public abstract class AbstractSensorAgent extends AbstractEnergyAgent implements Observer {

	private static final long serialVersionUID = -5336718073413159211L;

	private boolean debug = false;
	
	
	/**
	 * Gets the internal data model.
	 *
	 * @return the internal data model
	 */
	public abstract InternalDataModel getInternalDataModel();
	
	/**
	 * Checking measurement delta.
	 *
	 * @return true, if successful
	 */
	protected abstract boolean checkingMeasurementDelta();
	
	/**
	 * Creates the electrical measurement.
	 *
	 * @return the electrical measurement
	 */
	protected abstract ElectricalMeasurement createElectricalMeasurement();
	
	/**
	 * Gets the sensor sending behaviour.
	 *
	 * @return the sensor sending behaviour
	 */
	protected abstract SensorSendingBehaviour getSensorSendingBehaviour();
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.enflexit.energyAgent.core.AbstractEnergyAgent#onObserverUpdate(java.util.Observable,
	 * java.lang.Object)
	 */
	@Override
	public void onObserverUpdate(Observable observable, Object updateObject) {
		// --- Will be invoked if the internal data model has changed ---------
		
		if (observable instanceof InternalDataModel) {
			
			if (updateObject == AbstractInternalDataModel.CHANGED.NETWORK_MODEL) {
				// System.out.println(this.getLocalName() + " NetworkModel was set!");
			} else if (updateObject == AbstractInternalDataModel.CHANGED.NETWORK_COMPONENT) {
				// System.out.println(this.getLocalName() + " NetworkComponent was set!");
			} else if (updateObject == AbstractInternalDataModel.CHANGED.MEASUREMENTS_FROM_SYSTEM) {

				// Checking if new measurement has changed significantly
				boolean newMeasurementDelta = checkingMeasurementDelta();

				MeasurementSubscriptionResponder a = this.getInternalDataModel().getMeasurementSubscriptionResponder(this);
				ACLMessage measurementMessage;
				if (newMeasurementDelta == true) {
					if (debug == true) {
						System.out.println("=> Sensor " + this.getLocalName() + ":New measurement sent!");
					}
					try {
						measurementMessage = this.createMeasurementMessage();
						if (measurementMessage!=null) {
							a.sendingNewMeasurementToSubscribers(measurementMessage);
						}
					} catch (CodecException e) {
						e.printStackTrace();
					} catch (OntologyException e) {
						e.printStackTrace();
					}

				}
			}
		}
	}
	
	/**
	 * This method calculates the deviation of two values in percent
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	protected float calculateDeviationInPercent(float a, float b) {
		float deviation = ((Math.abs(a) - Math.abs(b)) / Math.abs(a)) * 100;

		return deviation;
	}

	/**
	 * This method create a measurement message
	 * 
	 * @return
	 * @throws CodecException
	 * @throws OntologyException
	 */
	private ACLMessage createMeasurementMessage() throws CodecException, OntologyException {
		ACLMessage measurementMessage = null;
		ElectricalMeasurement powerSensorData = this.createElectricalMeasurement();
		if (powerSensorData!=null) {
			// --- Creating ACL Message from powerSensorData
			measurementMessage = new ACLMessage(ACLMessage.INFORM);
			measurementMessage.setLanguage(new SLCodec().getName());
			measurementMessage.setOntology(HyGridOntology.getInstance().getName());
			Action content = new Action(this.getAID(), powerSensorData);
			getContentManager().fillContent(measurementMessage, content);
		}

		return measurementMessage;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.AbstractEnergyAgent#getMonitoringBehaviourRT()
	 */
	@Override
	public MonitoringBehaviourRT getMonitoringBehaviourRT() {
		if (this.monitoringBehaviourRT==null) {
			this.monitoringBehaviourRT = new SensorMonitoringBehaviourRT(this);
		}
		return this.monitoringBehaviourRT;
	}

//	@Override
//	protected void startMonitoringBehaviourRT() {
//	}

//	@Override
//	protected void startSystemStateLogWriter() {
//		this.startMonitoringBehaviourRT();
//	}
	
	

	
}
