package energy.samples.groupStrategies;

import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;

import energy.GlobalInfo;
import energy.OptionModelController;
import energy.evaluation.TechnicalSystemStateDeltaEvaluation;
import energy.optionModel.EnergyFlowMeasured;
import energy.optionModel.Schedule;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalInterface;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energygroup.GroupTreeNodeObject;
import energygroup.calculation.EnergyFlowsMeasuredGroup;
import energygroup.calculation.EnergyFlowsMeasuredGroupMember;
import energygroup.evaluation.AbstractGroupEvaluationStrategy;

/**
 * The Class SmartHouseStrategy.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class SmartHouseStrategy extends AbstractGroupEvaluationStrategy {

	
	/**
	 * Instantiates a new smart house strategy.
	 * @param optionModelController the option model controller
	 */
	public SmartHouseStrategy(OptionModelController optionModelController) {
		super(optionModelController);
	}
	
	/* (non-Javadoc)
	 * @see energy.evaluation.AbstractEvaluationStrategy#getCustomToolBarElements()
	 */
	@Override
	public Vector<JComponent> getCustomToolBarElements() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see energy.evaluation.AbstractEvaluationStrategy#runEvaluation()
	 */
	@Override
	public void runEvaluation() {
		
//		if (this.executeSubEvaluationStrategies()==false) return;
		
		// --- Get Start situation ------------------------
		TechnicalSystemStateEvaluation tsse = this.getInitialTechnicalSystemStateEvaluation();
		// --- Search by walking through time -------------------------------------------
		while (tsse.getGlobalTime() < this.getEndTime() ) {
			
			// --------------------------------------------------------------------------
			// --- Get the possible subsequent steps and states -------------------------
			// --------------------------------------------------------------------------
			Vector<TechnicalSystemStateDeltaEvaluation> deltaSteps = this.getAllDeltaEvaluationsStartingFromTechnicalSystemState(tsse);
			if (deltaSteps.size()==0) {
				System.err.println("No further 'deltaStepsPossible' => interrupt search!");
				deltaSteps = this.getAllDeltaEvaluationsStartingFromTechnicalSystemState(tsse);
				break;
			}
			
			// --------------------------------------------------------------------------
			// --- Prepare the next time step -------------------------------------------
			// --------------------------------------------------------------------------			
			// --- Make a decision ------------------------------------------------------
			int decisionIndex = 0;
			// --- Find specific state --------------------------------------------------
			for (int i = 0; i < deltaSteps.size(); i++) {
				if (deltaSteps.get(i).getTechnicalSystemStateEvaluation().getStateID().equals("Load") ) {
					decisionIndex = i;
					break;
				}
			}
			decisionIndex = GlobalInfo.getRandomInteger(0, deltaSteps.size()-1);
			TechnicalSystemStateDeltaEvaluation tssDeltaEvaluation = deltaSteps.get(decisionIndex);
			
			// --- Replace TechnicalSystemStateEvaluation by the next one ---------------
			TechnicalSystemStateEvaluation tsseNext = this.getNextTechnicalSystemStateEvaluation(tsse, tssDeltaEvaluation);
			while (tsseNext==null) {
				// --- Error with the previous decision - make a new decision -----------
				decisionIndex++;
				if (decisionIndex > (deltaSteps.size()-1)) break;
				
				tssDeltaEvaluation = deltaSteps.get(decisionIndex);
				tsseNext = this.getNextTechnicalSystemStateEvaluation(tsse, tssDeltaEvaluation);
				if (isStopEvaluation()==true) break;
			}
			
			// --------------------------------------------------------------------------
			// --- Set new current TechnicalSystemStateEvaluation -----------------------
			// --------------------------------------------------------------------------
			if (tsseNext==null) {
				System.err.println("No possible delta evaluation could be found!");
				break;
			} else {
				tsse = tsseNext;
			}
			
			// --- Stop evaluation ? ----------------------------------------------------
			if (isStopEvaluation()==true) break;
		}
		this.addStateToResults(tsse);
		
	}
	
	/* (non-Javadoc)
	 * @see energygroup.AbstractGroupEvaluationStrategy#doNetworkCalculation(javax.swing.tree.DefaultMutableTreeNode, java.util.List, energygroup.calculation.EnergyFlowsMeasuredGroup)
	 */
	@Override
	public EnergyFlowsMeasuredGroupMember doNetworkCalculation(DefaultMutableTreeNode currentParentNode, List<TechnicalInterface> outerInterfaces, EnergyFlowsMeasuredGroup efmGroup) {

		EnergyFlowsMeasuredGroupMember efmGroupMember = new EnergyFlowsMeasuredGroupMember();
		for (TechnicalInterface ti : outerInterfaces) {
			// --- Calculate the energy flow for this interface ---------------
			EnergyFlowMeasured efm = efmGroup.sumUpEnergyFlowMeasuredByEnergyCarrier(currentParentNode, ti.getEnergyCarrier(), ti.getInterfaceID(), this.getDefaultSIPrefix());
			efmGroupMember.addEnergyFlowMeasured(efm, ti.getInterfaceID(), ti.getEnergyCarrier());
		}
		return efmGroupMember;
	}

	/* (non-Javadoc)
	 * @see energygroup.AbstractGroupEvaluationStrategy#meetDecisionForGroupMemberTechnicalSystem(javax.swing.tree.DefaultMutableTreeNode, energygroup.GroupTreeNodeObject, java.util.Vector)
	 */
	@Override
	public TechnicalSystemStateDeltaEvaluation meetDecisionForTechnicalSystem(DefaultMutableTreeNode currentNode, GroupTreeNodeObject gtno, Vector<TechnicalSystemStateDeltaEvaluation> deltaSteps) {
		
//		OptionModelController omc = gtno.getGroupMemberOptionModelController();
//		TechnicalSystemStateEvaluation tsse = deltaSteps.get(0).getTechnicalSystemStateEvaluation();
//		TechnicalInterfaceConfiguration tic = omc.getTechnicalInterfaceConfiguration(tsse.getConfigID());
//		Vector<TechnicalInterface> tiVector = omc.getTechnicalInterfacesByEnergyCarrier(tic, EnergyCarrier.ELECTRICITY);
//		TechnicalInterface ti = tiVector.get(0);
//		Collections.sort(deltaSteps, TechnicalSystemStateDeltaHelper.getComparatorInterfaceEnergyFlow(ti.getInterfaceID()));

		// --- Random decision --------------------------------------
		int decision = GlobalInfo.getRandomInteger(0, deltaSteps.size()-1);
		return deltaSteps.get(decision);
	}

	
	public Schedule meetDecisionForScheduleList(DefaultMutableTreeNode currentNode, GroupTreeNodeObject gtno, ScheduleList scheduleList) {
		// --- Random decision --------------------------------------
		int decision = GlobalInfo.getRandomInteger(0, scheduleList.getSchedules().size()-1);
		return scheduleList.getSchedules().get(decision);
	}
	
	
}
