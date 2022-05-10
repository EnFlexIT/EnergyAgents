package de.enflexit.ea.electricity.sensor.lvSensor;

import java.util.List;

import de.enflexit.ea.core.AbstractEnergyAgent;
import hygrid.modbusTCP.ModbusRegistryEntry;
import jade.core.Agent;

// TODO: Auto-generated Javadoc
/**
 * The Class InternalDataModel represents the whole internal data model of the corresponding agent.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class InternalDataModel extends de.enflexit.ea.electricity.sensor.InternalDataModel {

	private static final long serialVersionUID = 8589606262871989270L;
	
	/** Specification of the measurement names */
	protected final static String VOLTAGE_L1 = "Voltage L1";
	protected final static String VOLTAGE_L2 = "Voltage L2";
	protected final static String VOLTAGE_L3 = "Voltage L3";
	
	protected final static String CURRENT_L1 = "Current L1";
	protected final static String CURRENT_L2 = "Current L2";
	protected final static String CURRENT_L3 = "Current L3";
	
	protected final static String COSPHI_L1 = "Cos Phi L1";
	protected final static String COSPHI_L2 = "Cos Phi L2";
	protected final static String COSPHI_L3 = "Cos Phi L3";
	
	public static final String V1= "V1";
	public static final String V2= "V2";
	public static final String V3= "V3";
	
	public static final String C1= "C1";
	public static final String C2= "C2";
	public static final String C3= "C3";
	
	public static final String PF1= "PF123";
	public static final String PF2= "PF123";
	public static final String PF3= "PF123";
	
	private List<ModbusRegistryEntry> modbusRegistryList; 
	private Float[] receivedRegisters;
	
	/**
	 * Instantiates a new data model.
	 * @param myAgent the my agent
	 */
	public InternalDataModel(AbstractEnergyAgent myAgent) {
		super(myAgent);
	}
	
	/**
	 * Gets the modbus registry list.
	 * @return the modbus registry list
	 */
	public List<ModbusRegistryEntry> getModbusRegistryList() {
		return modbusRegistryList;
	}
	
	/**
	 * Sets the modbus registry list.
	 * @param modbusRegistryList the new modbus registry list
	 */
	public void setModbusRegistryList(List<ModbusRegistryEntry> modbusRegistryList) {
		this.modbusRegistryList = modbusRegistryList;
	}
	
	/**
	 * Gets the received registers.
	 * @return the received registers
	 */
	public Float[] getReceivedRegisters() {
		return receivedRegisters;
	}
	
	/**
	 * Sets the received registers.
	 * @param receivedRegisters the new received registers
	 */
	public void setReceivedRegisters(Float[] receivedRegisters) {
		this.receivedRegisters = receivedRegisters;
	}
	
	/**
	 * Gets the measurement subscription responder.
	 * @param myAgent the agent
	 * @return the measurement subscription responder
	 */
	public MeasurementSubscriptionResponder getMeasurementSubscriptionResponder(Agent myAgent) {
		if (measurementSubscriptionResponder == null) {
			measurementSubscriptionResponder = new MeasurementSubscriptionResponder(myAgent);
		}
		return (MeasurementSubscriptionResponder) measurementSubscriptionResponder;
	}
	
}
