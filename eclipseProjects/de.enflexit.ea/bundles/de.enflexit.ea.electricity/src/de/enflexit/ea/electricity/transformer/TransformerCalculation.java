package de.enflexit.ea.electricity.transformer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.awb.env.networkModel.NetworkComponent;

import agentgui.simulationService.time.TimeModelDiscrete;
import de.enflexit.common.SerialClone;
import de.enflexit.ea.core.aggregation.AbstractAggregationHandler;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.TimeModelType;
import de.enflexit.ea.core.dataModel.ontology.ElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseElectricalNodeState;
import de.enflexit.ea.electricity.ElectricalNodeStateConverter;
import de.enflexit.ea.electricity.aggregation.taskThreading.ElectricitySubNetworkGraph.SubNetworkConnection;
import de.enflexit.ea.electricity.aggregation.taskThreading.ElectricitySubNetworkGraph.SubNetworkGraphNode;
import de.enflexit.ea.electricity.aggregation.taskThreading.ElectricityTaskThreadCoordinator;
import de.enflexit.ea.electricity.transformer.TransformerDataModel.TapSide;
import de.enflexit.ea.electricity.transformer.TransformerDataModel.TransformerSystemVariable;
import energy.OptionModelController;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.helper.FixedVariableHelper;
import energy.helper.NumberHelper;
import energy.helper.ScheduleListHelper;
import energy.helper.TechnicalSystemStateHelper;
import energy.optionModel.FixedDouble;
import energy.optionModel.FixedInteger;
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
	public TransformerDataModel getTransformerDataModel() {
		if (transformerDataModel==null && this.getOptionModelController()!=null) {
			transformerDataModel = TransformerCalculation.getTransformerDataModelFromOptionModelController(this.getOptionModelController());
		}
		return transformerDataModel;
	}
	
	// --------------------------------------------------------------------------------------------
	// --- From here, transformer updates after network calculation -------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * Updates the transformers low voltage site.
	 * @param subNetworkGraphNode the current sub network graph node
	 */
	public void updateLowVoltageSideTransformerState(SubNetworkGraphNode subNetworkGraphNode) {
	
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
		
		String iString = "";
		TransformerTotalCurrentCalculation tcc = subNetworkGraphNode.getTransformersTotalCurrentCalculation().calculate();
		TechnicalSystemStateEvaluation tsseWork = this.getTechnicalSystemStateEvaluation();
		if (this.getTransformerDataModel().isLowerVoltage_ThriPhase()==true) {
			
			double iRealL1 = tcc.getTotalCurrentReal().get(Phase.L1);
			double iImagL1 = tcc.getTotalCurrentImag().get(Phase.L1); 
			double iRealL1Old = this.updateIOListValue(tsseWork, TransformerSystemVariable.lvTotaCurrentRealL1.name(), iRealL1);
			double iImagL1Old = this.updateIOListValue(tsseWork, TransformerSystemVariable.lvTotaCurrentImagL1.name(), iImagL1);
			
			double iRealL2 = tcc.getTotalCurrentReal().get(Phase.L2);
			double iImagL2 = tcc.getTotalCurrentImag().get(Phase.L2);
			double iRealL2Old = this.updateIOListValue(tsseWork, TransformerSystemVariable.lvTotaCurrentRealL2.name(), iRealL2);
			double iImagL2Old = this.updateIOListValue(tsseWork, TransformerSystemVariable.lvTotaCurrentImagL2.name(), iImagL2);
			
			double iRealL3 = tcc.getTotalCurrentReal().get(Phase.L3);
			double iImagL3 = tcc.getTotalCurrentImag().get(Phase.L3); 
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
			double iReal = tcc.getTotalCurrentReal().get(Phase.AllPhases);
			double iImag = tcc.getTotalCurrentImag().get(Phase.AllPhases);
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
	public void updateHighVoltageSideTransformerState() {
		
		boolean debugThisMethod = false;
		debugThisMethod = debugThisMethod && this.subNetworkConnection.getConnectingNetworkComponentID().equals("MV-Transformer-1");
		
		// ----------------------------------------------------------------------------------------
		// --- What's ToDo here? ------------------------------------------------------------------
		// ----------------------------------------------------------------------------------------
		// --- HIGH Voltage Side & UPWARDS calculation:
		// --- => Network Calculation provides Upper Voltage level for the transformer
		// --- + Get the target upper voltage level for the transformer 
		// --- + 
		// --- +  
		// --- + Update SlackNode voltage level for lower voltage network calculation 
		// ----------------------------------------------------------------------------------------
		
		// --- Get transformer state ----------------------
		TechnicalSystemStateEvaluation tsseWork = this.getTechnicalSystemStateEvaluation();
		double targetUpperVoltageLevel = 0.0;
		
		ElectricalNodeState elNodeState = this.subNetworkConnection.getHighVoltageElectricalNodeState();
		boolean isTriPhase = this.getTransformerDataModel().isUpperVoltage_ThriPhase();
		if (isTriPhase==true) {
			// --- Three Phases -------------
			UniPhaseElectricalNodeState upElNodeState = ElectricalNodeStateConverter.convertToUniPhaseElectricalNodeState(elNodeState);
			targetUpperVoltageLevel = upElNodeState.getVoltageReal().getValue();
			
		} else {
			// --- Single Phase -------------
			UniPhaseElectricalNodeState upElNodeState = (UniPhaseElectricalNodeState) elNodeState;
			targetUpperVoltageLevel = upElNodeState.getVoltageReal().getValue();
			this.updateIOListValue(tsseWork, TransformerSystemVariable.hvVoltageRealAllPhases.name(), targetUpperVoltageLevel);
		}
		
		
		if (debugThisMethod==true) this.debugPrint("Flows BEFORE transformer calculation: " + TechnicalSystemStateHelper.toStringEnergyAndGoodFlows(tsseWork, 4, false, ", "));

		// --- Consider the transformers slack node side --
		TapSide slacknodeSide = this.getTransformerDataModel().getSlackNodeSide();
		if (slacknodeSide==TapSide.HighVoltageSide) {
			// --------------------------------------------
			// --- Slack node on high voltage side --------
			// --------------------------------------------
			this.getOptionModelController().updateTechnicalSystemStateEnergyFlows(tsseWork, 0, true);
			
		} else {
			// --------------------------------------------
			// --- Slack node on low voltage side ---------
			// --------------------------------------------
			
			// --- Find the right low voltage level to match the target upper voltage level ---- 
			FixedInteger fiTapPos = (FixedInteger) TechnicalSystemStateHelper.getFixedVariable(tsseWork.getIOlist(), TransformerSystemVariable.tapPos.name());
			
			double targetLowVoltageLevel = this.getLowVoltageLevelFromHighVoltageLevel(fiTapPos.getValue(), targetUpperVoltageLevel);
			this.updateIOListValue(tsseWork, TransformerSystemVariable.lvVoltageRealAllPhases.name(), targetLowVoltageLevel);
			this.getOptionModelController().updateTechnicalSystemStateEnergyFlows(tsseWork, 0, true);
		}
		if (debugThisMethod==true) this.debugPrint("Flows AFTER transformer calculation:  " + TechnicalSystemStateHelper.toStringEnergyAndGoodFlows(tsseWork, 4, false, ", "));
	}
	
	/**
	 * Based on the upper voltage level and the {@link TransformerDataModel}, returns the the low voltage level.
	 *
	 * @param tapPos the current transformers tap position
	 * @param upperVoltageLevel the target upper voltage level
	 * @return the estimated low voltage level
	 */
	private double getLowVoltageLevelFromHighVoltageLevel(int tapPos, double upperVoltageLevel) {
		
		double U_rTUS = this.getTransformerDataModel().getLowerVoltage_vmLV() * 1000;
		double U_rTOS = this.getTransformerDataModel().getUpperVoltage_vmHV() * 1000;
		double transLVRealAllPhases = U_rTUS;
		
		double tapNeutral = this.getTransformerDataModel().getTapNeutral();
		double voltageDeltaPerTap = this.getTransformerDataModel().getVoltageDeltaPerTap_dVm();
		
		switch (this.getTransformerDataModel().getTapSide()) {
		case LowVoltageSide:
			transLVRealAllPhases = upperVoltageLevel / U_rTOS * U_rTUS * (1 + voltageDeltaPerTap * (tapPos - tapNeutral)/100);
			break;

		case HighVoltageSide:
			transLVRealAllPhases = upperVoltageLevel / U_rTOS * U_rTUS / (1 + voltageDeltaPerTap * (tapPos - tapNeutral)/100);
			break;
		}
		return transLVRealAllPhases;
	}
	
	// --------------------------------------------------------------------------------------------
	// --- From here, some TSSE transformer help functions ----------------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * Returns a working copy of the latest {@link TechnicalSystemStateEvaluation} from the aggregation handler.
	 * @return the technical system state evaluation
	 */
	public TechnicalSystemStateEvaluation getTechnicalSystemStateEvaluation() {
		if (tsseCurrent==null) {
			// --- Get a copy of the latest TSSE ------------------------------
			AbstractAggregationHandler aggregationHandler = this.taskThreadCoordinator.getAggregationHandler();
			TechnicalSystemStateEvaluation tsseLatest = aggregationHandler.getLastTechnicalSystemStateFromScheduleController(this.subNetworkConnection.getConnectingNetworkComponentID(), 0, true);
			
			// --- Check time model type --------------------------------------
			TimeModelType timeModelType = aggregationHandler.getHyGridAbstractEnvironmentModel().getTimeModelType();
			if (timeModelType==TimeModelType.TimeModelDiscrete) {
				// --- Consider the discrete step length ----------------------
				TimeModelDiscrete tmCont = (TimeModelDiscrete) aggregationHandler.getTimeModel();
				tsseLatest.setStateTime(tmCont.getStep());
			}
			
			// --- Set this copy as current instance --------------------------
			tsseCurrent = tsseLatest;

			// --- Add to Schedule for network calculation --------------------
			Schedule secTransSchedule = this.getTransformerScheduleOfNetworkCalculation();
			
			// --- Check for the parent -----------------------
			TechnicalSystemStateEvaluation tsseParent = secTransSchedule.getTechnicalSystemStateEvaluation(); 
			if (tsseParent!=null) {
				this.tsseCurrent.setParent(tsseParent);
			}
			secTransSchedule.setTechnicalSystemStateEvaluation(this.tsseCurrent);
			
		}
		return tsseCurrent;
	}
	
	/**
	 * Returns the schedule that is created during the network calculation.
	 * @return the transformer schedule of network calculation
	 */
	private Schedule getTransformerScheduleOfNetworkCalculation() {
		
		// --- Get the second Schedule --------------------
		String strategyClassName = this.taskThreadCoordinator.getClass().getName();
		final ScheduleController sc = this.subNetworkConnection.getResultScheduleController();
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
		return secTransSchedule;
	}
	
	/**
	 * Moves the last calculated transformer states to the second transformer Schedule and restores the state before the network calculations.
	 */
	public void finalizeTransformerCalculation() {

		this.tsseCurrent = null;
		
		// --- Update Aggregations UI ---------------------
		final ScheduleController sc = this.subNetworkConnection.getResultScheduleController();
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
		if (fdValue==null) {
			tsse.getIOlist().add(FixedVariableHelper.createFixedDouble(ioVarName, newValue));
			return 0.0;
		}
		double oldValue = fdValue.getValue();
		fdValue.setValue(newValue);
		return oldValue;
	}
	
	
	// ------------------------------------------------------------------------
	// --- From here, some static help methods --------------------------------
	// ------------------------------------------------------------------------
	/**
	 * Returns the transformer data model from the specified {@link TechnicalSystem}.
	 *
	 * @param technicalSystem the technical system
	 * @return the transformer data model from option model controller
	 */
	public static TransformerDataModel getTransformerDataModelFromTechnicalSystem(TechnicalSystem technicalSystem) {
		if (technicalSystem==null) return null;
		OptionModelController omc = new OptionModelController();
		omc.setTechnicalSystem(technicalSystem);
		return getTransformerDataModelFromOptionModelController(omc);
	}
	/**
	 * Returns the transformer data model from the specified option model controller.
	 *
	 * @param omc the OptionModelController to use
	 * @return the transformer data model from option model controller
	 */
	public static TransformerDataModel getTransformerDataModelFromOptionModelController(OptionModelController omc) {
		
		if (omc==null) return null;
		
		List<SystemVariableDefinitionStaticModel> sysVarDefStaticModelList = new ArrayList<>();
		List<SystemVariableDefinition> sysVarDefList = omc.getTechnicalSystem().getSystemVariables();
		for (SystemVariableDefinition sysVarDef : sysVarDefList) {
			if (sysVarDef instanceof SystemVariableDefinitionStaticModel) {
				sysVarDefStaticModelList.add((SystemVariableDefinitionStaticModel) sysVarDef);
			}
		}
		// --- Check each static model for a transformer model ------
		for (SystemVariableDefinitionStaticModel sysVarDefStaticModel : sysVarDefStaticModelList) {
			Serializable model = omc.getStaticModelInstance(sysVarDefStaticModel);
			if (model instanceof TransformerDataModel) {
				return (TransformerDataModel) model;
			}
		}
		return null;
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
