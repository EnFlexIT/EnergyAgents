package de.enflexit.ea.validation;

import java.util.ArrayList;
import java.util.List;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.settings.ComponentTypeSettings;
import org.awb.env.networkModel.settings.GeneralGraphSettings4MAS;

import de.enflexit.ea.core.awbIntegration.adapter.triPhase.TriPhaseBreakerAdapter;
import de.enflexit.ea.core.awbIntegration.adapter.triPhase.TriPhaseCableAdapter;
import de.enflexit.ea.core.awbIntegration.adapter.triPhase.TriPhaseSensorAdapter;
import de.enflexit.ea.core.awbIntegration.adapter.uniPhase.UniPhaseBreakerAdapter;
import de.enflexit.ea.core.awbIntegration.adapter.uniPhase.UniPhaseCableAdapter;
import de.enflexit.ea.core.awbIntegration.adapter.uniPhase.UniPhaseSensorAdapter;
import de.enflexit.ea.core.dataModel.ontology.CableProperties;
import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationMessage;
import de.enflexit.ea.core.validation.HyGridValidationMessage.MessageType;

/**
 * The Class ValidateCable.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class ValidateCable extends HyGridValidationAdapter {

	private GeneralGraphSettings4MAS graphSettings;
	private List<String> adapterClassList; 
	
	/**
	 * Returns the {@link GeneralGraphSettings4MAS} form the current {@link GraphEnvironmentController}.
	 * @return the graph settings
	 */
	private GeneralGraphSettings4MAS getGraphSettings() {
		if (graphSettings==null) {
			graphSettings = this.getGraphController().getGeneralGraphSettings4MAS();
		}
		return graphSettings;
	}
	/**
	 * Returns the component type settings for the specified NetworkComponent.
	 *
	 * @param netComp the NetworkComponent
	 * @return the component type settings
	 */
	private ComponentTypeSettings getComponentTypeSettings(NetworkComponent netComp) {
		if (netComp==null) return null;
		return this.getGraphSettings().getCurrentCTS().get(netComp.getType());
	}
	
	/**
	 * Returns the list of classes that use CableProperties for the configuration.
	 * @return the adapter class list
	 */
	private List<String> getAdapterClassList() {
		if (adapterClassList==null) {
			adapterClassList = new ArrayList<>();
			// --- Insert the adapter classes that are of interest for the validation ---
			adapterClassList.add(UniPhaseCableAdapter.class.getName());
			adapterClassList.add(TriPhaseCableAdapter.class.getName());
			
			adapterClassList.add(UniPhaseBreakerAdapter.class.getName());
			adapterClassList.add(TriPhaseBreakerAdapter.class.getName());
			
			adapterClassList.add(UniPhaseSensorAdapter.class.getName());
			adapterClassList.add(TriPhaseSensorAdapter.class.getName());
		}
		return adapterClassList;
	}
	/**
	 * Checks if the specified {@link NetworkComponent} is of type cable.
	 *
	 * @param netComp the NetworkComponent to check
	 * @return true, if is cable type
	 */
	private boolean isCableType(NetworkComponent netComp) {
		ComponentTypeSettings cts = this.getComponentTypeSettings(netComp);
		return cts!=null ? this.getAdapterClassList().contains(cts.getAdapterClass()) : null;
	}
	/**
	 * Checks if the specified {@link NetworkComponent} is of type single phase cable.
	 *
	 * @param netComp the NetworkComponent to check
	 * @return true, if is of type single phase cable 
	 */
	private boolean isUniPhaseCableType(NetworkComponent netComp) {
		if (this.isCableType(netComp)==true) {
			String adapterClassName = this.getComponentTypeSettings(netComp).getAdapterClass();
			if (adapterClassName.contains(".Uni")) return true;
		}
		return false;
	}
	/**
	 * Checks if the specified {@link NetworkComponent} is of type breaker.
	 *
	 * @param netComp the NetworkComponent to check
	 * @return true, if is of type breaker
	 */
	private boolean isBreakerComponent(NetworkComponent netComp) {
		if (this.isCableType(netComp)==true) {
			String adapterClassName = this.getComponentTypeSettings(netComp).getAdapterClass();
			if (adapterClassName.contains("PhaseBreakerAdapter")) return true;
		}
		return false;
		
	}
	
	
	/* (non-Javadoc)
	 * @see net.agenthygrid.core.validation.HyGridValidationAdapter#validateNetworkComponent(org.awb.env.networkModel.NetworkComponent)
	 */
	@Override
	public HyGridValidationMessage validateNetworkComponent(NetworkComponent netComp) {
		
		if (this.isCableType(netComp)==false) return null;
			
		// --- Extract dataModel ------------------------------------
		Object[] dataModel = (Object[]) netComp.getDataModel();
		if (dataModel==null || (dataModel.getClass().isArray()==true && dataModel[0]==null)) {
			// --- No data model exists. ----------------------------
			String message = "Error found for " + netComp.getType() + " " + netComp.getId() + ": No Data Model found for this Component!";
			String description = "Try to edit the cable properties of " + netComp.getType() + " " + netComp.getId() + " - this is possibly already enough.";
			return new HyGridValidationMessage(message, MessageType.Error,description);
			
		} else {
			// --- Get the actual cable model -----------------------
			CableProperties cdm = (CableProperties) dataModel[0];
			// --- Check for a permitted length of the cable --------
			if (this.isBreakerComponent(netComp)==false && (cdm.getLength()==null || cdm.getLength().getValue() == 0)) {
				String message = "Error found for " + netComp.getType() + " " + netComp.getId() + ": Length is not specified or zero!";
				String description = "Edit the length of the Cable in [m].\nOtherwise the subnetwork can't be calculated.";
				return new HyGridValidationMessage(message, MessageType.Error,description);
			}
			
			// --- Check for linear Resistance or Inductance --------
			if (cdm.getLinearResistance().getValue()==0 && cdm.getLinearReactance().getValue()==0) {
				String message = "Error found for " + netComp.getType() + " " + netComp.getId() + ": Linear Resistance and Linear Reactance are both zero!";
				String description = "Fill in at least one of the values for Linear Resistance and Linear Reactance in [Ω\\km].\nOtherwise the subnetwork can't be calculated.";
				return new HyGridValidationMessage(message, MessageType.Error,description);
			}

			// --- Check for UniPhase Cable -------------------------
			if (this.isUniPhaseCableType(netComp)==true) {
				// Check for linear Capacitance and warn if zero
				if (cdm.getLinearCapacitance().getValue()==0) {
					String message = "Warning for " + netComp.getType() + " " + netComp.getId() + ": Linear Capacitance is zero! ";
					String description = "Fill in the cable's capacitance in [nF/km], since it is not negligible for voltage levels above low voltage.\nNevertheless, the subnetwork can still be calculated without this value.";
					return new HyGridValidationMessage(message, MessageType.Warning,description);
				}
			}
		}
		return null;
	}
	
}
