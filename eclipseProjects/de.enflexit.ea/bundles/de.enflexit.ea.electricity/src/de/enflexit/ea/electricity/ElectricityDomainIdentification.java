package de.enflexit.ea.electricity;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.settings.DomainSettings;
import org.awb.env.networkModel.settings.GeneralGraphSettings4MAS;
import de.enflexit.awb.core.Application;
import de.enflexit.awb.core.environment.EnvironmentController;
import de.enflexit.awb.core.project.Project;
import de.enflexit.ea.core.aggregation.SubNetworkConfigurationService;
import de.enflexit.ea.core.awbIntegration.adapter.triPhase.TriPhaseElectricalNodeAdapter;
import de.enflexit.ea.core.awbIntegration.adapter.uniPhase.UniPhaseElectricalNodeAdapter;
import de.enflexit.ea.core.dataModel.GlobalHyGridConstants.ElectricityNetworkType;
import de.enflexit.ea.core.dataModel.graphLayout.GraphElementLayoutService;
import de.enflexit.ea.electricity.aggregation.triPhase.TriPhaseElectricalNetworkGraphElementLayoutService;
import de.enflexit.ea.electricity.aggregation.triPhase.TriPhaseElectricalSubNetworkConfigurationService;
import de.enflexit.ea.electricity.aggregation.uniPhase.UniPhaseElectricalNetworkGraphElementLayoutService;
import de.enflexit.ea.electricity.aggregation.uniPhase.UniPhaseElectricalSubNetworkConfigurationService;

