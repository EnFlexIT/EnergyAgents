package de.enflexit.energyAgent.ops.fieldDataRequest.agent;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import agentgui.core.application.Application;
import de.enflexit.energyAgent.core.monitoring.MonitoringStrategyRT;
import de.enflexit.energyAgent.ops.OpsController;
import de.enflexit.eom.database.DatabaseStorageHandler_ScheduleList;
import energy.optionModel.Schedule;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.persistence.ScheduleList_StorageHandler;
import hygrid.globalDataModel.cea.ConversationID;
import hygrid.ops.ontology.FieldDataReply;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This behaviour handles the incoming replies for previously sent FieldDataRequests
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class HandleRepliesBehaviour extends Behaviour {

	private static final long serialVersionUID = 1272231481894137224L;
	
	private DatabaseStorageHandler_ScheduleList storageHandler;
	private MessageTemplate messageTemplate;
	
	private boolean done = false;
	
	private boolean debug = false;
	
	private HashMap<String, Integer> numberOfReceivedTSSEs;
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		
		ACLMessage msg = myAgent.receive(this.getMessageTemplate());
		if (msg!=null) {
			if (msg.getPerformative()==ACLMessage.INFORM) {
				
				FieldDataReply reply = this.extractMessageContent(msg);
				String scheduleListXML = reply.getScheduleListXML();
				
				ScheduleList_StorageHandler slsh = new ScheduleList_StorageHandler();
				ScheduleList scheduleList = slsh.getScheduleListFromXMLString(scheduleListXML);
				
				if (scheduleList!=null) {
					
					String agentID = scheduleList.getNetworkID();
					int number = this.getNumberOfTSSEsForScheduleList(scheduleList);
					
					
					OpsController.getInstance().setStatusBarMessage(this.createStatusMessage(agentID, number, reply.getTotalTSSEs()));

					if (this.debug==true) {
						System.out.print(myAgent.getLocalName() + ": Received " + number + " TSSEs from agent " + scheduleList.getNetworkID());
					}
					
					
					Schedule schedule = scheduleList.getSchedules().get(0);
					
					// --- Check if the ScheduleList contains data --------------
					if (schedule!=null && (schedule.getTechnicalSystemStateList().isEmpty()==false || schedule.getTechnicalSystemStateEvaluation()!=null)) {
						Integer scheduleListID = this.getScheduleListID(agentID, Application.getProjectFocused().getSimulationSetupCurrent());
						
						if (scheduleListID==null) {
							// --- No schedule list found, store as a new one ---
							if (this.debug==true) {
								System.out.println(" - storing as a new schedule.");
							}
							scheduleList.setSetup(Application.getProjectFocused().getSimulationSetupCurrent());
							scheduleList.setDescription("Field data from network component " + scheduleList.getNetworkID());
							scheduleList.setDateSaved(Calendar.getInstance());
							this.getStorageHandler().saveScheduleList(null, scheduleList, null, true, false, true);
						} else {
							if (this.debug==true) {
								System.out.println(" - appending to an existing schedule.");
							}
							
							this.getStorageHandler().setScheduleListDateSaved(scheduleListID, System.currentTimeMillis());
							
							// --- Append to an existing schedule ---------------
							int scheduleID = this.getScheduleID(scheduleListID);
							
							if (schedule.getTechnicalSystemStateList()==null || schedule.getTechnicalSystemStateList().size()==0) {
								ScheduleList_StorageHandler.convertToListSchedule(schedule);
							}
							
							this.getStorageHandler().saveListOfTechnicalSystemStateEvaluation(scheduleID, schedule.getTechnicalSystemStateList(), null);
						}
						
					} else {
						System.err.println(myAgent.getLocalName() + ": Agent " + agentID + " returned an empty schedule for the requested period!");
						OpsController.getInstance().setStatusBarMessage("Agent " + agentID + " returned an empty schedule for the requested period!");
					}
				} else {	
					// --- scheduleList==null -------------
					System.out.println(myAgent.getLocalName() + ": Empty reply from agent " + reply.getAgentID() + ", no TSSEs for the requested period");
				}

				
				// --- Check if this was the last message from this agent -----
				if (reply.getMoreComming()==false) {
					((FieldDataRequestAgent)myAgent).getInternalDataModel().getPendingReplies().remove(reply.getAgentID());
				}
				
						
			} else if (msg.getPerformative()==ACLMessage.FAILURE) {
				String failedAgentID = msg.getContent();
				if (this.debug==true) {
					System.out.println(myAgent.getLocalName() + ": Field data request for agent " + failedAgentID + " failed!");
				}
				OpsController.getInstance().setStatusBarMessage("Field data request to agent " + failedAgentID + " failed!");
				((FieldDataRequestAgent)myAgent).getInternalDataModel().getPendingReplies().remove(failedAgentID);
			}
			
			// --- Check if all expected replies have arrived -------------
			if (((FieldDataRequestAgent)myAgent).getInternalDataModel().getPendingReplies().isEmpty()==true) {
				this.done = true;
			}
		} else {
			block();
		}
	}
	
	/**
	 * Extract message content.
	 * @param replyMessage the reply message
	 * @return the field data reply
	 */
	private FieldDataReply extractMessageContent(ACLMessage replyMessage) {
		FieldDataReply reply = null;
		
		ContentElement contentElement;
		try {
			contentElement = myAgent.getContentManager().extractContent(replyMessage);
			// --- Extract message contents ---------------
			if (contentElement instanceof Action) {
				Concept concept = ((Action)contentElement).getAction();
				if (concept instanceof FieldDataReply) {
					reply = (FieldDataReply) concept;
				}
			}
		} catch (CodecException | OntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return reply;
	}
	
	
	/**
	 * Gets the message template.
	 * @return the message template
	 */
	private MessageTemplate getMessageTemplate() {
		if (messageTemplate==null) {
			messageTemplate = MessageTemplate.MatchConversationId(ConversationID.OPS_FIELD_DATA_REQUEST.toString());
		}
		return messageTemplate;
	}

	/**
	 * Gets the storage handler.
	 * @return the storage handler
	 */
	private DatabaseStorageHandler_ScheduleList getStorageHandler() {
		if (storageHandler==null) {
			storageHandler = new DatabaseStorageHandler_ScheduleList();
		}
		return storageHandler;
	}
	
	/**
	 * Gets the schedule list ID for the specified network component from the database.
	 * @param networkComponentID the network component ID
	 * @return the schedule list ID, null if not found
	 */
	private Integer getScheduleListID(String networkComponentID, String setup) {
		
		Integer scheduleListID = null;
		// --- Execute query --------------------------------
		List<Integer> idsList = this.getStorageHandler().getScheduleListIDs(networkComponentID, null, setup);
		if (idsList!=null && idsList.size()==1) {
			// --- Everything is fine -----------------------
			scheduleListID = idsList.get(0);
		} else {
			// --- Wrong number of results ------------------
			System.err.println("[" + this.getClass().getSimpleName() + "] " + idsList.size() + " ScheduleLists found for system " + networkComponentID + " and setup " + setup);
		}
		return scheduleListID;
	}
	
	/**
	 * Returns the id schedule.
	 * @return the id schedule
	 */
	private Integer getScheduleID(int scheduleListID) {
		Integer idSchedule = null;; 
		// --- Execute query ----------------------------------------
		List<?> idsList = this.getStorageHandler().getScheduleIDs(scheduleListID, MonitoringStrategyRT.class.getName());
		if (idsList!=null &&  idsList.size()==1) {
			// --- Everything is fine -------------------------------
			idSchedule = (Integer) idsList.get(0);
		} else {
			// --- Wrong number of results --------------------------
			System.err.println("[" + this.getClass().getSimpleName() + "] " +  idsList.size() + "Schedules were found for the ScheduleListID " + scheduleListID);
		}
		return idSchedule;
	}
	
	/**
	 * Gets the number of TSSEs for a schedule list (assuming it contains only one schedule).
	 * @param scheduleList the schedule list
	 * @return the number of TSSEs
	 */
	private int getNumberOfTSSEsForScheduleList(ScheduleList scheduleList) {
		int number = 0;
		Schedule schedule = scheduleList.getSchedules().get(0);
		if (schedule!=null) {
			if (schedule.getTechnicalSystemStateEvaluation()!=null) {
				TechnicalSystemStateEvaluation tsse = schedule.getTechnicalSystemStateEvaluation();
				while (tsse!=null) {
					number++;
					tsse = tsse.getParent();
				} 
			} else {
				number = schedule.getTechnicalSystemStateList().size();	
			}
		}
		return number;
	}
	
	/**
	 * Gets the number of received TSSEs for every agent that already sent something.
	 * @return the number of received TSSEs
	 */
	private HashMap<String, Integer> getNumberOfReceivedTSSEs() {
		if (numberOfReceivedTSSEs==null) {
			numberOfReceivedTSSEs = new HashMap<>();
		}
		return numberOfReceivedTSSEs;
	}
	
	/**
	 * Creates the status message.
	 *
	 * @param agentID the agent ID
	 * @param newTSSEs the new TSS es
	 * @param totalTSSEs the total TSS es
	 * @return the string
	 */
	private String createStatusMessage(String agentID, int newTSSEs, int totalTSSEs) {
		int received = 0;
		if (this.getNumberOfReceivedTSSEs().get(agentID)!=null) {
			received = this.getNumberOfReceivedTSSEs().get(agentID); 
		}
		
		String statusMessage = "Received TSSEs " + (received+1) + " to " + (received+newTSSEs) + " of " + totalTSSEs + " from agent " + agentID;
		this.getNumberOfReceivedTSSEs().put(agentID, received+newTSSEs);
		return statusMessage;
	}

	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#done()
	 */
	@Override
	public boolean done() {
		return done;
	}

	@Override
	public int onEnd() {
		// --- Job done, terminate --------------
		if (this.debug==true) {
			System.out.println(myAgent.getLocalName() + ": Job done, terminating...");
		}
		
		OpsController.getInstance().setStatusBarMessage("Field data acquisition complete.");
		
		myAgent.doDelete();
		return super.onEnd();
	}
	
	

}
