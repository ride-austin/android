package com.rideaustin.ui.earnings;

import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.db.chart.model.BarSet;
import com.db.chart.renderer.XRenderer;
import com.db.chart.renderer.YRenderer;
import com.db.chart.util.Tools;
import com.db.chart.view.BarChartView;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.SupportedCity;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.api.model.driver.earnings.DriverEarningResponse;
import com.rideaustin.api.model.driver.earnings.DriverEarningResponseContent;
import com.rideaustin.api.model.driver.earnings.DriverOnlineResponse;
import com.rideaustin.api.model.driver.earnings.DriverResponse;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.base.BaseEarningsViewModel;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.DateHelper;
import com.rideaustin.utils.NetworkHelper;
import com.rideaustin.utils.toast.RAToast;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Weeks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static com.rideaustin.utils.CommonConstants.UNEXPECTED_STATE_KEY;

/**
 * Created by vokol on 10.08.2016.
 */
public class WeeklyEarningsViewModel extends BaseEarningsViewModel {
    private static final int X_AXIS_GRIDS = 7;
    private static final int MAX_STEP_VALUE = 100000;

    private static final float BAR_SPACING = 10;
    private static final float BAR_CORNERS = 2;
    private static final float MISSING_EARNING_VALUE = 0.01f;

    public final ObservableBoolean needShowPreviousWeek = new ObservableBoolean(false);
    public final ObservableBoolean needShowNextWeek = new ObservableBoolean(false);
    public final ObservableBoolean needShowCalendarView = new ObservableBoolean(false);
    public final ObservableField<String> weekName = new ObservableField<>();
    public final ObservableField<String> rating = new ObservableField<>();
    public final ObservableField<String> tripCount = new ObservableField<>();
    public final ObservableField<String> totalHours = new ObservableField<>();
    public final ObservableField<String> weeklyEarnings = new ObservableField<>();

    private final WeekEarningsView view;
    private final String[] days;

    private Date firstDayOfTheWeek;
    private Date lastDayOfTheWeek;
    private int weekId = 0;

    private CalendarViewInteractor calendarViewInteractor;
    private List<DriverEarningResponseContent> driverResponseContents;
    private Subscription driverResponseSubscription = Subscriptions.empty();


    WeeklyEarningsViewModel(WeekEarningsView view, int weekId) {
        this.view = view;
        this.weekId = weekId;
        days = App.getInstance().getResources().getStringArray(R.array.week_days);
    }

    int getWeekId() {
        return weekId;
    }

    void init() {
        changeWeek();

        calendarViewInteractor = new CalendarViewInteractor(this);
        calendarViewInteractor.addCalendarView();
        calendarViewInteractor.init(firstDayOfTheWeek);
    }

    void onDestroy() {
        driverResponseSubscription.unsubscribe();
    }

    void setNeedShowCalendarView(boolean needShowCalendarView) {
        this.needShowCalendarView.set(needShowCalendarView);
    }

    void showPreviousWeek() {
        weekId++;
        changeWeek();
    }

    void showNextWeek() {
        weekId--;
        if (weekId < 0) {
            Exception e = new Exception(UNEXPECTED_STATE_KEY);
            Timber.e(e, "showNextWeek: Week is lower than 0");
            weekId = 0;
        }
        changeWeek();
    }

    void showEarnings(Date startDate) {
        DateTime thisMonday = DateTime.now().withDayOfWeek(DateTimeConstants.MONDAY);
        DateTime chosenMonday = new DateTime(startDate).withDayOfWeek(DateTimeConstants.MONDAY);
        weekId = Weeks.weeksBetween(chosenMonday, thisMonday).getWeeks();
        if (weekId < 0) {
            Exception e = new Exception(UNEXPECTED_STATE_KEY);
            Timber.e(e, "showEarnings: Week is lower than 0");
        }
        changeWeek();
    }