/**
 * The Class ElectricityDomainIdentification determines the configured domains within a
 * NetworkModel and assigns them to the corresponding class that implements
 * {@link SubNetworkConfigurationService}. Within the local bundle, the classes
 * {@link TriPhaseElectricalSubNetworkConfigurationService} and
 * {@link UniPhaseElectricalSubNetworkConfigurationService} are using this
 * process.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class ElectricityDomainIdentification {

	private static Project currProject;
	
	/**
	 * Returns the domain list for the specified SubNetworkConfigurationService.
	 *
	 * @param subNetConfigService the SubNetworkConfigurationService
	 * @return the domain id list
	 */
	public static List<String> getDomainList(SubNetworkConfigurationService subNetConfigService) {

		List<String> domainIdList = new ArrayList<>();
		
		// --- Get the current domain settings ------------
		TreeMap<String, DomainSettings> dsTreeMap = getDomainSettings();
		
		List<String> domainList = new ArrayList<>(dsTreeMap.keySet());
		for (String domain : domainList) {
			DomainSettings ds = dsTreeMap.get(domain);
			String adapterClass = ds.getAdapterClass();
			if (adapterClass==null || adapterClass.isEmpty()==true) continue;
			
			if (subNetConfigService instanceof TriPhaseElectricalSubNetworkConfigurationService) {
				if (adapterClass.equals(TriPhaseElectricalNodeAdapter.class.getName())) {
					domainIdList.add(domain);
				}
			} else if (subNetConfigService instanceof UniPhaseElectricalSubNetworkConfigurationService) {
				if (adapterClass.equals(UniPhaseElectricalNodeAdapter.class.getName())) {
					domainIdList.add(domain);
				}
			}
		}
		return domainIdList;
	}
	
	/**
	 * Returns the domain list for the specified GraphElementLayoutService.
	 *
	 * @param graphElementLayoutService the graph element layout service
	 * @return the domain id list
	 */
	public static List<String> getDomainList(GraphElementLayoutService graphElementLayoutService) {
		return getDomainList(graphElementLayoutService, null);
	}
	/**
	 * Returns the domain list for the specified GraphElementLayoutService.
	 *
	 * @param graphElementLayoutService the graph element layout service
	 * @param currProject the current project instance
	 * @return the domain id list
	 */
	public static List<String> getDomainList(GraphElementLayoutService graphElementLayoutService, Project currProject) {

		// ------------------------------------------------
		// --- Temporary set the current project ----------
		// ------------------------------------------------
		ElectricityDomainIdentification.currProject = currProject;
		// ------------------------------------------------
		
		
		List<String> domainIdList = new ArrayList<>();
		
		// --- Get the current domain settings ------------
		TreeMap<String, DomainSettings> dsTreeMap = getDomainSettings();
		if (dsTreeMap==null) return domainIdList;
		
		List<String> domainList = new ArrayList<>(dsTreeMap.keySet());
		for (String domain : domainList) {
			DomainSettings ds = dsTreeMap.get(domain);
			String adapterClass = ds.getAdapterClass();
			if (adapterClass==null || adapterClass.isEmpty()==true) continue;
			
			if (graphElementLayoutService instanceof TriPhaseElectricalNetworkGraphElementLayoutService) {
				if (adapterClass.equals(TriPhaseElectricalNodeAdapter.class.getName())) {
					domainIdList.add(domain);
				}
			} else if (graphElementLayoutService instanceof UniPhaseElectricalNetworkGraphElementLayoutService) {
				if (adapterClass.equals(UniPhaseElectricalNodeAdapter.class.getName())) {
					domainIdList.add(domain);
				}
			}
		}
		
		// ------------------------------------------------
		// --- Reset the temporary project ----------------
		// ------------------------------------------------
		ElectricityDomainIdentification.currProject = null;
		// ------------------------------------------------
		
		return domainIdList;
	}
	
	/**
	 * Return the {@link ElectricityNetworkType} for the specified domain (or sub network).
	 *
	 * @param domain the domain to search for
	 * @return the ElectricityNetworkType or <code>null</code>, if the network is not an electricity network
	 */
	public static ElectricityNetworkType getElectricityNetworkType(String domain) {
		
		TreeMap<String, DomainSettings> dsTreeMap = getDomainSettings();
		
		DomainSettings ds = dsTreeMap.get(domain);
		String adapterClass = ds.getAdapterClass();
		if (adapterClass==null || adapterClass.isBlank()==true) return null;
		
		if (adapterClass.equals(TriPhaseElectricalNodeAdapter.class.getName())==true) {
			return ElectricityNetworkType.TriPhaseNetwork;
		}
		
		if (adapterClass.equals(UniPhaseElectricalNodeAdapter.class.getName())==true) {
			return ElectricityNetworkType.UniPhaseNetwork;
		}
		return null;
	}
	
	/**
	 * Checks if the specified domain is an electricity domain.
	 *
	 * @param domain the domain to check
	 * @return true, if is electricity domain
	 */
	public static boolean isElectricityDomain(String domain) {
		return getElectricityNetworkType(domain)!=null;
	}
	
	/**
	 * Returns all domains that are of type electricity.
	 *
	 * @param subNetConfigService the SubNetworkConfigurationService
	 * @return the domain id list
	 */
	public static List<String> getDomainList() {

		List<String> domainIdList = new ArrayList<>();
		
		// --- Get the current domain settings ------------
		TreeMap<String, DomainSettings> dsTreeMap = getDomainSettings();
		
		List<String> domainList = new ArrayList<>(dsTreeMap.keySet());
		for (String domain : domainList) {
			DomainSettings ds = dsTreeMap.get(domain);
			String adapterClass = ds.getAdapterClass();
			if (adapterClass==null || adapterClass.isEmpty()==true) continue;
			
			if (adapterClass.equals(TriPhaseElectricalNodeAdapter.class.getName()) || adapterClass.equals(UniPhaseElectricalNodeAdapter.class.getName())) {
				domainIdList.add(domain);
			}
		}
		return domainIdList;
	}
	
	
	// ------------------------------------------------------------------------
	// --- From here some basic access functions ------------------------------
	// ------------------------------------------------------------------------	
	/**
	 * Returns the current domain settings.
	 * @return the domain settings
	 */
	private static TreeMap<String, DomainSettings> getDomainSettings() {
		
		GeneralGraphSettings4MAS graphSettings = getGraphSettings();
		if (graphSettings!=null) {
			return graphSettings.getDomainSettings();
		}
		return null;
	}
	/**
	 * Returns the current {@link GeneralGraphSettings4MAS}.
	 * @return the graph settings
	 */
	private static GeneralGraphSettings4MAS getGraphSettings() {

		GraphEnvironmentController graphController = getGraphEnvironmentController();
		if (graphController!=null) {
			return graphController.getGeneralGraphSettings4MAS();
		}
		return null;
	}
	/**
	 * Returns the current {@link GraphEnvironmentController}.
	 * @return the graph environment controller
	 */
	private static GraphEnvironmentController getGraphEnvironmentController() {
		
		Project project = getProject();
		if (project!=null) {
			EnvironmentController envController = project.getEnvironmentController();
			if (envController instanceof GraphEnvironmentController) {
				return (GraphEnvironmentController) envController;
			}
		}
		return null;
	}
	/**
	 * Return the current project instance.
	 * @return the project
	 */
	private static Project getProject() {
		if (currProject!=null) return currProject;
		return Application.getProjectFocused();
	}

}
