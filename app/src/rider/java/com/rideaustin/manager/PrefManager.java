package com.rideaustin.manager;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.ConfigAppInfoResponse;
import com.rideaustin.api.model.Payment;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.Rider;
import com.rideaustin.api.model.UpgradeRequestStatus;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.entities.GeoPosition;
import com.rideaustin.ui.drawer.favorite.FavoritesViewModel;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.SerializationHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import java8.util.Optional;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

import static com.rideaustin.ui.drawer.favorite.FavoritesViewModel.TYPE_HOME;
import static com.rideaustin.ui.drawer.favorite.FavoritesViewModel.TYPE_WORK;

/**
 * Created by supreethks on 23/10/16.
 */

public class PrefManager extends BasePrefManager {

    private static final String RIDE_STATUS = "ride_status";
    private static final String RIDE_ID = "ride_id";
    private static final String RIDE_TO_RATE_ID = "ride_rate_id";

    private static final String PICKUP_ADDRESS_STRING = "pickup_address";
    private static final String DESTINATION_ADDRESS_STRING = "destination_address";

    private static final String FAVORITE_PLACE_ = "FAVORITE_PLACE_";

    private static final String PICKUP_ADDRESS = "PICKUP_ADDRESS";
    private static final String DESTINATION_ADDRESS = "DESTINATION_ADDRESS";

    private static final String UPDATE_AVAILABLE_SHOWN_DATE = "update_available_shown_date";
    private static final String ROUNDUP_ENABLED = "roundup_enabled";
    private static final String ROUNDUP_POPUP_SHOW_COUNT = "roundup_popup_show_count";
    private static final String RIDE_REQUEST_SHOWN = "ride_request_shown";
    private static final String REQUESTED_CAR_TYPE = "requested_car_type";
    private static final String SLIDER_REQUESTED_CAR_TYPE = "slider_requested_car_type";
    private static final String RIDE_UPGRADE_STATUS = "ride_upgrade_status";

    private static Optional<GeoPosition> home = Optional.empty();
    private static Optional<GeoPosition> work = Optional.empty();
    private static boolean homeSet = false;
    private static boolean workSet = false;
    private static PublishSubject<String> favoritePlaceChanged = PublishSubject.create();

    private static final String KEY_RIDE = "key_ride";
    private static final String KEY_RIDER = "key_rider";
    private static final String KEY_PAYMENTS = "key_payments";
    private static final String KEY_PRIMARY_PAYMENTS = "key_primary_payments";
    private static final String KEY_CONFIG = "key_config";
    private static final String FEMALE_ONLY_ENABLED = "female_only_enabled";
    private static final String FINGERPRINTED_ONLY_ENABLED = "fingerprinted_only_enabled";

    @Override
    public void clearPrefs() {
        super.clearPrefs();
        home = Optional.empty();
        work = Optional.empty();
        homeSet = false;
        workSet = false;
    }

    public void setRideStatus(String rideStatus) {
        putUserString(RIDE_STATUS, rideStatus);
        if (TextUtils.isEmpty(rideStatus)) {
            setDestinationAddressString("");
            setPickupAddressString("");
        }
    }

    public String getRideStatus() {
        return getUserString(RIDE_STATUS, "");
    }

    public void setRideId(Long rideId) {
        putUserLong(RIDE_ID, rideId);
    }

    public Long getRideId() {
        return getUserLong(RIDE_ID, 0L);
    }

    public boolean hasRideId() {
        return getRideId() != 0L;
    }

    public void setPickupAddressString(String s) {
        putString(PICKUP_ADDRESS_STRING, s);
    }

    public void setDestinationAddressString(String s) {
        putString(DESTINATION_ADDRESS_STRING, s);
    }

    public void setPickupGeoPosition(GeoPosition pickupAddress) {
        putString(PICKUP_ADDRESS, pickupAddress != null ? SerializationHelper.serialize(pickupAddress) : "");
    }

    public void setDestinationGeoPosition(GeoPosition destinationAddress) {
        putString(DESTINATION_ADDRESS, destinationAddress != null ? SerializationHelper.serialize(destinationAddress) : "");
    }

    @Nullable
    public GeoPosition getDestinationGeoPosition() {
        String addressString = getString(DESTINATION_ADDRESS, "");
        if (!TextUtils.isEmpty(addressString)) {
            return SerializationHelper.deSerialize(addressString, GeoPosition.class);
        }
        return null;
    }

    @Nullable
    public GeoPosition getPickupGeoPosition() {
        String addressString = getString(PICKUP_ADDRESS, "");
        if (!TextUtils.isEmpty(addressString)) {
            return SerializationHelper.deSerialize(addressString, GeoPosition.class);
        }
        return null;
    }