    void selectEarning(Earning earning, int day) {
        if (driverResponseContents == null || !isProfitable(earning)) {
            Timber.d("Earning is not profitable");
            return;
        }
        final Intent intent = new Intent(view.getContext(), DailyEarningActivity.class);
        intent.putExtra(Constants.SELECTED_EARNING, earning);
        intent.putExtra(Constants.DAY_ID, day);
        intent.putExtra(Constants.WEEK_ID, weekId);
        if (driverResponseContents.size() != 0) {
            intent.putExtra(Constants.RESPONSE, (Serializable) driverResponseContents);
        }
        view.startActivity(intent);
    }

    private void changeWeek() {
        firstDayOfTheWeek = WeekDateHelper.getDateOfTheWeek(DateTimeConstants.MONDAY, weekId);
        lastDayOfTheWeek = WeekDateHelper.getDateOfTheWeek(DateTimeConstants.SUNDAY, weekId);

        constructUI(firstDayOfTheWeek, lastDayOfTheWeek);
        displayDateRange(firstDayOfTheWeek, lastDayOfTheWeek);
        updateCalendar(firstDayOfTheWeek);

        checkPreviousWeekEnabled();
        checkNextWeekEnabled();
    }

    private void constructUI(Date startWeekDay, Date endWeekDay) {
        driverResponseSubscription.unsubscribe();
        driverResponseSubscription = getDriverResponse(startWeekDay, endWeekDay)
                .subscribeOn(RxSchedulers.network())
                .subscribe(new ApiSubscriber<DriverResponse>(view.getCallback()) {
                    @Override
                    public void onNext(final DriverResponse driverResponse) {
                        doOnDriverResponse(driverResponse);
                    }
                });
    }

    private void displayDateRange(Date startDate, Date endDate) {
        StringBuilder dateRangeBuilder = WeekDateHelper.buildDateRange(view.getContext(), startDate, endDate);
        weekName.set(dateRangeBuilder.toString());
    }

    private void updateCalendar(Date selectedDate) {
        if (calendarViewInteractor != null) {
            calendarViewInteractor.refreshCaldroidView();
            calendarViewInteractor.setSelectedWeekBackground(selectedDate);
        }
    }

    private void checkNextWeekEnabled() {
        needShowNextWeek.set(weekId > 0);
    }

    private void checkPreviousWeekEnabled() {
        boolean hasPreviousWeek = WeekDateHelper.isValidDate(WeekDateHelper.getDateOfTheWeek(DateTimeConstants.MONDAY, weekId));
        needShowPreviousWeek.set(hasPreviousWeek);
    }

    private void showDriverEarningData(List<DriverEarningResponseContent> responseContents,
                                       final DriverOnlineResponse onlineResponse) {
        RxSchedulers.schedule(() -> {
            showDriverRating();
            showTotalHours(onlineResponse);
            showTrips(responseContents);
            showWeeklyEarningAmount(responseContents);
            showWeeklyEarningsReport(responseContents);
        });
    }

    private void showDriverRating() {
        Double rating = App.getDataManager().getCurrentDriver().getRating();
        this.rating.set(String.format(Locale.US, "%.2f", rating));
    }

    private void showTotalHours(DriverOnlineResponse driverOnlineResponse) {
        this.totalHours.set(getTotalHours(driverOnlineResponse));
    }

    private void showTrips(List<DriverEarningResponseContent> responseContents) {
        int tripCount = 0;
        for (DriverEarningResponseContent responseContent : responseContents) {
            if (responseContent.getStatus().equals(RideStatus.COMPLETED.name())
                    || responseContent.getStatus().equals(RideStatus.RIDER_CANCELLED.name()) || responseContent.getStatus().equals(RideStatus.DRIVER_CANCELLED.name())) {
                tripCount++;
            }
        }
        this.tripCount.set(String.format(Locale.US, "%d", tripCount));
    }

