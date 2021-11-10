package de.enflexit.ea.core.aggregation;

import java.awt.Container;
import java.util.HashMap;
import java.util.List;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.helper.DomainCluster;

import de.enflexit.common.classLoadService.BaseClassLoadServiceUtility;
import de.enflexit.ea.core.dataModel.ontology.NetworkStateInformation;
import energy.OptionModelController;
import energy.optionModel.AbstractDomainModel;
import energy.optionModel.InterfaceSetting;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalInterface;
import energy.optionModel.TechnicalInterfaceConfiguration;
import energy.optionModel.TechnicalSystem;
import energy.optionModel.TechnicalSystemGroup;
import energy.optionModel.TechnicalSystemState;
import energy.optionModel.TechnicalSystemStateTime;
import energygroup.GroupController;
import energygroup.calculation.GroupCalculation;
import jade.core.AID;

/**
 * The Class AggregationHandlerConfiguration.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public abstract class AbstractSubNetworkConfiguration {

	private AbstractAggregationHandler aggregationHandler;
	
	private int id;
	private DomainCluster domainCluster;
	
	private AbstractSubAggregationBuilder subAggregationBuilder;
	private AbstractNetworkCalculationPreprocessor netCalcPreprocessor; 
	private AbstractNetworkCalculationStrategy netCalcStrategy;
	private AbstractSubBlackboardModel subBlackboardModel;

	private AbstractNetworkModelDisplayUpdater netDisplayUpdater;
	
	private HashMap<String, Class<?>> userClasses;
	private HashMap<String, Object> userClassInstances;
	
	private Container aggregationVisualizationParentContainer;
	
	
	/**
	 * Sets the current aggregation handler.
	 * @param aggregationHandler the new aggregation handler
	 */
	public void setAggregationHandler(AbstractAggregationHandler aggregationHandler) {
		this.aggregationHandler = aggregationHandler;
	}
	/**
	 * Returns the current aggregation handler.
	 * @return the aggregation handler
	 */
	public AbstractAggregationHandler getAggregationHandler() {
		return aggregationHandler;
	}
	
	
	/**
	 * Sets the ID of the subnetwork.
	 * @param id the new id
	 */
	public void setID(int id) {
		this.id = id;
	}
	/**
	 * Return the ID of the subnetwork.
	 * @return the subnetwork ID
	 */
	public int getID() {
		return this.id;
	}
	
	/**
	 * Has to return a description for the subnetwork that describes the here defined aggregation handling.
	 * @return the subnetwork ID
	 */
	public abstract String getSubNetworkDescription();

	/**
	 * Returns the combination of ID and description for the current configuration.
	 * @return the sub network description ID
	 */
	public String getSubNetworkDescriptionID() {
		String description = this.getSubNetworkDescription();
		if (description==null || description.isEmpty()) {
			description = "Undescribed Sub-Network Description of class " + this.getClass().getSimpleName(); 
		}
		return this.getID() + ": " + description;
	}
	
	/**
	 * Gets the sub network model.
	 * @return the sub network model
	 */
	public NetworkModel getSubNetworkModel() {
		return this.getSubAggregationBuilder().getAggregationNetworkModel();
	}
	
	
	/**
	 * Sets the domain cluster to the current configuration.
	 * @param domainCluster the new domain cluster
	 */
	public void setDomainCluster(DomainCluster domainCluster) {
		this.domainCluster = domainCluster;
	}
	/**
	 * Returns the DomainCluster of the current configuration.
	 * @return the domain cluster
	 */
	public DomainCluster getDomainCluster() {
		return domainCluster;
	}
	/**
	 * Return the domain of the current configuration.
	 * @return the domain
	 */
	public String getDomain() {
		if (this.getDomainCluster()==null || this.getDomainCluster().getDomain()==null || this.getDomainCluster().getDomain().isEmpty()) {
			return null;
		}
		return this.getDomainCluster().getDomain();
	}

	/**
	 * Has to check, if the specified parameter match with the pattern defined with this sub network configuration.
	 *
	 * @param domain the domain
	 * @param domainModel the actual domain model
	 * @return true, if is part of subnetwork
	 */
	public abstract boolean isPartOfSubnetwork(String domain, AbstractDomainModel domainModel);
	
	// --------------------------------------------------------------------------------------------
	// --- From here, some help methods for identifying sub elements of a network can be found ---- 
	// --------------------------------------------------------------------------------------------	
	/**
	 * Checks if the specified NetworkComponent is part of the subnetwork.
	 *
	 * @param netComp the NetworkComponent 
	 * @return true, if is part of subnetwork
	 */
	public boolean isPartOfSubnetwork(NetworkComponent netComp) {
		
		if (netComp==null) return false;
		
		if (netComp.getDataModel() instanceof TechnicalSystemGroup) {
			return this.isPartOfSubNetwork((TechnicalSystemGroup) netComp.getDataModel());
		} else if (netComp.getDataModel() instanceof TechnicalSystem) {
			return this.isPartOfSubNetwork((TechnicalSystem) netComp.getDataModel());
		} else if (netComp.getDataModel() instanceof ScheduleList) {
			return this.isPartOfSubNetwork((ScheduleList) netComp.getDataModel());
		}
		return false;
	}
	/**
	 * Checks if the specified TechnicalSystemGroup is part of the subnetwork.
	 *
	 * @param tsg the TechnicalSystemGroup
	 * @return true, if is part of sub network
	 */
	public boolean isPartOfSubNetwork(TechnicalSystemGroup tsg) {
		if (tsg==null) return false;
		return this.isPartOfSubNetwork(tsg.getTechnicalSystem());
	}
	/**
	 * Checks if the specified TechnicalSystem is part of the subnetwork.
	 *
	 * @param ts the TechnicalSystem to check
	 * @return true, if is part of sub network
	 */
	public boolean isPartOfSubNetwork(TechnicalSystem ts) {

		if (ts==null) return false;
		
		// --- Check the configured initial evaluation state --------
		TechnicalInterfaceConfiguration tic = null;
		if (ts.getEvaluationSettings().getEvaluationStateList().size()!=0) {
			TechnicalSystemStateTime tsst = ts.getEvaluationSettings().getEvaluationStateList().get(0);
			if (tsst instanceof TechnicalSystemState) {
				TechnicalSystemState tss = (TechnicalSystemState) tsst;
				if (tss.getConfigID()!=null) {
					OptionModelController omc = new OptionModelController();
					omc.setTechnicalSystem(ts);
					tic = omc.getTechnicalInterfaceConfiguration(tss.getConfigID());
				}
			} 
		}
		
		// --- Backup solution ---------------
		if (tic==null) {
			if (ts.getInterfaceConfigurations().size()>0) {
				tic = ts.getInterfaceConfigurations().get(0);
			}
		}
		
		if (tic!=null) {
			return this.isPartOfSubnetwork(tic.getTechnicalInterfaces());
		}
		return false;
	}
	/**
	 * Checks if the specified ScheduleList is part of the subnetwork.
	 *
	 * @param sl the ScheduleList
	 * @return true, if is part of sub network
	 */
	public boolean isPartOfSubNetwork(ScheduleList sl) {
		
		if (sl==null) return false;

		for (int i = 0; i < sl.getInterfaceSettings().size(); i++) {
			InterfaceSetting intSet = sl.getInterfaceSettings().get(i);
			if (intSet!=null) {
				boolean isPart = this.isPartOfSubnetwork(intSet.getDomain(), intSet.getDomainModel());
				if (isPart==true) return true;
			}
		}
		return false;
	}
	/**
	 * Checks if one of the specified {@link TechnicalInterface} instances matches for the current subnetwork configuration.
	 *
	 * @param technicalInterfaces the technical interfaces
	 * @return true, if is part of subnetwork
	 */
	public boolean isPartOfSubnetwork(List<TechnicalInterface> technicalInterfaces) {
		
		for (int i = 0; i < technicalInterfaces.size(); i++) {
			TechnicalInterface ti =  technicalInterfaces.get(i);
			boolean isPart = this.isPartOfSubnetwork(ti.getDomain(), ti.getDomainModel());
			if (isPart==true) return true;
		}
		return false;
	};
	
	// --------------------------------------------------------------------------------------------
	// --- From here, further abstract methods are specified -------------------------------------- 
	// --------------------------------------------------------------------------------------------	
	
	
	// ------------------------------------------------------------------------
	// --- Handling of the sub aggregation builder ----------------------------
	// ------------------------------------------------------------------------
	/**
	 * Has to return the class that build the sub aggregation for the current sub aggregation.
	 * @return the sub aggregation builder class
	 */
	public abstract Class<? extends AbstractSubAggregationBuilder> getSubAggregationBuilderClass();
	/**
	 * Gets the network calculation strategy class internal.
	 * @return the network calculation strategy class internal
	 */
	private final Class<? extends AbstractSubAggregationBuilder> getSubAggregationBuilderClassInternal() {
		
		// --- Get the specified class ------------------------------
		Class<? extends AbstractSubAggregationBuilder> calculationStrategyClass = this.getSubAggregationBuilderClass();
		// --- If nothing is specified, use the default -------------
		if (calculationStrategyClass == null) {
			calculationStrategyClass = DefaultSubAggregationBuilder.class;
		}
		return calculationStrategyClass;
	}
	/**
	 * Returns the sub aggregation builder for the current sub network. Therefore, the configured class of the
	 * type {@link AbstractSubAggregationBuilder} is used. If the corresponding class in the individual configuration
	 * returns null <code>null</code>, the {@link DefaultSubAggregationBuilder} is used that will build
	 * an EOM aggregation (a {@link TechnicalSystemGroup}) based on a {@link TechnicalSystem} as root, while 
	 * sub systems will be represented as {@link ScheduleList}'s for a static representation of single system 
	 * behaviour over time.
	 * 
	 * @see #getSubAggregationBuilderClass()
	 *  
	 * @return the aggregation builder
	 */
	public final AbstractSubAggregationBuilder getSubAggregationBuilder() {
		if (subAggregationBuilder==null) {
			// --- Get the class --------------------------
			Class<? extends AbstractSubAggregationBuilder> builderClass = this.getSubAggregationBuilderClassInternal();
			subAggregationBuilder = this.getNewInstance(builderClass);
			if (subAggregationBuilder!=null) {
				subAggregationBuilder.setAggregationHandler(this.getAggregationHandler());
				subAggregationBuilder.setSubAggregationConfiguration(this);
			}
		}
		return subAggregationBuilder;
	}
	
	
	// ------------------------------------------------------------------------
	// --- Handling of the sub aggregation visualization ----------------------
	// ------------------------------------------------------------------------
	/**
	 * Gets the aggregation visualization parent container.
	 * @return the aggregation visualization parent container
	 */
	public Container getAggregationVisualizationParentContainer() {
		return aggregationVisualizationParentContainer;
	}
	/**
	 * Sets the aggregation visualization parent container.
	 * @param aggregationVisualizationParentContainer the new aggregation visualization parent container
	 */
	public void setAggregationVisualizationParentContainer(Container aggregationVisualizationParentContainer) {
		this.aggregationVisualizationParentContainer = aggregationVisualizationParentContainer;
	}
	// ------------------------------------------------------------------------
	// --- Handling of the NetworkCalculationPreprocessor ---------------------
	// ------------------------------------------------------------------------
	/**
	 * Has to return the network calculation preprocessor class.
	 * @return the network calculation preprocessor class (may be null)
	 */
	public abstract Class<? extends AbstractNetworkCalculationPreprocessor> getNetworkCalculationPreprocessorClass();
	
	/**
	 * Gets the network calculation preprocessor class internal.
	 * @return the network calculation preprocessor class internal
	 */
	private final Class<? extends AbstractNetworkCalculationPreprocessor> getNetworkCalculationPreprocessorClassInternal(){
		return this.getNetworkCalculationPreprocessorClass();
	}
	/**
	 * Gets the network calculation preprocessor.
	 * @return the network calculation preprocessor
	 */
	public AbstractNetworkCalculationPreprocessor getNetworkCalculationPreprocessor() {
		if (netCalcPreprocessor==null) {
			Class<? extends AbstractNetworkCalculationPreprocessor> netCaclPrepoc = this.getNetworkCalculationPreprocessorClassInternal();
			if (netCaclPrepoc!=null) {
				netCalcPreprocessor = this.getNewInstance(netCaclPrepoc);
				if (netCalcPreprocessor!=null) {
					netCalcPreprocessor.setAggregationHandler(this.getAggregationHandler());
					netCalcPreprocessor.setSubAggregationConfiguration(this);
				}
			}
		}
		return netCalcPreprocessor;
	}
	
	
	// ------------------------------------------------------------------------
	// --- Handling of the NetworkCalculationStrategy -------------------------
	// ------------------------------------------------------------------------
	/**
	 * Has to returns the configured network calculation strategy class. 
	 * @return the network calculation strategy class
	 */
	public abstract Class<? extends AbstractNetworkCalculationStrategy> getNetworkCalculationStrategyClass();
	/**
	 * Gets the network calculation strategy class internal.
	 * @return the network calculation strategy class internal
	 */
	private final Class<? extends AbstractNetworkCalculationStrategy> getNetworkCalculationStrategyClassInternal(){
		
		// --- Get the specified class ------------------------------
		Class<? extends AbstractNetworkCalculationStrategy> calculationStrategyClass = this.getNetworkCalculationStrategyClass();
		return calculationStrategyClass;
	}
	/**
	 * Gets the network calculation strategy.
	 * @return the network calculation strategy
	 */
	public final AbstractNetworkCalculationStrategy getNetworkCalculationStrategy() {
		if (netCalcStrategy==null) {
			try {
				// --- Get and configure the NetworkCalculationStrategy -----------------
				GroupController gc = this.getSubAggregationBuilder().getGroupController();
				// --- Ensure that the GroupCalculation was set as calculation class ----
				gc.getTechnicalSystemGroup().getTechnicalSystem().setCalculationClass(GroupCalculation.class.getName());
				GroupCalculation groupCalc = (GroupCalculation) gc.getGroupOptionModelController().getOptionModelCalculation(); 
				// --- Initiate the network calculation strategy ------------------------
				Class<? extends AbstractNetworkCalculationStrategy> netCalcStratClass = this.getNetworkCalculationStrategyClassInternal();
				if (netCalcStratClass!=null) {
					netCalcStrategy = (AbstractNetworkCalculationStrategy) gc.getGroupOptionModelController().createEvaluationStrategy(netCalcStratClass.getName());
					if (netCalcStrategy!=null) {
						groupCalc.setGroupEvaluationStrategy(netCalcStrategy);
						// --- Set further instance -------------------------------------
						netCalcStrategy.setAggregationHandler(this.getAggregationHandler());
						netCalcStrategy.setSubNetworkConfiguration(this);
						// --- Activate the performance measurements? -------------------
						if (this.aggregationHandler.debugIsDoPerformanceMeasurements()==true) {
							netCalcStrategy.setPerformanceMeasurementAverageBase(this.aggregationHandler.debugGetMaxNumberForPerformanceAverage());
							netCalcStrategy.setPerformanceMeasurementNameAddition(this.getID() + "");
							netCalcStrategy.setDoPerformanceMeasurement(true);
						}
					}
				}
				
			} catch (ClassCastException ccEx) {
				ccEx.printStackTrace();
			}
		}
		return netCalcStrategy;
	}
	
	
	// ------------------------------------------------------------------------
	// --- Handling of the sub blackboard model -------------------------------
	// ------------------------------------------------------------------------
	/**
	 * Returns the class that implements the sub blackboard model for the current sub aggregation.
	 * @return the sub blackboard model class
	 */
	public abstract Class<? extends AbstractSubBlackboardModel> getSubBlackboardModelClass();
	/**
	 * Gets the network calculation strategy class internal.
	 * @return the network calculation strategy class internal
	 */
	private final Class<? extends AbstractSubBlackboardModel> getSubBlackboardModelClassInternal() {
		
		// --- Get the specified class ------------------------------
		Class<? extends AbstractSubBlackboardModel> subBlackboardModelClass = this.getSubBlackboardModelClass();
		return subBlackboardModelClass;
	}
	/**
	 * Returns the sub blackboard model for the current sub network. Therefore, the configured class of the
	 * type {@link AbstractSubBlackboardModel} is used.
	 * 
	 * @see #getSubBlackboardModelClass()
	 *  
	 * @return the aggregation builder
	 */
	public final AbstractSubBlackboardModel getSubBlackboardModel() {
		if (subBlackboardModel==null) {
			// --- Get the class --------------------------
			Class<? extends AbstractSubBlackboardModel> blackboardModelClass = this.getSubBlackboardModelClassInternal();
			if (blackboardModelClass!=null) {
				subBlackboardModel = this.getNewInstance(blackboardModelClass);
				if (subBlackboardModel!=null) {
					subBlackboardModel.setAggregationHandler(this.getAggregationHandler());
					subBlackboardModel.setSubAggregationConfiguration(this);
				}
			}
		}
		return subBlackboardModel;
	}
	
	
	// ------------------------------------------------------------------------
	// --- Handling of the NetworkDisplayUpdater ------------------------------
	// ------------------------------------------------------------------------
	/**
	 * Has to returns the configured network display updater class. 
	 * @return the network calculation strategy class
	 */
	public abstract Class<? extends AbstractNetworkModelDisplayUpdater> getNetworkDisplayUpdaterClass();
	/**
	 * Gets the network calculation strategy class internal.
	 * @return the network calculation strategy class internal
	 */
	private final Class<? extends AbstractNetworkModelDisplayUpdater> getNetworkDisplayUpdaterClassInternal(){
		return this.getNetworkDisplayUpdaterClass();
	}
	/**
	 * Gets the network calculation strategy.
	 * @return the network calculation strategy
	 */
	public final AbstractNetworkModelDisplayUpdater getNetworkDisplayUpdater() {
		if (netDisplayUpdater==null) {
			Class<? extends AbstractNetworkModelDisplayUpdater> netDisUp = this.getNetworkDisplayUpdaterClassInternal();
			if (netDisUp!=null) {
				netDisplayUpdater = this.getNewInstance(netDisUp);
				if (netDisplayUpdater!=null) {
					netDisplayUpdater.setAggregationHandler(this.getAggregationHandler());
					netDisplayUpdater.setSubAggregationConfiguration(this);
				}
			}
		}
		return netDisplayUpdater;
	}
		
	
	// ------------------------------------------------------------------------
	// --- Handling of the user defined classes -------------------------------
	// ------------------------------------------------------------------------
	/**
	 * Has to return a HashMap of individual classes that are to be used within an aggregation handler.
	 * Each user class will be identified by an string identifier (e.g. <i>"PowerFlowCalculation"=net.energy.agent.MyPowerFlowCalculation</i>)  
	 * @return the user classes (can return <code>null</code>)
	 */
	public abstract HashMap<String, Class<?>> getUserClasses();
	/**
	 * Internally returns the user classes.
	 * @return the user classes internal
	 */
	private final HashMap<String, Class<?>> getUserClassesInternal() {
		if (userClasses==null) {
			userClasses = this.getUserClasses();
		}
		return userClasses;
	}
	/**
	 * Returns the specified user class instance if a corresponding class was configured.
	 * Each user class will only be create once. That means that a repeatable request to 
	 * this method will return the instance that was previously initiated. 
	 *
	 * @param classQualifier the class qualifier (e.g. "PowerFlowCalculation")
	 * @return the user class instance
	 */
	public final Object getUserClassInstance(String classQualifier) {
		
		Object userClassInstance = this.getUserClassInstances().get(classQualifier);
		if (userClassInstance==null) {
			// --- Class was not initiated yet ----------------------
			HashMap<String, Class<?>> userClasses = this.getUserClassesInternal();
			if (userClasses!=null) {
				// --- Try to get the class -------------------------
				Class<?> userClass = userClasses.get(classQualifier);
				if (userClass!=null) {
					userClassInstance = this.getNewInstance(userClass);
				}
			}
		}
		return userClassInstance;
	}
	/**
	 * Returns the currently initiated user class instances.
	 * @return the user class instances
	 */
	private HashMap<String, Object> getUserClassInstances() {
		if (userClassInstances==null) {
			userClassInstances = new HashMap<>();
		}
		return userClassInstances;
	}

	/**
	 * Returns a new instance of the specified class.
	 *
	 * @param <T> the generic type
	 * @param classToInitiate the class to initiate
	 * @return the new instance
	 */
	private <T> T getNewInstance(Class<T> classToInitiate) {
		
		T newInstance=null;
		try {
			newInstance = classToInitiate.newInstance();
		} catch (InstantiationException | IllegalAccessException ex) {
			//e.printStackTrace();
		}
		// --- Try to get the instance by using the class load service --------
		if (newInstance==null) {
			newInstance = this.getNewInstanceByClassLoadService(classToInitiate);
		}
		return newInstance;
	}
	
	/**
	 * Returns a new instance of the specified class by using the class load service.
	 *
	 * @param <T> the generic type
	 * @param classToInitiate the class to initiate
	 * @return the new instance by class load service
	 */
	@SuppressWarnings("unchecked")
	private <T> T getNewInstanceByClassLoadService(Class<T> classToInitiate) {
		
		T newInstance=null;
		try {
			newInstance = (T) BaseClassLoadServiceUtility.newInstance(classToInitiate.getName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return newInstance;	
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " for " + this.getSubNetworkDescriptionID(); 
	}
	
	/**
	 * Handle domain-specific network state information notifications
	 * @param sender the sender
	 * @param networkStateInformation the network state information
	 * @return true, if processed by this subNetworkConfiguration
	 */
	public abstract boolean onNetworkStateInformation(AID sender, NetworkStateInformation networkStateInformation);
	
}
