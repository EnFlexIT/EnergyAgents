package de.enflexit.ea.core.simulation.classFilter;

import java.lang.reflect.Modifier;

import de.enflexit.common.bundleEvaluation.AbstractBundleClassFilter;
import de.enflexit.ea.core.simulation.decisionControl.AbstractCentralDecisionProcess;

/**
 * The Filter for {@link AbstractBundleClassFilter} classes that are to be used as static data models.
 * 
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class FilterForCentralDecisonProcess extends AbstractBundleClassFilter {

	private Class<AbstractCentralDecisionProcess> filterclass = AbstractCentralDecisionProcess.class;
	
	/* (non-Javadoc)
	 * @see de.enflexit.common.bundleEvaluation.AbstractBundleClassFilter#getFilterScope()
	 */
	@Override
	public String getFilterScope() {
		return this.filterclass.getName();
	}

	/* (non-Javadoc)
	 * @see de.enflexit.common.bundleEvaluation.AbstractBundleClassFilter#isFilterCriteria(java.lang.Class)
	 */
	@Override
	public boolean isFilterCriteria(Class<?> clazz) {
		return (clazz==this.filterclass);
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.common.bundleEvaluation.AbstractBundleClassFilter#isInFilterScope(java.lang.Class)
	 */
	@Override
	public boolean isInFilterScope(Class<?> clazz) {
		return this.filterclass.isAssignableFrom(clazz) && this.filterclass.equals(clazz)==false && Modifier.isAbstract(clazz.getModifiers())==false;
	}

}
