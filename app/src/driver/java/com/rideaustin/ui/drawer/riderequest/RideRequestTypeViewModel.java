package com.rideaustin.ui.drawer.riderequest;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.DataManager;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.ProgressCallback;
import com.rideaustin.manager.RideRequestManager;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.toast.RAToast;

import java.util.ArrayList;
import java.util.List;

import java8.util.Optional;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static com.rideaustin.utils.CommonConstants.DIRECT_CONNECT_DRIVER_TYPE;
import static com.rideaustin.utils.CommonConstants.WOMEN_ONLY_DRIVER_TYPE;

/**
 * @author sdelaysam.
 */

public class RideRequestTypeViewModel {

    public final ObservableBoolean filtersAvailable = new ObservableBoolean(false);
    public final ObservableBoolean womanModeAtCenter = new ObservableBoolean(false);
    public final ObservableBoolean womanModeAtRight = new ObservableBoolean(false);
    public final ObservableBoolean directAvailable = new ObservableBoolean(false);
    public final ObservableBoolean noneChecked = new ObservableBoolean(true);
    public final ObservableBoolean womanModeChecked = new ObservableBoolean(false);
    public final ObservableBoolean directChecked = new ObservableBoolean(false);
    public final ObservableField<String> summary = new ObservableField<>("");

    private final RideRequestManager rideRequestManager;
    private final DataManager dataManager;
    private final View view;

    private Subscription initialSubscription = Subscriptions.empty();
    private Subscription carTypeSubscription = Subscriptions.empty();
    private Subscription availableSubscription = Subscriptions.empty();
    private Subscription selectedSubscription = Subscriptions.empty();
    private Subscription summarySubscription = Subscriptions.empty();

    private Optional<RideRequestViewData> data = Optional.empty();

    RideRequestTypeViewModel(View view) {
        this.view = view;
        dataManager = App.getDataManager();
        rideRequestManager = App.getInstance().getRideRequestManager();

        updateFilters();

        initialSubscription = Observable.zip(rideRequestManager.getCarTypes(),
                rideRequestManager.getAvailableCarTypes(false),
                rideRequestManager.getSelectedCarTypes(false),
                RideRequestViewData::new)
                .take(1)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .doOnUnsubscribe(() -> view.getCallback().hideProgress())
                .subscribe(new ApiSubscriber2<RideRequestViewData>(view.getCallback()) {
                    @Override
                    public void onNext(RideRequestViewData data) {
                        super.onNext(data);
                        doOnInitialData(data);
                    }
                });
    }

    private void updateFilters() {
        boolean womanModeAvailable = rideRequestManager.isWomenOnlyAvailable();
        directAvailable.set(rideRequestManager.isDirectConnectAvailable());
        womanModeAtCenter.set(womanModeAvailable && directAvailable.get());
        womanModeAtRight.set(womanModeAvailable && !directAvailable.get());
        filtersAvailable.set(womanModeAvailable || directAvailable.get());

        boolean directConnect = rideRequestManager.isDirectConnect();
        boolean womanMode = !directConnect && rideRequestManager.isWomenOnly();
        womanModeChecked.set(womanMode);
        directChecked.set(directConnect);
        noneChecked.set(!(womanMode || directConnect));
    }

    void onTypeClicked(RideRequestType type) {
        if (!type.isActive || !data.isPresent()) {
            return;
        }
        if (type.isSelected && getSelectedCount() <= 1) {
            RAToast.showShort(R.string.car_type_save_warning);
            return;
        }
        if (type.isSelected) {
            rideRequestManager.saveSelectedCategories(data.get().getSelectedMinus(type.carType));
        } else {
            rideRequestManager.saveSelectedCategories(data.get().getSelectedPlus(type.carType));
        }
    }

    void onWomenModeSelected(boolean selected) {
        if (this.data.isPresent()) {
            rideRequestManager.setWomenOnly(selected);
        }
    }

    void onDirectConnectSelected(boolean selected) {
        if (this.data.isPresent()) {
            rideRequestManager.setDirectConnect(selected);
        }
    }

    public String getCarModel() {
        return dataManager.getDriver()
                .map(Driver::getSelectedCar)
                .filter(Optional::isPresent) // RA-14367
                .map(Optional::get)
                .map(car -> car.getMake() + " " + car.getModel())
                .orElse("");
    }

