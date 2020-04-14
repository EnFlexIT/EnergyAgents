package de.enflexit.ea.core.aggregation;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.helper.DomainCluster;
import org.awb.env.networkModel.helper.DomainClustering;

import de.enflexit.common.ServiceFinder;
import de.enflexit.common.classLoadService.BaseClassLoadServiceUtility;

/**
 * The Class DefaultSubNetworkConfigurations provides all single and an all-in-one ArrayList
 * for the so far known and proofed types of domain specific networks (e.g. electrical distribution grid).
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class DefaultSubNetworkConfigurations extends ArrayList<AbstractSubNetworkConfiguration> {

	private static final long serialVersionUID = 4268158344918811861L;
	
	private final boolean isPrintClusterResult = false;
	
	protected AbstractAggregationHandler aggregationHandler;
	private TreeMap<String, String> domainToSubNetworkConfigurationHash;

	private DomainClustering domainClustering;
	
	
	/**
	 * Instantiates the configuration of the in Agent.HyGrid known sub network configurations.
	 * @param aggregationHandler the current aggregation handler
	 */
	public DefaultSubNetworkConfigurations(AbstractAggregationHandler aggregationHandler) {
		this.aggregationHandler = aggregationHandler;
		if (this.aggregationHandler==null) {
			throw new NullPointerException("[" + this.getClass().getSimpleName() + "] The Aggregationhandler ist not allowed to be null!");
		} else {
			if (this.getNetworkModel()==null) {
				throw new NullPointerException("[" + this.getClass().getSimpleName() + "] The NetworkModel of the Aggregationhandler is not allowed to be null!");
			} else {
				this.createSubNetworkConfigurations();
			}
		}
	}

	/**
	 * Creates the sub network configurations base on the domain cluster that were determined from the current NetworkModel.
	 * 
	 * @see #getDomainClustering()
	 * @see #getNetworkModel()
	 */
	protected void createSubNetworkConfigurations() {
		
		int configIdCounter = 1;
		for (int i = 0; i < this.getDomainClustering().size(); i++) {
			DomainCluster dCluster = this.getDomainClustering().get(i);
			String configClassName = this.getDomainToSubNetworkConfigurationHash().get(dCluster.getDomain());
			if (configClassName!=null && dCluster.getNetworkComponents().size()>1) {
				
				try {
					// --- Initiate, configure and add to local data model ----
					AbstractSubNetworkConfiguration subNetworkConfiguration = (AbstractSubNetworkConfiguration) BaseClassLoadServiceUtility.newInstance(configClassName);
					subNetworkConfiguration.setID(configIdCounter);
					subNetworkConfiguration.setDomainCluster(dCluster);
					this.add(subNetworkConfiguration);
					configIdCounter++;
					
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
					System.err.println("[" + this.getClass().getSimpleName() + "] The class '" + configClassName + "' could not be initiated!");
					ex.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Returns the current NetworkModel.
	 * @return the network model
	 */
	private NetworkModel getNetworkModel() {
		return this.aggregationHandler.getNetworkModel();
	}
	
	/**
	 * Returns the NetworkModel's domain to sub network configuration hash, where the value contains the class name for a known NetworkModel domain (which is the key of the tree map).
	 * @return the domain to sub network configuration hash
	 */
	public TreeMap<String, String> getDomainToSubNetworkConfigurationHash() {
		if (domainToSubNetworkConfigurationHash==null) {
			domainToSubNetworkConfigurationHash = new TreeMap<>();

			// --- Look for available SubNetworkConfigurations ----------------
			List<SubNetworkConfigurationService> services = ServiceFinder.findServices(SubNetworkConfigurationService.class);
			for (int i=0; i<services.size(); i++) {
				SubNetworkConfigurationService service = services.get(i);
				domainToSubNetworkConfigurationHash.put(service.getDomainID(), service.getSubNetworkConfigurationCass().getName());
			}
			
		}
		return domainToSubNetworkConfigurationHash;
	}
	
	/**
	 * Returns the domain clustering that is a vector that contains the domain specific cluster of the current {@link NetworkModel}.
	 * @return the domain cluster vector
	 */
	public DomainClustering getDomainClustering() {
		if (domainClustering==null) {
			domainClustering = new DomainClustering(this.getNetworkModel(), this.isPrintClusterResult);
		}
		return domainClustering;
	}	
	
}
