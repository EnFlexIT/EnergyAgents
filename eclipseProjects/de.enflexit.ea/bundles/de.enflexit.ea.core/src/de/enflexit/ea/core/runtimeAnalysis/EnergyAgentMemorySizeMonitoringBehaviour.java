package de.enflexit.ea.core.runtimeAnalysis;

import agentgui.simulationService.load.ObjectSizeMeasurement;
import de.enflexit.common.DateTimeHelper;
import de.enflexit.ea.core.AbstractEnergyAgent;
import jade.core.behaviours.TickerBehaviour;

/**
 * A simple diagnostic behaviour to keep track of the amount of memory an energy agent uses.
 * Prints the currently consumed amount to the console in regular intervals.
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public class EnergyAgentMemorySizeMonitoringBehaviour extends TickerBehaviour {

	private static final long serialVersionUID = 7937539253341404058L;

	/**
	 * Instantiates a new energy agent memory size monitoring behaviour.
	 * @param ea the energy agent
	 * @param interval the interval in milliseconds
	 */
	public EnergyAgentMemorySizeMonitoringBehaviour(AbstractEnergyAgent ea, long interval) {
		super(ea, interval);
	}

	/* (non-Javadoc)
	 * @see jade.core.behaviours.TickerBehaviour#onStart()
	 */
	@Override
	public void onStart() {
		System.out.println("[" + this.getClass().getSimpleName() + "] Memory monitoring started for " + this.myAgent.getClass().getSimpleName() + " " + this.myAgent.getLocalName() + ", checking every " + (this.getPeriod()/1000/60) + " minutes");
	}

	/* (non-Javadoc)
	 * @see jade.core.behaviours.TickerBehaviour#onTick()
	 */
	@Override
	protected void onTick() {
		
		// --- Start a new thread that checks an prints the memory size -------
		new Thread(new Runnable() {
			
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				AbstractEnergyAgent ea = (AbstractEnergyAgent) EnergyAgentMemorySizeMonitoringBehaviour.this.myAgent;
				String timeString = DateTimeHelper.getTimeAsString(ea.getEnergyAgentIO().getTime());
				String sizeString = ObjectSizeMeasurement.getSizeOfObjectBin(EnergyAgentMemorySizeMonitoringBehaviour.this.myAgent);
				System.out.println("[" + ea.getClass().getSimpleName() + " " + ea.getLocalName() + "] Time " + timeString + ", current size " + sizeString);
			}
		}, this.myAgent.getLocalName() + "_SizeMeasurement").start();
	}

}
