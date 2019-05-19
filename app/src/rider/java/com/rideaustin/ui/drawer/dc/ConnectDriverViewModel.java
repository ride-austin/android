package com.rideaustin.ui.drawer.dc;

import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.AvatarType;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.api.model.driver.DirectConnectDriver;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.entities.GeoPosition;
import com.rideaustin.events.RideStatusEvent;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.RxBaseViewModel;
import com.rideaustin.ui.payment.PaymentType;
import com.rideaustin.ui.ride.RideStatusService;
import com.rideaustin.ui.utils.UIUtils;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.DeviceInfoUtil;
import com.rideaustin.utils.RetryWhenNoNetwork;
import com.rideaustin.utils.RxImageLoader;
import com.rideaustin.utils.SingleSubject;
import com.rideaustin.utils.location.LocationHelper;

import rx.Observable;
import timber.log.Timber;

import static com.rideaustin.api.model.RideStatus.RIDER_CANCELLED;

/**
 * Created by hatak on 24.10.2017.
 */

public class ConnectDriverViewModel extends RxBaseViewModel {

    public final ObservableField<Drawable> driverImage = new ObservableField<>();
    public final ObservableField<String> driverRating = new ObservableField<>();
    public final ObservableField<String> driverNickname = new ObservableField<>();
    public final ObservableField<Drawable> carImage = new ObservableField<>();
    public final ObservableField<String> carCategory = new ObservableField<>();
    public final ObservableField<String> carCategorySeats = new ObservableField<>();
    public final ObservableField<String> carCategorySurgeFactor = new ObservableField<>();
    public final ObservableField<Drawable> selectedPaymentImage = new ObservableField<>();
    public final ObservableField<String> selectedPaymentName = new ObservableField<>();
    public final ObservableField<Integer> priorityIconVisibility = new ObservableField<>(View.INVISIBLE);
    public final ObservableBoolean requesting = new ObservableBoolean();
    public final Boolean isEspresso = DeviceInfoUtil.isEspresso();

    private DirectConnectViewModel model;
    private SingleSubject<Boolean> rideStarted = SingleSubject.create();

    public ConnectDriverViewModel() {
    }

    public void setModel(DirectConnectViewModel model) {
        this.model = model;
        showDriver();
        showSelectedCarType();
        showSelectedPayment();
        listenToRideStatus();
    }

    public void selectCarType() {
        model.navigateTo(DirectConnectViewModel.Step.CATEGORY_PICKER);
    }

    public void selectPaymentType() {
        model.navigateTo(DirectConnectViewModel.Step.PAYMENT_PICKER);
    }

    public void cancel() {
        requesting.set(false);
        if (App.getPrefs().hasRideId()) {
            RideStatusService.stop();
            App.getDataManager().cancelRide(App.getPrefs().getRideId(), AvatarType.RIDER.name())
                    .subscribeOn(RxSchedulers.network())
                    .observeOn(RxSchedulers.main())
                    .retryWhen(new RetryWhenNoNetwork(1000))
                    .subscribe(isSuccess -> {
                        App.getPrefs().updateRideInfo(0L, RIDER_CANCELLED.name());
                        App.getStateManager().post(RideStatusEvent.riderCancelled());
                    }, throwable -> {
                        RideStatusService.startIfNeeded();
                        Timber.e(throwable, "Error when cancelling ride");
                    });
        }
    }

