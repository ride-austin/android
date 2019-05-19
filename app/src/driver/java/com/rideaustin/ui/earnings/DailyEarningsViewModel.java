package com.rideaustin.ui.earnings;

import android.content.Context;
import android.databinding.ObservableField;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.api.model.driver.earnings.DriverEarningResponseContent;
import com.rideaustin.api.model.driver.earnings.DriverOnlineResponse;
import com.rideaustin.api.model.driver.earnings.End;
import com.rideaustin.api.model.driver.earnings.Start;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.base.BaseEarningsViewModel;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.DateHelper;
import com.rideaustin.utils.ImageHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import rx.subscriptions.CompositeSubscription;

import static com.rideaustin.utils.MathUtils.compare;

/**
 * Created by vokol on 22.08.2016.
 */
public class DailyEarningsViewModel extends BaseEarningsViewModel {

    private final DailyEarningsView view;
    private final Driver currentUser;

    public final ObservableField<String> dayName = new ObservableField<>();
    public final ObservableField<String> rating = new ObservableField<>();
    public final ObservableField<String> tripCount = new ObservableField<>();
    public final ObservableField<String> onlineHours = new ObservableField<>();
    public final ObservableField<String> dailyEarnings = new ObservableField<>();

    private TripHistoryAdapter adapter;
    private int lastExpandedPosition = -1;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private List<Target<Bitmap>> glideTargets = new ArrayList<>();
    private Set<Long> mapRequests = new HashSet<>();

    DailyEarningsViewModel(DailyEarningsView view) {
        this.view = view;
        this.currentUser = App.getDataManager().getCurrentDriver();
    }

    @SuppressWarnings("unchecked")
    void init(Bundle bundle) {
        int weekId = bundle.getInt(Constants.WEEK_ID, 0);
        int dayId = bundle.getInt(Constants.DAY_ID, 0);
        Earning earning = (Earning) bundle.getSerializable(Constants.SELECTED_EARNING);
        List<DriverEarningResponseContent> response = bundle.containsKey(Constants.RESPONSE)
                ? (List<DriverEarningResponseContent>) bundle.getSerializable(Constants.RESPONSE) : null;

        showDay(weekId, dayId);
        showRating();
        showTripCount(earning);
        showEarnings(earning);
        showDriverData(weekId, dayId,response);
    }

    void loadMapIntoImageView(ImageView imageView, ProgressBar progressBar, TripHistoryContent content) {
        if (content.getMapUrl() != null && (content.getMapUrl().contains("http") || content.getMapUrl().isEmpty())) {
            // load map if url is:
            // * valid
            // * empty string (map couldn't be loaded)
            progressBar.setVisibility(View.VISIBLE);
            glideTargets.add(ImageHelper.loadImageIntoView(imageView, content.getMapUrl(), 0, R.drawable.map_placeholder, new MapRequestListener(progressBar)));
        } else if (!mapRequests.contains(content.getRideId())) {
            mapRequests.add(content.getRideId());
            subscriptions.add(App.getDataManager().getRidesService()
                    .getRideMap(content.getRideId())
                    .subscribeOn(RxSchedulers.network())
                    .observeOn(RxSchedulers.main())
                    .subscribe(new ApiSubscriber2<com.rideaustin.api.model.Map>(false) {
                        @Override
                        public void onNext(com.rideaustin.api.model.Map map) {
                            super.onNext(map);
                            // If service doesn't provide map url
                            // set it to empty string to prevent cycle requests
                            content.setMapUrl(!TextUtils.isEmpty(map.getUrl()) ? map.getUrl() : "");
                            // RA-9326: can't call loadMapIntoImageView() directly
                            // strange, but often load is never completed
                            adapter.notifyDataSetChanged();
                        }
                    }));
        }

    }

    void onListGroupExpanded(int groupPosition) {
        if (groupPosition != lastExpandedPosition && lastExpandedPosition < adapter.getGroupCount()) {
            view.getTripHistoryList().collapseGroup(lastExpandedPosition);
        }
        lastExpandedPosition = groupPosition;
    }

    void onDestroy() {
        mapRequests.clear();
        subscriptions.clear();
        for (Target<Bitmap> target : glideTargets) {
            Glide.with(App.getInstance()).clear(target);
        }
        glideTargets.clear();
    }

