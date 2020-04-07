package de.enflexit.energyAgent.ops.classLoadService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import energy.OptionModelController;
import energy.calculations.AbstractOptionModelCalculation;
import energy.classLoadService.EomClassLoadService;
import energy.evaluation.AbstractEvaluationStrategy;
import energy.optionModel.gui.sysVariables.AbstractStaticModel;
import jade.content.onto.Ontology;

/**
 * The Class EomClassLoadServiceImpl represents the .
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg - Essen
 */
public class EomClassLoadServiceImpl implements EomClassLoadService {

	
	/* (non-Javadoc)
	 * @see de.enflexit.common.classLoadService.BaseClassLoadService#forName(java.lang.String)
	 */
	@Override
	public Class<?> forName(String className) throws ClassNotFoundException, NoClassDefFoundError {
		return Class.forName(className);
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.common.classLoadService.BaseClassLoadService#newInstance(java.lang.String)
	 */
	@Override
	public Object newInstance(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		return this.forName(className).newInstance();
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.common.classLoadService.BaseClassLoadService#getOntologyInstance(java.lang.String)
	 */
	@Override
	public Ontology getOntologyInstance(String ontologyClassName) throws ClassNotFoundException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		Class<?> currOntoClass = Class.forName(ontologyClassName);
		Method method = currOntoClass.getMethod("getInstance", new Class[0]);
		return (Ontology) method.invoke(currOntoClass, new Object[0]);
	}
	
	/* (non-Javadoc)
	 * @see energy.classLoadService.EomClassLoadService#getStaticModelInstance(java.lang.String, energy.OptionModelController)
	 */
	@Override
	public AbstractStaticModel getStaticModelInstance(String staticModelClassName, OptionModelController omc) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		@SuppressWarnings("unchecked")
		Class<? extends AbstractStaticModel> asmClass = (Class<? extends AbstractStaticModel>) Class.forName(staticModelClassName);
		return asmClass.getDeclaredConstructor( new Class[] { OptionModelController.class }).newInstance( new Object[] {omc});
	}

	/* (non-Javadoc)
	 * @see energy.classLoadService.EomClassLoadService#getOptionModelCalculationInstance(java.lang.String, energy.OptionModelController)
	 */
	@Override
	public AbstractOptionModelCalculation getOptionModelCalculationInstance(String omcClassName, OptionModelController omc) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		@SuppressWarnings("unchecked")
		Class<? extends AbstractOptionModelCalculation> omcClass = (Class<? extends AbstractOptionModelCalculation>) Class.forName(omcClassName);
		return omcClass.getDeclaredConstructor( new Class[] { OptionModelController.class }).newInstance( new Object[] {omc});
	}

	/* (non-Javadoc)
	 * @see energy.classLoadService.EomClassLoadService#getEvaluationStrategyInstance(java.lang.String, energy.OptionModelController)
	 */
	@Override
	public AbstractEvaluationStrategy getEvaluationStrategyInstance(String strategyClassName, OptionModelController omc) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		@SuppressWarnings("unchecked")
		Class<? extends AbstractEvaluationStrategy> omcClass = (Class<? extends AbstractEvaluationStrategy>) Class.forName(strategyClassName);
		return omcClass.getDeclaredConstructor( new Class[] {OptionModelController.class}).newInstance( new Object[] {omc});
	}

}
