package de.enflexit.ea.core.simulation.classFilter;

import java.util.Vector;

import de.enflexit.awb.core.bundleEvaluation.BundleClassFilterService;
import de.enflexit.common.bundleEvaluation.AbstractBundleClassFilter;

/**
 * The Class BundleClassFilterDefinifion provides the class filter definitions
 * that are required by the simulation manager.
 * 
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class BundleClassFilterDefinifion implements BundleClassFilterService {

	private Vector<AbstractBundleClassFilter> bundleClassFilter;
	
	/* (non-Javadoc)
	 * @see org.agentgui.bundle.evaluation.BundleClassFilterService#getVectorOfBundleClassFilter()
	 */
	@Override
	public Vector<AbstractBundleClassFilter> getVectorOfBundleClassFilter() {
		if (bundleClassFilter==null) {
			bundleClassFilter = new Vector<>();
			bundleClassFilter.add(new FilterForCentralDecisonProcess());
		}
		return bundleClassFilter;
	}

}
