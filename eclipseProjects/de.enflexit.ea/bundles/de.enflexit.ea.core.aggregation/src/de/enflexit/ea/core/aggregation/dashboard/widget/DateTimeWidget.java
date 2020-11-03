package de.enflexit.ea.core.aggregation.dashboard.widget;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.enflexit.ea.core.aggregation.dashboard.DashboardWidgetUpdate;

/**
 * A JTextfield-based widget for displaying date and/or time data.  
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public class DateTimeWidget extends SimpleTextFieldWidget {

	private static final long serialVersionUID = 7282968634051694155L;
	private static final int DEFAULT_TEXTFIELD_WIDTH = 10;
	
	private SimpleDateFormat dateFormat;
	private String formatString;

	/**
	 * Instantiates a new date time widget.
	 * @param widgetID the widget ID
	 * @param formatString the format string
	 */
	public DateTimeWidget(String widgetID, String formatString) {
		this(widgetID, formatString, DEFAULT_TEXTFIELD_WIDTH);
	}
	
	/**
	 * Instantiates a new date time widget.
	 * @param widgetID the widget ID
	 * @param formatString the format string
	 * @param textfieldWidth the textfield width
	 */
	public DateTimeWidget(String widgetID, String formatString,	int textfieldWidth) {
		super(widgetID, null);
		this.formatString = formatString;
		this.setTextFieldWidth(textfieldWidth);
	}

	/**
	 * Gets the date format.
	 * @return the date format
	 */
	private SimpleDateFormat getDateFormat() {
		if (dateFormat==null) {
			dateFormat = new SimpleDateFormat(this.formatString);
		}
		return dateFormat;
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
