package com.rideaustin.engine.state;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.Ride;
import com.rideaustin.engine.BaseEngineState;
import com.rideaustin.engine.DriverTrackingType;
import com.rideaustin.engine.SwitchNextData;
import com.rideaustin.schedulers.RxSchedulers;

import rx.Observable;
import timber.log.Timber;

import static com.rideaustin.utils.CommonConstants.UNEXPECTED_STATE_KEY;

/**
 * Created by rost on 8/11/16.
 */
public class OfflinePollingState extends BaseEngineState {

    public OfflinePollingState() {
        super(DriverTrackingType.NONE);
        App.getInstance().getAirportQueueManager().onOffline();
    }

    @Override
    public Observable<?> switchNext(SwitchNextData data) {
        return getDriverLocationManager()
                .getLastLocation(true, true)
                .switchMap(location -> {
                    final Integer cityId = App.getConfigurationManager().getLastConfiguration().getCurrentCity().getCityId();
                    return getDataManager().activateDriver(location, cityId);
                })
                .observeOn(RxSchedulers.main())
                .doOnError(throwable -> {
                    Timber.e(throwable);
                    switchToCorrectState();
                })
                .doOnCompleted(() -> switchState(getStateManager().restoreOnlineState()));
    }

    @Override
    protected void onActivated() {
        super.onActivated();
        subscribeUntilDeactivated(App.getInstance().getLongPollingManager()
                .getEvents()
                .observeOn(RxSchedulers.main())
                .subscribe(event -> {
                    Timber.d("::offline state:: :event: %s", event);
                    switch (event.getEventType()) {
                        case SURGE_AREA_UPDATE:
                        case SURGE_AREA_UPDATES:
                            getStateManager().postSurgeUpdate(event);
                            break;
                        case CAR_CATEGORY_CHANGE:
                            App.getInstance().getRideRequestManager().postCarCategoriesEvent(event);
                            break;
                        case DRIVER_TYPE_UPDATE:
                            App.getInstance().getRideRequestManager().postDriverTypeEvent(event);
                            break;
                        case QUEUED_AREA_ENTERING:
                            App.getInstance().getAirportQueueManager().onEntered();
                            break;
                        case QUEUED_AREA_UPDATE:
                            App.getInstance().getAirportQueueManager().onUpdated();
                            break;
                        case QUEUED_AREA_LEAVING:
                            App.getInstance().getAirportQueueManager().onLeaving(getAirportQueueMessage(event, R.string.queue_leaving_area_desc));
                            break;
                        case QUEUED_AREA_LEAVING_INACTIVE:
                            App.getInstance().getAirportQueueManager().onLeaving(getAirportQueueMessage(event, R.string.queue_leaving_inactive_desc));
                            break;
                        case QUEUED_AREA_LEAVING_PENALTY:
                            App.getInstance().getAirportQueueManager().onLeaving(getAirportQueueMessage(event, R.string.queue_leaving_penalty_desc));
                            break;
                        case RIDER_CANCELLED:
                        case ADMIN_CANCELLED:
                            // sometimes ride's status is not up-to-date
                            Ride ride = event.getRide();
                            ride.setStatus(event.getEventType().name());
                            getStateManager().postCancelledRide(ride);
                            break;
                        case DRIVER_CANCELLED:
                            // driver aware, skip
                            break;
                        case GO_OFFLINE:
                            // already offline
                            break;
                        case RATING_UPDATED:
                            //TODO:
                            break;
                        case HANDSHAKE:
                            Exception e = new Exception(UNEXPECTED_STATE_KEY);
                            Timber.e(e, "Received handshake request being offline: event=[" + event + "]");
                            processHandshake(event);
                            break;
                        case REQUESTED:
                            processRideRequest(event);
                            break;
                        default:
                            Exception e2 = new Exception(UNEXPECTED_STATE_KEY);
                            Timber.e(e2, "Offline state, unexpected event received: %s, switch to correct state", event.getEventType());
                            switchToCorrectState();
                    }
                }));
    }

    @Override
    public Type getType() {
        return Type.OFFLINE;
    }
}
