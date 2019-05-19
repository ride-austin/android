package com.rideaustin.engine;


import android.support.annotation.NonNull;

import com.rideaustin.App;
import com.rideaustin.api.DataManager;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.config.LocationUpdateIntervals;
import com.rideaustin.api.model.Coordinates;
import com.rideaustin.api.model.events.PendingEventType;
import com.rideaustin.base.retrofit.RetrofitException;
import com.rideaustin.manager.ConfigurationManager;
import com.rideaustin.manager.location.RALocation;
import com.rideaustin.manager.location.RALocationManager;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.MathUtils;
import com.rideaustin.utils.ObjectUtils;
import com.rideaustin.utils.RetryWithDelay;
import com.rideaustin.utils.TimeUtils;
import com.rideaustin.utils.location.DistanceUtil;

import java.util.concurrent.TimeUnit;

import java8.util.Optional;
import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static com.rideaustin.utils.Constants.BACKGROUND_ERROR_RETRY_DELAY_S;
import static com.rideaustin.utils.Constants.INACTIVE_DRIVER;

/**
 * Created by rost on 8/17/16.
 */
public class DriverLocationManager {

    // correction for timer deviation
    private static final long TIMER_DEVIATION_MS = 50L;
    private static final long DEFAULT_WHEN_ON_TRIP = 2;
    private static final long DEFAULT_WHEN_ONLINE_AND_NOT_MOVING = 10;
    private static final long DEFAULT_WHEN_ONLINE_AND_MOVING = 7;

    private DataManager dataManager;
    private final StateManager stateManager;
    private final RALocationManager locationManager;
    private final ConfigurationManager configurationManager;

    private volatile RALocation lastSentLocation;
    private volatile RALocation lastTakenLocation;
    private long lastSentTimestamp = 0L;

    private long interval;
    private MovingFilter movingFilter;
    private LocationUpdateIntervals intervals;
    private DriverTrackingType trackingType = DriverTrackingType.NONE;

    private PublishSubject<Throwable> errorSubject = PublishSubject.create();
    private Subscription updateSubscription = Subscriptions.unsubscribed();
    private Subscription locationSubscription = Subscriptions.empty();

