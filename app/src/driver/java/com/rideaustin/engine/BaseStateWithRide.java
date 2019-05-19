package com.rideaustin.engine;

import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.Event;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideSpecificUpgradeStatus;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.api.model.RiderLocationUpdate;
import com.rideaustin.api.model.UpgradeRequest;
import com.rideaustin.api.model.UpgradeRequestStatus;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.ObjectUtils;
import com.rideaustin.utils.location.DistanceUtil;

import java8.util.Optional;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

import static com.rideaustin.utils.CommonConstants.UNEXPECTED_STATE_KEY;
import static com.rideaustin.utils.Constants.DIRECTION_KEY;

/**
 * Created by rost on 8/12/16.
 */
public abstract class BaseStateWithRide extends BaseEngineState {

    private Ride ride;
    private LatLng currentDestinationPoint;
    private BehaviorSubject<RideSpecificUpgradeStatus> rideUpgradeSubject = BehaviorSubject.create(RideSpecificUpgradeStatus.create(0L, UpgradeRequestStatus.NONE));

    public BaseStateWithRide(Ride ride, DriverTrackingType driverTrackingType) {
        super(driverTrackingType);
        this.ride = ride;
        rideUpgradeSubject.onNext(Optional.of(ride)
                .map(Ride::getUpgradeRequest)
                .map(UpgradeRequest::getStatus)
                .map(name -> RideSpecificUpgradeStatus.create(ride.getId(), UpgradeRequestStatus.valueOf(name)))
                .orElse(RideSpecificUpgradeStatus.create(0L, UpgradeRequestStatus.NONE)));
        setDestinationPoint(ride);
    }

    @Override
    protected void onActivated() {
        super.onActivated();
        startPoolingEvents();
    }

    public Observable<Direction> getDirection() {
        return getSerializer().readRx(DIRECTION_KEY, Direction.class)
                .subscribeOn(RxSchedulers.computation())
                .switchIfEmpty(loadDirectionToDestination())
                .observeOn(RxSchedulers.main());
    }

    private Observable<Direction> loadDirectionToDestination() {
        return getDriverLocationManager().getLastLocation(true, true)
                .switchMap(raLocation -> {
                    Double endLocationLat = getRide().getEndLocationLat();
                    Double endLocationLong = getRide().getEndLocationLong();
                    if (endLocationLat != null && endLocationLong != null) {
                        LatLng endPoint = new LatLng(endLocationLat, endLocationLong);
                        final LatLng startPoint = raLocation.getCoordinates();
                        return getDataManager().loadDirection(startPoint, endPoint);
                    } else {
                        return Observable.empty();
                    }
                });
    }

    public Observable<String> getStartAddress() {
        if (ride == null) {
            return Observable.just("");
        }
        if (ride.hasStartAddress()) {
            return Observable.just(ride.getStartAddressText());
        }
        //TODO: handle if we can't reverse geocode
        final LatLng latLng = new LatLng(ride.getStartLocationLat(), ride.getStartLocationLong());
        return getDataManager().loadAddress(latLng)
                .observeOn(RxSchedulers.serializer())
                .doOnNext(s -> ride.setStartAddress(s))
                .observeOn(RxSchedulers.main());
    }

    public Observable<String> getFinishAddress() {
        if (ride == null) {
            return Observable.just("");
        }
        if (!TextUtils.isEmpty(ride.getEndAddress())) {
            return Observable.just(ride.getEndAddress());
        }
        if ((ride.getEndLocationLat() == null) && (ride.getEndLocationLong() == null)) {
            return Observable.just("");
        }
        //TODO: handle if we can't reverse geocode
        final LatLng latLng = new LatLng(ride.getEndLocationLat(), ride.getEndLocationLong());
        return getDataManager().loadAddress(latLng)
                .observeOn(RxSchedulers.serializer())
                .doOnNext(s -> ride.setEndAddress(s))
                .observeOn(RxSchedulers.main());
    }


    public Observable<String> getFinishAddressForNavigation() {
        return isTripStartPending() ? getStartAddress() : getFinishAddress();
    }

    public LatLng getNavigationLocation() {
        return isTripStartPending() ? getPickupLocation() : getDropLocation();
    }

    private boolean isTripStartPending() {
        Type stateType = getStateManager().getCurrentEngineStateType();
        return stateType.equals(Type.ACCEPTED);
    }

