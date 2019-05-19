package com.rideaustin.ui.drawer.favorite;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.entities.GeoPosition;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.BaseViewModel;
import com.rideaustin.utils.LocationHintHelper;
import com.rideaustin.utils.location.DistanceUtil;
import com.rideaustin.utils.location.LocationHelper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.atomic.AtomicBoolean;

import java8.util.Optional;
import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;


/**
 * Created by crossover on 04/07/2017.
 */

public class FavoritesViewModel extends BaseViewModel<FavoritesView> {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({TYPE_HOME, TYPE_WORK, TYPE_PICKUP, TYPE_DESTINATION})
    public @interface FavoriteType {
    }

    public static final String TYPE_HOME = "TYPE_HOME";
    public static final String TYPE_WORK = "TYPE_WORK";
    public static final String TYPE_PICKUP = "TYPE_PICKUP";
    public static final String TYPE_DESTINATION = "TYPE_DESTINATION";

    @FavoriteType
    private final String favoriteType;
    private Subscription addressLoadSubscription = Subscriptions.empty();
    private BehaviorSubject<LatLng> selectedLocation = BehaviorSubject.create();
    private GeoPosition selectedAddress;
    private AtomicBoolean cameraMoving = new AtomicBoolean(false);
    private AtomicBoolean addressPicked = new AtomicBoolean(false);

    public final ObservableField<String> addressLine = new ObservableField<>("");
    public final ObservableBoolean saveEnabled = new ObservableBoolean(false);

    public FavoritesViewModel(@NonNull FavoritesView view, String type, @Nullable GeoPosition geoPosition) {
        super(view);
        this.favoriteType = type;
        Optional.ofNullable(geoPosition)
                .or(() -> App.getPrefs().getFavoritePlace(favoriteType))
                .ifPresent(p -> {
                    addressPicked.set(true);
                    selectedAddress = p;
                    addressLine.set(p.getAddressLine());
                    selectedLocation.onNext(p.getLatLng());
                    saveEnabled.set(true);
                });
    }

    @StringRes
    public int getTitleRes() {
        switch (favoriteType) {
            case TYPE_HOME:
                return R.string.home;
            case TYPE_WORK:
                return R.string.work;
            case TYPE_PICKUP:
                return R.string.address_choose_pickup;
            case TYPE_DESTINATION:
                return R.string.address_choose_destination;
            default:
                throw new IllegalArgumentException("Unsupported type: " + favoriteType);
        }
    }

    @DrawableRes
    public int getLeftIconRes() {
        switch (favoriteType) {
            case TYPE_HOME:
                return R.drawable.ic_home_black;
            case TYPE_WORK:
                return R.drawable.ic_work_black;
            case TYPE_PICKUP:
                return R.drawable.ic_green;
            case TYPE_DESTINATION:
                return R.drawable.ic_red;
            default:
                throw new IllegalArgumentException("Unsupported type: " + favoriteType);
        }
    }

    @DrawableRes
    public int getMarkerRes() {
        switch (favoriteType) {
            case TYPE_HOME:
            case TYPE_WORK:
            case TYPE_PICKUP:
                return R.drawable.icn_pickup_pin;
            case TYPE_DESTINATION:
                return R.drawable.icn_destination_pin;
            default:
                throw new IllegalArgumentException("Unsupported type: " + favoriteType);
        }
    }

    @Nullable
    public LocationHintHelper.AreaType getAreaType() {
        switch (favoriteType) {
            case TYPE_PICKUP:
                return LocationHintHelper.AreaType.PICKUP;
            case TYPE_DESTINATION:
                return LocationHintHelper.AreaType.DESTINATION;
            default:
                return null;
        }
    }

    void onCameraChange(LatLng location) {
        if (!addressPicked.getAndSet(false)) {
            LatLng selected = selectedLocation.getValue();
            if (selected != null && location != null && DistanceUtil.distance(selected, location) < 1) {
                return;
            }
            selectedAddress = null;
            saveEnabled.set(false);

            if (location != null) {
                Timber.d("::onCameraChange:: Position: (%f,%f)", location.latitude, location.longitude);
                loadAddressForLocation(location);
            }
        }
    }

    @Nullable
    GeoPosition getSelectedAddress() {
        return selectedAddress;
    }

    Observable<LatLng> observeSelectedLocation() {
        return selectedLocation.asObservable().serialize().onBackpressureLatest();
    }

    private void loadAddressForLocation(LatLng latLng) {
        selectedLocation.onNext(latLng);
        addressLoadSubscription.unsubscribe();
        addressLoadSubscription = LocationHelper.loadBestAddress(latLng)
                .subscribeOn(RxSchedulers.computation())
                .observeOn(RxSchedulers.main())
                .subscribe(this::setAddressLine, throwable -> {
                    Timber.e(throwable);
                    setAddressLine(null);
                });
    }

    void setCameraMoving(boolean moving) {
        if (cameraMoving.compareAndSet(!moving, moving)) {
            if (moving) {
                addressLoadSubscription.unsubscribe();
                selectedAddress = null;
                saveEnabled.set(false);
            }
        }
    }

    void onSave(FavoritesMapFragment.FavoritesMapListener listener) {
        listener.onFavoritePlaceSelected(favoriteType, selectedAddress);
    }

    void addressItemSelected(GoogleApiClient apiClient, BaseActivityCallback callback, AutocompletePrediction prediction) {
        addressLoadSubscription.unsubscribe();
        addressLoadSubscription = LocationHelper.getPlaceById(apiClient, prediction.getPlaceId())
                .subscribeOn(RxSchedulers.computation())
                .flatMap(place -> LocationHelper.getZipCodeOrFail(place.getLatLng())
                        .map(zipCode -> {
                            zipCode.ifPresent(place::setZipCode);
                            return place;
                        }))
                .onErrorResumeNext(Observable.just(null))
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<GeoPosition>(callback) {
                    @Override
                    public void onNext(@Nullable GeoPosition address) {
                        addressPicked.set(true);
                        if (address != null) {
                            performOnView(favoritesView -> favoritesView.moveCamera(address.getLatLng()));
                            CharSequence primaryText = prediction.getPrimaryText(null);
                            if (!TextUtils.isEmpty(primaryText)) {
                                address.setAddressLine(String.valueOf(primaryText));
                            }
                        }
                        setAddressLine(address);
                    }
                });
    }

    private void setAddressLine(@Nullable GeoPosition address) {
        performOnView(favoritesView -> favoritesView.setEditableInput(false));
        selectedAddress = address;
        if (selectedAddress == null) {
            addressLine.set(App.getInstance().getString(R.string.invalid_address));
        } else {
            addressLine.set(selectedAddress.getAddressLine());
        }
        saveEnabled.set(selectedAddress != null);
    }
}
