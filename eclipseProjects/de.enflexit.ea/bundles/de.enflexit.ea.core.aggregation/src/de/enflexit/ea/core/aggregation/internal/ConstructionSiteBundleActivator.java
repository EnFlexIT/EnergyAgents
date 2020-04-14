package de.enflexit.ea.core.aggregation.internal;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.helper.DomainCluster;
import org.awb.env.networkModel.helper.DomainClustering;
import org.osgi.framework.BundleContext;

import de.enflexit.ea.core.aggregation.AbstractAggregationHandler;
import hygrid.plugin.JButtonConstructionSite;

/**
 * The Class ConstructionSiteBundleActivator.
 */
public class ConstructionSiteBundleActivator implements org.osgi.framework.BundleActivator, ActionListener {

	private boolean activateConstrutionSite = true;
	
	// Set to true if aggregation building should be tested
	private boolean aggregationsEnabled = false;
	// Set to true if the network model should be checked for domain clusters
	private boolean clusterCheckEnabled = true;
	
	private AbstractAggregationHandler aggregationHandler;
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		
//		System.err.println("[" + this.getClass().getSimpleName() + "] actionPerformed( xxx ) ... ! ");
		
		if (this.aggregationsEnabled==true) {
			this.startAggregationBuilder();
		}
		if (this.clusterCheckEnabled==true) {
			this.checkForDomainClusters();
		}
		
	}
	
	/**
	 * Start aggregation builder.
	 */
	private void startAggregationBuilder() {
		new Thread(new Runnable() {
			
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				// --- Terminate current instance of the AggregationHandler ----------- 
				ConstructionSiteBundleActivator.this.terminateAggregationHandler();
				// --- Build new AggregationHandler -----------------------------------
				ConstructionSiteBundleActivator.this.getAggregationHandler();
				
			}
		}, this.getClass().getSimpleName() + "Thread").start();
	}

	/**
	 * Returns the aggregation display behaviour.
	 * @return the display behaviour
	 */
	private AbstractAggregationHandler getAggregationHandler() {
		if (aggregationHandler==null) {
			aggregationHandler = new ConstructionSiteAggregationHandler(this.getNetworkModel(), false, this.getClass().getSimpleName());
			aggregationHandler.setOwnerInstance(this);
		}
		return aggregationHandler;
	}
	/**
	 * Terminates the current AggregationHandler.
	 */
	private void terminateAggregationHandler() {
		if (aggregationHandler!=null) {
			aggregationHandler.terminate();
			aggregationHandler = null;
		}
	}
	
	/**
	 * Checks for domain clusters.
	 */
	private void checkForDomainClusters() {
		boolean showMainClusterDetails = false;
		DomainClustering dc = new DomainClustering(getNetworkModel(), true);
		for (int i=0; i<dc.size(); i++) {
			if (i>0 || showMainClusterDetails==true) {
				System.out.println("Cluster " + i);
				DomainCluster cluster = dc.get(i);
				for (int j=0; j<cluster.getNetworkComponents().size(); j++) {
					System.out.println(cluster.getNetworkComponents().get(j).getId());
				}
			}
		}
	}

	/**
	 * Return the current network model.
	 * @return the network model
	 */
	private NetworkModel getNetworkModel() {
		return JButtonConstructionSite.getGraphEnvironmentController().getNetworkModel();
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		System.err.println("[" + this.getClass().getSimpleName() + "] Start Bundle " + context.getBundle().getSymbolicName());
		if (activateConstrutionSite==true) {
			System.err.println("[" + this.getClass().getSimpleName() + "] Added action listener to JButtonConstructionSite!");
			JButtonConstructionSite.addActionListener(this);
			
		} else {
			System.err.println("[" + this.getClass().getSimpleName() + "] Consider to remove this bundle activator!");
		}
	}
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {	
		
		System.err.println("[" + this.getClass().getSimpleName() + "] Stopping Bundle " + context.getBundle().getSymbolicName());
		
	}

}