    private void showRating() {
        if (App.getDataManager().getCurrentDriver() != null) {
            double rating = App.getDataManager().getCurrentDriver().getRating();
            this.rating.set(String.format(Locale.US, "%.2f", rating));
        }
    }

    private void showTripCount(Earning earning) {
        int tripsCount = earning.getEarningAmount().getTripsPerformed();
        this.tripCount.set(String.format(Locale.US, "%d", tripsCount));
    }

    private void showEarnings(Earning earning) {
        String amount = getMoneyString(String.format(Locale.US, "%.2f", earning.getEarningAmount().getDriverPayment()));
        dailyEarnings.set(amount);
    }

    private void showDay(int weekId, int dayId) {
        StringBuilder dayBuilder = new StringBuilder();
        WeekDays weekDay = WeekDays.values()[dayId > 0 ? dayId - 1 : dayId];
        String dayName = weekDay.name().substring(0, 3);
        String[] weekDays = WeekDateHelper.getWeekDays(weekId);
        dayBuilder.append(dayName).append(", ").append(weekDays[dayId - 1]);
        this.dayName.set(dayBuilder.toString());
    }

    private void showDriverData(int weekId, int dayId, List<DriverEarningResponseContent> response) {
        Date[] weekDates = WeekDateHelper.getWeekDaysInDateFormat(weekId);
        final Date selectedDate = weekDates[dayId > 0 ? dayId - 1 : dayId];
        final String selectedDateUTC = DateHelper.dateToServerDateTimeFormat(selectedDate);

        long selectedDateInMillis = selectedDate.getTime();
        Date midnight = new Date((selectedDateInMillis + Constants.DAY_IN_MILLIS) - Constants.MINUTE_IN_MILLIS);
        String selectedDateMidnightUTC = DateHelper.dateToServerDateTimeFormat(midnight);

        getDriverOnline(selectedDateUTC, selectedDateMidnightUTC);
        showDriverEarnings(selectedDate, response);
    }

    private void getDriverOnline(String selectedDateUtc, String selectedDateMidnightUtc) {
        subscriptions.add(App.getDataManager().getEarningsService()
                .getDriverOnline(currentUser.getId(), selectedDateUtc, selectedDateMidnightUtc)
                .subscribeOn(RxSchedulers.network())
                .subscribe(new ApiSubscriber2<DriverOnlineResponse>(view.getCallback()) {
                    @Override
                    public void onNext(DriverOnlineResponse driverOnlineResponse) {
                        super.onNext(driverOnlineResponse);
                        onlineHours.set(getTotalHours(driverOnlineResponse));
                    }
                }));
    }

    @SuppressWarnings("unchecked")
    private void showDriverEarnings(Date startDate, List<DriverEarningResponseContent> response) {
        if (response == null) {
            return;
        }

        List<TripHistory> tripHistories = new ArrayList<>();
        Date endDate = new Date((startDate.getTime() + Constants.DAY_IN_MILLIS) - Constants.MINUTE_IN_MILLIS);

        for (DriverEarningResponseContent earning : response) {
            boolean isCancelled = earning.getCancelledOn() > 0;
            Date completedDate = new Date(earning.getCompletedOn());
            Date cancelledDate = new Date(earning.getCancelledOn());

            if (!isCancelled && isWithinRange(completedDate, startDate, endDate)) {
                tripHistories.add(createTrip(true, earning));
            } else if (isCancelled && isWithinRange(cancelledDate, startDate, endDate)) {
                tripHistories.add(createTrip(false, earning));
            }
        }

        mapRequests.clear();
        if (adapter == null) {
            adapter = new TripHistoryAdapter(view.getContext(), tripHistories, this);
            view.getTripHistoryList().setAdapter(adapter);
        } else {
            adapter.setTripHistories(tripHistories);
            adapter.notifyDataSetChanged();
        }
    }