    public LatLng getPickupLocation() {
        return (ride.getStartLocationLat() != null && ride.getStartLocationLong() != null)
                ? new LatLng(ride.getStartLocationLat(), ride.getStartLocationLong())
                : null;
    }

    public LatLng getDropLocation() {
        return (ride.getEndLocationLat() != null && ride.getEndLocationLong() != null)
                ? new LatLng(ride.getEndLocationLat(), ride.getEndLocationLong())
                : null;
    }

    public Observable<Direction> loadDirection(LatLng startPoint, LatLng endPoint) {
        return getDataManager().loadDirection(startPoint, endPoint);
    }

    public Ride getRide() {
        return ride;
    }

    private void startPoolingEvents() {
        subscribeUntilDeactivated(App.getInstance().getLongPollingManager()
                .getEvents()
                .observeOn(RxSchedulers.main())
                .subscribe(event -> {
                    Timber.d(":: " + getType() + " state:: :event: %s", event);
                    if (isNextRideEvent(event)) {
                        processNextRideEvent(event);
                    } else if (isValidEvent(event)) {
                        processEvent(event);
                    } else if (shouldSwitchStateOnEvent()) {
                        switchToCorrectState();
                    }
                }));
    }

    private boolean isNextRideEvent(Event event) {
        if (event.getNextRide() != null) {
            return true;
        }
        if (event.getEventType() == RideStatus.RIDE_STACKED_REASSIGNED) {
            return true;
        }
        return event.getRide() != null && getStateManager().getNextRideIfAny()
                    .filter(nextRide -> ObjectUtils.equals(nextRide.getId(), event.getRide().getId()))
                    .isPresent();
    }

    private boolean isValidEvent(Event event) {
        return event.getRide() == null || ObjectUtils.equals(event.getRide().getId(), getRide().getId());
    }

    private void processEvent(Event event) {
        if (event.getRide() != null) {
            getStateManager().postCurrentRide(event.getRide());
        }
        switch (event.getEventType()) {
            case RIDER_CANCELLED:
            case ADMIN_CANCELLED:
                getSerializer().remove(DIRECTION_KEY);
                // sometimes ride's status is not up-to-date
                Ride ride = event.getRide();
                ride.setStatus(event.getEventType().name());
                getStateManager().postCancelledRide(ride);
                if (shouldSwitchStateOnEvent()) {
                    getStateManager().getNextRideIfAny()
                            .ifPresentOrElse(nextRide -> {
                                switchState(getStateManager().createAcceptedState(nextRide));
                            }, () -> {
                                switchState(getStateManager().createOnlineState());
                            });
                    switchToCorrectState();
                }
                break;
            case GO_OFFLINE:
                getStateManager().postOfflineEvent(event);
                getSerializer().remove(DIRECTION_KEY);
                if (shouldSwitchStateOnEvent()) {
                    switchState(getStateManager().createOfflinePoolingState());
                }
                break;
            case QUEUED_AREA_LEAVING:
                App.getInstance().getAirportQueueManager().onLeaving(getAirportQueueMessage(event, R.string.queue_leaving_area_desc));
                break;
            case QUEUED_AREA_LEAVING_PENALTY:
                App.getInstance().getAirportQueueManager().onLeaving(getAirportQueueMessage(event, R.string.queue_leaving_penalty_desc));
                break;
            case QUEUED_AREA_LEAVING_RIDE:
                App.getInstance().getAirportQueueManager().onLeavedInRide();
                break;
            case RIDER_COMMENT_UPDATED:
                processRideComment(event);
                break;
            case END_LOCATION_UPDATED:
                processDestinationUpdate(event);
                break;
            case RIDER_LOCATION_UPDATED:
                RiderLocationUpdate update = RiderLocationUpdate.fromJson(event.getParameters());
                if (update != null) {
                    getStateManager().riderLocationUpdated(update);
                }
                break;
            case RIDE_UPGRADE_ACCEPTED:
                onRideUpgradeAccepted(event);
                break;
            case RIDE_UPGRADE_DECLINED:
                onRideUpgradeDeclined(event);
                break;
            default:
                if (isValidForCurrentState(event)) {
                    onRideEvent(event);
                } else {
                    switchToCorrectState();
                }
        }
    }

