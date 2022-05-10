package de.enflexit.ea.electricity.sensor.lvSensor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.enflexit.ea.core.EnergyAgentIO;
import energy.FixedVariableList;
import energy.optionModel.FixedDouble;
import hygrid.modbusTCP.ModbusCommunicationInterface;
import hygrid.modbusTCP.ModbusCommunicationListener;
import hygrid.modbusTCP.ModbusRegistryEntry;

/**
 * The Class IOReal can be used for real measurements on physical hardware.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class IOReal extends de.enflexit.ea.electricity.sensor.IOReal implements EnergyAgentIO, ModbusCommunicationListener {

	private static final long serialVersionUID = 3659353219575016108L;
	
//	private static final long CYCLE_TIME_MS = 5000;
//	private static final long CYCLE_TIME_MS = 60000;
	
	private boolean debug = false;
	
	/**
	 * Instantiates this behaviour.
	 * @param agent  the agent
	 */
	public IOReal(LVSensorAgent agent) {
		super(agent);
	}

	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#onStart()
	 */
	@Override
	public void onStart() {
		ModbusCommunicationInterface.getInstance().addModbusCommunicationListener(this);
	}

	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#onEnd()
	 */
	@Override
	public int onEnd() {
		ModbusCommunicationInterface.getInstance().removeModbusCommunicationListener(this);
		return super.onEnd();
	}
	
	/**
	 * Cyclic behaviour to read modbus register 
	 */
	@Override
	public void action() {


		// --- Get identifier list ----------------------
		List<ModbusRegistryEntry> modbusRegistryList = ModbusCommunicationInterface.getInstance().getModbusRegistryList();

		if (this.newRegisterValues!=null && modbusRegistryList!=null) {

			FixedVariableList fvList = new FixedVariableList();

			// --- Checking if size of list is equal to received registers
//			if (modbusRegistryList.size() == this.newMeasurement.length) {

				for (int i = 0; i < modbusRegistryList.size(); i++) {
					String name = modbusRegistryList.get(i).getName();
					int separatorLine = name.indexOf("_");
					 if (separatorLine>2) {
						 
						// --- Get Sensor Id ---------------------------------
						String sensorID = name.substring(2, separatorLine);
	
						// --- Find right index
						if (sensorID.equals(this.getSensorID())) {
	
							// --- Get measurment Type ---------------------------
							String measurementType = name.substring(separatorLine + 1, name.length());
	
							// --- Write value in fv list ------------------------
							if (measurementType.equals(InternalDataModel.V1)) {
								FixedDouble fd = new FixedDouble();
								fd.setVariableID(InternalDataModel.VOLTAGE_L1);
								fd.setValue(this.newRegisterValues[i]);
								fvList.add(fd);
	
							} else if (measurementType.equals(InternalDataModel.V2)) {
								FixedDouble fd = new FixedDouble();
								fd.setVariableID(InternalDataModel.VOLTAGE_L2);
								fd.setValue(this.newRegisterValues[i]);
								fvList.add(fd);
	
							} else if (measurementType.equals(InternalDataModel.V3)) {
								FixedDouble fd = new FixedDouble();
								fd.setVariableID(InternalDataModel.VOLTAGE_L3);
								fd.setValue(this.newRegisterValues[i]);
								fvList.add(fd);
	
							} else if (measurementType.equals(InternalDataModel.C1)) {
								FixedDouble fd = new FixedDouble();
								fd.setVariableID(InternalDataModel.CURRENT_L1);
								fd.setValue(this.newRegisterValues[i]);
								fvList.add(fd);
	
							} else if (measurementType.equals(InternalDataModel.C2)) {
								FixedDouble fd = new FixedDouble();
								fd.setVariableID(InternalDataModel.CURRENT_L2);
								fd.setValue(this.newRegisterValues[i]);
								fvList.add(fd);
	
							} else if (measurementType.equals(InternalDataModel.C3)) {
								FixedDouble fd = new FixedDouble();
								fd.setVariableID(InternalDataModel.CURRENT_L3);
								fd.setValue(this.newRegisterValues[i]);
								fvList.add(fd);
	
							} else if (measurementType.equals(InternalDataModel.PF1)){
								FixedDouble fd1 = new FixedDouble();
								fd1.setVariableID(InternalDataModel.COSPHI_L1);
								fd1.setValue(this.newRegisterValues[i]);
								fvList.add(fd1);
	
								FixedDouble fd2 = new FixedDouble();
								fd2.setVariableID(InternalDataModel.COSPHI_L2);
								fd2.setValue(this.newRegisterValues[i]);
								fvList.add(fd2);
	
								FixedDouble fd3 = new FixedDouble();
								fd3.setVariableID(InternalDataModel.COSPHI_L3);
								fd3.setValue(this.newRegisterValues[i]);
								fvList.add(fd3);
	
							}
							
						}
					} else {
						// Register from Control Setpoints handling
//						System.err.println("Sensor Agent: " + this.myAgent.getName() + " Invalid modbus data received: " + name);
					}
				}
				if (debug==true) {
					SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
					System.out.println(myAgent.getLocalName() + " received " + fvList.size() + " modbus registers at " + sdf.format(new Date(System.currentTimeMillis())));
//					System.out.println(TechnicalSystemStateHelper.toString(fvList));
				}
				this.setMeasurementsFromSystem(fvList);
				this.newRegisterValues = null;
//			} else {
//				System.err.println("Sensor Agent: " + this.myAgent.getName() + " Error in reading: Index exceeds");
//			}
		
//		} else {
//			System.err.println("Sensor Agent: " + this.myAgent.getName() + " No modbus register received");
		}
		
//		this.block(CYCLE_TIME_MS);
		this.block();
	}
	
	/* (non-Javadoc)
	 * @see hygrid.modbusTCP.ModbusCommunicationListener#receivedNewRegisterValues(java.lang.Float[])
	 */
	@Override
	public void receivedNewRegisterValues(Float[] receivedRegisters) {
		this.newRegisterValues = receivedRegisters;
		this.restart();
	}

}
