package de.enflexit.ea.core.aggregation.dashboard.widget;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.enflexit.ea.core.aggregation.dashboard.DashboardWidgetUpdate;

public class DateTimeWidget extends SimpleTextFieldWidget {

	private static final long serialVersionUID = 1L;
	
	private SimpleDateFormat dateFormat;
	private String dateFormatString;

	/**
	 * Instantiates a new date time widget.
	 * @param id the id
	 */
	public DateTimeWidget(String id) {
		super(id, null);
		this.setTextFieldWidth(10);
	}
	
	/**
	 * Gets the date format.
	 * @return the date format
	 */
	private SimpleDateFormat getDateFormat() {
		if (dateFormat==null) {
			dateFormat = new SimpleDateFormat(this.dateFormatString);
		}
		return dateFormat;
	}
	
	/**
	 * Sets the date format string.
	 * @param dateFormatString the new date format string
	 */
	public void setDateFormatString(String dateFormatString) {
		this.dateFormatString = dateFormatString;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.dashboard.widget.SimpleTextFieldWidget#processUpdate(de.enflexit.ea.core.aggregation.dashboard.DashboardWidgetUpdate)
	 */
	@Override
	public void processUpdate(DashboardWidgetUpdate update) {
		if (update.getValue() instanceof Long) {
			long timeStamp = ((Long)update.getValue()).longValue();
			String dateString = this.getDateFormat().format(new Date(timeStamp));
			this.getJTextfieldValue().setText(dateString);
		} else {
			System.err.println("[" + this.getClass().getSimpleName() + "] Wrong data type in update object: " + update.getValue().getClass().getName());
		}
	};
	
	

}
