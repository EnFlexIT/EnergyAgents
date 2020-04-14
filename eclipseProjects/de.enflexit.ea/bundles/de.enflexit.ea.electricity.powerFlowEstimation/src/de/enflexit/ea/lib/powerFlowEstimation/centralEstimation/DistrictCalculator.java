package de.enflexit.ea.lib.powerFlowEstimation.centralEstimation;

import de.enflexit.ea.lib.powerFlowEstimation.decentralEstimation.DistrictModel;

/**
 * 
 * @author Marcel Ludwig - EVT - University of Wuppertal (BUW)
 *
 */
public class DistrictCalculator {

	private boolean debug = true;
	private DistrictModel districtAgentModel = new DistrictModel();
	
	
	
	public DistrictModel getDistrictAgentModel() {
		return districtAgentModel;
	}
	public void setDistrictAgentModel(DistrictModel districtAgentModel) {
		this.districtAgentModel = districtAgentModel;
	}

}
