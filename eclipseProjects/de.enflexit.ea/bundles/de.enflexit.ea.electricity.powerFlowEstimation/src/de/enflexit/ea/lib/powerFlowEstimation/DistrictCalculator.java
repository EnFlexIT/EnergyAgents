package de.enflexit.ea.lib.powerFlowEstimation;

/**
 * 
 * @author Marcel Ludwig - EVT - University of Wuppertal (BUW)
 */
public class DistrictCalculator {

	private DistrictModel districtAgentModel = new DistrictModel();
	
	
	public DistrictModel getDistrictAgentModel() {
		return districtAgentModel;
	}
	public void setDistrictAgentModel(DistrictModel districtAgentModel) {
		this.districtAgentModel = districtAgentModel;
	}

}
