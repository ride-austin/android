package com.rideaustin.utils;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by yura on 7/23/16.
 */

public class DateHelper {

    private static final String SERVER_TIME_FORMAT = "HH-mm-ss";
    private static final String SERVER_DATE_FORMAT = "yyyy-MM-dd";
    private static final String SERVER_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String UI_DATE_TIME_AT_FORMAT = "MM/dd/yy 'at' hh:mm a";
    private static final String UI_DATE_SHORT_FORMAT = "MMM d, yyyy";
    private static final String UI_DATE_SHORTEST_FORMAT = "MMM d";
    private static final String UI_TIME_AM_PM_FORMAT = "hh:mm a";
    private static final String INITIAL_DATE = "2016-07-07";

    private static Map<String, DateFormat> formatters = new HashMap<>();
    private static DateFormat serverDateTimeFormat;
    private static DateFormat uiSimpleDateFormat;

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private DateHelper() {
    }

    // Date to string converters

    /**
     * Converts date to {@link #SERVER_DATE_FORMAT} representation.
     * Date is not converted, local timezone will be used.
     *
     * @param date date to be formatted
     * @return string representation of date
     */
    public static String dateToServerDateFormat(Date date) {
        return getFormatter(SERVER_DATE_FORMAT).format(date);
    }

    /**
     * Converts date to {@link #SERVER_DATE_TIME_FORMAT} representation.
     * Date will be converted with UTC formatter.
     * If you need to preserve date as-is, convert {@code date} to UTC first using {@link #getUtcDateWithoutShift(Date)}
     *
     * @param date date to be formatter
     * @return string representation of date
     */
    public static String dateToServerDateTimeFormat(Date date) {
        return getServerDateTimeFormat().format(date);
    }

    /**
     * Returns date representation in {@link #UI_DATE_TIME_AT_FORMAT}
     * Date is not converted, local timezone will be used.
     *
     * @return string representation of date
     */
    public static String dateToUiDateTimeAtFormat(Date date) {
        return getFormatter(UI_DATE_TIME_AT_FORMAT).format(date);
    }

    /**
     * Returns date representation in {@link #UI_DATE_SHORT_FORMAT}
     * Date is not converted, local timezone will be used.
     *
     * @return string representation of date
     */
    public static String dateToUiShortDateFormat(Date date) {
        return getFormatter(UI_DATE_SHORT_FORMAT).format(date);
    }

    /**
     * Returns date representation in {@link #UI_DATE_SHORTEST_FORMAT}
     * Date is not converted, local timezone will be used.
     *
     * @return string representation of date
     */
    public static String dateToUiShortestDateFormat(Date date) {
        return getFormatter(UI_DATE_SHORTEST_FORMAT).format(date);
    }

    /**
     * Returns date representation in default simple date format, see {@link SimpleDateFormat#getDateInstance()}
     * Date is not converted, local timezone will be used.
     *
     * @return string representation of date
     */
    public static String dateToSimpleDateFormat(Date date) {
        return getUiSimpleDateFormat().format(date);
    }

    /**
     * Returns date representation in {@link #UI_TIME_AM_PM_FORMAT}
     * Date is not converted, local timezone will be used.
     *
     * @return string representation of date
     */
    public static String dateToUiTimeAmPmFormat(Date date) {
        return getFormatter(UI_TIME_AM_PM_FORMAT).format(date);
    }

    // String to date converters

    /**
     * @param str format must match {@link #SERVER_DATE_FORMAT}
     * @return parsed time.
     * @throws ParseException if format fails
     */
    public static Date dateFromServerDateFormat(String str) throws ParseException {
        return getFormatter(SERVER_DATE_FORMAT).parse(str);
    }

    /**
     * @param str format must match {@link #SERVER_DATE_TIME_FORMAT}
     * @return parsed time.
     * @throws ParseException if format fails
     */
    public static Date dateFromServerDateTimeFormat(String str) throws ParseException {
        return getServerDateTimeFormat().parse(str);
    }

    // shortcuts

    /**
     * Shortcut for date creation from fields
     */
    public static Date getDate(int year, int month, int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, date);
        return calendar.getTime();
    }

    /**
     * Shortcut for date creation from timestamp
     */
    public static Date getDate(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return calendar.getTime();
    }


    // utility methods

    public static Date getInitialDate() {
        try {
            return dateFromServerDateFormat(INITIAL_DATE);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static DateFormat getFormatter(String format) {
        if (!formatters.containsKey(format)) {
            DateFormat formatter = new SimpleDateFormat(format, Locale.US);
            formatters.put(format, formatter);
        }
        return formatters.get(format);
    }

    private static DateFormat getServerDateTimeFormat() {
        if (serverDateTimeFormat == null) {
            serverDateTimeFormat = new SimpleDateFormat(SERVER_DATE_TIME_FORMAT, Locale.US);
            serverDateTimeFormat.setTimeZone(UTC);
        }
        return serverDateTimeFormat;
    }

    private static DateFormat getUiSimpleDateFormat() {
        if (uiSimpleDateFormat == null) {
            uiSimpleDateFormat = SimpleDateFormat.getDateInstance();
        }
        return uiSimpleDateFormat;
    }

    public static Date getUtcDateWithoutShift(Date date) {
        Calendar input = Calendar.getInstance();
        input.setTime(date);
        Calendar utc = Calendar.getInstance(UTC);
        utc.set(Calendar.YEAR, input.get(Calendar.YEAR));
        utc.set(Calendar.MONTH, input.get(Calendar.MONTH));
        utc.set(Calendar.DAY_OF_MONTH, input.get(Calendar.DAY_OF_MONTH));
        utc.set(Calendar.HOUR_OF_DAY, input.get(Calendar.HOUR_OF_DAY));
        utc.set(Calendar.MINUTE, input.get(Calendar.MINUTE));
        utc.set(Calendar.SECOND, input.get(Calendar.SECOND));
        return utc.getTime();
    }

    public static Date getLocalDateWithoutShift(long time) {
        Calendar utc = Calendar.getInstance(UTC);
        utc.setTimeInMillis(time);
        Calendar output = Calendar.getInstance();
        output.set(Calendar.YEAR, utc.get(Calendar.YEAR));
        output.set(Calendar.MONTH, utc.get(Calendar.MONTH));
        output.set(Calendar.DAY_OF_MONTH, utc.get(Calendar.DAY_OF_MONTH));
        output.set(Calendar.HOUR_OF_DAY, utc.get(Calendar.HOUR_OF_DAY));
        output.set(Calendar.MINUTE, utc.get(Calendar.MINUTE));
        output.set(Calendar.SECOND, utc.get(Calendar.SECOND));
        return output.getTime();
    }

    public static Interval getTimeIntervalFromServerFormat(String periodStart, String periodEnd) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(SERVER_TIME_FORMAT);
        DateTime start = dateTimeFormatter.parseDateTime(periodStart);
        DateTime end = dateTimeFormatter.parseDateTime(periodEnd);
        return new Interval(start, end);
    }

}
