package energy.samples.groupStrategies;

import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;

import energy.OptionModelController;
import energy.evaluation.TechnicalSystemStateDeltaEvaluation;
import energy.optionModel.Schedule;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalInterface;
import energygroup.GroupTreeNodeObject;
import energygroup.calculation.EnergyFlowsMeasuredGroup;
import energygroup.calculation.EnergyFlowsMeasuredGroupMember;
import energygroup.evaluation.AbstractGroupEvaluationStrategy;


public class MyGroupEvaluationStrategy extends AbstractGroupEvaluationStrategy {

	public MyGroupEvaluationStrategy(OptionModelController optionModelController) {
		super(optionModelController);
	}

	@Override
	public Vector<JComponent> getCustomToolBarElements() {
		return null;
	}

	@Override
	public void runEvaluation() {
		
	}

	@Override
	public EnergyFlowsMeasuredGroupMember doNetworkCalculation(DefaultMutableTreeNode currentParentNode, List<TechnicalInterface> outerInterfaces, EnergyFlowsMeasuredGroup efmGroup) {
		return null;
	}

	@Override
	public TechnicalSystemStateDeltaEvaluation meetDecisionForTechnicalSystem(DefaultMutableTreeNode currentNode, GroupTreeNodeObject gtno, Vector<TechnicalSystemStateDeltaEvaluation> deltaSteps) {
		return null;
	}

	@Override
	public Schedule meetDecisionForScheduleList(DefaultMutableTreeNode currentNode, GroupTreeNodeObject gtno, ScheduleList scheduleList) {
		return null;
	}

}
