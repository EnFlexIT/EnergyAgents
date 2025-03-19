package de.enflexit.ea.core.aggregation;

import java.awt.Container;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.awb.env.networkModel.GraphEdge;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import de.enflexit.awb.core.Application;
import de.enflexit.awb.simulation.environment.time.TimeModelContinuous;
import de.enflexit.awb.simulation.environment.time.TimeModelDateBased;
import de.enflexit.awb.simulation.environment.time.TimeModelDiscrete;
import de.enflexit.common.SerialClone;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import edu.uci.ics.jung.graph.SparseGraph;
import energy.OptionModelController;
import energy.optionModel.Duration;
import energy.optionModel.EnergyFlowGeneral;
import energy.optionModel.EnergyFlowInWattCalculated;
import energy.optionModel.EvaluationClass;
import energy.optionModel.EvaluationSettings;
import energy.optionModel.GroupMember;
import energy.optionModel.InterfaceSetting;
import energy.optionModel.ScheduleList;
import energy.optionModel.State;
import energy.optionModel.StateTransition;
import energy.optionModel.TechnicalInterface;
import energy.optionModel.TechnicalInterfaceConfiguration;
import energy.optionModel.TechnicalSystem;
import energy.optionModel.TechnicalSystemGroup;
import energy.optionModel.TechnicalSystemState;
import energy.optionModel.TechnicalSystemStateTime;
import energy.optionModel.TimeUnit;
import energy.schedule.ScheduleController;
import energygroup.GroupController;
import energygroup.GroupNotification;
import energygroup.GroupTreeNodeObject;
import energygroup.calculation.GroupCalculation;
import energygroup.gui.GroupMainPanel;
import energygroup.gui.GroupTree.GroupNodeCaptionStyle;