    void onDestroy() {
        initialSubscription.unsubscribe();
        carTypeSubscription.unsubscribe();
        availableSubscription.unsubscribe();
        selectedSubscription.unsubscribe();
        summarySubscription.unsubscribe();
    }

    private void doOnInitialData(RideRequestViewData data) {
        this.data = Optional.of(data);
        carTypeSubscription = rideRequestManager.getCarTypes().skip(1)
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnCarTypesChanged, Timber::e);
        availableSubscription = rideRequestManager.getAvailableCarTypes(false).skip(1)
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnAvailableChanged, Timber::e);
        selectedSubscription = rideRequestManager.getSelectedCarTypes(false).skip(1)
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnSelectedChanged, Timber::e);
        updateView();
    }

    private void doOnCarTypesChanged(List<RequestedCarType> carTypes) {
        data.ifPresent(data -> data.carTypes = carTypes);
        updateView();
    }

    private void doOnAvailableChanged(List<RequestedCarType> available) {
        data.ifPresent(data -> data.available = available);
        updateView();
    }

    private void doOnSelectedChanged(List<RequestedCarType> selected) {
        data.ifPresent(data -> data.selected = selected);
        updateView();
    }

    private void updateView() {
        if (this.data.isPresent()) {
            RideRequestViewData data = this.data.get();
            List<RideRequestType> types = new ArrayList<>();
            for (RequestedCarType carType : data.carTypes) {
                RideRequestType type = new RideRequestType();
                type.carType = carType;
                type.name = carType.getTitle();
                type.carIcon = carType.getPlainIconUrl();
                type.isActive = data.available.contains(carType);
                type.isSelected = type.isActive && data.selected.contains(carType);
                types.add(type);
            }
            view.onRideRequestTypes(types);
            updateSummary();
        } else {
            view.onClear();
            summary.set("");
        }
    }

    private int getSelectedCount() {
        return data.map(RideRequestViewData::numSelected).orElse(0);
    }

    private void updateSummary() {
        summarySubscription.unsubscribe();
        summarySubscription = getSummaryData()
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnSummaryData, throwable -> {
                    Timber.e(throwable);
                    summary.set("");
                    updateFilters();
                });
    }

    private void doOnSummaryData(RideRequestSummaryData summaryData) {
        Context context = App.getInstance();
        if (summaryData.isAnySelected()) {
            StringBuilder sb = new StringBuilder();
            sb.append(context.getString(R.string.ride_request_description));
            sb.append(summaryData.getSelectedAsString());
            if (rideRequestManager.isDirectConnect()) {
                sb.append(context.getString(R.string.ride_request_dc_only));
            } else if (rideRequestManager.isWomenOnly()) {
                sb.append(context.getString(R.string.ride_request_female_only));
            } else {
                if (rideRequestManager.isWomenOnlyAvailable()) {
                    sb.append(context.getString(R.string.ride_request_and));
                    sb.append(summaryData.getWomenOnlyAvailableAsString());
                    sb.append(context.getString(R.string.ride_request_female_driver_requests));
                }
                if (rideRequestManager.isDirectConnectAvailable()) {
                    sb.append(context.getString(R.string.ride_request_and));
                    sb.append(summaryData.getDirectConnectAvailableAsString());
                    sb.append(context.getString(R.string.ride_request_dc_requests));
                }
            }
            if (summaryData.isAllSelected()) {
                sb.append(context.getString(R.string.ride_request_all_selected));
            } else {
                sb.append(context.getString(R.string.ride_request_select_others));
            }
            summary.set(sb.toString());
        } else {
            summary.set(context.getString(R.string.ride_request_please_select));
        }
        updateFilters();
    }

    private Observable<RideRequestSummaryData> getSummaryData() {
        RideRequestViewData data = this.data.get();
        return Observable.zip(
                rideRequestManager.getCustomTypeCarTypes(WOMEN_ONLY_DRIVER_TYPE),
                rideRequestManager.getCustomTypeCarTypes(DIRECT_CONNECT_DRIVER_TYPE),
                (womenOnly, directConnect) -> new RideRequestSummaryData(data.available, data.selected, womenOnly, directConnect));
    }

    interface View {
        ProgressCallback getCallback();
        void onRideRequestTypes(List<RideRequestType> types);
        void onClear();
    }

    static class RideRequestType {
        boolean isActive;
        boolean isSelected;
        String name;
        String carIcon;
        RequestedCarType carType;
    }

}
