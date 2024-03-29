package de.enflexit.ea.electricity.aggregation;

import java.util.TreeMap;
import java.util.Vector;

import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.helper.DomainCluster;
import org.awb.env.networkModel.settings.ComponentTypeSettings;

import de.enflexit.ea.core.aggregation.AbstractAggregationHandler;
import de.enflexit.ea.core.awbIntegration.adapter.EnergyAgentAdapter;
import de.enflexit.ea.core.awbIntegration.adapter.triPhase.TriPhaseSensorAdapter;
import de.enflexit.ea.core.awbIntegration.adapter.uniPhase.UniPhaseSensorAdapter;
import de.enflexit.ea.core.dataModel.TransformerHelper;
import de.enflexit.ea.core.dataModel.ontology.SlackNodeState;
import de.enflexit.ea.core.dataModel.ontology.TransformerNodeProperties;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseSlackNodeState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseSlackNodeState;
import de.enflexit.ea.core.dataModel.ontology.UnitValue;
import de.enflexit.ea.electricity.NetworkModelToCsvMapper.SlackNodeDescription;
import de.enflexit.ea.electricity.transformer.TransformerDataModel.TransformerSystemVariable;
import de.enflexit.eom.awb.adapter.EomAdapter;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.optionModel.FixedDouble;
import energy.optionModel.FixedVariable;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class SlackNodeHandler concludes all methods for the Handling of the 
 * voltage level informations for electrical slack nodes that are the base 
 * for electrical network calculations.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public abstract class AbstractSlackNodeHandler {

	private AbstractElectricalNetworkCalculationStrategy electricalNetworkCalculationStrategy;

	private Float initialSlackNodeVoltage;
	
	private boolean changedSlackNodeState;
	
	private NetworkComponent networkComponentTransformer;
	private NetworkComponent networkComponentSlackNodeSensor;
	
	/**
	 * Instantiates a new abstract slack node handler.
	 * @param electricalNetworkCalculationStrategy the electrical network calculation strategy
	 */
	public AbstractSlackNodeHandler(AbstractElectricalNetworkCalculationStrategy electricalNetworkCalculationStrategy) {
		this.electricalNetworkCalculationStrategy = electricalNetworkCalculationStrategy;
		this.getInitialVoltageLevel();
	}
	
	// ------------------------------------------------------------------------
	// --- From here access to the aggregation --------------------------------
	// ------------------------------------------------------------------------
	/**
	 * Return the current AggregationHandler if the electrical network calculation strategy is known (see constructor).
	 * @return the aggregation handler
	 */
	public AbstractAggregationHandler getAggregationHandler() {
		if (this.electricalNetworkCalculationStrategy!=null) {
			return this.electricalNetworkCalculationStrategy.getAggregationHandler();
		}
		return null;
	}
	/**
	 * Returns the current sub network configuration.
	 * @return the sub network configuration
	 */
	public AbstractElectricalNetworkConfiguration getElectricalNetworkConfiguration() {
		if (this.electricalNetworkCalculationStrategy!=null) {
			return (AbstractElectricalNetworkConfiguration) this.electricalNetworkCalculationStrategy.getSubNetworkConfiguration();
		}
		return null;
	}
	
	/**
	 * Return the current DomainCluster.
	 * @return the domain cluster
	 */
	public DomainCluster getDomainCluster() {
		if (this.getElectricalNetworkConfiguration()!=null) {
			return this.getElectricalNetworkConfiguration().getDomainCluster();
		}
		return null;
	}
	/**
	 * Returns the current electrical network calculation strategy.
	 * @return the electrical network calculation strategy
	 */
	public AbstractElectricalNetworkCalculationStrategy getElectricalNetworkCalculationStrategy() {
		return electricalNetworkCalculationStrategy;
	}
	
	// ------------------------------------------------------------------------
	// --- From here two static factory methods -------------------------------
	// ------------------------------------------------------------------------
	/**
	 * Creates an UniPhaseSlackNodeState with 0 as start value for the real parts.
	 * @return the uni phase slack node state
	 */
	public static UniPhaseSlackNodeState createUniPhaseSlackNodeState() {
		return createUniPhaseSlackNodeState(0);
	}
	/**
	 * Creates an UniPhaseSlackNodeState with the specified start value for the real part.
	 *
	 * @param errorIndicatingRealStartValue an error indicating start value (e.g. -1)
	 * @return the uni phase slack node state
	 */
	public static UniPhaseSlackNodeState createUniPhaseSlackNodeState(float errorIndicatingRealStartValue) {
		UniPhaseSlackNodeState upsns = new UniPhaseSlackNodeState();
		upsns.setVoltageReal(new UnitValue(errorIndicatingRealStartValue, "V"));
		upsns.setVoltageImag(new UnitValue(0, "V"));
		return upsns;
	}
	/**
	 * Checks if there is an error in the specified UniPhaseSlackNodeState, where the specified error indicating value
	 * serves as comparator for the voltage real parts.
	 *
	 * @param snsToCheck the UniPhaseSlackNodeState to check
	 * @param errorIndicatingRealStartValue the error indicating voltage real start value
	 * @return true, if is error in uni phase slack node state
	 */
	public static boolean isErrorInUniPhaseSlackNodeState(UniPhaseSlackNodeState snsToCheck, float errorIndicatingRealStartValue) {
		if (snsToCheck==null || snsToCheck.getVoltageReal()==null) return true;
		return snsToCheck.getVoltageReal().getValue()==errorIndicatingRealStartValue;
	}

	/**
	 * Return a UniPhaseSlackNodeState from the specified TechnicalSystemStateEvaluation.
	 *
	 * @param tsse the TechnicalSystemStateEvaluation
	 * @return the UniPhaseSlackNodeState from technical system state evaluation
	 */
	public static UniPhaseSlackNodeState getUniPhaseSlackNodeStateFromTechnicalSystemStateEvaluation(TechnicalSystemStateEvaluation tsse) {
		return getUniPhaseSlackNodeStateFromTechnicalSystemStateEvaluation(tsse, null);
	}
	
	/**
	 * Return a UniPhaseSlackNodeState from the specified TechnicalSystemStateEvaluation.
	 *
	 * @param tsse the TechnicalSystemStateEvaluation
	 * @param targetVoltageLevelReal the target voltage level real
	 * @return the UniPhaseSlackNodeState from technical system state evaluation
	 */
	public static UniPhaseSlackNodeState getUniPhaseSlackNodeStateFromTechnicalSystemStateEvaluation(TechnicalSystemStateEvaluation tsse, Double targetVoltageLevelReal) {
		
		float lvVoltageRealAllPhases = 0.0f;
		float lvVoltageImaglAllPhases = 0.0f;
		
		float hvVoltageRealAllPhases = 0.0f;
		float hvVoltageImaglAllPhases = 0.0f;
		
		// --- Collect low and high voltage levels from current state --------- 
		for (int i = 0; i < tsse.getIOlist().size(); i++) {
			// --- Check IO-value ---------------------------------------------
			FixedVariable fv = tsse.getIOlist().get(i);
			if (fv instanceof FixedDouble) {
				// --- Get the float value ------------------------------------
				float value = (float) ((FixedDouble) fv).getValue();
				if (fv.getVariableID().equals(TransformerSystemVariable.lvVoltageRealAllPhases.name())) {
					lvVoltageRealAllPhases = value;
				} else if (fv.getVariableID().equals(TransformerSystemVariable.lvVoltageImagAllPhases.name())) {
					lvVoltageImaglAllPhases = value;
				} else if (fv.getVariableID().equals(TransformerSystemVariable.hvVoltageRealAllPhases.name())) {
					hvVoltageRealAllPhases = value;
				} else if (fv.getVariableID().equals(TransformerSystemVariable.hvVoltageImagAllPhases.name())) {
					hvVoltageImaglAllPhases = value;
				}
			}
		}

		// --- Low or high voltage level? -------------------------------------
		float voltageRealAllPhases = lvVoltageRealAllPhases;	// -- Default - 
		float voltageImagAllPhases = lvVoltageImaglAllPhases;	// -- Default -
		if (targetVoltageLevelReal!=null) {
			// --- Determine if low or the high voltage needs to be used ------
			double lvRealTargetDiff = Math.abs((lvVoltageRealAllPhases - targetVoltageLevelReal) / targetVoltageLevelReal);
			double hvRealTargetDiff = Math.abs((hvVoltageRealAllPhases - targetVoltageLevelReal) / targetVoltageLevelReal);
			if (hvRealTargetDiff < lvRealTargetDiff) {
				voltageRealAllPhases = hvVoltageRealAllPhases;	// -- Default - 
				voltageImagAllPhases = hvVoltageImaglAllPhases;
			}
		}
		
		// --- Check for invalid slack node state -----------------------------
		float errorIndicatingValue = 0;
		UniPhaseSlackNodeState upSns = createUniPhaseSlackNodeState(errorIndicatingValue);
		upSns.getVoltageReal().setValue((float) voltageRealAllPhases);
		upSns.getVoltageImag().setValue((float) voltageImagAllPhases);

		if (isErrorInUniPhaseSlackNodeState(upSns, errorIndicatingValue)==true) {
			upSns = null;
		}
		return upSns;
	}
	
	
	/**
	 * Creates the TriPhaseSlackNodeState with 0 as start value for the real parts..
	 * @return the tri phase slack node state
	 */
	public static TriPhaseSlackNodeState createTriPhaseSlackNodeState() {
		return createTriPhaseSlackNodeState(0);
	}
	/**
	 * Creates the TriPhaseSlackNodeState with the specified start value for the real parts.
	 *
	 * @param errorIndicatingRealStartValue an error indicating start value (e.g. -1)
	 * @return the tri phase slack node state
	 */
	public static TriPhaseSlackNodeState createTriPhaseSlackNodeState(float errorIndicatingRealStartValue) {
		TriPhaseSlackNodeState tpsns = new TriPhaseSlackNodeState();
		tpsns.setSlackNodeStateL1(createUniPhaseSlackNodeState(errorIndicatingRealStartValue));
		tpsns.setSlackNodeStateL2(createUniPhaseSlackNodeState(errorIndicatingRealStartValue));
		tpsns.setSlackNodeStateL3(createUniPhaseSlackNodeState(errorIndicatingRealStartValue));
		return tpsns;
	}
	/**
	 * Checks if there is an error in the specified TriPhaseSlackNodeState, where the specified error indicating value
	 * serves as comparator for the voltage real parts.
	 *
	 * @param snsToCheck the UniPhaseSlackNodeState to check
	 * @param errorIndicatingRealStartValue the error indicating voltage real start value
	 * @return true, if is error in uni phase slack node state
	 */
	public static boolean isErrorInTriPhaseSlackNodeState(TriPhaseSlackNodeState snsToCheck, float errorIndicatingRealStartValue) {
		if (snsToCheck==null) return true;
		if (isErrorInUniPhaseSlackNodeState(snsToCheck.getSlackNodeStateL1(), errorIndicatingRealStartValue)==true) return true;
		if (isErrorInUniPhaseSlackNodeState(snsToCheck.getSlackNodeStateL2(), errorIndicatingRealStartValue)==true) return true;
		if (isErrorInUniPhaseSlackNodeState(snsToCheck.getSlackNodeStateL3(), errorIndicatingRealStartValue)==true) return true;
		return false;
	}
	/**
	 * Return a TriPhaseSlackNodeState from the specified TechnicalSystemStateEvaluation.
	 *
	 * @param tsse the TechnicalSystemStateEvaluation
	 * @return the TriPhaseSlackNodeState from technical system state evaluation
	 */
	public static TriPhaseSlackNodeState getTriPhaseSlackNodeStateFromTechnicalSystemStateEvaluation(TechnicalSystemStateEvaluation tsse) {
		
		float errorIndicatingValue = 0;
		TriPhaseSlackNodeState tpSns = createTriPhaseSlackNodeState(errorIndicatingValue);
		
		for (int i = 0; i < tsse.getIOlist().size(); i++) {
			// --- Check IO-value -----------------------------------
			FixedVariable fv = tsse.getIOlist().get(i);
			if (fv instanceof FixedDouble) {
				// --- Get the float value --------------------------
				float value = (float) ((FixedDouble) fv).getValue();

				if (fv.getVariableID().equals(TransformerSystemVariable.lvVoltageRealL1.name())) {
					tpSns.getSlackNodeStateL1().getVoltageReal().setValue(value);
				} else if (fv.getVariableID().equals(TransformerSystemVariable.lvVoltageImagL1.name())) {
					tpSns.getSlackNodeStateL1().getVoltageImag().setValue(value);

				} else if (fv.getVariableID().equals(TransformerSystemVariable.lvVoltageRealL2.name())) {
					tpSns.getSlackNodeStateL2().getVoltageReal().setValue(value);
				} else if (fv.getVariableID().equals(TransformerSystemVariable.lvVoltageImagL2.name())) {
					tpSns.getSlackNodeStateL2().getVoltageImag().setValue(value);

				} else if (fv.getVariableID().equals(TransformerSystemVariable.lvVoltageRealL3.name())) {
					tpSns.getSlackNodeStateL3().getVoltageReal().setValue(value);
				} else if (fv.getVariableID().equals(TransformerSystemVariable.lvVoltageImagL3.name())) {
					tpSns.getSlackNodeStateL3().getVoltageImag().setValue(value);
				}
			}
		}
		
		// --- Check for invalid slack node state -------------------
		if (isErrorInTriPhaseSlackNodeState(tpSns, errorIndicatingValue)) {
			tpSns = null;
		}
		return tpSns;
	}
	
	// ------------------------------------------------------------------------
	// --- From here method to find the initial slack node voltage ------------
	// ------------------------------------------------------------------------
	/**
	 * Has to return the initial (or default) voltage level for the current power flow calculation.
	 * @return the default voltage level
	 */
	public float getInitialVoltageLevel() {
		if (initialSlackNodeVoltage==null) {
			initialSlackNodeVoltage = this.getDefaultVoltageLevel();
			// --- Try to get the TransformerNodeProperties --------- 
			TransformerNodeProperties tnp = this.getTransformerNodeProperties();
			if (tnp!=null && tnp.getRatedVoltage()!=null && tnp.getRatedVoltage().getValue()>0) {
				// --- Nominate to single phase voltage level -------
				initialSlackNodeVoltage = (float) tnp.getRatedVoltage().getValue();
			}
			System.out.println("[" + this.getClass().getSimpleName() + "] Initial slack node voltage level: " + initialSlackNodeVoltage + " for " + this.getElectricalNetworkConfiguration().getSubNetworkDescriptionID());
		}
		return initialSlackNodeVoltage;
	}
	/**
	 * Returns the transformer node properties for the current slack node handler.
	 * @return the transformer node properties
	 */
	protected TransformerNodeProperties getTransformerNodeProperties() {
		
		TransformerNodeProperties transformerNodeProps = null;

		String domain = this.getDomainCluster().getDomain();
		GraphNode graphNode = this.getSlackGraphNode();
		if (graphNode!=null) {
			// --- Check the GraphNodes data model ------------------
			Object dataModel = graphNode.getDataModel();
			if (dataModel!=null) {
				// --- Check for a TreeMap data model ---------------
				if (dataModel instanceof TreeMap<?, ?>) {
					// --- Get the domain data model ----------------
					TreeMap<?, ?> treeMapDM = (TreeMap<?, ?>) dataModel;
					dataModel = treeMapDM.get(domain);
				}
				// --- If still not null ----------------------------
				if (dataModel!=null) {
					// -- Check object array of data model ----------
					if (dataModel.getClass().isArray()==true) {
						// --- Find TransformerNodeProperties -------
						Object[] dmArray = (Object[]) dataModel;
						for (int i = 0; i < dmArray.length; i++) {
							if (dmArray[i]!=null && dmArray[i] instanceof TransformerNodeProperties) {
								transformerNodeProps = (TransformerNodeProperties) dmArray[i];
								break;
							}
						}
						
					} else if (dataModel instanceof TransformerNodeProperties) {
						// --- Will most probably not happen --------
						transformerNodeProps = (TransformerNodeProperties) dataModel;
					}
				}
			} // end dataModel!=null
		} // end graphNode!=null
		return transformerNodeProps;
	}
	/**
	 * Returns the slack graph node.
	 * @return the slack graph node
	 */
	protected GraphNode getSlackGraphNode() {
		SlackNodeDescription snDesc = this.electricalNetworkCalculationStrategy.getNetworkModelToCsvMapper().getSlackNodeVector().get(0);
		NetworkModel networkModel = this.getAggregationHandler().getNetworkModel();
		NetworkComponent netComp = networkModel.getNetworkComponent(snDesc.getNetworkComponentID());
		return networkModel.getGraphNodeFromDistributionNode(netComp);
	}

	// ------------------------------------------------------------------------
	// --- From here the abstract methods -------------------------------------
	// ------------------------------------------------------------------------
	/**
	 * Has to return the default voltage level.
	 * @return the default voltage level
	 */
	public abstract float getDefaultVoltageLevel();
	
	/**
	 * Sets the slack node state.
	 * @param slackNodeState the new slack node state
	 */
	public abstract void setSlackNodeState(SlackNodeState slackNodeState);

	/**
	 * Has to return the current slack node state.
	 * @return the slack node state
	 */
	public abstract SlackNodeState getSlackNodeState();

	/**
	 * Has to return the slack node state for the specified {@link Phase}. (if available)
	 *
	 * @param phase the phase
	 * @return the slack node state
	 */
	public abstract UniPhaseSlackNodeState getSlackNodeState(Phase phase);
	

	// ------------------------------------------------------------------------
	// --- From here slack node voltage handling during runtime ---------------
	// ------------------------------------------------------------------------
	/**
	 * Answer the question, if the slack node state has changed after 
	 * the call of the method {@link #updateSlackNodeState()}.
	 * @return true, if the slack node state has changed 
	 * 
	 * @see #updateSlackNodeState()
	 */
	public boolean isChangedSlackNodeState() {
		return changedSlackNodeState;
	}
	/**
	 * Sets the changed state of the slack node state. Set the state to <code>true</code>, 
	 * if the individual {@link #setSlackNodeState(SlackNodeState)} is called.
	 *  
	 * @param isChanged the new changed slack node state
	 */
	protected void setChangedSlackNodeState(boolean isChanged) {
		this.changedSlackNodeState = isChanged;
	}
	
	
	/**
	 * Checks the current runtime configuration and will call the corresponding method to update the slack node state.
	 * 
	 * @see #updateSlackNodeStateFromLastTransformerSystemState()
	 * @see #updateSlackNodeStateFromSensorData()
	 */
	public final void updateSlackNodeState() {

		SlackNodeState newSlackNodeState = null;
		// --- Get the slack node state dependent on ExecutionDataBase ----------------------------
		switch (this.getAggregationHandler().getExecutionDataBase()) {
		case NodePowerFlows:
			NetworkComponent transformerNC = this.getNetworkComponentTransformer();
			if (transformerNC!=null) {
				// --- Try getting the state from the network calculation Schedule (index=1) ------
				TechnicalSystemStateEvaluation tsseLast = this.getAggregationHandler().getLastTechnicalSystemStateFromScheduleController(transformerNC.getId(), 1, true);
				if (tsseLast!=null) {
					newSlackNodeState = this.getSlackNodeStateFromLastTransformerState(tsseLast);
				} else {
					// --- Backup take from index==0 ---------------------------------------------
					tsseLast = this.getAggregationHandler().getLastTechnicalSystemStateFromScheduleController(transformerNC.getId(), 0, true); 
					if (tsseLast!=null) {
						newSlackNodeState = this.getSlackNodeStateFromLastTransformerState(tsseLast);
					}
				}
			}
			break;

		case SensorData:
			NetworkComponent sensorNC = this.getNetworkComponentSlackNodeSensor();
			if (sensorNC!=null) {
				TechnicalSystemStateEvaluation tsseLast = this.getAggregationHandler().getLastTechnicalSystemStateFromScheduleController(sensorNC.getId());
				if (tsseLast!=null) {
					newSlackNodeState = this.getSlackNodeStateFromLastSensorState(tsseLast);
				}
			}
			break;
		}
		
		// --- Reset the changed state ----------------------------------------
		this.setChangedSlackNodeState(false);
		
		// --- Is this a new slack node state ---------------------------------
		if (newSlackNodeState!=null) {
			// --- Compare with old state -------------------------------------
			SlackNodeState oldSlackNodeState = this.getSlackNodeState();
			
			boolean isSetNewSlackNodeState = false;
			// --- Check for different class type -----------------------------
			boolean isDifferentClass = oldSlackNodeState.getClass()!=newSlackNodeState.getClass();
			isSetNewSlackNodeState = isSetNewSlackNodeState || isDifferentClass;
			
			// --- Check for a different value --------------------------------
			if (isSetNewSlackNodeState==false) {
				if (oldSlackNodeState instanceof UniPhaseSlackNodeState) {
					// --- UniPhaseSlackNodeState -----------------------------
					UniPhaseSlackNodeState snStateNew = (UniPhaseSlackNodeState) newSlackNodeState;
					UniPhaseSlackNodeState snStateOld = (UniPhaseSlackNodeState) oldSlackNodeState;
					isSetNewSlackNodeState = this.isEqualUniPhaseSlackNodeState(snStateNew, snStateOld)==false;
					
				} else if (oldSlackNodeState instanceof TriPhaseSlackNodeState) {
					// --- TriPhaseSlackNodeState -----------------------------
					TriPhaseSlackNodeState snStateNew = (TriPhaseSlackNodeState) newSlackNodeState;
					TriPhaseSlackNodeState snStateOld = (TriPhaseSlackNodeState) oldSlackNodeState;
					
					isSetNewSlackNodeState = this.isEqualUniPhaseSlackNodeState(snStateNew.getSlackNodeStateL1(), snStateOld.getSlackNodeStateL1())==false;
					isSetNewSlackNodeState = isSetNewSlackNodeState || this.isEqualUniPhaseSlackNodeState(snStateNew.getSlackNodeStateL2(), snStateOld.getSlackNodeStateL2())==false;
					isSetNewSlackNodeState = isSetNewSlackNodeState || this.isEqualUniPhaseSlackNodeState(snStateNew.getSlackNodeStateL3(), snStateOld.getSlackNodeStateL3())==false;
				}
			}
			
			if (isSetNewSlackNodeState==true) {
				this.setSlackNodeState(newSlackNodeState);
				this.setChangedSlackNodeState(true);
			}
		}
	}
	/**
	 * Checks if both parameter are equal {@link UniPhaseSlackNodeState}s.
	 *
	 * @param snState1 the sn state 1
	 * @param snState2 the sn state 2
	 * @return true, if is equal uni phase slack node state
	 */
	private boolean isEqualUniPhaseSlackNodeState(UniPhaseSlackNodeState snState1, UniPhaseSlackNodeState snState2) {
		
		if (snState1==snState2) {
			return true;
		} else if (snState1==null && snState2!=null || snState1!=null && snState2==null) {
			return false;
		}
		return snState1.getVoltageReal()==snState2.getVoltageReal() && snState1.getVoltageImag()==snState2.getVoltageImag();
	}
	
	/**
	 * Has to update the current slack node state from the last transformer system state.
	 *
	 * @param tsseLast the last system state of the transformer found.
	 * @return the slack node state from last transformer system state
	 * @see #getNetworkComponentTransformer()
	 */
	public abstract SlackNodeState getSlackNodeStateFromLastTransformerState(TechnicalSystemStateEvaluation tsseLast);
	
	/**
	 * Update slack node state from sensor data. For this, the next sensor NetworkComponet
	 * will be determined and
	 *
	 * @param tsseLast the last system state of the sensor found.
	 * @return the slack node state from sensor data
	 * @see #getNetworkComponentSlackNodeSensor()
	 */
	public abstract SlackNodeState getSlackNodeStateFromLastSensorState(TechnicalSystemStateEvaluation tsseLast);

	
	/**
	 * Returns the network component transformer.
	 * @return the network component transformer
	 */
	public NetworkComponent getNetworkComponentTransformer() {
		if (networkComponentTransformer==null) {

			SlackNodeDescription snDesc = this.electricalNetworkCalculationStrategy.getNetworkModelToCsvMapper().getSlackNodeVector().get(0);
			
			NetworkModel networkModel = this.getAggregationHandler().getNetworkModel();
			DomainCluster dc = this.getDomainCluster();

			// --- Check the NetworkComponent in the domain cluster ----------- 
			for (int i = 0; i < dc.getNetworkComponents().size(); i++) {
				
				NetworkComponent netCompCheck = dc.getNetworkComponents().get(i);
				ComponentTypeSettings cts = networkModel.getGeneralGraphSettings4MAS().getCurrentCTS().get(netCompCheck.getType());
				// --- Check if transformer, agent and based on a EOM model ---
				if (TransformerHelper.isTransformer(netCompCheck)==true && cts.getAgentClass()!=null && cts.getAdapterClass()!=null && (cts.getAdapterClass().equals(EomAdapter.class.getName())==true || cts.getAdapterClass().equals(EnergyAgentAdapter.class.getName())==true)) {
					// --- Found a transformer with agent and EOM model -------
					if (netCompCheck.getId().equals(snDesc.getNetworkComponentID())==true) {
						networkComponentTransformer = netCompCheck;
						break;
					}
				}
			}
			
			if (networkComponentTransformer==null) {
				String domainDescription = this.getElectricalNetworkConfiguration().getDomain() + " (ID = " + this.getElectricalNetworkConfiguration().getID() + ")";
				System.out.println("[" + this.getClass().getSimpleName() + "] No active transformer could be found in the current sub network model of domain '" + domainDescription + "'!");
				System.out.println("[" + this.getClass().getSimpleName() + "] => A constant slack node voltage of " + this.getInitialVoltageLevel() + " V will be used for the power flow calculation.");
			}
		}
		return networkComponentTransformer;
	}
	
	/**
	 * Returns the NetworkComponent of the slack node sensor.
	 * @return the sensor network component
	 */
	public NetworkComponent getNetworkComponentSlackNodeSensor() {
		if (networkComponentSlackNodeSensor==null) {

			// ---- Which sensor / sensor data? -------------------------------
			SlackNodeDescription snDesc = this.electricalNetworkCalculationStrategy.getNetworkModelToCsvMapper().getSlackNodeVector().get(0);
			
			NetworkModel networkModel = this.getAggregationHandler().getNetworkModel();
			NetworkComponent netCompSlackNode = networkModel.getNetworkComponent(snDesc.getNetworkComponentID());

			// --- Try to find a sensor Neighbor here -------------------------
			Vector<NetworkComponent> snNeighbours = networkModel.getNeighbourNetworkComponents(netCompSlackNode);
			for (int i = 0; i < snNeighbours.size(); i++) {
				NetworkComponent snNeighbour = snNeighbours.get(i);
				String netCompType = snNeighbour.getType();
				ComponentTypeSettings cts = networkModel.getGeneralGraphSettings4MAS().getCurrentCTS().get(netCompType);
				
				// --- Check if transformer, agent and sensor adapter ---------
				boolean isTriPhaseSensorAdapter = cts.getAdapterClass().equals(TriPhaseSensorAdapter.class.getName());
				boolean isUniPhaseSensorAdapter = cts.getAdapterClass().equals(UniPhaseSensorAdapter.class.getName());
				if (snNeighbour.getType().toLowerCase().contains("sensor")==true && cts.getAgentClass()!=null && (isUniPhaseSensorAdapter || isTriPhaseSensorAdapter)) {
					networkComponentSlackNodeSensor = snNeighbour;
					break;
				}
			}
			if (networkComponentSlackNodeSensor==null) {
				String domainDescription = this.getElectricalNetworkConfiguration().getDomain() + " (ID = " + this.getElectricalNetworkConfiguration().getID() + ")";
				System.out.println("[" + this.getClass().getSimpleName() + "] Could not find any active slack node sensor for slack node '" + snDesc.getNetworkComponentID() + "' in the sub network model of domain '" + domainDescription + "'!");
				System.out.println("[" + this.getClass().getSimpleName() + "] => A constant slack node voltage of " + this.getInitialVoltageLevel() + " V will be used for the power flow calculation.");
			}
		}
		return networkComponentSlackNodeSensor;
	}
	
}