    private void showWeeklyEarningAmount(List<DriverEarningResponseContent> responseContents) {
        Double weeklyEarnings = 0d;
        for (DriverEarningResponseContent content : responseContents) {
            if (content.getCompletedOn() > 0 || (content.getCancelledOn() > 0 && (content.getStatus().equals(RideStatus.RIDER_CANCELLED.name()) || content.getStatus().equals(RideStatus.DRIVER_CANCELLED.name())))) {
                String payment = content.getDriverPayment();
                Double paymentAmount = Double.parseDouble(payment);
                weeklyEarnings += paymentAmount;
            }
        }
        this.weeklyEarnings.set(String.format(Locale.US, "$%.2f", weeklyEarnings));
    }

    private void showWeeklyEarningsReport(List<DriverEarningResponseContent> responseContents) {
        Collections.sort(responseContents, (lhs, rhs) -> {
            long lhsTime = getTime(lhs);
            long rhsTime = getTime(rhs);

            // copied from {@code Long#compare}
            return (lhsTime < rhsTime) ? -1 : ((lhsTime == rhsTime) ? 0 : 1);
        });

        List<Earning> earnings = new ArrayList<>();
        for (DriverEarningResponseContent responseContent : responseContents) {
            if (responseContent.getCompletedOn() > 0 && responseContent.getStatus().equals(RideStatus.COMPLETED.name())) {
                parseResponseContents(earnings, responseContent, false);
            } else if (responseContent.getCancelledOn() > 0 && responseContent.getStatus().equals(RideStatus.RIDER_CANCELLED.name())) {
                parseResponseContents(earnings, responseContent, true);
            } else if (responseContent.getCancelledOn() > 0 && responseContent.getStatus().equals(RideStatus.DRIVER_CANCELLED.name())) {
                parseResponseContents(earnings, responseContent, true);
            }
        }

        LinkedHashMap<String, EarningAmount> amountMap = filterEarnings(earnings);
        List<String> weekDays = Arrays.asList(WeekDateHelper.getWeekDays(weekId));
        ArrayList<Earning> list = new ArrayList<>();
        final double zero = 0d;

        if (amountMap.size() > 0 && amountMap.size() == Constants.DAYS_COUNT) {
            for (Map.Entry<String, EarningAmount> entry : amountMap.entrySet()) {
                String dayOfTheWeek = entry.getKey();
                EarningAmount amount = entry.getValue();

                Earning earning = new Earning(dayOfTheWeek, amount);
                list.add(earning);
            }
        } else if (amountMap.size() > 0 && amountMap.size() < Constants.DAYS_COUNT) {
            for (int dayId = 0; dayId < Constants.DAYS_COUNT; dayId++) {

                String day = weekDays.get(dayId);
                EarningAmount dayEarning = amountMap.get(day);
                if (dayEarning == null) {
                    String emptyEarningDay = weekDays.get(dayId);
                    EarningAmount amount = new EarningAmount(zero, zero, zero, zero);
                    amount.setTripsPerformed(Constants.NO_TRIPS);
                    Earning emptyEarning = new Earning(emptyEarningDay, amount);
                    list.add(dayId, emptyEarning);
                } else {
                    String dayOfWeek = weekDays.get(dayId);
                    EarningAmount amount = new EarningAmount(dayEarning.getTotalFare(), dayEarning.getRaFee(), dayEarning.getDriverPayment(), dayEarning.getTipsAmount());
                    amount.setTripsPerformed(dayEarning.getTripsPerformed());
                    Earning emptyEarning = new Earning(dayOfWeek, amount);
                    list.add(dayId, emptyEarning);
                }
            }
        } else {
            for (String weekDay : weekDays) {
                EarningAmount earningAmount = new EarningAmount(zero, zero, zero, zero);
                Earning earning = new Earning(weekDay, earningAmount);
                list.add(earning);
            }
        }
        updateEarningsList(list);
    }

    private void updateEarningsList(List<Earning> list) {
        ListView listView = view.getEarningsListView();
        LayoutInflater inflater = LayoutInflater.from(view.getContext());
        View header = inflater.inflate(R.layout.layout_earnings_header, null);
        if (listView.getHeaderViewsCount() == 0) {
            listView.addHeaderView(header);
        }
        listView.setAdapter(new EarningsAdapter(view.getContext(), list));
    }

