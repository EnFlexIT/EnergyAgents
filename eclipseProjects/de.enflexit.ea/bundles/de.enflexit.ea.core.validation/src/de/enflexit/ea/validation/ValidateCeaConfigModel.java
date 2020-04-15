package de.enflexit.ea.validation;

import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.ea.core.dataModel.cea.CeaConfigModel;
import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationMessage;

/**
 * Checks for CeaConfigModels based on the old class / package structure. 
 * If found, copies the data to a new instance based on the new structure.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class ValidateCeaConfigModel extends HyGridValidationAdapter {
	
	private boolean debug = true;

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.validation.HyGridValidationAdapter#validateNetworkComponent(org.awb.env.networkModel.NetworkComponent)
	 */
	@Override
	public HyGridValidationMessage validateNetworkComponent(NetworkComponent netComp) {
		
		HyGridValidationMessage vMessage = null;
		
		// --- Check for CeaConfigModels based on the old package structure ---
		if (netComp.getDataModel()!=null && netComp.getDataModel() instanceof hygrid.globalDataModel.cea.CeaConfigModel) {
			
			if (this.debug==true) {
				System.out.println("Converting CeaConfigModel for " + netComp.getId());
			}
			
			hygrid.globalDataModel.cea.CeaConfigModel configOld = (hygrid.globalDataModel.cea.CeaConfigModel) netComp.getDataModel(); 

			// --- Copy the data to the new instance --------------------------
			CeaConfigModel configNew = new CeaConfigModel();
			configNew.setStartSecondMTP(configOld.isStartSecondMTP());
			configNew.setMtpProtocol(configOld.getMtpProtocol());
			configNew.setUrlOrIp(configOld.getUrlOrIp());
			configNew.setMtpPort(configOld.getMtpPort());
			configNew.setMirrorSourceP2Repository(configOld.getMirrorSourceP2Repository());
			configNew.setMirrorDestinationP2Repository(configOld.getMirrorDestinationP2Repository());
			configNew.setMirrorProviderURLP2Repository(configOld.getMirrorProviderURLP2Repository());
			configNew.setMirrorSourceProjectRepository(configOld.getMirrorSourceProjectRepository());
			configNew.setMirrorDestinationProjectRepository(configOld.getMirrorDestinationProjectRepository());
			configNew.setMirrorProviderURLProjectRepository(configOld.getMirrorProviderURLProjectRepository());
			configNew.setMirrorInterval(configOld.getMirrorInterval());
			
			// --- Set the new instance as component data model ---------------
			netComp.setDataModel(configNew);
		}
		
		return vMessage;
	}
	
	
}