    private TripHistory createTrip(boolean complete, DriverEarningResponseContent earning) {
        long startedOn = complete ? earning.getStartedOn() : earning.getCancelledOn();
        long endedOn = complete ? earning.getCompletedOn() : earning.getCancelledOn();

        // NOTE: these are in local timezone.
        // If need to get UTC representation, please use DateHelper#timeToUiTimeAmPmFormat(long)
        String startedOnStr = DateHelper.dateToUiTimeAmPmFormat(new Date(startedOn));
        String endedOnStr = DateHelper.dateToUiTimeAmPmFormat(new Date(endedOn));

        String carInfo = view.getContext().getString(R.string.earning_car_info, earning.getRequestedCarType().getTitle(), earning.getCar().getModel(), earning.getCar().getMake());
        String payment = view.getContext().getString(R.string.money, earning.getDriverPayment());
        TripHistoryHeader header = new TripHistoryHeader(endedOn, endedOnStr, carInfo, payment);

        TripHistoryContent content = new TripHistoryContent();
        content.setRideId(earning.getId());
        content.setMapUrl(earning.getRideMap());
        content.setHomeAddress(earning.getStart());
        content.setDestinationAddress(earning.getEnd());
        content.setStartedOn(startedOn);
        content.setEndedOn(endedOn);
        content.setStartedOnStr(startedOnStr);
        content.setEndedOnStr(endedOnStr);
        content.setBaseFare(getMoneyString(!TextUtils.isEmpty(earning.getBaseFare()) ? earning.getBaseFare() : "0"));
        content.setDistanceFare(getMoneyString(!TextUtils.isEmpty(earning.getDistanceFare()) ? earning.getDistanceFare() : "0"));
        content.setTimeFare(getMoneyString(!TextUtils.isEmpty(earning.getTimeFare()) ? earning.getTimeFare() : "0"));
        content.setSubTotal(getMoneyString(!TextUtils.isEmpty(earning.getSubTotal()) ? earning.getSubTotal() : "0"));
        content.setRideAustinFee(getMoneyString(!TextUtils.isEmpty(earning.getRaFee()) ? earning.getRaFee() : "0"));
        content.setYourEarning(getMoneyString(!TextUtils.isEmpty(earning.getDriverPayment()) ? earning.getDriverPayment() : "0"));
        content.setRideCompleted(complete);
        content.setTipAmount(getMoneyString(String.format(Locale.US, "%.2f", earning.getTip())));
        content.setTotalFare(getMoneyString(!TextUtils.isEmpty(earning.getTotalFare()) ? earning.getTotalFare() : "0"));
        content.setPriorityFareAmount(isPriorityFare(earning) ? String.format(Locale.US, "%.2f", earning.getSurgeFactor()) : "");
        content.setSurgeFare(earning.getSurgeFare() != null && isPriorityFare(earning) ? earning.getSurgeFare() : "");
        return new TripHistory(header, content);
    }

    private boolean isPriorityFare(DriverEarningResponseContent earning) {
        return earning.getSurgeFactor() > 1;
    }

    private boolean isWithinRange(Date testDate, Date startDate, Date endDate) {
        return testDate.getTime() >= startDate.getTime() &&
                testDate.getTime() <= endDate.getTime();
    }

    private String getMoneyString(String amount) {
        return view.getContext().getString(R.string.money, amount);
    }

    static class TripHistoryHeader implements Comparable<TripHistoryHeader> {

        private final long endedOn;
        private final String endedOnStr;
        private final String carInfo;
        private final String payment;

        TripHistoryHeader(long endedOn, String endedOnStr, String carInfo, String payment) {
            this.endedOn = endedOn;
            this.endedOnStr = endedOnStr;
            this.carInfo = carInfo;
            this.payment = payment;
        }

        String getCarInfo() {
            return carInfo;
        }

        long getEndedOn() {
            return endedOn;
        }

        public String getEndedOnStr() {
            return endedOnStr;
        }

        String getPayment() {
            return payment;
        }

        @Override
        public int compareTo(@NonNull TripHistoryHeader another) {
            return compare(endedOn, another.endedOn);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TripHistoryHeader that = (TripHistoryHeader) o;

            if (endedOn != that.endedOn) return false;
            if (carInfo != null ? !carInfo.equals(that.carInfo) : that.carInfo != null)
                return false;
            return payment != null ? payment.equals(that.payment) : that.payment == null;
        }

        @Override
        public int hashCode() {
            int result = (int) (endedOn ^ (endedOn >>> 32));
            result = 31 * result + (carInfo != null ? carInfo.hashCode() : 0);
            result = 31 * result + (payment != null ? payment.hashCode() : 0);
            return result;
        }
    }

