package de.enflexit.ea.core.eomStateStream;

import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.eom.awb.stateStream.SystemStateDispatcherAgentConnector;
import de.enflexit.eom.awb.stateStream.SystemStateLoadListener;
import de.enflexit.eom.awb.stateStream.SystemStateLoadRequest;
import de.enflexit.eom.awb.stateStream.SystemStateLoadResult;
import energy.optionModel.Schedule;
import energy.optionModel.ScheduleList;
import energy.schedule.loading.ScheduleTimeRange;

/**
 * The Class StateQueueKeeperScheduleList extends the {@link AbstractStateQueueKeeper}
 * but is designed to work with {@link ScheduleList} representations for {@link NetworkComponent}s.
 * 
 * @author Christian Derksen - DAWIS - ICB University of Duisburg - Essen
 */
public class StateQueueKeeperScheduleList extends AbstractStateQueueKeeper implements SystemStateLoadListener {

	/** Ensures that the keeper only awaits one RequestResult */
	private boolean iswWaitingForRequestResult = false;

	/** Reminder for later checks and empty results */ 
	private ScheduleTimeRange lastScheduleTimeRange;
	/** Reminder that the last Schedule was empty */
	private boolean lastReceivedScheduleWasEmpty;
	
	
	/**
	 * Instantiates a new state queue keeper for a ScheduleList.
	 *
	 * @param owningEomInputStream the owning Eom model state input stream
	 * @param dispatchConnector the dispatch connector
	 */
	public StateQueueKeeperScheduleList(EomModelStateInputStream owningEomInputStream, SystemStateDispatcherAgentConnector dispatchConnector) {
		super(owningEomInputStream, dispatchConnector);
	}

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.eomStateStream.AbstractStateQueueKeeper#checkRemainingStatesAndPossiblyStartStartLoading(int, boolean)
	 */
	@Override
	public void checkRemainingStatesAndPossiblyStartLoading(int remainingStatesInQueue, boolean isDataReloadRecommended) {
		
		if (this.iswWaitingForRequestResult==false && isDataReloadRecommended==true) {
			SystemStateLoadRequest loadRequest = this.createLoadRequest();
			if (loadRequest!=null) {
				this.setProvisioningStarted();
				this.iswWaitingForRequestResult = true;
				this.dispatchConnector.forwardSystemStateLoadRequest(loadRequest);
			}
		}
	}
	
	/**
	 * Creates a new SystemStateLoadRequest that reflects the global ScheduleTimeRange
	 * and the currently available system states.
	 * @return the system state load request
	 */
	protected SystemStateLoadRequest createLoadRequest() {
		
		// --- Define a ScheduleTimeRange for the request -------------------------------
		long timeFrom = this.getScheduleEnergyTransmission().getTechnicalSystemStateList().get(0).getGlobalTime();
		
		ScheduleTimeRange strRequest = new ScheduleTimeRange();
		strRequest.setRangeType(this.globalScheduleTimeRange.getRangeType());
		strRequest.setTimeFrom(timeFrom);
		
		switch (this.globalScheduleTimeRange.getRangeType()) {
		case TimeRange:
			long diffTime = this.globalScheduleTimeRange.getTimeTo() - this.globalScheduleTimeRange.getTimeFrom();
			strRequest.setTimeTo(timeFrom + diffTime);
			break;
			
		case StartTimeAndNumber:
			strRequest.setNumberOfSystemStates(this.globalScheduleTimeRange.getNumberOfSystemStates());
			break;
		}
		
		// --- Create a SystemStateLoadRequest? -----------------------------------------
		boolean isCreateRequest = true;
		SystemStateLoadRequest sslr = null;
		if (this.lastReceivedScheduleWasEmpty==true && this.lastScheduleTimeRange.equals(strRequest)==true) {
			// --- Don't ask the same question, if you know the answer already ---------- 
			isCreateRequest = false;
			
		} else {
			// --- Remind last request ScheduleTimeRange ------------------------------------
			this.lastScheduleTimeRange = strRequest;
			sslr = new SystemStateLoadRequest(this.eomInputStream.getNetworkComponent(), strRequest, this);
		}
		
		// --- Print out, what the ScheduleTimeRange will request -----------------------
		if (this.isDebug()==true) {
			if (isCreateRequest==true) {
				System.out.println("[" + this.getClass().getSimpleName() + "][" + this.eomInputStream.getNetworkComponent().getId() + "] Request: " + strRequest.getDisplayText(false));
			} else {
				System.err.println("[" + this.getClass().getSimpleName() + "][" + this.eomInputStream.getNetworkComponent().getId() + "] Will not repeat the request for an empty Schedule: " + strRequest.getDisplayText(false));
			}
		}
		
		// --- Return a new SystemStateLoadRequest --------------------------------------
		return sslr;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.eom.awb.stateStream.SystemStateLoadListener#setRequestResult(de.enflexit.eom.awb.stateStream.SystemStateLoadResult)
	 */
	@Override
	public void setRequestResult(SystemStateLoadResult systemStateLoadResult) {
		
		ScheduleList sl = systemStateLoadResult.getScheduleList();
		if (sl!=null && sl.getSchedules().size()>0) {
			// --- Select the Schedule for the extension ------------
			Schedule resultSchedule = sl.getSchedules().get(0);
			// --- Do not forward empty Schedule's ------------------
			if (resultSchedule.getTechnicalSystemStateEvaluation()!=null || resultSchedule.getTechnicalSystemStateList().size()>0) {
				// --- Put to transmission schedule -----------------
				this.extendScheduleEnergyTransmission(resultSchedule);
			} else {
				// --- Remind for next request build ----------------
				this.lastReceivedScheduleWasEmpty = true;
			}
		}
		this.setProvisioningFinalized();
		this.iswWaitingForRequestResult = false;
	}
	
}