    private void showChart(final List<DriverEarningResponseContent> driverEarnings, final float[] paymentChartValues) {
        RxSchedulers.schedule(() -> {
            double totalAmount = getMaxWeeklyPaymentAmount(driverEarnings);
            int stepValue = calculateStepValue(totalAmount);
            final int maxValue = Math.max(1, ((int) Math.ceil(totalAmount / stepValue)) * stepValue);
            constructChartUI(paymentChartValues, BarChartViewBuilder.AXIS_START_VALUE, maxValue, stepValue);
        });
    }

    private void constructChartUI(@NonNull float[] barData, int startValue, int endValue, int step) {
        BarSet barSet = new BarSet(days, barData);
        barSet.setColor(ContextCompat.getColor(App.getInstance(), R.color.green_300));
        BarChartView barChartView = view.getBarChartView();

        if (barChartView.getData().size() > 0) {
            barChartView.updateValues(0, barData);
            barChartView.notifyDataUpdate();
        } else {
            barChartView.addData(barSet);
        }

        barChartView.setBarSpacing(Tools.fromDpToPx(BAR_SPACING));
        barChartView.setRoundCorners(Tools.fromDpToPx(BAR_CORNERS));

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(ContextCompat.getColor(App.getInstance(), R.color.grey_400));

        BarChartViewBuilder barChartViewBuilder = new BarChartViewBuilder(barChartView, paint, days);
        barChartView = barChartViewBuilder.build(startValue, endValue, step);
        barChartView.show();
    }

    private Observable<DriverResponse> getDriverResponse(Date startWeekDay, Date endWeekDay) {
        return Observable.defer(() -> {
            final String convertedToUTCMonday = DateHelper.dateToServerDateTimeFormat(startWeekDay);
            final String convertedToUTCSunday = DateHelper.dateToServerDateTimeFormat(new Date((endWeekDay.getTime() + Constants.DAY_IN_MILLIS - Constants.MINUTE_IN_MILLIS)));

            Driver currentUser = App.getDataManager().getCurrentDriver();

            if (currentUser == null || !currentUser.getUser().isDriver() || !NetworkHelper.isNetworkAvailable()) {
                RAToast.show(R.string.network_error, Toast.LENGTH_SHORT);
                return Observable.empty();
            }

            Observable<List<DriverEarningResponseContent>> driverEarnings = App.getDataManager()
                    .getEarningsService()
                    .getDriverEarnings(currentUser.getId(), convertedToUTCMonday, convertedToUTCSunday)
                    .map(DriverEarningResponse::getContent);

            Observable<DriverOnlineResponse> driverOnline = App.getDataManager()
                    .getEarningsService()
                    .getDriverOnline(currentUser.getId(), convertedToUTCMonday, convertedToUTCSunday);

            return Observable.zip(
                    driverEarnings,
                    driverOnline,
                    DriverResponse::new);
        });
    }

    private void doOnDriverResponse(DriverResponse driverResponse) {
        // NOTE: not main thread!
        // The following methods which touch UI should take care:
        // * showChart()
        // * showDriverEarningData()

        List<DriverEarningResponseContent> responseContents = driverResponse.getDriverEarnings();
        driverResponseContents = responseContents;

        Map<Integer, Double> dailyPayments = getWeeklyPayments(responseContents);
        final List<Float> paymentData = new ArrayList<>();

        for (int dayId = 0; dayId < days.length; dayId++) {
            Double payment = dailyPayments.get(dayId);
            if (payment != null) {
                paymentData.add(payment.floatValue());
            } else {
                paymentData.add(MISSING_EARNING_VALUE);
            }
        }

        final float[] paymentChartData = convertToPrimitives(paymentData);
        showChart(responseContents, paymentChartData);
        showDriverEarningData(responseContents, driverResponse.getDriverOnlineResponse());
    }

    // Helper methods

