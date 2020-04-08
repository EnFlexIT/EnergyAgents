package de.enflexit.energyAgent.core.aggregation;

import java.util.ArrayList;
import java.util.List;

import energy.optionModel.TechnicalInterface;

/**
 * The Class DefaultSubAggregationBuilder basically extends the {@link AbstractSubAggregationBuilder} 
 * and use its methods. This class can be used if no specific SubAggregationBuilder is specified. 
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class DefaultSubAggregationBuilder extends AbstractSubAggregationBuilder {

	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractSubAggregationBuilder#getTechnichalInterfaces()
	 */
	@Override
	protected List<TechnicalInterface> getAggregationTechnicalInterfaces() {
		// --- No interfaces defined, return an empty list. The actual interfaces will be derived from the aggregation's subsystems.
		return new ArrayList<TechnicalInterface>();
	}

}
