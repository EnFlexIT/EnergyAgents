package de.enflexit.ea.core.simulation.decisionControl;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import agentgui.simulationService.environment.AbstractDiscreteSimulationStep.DiscreteSystemStateType;
import energy.GlobalInfo;
import energy.helper.DisplayHelper;
import energy.helper.TechnicalSystemStateHelper;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class RandomCentralDecisionProcess serves as test class for a central decision process in simulations
 * that uses random decisions for system states.
 * 
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class RandomCentralDecisionProcess extends AbstractCentralDecisionProcess {

	private int iterationCount = 0;
	private int iterationCountMax = 0;
	private boolean isDebug = false;
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.simulation.decisionControl.AbstractCentralDecisionProcess#getTimeStepDecisions(long)
	 */
	@Override
	public TimeStepDecisions getTimeStepDecisions(long evaluationTime) {
		
		// --- Check if the iteration counter needs to be reseted ---
		if (this.isNewEvaluationTime()==true) {
			this.iterationCount = 0;
		} else {
			this.iterationCount++;
		}
		
		// --- Define return type -----------------------------------
		TimeStepDecisions tsd = new TimeStepDecisions();
		
		// --- Run through list of variable systems -----------------
		List<String> netCompIDs = new ArrayList<String>(this.getSystemsVariability().keySet());
		for (int i = 0; i < netCompIDs.size(); i++) {
			
			String netCompID = netCompIDs.get(i);
			Vector<TechnicalSystemStateEvaluation> tsseVector = this.getSystemsVariability().get(netCompID);
			
			// --- Make a random decision here ----------------------
			int selectionIndex = GlobalInfo.getRandomInteger(0, tsseVector.size()-1);
			TechnicalSystemStateEvaluation tsseDecision = tsseVector.get(selectionIndex);
			tsd.getSystemStates().put(netCompID, tsseDecision);
		}
		
		// --- Set the DiscreteSystemStateType ----------------------
		DiscreteSystemStateType dst = DiscreteSystemStateType.Iteration;
		if (this.iterationCount>=this.iterationCountMax) {
			dst = DiscreteSystemStateType.Final;
		} 
		tsd.setDiscreteSystemStateType(dst);
		
		// --- Some debug output ------------------------------------
		if (this.isDebug==true) {
			String stateDescription = "";
			for (int i = 0; i < netCompIDs.size(); i++) {
				String netCompID = netCompIDs.get(i);
				TechnicalSystemStateEvaluation tsseDecision = tsd.getSystemStates().get(netCompID);
				if (stateDescription.isEmpty()==false) {
					stateDescription += "\n";
				}
				stateDescription += TechnicalSystemStateHelper.toString(tsseDecision, false);
			}
			// --- Print to console --------------------------------- 
			if (this.iterationCount==0) System.out.println();
			DisplayHelper.systemOutPrintlnGlobalTime(this.getEvaluationTime(), "[" + this.getClass().getSimpleName() + "]", (this.iterationCount+1) + " iteration, answered with: \n" + stateDescription);
		}
		return tsd;
	}

}