    public void updateRideInfo(Long rideId, String status) {
        setRideId(rideId);
        setRideStatus(status);
    }

    public void saveRideToRate(Long id) {
        putUserLong(RIDE_TO_RATE_ID, id);
    }

    public long getRideToRate() {
        return getUserLong(RIDE_TO_RATE_ID, 0);
    }

    public void removeRideToRate() {
        clearUserValue(RIDE_TO_RATE_ID);
    }

    public Calendar getAppUpdateShownDate() {
        long aLong = getLong(UPDATE_AVAILABLE_SHOWN_DATE, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(aLong);
        return calendar;
    }

    public void setAppUpdateShownDate(Calendar calendar) {
        putLong(UPDATE_AVAILABLE_SHOWN_DATE, calendar.getTimeInMillis());
    }

    public int getRoundUpPopupShowCount() {
        return getUserInt(ROUNDUP_POPUP_SHOW_COUNT, 0);
    }

    public void increaseRoundUpPopupShowCount() {
        putUserInt(ROUNDUP_POPUP_SHOW_COUNT, getRoundUpPopupShowCount() + 1);
    }

    public boolean roundUpPopupReachedShowLimit() {
        return getRoundUpPopupShowCount() >= Constants.ROUNDUP_SHOW_POPUP_LIMIT;
    }

    public boolean isRoundUpEnabled() {
        return getUserBoolean(ROUNDUP_ENABLED, false);
    }

    public void setRoundUpEnabled(boolean roundupEnabled) {
        putUserBoolean(ROUNDUP_ENABLED, roundupEnabled);
    }

    public boolean isRideRequestShown() {
        return getBoolean(RIDE_REQUEST_SHOWN, false);
    }

    public void setRideRequestShown(boolean rideRequestShown) {
        putBoolean(RIDE_REQUEST_SHOWN, rideRequestShown);
    }

    public void setRequestedCarType(@NonNull RequestedCarType carType) {
        putString(REQUESTED_CAR_TYPE, SerializationHelper.serialize(carType));
    }

    public void setSliderRequestedCarType(@NonNull RequestedCarType carType) {
        putString(SLIDER_REQUESTED_CAR_TYPE, SerializationHelper.serialize(carType));
    }

    public Optional<RequestedCarType> getSliderRequestedCarType() {
        String carTypeStr = getString(SLIDER_REQUESTED_CAR_TYPE, null);
        if (!TextUtils.isEmpty(carTypeStr)) {
            return Optional.of(SerializationHelper.deSerialize(carTypeStr, RequestedCarType.class));
        }
        return Optional.empty();
    }

    @Nullable
    public RequestedCarType getRequestedCarType() {
        String carTypeStr = getString(REQUESTED_CAR_TYPE, null);
        if (!TextUtils.isEmpty(carTypeStr)) {
            return SerializationHelper.deSerialize(carTypeStr, RequestedCarType.class);
        }
        return null;
    }

    public void saveRideUpgradeStatus(UpgradeRequestStatus status) {
        putUserString(RIDE_UPGRADE_STATUS, status.name());
    }

    public UpgradeRequestStatus getRideUpgradeStatus() {
        String status = getUserString(RIDE_UPGRADE_STATUS, null);
        if (status == null) {
            return UpgradeRequestStatus.NONE;
        } else {
            return UpgradeRequestStatus.valueOf(status);
        }
    }

    public Optional<GeoPosition> getFavoritePlace(@FavoritesViewModel.FavoriteType String favoriteType) {
        String geoPositionStr = getUserString(FAVORITE_PLACE_ + favoriteType, null);
        if (!TextUtils.isEmpty(geoPositionStr)) {
            GeoPosition geoPosition = SerializationHelper.deSerialize(geoPositionStr, GeoPosition.class);
            if (geoPosition != null) {
                geoPosition.setCustomName(getFavoritePlaceCustomName(favoriteType));
                return Optional.of(geoPosition);
            }
        }
        return Optional.empty();
    }

    public void setFavoritePlace(@FavoritesViewModel.FavoriteType String favoriteType, GeoPosition selectedAddress) {
        selectedAddress.setCustomName(getFavoritePlaceCustomName(favoriteType));
        switch (favoriteType) {
            case TYPE_HOME:
                home = Optional.ofNullable(selectedAddress);
                App.getDataManager().resetCustomLocations();
                break;
            case TYPE_WORK:
                work = Optional.ofNullable(selectedAddress);
                App.getDataManager().resetCustomLocations();
                break;
        }
        favoritePlaceChanged.onNext(favoriteType);
        putUserString(FAVORITE_PLACE_ + favoriteType, SerializationHelper.serialize(selectedAddress));
    }

    public Optional<GeoPosition> getHome() {
        if (!homeSet) {
            home = getFavoritePlace(TYPE_HOME);
            homeSet = true;
        }
        return home;
    }

    public Optional<GeoPosition> getWork() {
        if (!workSet) {
            work = getFavoritePlace(TYPE_WORK);
            workSet = true;
        }
        return work;
    }

    public Observable<String> getFavoritePlaceChanged() {
        return favoritePlaceChanged.asObservable().onBackpressureBuffer();
    }

    private String getFavoritePlaceCustomName(@FavoritesViewModel.FavoriteType String favoriteType) {
        return App.getInstance().getString(favoriteType.equals(TYPE_WORK) ? R.string.work : R.string.home);
    }

    public void setConfig(@Nullable ConfigAppInfoResponse config) {
        serialize(KEY_CONFIG, config, true);
    }

    @Nullable
    public ConfigAppInfoResponse getConfig() {
        return deserialize(KEY_CONFIG, ConfigAppInfoResponse.class, true);
    }

    public void setRide(@Nullable Ride ride) {
        serialize(KEY_RIDE, ride, false);
    }

    @Nullable
    public Ride getRide() {
        return deserialize(KEY_RIDE, Ride.class, false);
    }

    public void setRider(@Nullable Rider rider) {
        // save rider in global properties
        // this will help to start application faster
        serialize(KEY_RIDER, rider, true);
    }

    @Nullable
    public Rider getRider() {
        return deserialize(KEY_RIDER, Rider.class, true);
    }

    public void setPayments(@Nullable List<Payment> payments) {
        serialize(KEY_PAYMENTS, payments, false);
    }

    @Nullable
    public List<Payment> getPayments() {
        Type type = new TypeToken<ArrayList<Payment>>() {
        }.getType();
        return deserialize(KEY_PAYMENTS, type, false);
    }


    private void serialize(@NonNull String key, @Nullable Object data, boolean globalPrefs) {
        SharedPreferences prefs = globalPrefs ? getPreferences() : getUserSpecificPreferences();
        if (data == null) {
            prefs.edit().remove(key).apply();
        } else {
            prefs.edit().putString(key, SerializationHelper.serialize(data)).apply();
        }
    }

    @Nullable
    public <T> T deserialize(@NonNull String key, Class<T> dataClass, boolean globalPrefs) {
        SharedPreferences prefs = globalPrefs ? getPreferences() : getUserSpecificPreferences();
        if (prefs.contains(key)) {
            String str = prefs.getString(key, "");
            if (!TextUtils.isEmpty(str)) {
                return SerializationHelper.deSerialize(str, dataClass);
            }
        }
        return null;
    }

    @Nullable
    public <T> T deserialize(@NonNull String key, Type dataType, boolean globalPrefs) {
        SharedPreferences prefs = globalPrefs ? getPreferences() : getUserSpecificPreferences();
        if (prefs.contains(key)) {
            String str = prefs.getString(key, "");
            if (!TextUtils.isEmpty(str)) {
                return SerializationHelper.deSerialize(str, dataType);
            }
        }
        return null;
    }

    public void savePaymentAsLocalPrimary(long id) {
        putUserLong(KEY_PRIMARY_PAYMENTS, id);
    }

    public void removePrimaryLocalPayment() {
        clearUserValue(KEY_PRIMARY_PAYMENTS);
    }

    public Optional<Long> getLocalPrimaryPayment() {
        long id = getUserLong(KEY_PRIMARY_PAYMENTS, -1);
        if (id > 0) {
            return Optional.of(id);
        } else {
            return Optional.empty();
        }
    }

    public void setFingerprintedOnlyEnabled(boolean fingerprintedOnlyEnabled) {
        putUserBoolean(FINGERPRINTED_ONLY_ENABLED, fingerprintedOnlyEnabled);
    }

    public boolean isFingerprintedOnlyEnabled() {
        return getUserBoolean(FINGERPRINTED_ONLY_ENABLED, false);
    }

    public void setFemaleOnlyEnabled(boolean femaleOnlyEnabled) {
        putUserBoolean(FEMALE_ONLY_ENABLED, femaleOnlyEnabled);
    }

    public boolean isFemaleOnlyEnabled() {
        return getUserBoolean(FEMALE_ONLY_ENABLED, false);
    }

    @VisibleForTesting
    public void saveRideToRate(long id, long userId) {
        getUserSpecificPreferences(userId).edit().putLong(RIDE_TO_RATE_ID, id).apply();
    }

}
