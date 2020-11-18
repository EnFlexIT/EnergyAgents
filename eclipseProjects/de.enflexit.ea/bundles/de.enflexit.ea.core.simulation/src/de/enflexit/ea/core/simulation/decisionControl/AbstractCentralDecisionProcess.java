package de.enflexit.ea.core.simulation.decisionControl;

import java.util.TreeMap;
import java.util.Vector;

import de.enflexit.common.classLoadService.BaseClassLoadServiceUtility;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.SnapshotDecisionLocation;
import de.enflexit.ea.core.simulation.manager.AggregationHandler;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class AbstractCentralDecisionProcess.
 * 
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public abstract class AbstractCentralDecisionProcess {

	private AggregationHandler aggregationHandler;

	private TreeMap<String, Vector<TechnicalSystemStateEvaluation>> systemsVariability;
	
	/**
	 * Constructor for central decision process.
	 */
	public AbstractCentralDecisionProcess() { }
	
	
	/**
	 * Sets the aggregation handler.
	 * @param aggregationHandler the new aggregation handler
	 */
	public void setAggregationHandler(AggregationHandler aggregationHandler) {
		this.aggregationHandler = aggregationHandler;
	}
	/**
	 * Returns the current aggregation handler.
	 * @return the aggregation handler
	 */
	public AggregationHandler getAggregationHandler() {
		return aggregationHandler;
	}
	
	/**
	 * Return the TreeMap with the involved systems variability.
	 * @return the systems variability
	 */
	public TreeMap<String, Vector<TechnicalSystemStateEvaluation>> getSystemsVariability() {
		if (systemsVariability==null) {
			systemsVariability = new TreeMap<String, Vector<TechnicalSystemStateEvaluation>>();
		}
		return systemsVariability;
	}
	
	/**
	 * Executes the central decision process.
	 */
	public void executeDecisionProcess() {
		// TODO Auto-generated method stub
		System.out.println("Execute central decision process ....");
		
	}
	
	
	// ----------------------------------------------------------------------------------
	// --- From here, static help methods central decisions are located -----------------
	// ----------------------------------------------------------------------------------
	/**
	 * Checks if the specified HyGridAbstractEnvironmentModel specifies a central controlled snapshot simulation.
	 *
	 * @param hyGridAbsEnvModel the {@link HyGridAbstractEnvironmentModel} to check
	 * @return true, if is central controlled snapshot simulation
	 */
	public static boolean isCentralControlledSnapshotSimulation(HyGridAbstractEnvironmentModel hyGridAbsEnvModel) {
		
		boolean centralControlledSnapshotSimulation = false;
		if (hyGridAbsEnvModel!=null) {
			boolean isSnapshot = hyGridAbsEnvModel.isDiscreteSnapshotSimulation();
			boolean isSnapshotCentral = hyGridAbsEnvModel.getSnapshotDecisionLocation()==SnapshotDecisionLocation.Central;
			boolean isSnapshotCentralClassAvailable = hyGridAbsEnvModel.getSnapshotCentralDecisionClass()!=null;
			centralControlledSnapshotSimulation = isSnapshot && isSnapshotCentral && isSnapshotCentralClassAvailable;
		} else {
			centralControlledSnapshotSimulation = false;
		}
		return centralControlledSnapshotSimulation;
	}
	
	/**
	 * Creates the specified central decision process.
	 *
	 * @param decisionProcessClassName the decision process class name
	 * @return the abstract central decision process
	 */
	public static AbstractCentralDecisionProcess createCentralDecisionProcess(String decisionProcessClassName) {
		
		AbstractCentralDecisionProcess cdp = null;
		if (decisionProcessClassName!=null && decisionProcessClassName.isEmpty()==false) {
			// --- Initiate this class --------------------------				
			try {
				cdp = (AbstractCentralDecisionProcess) BaseClassLoadServiceUtility.newInstance(decisionProcessClassName);
				
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return cdp;
	}
	
}
