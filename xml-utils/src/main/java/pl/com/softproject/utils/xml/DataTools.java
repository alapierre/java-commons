/*
 * Copyright 2012-06-05 the original author or authors.
 */
package pl.com.softproject.utils.xml;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author Adrian Lapierre {@literal al@alapierre.io}
 */
public class DataTools {

    private static final int MILLISECONDS_IN_DAY = 1000 * 60 * 60 * 24;

    /**
     * Calculates the number of days between start and end dates, taking
     * into consideration leap years, year boundaries etc.
     *
     * @param start the start date
     * @param end the end date, must be later than the start date
     * @return the number of days between the start and end dates
     */
    public static long countDaysBetween(Date start, Date end) {
        if (end.before(start)) {
            return 0;
        }

        //reset all hours mins and secs to zero on start date
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(start);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        long startTime = startCal.getTimeInMillis();

        //reset all hours mins and secs to zero on end date
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(end);
        endCal.set(Calendar.HOUR_OF_DAY, 0);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);
        long endTime = endCal.getTimeInMillis();

        return (endTime - startTime) / MILLISECONDS_IN_DAY;
    }

    public static Date convertXMLCalendar(XMLGregorianCalendar calendar) {
        return calendar.toGregorianCalendar().getTime();
    }

    public static XMLGregorianCalendar toXMLCalendar(Date date) {

        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);

        try {
            XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendarDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DATE), 0);
            return xmlCal;
        } catch (DatatypeConfigurationException ex) {
            return null;
        }
    }

    public static Date addDaysToDate(Date start, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        calendar.add(Calendar.DAY_OF_MONTH, days);

        return calendar.getTime();
    }

    public static String formatDateWithTime(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sXXX");
        return df.format(date);
    }

    public static String formatDate(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(date);
    }
}
