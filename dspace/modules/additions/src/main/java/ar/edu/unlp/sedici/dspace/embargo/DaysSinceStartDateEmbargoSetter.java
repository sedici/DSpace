package ar.edu.unlp.sedici.dspace.embargo;

import java.util.Date;
import java.util.List;

import org.dspace.content.DCDate;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;

/**
 * Plugin implementation of the embargo setting function. The parseTerms()
 * method performs a look-up to a table that relates a terms expression to a
 * fixed number of days. Table constructed from a dspace.cfg property with
 * syntax:
 * 
 * embargo.terms.days = 90 days:90,1 year:365,2 years:730
 * 
 * 
 * If field named embargo.field.startDate (dc.date.created by default) is empty , NOW is used
 * as relative Date in order to calculate the end of embargo period (lift)
 */
public class DaysSinceStartDateEmbargoSetter extends DaysEmbargoSetter {


//	private Properties termProps = new Properties();

	private static String startDate = "dc.date.issued";


	protected Date getEmbargoStartDate(Item item) {
		List<MetadataValue> embargoStartDates = getItemService().getMetadataByMetadataString(item, startDate);
		if (embargoStartDates.size() > 0)
			return new DCDate(embargoStartDates.get(0).getValue()).toDate();
		else 
			return null;
	}
}
