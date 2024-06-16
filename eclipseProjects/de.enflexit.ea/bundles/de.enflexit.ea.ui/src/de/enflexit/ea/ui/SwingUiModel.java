package de.enflexit.ea.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import de.enflexit.ea.core.AbstractEnergyAgent;

/**
 * The Class SwingUiModel.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class SwingUiModel implements SwingUiModelInterface {

	private List<PropertyChangeListener> propertyChangeListener;
	
	public enum PropertyEvent {
		ShowOrFocusView,
		UpdateView, 
		CloseView,
		FocusEvent
	}
	
	public enum UiDataCollection {
		PlannerResultAsSelected,
		NextPlannerEvent
	}
	
	private AbstractEnergyAgent energyAgent;
	
	/**
	 * Instantiates a new property model.
	 * @param energyAgent the energy agent
	 */
	public SwingUiModel(AbstractEnergyAgent energyAgent) {
		this.energyAgent = energyAgent;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.ui.SwingUiModelInterface#getEnergyAgent()
	 */
	@Override
	public AbstractEnergyAgent getEnergyAgent() {
		return this.energyAgent;
	}
	
	
	/**
	 * Returns the property change listener.
	 * @return the property change listener
	 */
	private List<PropertyChangeListener> getPropertyChangeListener() {
		if (propertyChangeListener==null) {
			propertyChangeListener = new ArrayList<>();
		}
		return propertyChangeListener;
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.ui.SwingUiModelInterface#addPropertyListener(java.beans.PropertyChangeListener)
	 */
	@Override
	public void addPropertyListener(PropertyChangeListener listener) {
		if (listener!=null && this.getPropertyChangeListener().contains(listener)==false) {
			this.getPropertyChangeListener().add(listener);
		}
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.ui.SwingUiModelInterface#removePropertyListener(java.beans.PropertyChangeListener)
	 */
	@Override
	public void removePropertyListener(PropertyChangeListener listener) {
		if (listener!=null && this.getPropertyChangeListener().contains(listener)==true) {
			this.getPropertyChangeListener().remove(listener);
		}
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.ui.SwingUiModelInterface#firePropertyEvent(de.enflexit.ea.ui.SwingUiModel.PropertyEvent)
	 */
	@Override
	public void firePropertyEvent(PropertyEvent event) {
		if (event==null) return;
		PropertyChangeEvent pce = new PropertyChangeEvent(this, event.name(), null, event);
		for (PropertyChangeListener pcl : this.getPropertyChangeListener()) {
			pcl.propertyChange(pce);
		}
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.ui.SwingUiModelInterface#fireTabFocusEvent(java.lang.String)
	 */
	@Override
	public void fireFocusEvent(SwingUiFocusDescription focusDescription) {
		if (focusDescription==null) return;
		SwingUiFocusEvent dataRequest = new SwingUiFocusEvent(this, PropertyEvent.FocusEvent.name(), null, PropertyEvent.FocusEvent, focusDescription);
		for (PropertyChangeListener pcl : this.getPropertyChangeListener()) {
			pcl.propertyChange(dataRequest);
		}
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.ui.SwingUiModelInterface#collectUiData(de.enflexit.ea.ui.SwingUiModel.UiDataCollection)
	 */
	@Override
	public Object collectUiData(UiDataCollection dataType) {
		if (dataType==null) return null;
		SwingUiDataCollector dataRequest = new SwingUiDataCollector(this, dataType.name(), null, dataType);
		for (PropertyChangeListener pcl : this.getPropertyChangeListener()) {
			pcl.propertyChange(dataRequest);
		}
		return dataRequest.getCollectedData();
	}

	
}