    public DriverLocationManager(DataManager dataManager,
                                 StateManager stateManager,
                                 RALocationManager locationManager,
                                 ConfigurationManager configurationManager) {
        this.dataManager = dataManager;
        this.stateManager = stateManager;
        this.locationManager = locationManager;
        this.configurationManager = configurationManager;
        configurationManager.getConfigurationUpdates()
                .map(GlobalConfig::getLocationUpdateIntervals)
                .subscribe(this::doOnIntervals, Timber::e);
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    void startTracking(DriverTrackingType type) {
        if (trackingType != type) {
            trackingType = type;
            update();
        }
    }

    void stopTracking() {
        trackingType = DriverTrackingType.NONE;
        updateSubscription.unsubscribe();
        movingFilter = null;
        lastSentTimestamp = 0L;
        interval = 0L;
    }

    Observable<Throwable> getErrors() {
        return errorSubject
                .serialize()
                .onBackpressureLatest()
                .asObservable();
    }

    private void doOnIntervals(LocationUpdateIntervals intervals) {
        if (!ObjectUtils.equals(this.intervals, intervals)) {
            this.intervals = intervals;
            update();
        }
    }

    private void update() {
        if (trackingType == DriverTrackingType.NONE) {
            if (!updateSubscription.isUnsubscribed()) {
                stopTracking();
            }
            return;
        }

        Optional<LocationUpdateIntervals> optional = Optional.ofNullable(intervals);
        // interval to send location updates when driver is moving
        interval = trackingType == DriverTrackingType.ON_TRIP
                ? optional.map(LocationUpdateIntervals::getWhenOnTrip).orElse(DEFAULT_WHEN_ON_TRIP)
                : optional.map(LocationUpdateIntervals::getWhenOnlineAndMoving).orElse(DEFAULT_WHEN_ONLINE_AND_MOVING);

        // max interval (RA-9441: don't send location updates on trip when not moving)
        long maxInterval = trackingType == DriverTrackingType.ON_TRIP
                ? Long.MAX_VALUE
                : optional.map(LocationUpdateIntervals::getWhenOnlineAndNotMoving).orElse(DEFAULT_WHEN_ONLINE_AND_NOT_MOVING);

        interval = Math.min(interval, maxInterval);

        // max possible interval for updates
        long updateInterval = trackingType == DriverTrackingType.ON_TRIP
                ? optional.map(LocationUpdateIntervals::getWhenOnTrip).orElse(DEFAULT_WHEN_ON_TRIP)
                : MathUtils.gcd(maxInterval, interval);


        lastSentTimestamp = TimeUtils.currentTimeMillis();
        // filter out location updates within maxInterval is there is no movement
        movingFilter = new MovingFilter(maxInterval);

        updateSubscription.unsubscribe();
        updateSubscription = Observable.interval(updateInterval, TimeUnit.SECONDS, RxSchedulers.computation())
                .flatMap(aLong -> Observable.just(getLastTakenLocation()))
                .filter(this::filter)
                .doOnSubscribe(this::doOnUpdateDriverStart)
                .doOnUnsubscribe(this::doOnUpdateDriverStop)
                .switchMap(this::sendLocationUpdate)
                .doOnError(errorSubject::onNext)
                .retryWhen(new RetryWithDelay(BACKGROUND_ERROR_RETRY_DELAY_S * 1000L))
                .subscribe();
    }

    private boolean isValidByAccuracy(final RALocation location) {
        return location.getLocation().getAccuracy() < Constants.LOCATION_HORIZONTAL_ACCURACY_FILTER;
    }

    private void doOnUpdateDriverStart() {
        locationSubscription.unsubscribe();
        locationSubscription = locationManager.getLocationUpdates()
                // start with cached location (may be invalid or too old)
                .startWith(locationManager.getLastOfDefault())
                .filter(RALocation::isValid)
                .subscribe(this::setLastTakenLocation, throwable -> Timber.e(throwable, "Unable to receive location updates"));
    }

    private void doOnUpdateDriverStop() {
        locationSubscription.unsubscribe();
    }

    private Observable<Coordinates> sendLocationUpdate(RALocation location) {
        setLastSentLocation(location);
        return dataManager.updateDriver(location)
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof RetrofitException) {
                        RetrofitException retrofitException = (RetrofitException) throwable;
                        final int statusCode = retrofitException.getStatusCode();
                        if (statusCode == INACTIVE_DRIVER) {
                            stateManager.switchState(stateManager.createOfflinePoolingState());
                            // Driver is offline, no need to retry, stop here
                            return Observable.empty();
                        }

                        // record pending location update - no matter what network error occurred
                        // maybe do it only on bad network connectivity?..
                        App.getInstance().getPendingEventsManager().record(PendingEventType.UPDATE_LOCATION, location.getLocation());

                        if (retrofitException.causedByNetwork()) {
                            // we won't disturb driver with problems-with-network toasts
                            // assuming there is an indicator already shown
                            return Observable.just(new Coordinates(location.getLocation().getLatitude(),
                                    location.getLocation().getLongitude()));
                        }

                    }
                    // Otherwise, rely on stream
                    // It may show toasts and initiate retry
                    return Observable.error(throwable);
                });
    }

    private boolean filter(RALocation location) {
        if (location == null) {
            // If location is null, we cannot send it. So drop it.
            return false;
        }
        long left = TimeUtils.currentTimeMillis() - lastSentTimestamp;
        if (left + TIMER_DEVIATION_MS < interval * 1000L) {
            // It's too early for update
            return false;
        }
        RALocation sentLocation = getLastSentLocation();
        if (sentLocation == null) {
            // If we haven't sent a location yet, then skip the filter and go send one if its valid.
            return isValidByAccuracy(location);
        }
        return isValidByAccuracy(location) && movingFilter.filter(sentLocation, location, left);
    }

    private synchronized void setLastTakenLocation(RALocation location) {
        lastTakenLocation = location;
    }

    private synchronized RALocation getLastTakenLocation() {
        return lastTakenLocation;
    }

    private synchronized void setLastSentLocation(RALocation location) {
        lastSentTimestamp = TimeUtils.currentTimeMillis();
        lastSentLocation = location;
    }

    private synchronized RALocation getLastSentLocation() {
        return lastSentLocation;
    }

    /**
     * Class which helps filter out {@link RALocation} duplicates.
     * Filters out location updates within {@link MovingFilter#maxIntervalSec} if there is no movement.
     */
    private static class MovingFilter {

        private long maxIntervalSec;

        MovingFilter(long maxIntervalSec) {
            this.maxIntervalSec = maxIntervalSec;
        }

        public boolean filter(@NonNull RALocation lastSent, @NonNull RALocation lastTaken, long interval) {
            if (interval / 1000 < maxIntervalSec) {
                return hasMovement(lastSent, lastTaken);
            }
            return true;
        }

        private boolean hasMovement(RALocation lastSent, RALocation lastTaken) {
            if (lastSent.getTimestamp() == lastTaken.getTimestamp()) {
                return false;
            }

            //Note: Location has also speed variable. however system sends same Location object with speed after stopped.
            // It maybe because of fake GPS or Emulator GPS. Simply don't risk it.
            if (Math.abs(lastTaken.getLocation().getBearing() - lastSent.getLocation().getBearing()) > Constants.UPDATE_DRIVER_THRESHOLD_DEGREE) {
                return true;
            }
            return DistanceUtil.distance(lastSent.getCoordinates(), lastTaken.getCoordinates()) > Constants.UPDATE_DRIVER_THRESHOLD_M;
        }

    }
}