    static class TripHistoryContent implements Comparable<TripHistoryContent> {
        private long rideId;
        private String mapUrl;
        private Start homeAddress;
        private End destinationAddress;
        private long startedOn;
        private long endedOn;
        private String startedOnStr;
        private String endedOnStr;
        private String baseFare;
        private String distanceFare;
        private String timeFare;
        private String subTotal;
        private String rideAustinFee;
        private String yourEarning;
        private boolean isRideCompleted;
        private String tipAmount;
        private String totalFare;
        private String priorityFareAmount;
        private String surgeFare;

        String getMapUrl() {
            return mapUrl;
        }

        void setMapUrl(String mapUrl) {
            this.mapUrl = mapUrl;
        }

        Start getHomeAddress() {
            return homeAddress;
        }

        void setHomeAddress(Start homeAddress) {
            this.homeAddress = homeAddress;
        }

        End getDestinationAddress() {
            return destinationAddress;
        }

        void setDestinationAddress(End destinationAddress) {
            this.destinationAddress = destinationAddress;
        }

        long getStartedOn() {
            return startedOn;
        }

        void setStartedOn(long startedOn) {
            this.startedOn = startedOn;
        }

        long getEndedOn() {
            return endedOn;

        }

        void setEndedOn(long endedOn) {
            this.endedOn = endedOn;
        }

        public String getStartedOnStr() {
            return startedOnStr;
        }

        public void setStartedOnStr(String startedOnStr) {
            this.startedOnStr = startedOnStr;
        }

        public String getEndedOnStr() {
            return endedOnStr;
        }

        public void setEndedOnStr(String endedOnStr) {
            this.endedOnStr = endedOnStr;
        }

        String getDistanceFare() {
            return distanceFare;
        }

        void setDistanceFare(String distanceFare) {
            this.distanceFare = distanceFare;
        }

        String getTimeFare() {
            return timeFare;
        }

        void setTimeFare(String timeFare) {
            this.timeFare = timeFare;
        }

        public String getSubTotal() {
            return subTotal;
        }

        public void setSubTotal(String totalFare) {
            this.subTotal = totalFare;
        }

        String getRideAustinFee() {
            return rideAustinFee;
        }

        void setRideAustinFee(String rideAustinFee) {
            this.rideAustinFee = rideAustinFee;
        }

        String getYourEarning() {
            return yourEarning;
        }

        void setYourEarning(String yourEarning) {
            this.yourEarning = yourEarning;
        }

        String getBaseFare() {
            return baseFare;
        }

        void setBaseFare(String baseFare) {
            this.baseFare = baseFare;
        }

        long getRideId() {
            return rideId;
        }

        void setRideId(long rideId) {
            this.rideId = rideId;
        }

        boolean isRideCompleted() {
            return isRideCompleted;
        }

        void setRideCompleted(boolean rideCompleted) {
            isRideCompleted = rideCompleted;
        }

        public String getPriorityFareAmount() {
            return priorityFareAmount;
        }

        public void setPriorityFareAmount(String priorityFareAmount) {
            this.priorityFareAmount = priorityFareAmount;
        }

        public String getSurgeFare() {
            return surgeFare;
        }

        public void setSurgeFare(String surgeFare) {
            this.surgeFare = surgeFare;
        }

        public void setTipAmount(String tipAmount) {
            this.tipAmount = tipAmount;
        }

        public String getTipAmount() {
            return tipAmount;
        }

        public void setTotalFare(String totalFare) {
            this.totalFare = totalFare;
        }

        public String getTotalFare() {
            return totalFare;
        }

        @Override
        public int compareTo(@NonNull TripHistoryContent another) {
            return compare(endedOn, another.endedOn);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TripHistoryContent that = (TripHistoryContent) o;

            return rideId == that.rideId;
        }

        @Override
        public int hashCode() {
            return (int) (rideId ^ (rideId >>> 32));
        }
    }

    private class MapRequestListener implements RequestListener<Bitmap> {
        ProgressBar progressBar;

        public MapRequestListener(ProgressBar progressBar) {
            this.progressBar = progressBar;
        }

        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
            return false;
        }

        @Override
        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
            progressBar.setVisibility(View.GONE);

            if (adapter != null && dataSource != DataSource.MEMORY_CACHE) {
                // notify only if resource was loaded for the first time
                // (adapter may require to update currently opened group)
                // other cases won't require this
                adapter.notifyDataSetChanged();
            }
            return false;
        }
    }

    interface DailyEarningsView {
        Context getContext();
        BaseActivityCallback getCallback();
        ExpandableListView getTripHistoryList();
    }
}
