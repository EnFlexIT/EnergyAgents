package de.enflexit.ea.core.blackboard.db.dataModel;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

/**
 * The Class AbstractStateResult serves as .
 */
public abstract class AbstractStateResult implements Serializable {

	private static final long serialVersionUID = -7983870859052646439L;

	private static final String TIME_ZONE_TO_SAVE_IN = "UTC+1";
	
	private static DateTimeFormatter dtf;
	
	/**
	 * Has to return the string for an SQL insert in brackets and with comma separated values (e.g '(a, b, c)');  
	 */
	public abstract String getSQLInsertValueArray();
	
	
	public static DateTimeFormatter getDateTimeFormatter() {
		if (dtf==null) {
			// --- Requires the format '2020-05-19 05:17:15.982' ---- 
			dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.of(TIME_ZONE_TO_SAVE_IN));
		}
		return dtf;
	}
	
	/**
	 * Returns the time stamp as SQL string.
	 *
	 * @param calendar the calendar
	 * @return the time stamp as SQL string
	 */
	public static String getTimeStampAsSQLString(Calendar calendar) {
		String timeStampString = null;
		if (calendar!=null) {
			timeStampString = getDateTimeFormatter().format(calendar.getTime().toInstant());
		}
		return timeStampString;
	}
	
}