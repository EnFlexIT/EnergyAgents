package de.enflexit.ea.core.simulation.decisionControl;

import java.util.TreeMap;
import java.util.Vector;

import org.awb.env.networkModel.NetworkModel;

import de.enflexit.common.classLoadService.BaseClassLoadServiceUtility;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class AbstractCentralDecisionProcess.
 * 
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public abstract class AbstractCentralDecisionProcess {

	private NetworkModel networkModel;

	private TreeMap<String, Vector<TechnicalSystemStateEvaluation>> systemsVariability;
	
	/**
	 * Constructor for central decision process.
	 */
	public AbstractCentralDecisionProcess() { }
	
	
	/**
	 * Return the network model.
	 * @return the network model
	 */
	public NetworkModel getNetworkModel() {
		return networkModel;
	}
	/**
	 * Sets the network model.
	 * @param networkModel the new network model
	 */
	public void setNetworkModel(NetworkModel networkModel) {
		this.networkModel = networkModel;
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
