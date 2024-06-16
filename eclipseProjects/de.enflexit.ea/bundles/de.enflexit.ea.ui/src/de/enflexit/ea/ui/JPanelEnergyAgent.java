package de.enflexit.ea.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import de.enflexit.common.swing.JTreeForJTabbedPaneCascade;
import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.ui.SwingUiFocusDescription.FocusTo;
import de.enflexit.ea.ui.SwingUiModel.PropertyEvent;
import de.enflexit.ea.ui.SwingUiModel.UiDataCollection;

/**
 * The Class JPanelEnergyAgent provides the predefined UI for an EnergyAgent
 * containing a {@link JMenuBar} and a  {@link JToolBar}.
 * 
 * @see #getJMenuBarEnergyAgent()
 * @see #getJToolBarEnergyAgent()
 * 
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class JPanelEnergyAgent extends JPanel implements SwingUiModelInterface, PropertyChangeListener {

	private static final long serialVersionUID = 3767077921829926692L;

	public static final String TAB_TITLE_GENERAL_INFORMATION = "General Information";
	public static final String TAB_TITLE_REAL_TIME_INFORMATION = "Real-Time Information";
	public static final String TAB_TITLE_PLANNER_INFORMATION = "Planner Information";
	public static final String TAB_TITLE_CONTROL_ASSISTANT = "Control Assistant";
	
	private AbstractEnergyAgent energyAgent;
	private SwingUiModelInterface propertyModel;
	
	private JMenuBarEnergyAgent jMenuBarEnergyAgent;
	private JToolBarEnergAgent jToolBarEnergAgent;
	
	private JSplitPane jSplitPane;
	private JScrollPane jScrollPaneTree;
	private JTreeForJTabbedPaneCascade jTreeJTabbedPane; 
	private JTabbedPane jTabbedPane;
	private JPanelGeneralInformation jPanelGeneralInformation;
	private JPanelRealTimeInformation jPanelRealTimeInformation;
	private JPanelPlannerInformation jPanelPlannerInformation;
	private JPanelEomPlanningEventTreeMap jPanelEomPlanningEventTreeMap;
	
	
	/**
	 * Instantiates a new j frame planning assistant.
	 * @param planningAssistantAgent the planning assistant agent
	 */
	public JPanelEnergyAgent(AbstractEnergyAgent energyAgent) {
		this.energyAgent = energyAgent;
		this.initialize();
	}
	
	/**
	 * Returns the local property model for the UI.
	 * @return the property model
	 */
	private SwingUiModelInterface getSwingUiModel() {
		if (propertyModel==null) {
			propertyModel = new SwingUiModel(this.getEnergyAgent());
			propertyModel.addPropertyListener(this);
		}
		return propertyModel;
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.ui.SwingUiModelInterface#getEnergyAgent()
	 */
	@Override
	public AbstractEnergyAgent getEnergyAgent() {
		return energyAgent;
	}
	/**
	 * Sets the local energy agent.
	 * @param energyAgent the new energy agent
	 */
	public void setEnergyAgent(AbstractEnergyAgent energyAgent) {
		this.energyAgent = energyAgent;
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.ui.SwingUiModelInterface#addPropertyListener(java.beans.PropertyChangeListener)
	 */
	@Override
	public void addPropertyListener(PropertyChangeListener listener) {
		this.getSwingUiModel().addPropertyListener(listener);
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.ui.SwingUiModelInterface#removePropertyListener(java.beans.PropertyChangeListener)
	 */
	@Override
	public void removePropertyListener(PropertyChangeListener listener) {
		this.getSwingUiModel().removePropertyListener(listener);
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.ui.SwingUiModelInterface#firePropertyEvent(de.enflexit.ea.ui.SwingUiModel.PropertyEvent)
	 */
	@Override
	public void firePropertyEvent(PropertyEvent event) {
		this.getSwingUiModel().firePropertyEvent(event);
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.ui.SwingUiModelInterface#fireTabFocusEvent(java.lang.String)
	 */
	@Override
	public void fireFocusEvent(SwingUiFocusDescription focusDescription) {
		this.getSwingUiModel().fireFocusEvent(focusDescription);
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.ui.SwingUiModelInterface#collectUiData(de.enflexit.ea.ui.SwingUiModel.UiDataCollection)
	 */
	@Override
	public Object collectUiData(UiDataCollection dataType) {
		return this.getSwingUiModel().collectUiData(dataType);
	}
	
	
	/**
	 * Initialize the PlanningAssitatnt UI.
	 */
	private void initialize() {
		
		this.setLayout(new BorderLayout());
		this.add(this.getJToolBarEnergyAgent(), BorderLayout.NORTH);
		this.add(this.getJSplitPane(), BorderLayout.CENTER);
	}

	public JMenuBarEnergyAgent getJMenuBarEnergyAgent() {
		if (jMenuBarEnergyAgent == null) {
			jMenuBarEnergyAgent = new JMenuBarEnergyAgent(this);
		}
		return jMenuBarEnergyAgent;
	}
	
	public JToolBarEnergAgent getJToolBarEnergyAgent() {
		if (jToolBarEnergAgent==null) {
			jToolBarEnergAgent = new JToolBarEnergAgent(this);
		}
		return jToolBarEnergAgent;
	}
	
	private JSplitPane getJSplitPane() {
		if (jSplitPane==null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setDividerSize(8);
			jSplitPane.setDividerLocation(300);
			jSplitPane.setResizeWeight(0.0);
			jSplitPane.setRightComponent(this.getJTabbedPane());
			jSplitPane.setLeftComponent(this.getjScrollPaneTree());
		}
		return jSplitPane;
	}
	
	private JScrollPane getjScrollPaneTree() {
		if (jScrollPaneTree==null) {
			jScrollPaneTree = new JScrollPane();
			jScrollPaneTree.setViewportView(this.getJTreeJTabbedPane());
		}
		return jScrollPaneTree;
	}
	private JTreeForJTabbedPaneCascade getJTreeJTabbedPane() {
		if (jTreeJTabbedPane==null) {
			jTreeJTabbedPane = new JTreeForJTabbedPaneCascade(this.getJSplitPane());
		}
		return jTreeJTabbedPane;
	}
	
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane(JTabbedPane.TOP);
			jTabbedPane.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTabbedPane.setBorder(BorderFactory.createEmptyBorder());
			jTabbedPane.addTab(" " + TAB_TITLE_GENERAL_INFORMATION + " ", null, getJPanelGeneralInformation(), null);
			jTabbedPane.addTab(" " + TAB_TITLE_REAL_TIME_INFORMATION + " ", null, getJPanelRealTimeInformation(), null);
			jTabbedPane.addTab(" " + TAB_TITLE_PLANNER_INFORMATION + " ", null, getJPanelPlannerInformation(), null);
			jTabbedPane.addTab(" " + TAB_TITLE_CONTROL_ASSISTANT + " ", null, getJPanelPlanningEvents(), null);
		}
		return jTabbedPane;
	}
	
	public JPanelGeneralInformation getJPanelGeneralInformation() {
		if (jPanelGeneralInformation == null) {
			jPanelGeneralInformation = new JPanelGeneralInformation(this);
		}
		return jPanelGeneralInformation;
	}
	
	public JPanelRealTimeInformation getJPanelRealTimeInformation() {
		if (jPanelRealTimeInformation == null) {
			jPanelRealTimeInformation = new JPanelRealTimeInformation(this);
		}
		return jPanelRealTimeInformation;
	}
	
	public JPanelPlannerInformation getJPanelPlannerInformation() {
		if (jPanelPlannerInformation == null) {
			jPanelPlannerInformation = new JPanelPlannerInformation(this);
		}
		return jPanelPlannerInformation;
	}
	public JPanelEomPlanningEventTreeMap getJPanelPlanningEvents() {
		if (jPanelEomPlanningEventTreeMap==null) {
			jPanelEomPlanningEventTreeMap = new JPanelEomPlanningEventTreeMap(this);
		}
		return jPanelEomPlanningEventTreeMap;
	}
	

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		if (evt.getNewValue() instanceof PropertyEvent == false) return;
		PropertyEvent pEvent = (PropertyEvent) evt.getNewValue();
		switch (pEvent) {
		case UpdateView:
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JPanelEnergyAgent.this.getJTreeJTabbedPane().reBuildView();
				}
			});
			break;
		case FocusEvent:
			SwingUiFocusEvent focusEvent = (SwingUiFocusEvent) evt;
			if (focusEvent.getFocusDescription().getFocusTo()==FocusTo.Tab) {
				this.getJTreeJTabbedPane().setFocusToTab(focusEvent.getFocusDescription().getArgument());
			}
			break;
		default:
			break;
		}
	}

}
