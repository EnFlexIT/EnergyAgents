package de.enflexit.energyAgent.core.aggregation.trafficLight;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import org.awb.env.networkModel.controller.ui.messaging.GraphUIStateMessage;
import org.awb.env.networkModel.controller.ui.messaging.GraphUIStateMessagePanel;

/**
 * The Class TrafficLightStatePanel defines the visual representation
 * for the HyGrid state indication during runtime.
 * 
 * @author Christian Derksen - DAWIS - ICB University of Duisburg - Essen
 */
public class TrafficLightStatePanel extends GraphUIStateMessagePanel {

	private static final long serialVersionUID = 7397421670376943942L;

	public static final String TRAFFIC_LIGHT_NETWORK_STATE = "Network State";
	public static final String TRAFFIC_LIGHT_VOLTAGE_LEVEL = "Voltage Level";
	public static final String TRAFFIC_LIGHT_CABLE_STATE = "Cable State";
	
	private TrafficLight trafficLightNetworkState;
	private TrafficLight trafficLightVoltageLevel;
	private TrafficLight trafficLightCableState;
	
	/**
	 * Instantiates a new traffic light state panel.
	 */
	public TrafficLightStatePanel() {
		this.initialize();
	}
	/** Initialize. */
	private void initialize() {
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{60, 60, 60};
		gridBagLayout.columnWeights = new double[]{0.0};
		gridBagLayout.rowWeights = new double[]{0.0};
		this.setLayout(gridBagLayout);
		
		GridBagConstraints gbc_trafficLightNetworkState = new GridBagConstraints();
		gbc_trafficLightNetworkState.anchor = GridBagConstraints.CENTER;
		gbc_trafficLightNetworkState.gridx = 0;
		gbc_trafficLightNetworkState.gridy = 0;
		this.add(this.getTrafficLightNetworkState(), gbc_trafficLightNetworkState);
		
		GridBagConstraints gbc_trafficLightVoltageLevel = new GridBagConstraints();
		gbc_trafficLightVoltageLevel.anchor = GridBagConstraints.CENTER;
		gbc_trafficLightVoltageLevel.gridx = 1;
		gbc_trafficLightVoltageLevel.gridy = 0;
		this.add(this.getTrafficLightVoltageLevel(), gbc_trafficLightVoltageLevel);

		GridBagConstraints gbc_trafficCableState = new GridBagConstraints();
		gbc_trafficCableState.anchor = GridBagConstraints.CENTER;
		gbc_trafficCableState.gridx = 2;
		gbc_trafficCableState.gridy = 0;
		this.add(this.getTrafficLightCableState(), gbc_trafficCableState);
		
		this.setMinimumSize(new Dimension(180, 100));
		
	}
	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.controller.ui.messaging.GraphUIStateMessagePanel#addMessage(org.awb.env.networkModel.controller.ui.messaging.GraphUIStateMessage)
	 */
	@Override
	public void addMessage(GraphUIStateMessage stateMessage) {
		
		if (stateMessage!=null && stateMessage instanceof TrafficLightStateMessage) {
			// --- Set the state to the visualization -----
			TrafficLightStateMessage tlsm = (TrafficLightStateMessage) stateMessage;
			if (tlsm.getTrafficLightName().equals(TRAFFIC_LIGHT_NETWORK_STATE)) {
				this.getTrafficLightNetworkState().setTrafficLightColor(tlsm.getTrafficLightColor());
			} else if (tlsm.getTrafficLightName().equals(TRAFFIC_LIGHT_VOLTAGE_LEVEL)) {
				this.getTrafficLightVoltageLevel().setTrafficLightColor(tlsm.getTrafficLightColor());
			} else if (tlsm.getTrafficLightName().equals(TRAFFIC_LIGHT_CABLE_STATE)) {
				this.getTrafficLightCableState().setTrafficLightColor(tlsm.getTrafficLightColor());
			}
		}
	}

	/**
	 * Gets the traffic light network state.
	 * @return the traffic light network state
	 */
	private TrafficLight getTrafficLightNetworkState() {
		if (trafficLightNetworkState == null) {
			trafficLightNetworkState = new TrafficLight(TRAFFIC_LIGHT_NETWORK_STATE);
		}
		return trafficLightNetworkState;
	}
	/**
	 * Gets the traffic light voltage state.
	 * @return the traffic light network state
	 */
	private TrafficLight getTrafficLightVoltageLevel() {
		if (trafficLightVoltageLevel == null) {
			trafficLightVoltageLevel = new TrafficLight(TRAFFIC_LIGHT_VOLTAGE_LEVEL);
		}
		return trafficLightVoltageLevel;
	}
	/**
	 * Gets the traffic light voltage state.
	 * @return the traffic light network state
	 */
	private TrafficLight getTrafficLightCableState() {
		if (trafficLightCableState == null) {
			trafficLightCableState = new TrafficLight(TRAFFIC_LIGHT_CABLE_STATE);
		}
		return trafficLightCableState;
	}
	
}
