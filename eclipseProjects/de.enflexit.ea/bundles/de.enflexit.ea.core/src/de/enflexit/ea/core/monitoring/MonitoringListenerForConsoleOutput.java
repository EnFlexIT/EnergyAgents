package de.enflexit.ea.core.monitoring;

import java.util.Date;

import de.enflexit.common.swing.TimeZoneDateFormat;
import energy.FixedVariableList;
import energy.GlobalInfo;
import energy.optionModel.FixedBoolean;
import energy.optionModel.FixedDouble;
import energy.optionModel.FixedInteger;
import energy.optionModel.FixedVariable;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * This {@link MonitoringListener}-implementation writes the monitored {@link TechnicalSystemStateEvaluation}s
 * to the console, i.e. for debugging purposes. 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class MonitoringListenerForConsoleOutput implements MonitoringListener {

	private String dateFormatDefinition = "dd.MM.yy HH:mm:ss:SSS";
	
	private TimeZoneDateFormat dateFormat;
	
	/**
	 * Gets the date format.
	 * @return the date format
	 */
	private TimeZoneDateFormat getDateFormat(){
		if (this.dateFormat == null){
			this.dateFormat = new TimeZoneDateFormat(dateFormatDefinition, GlobalInfo.getInstance().getZoneId());
		}
		return this.dateFormat;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.monitoring.MonitoringListener#onMonitoringEvent(de.enflexit.energyAgent.core.monitoring.MonitoringEvent)
	 */
	@Override
	public void onMonitoringEvent(MonitoringEvent monitoringEvent) {
		StringBuilder output = new StringBuilder();

		TechnicalSystemStateEvaluation tsse = monitoringEvent.getTSSE();
		long timestamp = tsse.getGlobalTime();
		
		String measurementTime = this.getDateFormat().format(new Date(timestamp));
		output.append(measurementTime + "\t");
		output.append("State: " + tsse.getStateID()+ "\t");
		output.append("StateTime: " + tsse.getStateTime() + "\t");

		output.append("Measurements: "+variableListToString(monitoringEvent.getMeasurements(), tsse) + "\t");
		output.append("SetPoints: "+variableListToString(monitoringEvent.getSetpoints(), tsse));

		System.out.println(output);

	}

	/**
	 * Build a String representation from a VariableList.
	 *
	 * @param variableList the variable list
	 * @param tsse the corresponding tsse
	 * @return the String representation
	 */
	private String variableListToString(FixedVariableList variableList, TechnicalSystemStateEvaluation tsse) {
		StringBuffer variableString = new StringBuffer();
		String variableID = "";
		if (variableList != null) {
			for (FixedVariable variable : variableList) {
				variableID = variable.getVariableID();
				variableString.append(variableID + "=");
				if (variable instanceof FixedBoolean) {
					variableString.append(((FixedBoolean) variable).isValue());
				} else if (variable instanceof FixedDouble) {
					variableString.append(((FixedDouble) variable).getValue());
				} else if (variable instanceof FixedInteger) {
					variableString.append(((FixedInteger) variable).getValue());
				} else {
					variableString.append("UNKNOWNTYPE("+variable.getClass().getSimpleName()+")");
				}
	
				variableString.append("; ");
			}
		}
		return variableString.toString();
	}
	
}
