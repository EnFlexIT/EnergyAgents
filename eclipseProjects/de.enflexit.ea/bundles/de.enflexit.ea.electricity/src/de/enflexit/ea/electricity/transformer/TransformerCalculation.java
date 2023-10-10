package de.enflexit.ea.electricity.transformer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.common.SerialClone;
import de.enflexit.ea.core.dataModel.ontology.ElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseElectricalNodeState;
import de.enflexit.ea.electricity.aggregation.taskThreading.ElectricitySubNetworkGraph.SubNetworkConnection;
import de.enflexit.ea.electricity.aggregation.taskThreading.ElectricityTaskThreadCoordinator;
import de.enflexit.ea.electricity.transformer.TransformerDataModel.TransformerSystemVariable;
import energy.OptionModelController;
import energy.helper.NumberHelper;
import energy.helper.ScheduleListHelper;
import energy.helper.TechnicalSystemStateHelper;
import energy.optionModel.FixedDouble;
import energy.optionModel.Schedule;
import energy.optionModel.SystemVariableDefinition;
import energy.optionModel.SystemVariableDefinitionStaticModel;
import energy.optionModel.TechnicalSystem;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.schedule.ScheduleController;
import energy.schedule.ScheduleNotification;

/**
 * The Class TransformerCalculation .
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class TransformerCalculation {

	private ElectricityTaskThreadCoordinator taskThreadCoordinator;
	private SubNetworkConnection subNetworkConnection;
	
	private OptionModelController omcTransformer;
	private TransformerDataModel transformerDataModel;
	
	private TechnicalSystemStateEvaluation tsseCurrent;
	private TechnicalSystemStateEvaluation tsseCurrentOriginal;
	
	/**
	 * Instantiates a new transformer integration.
	 *
	 * @param taskThreadCoordinator the current {@link ElectricityTaskThreadCoordinator}
	 * @param subNetworkConnection the {@link SubNetworkConnection} for which the current transformer is responsible
	 */
	public TransformerCalculation(ElectricityTaskThreadCoordinator taskThreadCoordinator, SubNetworkConnection subNetworkConnection) {
		this.taskThreadCoordinator = taskThreadCoordinator;
		this.subNetworkConnection = subNetworkConnection;
		this.initialize();
	}
	/**
	 * Initializes this class.
	 */
	private void initialize() {
		this.getOptionModelController();
		this.getTransformerDataModel();
	}
	
	/**
	 * Returns the instance of a EOM - transformer model the transformer option model controller.
	 * @return the transformer option model controller
	 */
	private OptionModelController getOptionModelController() {
		if (omcTransformer==null) {
			// --- Try getting the NetworksModels NetworkComponent ------------
			NetworkComponent netCompTransformer = this.taskThreadCoordinator.getAggregationHandler().getNetworkModel().getNetworkComponent(this.subNetworkConnection.getConnectingNetworkComponentID());
			Object dataModel = netCompTransformer.getDataModel();
			if (dataModel!=null && dataModel instanceof TechnicalSystem) {
				// --- Get the TechnicalSystem of the transformer -------------
				TechnicalSystem tsTransformer = (TechnicalSystem) dataModel;
				omcTransformer = new OptionModelController();
				omcTransformer.setTechnicalSystem(SerialClone.clone(tsTransformer));
				
			} else {
				System.err.println("[" + this.getClass().getSimpleName() + "] Could not find an EOM-TechnicalSystem definition for the Transformer '" + this.subNetworkConnection.getConnectingNetworkComponentID() + "'!");
			}
		}
		return omcTransformer;
	}
	/**
	 * Returns the current {@link TransformerDataModel}. This can be used to read and understand the current 
	 * transformer configuration. Further, it can be used to set the current SlackNode voltage level for the 
	 * transformer calculation.
	 * 
	 * @return the transformer data model
	 */
	private TransformerDataModel getTransformerDataModel() {
		if (transformerDataModel==null && this.getOptionModelController()!=null) {
			// --- Get all static models --------------------------------
			List<SystemVariableDefinitionStaticModel> sysVarDefStaticModelList = new ArrayList<>();
			List<SystemVariableDefinition> sysVarDefList = this.getOptionModelController().getTechnicalSystem().getSystemVariables();
			for (SystemVariableDefinition sysVarDef : sysVarDefList) {
				if (sysVarDef instanceof SystemVariableDefinitionStaticModel) {
					sysVarDefStaticModelList.add((SystemVariableDefinitionStaticModel) sysVarDef);
				}
			}
			// --- Check each static model for a transformer model ------
			for (SystemVariableDefinitionStaticModel sysVarDefStaticModel : sysVarDefStaticModelList) {
				Serializable model = this.getOptionModelController().getStaticModelInstance(sysVarDefStaticModel);
				if (model instanceof TransformerDataModel) {
					transformerDataModel = (TransformerDataModel) model;
					break;
				}
			}
		}
		return transformerDataModel;
	}
	
	/**
	 * Updates the transformer state according to the calculation direction of the network calculation.
	 *
	 * @param isLowVoltageSide true, if the network calculation was executed on the low voltage side
	 * @param isUpwardsCalculation true, if the network calculation is executed from lower to higher voltage level
	 */
	public void updateTransformerState(boolean isLowVoltageSide, boolean isUpwardsCalculation) {

		// ------------------------------------------------------------------------------------------------------------
		// --- Current Situation: Network Calculation for an Electrical Graph Level / Voltage Level is done !! -------- 
		// ------------------------------------------------------------------------------------------------------------
		if (isUpwardsCalculation==true) {
			if (isLowVoltageSide==true) {
				this.updateLowVoltageSideTransformerStateForUpwardsCalculation();
			} else {
				this.updateHighVoltageSideTransformerStateForUpwardsCalculation();
			}
		} else {
			if (isLowVoltageSide==true) {
				this.updateLowVoltageTransformerStateForDownwardsCalculation();
			} else {
				this.updateHighVoltageTransformerStateForDownwardsCalculation();
			}
		}
	}
	
	// --------------------------------------------------------------------------------------------
	// --- From here, transformer updates for UPWARDS network calculation -------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * Updates the transformer state according to the calculation direction of the network calculation.
	 * @param isLowVoltageSide true, if the network calculation was executed on the low voltage side
	 */
	private void updateLowVoltageSideTransformerStateForUpwardsCalculation() {
	
		boolean debugThisMethod = true;
		debugThisMethod = debugThisMethod && this.subNetworkConnection.getConnectingNetworkComponentID().equals("MV-Transformer-1");
		
		// ----------------------------------------------------------------------------------------
		// --- What's ToDo here? ------------------------------------------------------------------
		// ----------------------------------------------------------------------------------------
		// --- LOW Voltage Side & UPWARDS calculation:
		// --- => Network calculation provides current for transformer calculation !
		// --- + Get the current TSSE from the aggregation 
		// --- + Get the calculated node state from the network calculation
		// --- + Update the 'measured' low voltage current 
		// --- + Update TSSE to provide correct energy flows for next level network calculation 
		// ----------------------------------------------------------------------------------------
		
		TechnicalSystemStateEvaluation tsseWork = this.getTechnicalSystemStateEvaluation();
		
		ElectricalNodeState elNodeState = this.subNetworkConnection.getLowVoltageElectricalNodeState();
		boolean isTriPhase = this.getTransformerDataModel().isLowerVoltage_ThriPhase();
		String iString = "";
		if (isTriPhase==true) {
			// --- Three Phases -------------
			TriPhaseElectricalNodeState tpElNodeState = (TriPhaseElectricalNodeState) elNodeState;
			
			UniPhaseElectricalNodeState upElNodeStateL1 = tpElNodeState.getL1();
			double iRealL1 = this.getCurrentReal(upElNodeStateL1);
			double iImagL1 = this.getCurrentImag(upElNodeStateL1); 
			double iRealL1Old = this.updateIOListValue(tsseWork, TransformerSystemVariable.lvTotaCurrentRealL1.name(), iRealL1);
			double iImagL1Old = this.updateIOListValue(tsseWork, TransformerSystemVariable.lvTotaCurrentImagL1.name(), iImagL1);
			
			UniPhaseElectricalNodeState upElNodeStateL2 = tpElNodeState.getL2();
			double iRealL2 = this.getCurrentReal(upElNodeStateL2);
			double iImagL2 = this.getCurrentImag(upElNodeStateL2);
			double iRealL2Old = this.updateIOListValue(tsseWork, TransformerSystemVariable.lvTotaCurrentRealL2.name(), iRealL2);
			double iImagL2Old = this.updateIOListValue(tsseWork, TransformerSystemVariable.lvTotaCurrentImagL2.name(), iImagL2);
			
			UniPhaseElectricalNodeState upElNodeStateL3 = tpElNodeState.getL3();
			double iRealL3 = this.getCurrentReal(upElNodeStateL3);
			double iImagL3 = this.getCurrentImag(upElNodeStateL3); 
			double iRealL3Old = this.updateIOListValue(tsseWork, TransformerSystemVariable.lvTotaCurrentRealL3.name(), iRealL3);
			double iImagL3Old = this.updateIOListValue(tsseWork, TransformerSystemVariable.lvTotaCurrentImagL3.name(), iImagL3);
			
			if (debugThisMethod==true) {
				iString += "I-Real: ";
				iString += NumberHelper.round(iRealL1Old, 2) + " => " + NumberHelper.round(iRealL1, 2) + ", ";
				iString += NumberHelper.round(iRealL2Old, 2) + " => " + NumberHelper.round(iRealL2, 2) + ", ";
				iString += NumberHelper.round(iRealL3Old, 2) + " => " + NumberHelper.round(iRealL3, 2) + ", ";
				iString += "I-Imag: ";
				iString += NumberHelper.round(iImagL1Old, 2) + " => " + NumberHelper.round(iImagL1, 2) + ", ";
				iString += NumberHelper.round(iImagL2Old, 2) + " => " + NumberHelper.round(iImagL2, 2) + ", ";
				iString += NumberHelper.round(iImagL3Old, 2) + " => " + NumberHelper.round(iImagL3, 2) + "";
			}
			
		} else {
			// --- Single Phase -------------
			UniPhaseElectricalNodeState upElNodeState = (UniPhaseElectricalNodeState) elNodeState;
			double iReal = this.getCurrentReal(upElNodeState);
			double iImag = this.getCurrentImag(upElNodeState);
			double iRealOld = this.updateIOListValue(tsseWork, TransformerSystemVariable.lvTotaCurrentRealAllPhases.name(), iReal);
			double iImagOld = this.updateIOListValue(tsseWork, TransformerSystemVariable.lvTotaCurrentImagAllPhases.name(), iImag);
		
			if (debugThisMethod==true) {
				iString += "I-Real: ";
				iString += NumberHelper.round(iRealOld, 2) + " to " + NumberHelper.round(iReal, 2) + ", ";
				iString += "I-Imag: ";
				iString += NumberHelper.round(iImagOld, 2) + " to " + NumberHelper.round(iImag, 2) + ", ";
			}
		}
		
		// --- Execute transformer calculation --------------------------------
		if (debugThisMethod==true) this.debugPrint("Current changes: " + iString);
		if (debugThisMethod==true) this.debugPrint("Flows BEFORE transformer calculation: " + TechnicalSystemStateHelper.toStringEnergyAndGoodFlows(tsseWork, 4, false, ", "));
		this.getOptionModelController().updateTechnicalSystemStateEnergyFlows(tsseWork, 0, true);
		if (debugThisMethod==true) this.debugPrint("Flows AFTER transformer calculation:  " + TechnicalSystemStateHelper.toStringEnergyAndGoodFlows(tsseWork, 4, false, ", "));
		
	}
	
	/**
	 * Updates the transformer state according to the calculation direction of the network calculation.
	 * @param isLowVoltageSide true, if the network calculation was executed on the low voltage side
	 */
	private void updateHighVoltageSideTransformerStateForUpwardsCalculation() {
		
		boolean debugThisMethod = true;
		debugThisMethod = debugThisMethod && this.subNetworkConnection.getConnectingNetworkComponentID().equals("MV-Transformer-1");
		
		// ----------------------------------------------------------------------------------------
		// --- What's ToDo here? ------------------------------------------------------------------
		// ----------------------------------------------------------------------------------------
		// --- HIGH Voltage Side & UPWARDS calculation:
		// --- => Network Calculation provides Upper Voltage level for underlying network 
		// --- +  
		// --- + 
		// --- +  
		// --- + Update SlackNode voltage level for underlying network calculation 
		// ----------------------------------------------------------------------------------------
		
		TechnicalSystemStateEvaluation tsseWork = this.getTechnicalSystemStateEvaluation();
		
		ElectricalNodeState elNodeState = this.subNetworkConnection.getHighVoltageElectricalNodeState();
		if (debugThisMethod==true) this.debugPrint("Flows BEFORE transformer calculation: " + TechnicalSystemStateHelper.toStringEnergyAndGoodFlows(tsseWork, 4, false, ", "));
		
		boolean isTriPhase = this.getTransformerDataModel().isLowerVoltage_ThriPhase();
		if (isTriPhase==true) {
			
		} else {
			
		}
		
	}
	
	
	// --------------------------------------------------------------------------------------------
	// --- From here, transformer updates for DOWNWARDS network calculation -----------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * Updates the transformer state according to the calculation direction of the network calculation.
	 */
	private void updateLowVoltageTransformerStateForDownwardsCalculation() {
		
		boolean debugThisMethod = true;
		
		// ----------------------------------------------------------------------------------------
		// --- What's ToDo here? ------------------------------------------------------------------
		
		// ----------------------------------------------------------------------------------------
		
		TechnicalSystemStateEvaluation tsseWork = this.getTechnicalSystemStateEvaluation();
		
		ElectricalNodeState elNodeState = this.subNetworkConnection.getLowVoltageElectricalNodeState();
		boolean isTriPhase = this.getTransformerDataModel().isLowerVoltage_ThriPhase();
		if (isTriPhase==true) {
			
		} else {
			
		}
	
		
	}
	/**
	 * Updates the transformer state according to the calculation direction of the network calculation.
	 */
	private void updateHighVoltageTransformerStateForDownwardsCalculation() {
		
		boolean debugThisMethod = true;
		
		// ----------------------------------------------------------------------------------------
		// --- What's ToDo here? ------------------------------------------------------------------
		
		// ----------------------------------------------------------------------------------------
		
		TechnicalSystemStateEvaluation tsseWork = this.getTechnicalSystemStateEvaluation();
		
		ElectricalNodeState elNodeState = this.subNetworkConnection.getHighVoltageElectricalNodeState();
		boolean isTriPhase = this.getTransformerDataModel().isLowerVoltage_ThriPhase();
		if (isTriPhase==true) {
			
		} else {
			
		}
		
	}
	
	
	/**
	 * Returns the latest {@link TechnicalSystemStateEvaluation} from the aggregation handler.
	 * @return the technical system state evaluation
	 */
	private TechnicalSystemStateEvaluation getTechnicalSystemStateEvaluation() {
		if (tsseCurrent==null) {
			tsseCurrent = this.taskThreadCoordinator.getAggregationHandler().getLastTechnicalSystemStateFromScheduleController(this.subNetworkConnection.getConnectingNetworkComponentID(), false);
			tsseCurrentOriginal = TechnicalSystemStateHelper.copyTechnicalSystemStateEvaluation(tsseCurrent, false);
		}
		return tsseCurrent;
	}
	/**
	 * Moves the last calculated transformer states to the second transformer Schedule and restores the state before the network calculations.
	 */
	public void moveCalculatedTransformerStatesToSecondSchedule() {
		
		TechnicalSystemStateEvaluation tsseWorkedAt = TechnicalSystemStateHelper.copyTechnicalSystemStateEvaluation(this.tsseCurrent);
		TechnicalSystemStateEvaluation tsseOriginal = this.tsseCurrentOriginal;
		// --- Reset local reminder variables -------------
		this.tsseCurrent = null;
		this.tsseCurrentOriginal = null;

		
		String strategyClassName = this.taskThreadCoordinator.getClass().getName();
		final ScheduleController sc = this.subNetworkConnection.getResultScheduleController();
		
		// ------------------------------------------------
		// --- Update the second transformer schedule -----
		// ------------------------------------------------
		Schedule secTransSchedule = ScheduleListHelper.getScheduleByStrategyClass(sc.getScheduleList(), strategyClassName);
		if (secTransSchedule==null) {
			// --- Create that Schedule -------------------
			secTransSchedule = new Schedule();
			secTransSchedule.setRealTimeSchedule(true);
			secTransSchedule.setStrategyClass(strategyClassName);
			secTransSchedule.setSourceThread(Thread.currentThread().getName());
			secTransSchedule.setPriority(0);
			sc.getScheduleList().getSchedules().add(secTransSchedule);
		}
		// --- Check for the parent -----------------------
		TechnicalSystemStateEvaluation tsseParent = secTransSchedule.getTechnicalSystemStateEvaluation(); 
		if (tsseParent!=null) {
			tsseWorkedAt.setParent(tsseParent);
		}
		// --- Set state to schedule ----------------------
		secTransSchedule.setTechnicalSystemStateEvaluation(tsseWorkedAt);
		
		// ------------------------------------------------
		// --- Restore first transformer schedule ---------
		// ------------------------------------------------
		Schedule firstTransSchedule = sc.getScheduleList().getSchedules().get(0);
		if (tsseOriginal!=null) {
			if (secTransSchedule.getTechnicalSystemStateEvaluation()!=null &&  secTransSchedule.getTechnicalSystemStateEvaluation().getParent()!=null) {
				tsseOriginal.setParent(secTransSchedule.getTechnicalSystemStateEvaluation().getParent());
			}
			firstTransSchedule.setTechnicalSystemStateEvaluation(tsseOriginal);
		}
		
 		// ------------------------------------------------
		// --- Update Aggregations UI ---------------------
		// ------------------------------------------------
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				sc.setChangedAndNotifyObservers(new ScheduleNotification(ScheduleNotification.Reason.ScheduleListLoaded, null)); 
			}
		});
		
	}
	
	/**
	 * Updates the specified IO list variable value of the TSSE.
	 *
	 * @param tsse the TechnicalSystemStateEvaluation to update
	 * @param ioVarName the IO variable name
	 * @param newValue the new value
	 * @return the previous value
	 */
	private double updateIOListValue(TechnicalSystemStateEvaluation tsse, String ioVarName, double newValue) {
		FixedDouble fdValue = ((FixedDouble) TechnicalSystemStateHelper.getFixedVariable(tsse.getIOlist(), ioVarName));
		double oldValue = fdValue.getValue();
		fdValue.setValue(newValue);
		return oldValue;
	}
	
	
	// ------------------------------------------------------------------------
	// --- Some electrical calculation methods --------------------------------
	// ------------------------------------------------------------------------
	/**
	 * Returns the electrical current real (iReal) based on the specified UniPhaseElectricalNodeState.
	 *
	 * @param upElNodeState the up el node state
	 * @return the current real
	 */
	private double getCurrentReal(UniPhaseElectricalNodeState upElNodeState) {
		return upElNodeState.getCurrent().getValue() * upElNodeState.getCosPhi();
	}
	/**
	 * Returns the electrical current real (iReal) based on the specified UniPhaseElectricalNodeState.
	 *
	 * @param upElNodeState the up el node state
	 * @return the current real
	 */
	private double getCurrentImag(UniPhaseElectricalNodeState upElNodeState) {
		if (upElNodeState.getSCalculated()!=0) {
			return upElNodeState.getCurrent().getValue() * (upElNodeState.getQCalculated() / upElNodeState.getSCalculated());
		}
		return 0.0;
		
	}
	
	
	// ------------------------------------------------------------------------
	// --- Some debug print methods -------------------------------------------
	// ------------------------------------------------------------------------
	/**
	 * Prints the specified message.
	 * @param msg the message to print
	 */
	private void debugPrint(String msg) {
		this.debugPrint(msg, false);
	}
	/**
	 * Prints the specified message.
	 *
	 * @param msg the message to print
	 * @param isError the is error
	 */
	private void debugPrint(String msg, boolean isError) {
		this.debugPrint(msg, isError, false);
	}
	/**
	 * Prints the specified message.
	 *
	 * @param msg the message to print
	 * @param isError the is error
	 * @param insertNewLineAhead the insert new line ahead
	 */
	private void debugPrint(String msg, boolean isError, boolean insertNewLineAhead) {
		String message = "[" + this.getClass().getSimpleName() + "] " + msg;
		this.taskThreadCoordinator.debugPrint(message, isError, insertNewLineAhead);
	}
	
}
