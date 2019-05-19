package com.rideaustin.ui.earnings;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.rideaustin.R;
import com.rideaustin.utils.DateHelper;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static com.rideaustin.utils.Constants.DAYS_COUNT;


/**
 * Created by viacheslavokolitiy on 19.08.2016.
 */
class CalendarViewInteractor {

    private final WeeklyEarningsViewModel viewModel;
    private CaldroidFragment caldroidFragment;
    private ArrayList<Date> selectedDates = new ArrayList<>();
    private Date startDate;

    CalendarViewInteractor(WeeklyEarningsViewModel viewModel) {
        this.viewModel = viewModel;
    }

    void addCalendarView() {
        caldroidFragment = new CaldroidFragment();
        caldroidFragment.setMinDate(getMinimumDate());
        caldroidFragment.setCaldroidListener(caldroidListener);
        viewModel.getView().showCalendar(caldroidFragment);
    }

    void init(Date date) {
        Bundle arguments = new Bundle();
        Calendar calendar = Calendar.getInstance(Locale.US);
        calendar.setTime(date);
        arguments.putInt(CaldroidFragment.MONTH, calendar.get(Calendar.MONTH) + 1);
        arguments.putInt(CaldroidFragment.YEAR, calendar.get(Calendar.YEAR));
        arguments.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.MONDAY);
        caldroidFragment.setArguments(arguments);
        changeWeekBackgroundColor();
    }

    private Date getMinimumDate() {
        return DateHelper.getInitialDate();
    }

    private void changeWeekBackgroundColor() {
        setColorsForRange();
    }

    void setSelectedWeekBackground(Date selectedDateOfWeek) {
        Calendar calendar = Calendar.getInstance(Locale.US);
        calendar.setTime(selectedDateOfWeek);
        calendar.add(Calendar.DAY_OF_YEAR, -1);// -1 is because Sunday is the first day in Locale.US

        ColorDrawable startDateDrawable = getSelectedDayBackgroundColor();
        Map<Date, Drawable> drawableDateMap = new LinkedHashMap<>();

        paintSelectedWeek(calendar, startDateDrawable, drawableDateMap);
    }

    private void paintSelectedWeek(Calendar calendar, ColorDrawable startDateDrawable, Map<Date, Drawable> drawableDateMap) {
        for (int dayId = 0; dayId < DAYS_COUNT; dayId++) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            calendar.add(Calendar.DATE, dayId);

            Date selectedDate = calendar.getTime();
            if (dayId == 0) {
                startDate = selectedDate;
            }
            drawableDateMap.put(selectedDate, startDateDrawable);
            selectedDates.add(selectedDate);
        }

        if (caldroidFragment != null) {
            caldroidFragment.setBackgroundDrawableForDates(drawableDateMap);
        }
    }

    private void setColorsForRange() {
        ColorDrawable startDateDrawable = getSelectedDayBackgroundColor();
        Map<Date, Drawable> drawableDateMap = new LinkedHashMap<>();
        for (int dayId = 0; dayId < DAYS_COUNT; dayId++) {
            Calendar calendar = Calendar.getInstance(Locale.US);
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            calendar.add(Calendar.DATE, dayId);

            Date selectedDate = calendar.getTime();
            drawableDateMap.put(selectedDate, startDateDrawable);
            selectedDates.add(selectedDate);
        }

        if (caldroidFragment != null) {
            caldroidFragment.setBackgroundDrawableForDates(drawableDateMap);
            caldroidFragment.setMaxDate(new Date());
        }
    }

    @NonNull
    private ColorDrawable getSelectedDayBackgroundColor() {
        return new ColorDrawable(ContextCompat.getColor(viewModel.getView().getContext(), R.color.caldroid_holo_blue_dark));
    }

    void refreshCaldroidView() {
        caldroidFragment.clearDisableDates();
        caldroidFragment.clearSelectedDates();
        caldroidFragment.clearBackgroundDrawableForDates(selectedDates);
        caldroidFragment.setMinDate(getMinimumDate());
        caldroidFragment.setMaxDate(new Date());
        caldroidFragment.setShowNavigationArrows(true);
        caldroidFragment.setEnableSwipe(true);
        caldroidFragment.refreshView();
    }

    private CaldroidListener caldroidListener = new CaldroidListener() {
        @Override
        public void onSelectDate(Date date, View view) {
            refreshCaldroidView();
            setSelectedWeekBackground(date);
            viewModel.showEarnings(startDate);
        }
    };
}
