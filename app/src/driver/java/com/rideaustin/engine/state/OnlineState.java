package com.rideaustin.engine.state;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.AirportQueueParams;
import com.rideaustin.api.model.Ride;
import com.rideaustin.engine.BaseEngineState;
import com.rideaustin.engine.DriverTrackingType;
import com.rideaustin.engine.SwitchNextData;
import com.rideaustin.schedulers.RxSchedulers;

import java8.util.Optional;
import rx.Observable;
import timber.log.Timber;

import static com.rideaustin.utils.CommonConstants.UNEXPECTED_STATE_KEY;
import static com.rideaustin.utils.Constants.DIRECTION_KEY;

/**
 * Created by rost on 8/11/16.
 */
public class OnlineState extends BaseEngineState {

    public OnlineState() {
        super(DriverTrackingType.ONLINE);
    }

    @Override
    public Observable<?> switchNext(SwitchNextData data) {
        return getDataManager().deactivateDriver()
                .observeOn(RxSchedulers.main())
                .doOnError(throwable -> {
                    Timber.e(throwable);
                    switchToCorrectState();
                })
                .doOnCompleted(() -> switchState(getStateManager().createOfflinePoolingState()));
    }

    @Override
    public Type getType() {
        return Type.ONLINE;
    }

    @Override
    protected void onActivated() {
        super.onActivated();
        App.getInstance().wakeLockAcquire();
        getSerializer().remove(DIRECTION_KEY);
        subscribeUntilDeactivated(App.getInstance().getLongPollingManager()
                .getEvents()
                .observeOn(RxSchedulers.main())
                .subscribe(event -> {
                    Timber.d("::online state:: :event: %s", event);
                    switch (event.getEventType()) {
                        case GO_OFFLINE:
                            getStateManager().postOfflineEvent(event);
                            switchState(getStateManager().createOfflinePoolingState());
                            break;
                        case HANDSHAKE:
                            processHandshake(event);
                            break;
                        case REQUESTED:
                            processRideRequest(event);
                            break;
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
                        case RATING_UPDATED:
                            //TODO:
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
                        default:
                            Exception e = new Exception(UNEXPECTED_STATE_KEY);
                            Timber.e(e, "Online state, unexpected event received: %s, switch to correct state", event.getEventType());
                            switchToCorrectState();
                    }
                }));
    }
}
