package com.rideaustin.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.rideaustin.App;
import com.rideaustin.api.config.ConfigAppInfoResponse;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.api.model.events.PendingEvent;
import com.rideaustin.api.model.events.PendingEvents;
import com.rideaustin.models.RideRating;
import com.rideaustin.ui.model.NavigationAppPreference;
import com.rideaustin.utils.SerializationHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import java8.util.Optional;

/**
 * @author shumelchyk
 */
public class PrefManager extends BasePrefManager {

    private static final String RIDE_STATUS = "ride_status";
    @Deprecated
    private static final String RIDE_RATING = "ride_rating";
    private static final String RIDE_ID = "ride_id";
    private static final String RIDE_TO_RATE_ID = "ride_rate_id";
    private static final String RIDE_REQUEST_TYPE = "ride_request_type";
    private static final String RIDE_REQUEST_TYPE_WOMAN_ONLY = "ride_request_type_woman_only";
    private static final String RIDE_REQUEST_TYPE_DC = "ride_request_type_dc";
    private static final String DRIVER_NAVIGATION_ACTIVITY = "driver_navigator_activity_v2";
    private static final String PICKUP_ADDRESS_STRING = "pickup_address";
    private static final String DESTINATION_ADDRESS_STRING = "destination_address";

    private static final String BOUNDS_PREFERENCES = "bounds_preferences";
    private static final String BOUNDS = "bounds";
    private static final String UPDATE_AVAILABLE_SHOWN_DATE = "update_available_shown_date";

    private static final String SAVED_PENDING_EVENTS = "typed_pending_events";

    private static final String UNRATED_RIDES = "unrated_rides";
    private static final String PENDING_RIDE_RATINGS = "pending_ride_ratings";

    private List<Long> unratedRides;
    private List<RideRating> pendingRideRatings;

    public PrefManager() {

    }

    private SharedPreferences getBoundsPreferences() {
        return App.getInstance().getSharedPreferences(BOUNDS_PREFERENCES, Context.MODE_PRIVATE);
    }

    public void setRideStatus(String rideStatus) {
        putUserString(RIDE_STATUS, rideStatus);
    }

    public void setRideId(Long rideId) {
        putUserLong(RIDE_ID, rideId);
    }

    public Long getRideId() {
        return getUserLong(RIDE_ID, 0);
    }

    public boolean hasRideId() {
        return getRideId() != 0L;
    }

    public void clearRideId() {
        clearUserValue(RIDE_ID);
    }

    public void setPickupAddressString(String s) {
        putString(PICKUP_ADDRESS_STRING, s);
    }

    public String getPickupAddressString() {
        return getString(PICKUP_ADDRESS_STRING, "");
    }

    public void setDestinationAddressString(String s) {
        putString(DESTINATION_ADDRESS_STRING, s);
    }

    public String getDestinationAddressString() {
        return getString(DESTINATION_ADDRESS_STRING, "");
    }

    public void updateRideInfo(Long rideId, String status) {
        setRideId(rideId);
        setRideStatus(status);
    }

    @Deprecated
    public void saveRideToRate(Long id) {
        putUserLong(RIDE_TO_RATE_ID, id);
    }

    @Deprecated
    public Optional<Long> getRideToRate() {
        long rideId = getUserLong(RIDE_TO_RATE_ID, 0);
        if (rideId != 0) {
            return Optional.of(rideId);
        } else {
            return Optional.empty();
        }
    }

    @Deprecated
    public void removeRideToRate() {
        clearUserValue(RIDE_TO_RATE_ID);
    }

    public String getRideStatus() {
        return getUserString(RIDE_STATUS, "");
    }

    public String getBounds() {
        return getBoundsPreferences().getString(BOUNDS, "");
    }

    public void setBounds(String bounds) {
        getBoundsPreferences().edit().putString(BOUNDS, bounds).apply();
    }

    public void saveRate(float rate) {
        putUserFloat(RIDE_RATING, rate);
    }

    public float getRate() {
        return getUserFloat(RIDE_RATING, -1);
    }

    @Deprecated
    public void clearRate() {
        clearUserValue(RIDE_RATING);
    }

    public void setCurrentRideRequestType(Driver driver, Set<String> type) {
        getPermanentPreferences().edit().putStringSet(RIDE_REQUEST_TYPE + driver.getEmail(), type).apply();
    }

    /**
     * Use {@link RideRequestManager#getSelectedCategories()}
     */
    public Set<String> getCurrentRideRequestType(Driver driver) {
        return getPermanentPreferences().getStringSet(RIDE_REQUEST_TYPE + driver.getEmail(), null);
    }