/**
 * The Class AbstractSubAggregationBuilder defines the base for an individual separation
 * of a specified NetworkModel. Thus, a topology or NetworkModel should can be 
 * separated into different domains (e.g. electricity, heat, gas and other).
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public abstract class AbstractSubAggregationBuilder {

	private AbstractAggregationHandler aggregationHandler;
	private AbstractSubNetworkConfiguration subAggregationConfiguration;
	
	private NetworkModel networkModel;
	
	private JInternalFrame jInternalFrameAggregation;
	private GroupMainPanel groupMainPanel;
	private GroupController groupController;
	
	private boolean containsSubSystems;
	
	/**
	 * Sets the aggregation handler.
	 * @param aggregationHandler the current parent aggregation handler
	 * 
	 * @see AbstractAggregationHandler
	 */
	public void setAggregationHandler(AbstractAggregationHandler aggregationHandler) {
		this.aggregationHandler = aggregationHandler;
	}
	/**
	 * Returns the current parent aggregation handler.
	 * @return the aggregation handler
	 */
	public AbstractAggregationHandler getAggregationHandler() {
		return aggregationHandler;
	}
	
	/**
	 * Sets the current sub aggregation configuration.
	 * @param subAggregationConfiguration the new sub aggregation configuration
	 */
	public void setSubAggregationConfiguration(AbstractSubNetworkConfiguration subAggregationConfiguration) {
		this.subAggregationConfiguration = subAggregationConfiguration;
	}
	/**
	 * Returns the current sub aggregation configuration.
	 * @return the sub aggregation configuration
	 */
	public AbstractSubNetworkConfiguration getSubAggregationConfiguration() {
		return subAggregationConfiguration;
	}


	/**
	 * Gets the time model.
	 * @return the time model
	 */
	protected TimeModelDateBased getTimeModel() {
		return this.getAggregationHandler().getTimeModel();
	}
	/**
	 * Gets the HyGridAbstractEnvironmentModel from the EnvironmentModel.
	 * @return the HyGridAbstractEnvironmentModel
	 */
	protected HyGridAbstractEnvironmentModel getHyGridAbstractEnvironmentModel() {
		return this.getAggregationHandler().getHyGridAbstractEnvironmentModel();
	}
	
	/**
	 * Checks if is headless operation.
	 * @return true, if is headless operation
	 */
	protected boolean isHeadlessOperation() {
		return this.getAggregationHandler().isHeadlessOperation();
	}
	
	/**
	 * Returns the HashMap of the NetworkComponents {@link ScheduleController}.
	 * @return the agents schedule controller
	 */
	protected HashMap<String, ScheduleController> getNetworkComponentsScheduleController() {
		return this.getAggregationHandler().getNetworkComponentsScheduleController();
	}
	
	/**
	 * Checks if is contains sub systems.
	 * @return true, if is contains sub systems
	 */
	public boolean hasSubSystems() {
		return containsSubSystems;
	}
	/**
	 * Sets the contains sub systems.
	 * @param containsSubSystems the new contains sub systems
	 */
	protected void setContainsSubSystems(boolean containsSubSystems) {
		this.containsSubSystems = containsSubSystems;
	}
	
	/**
	 * Creates the EOM aggregation in a dedicated thread.
	 */
	protected void createEomAggregationInThread() {
		this.createEomAggregationInThread(true);
	}
	/**
	 * Creates the EOM aggregation in a dedicated thread.
	 * @param showVisualization the indicator to directly show the aggregations visualization
	 */
	protected void createEomAggregationInThread(boolean showVisualization) {
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				// --- Create aggregation ---------------------------
				AbstractSubAggregationBuilder.this.createEomAggregation(showVisualization);
			}
		}, "EOM-Aggregation-Build_" + this.getSubAggregationConfiguration().getID()).start();
	}
	
	/**
	 * Will be invoked to create the EOM - aggregation.
	 */
	protected void createEomAggregation() {
		this.createEomAggregation(true);
	}
	/**
	 * Will be invoked to create the EOM - aggregation.
	 * @param showVisualization the show visualization
	 */
	protected void createEomAggregation(boolean showVisualization) {
	
		// --- Create / fill the aggregation model ----------------------------
		this.createAggregationAsTechnicalSystemGroup();
		this.addSubsystemsToAggregation();

		// --- Show visualization of the aggregation ? ------------------------
		if (showVisualization==true && this.isHeadlessOperation()==false && this.hasSubSystems()==true) {
			this.showVisualization();
		}
		
		// --- Unregister at the executed builds Vector -----------------------
		this.getAggregationHandler().getExecutedBuilds().remove(this);
	}
	
	/**
	 * Show the visualization of the aggregation.
	 */
	protected void showVisualization() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				// --- Is there a parent container defined? ---------------
				Container parentContainer = AbstractSubAggregationBuilder.this.getSubAggregationConfiguration().getAggregationVisualizationParentContainer();
				if (parentContainer!=null) {
					if (parentContainer instanceof JTabbedPane) {
						JTabbedPane parentTabbedPane = (JTabbedPane) parentContainer;
						parentTabbedPane.addTab(AbstractSubAggregationBuilder.this.getSubAggregationConfiguration().getSubNetworkDescriptionID(), AbstractSubAggregationBuilder.this.getGroupMainPanel(parentContainer));
					} else {
						// --- Use parent container from configuration ----
						parentContainer.add(AbstractSubAggregationBuilder.this.getGroupMainPanel(parentContainer));
					}
					
				} else {
					
					// --- Use the project desktop ------------------------
					JDesktopPane desktop = Application.getProjectFocused().getProjectDesktop();
					
					// --- Build the visualization ------------------------
					JInternalFrame intFrame = AbstractSubAggregationBuilder.this.getJInternalFrameAggregation();
					intFrame.getContentPane().add(AbstractSubAggregationBuilder.this.getGroupMainPanel());
					intFrame.setSize((int) (desktop.getWidth() * 0.95), (int) (desktop.getHeight() * 0.9));
					
					// --- Add JInternalFrame to the project desktop ------
					try {
						desktop.add(intFrame);
						// --- Define a movement? -------------------------
						int noOfIntFrames = desktop.getAllFrames().length;
						if (noOfIntFrames>1) {
							int movement = (noOfIntFrames-1) * 20;
							intFrame.setLocation(movement, movement);
						}
						intFrame.setSelected(true);
						intFrame.moveToFront();
						
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				
				// --- Configure visualization to network- and system-ID --
				AbstractSubAggregationBuilder.this.setGroupVisualization();
			}
		});
	}
	
	/**
	 * Terminates the current aggregation and its visualization.
	 */
	public void terminateEomAggregation() {

		// --- Stop the calculation -----------------------------
		if (this.getNetworkCalculationStrategy()!=null) {
			this.getNetworkCalculationStrategy().terminateRelatedStrategyInstances();
		}
		
		// --- Dispose the GUI ----------------------------------
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				AbstractSubAggregationBuilder.this.getGroupMainPanel().setDoModelChangeCheckBeforeClosing(false);
				AbstractSubAggregationBuilder.this.getGroupMainPanel().closeEomUserInterface();
			}
		});
	}
	
	// ----------------------------------------------------------------------------------
	// --- From here, methods for the definition of the aggregation can be found --------
	// --- that is an EOM TechnicalSystemGroup ------------------------------------------
	// ----------------------------------------------------------------------------------
	protected JInternalFrame getJInternalFrameAggregation() {
		if (jInternalFrameAggregation==null) {
			jInternalFrameAggregation = new JInternalFrame();
			jInternalFrameAggregation.setTitle(this.getSubAggregationConfiguration().getSubNetworkDescriptionID() + " - " + this.getAggregationHandler().getOwnerName());
			jInternalFrameAggregation.setClosable(false);
			jInternalFrameAggregation.setMaximizable(true);
			jInternalFrameAggregation.setResizable(true);
			jInternalFrameAggregation.setVisible(true);
		}
		return jInternalFrameAggregation;
	}
	
	/**
	 * Gets the group main panel.
	 * @return the group main panel
	 */
	protected GroupMainPanel getGroupMainPanel() {
		return this.getGroupMainPanel(null);
	}
	
	/**
	 * Gets the group main panel.
	 * @param parentContainer the parent container
	 * @return the group main panel
	 */
	protected GroupMainPanel getGroupMainPanel(Container parentContainer) {
		if (groupMainPanel==null) {
			if (parentContainer==null) {
				parentContainer = this.getJInternalFrameAggregation();
			}
			groupMainPanel = new GroupMainPanel(this.getGroupController(), parentContainer);
			groupMainPanel.setHostIntegrated();
		}
		return groupMainPanel;
	}
	
	/**
	 * Gets the group controller.
	 * @return the group controller
	 */
	public GroupController getGroupController() {
		if (groupController==null) {
			groupController = new GroupController();
		}
		return groupController;
	}
	
	/**
	 * Gets the reduced network model, containing only components that belong to the aggregation.
	 * @return the aggregation network model
	 */
	protected NetworkModel getAggregationNetworkModel() {
		if (networkModel==null) {
			// --- Build the network model based on the aggregation's domain cluster ----
			networkModel = this.buildAggregationNetworkModel();
			
			// --- If not available, use  the original network model as fallback --------
			if (networkModel==null) {
				networkModel = this.getOriginalNetworkModel();
			}
		}
		return networkModel;
	}
	
	/**
	 * Gets the original network model, containing all network components.
	 * @return the original network model
	 */
	private NetworkModel getOriginalNetworkModel() {
		return this.getAggregationHandler().getNetworkModel();
	}
	
	/**
	 * Built the network model for the aggregation.
	 * @return the network model
	 */
	private NetworkModel buildAggregationNetworkModel() {
		NetworkModel aggregationNetworkModel = null;
		
		// --- Check if the domain cluster for the aggregation exists and contains network components
		if (this.getSubAggregationConfiguration().getDomainCluster()!=null && this.getSubAggregationConfiguration().getDomainCluster().getNetworkComponents().size()>0) {
			// --- Prepare the network model for the aggregation --------------
			aggregationNetworkModel = new NetworkModel();
			aggregationNetworkModel.setGeneralGraphSettings4MAS(this.getOriginalNetworkModel().getGeneralGraphSettings4MAS());
			aggregationNetworkModel.setGraph(new SparseGraph<GraphNode, GraphEdge>());
			aggregationNetworkModel.setLayoutID(this.getOriginalNetworkModel().getLayoutID());
			
			// --- Add the initial network component --------------------------
			NetworkComponent initialComponent = this.getSubAggregationConfiguration().getDomainCluster().getNetworkComponents().get(0);
			if (this.getOriginalNetworkModel().getGraphElementsFromNetworkComponent(initialComponent).size()==1) {
				GraphNode initialNode =  (GraphNode) this.getOriginalNetworkModel().getGraphElementsFromNetworkComponent(initialComponent).get(0);
				aggregationNetworkModel.addNetworkComponent(initialComponent, false);
				aggregationNetworkModel.getGraph().addVertex(initialNode);
				aggregationNetworkModel.addGraphElementToNetworkComponentRelation(initialNode, initialComponent);
				
				// --- Check the adjacent edges of the initial node -----------
				this.addAdjacentEdgesRecursively(initialNode, aggregationNetworkModel);
				
			} else {
				//TODO handle complex components 
			}
			aggregationNetworkModel.refreshGraphElements();
		}
		
		return aggregationNetworkModel;
	}
	
	/**
	 * This method the adjacent edges recursively (depth first graph traversal). For all adjacent edges of the specified node,
	 * this method will check the opposite node. If it is part of the domain cluster, both edge and opposite node will be added
	 * to the aggregation network model. Then the opposite node's adjacent edges will be processed. Terminates if no further 
	 * unprocessed edges are found.  
	 * @param originalNode the original node
	 * @param aggregationNetworkModel the aggregation network model
	 */
	private void addAdjacentEdgesRecursively(GraphNode originalNode, NetworkModel aggregationNetworkModel) {
		
		// --- Iterate over the node's adjacent edges -------------------------
		Vector<GraphEdge> adjacentEdges = new Vector<GraphEdge>(this.getOriginalNetworkModel().getGraph().getIncidentEdges(originalNode));
		for (int i=0; i<adjacentEdges.size(); i++) {
			GraphEdge adjacentEdge = adjacentEdges.get(i);
			NetworkComponent edgeComponent = this.getOriginalNetworkModel().getNetworkComponent(adjacentEdge);
			// --- Check if this edge was already added -----------------------
			if (aggregationNetworkModel.getNetworkComponent(edgeComponent.getId())==null) {
				// --- Check the opposite graph node --------------------------
				GraphNode oppositeNode = this.getOriginalNetworkModel().getGraph().getOpposite(originalNode, adjacentEdge);
				NetworkComponent oppositeComponent = this.getDistributionNodeComponentForGraphNode(oppositeNode);
				if (oppositeComponent!=null && this.getSubAggregationConfiguration().getDomainCluster().getNetworkComponents().contains(oppositeComponent)) {
					
					// --- Check if the opposite component was added already, add if not
					if (aggregationNetworkModel.getNetworkComponent(oppositeComponent.getId())==null) {
						aggregationNetworkModel.addNetworkComponent(oppositeComponent, false);
						aggregationNetworkModel.getGraph().addVertex(oppositeNode);
						aggregationNetworkModel.addGraphElementToNetworkComponentRelation(oppositeNode, oppositeComponent);
					}
					
					// --- Add the edge's component ---------------------------
					aggregationNetworkModel.addNetworkComponent(edgeComponent, false);
					aggregationNetworkModel.getGraph().addEdge(adjacentEdge, originalNode, oppositeNode);
					aggregationNetworkModel.addGraphElementToNetworkComponentRelation(adjacentEdge, edgeComponent);
					
					// --- Check adjacent edges of the opposite node ----------
					this.addAdjacentEdgesRecursively(oppositeNode, aggregationNetworkModel);
				}
			}
		}
	}
	
	/**
	 * Gets the distribution node component for Graph node.
	 * @param graphNode the graph node
	 * @return the distribution node component for Graph node
	 */
	private NetworkComponent getDistributionNodeComponentForGraphNode(GraphNode graphNode) {
		List<NetworkComponent> nodeComponents = this.getOriginalNetworkModel().getNetworkComponents(graphNode);
		for(NetworkComponent netComp : nodeComponents) {
			if (this.getOriginalNetworkModel().isDistributionNode(netComp)) {
				return netComp;
			}
		}
		return null;
	}
	
	
	/**
	 * Creates the {@link TechnicalSystemGroup} for the aggregation.
	 */
	protected void createAggregationAsTechnicalSystemGroup() {
		
		// --- Create new TechnicalSystemGroup --------------------------------
		TechnicalSystemGroup tsg = new TechnicalSystemGroup();
		
		// --- Get the top level TechnicalSystem ------------------------------
		TechnicalSystem ts = new TechnicalSystem();
		ts.setSystemID(this.getAggregationHandler().getOwnerName());
		ts.setSystemDescription("Aggregation of the " + this.getAggregationHandler().getOwnerName());
		ts.setCalculationClass(GroupCalculation.class.getName());
		tsg.setTechnicalSystem(ts);
		
		// --- Set Interface configuration to main TechnicalSystem ------------
		TechnicalInterfaceConfiguration tic = new TechnicalInterfaceConfiguration();
		tic.setConfigID("Default");
		tic.setConfigDescription("Default Interface Configuration");
		ts.getInterfaceConfigurations().add(tic);
		
		// --- Define the default state of the aggregation --------------------
		State state = new State();
		state.setStateID("Operation");
		state.setStateDescription("Operation of the Simulation");
		state.setDuration(this.getStateDuration());
		state.setFormatTimeUnit(this.getStateDurationTimeFormat());
		tic.setInitialStateID(state.getStateID());
		tic.getSystemStates().add(state);
		
		// --- Create StateTransition for the above State ---------------------
		StateTransition stateTransition = new StateTransition();
		stateTransition.setNextStateID(state.getStateID());
		// --- Min Duration ---------------------
		Duration minDur = new Duration();
		minDur.setValue(0);
		minDur.setUnit(TimeUnit.SECOND_S);
		stateTransition.setMinDuration(minDur);
		// --- Max Duration ---------------------
		Duration maxDur = new Duration();
		maxDur.setValue(0);
		maxDur.setUnit(TimeUnit.SECOND_S);
		stateTransition.setMaxDuration(maxDur);
		state.getStateTransitions().add(stateTransition);

		// --- Define the TechnicalInterfaces and add EnergyFlow to state -----
		List<TechnicalInterface> interfaces = this.getAggregationTechnicalInterfaces();
		for (int i=0; i<interfaces.size(); i++) {
			tic.getTechnicalInterfaces().add(interfaces.get(i));
			state.getFlows().add(this.getNewEnergyFlowCalculated(interfaces.get(i)));
		}
		
		// --- Finally, set TechnicalSystemGroup to GroupController -----------
		this.getGroupController().setTechnicalSystemGroup(tsg);
		
		// --- Set the evaluation settings for the aggregation ----------------
		this.setEvaluationSettingsToAggregation(tic, state);
		
	}
	
	/**
	 * Gets the technical interfaces for the aggregation.
	 * @return the aggregation technical interfaces
	 */
	protected abstract List<TechnicalInterface> getAggregationTechnicalInterfaces();
	
	/**
	 * Sets the evaluation settings to the aggregation.
	 *
	 * @param tic the {@link TechnicalInterfaceConfiguration} to use 
	 * @param state the {@link State} to use
	 */
	protected void setEvaluationSettingsToAggregation(TechnicalInterfaceConfiguration tic, State state) {
		
		// --- Get some settings from the time model --------------------------
		long startTime = this.getTimeModel().getTimeStart();
		long endTime = this.getTimeModel().getTimeStop();
		String timeFormat = this.getTimeModel().getTimeFormat();
		
		// --- State at start time --------------------------------------------
		TechnicalSystemState tssStart = new TechnicalSystemState();
		tssStart.setDescription("Initial State of the Aggregation");
		tssStart.setGlobalTime(startTime);
		tssStart.setStateTime(0);
		tssStart.setConfigID(tic.getConfigID());
		tssStart.setStateID(state.getStateID());
		
		// --- End time of the Simulation -------------------------------------
		TechnicalSystemStateTime tssEnd = new TechnicalSystemStateTime();
		tssEnd.setDescription("End time for the evaluation");
		tssEnd.setGlobalTime(endTime);
		
		// --- Get the instance of the EvaluationSettings --------------------- 
		EvaluationSettings evalSettings = this.getGroupController().getGroupOptionModelController().getEvaluationSettings();
		evalSettings.setTimeFormat(timeFormat);
		
		// --- Set the default evaluation strategy ----------------------------
		EvaluationClass evalClass = new EvaluationClass();
		if (this.getNetworkCalculationStrategyClass()!=null) {
			evalClass.setStrategyID(this.getNetworkCalculationStrategyClass().getSimpleName());
			evalClass.setClassName(this.getNetworkCalculationStrategyClass().getName());
			evalClass.setDescription("GroupStrategy for the Network Calculation");
			evalSettings.getEvaluationClasses().add(evalClass);
			evalSettings.setEvaluationClass(this.getNetworkCalculationStrategyClass().getName()); // - as default -
		}
		evalSettings.getEvaluationStateList().add(tssStart);
		evalSettings.getEvaluationStateList().add(tssEnd);
	}
	
	/**
	 * Adds the subsystems to the built aggregation.
	 */
	protected void addSubsystemsToAggregation() {
		
		// --- Get the list of NetworkComponents to work on -------------------
		Vector<NetworkComponent> netComps = null;
		if (this.getSubAggregationConfiguration().getDomainCluster()!=null) {
			netComps = this.getSubAggregationConfiguration().getDomainCluster().getNetworkComponents();
		} else {
			netComps = this.getAggregationNetworkModel().getNetworkComponentVectorSorted();
		}
		
		// --- Collect all components that belong to the aggregation --------- 
		for (int i = 0; i < netComps.size(); i++) {
			// --- Check single NetworkComponent ------------------------------
			NetworkComponent netComp = netComps.get(i);
			boolean isPartOfSubnetwork = this.getSubAggregationConfiguration().isPartOfSubnetwork(netComp);
			if (isPartOfSubnetwork==true) {
				// --- Add the component to the aggregation -------------------
				this.addSubSystemToAggregation(netComp);
			}
		}
	}
	
	
	/**
	 * Registers the current subsystem build for a concurrent sub system construction.
	 *
	 * @param netCompID the NetworkComponent ID
	 * @return true, if the calling method should create the subsystem; false otherwise
	 */
	private boolean registerForConcurrentSubSystemConstruction(String netCompID) {
		
		boolean doBuild = false;
		boolean doWait = false;
		Object synchObject = null;
		
		// --- Try getting the synchronization object and further proceeding ------------
		synchronized (this.getAggregationHandler().getSubSystemConstructionHashMap()) {
			synchObject = this.getAggregationHandler().getSubSystemConstructionHashMap().get(netCompID);
			if (synchObject==null) {
				doBuild = true;
				doWait = false;
				this.getAggregationHandler().getSubSystemConstructionHashMap().put(netCompID, new Object());
			} else {
				doBuild = false;
				doWait = true;
			}
		}

		if (doWait==true) {
			synchronized (synchObject) {
				try {
					synchObject.wait(100);
				} catch (InterruptedException intEx) {
					intEx.printStackTrace();
				}
			} 
		}
		return doBuild;
	}
	/**
	 * Unregisters the current subsystem build for the concurrent sub system construction.
	 * 
	 * @param netCompID the NetworkComponent ID
	 */
	private void unregisterForConcurrentSubSystemConstruction(String netCompID) {
		
		// --- Try getting the synchronization object and further proceeding ------------
		synchronized (this.getAggregationHandler().getSubSystemConstructionHashMap()) {
			Object synchObject = this.getAggregationHandler().getSubSystemConstructionHashMap().remove(netCompID);
			if (synchObject!=null) {
				synchronized (synchObject) {
					synchObject.notifyAll();
				}
			}
		}
	}
	
	/**
	 * Adds a single sub system to the aggregation.
	 * @param subSystemComponent the sub system's {@link NetworkComponent}
	 */
	protected void addSubSystemToAggregation(NetworkComponent subSystemComponent) {
		
		ScheduleController scheduleController = this.getNetworkComponentsScheduleController().get(subSystemComponent.getId());
		if (scheduleController==null) {
			// --- Check if the system is 'under construction' ------
			boolean doBuild = this.registerForConcurrentSubSystemConstruction(subSystemComponent.getId());
			if (doBuild==true) {
				// --- Create a new ScheduleList --------------------
				ScheduleList sl = new ScheduleList();
				sl.setNetworkID(subSystemComponent.getId());
				sl.setSystemID(subSystemComponent.getType());
				sl.getInterfaceSettings().addAll(this.getInterfaceSettings(subSystemComponent.getDataModel()));
				
				// --- Add as a new GroupMember ---------------------
				GroupMember groupMember = this.getGroupController().addScheduleList(sl, null);
				GroupTreeNodeObject gtno = this.getGroupController().getGroupTreeModel().getGroupTreeNodeObject(groupMember);
				
				// --- Remind the ScheduleController ----------------
				this.getNetworkComponentsScheduleController().put(subSystemComponent.getId(), gtno.getGroupMemberScheduleController());
				
				// --- Unregister for construction check ------------ 
				this.unregisterForConcurrentSubSystemConstruction(subSystemComponent.getId());
				
			} else {
				// --- System is available now. Just recall method --
				this.addSubSystemToAggregation(subSystemComponent);
			}
			
		} else {
			// --- Use existing ScheduleList & ScheduleController ---
			ScheduleList sl = scheduleController.getScheduleList();
			
			// --- Add as a new GroupMember -------------------------
			GroupMember groupMember = this.getGroupController().addScheduleList(sl, null);
			GroupTreeNodeObject gtno = this.getGroupController().getGroupTreeModel().getGroupTreeNodeObject(groupMember);
			gtno.setGroupMemberScheduleController(scheduleController);
			
		}
		// --- Set indicator for available sub systems --------------
		this.setContainsSubSystems(true);
	}
	
	/**
	 * This method will be invoked after the aggregation (a {@link TechnicalSystemGroup}) and its subsystems are
	 * created / defined to update the group visualization. It will ensure that for each system the network ID and
	 * the system Id will be shown.
	 */
	protected void setGroupVisualization() {
		// --- Set tree view to Network & System-ID --------------------------- 
		this.getGroupController().setChangedAndNotifyObservers(new GroupNotification(GroupNotification.Reason.GroupTreeViewChanged, GroupNodeCaptionStyle.Both));
		this.getGroupController().setTechnicalSystemGroupReminder();
	}
	
	
	/**
	 * Gets the state duration.
	 * @return the state duration
	 */
	protected Duration getStateDuration() {
		
		Duration duration = new Duration();
		duration.setValue(5000);					// --- Default case -----
		duration.setUnit(TimeUnit.MILLISECOND_MS);	// --- Always! ----------
		
		if (this.getTimeModel() instanceof TimeModelDiscrete) {
			TimeModelDiscrete timeModel = (TimeModelDiscrete) this.getTimeModel();
			duration.setValue(timeModel.getStep());
		} else if (this.getTimeModel() instanceof TimeModelContinuous) {
			duration.setValue(this.getHyGridAbstractEnvironmentModel().getNetworkCalculationIntervalLength());
		}
		return duration;
	}
	/**
	 * Gets the state duration time format.
	 * @return the state duration time format
	 */
	protected TimeUnit getStateDurationTimeFormat() {
		// --- Default case -------------------------------
		TimeUnit eomTuSelected = TimeUnit.SECOND_S;	
		if (this.getTimeModel() instanceof TimeModelDiscrete) {
			TimeModelDiscrete timeModel = (TimeModelDiscrete) this.getTimeModel();
			eomTuSelected = this.getTimeUnitOfTimeUnitVectorIndex(timeModel.getStepDisplayUnitAsIndexOfTimeUnitVector());
		} else if (this.getTimeModel() instanceof TimeModelContinuous) {
			eomTuSelected = this.getTimeUnitOfTimeUnitVectorIndex(this.getHyGridAbstractEnvironmentModel().getNetworkCalculationIntervalUnitIndex());
		}
		return eomTuSelected;
	}
	/**
	 * Gets the time unit of time unit vector index.
	 *
	 * @param vectorIndex the vector index
	 * @return the time unit of time unit vector index
	 */
	private TimeUnit getTimeUnitOfTimeUnitVectorIndex(int vectorIndex) {
		
		TimeUnit eomTuSelected = TimeUnit.SECOND_S;	
		switch (vectorIndex) {
		case 0:
			eomTuSelected = TimeUnit.MILLISECOND_MS;
			break;
		case 1:
			eomTuSelected = TimeUnit.SECOND_S;
			break;
		case 2:
			eomTuSelected = TimeUnit.MINUTE_M;
			break;
		case 3:
			eomTuSelected = TimeUnit.HOUR_H;
			break;
		default:
			eomTuSelected = TimeUnit.SECOND_S;
			break;
		}
		return eomTuSelected;
	}
	
	/**
	 * Returns the time step configured.
	 * @return the time step configured
	 */
	public long getTimeStepConfigured() {
		long timeStepConfigured = 0;
		if (this.getTimeModel() instanceof TimeModelDiscrete) {
			timeStepConfigured = ((TimeModelDiscrete)this.getTimeModel()).getStep();
		} else if (this.getTimeModel() instanceof TimeModelContinuous) {
			timeStepConfigured = this.getHyGridAbstractEnvironmentModel().getNetworkCalculationIntervalLength();
		}
		return timeStepConfigured;
	}
	
	/**
	 * Returns the interface settings out of the specified {@link NetworkComponent}'s data model, where
	 * the data model should be of type {@link TechnicalSystem}, {@link TechnicalSystemGroup} orr {@link ScheduleList}.
	 *
	 * @param netCompDataModel the actual data model of the network component
	 * @return the interface settings
	 */
	protected List<InterfaceSetting> getInterfaceSettings(Object netCompDataModel) {
		
		List<InterfaceSetting> intSettings = new ArrayList<>();
		if (netCompDataModel instanceof ScheduleList) {
			// --- ScheduleList -------------------------------------
			ScheduleList sl = (ScheduleList) netCompDataModel;
			intSettings = sl.getInterfaceSettings();
			
		} else if (netCompDataModel instanceof TechnicalSystem || netCompDataModel instanceof TechnicalSystemGroup) {
			// --- TechnicalSystem or TechnicalSystemGroup ----------
			TechnicalSystem ts = null;
			if (netCompDataModel instanceof TechnicalSystem) {
				ts = (TechnicalSystem) netCompDataModel;
			} else {
				TechnicalSystemGroup  tsg = (TechnicalSystemGroup) netCompDataModel;
				ts = tsg.getTechnicalSystem();
			}
			
			// --- Temporally create an OptionModelController -------
			OptionModelController omc = new OptionModelController();
			omc.setTechnicalSystem(ts);
			
			// --- Get the interface settings -----------------------
			String configID = null;
			if (omc.getEvaluationSettings().getEvaluationStateList().size()>0) {
				EvaluationSettings evaluationSettings = ts.getEvaluationSettings();
				TechnicalSystemState tss = (TechnicalSystemState) evaluationSettings.getEvaluationStateList().get(0);
				configID = tss.getConfigID();
			} else {
				configID = ts.getInterfaceConfigurations().get(0).getConfigID();
			}
			TechnicalInterfaceConfiguration tic = omc.getTechnicalInterfaceConfiguration(configID);
			
			// --- Create settings from technical interfaces --------
			for (int i = 0; i < tic.getTechnicalInterfaces().size(); i++) {
				TechnicalInterface ti = tic.getTechnicalInterfaces().get(i);
				InterfaceSetting intSetting = new InterfaceSetting();
				intSetting.setInterfaceID(ti.getInterfaceID());
				intSetting.setDomain(ti.getDomain());
				intSetting.setDomainModel(SerialClone.clone(ti.getDomainModel()));
				intSettings.add(intSetting);
			}
		}
		return intSettings;
	}
	
	/**
	 * Returns a new energy flow that is specified as {@link EnergyFlowInWattCalculated}
	 *
	 * @param ti the {@link TechnicalInterface}
	 * @return the energy flow calculated
	 */
	protected EnergyFlowGeneral getNewEnergyFlowCalculated(TechnicalInterface ti) {
		EnergyFlowGeneral eFlow = new EnergyFlowGeneral();
		eFlow.setInterfaceID(ti.getInterfaceID());
		eFlow.setEnergyFlow(new EnergyFlowInWattCalculated());
		return eFlow;
	}

	/**
	 * Returns the network calculation strategy class.
	 * @return the network calculation strategy class
	 */
	protected Class<? extends AbstractNetworkCalculationStrategy> getNetworkCalculationStrategyClass() {
		return this.getSubAggregationConfiguration().getNetworkCalculationStrategyClass();
	}
	/**
	 * Returns the network calculation strategy.
	 * @return the network calculation strategy
	 */
	protected AbstractNetworkCalculationStrategy getNetworkCalculationStrategy() {
		return this.getSubAggregationConfiguration().getNetworkCalculationStrategy();
	}
	
}
