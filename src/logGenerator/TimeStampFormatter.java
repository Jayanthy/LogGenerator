package logGenerator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/** 
 * @author Jayanthy
 * Provides 2 methods for UNIX EPOCH time stamp and Java date formatting. 
 * Enables representing time stamp as long and String data types as required
 */

public class TimeStampFormatter {

	/**
	 * This method takes a date input represented as a string as an argument.
	 * It converts the String into Java Date format (yyyy-MM-dd hh:mm) and returns the date in milliseconds
	 * @param theDate This is the string date that needs to be converted
	 * @return long Unix Epoch representation of the given date 
	 * @exception ParseException on input error.
	 */

	public long convertStringtoUnix(String theDate) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		Date date = null;
		try {
			date = format.parse(theDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return (long) date.getTime()/1000;		
	}

	/**
	 * This method takes a UNIX time stamp and returns it as a String
	 * It converts the given long UNIX time stamp into a date represented as a String
	 * @param theDate This is the long UNIX epoch time stamp that needs to be converted
	 * @return String Unix time stamp converted and represented as a String 
	 * @exception Nothing	  
	 */
	
	public String convertUnixtoString(long theDate) {	
		Calendar mydate = Calendar.getInstance();
		mydate.setTimeInMillis(theDate*1000);
		String date = new SimpleDateFormat("dd-MM-yyyy hh:mm").format(mydate.getTime());
		return date;
	}
}
