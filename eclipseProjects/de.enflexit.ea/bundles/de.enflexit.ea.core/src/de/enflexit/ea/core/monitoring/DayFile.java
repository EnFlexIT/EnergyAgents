package de.enflexit.ea.core.monitoring;

import java.io.File;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;

import de.enflexit.ea.core.dataModel.DirectoryHelper;

/**
 * The Class DayFile extends the regular {@link File} and contains
 * further information about the time ranges for which such a file should 
 * be use used.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class DayFile extends File {

	private static final long serialVersionUID = 7295038651763032090L;

	private long timeStampValidFrom;
	private long timeStampValidTo;
	
	/**
	 * Instantiates a new day file.
	 * @param pathname the pathname
	 */
	public DayFile(String pathname) {
		super(pathname);
	}
	/**
	 * Instantiates a new day file.
	 * @param uri the uri
	 */
	public DayFile(URI uri) {
		super(uri);
	}
	/**
	 * Instantiates a new day file.
	 *
	 * @param parent the parent
	 * @param child the child
	 */
	public DayFile(File parent, String child) {
		super(parent, child);
	}
	/**
	 * Instantiates a new day file.
	 *
	 * @param parent the parent
	 * @param child the child
	 */
	public DayFile(String parent, String child) {
		super(parent, child);
	}
	
	/**
	 * Gets the time stamp valid from.
	 * @return the time stamp valid from
	 */
	public long getTimeStampValidFrom() {
		return timeStampValidFrom;
	}
	/**
	 * Sets the time stamp valid from.
	 * @param timeStampValidFrom the new time stamp valid from
	 */
	public void setTimeStampValidFrom(long timeStampValidFrom) {
		this.timeStampValidFrom = timeStampValidFrom;
	}
	
	/**
	 * Gets the time stamp valid to.
	 * @return the time stamp valid to
	 */
	public long getTimeStampValidTo() {
		return timeStampValidTo;
	}
	/**
	 * Sets the time stamp valid to.
	 * @param timeStampValidTo the new time stamp valid to
	 */
	public void setTimeStampValidTo(long timeStampValidTo) {
		this.timeStampValidTo = timeStampValidTo;
	}
	
	// ----------------------------------------------------------------------------------
	// --- From here, static access methods for the creation of a DayFile can be found --
	// ----------------------------------------------------------------------------------
	/**
	 * Creates a new DayFile that considers the specified parameter.
	 *
	 * @param baseDirectory the base directory to use
	 * @param fileName the file name
	 * @param timeStamp the time stamp for which the file should be created
	 * @return the day file
	 */
	public static DayFile createDayFile(File baseDirectory, String fileName, long timeStamp) {
		
		DayFile dayFile = null;
		
		String logFilePath  = DirectoryHelper.checkPathEndForFileSeparator(baseDirectory.getAbsolutePath());
		logFilePath += DirectoryHelper.checkPathEndForFileSeparator(DirectoryHelper.getSubPathForMonth(timeStamp));
		logFilePath += DirectoryHelper.getDayFilePrefix(timeStamp) + fileName;
		
		dayFile = new DayFile(logFilePath);
		dayFile.setTimeStampValidFrom(getTimeStampBeginOfDay(timeStamp));
		dayFile.setTimeStampValidTo(getTimeStampEndOfDay(timeStamp));
		
		return dayFile;
	}
	/**
	 * Gets the time stamp end of day.
	 *
	 * @param currTimeStamp the current time stamp
	 * @return the time stamp end of day
	 */
	public static long getTimeStampEndOfDay(long currTimeStamp) {
		return getDateEndOfDay(new Date(currTimeStamp)).getTime();
	}
	/**
	 * Gets the date end of day.
	 *
	 * @param date the date
	 * @return the date end of day
	 */
	public static Date getDateEndOfDay(Date date) {
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
	    calendar.set(Calendar.HOUR_OF_DAY, 23);
	    calendar.set(Calendar.MINUTE, 59);
	    calendar.set(Calendar.SECOND, 59);
	    calendar.set(Calendar.MILLISECOND, 999);
	    return calendar.getTime();
	}

	/**
	 * Gets the time stamp begin of day.
	 *
	 * @param currTimeStamp the current time stamp
	 * @return the time stamp begin of day
	 */
	public static long getTimeStampBeginOfDay(long currTimeStamp) {
		return getDateEndOfDay(new Date(currTimeStamp)).getTime();
	}
	/**
	 * Gets the date begin of day.
	 *
	 * @param date the date
	 * @return the date begin of day
	 */
	public static Date getDateBeginOfDay(Date date) {
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
	    calendar.set(Calendar.HOUR_OF_DAY, 0);
	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.SECOND, 0);
	    calendar.set(Calendar.MILLISECOND, 0);
	    return calendar.getTime();
	}
	
	
}
