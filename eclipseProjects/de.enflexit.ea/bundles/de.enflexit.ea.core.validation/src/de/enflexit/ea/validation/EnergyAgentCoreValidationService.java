package de.enflexit.ea.validation;

import java.util.ArrayList;

import de.enflexit.common.ontology.OntologyMapper;
import de.enflexit.common.ontology.OntologyMapping;
import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationService;

/**
 * The Class HygridValidationService provides specific setup tests.
 */
public class EnergyAgentCoreValidationService implements HyGridValidationService {

	private OntologyMapping hygridOntologyMapping;
	
	/**
	 * Instantiates a new hy grid validation service impl.
	 */
	public EnergyAgentCoreValidationService() {
		// --- Define the OntologyMapping for the HyGridOntolgy ------------------------- 
		OntologyMapper.registerOntologyMapping(this.getHygridOntologyMapping());
	}
	
	/* (non-Javadoc)
	 * @see net.agenthygrid.core.validation.HyGridValidationService#getHyGridValidationChecks(boolean)
	 */
	@Override
	public ArrayList<HyGridValidationAdapter> getHyGridValidationChecks(boolean isHeadlessOperation) {
		
		
		// --- Provide the list of individual checks for this bundle components ---------
		ArrayList<HyGridValidationAdapter> checkList = new ArrayList<>();
		checkList.add(new ValidateSwitchableEomAdapter());
		checkList.add(new ValidateCable());
		checkList.add(new ValidateCeaConfigModel());
		return checkList;
	}

	/**
	 * Gets the ontology mapping for the HyGridOntolgy.
	 * @return the ontology mapping
	 */
	private OntologyMapping getHygridOntologyMapping() {
		if (hygridOntologyMapping==null) {
			hygridOntologyMapping = new OntologyMapping("hygrid.globalDataModel.ontology", "de.enflexit.ea.core.globalDataModel.ontology");
		}
		return hygridOntologyMapping;
	}
	
}
