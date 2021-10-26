package de.enflexit.ea.core.awbIntegration.adapter;

import java.awt.Color;

import javax.swing.ImageIcon;

import org.awb.env.networkModel.GraphElement;
import org.awb.env.networkModel.adapter.AbstractDynamicGraphElementLayout;
import org.awb.env.networkModel.controller.GraphEnvironmentController;

import de.enflexit.ea.core.awbIntegration.ImageHelper;
import de.enflexit.ea.core.dataModel.ontology.CableWithBreakerProperties;
import de.enflexit.ea.core.dataModel.ontology.CircuitBreaker;

/**
 * The Class CableWithBreakDynamicLayout.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class CableWithBreakerDynamicLayout extends AbstractDynamicGraphElementLayout {

	private static final ImageIcon IMAGE_ICON_OPEN   = ImageHelper.getImageIcon("breakerOpen.png");
	private static final ImageIcon IMAGE_ICON_CLOSED = ImageHelper.getImageIcon("breakerClosed.png");
	
	private boolean isBreakerOpen;
	
	
	/**
	 * Instantiates a new cable with break dynamic layout.
	 * @param graphElement the graph element
	 */
	public CableWithBreakerDynamicLayout(GraphElement graphElement) {
		super(graphElement);
	}

	// ----------------------------------------------------------------------------------
	// --- From here, methods to return state dependent layout settings ----------------- 
	// ----------------------------------------------------------------------------------	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.GraphElementLayout#getSize()
	 */
	@Override
	public float getSize() {
		if (this.isBreakerOpen==true) {
			return 1f;
		}
		return super.getSize();
	}
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.GraphElementLayout#getColor()
	 */
	@Override
	public Color getColor() {
		if (this.isBreakerOpen==true) {
			return Color.LIGHT_GRAY;
		}
		return super.getColor();
	}
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.GraphElementLayout#getImageIcon()
	 */
	@Override
	public ImageIcon getImageIcon() {
		if (this.isBreakerOpen==true) {
			return IMAGE_ICON_OPEN;
		}
		return IMAGE_ICON_CLOSED;
	}
	
	
	// ----------------------------------------------------------------------------------
	// --- From here, methods to update the local, data state --------------------------- 
	// ----------------------------------------------------------------------------------	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.adapter.DynamicGraphElementLayout#updateGraphElementLayout(org.awb.env.networkModel.controller.GraphEnvironmentController)
	 */
	@Override
	public void updateGraphElementLayout(GraphEnvironmentController graphController) {

		CableWithBreakerProperties breakerConfig = this.getCableWithBreakerProperties(graphController);
		if (breakerConfig==null) return;
		
		CircuitBreaker cbBegin = this.getValidCircuitBreaker(breakerConfig.getBreakerBegin());
		CircuitBreaker cbEnd   = this.getValidCircuitBreaker(breakerConfig.getBreakerEnd());
		this.isBreakerOpen = (cbBegin==null ? false : cbBegin.getIsClosed()==false) || (cbEnd==null ? false : cbEnd.getIsClosed()==false); 
	}
	/**
	 * Returns the cable with breaker properties.
	 *
	 * @param graphController the graph controller
	 * @return the cable with breaker properties
	 */
	private CableWithBreakerProperties getCableWithBreakerProperties(GraphEnvironmentController graphController) {
		
		Object dm = this.getDataModelForLocalGraphEdgeNetworkComponent(graphController);
		if (dm!=null && dm.getClass().isArray()==true) {
			Object[] dmArray = (Object[]) dm;
			if (dmArray.length>1 && dmArray[0] instanceof CableWithBreakerProperties) {
				return (CableWithBreakerProperties) dmArray[0]; 
			}
		}
		return null;
	}
	/**
	 * Check and returns valid circuit breaker only .
	 *
	 * @param cb the CircuitBreaker instance to check
	 * @return the valid circuit breaker
	 */
	private CircuitBreaker getValidCircuitBreaker(CircuitBreaker cb) {
		if (cb==null) return null;
		if (cb.getAtComponent()==null || cb.getAtComponent().isEmpty()) return null;
		//if (cb.getBreakerID()==null || cb.getBreakerID().isEmpty()) return null;
		return cb;
	}
	
}
