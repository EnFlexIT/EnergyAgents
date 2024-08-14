package de.enflexit.ea.electricity.aggregation;

import java.util.Vector;

import org.awb.env.networkModel.NetworkModel;

import de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration;
import de.enflexit.ea.core.dataModel.TransformerComponent;
import de.enflexit.ea.core.dataModel.TransformerHelper;
import de.enflexit.ea.electricity.aggregation.triPhase.TriPhaseSubNetworkConfiguration;
import de.enflexit.ea.electricity.aggregation.uniPhase.UniPhaseSubNetworkConfiguration;
import energy.helper.NumberHelper;

/**
 * The Class AbstractElectricalNetworkConfiguration serves a super class for the classes
 * {@link TriPhaseSubNetworkConfiguration} and {@link UniPhaseSubNetworkConfiguration}
 * and provides a method to determine the current voltage level from the current sub {@link NetworkModel}.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public abstract class AbstractElectricalNetworkConfiguration extends AbstractSubNetworkConfiguration {

	private Double configuredRatedVoltage;
	
	/**
	 * Return the configured rated voltage.
	 * @return the configured rated voltage
	 */
	public double getConfiguredRatedVoltageFromNetwork() { 
		if (configuredRatedVoltage==null) {
			
			// --- Define a default / start value -------------------  
			if (this instanceof TriPhaseSubNetworkConfiguration) {
				configuredRatedVoltage = 230.0;
			} else if (this instanceof UniPhaseSubNetworkConfiguration) {
				configuredRatedVoltage = 10000.0;
			}
			
			// --- Get the configured rated voltage ----------------- 
			if (this.getDomainCluster()!=null && this.getDomain()!=null) {

				// --- Find the SlackNode in the Network ------------ 
				Float ratedVoltage = configuredRatedVoltage.floatValue();
				Vector<TransformerComponent> transformerVector = TransformerHelper.getTransformerComponents(this.getAggregationHandler().getNetworkModel(), this.getDomain(), this.getDomainCluster().getNetworkComponents());
				if (transformerVector.size()==0) {
					System.err.println("[" + this.getClass().getSimpleName() + "] No rated voltage level could be found for sub aggregation '" + this.getSubNetworkDescriptionID() + "', Domain: " + this.getDomain() + "!");
				} else {
					if (TransformerHelper.isEqualRatedVoltage(transformerVector)==true) {
						ratedVoltage = transformerVector.get(0).getRatedVoltage();
					} else {
						System.err.println("[" + this.getClass().getSimpleName() + "] Found inconsistent rated voltage levels in graph node modles for sub aggregation '" + this.getSubNetworkDescriptionID() + "': " + TransformerHelper.getRatedVoltagesAsString(transformerVector));
					}
				}
				
				// --- Check if three- or uni-phase -----------------
				if (this instanceof TriPhaseSubNetworkConfiguration) {
					configuredRatedVoltage = ratedVoltage==400.0 ? 230.0 : NumberHelper.round(ratedVoltage / Math.sqrt(3.0), 0);
				} else if (this instanceof UniPhaseSubNetworkConfiguration) {
					configuredRatedVoltage = ratedVoltage.doubleValue();
				}
			}
		}
		return configuredRatedVoltage;
	}

}
