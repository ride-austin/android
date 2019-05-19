package com.rideaustin.ui.earnings;

import android.content.Context;
import android.support.annotation.NonNull;

import com.rideaustin.R;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.DateHelper;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Sergey Petrov on 23/06/2017.
 */

public class WeekDateHelper {

    public static Date getDateOfTheWeek(int dayOfTheWeek) {
        LocalDate now = new LocalDate();
        LocalDate dayOfWeek = now.withDayOfWeek(dayOfTheWeek);

        return dayOfWeek.toDate();
    }

    public static Date getDateOfTheWeek(int dayOfWeek, int minusWeeks) {
        LocalDate now = new LocalDate();
        LocalDate lastWeek = now.minusWeeks(minusWeeks);

        LocalDate dayOfLastWeek = lastWeek.withDayOfWeek(dayOfWeek);

        return dayOfLastWeek.toDate();
    }

    public static StringBuilder buildDateRange(Context context, Date startDate, Date endDate) {
        StringBuilder dateRangeBuilder = new StringBuilder();
        dateRangeBuilder
                .append(new SimpleDateFormat("MMM d", Locale.US).format(startDate))
                .append(context.getString(R.string.text_date_delimiter))
                .append(new SimpleDateFormat("MMM d, yyyy", Locale.US).format(endDate));

        return dateRangeBuilder;
    }

    // -1 : Calender#get returns values starting by 1, it is in order to make it 0 based
    // -1 : java Calender starts week by Sunday, however we use Monday on our apps.
    public static int getIndexOfTheWeek(Date day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day);
        int dayOfTheWeek = calendar.get(Calendar.DAY_OF_WEEK) - 2 + 7;
        dayOfTheWeek %= 7;
        return dayOfTheWeek;
    }

    private static long getCurrentMondayInMillis() {
        return getDateOfTheWeek(DateTimeConstants.MONDAY).getTime();
    }

    public static String[] getWeekDays(int week) {

        Date sunday = new Date(getCurrentMondayInMillis());

        long nextOrPreviousSundayInMillis = getMonday(week);
        Date nextOrPreviousSunday = new Date(nextOrPreviousSundayInMillis);
        DateFormat format = new SimpleDateFormat("MMM d", Locale.US);
        Calendar calendar = setFirstDayOfSelectedWeek(week, sunday, nextOrPreviousSunday);

        String[] days = new String[Constants.DAYS_COUNT];
        for (int i = 0; i < Constants.DAYS_COUNT; i++) {
            days[i] = format.format(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return days;
    }

    public static Date[] getWeekDaysInDateFormat(int week) {
        Date sunday = new Date(getCurrentMondayInMillis());

        long nextOrPreviousSundayInMillis = getMonday(week);
        Date nextOrPreviousSunday = new Date(nextOrPreviousSundayInMillis);

        Calendar calendar = setFirstDayOfSelectedWeek(week, sunday, nextOrPreviousSunday);

        Date[] dates = new Date[Constants.DAYS_COUNT];
        for (int i = 0; i < Constants.DAYS_COUNT; i++) {
            dates[i] = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return dates;
    }

    private static long getMonday(int week) {
        return getDateOfTheWeek(DateTimeConstants.MONDAY, week).getTime();
    }

    @NonNull
    private static Calendar setFirstDayOfSelectedWeek(int week, Date sunday, Date nextOrPreviousSunday) {
        Calendar calendar = Calendar.getInstance(Locale.US);
        calendar.setTime(week == 0 ? sunday : nextOrPreviousSunday);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        return calendar;
    }

    public static boolean isValidDate(Date dateForCompare) {
        Date initialDate = DateHelper.getInitialDate();

        long millisToCompare = dateForCompare.getTime();
        long initialMillis = initialDate.getTime();

        return initialMillis < millisToCompare;
    }

}