    Observable<Intent> getRequestIntent() {
        Observable<GeoPosition> pickup;
        if (App.getPrefs().getPickupGeoPosition() != null) {
            pickup = Observable.just(App.getPrefs().getPickupGeoPosition());
        } else {
            pickup = App.getLocationManager().getLastLocation(true, false)
                    .flatMap(location -> LocationHelper.loadBestAddress(location.getCoordinates()));
        }
        return pickup.map(address -> {
                    Intent intent = new Intent(App.getInstance(), RideStatusService.class);
                    intent.putExtra(Constants.START_ADDRESS, address);
                    if (App.getPrefs().getDestinationGeoPosition() != null) {
                        intent.putExtra(Constants.DESTINATION_ADDRESS, App.getPrefs().getDestinationGeoPosition());
                    }
                    intent.putExtra(Constants.SELECTED_CAR_CATEGORY, model.getCarType()
                            .map(RequestedCarType::getCarCategory)
                            .orElse(Constants.REGULAR_CAR_TYPE));
                    intent.putExtra(Constants.DIRECT_CONNECT_ID, model.getDirectConnectId());
                    return intent;
                })
                .doOnSubscribe(() -> requesting.set(true))
                .doOnError(throwable -> requesting.set(false));
    }

    Observable<Boolean> getRideStarted() {
        return rideStarted.asObservable().onBackpressureLatest();
    }

    private void listenToRideStatus() {
        untilDestroy(App.getStateManager()
                .getRideStatus()
                .onErrorReturn(throwable -> new RideStatusEvent(RideStatus.UNKNOWN, "", null))
                .subscribe(this::onRideStatusEvent));
    }

    private void onRideStatusEvent(RideStatusEvent event) {
        switch (event.getData()) {
            case REQUESTED:
                requesting.set(true);
                break;
            case NO_AVAILABLE_DRIVER:
            case ADMIN_CANCELLED:
            case DRIVER_CANCELLED:
            case RIDER_CANCELLED:
                requesting.set(false);
                rideStarted.onNext(false);
                break;
            case DRIVER_ASSIGNED:
            case DRIVER_REACHED:
            case ACTIVE:
                rideStarted.onNext(true);
                break;
            case RIDE_REQUEST_ERROR:
            case COMPLETED:
            case FINISHED:
            case UNKNOWN:
                requesting.set(false);
                rideStarted.onNext(false);
                break;
        }
    }

    private void showDriver() {
        final DirectConnectDriver driver = model.getDriver();
        String url = driver.getPhotoUrl();
        untilDestroy(RxImageLoader.execute(new RxImageLoader.Request(url)
                .target(driverImage)
                .progress(R.drawable.rotating_circle)
                .error(R.drawable.ic_user_icon)
                .circular(true)));
        driverRating.set(String.valueOf(driver.getRating()));
        driverNickname.set(driver.getFirstName() + " " + driver.getLastName());
        showSelectedPayment();
    }

    private void showSelectedCarType() {
        model.getCarType().ifPresentOrElse(carType -> {
            final float surgeFactor = model.getSurgeFactor(carType);
            String url = carType.getPlainIconUrl();
            untilDestroy(RxImageLoader.execute(new RxImageLoader.Request(url)
                    .target(carImage)
                    .progress(R.drawable.rotating_circle)
                    .error(R.drawable.icn_generic_car)));
            carCategory.set(carType.getTitle());
            carCategorySeats.set(carType.getMaxPersons() + " seats");
            carCategorySurgeFactor.set(UIUtils.formatSurgeFactor(surgeFactor));
            if (surgeFactor > 1) {
                priorityIconVisibility.set(View.VISIBLE);
            } else {
                priorityIconVisibility.set(View.INVISIBLE);
            }
        }, () -> {
            carImage.set(null);
            carCategory.set("");
            carCategorySeats.set("");
            carCategorySurgeFactor.set("");
            priorityIconVisibility.set(View.INVISIBLE);
        });
    }

    private void showSelectedPayment() {
        App.getDataManager().getLocalSelectedPayment()
                .ifPresentOrElse(payment -> {
                    selectedPaymentImage.set(ContextCompat.getDrawable(App.i(), PaymentType.parse(payment.getCardBrand()).getIconResId()));
                    selectedPaymentName.set(App.i().getString(R.string.payment_card_template, payment.getCardNumber()));
                }, () -> {
                    selectedPaymentImage.set(ContextCompat.getDrawable(App.i(), PaymentType.UNKNOWN.getIconResId()));
                    selectedPaymentName.set(App.i().getString(R.string.payment_select_method));
                });
    }
}