    private void processNextRideEvent(Event event) {
        Ride ride = event.getNextRide() != null ? event.getNextRide() : event.getRide();
        switch (event.getEventType()) {
            case RIDER_CANCELLED:
            case ADMIN_CANCELLED:
                // sometimes ride's status is not up-to-date
                ride.setStatus(event.getEventType().name());
                getStateManager().postCancelledRide(ride);
                getStateManager().postNextRide(null);
                break;
            case RIDE_STACKED_REASSIGNED:
                getStateManager().postNextRide(null);
                App.getNotificationManager().notifyRideReassigned(event);
                break;
            case HANDSHAKE:
                if (getType() != Type.STARTED) {
                    Exception e = new Exception(UNEXPECTED_STATE_KEY);
                    Timber.e(e, "Received handshake request: event=[" + event + "], state=" + getType());
                }
                processHandshake(event);
                break;
            case REQUESTED:
                processRideRequest(event);
                break;
            default:
                getStateManager().postNextRide(ride);
        }
    }

    protected void onRideEvent(Event event) {
        ride = event.getRide();
        processRideComment(event);
        checkDestinationChanged(event);
    }

    private void processRideComment(Event event) {
        ride = event.getRide();
        getStateManager().processRiderCommentsUpdate(ride);
    }

    private void processDestinationUpdate(Event event) {
        ride = event.getRide();
        getStateManager().postDestinationUpdateEvent(event);
        getSerializer().remove(DIRECTION_KEY);
        setDestinationPoint(ride);
    }

    private void onRideUpgradeDeclined(Event event) {
        NavigationDrawerActivity.getStartIntentForRideRequestLaunch();
        postUpgradeStatus(RideSpecificUpgradeStatus.create(ride.getId(), UpgradeRequestStatus.DECLINED));
        App.getNotificationManager().notifyRideUpgrade(R.string.upgrade_failed);
    }

    private void onRideUpgradeAccepted(Event event) {
        ride = event.getRide();
        NavigationDrawerActivity.getStartIntentForRideRequestLaunch();
        postUpgradeStatus(RideSpecificUpgradeStatus.create(ride.getId(), UpgradeRequestStatus.ACCEPTED));
        App.getNotificationManager().notifyRideUpgrade(R.string.upgrade_rider_confirmed);
    }

    public Observable<RideSpecificUpgradeStatus> getRideUpgradeObservable() {
        return rideUpgradeSubject;
    }

    public void postUpgradeStatus(RideSpecificUpgradeStatus newStatus) {
        rideUpgradeSubject.onNext(newStatus);
    }

    public RideSpecificUpgradeStatus getUpgradeStatus() {
        return rideUpgradeSubject.getValue();
    }

    private void setDestinationPoint(Ride ride) {
        if (ride != null && ride.getEndLocationLat() != null && ride.getEndLocationLong() != null) {
            currentDestinationPoint = new LatLng(ride.getEndLocationLat(), ride.getEndLocationLong());
        } else {
            currentDestinationPoint = null;
        }
    }

    private void checkDestinationChanged(Event event) {
        final LatLng newEndDestinationPoint = event.getRide().getEndLocation();
        final float distance;
        if (currentDestinationPoint != null && newEndDestinationPoint != null) {
            distance = DistanceUtil.distance(newEndDestinationPoint, currentDestinationPoint);
        } else if (currentDestinationPoint == null && newEndDestinationPoint != null) {
            distance = Float.MAX_VALUE;
        } else if (currentDestinationPoint != null) {
            distance = Float.MAX_VALUE;
        } else {
            //both currentDestinationPoint == null && newEndDestinationPoint == null should not change.
            distance = 0;
        }
        if (distance > Constants.CHANGE_DESTINATION_THRESHOLD_M) {
            processDestinationUpdate(event);
        }
    }

    private boolean isValidForCurrentState(Event event) {
        switch (event.getEventType()) {
            case REQUESTED:
                return getType() == Type.PENDING_ACCEPT;
            case DRIVER_ASSIGNED:
                return getType() == Type.ACCEPTED;
            case DRIVER_REACHED:
                return getType() == Type.ARRIVED;
            case ACTIVE:
                return getType() == Type.STARTED;
            case COMPLETED:
                return getType() == Type.ENDED;
        }
        return false;
    }

    private boolean shouldSwitchStateOnEvent() {
        return getType() != Type.ENDED;
    }
}