    private void parseResponseContents(List<Earning> earnings, DriverEarningResponseContent earningResponseContent, boolean rideCancelled) {
        if (!rideCancelled) {
            parseRide(earnings, earningResponseContent, earningResponseContent.getCompletedOn());
        } else {
            parseRide(earnings, earningResponseContent, earningResponseContent.getCancelledOn());
        }
    }

    private void parseRide(List<Earning> earnings, DriverEarningResponseContent earningResponseContent, long timestamp) {
        Double totalFare = Double.parseDouble(earningResponseContent.getSubTotal());
        Double fee = Double.parseDouble(earningResponseContent.getRaFee());
        Double payments = Double.parseDouble(earningResponseContent.getDriverPayment());
        Double tips = earningResponseContent.getTip();

        String date = DateHelper.dateToUiShortestDateFormat(new Date(timestamp));
        EarningAmount amount = new EarningAmount(totalFare, fee, payments, tips);

        Earning earning = new Earning(date, amount);
        earnings.add(earning);
    }

    private Double getMaxWeeklyPaymentAmount(List<DriverEarningResponseContent> responseContents) {
        Map<Integer, Double> grouped = new HashMap<>();
        for (DriverEarningResponseContent content : responseContents) {
            if (content.getCompletedOn() > 0 || (content.getCancelledOn() > 0 && (content.getStatus().equals(RideStatus.RIDER_CANCELLED.name()) || content.getStatus().equals(RideStatus.DRIVER_CANCELLED.name())))) {
                String payment = content.getDriverPayment();
                Double paymentAmount = Double.parseDouble(payment);
                DateTime dateTime = new DateTime(content.getCompletedOn() + content.getCancelledOn());
                final Integer day = dateTime.dayOfWeek().get();
                if (grouped.containsKey(day)) {
                    grouped.put(day, grouped.get(day) + paymentAmount);
                } else {
                    grouped.put(day, paymentAmount);
                }
            }
        }
        final ArrayList<Double> amounts = new ArrayList<>(grouped.values());
        Collections.sort(amounts);
        Double maxAmount = 0d;
        if (amounts.size() > 0) {
            maxAmount = amounts.get(amounts.size() - 1);
        }
        return maxAmount;
    }

    private LinkedHashMap<String, EarningAmount> filterEarnings(List<Earning> earnings) {
        LinkedHashMap<String, EarningAmount> earningsMap = new LinkedHashMap<>();
        int initialTripCount = 1;
        for (Earning earning : earnings) {
            if (earningsMap.containsKey(earning.getDayOfTheWeek())) {
                String totalFare = String.format(Locale.US, "%.2f", earning.getEarningAmount().getTotalFare());
                String raFee = String.format(Locale.US, "%.2f", earning.getEarningAmount().getRaFee());
                String driverAmount = String.format(Locale.US, "%.2f", earning.getEarningAmount().getDriverPayment());
                String tipAmount = String.format(Locale.US, "%.2f", earning.getEarningAmount().getTipsAmount());

                EarningAmount amount = earningsMap.get(earning.getDayOfTheWeek());

                Double updatedFare = Double.parseDouble(totalFare) + amount.getTotalFare();
                Double updatedFee = Double.parseDouble(raFee) + amount.getRaFee();
                Double updatedPayment = Double.parseDouble(driverAmount) + amount.getDriverPayment();
                Double updatedTips = Double.parseDouble(tipAmount) + amount.getTipsAmount();

                EarningAmount updatedAmount = new EarningAmount(updatedFare,
                        updatedFee, updatedPayment, updatedTips);
                updatedAmount.setTripsPerformed(amount.getTripsPerformed() + 1);

                earningsMap.put(earning.getDayOfTheWeek(), updatedAmount);
            } else {
                EarningAmount amount = new EarningAmount(earning.getEarningAmount().getTotalFare(),
                        earning.getEarningAmount().getRaFee(), earning.getEarningAmount().getDriverPayment(), earning.getEarningAmount().getTipsAmount());
                amount.setTripsPerformed(initialTripCount);
                earningsMap.put(earning.getDayOfTheWeek(), amount);
            }

        }

        return earningsMap;
    }