    public void setWomanOnlyModeEnabled(Driver driver, boolean isWomanMode) {
       putBoolean(RIDE_REQUEST_TYPE_WOMAN_ONLY + driver.getEmail(), isWomanMode);
    }

    public boolean isWomanOnlyModeEnabled(Driver driver) {
        return getBoolean(RIDE_REQUEST_TYPE_WOMAN_ONLY + driver.getEmail(), false);
    }

    public void setDirectConnectModeEnabled(Driver driver, boolean isDirectConnect) {
        putBoolean(RIDE_REQUEST_TYPE_DC + driver.getEmail(), isDirectConnect);
    }

    public boolean isDirectConnectModeEnabled(Driver driver) {
        return getBoolean(RIDE_REQUEST_TYPE_DC + driver.getEmail(), false);
    }

    public void setDriverNavigationActivity(Driver driver, NavigationAppPreference prefernce) {
        getPermanentPreferences().edit().putString(DRIVER_NAVIGATION_ACTIVITY + driver.getEmail(), SerializationHelper.serialize(prefernce)).apply();
    }

    @Nullable
    public NavigationAppPreference getDriverNavigationActivity(Driver driver) {
        if (driver == null) {
            return null;
        } else {
            String navigationActivity = getPermanentPreferences().getString(DRIVER_NAVIGATION_ACTIVITY + driver.getEmail(), "");
            return TextUtils.isEmpty(navigationActivity) ? null : SerializationHelper.deSerialize(navigationActivity, NavigationAppPreference.class);
        }
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

    public PendingEvents loadPendingEvents() {
        final String serialized = getString(SAVED_PENDING_EVENTS, "");
        PendingEvents pendingEvents = null;
        if (!TextUtils.isEmpty(serialized)) {
            pendingEvents = SerializationHelper.deSerialize(serialized, PendingEvents.class);
        }
        if (pendingEvents == null) {
            pendingEvents = new PendingEvents();
        }
        return pendingEvents;
    }

    public void savePendingEvents(final PendingEvents pendingEvents) {
        putString(SAVED_PENDING_EVENTS, SerializationHelper.serialize(pendingEvents));
    }

    public void setConfig(@Nullable ConfigAppInfoResponse config) {
        // do nothing, driver fast-run not supported yet
    }

    public synchronized void addUnratedRide(long rideId) {
        if (unratedRides == null) {
            readUnratedRides();
        }
        addToList(UNRATED_RIDES, getUserSpecificPreferences(), unratedRides, rideId);
    }

    public synchronized void removeUnratedRide(long rideId) {
        if (unratedRides == null) {
            readUnratedRides();
        }
        removeFromList(UNRATED_RIDES, getUserSpecificPreferences(), unratedRides, rideId);
    }

    public synchronized List<Long> getUnratedRides() {
        if (unratedRides == null) {
            readUnratedRides();
        }
        return new ArrayList<>(unratedRides);
    }

    private void readUnratedRides() {
        Type type = new TypeToken<ArrayList<Long>>() {}.getType();
        unratedRides = getList(UNRATED_RIDES, getUserSpecificPreferences(), type);
        // For backward-compatibility
        long oldRideId = getUserLong(RIDE_TO_RATE_ID, -1);
        if (oldRideId != -1) {
            unratedRides.add(-1L);
            clearUserValue(RIDE_TO_RATE_ID);
        }
    }

    public synchronized void addPendingRideRating(RideRating rating) {
        if (pendingRideRatings == null) {
            readPendingRideRatings();
        }
        addToList(PENDING_RIDE_RATINGS, getUserSpecificPreferences(), pendingRideRatings, rating);
    }

    public synchronized void removePendingRideRating(RideRating rating) {
        if (pendingRideRatings == null) {
            readPendingRideRatings();
        }
        removeFromList(PENDING_RIDE_RATINGS, getUserSpecificPreferences(), pendingRideRatings, rating);
    }

    public synchronized List<RideRating> getPendingRideRatings() {
        if (pendingRideRatings == null) {
            readPendingRideRatings();
        }
        return new ArrayList<>(pendingRideRatings);
    }

    private void readPendingRideRatings() {
        Type type = new TypeToken<ArrayList<RideRating>>() {}.getType();
        pendingRideRatings = getList(PENDING_RIDE_RATINGS, getUserSpecificPreferences(), type);

    }
}
