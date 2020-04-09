package de.enflexit.ea.electricity.scheduleImport;

import java.util.Date;

/**
 * Abstract superclass for schedule importers for electrical networks.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class AbstractElectricalNetworkScheduleImporter {
	private Date startDateTime;
	private long stateDurationMillis;
	private boolean skipFirstRow = true;
	private double cosPhi;
	
	/**
	 * Gets the start date time.
	 * @return the start date time
	 */
	protected Date getStartDateTime() {
		return startDateTime;
	}
	
	/**
	 * Sets the start date time.
	 * @param startDateTime the new start date time
	 */
	protected void setStartDateTime(Date startDateTime) {
		this.startDateTime = startDateTime;
	}
	
	/**
	 * Gets the state duration millis.
	 * @return the state duration millis
	 */
	protected long getStateDurationMillis() {
		return stateDurationMillis;
	}
	
	/**
	 * Sets the state duration millis.
	 * @param stateDurationMillis the new state duration millis
	 */
	protected void setStateDurationMillis(long stateDurationMillis) {
		this.stateDurationMillis = stateDurationMillis;
	}
	
	/**
	 * Checks if is skip first row.
	 * @return true, if is skip first row
	 */
	protected boolean isSkipFirstRow() {
		return skipFirstRow;
	}
	
	/**
	 * Sets the skip first row.
	 * @param skipFirstRow the new skip first row
	 */
	protected void setSkipFirstRow(boolean skipFirstRow) {
		this.skipFirstRow = skipFirstRow;
	}
	
	/**
	 * Gets the cos phi.
	 * @return the cos phi
	 */
	protected double getCosPhi() {
		return cosPhi;
	}
	
	/**
	 * Sets the cos phi.
	 * @param cosPhi the new cos phi
	 */
	protected void setCosPhi(double cosPhi) {
		this.cosPhi = cosPhi;
	}

}