    public Map<Integer, Double> getWeeklyPayments(List<DriverEarningResponseContent> responseContents) {
        Map<Integer, Double> weeklyPayments = new HashMap<>();

        for (DriverEarningResponseContent content : responseContents) {
            Date date = new Date(getTime(content));
            int day = WeekDateHelper.getIndexOfTheWeek(date);
            Double paymentsAmount = Double.parseDouble(content.getDriverPayment());

            if ((content.getStatus().equals(RideStatus.COMPLETED.name())
                    || content.getStatus().equals(RideStatus.RIDER_CANCELLED.name())
                    || content.getStatus().equals(RideStatus.DRIVER_CANCELLED.name())) && day >= 0) {

                if (weeklyPayments.containsKey(day)) {
                    weeklyPayments.put(day, weeklyPayments.get(day) + paymentsAmount);
                } else {
                    weeklyPayments.put(day, paymentsAmount);
                }
            }
        }

        return weeklyPayments;
    }


    private boolean isProfitable(@Nullable Earning earning) {
        if (earning != null) {
            EarningAmount amount = earning.getEarningAmount();
            if (amount != null && (amount.getDriverPayment() + amount.getRaFee() + amount.getTipsAmount()
                    + amount.getTotalFare() + amount.getTripsPerformed()) > 0) {
                return true;
            }
        }
        return false;
    }

    private long getTime(DriverEarningResponseContent earning) {
        long lhsTime = earning.getCompletedOn();
        if (lhsTime == 0L) {
            lhsTime = earning.getCancelledOn();
        }
        if (lhsTime == 0L) {
            lhsTime = earning.getStartedOn();
        }
        return lhsTime;
    }

    private float[] convertToPrimitives(List<Float> driverPayments) {
        float[] floatArray = new float[driverPayments.size()];
        int i = 0;

        for (Float f : driverPayments) {
            floatArray[i++] = (f != null ? f : Float.NaN);
        }
        return floatArray;
    }

    private int calculateStepValue(double totalAmount) {
        int stepValue = (int) (totalAmount / X_AXIS_GRIDS);
        for (int roundStep = 1; roundStep < MAX_STEP_VALUE; roundStep = roundStep * 10) {
            final int modulo = stepValue % roundStep;
            if (modulo == stepValue) {
                return roundStep;
            }
        }
        return stepValue;
    }

    //

    private static class BarChartViewBuilder {
        static final float AXIS_THICKNESS = 2f;
        static final int AXIS_START_VALUE = 0;
        final BarChartView barChartView;
        final Paint paint;
        final String[] weekDays;

        BarChartViewBuilder(BarChartView barChartView, Paint paint, String[] weekDays) {
            this.barChartView = barChartView;
            this.paint = paint;
            this.weekDays = weekDays;
        }

        BarChartView build(int startValue, int endValue, int step) {
            barChartView.setXAxis(false)
                    .setYAxis(true)
                    .setYLabels(YRenderer.LabelPosition.OUTSIDE)
                    .setGrid(weekDays.length, weekDays.length, paint)
                    .setXLabels(XRenderer.LabelPosition.OUTSIDE)
                    .setYLabels(YRenderer.LabelPosition.NONE)
                    .setLabelsColor(ContextCompat.getColor(App.getInstance(), R.color.grey_400))
                    .setAxisColor(ContextCompat.getColor(App.getInstance(), R.color.grey_400))
                    .setAxisThickness(AXIS_THICKNESS)
                    .setAxisBorderValues(startValue, endValue, step)
                    .setYLabels(YRenderer.LabelPosition.OUTSIDE)
                    .setOnEntryClickListener((setIndex, entryIndex, rect) -> {
                    });

            return barChartView;
        }
    }

    WeekEarningsView getView() {
        return view;
    }

    interface WeekEarningsView {
        Context getContext();
        BaseActivityCallback getCallback();
        BarChartView getBarChartView();
        ListView getEarningsListView();
        void showCalendar(Fragment fragment);
        void startActivity(Intent intent);
    }
}
