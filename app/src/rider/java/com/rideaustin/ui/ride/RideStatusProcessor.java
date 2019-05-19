package com.rideaustin.ui.ride;

import com.rideaustin.api.DataManager;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.events.RideStatusEvent;
import com.rideaustin.manager.AppNotificationManager;
import com.rideaustin.manager.PrefManager;
import com.rideaustin.manager.StateManager;

import timber.log.Timber;

import static com.rideaustin.api.model.RideStatus.ACTIVE;
import static com.rideaustin.api.model.RideStatus.ADMIN_CANCELLED;
import static com.rideaustin.api.model.RideStatus.DRIVER_ASSIGNED;
import static com.rideaustin.api.model.RideStatus.DRIVER_CANCELLED;
import static com.rideaustin.api.model.RideStatus.DRIVER_REACHED;
import static com.rideaustin.api.model.RideStatus.NO_AVAILABLE_DRIVER;
import static com.rideaustin.api.model.RideStatus.REQUESTED;
import static com.rideaustin.api.model.RideStatus.RIDER_CANCELLED;
import static com.rideaustin.api.model.RideStatus.RIDE_REQUEST_ERROR;

/**
 * Extracted the ride response processing code out of RideStatusService
 * so that it can be reused from other places which are sources of RideStatus.
 * <p>
 * Created by supreethks on 25/11/16.
 */

public class RideStatusProcessor {

    private RideStatus cachedStatus;
    private DataManager dataManager;
    private PrefManager prefManager;
    private StateManager stateManager;
    private AppNotificationManager notificationManager;
    private boolean isDriverAssigned;
    private boolean isDirectConnect;

    public RideStatusProcessor(DataManager dataManager, StateManager stateManager, AppNotificationManager notificationManager, PrefManager prefManager) {
        this.dataManager = dataManager;
        this.stateManager = stateManager;
        this.notificationManager = notificationManager;
        this.prefManager = prefManager;
    }

    public void setDirectConnect(boolean directConnect) {
        isDirectConnect = directConnect;
    }

    public RideStatusEvent processRideStatus(Ride response) {
        //We initialize with this state and then override in subsequent lines
        RideStatusEvent event = RideStatusEvent.from(response);
        RideStatus status = event.getData();
        dataManager.postFemaleModeEditable(isFemaleDriverModeEditable(status));

        switch (status) {
            case REQUESTED:
                prefManager.updateRideInfo(response.getId(), REQUESTED.name());
                stateManager.post(event);
                break;
            case NO_AVAILABLE_DRIVER:
                prefManager.updateRideInfo(0L, NO_AVAILABLE_DRIVER.name());
                stateManager.post(event);
                onNoAvailableDrivers();
                break;
            case ADMIN_CANCELLED:
                prefManager.updateRideInfo(0L, ADMIN_CANCELLED.name());
                stateManager.post(event);
                notificationManager.notifyAdminCancelled(response);
                break;
            case DRIVER_CANCELLED:
                prefManager.updateRideInfo(0L, DRIVER_CANCELLED.name());
                stateManager.post(event);
                notificationManager.notifyDriverCancelled(response);
                break;
            case RIDER_CANCELLED:
                prefManager.updateRideInfo(0L, RIDER_CANCELLED.name());
                stateManager.post(event);
                notificationManager.notifyRiderCancelled(response);
                break;
            case DRIVER_ASSIGNED:
                prefManager.updateRideInfo(response.getId(), DRIVER_ASSIGNED.name());
                stateManager.post(event);
                isDriverAssigned = true;
                if (cachedStatus != status) {
                    notificationManager.notifyDriverAssigned();
                }
                break;
            case DRIVER_REACHED:
                prefManager.updateRideInfo(response.getId(), DRIVER_REACHED.name());
                stateManager.post(event);
                isDriverAssigned = true;
                if (cachedStatus != status) {
                    notificationManager.notifyDriverReached(response);
                }
                break;
            case ACTIVE:
                prefManager.updateRideInfo(response.getId(), ACTIVE.name());
                stateManager.post(event);
                break;
            case COMPLETED:
                notificationManager.readMessages(response.getId());
                prefManager.saveRideToRate(response.getId());
                stateManager.post(event);
                prefManager.updateRideInfo(0L, "");
                // server will deliver push
                break;
            case FINISHED:
                prefManager.updateRideInfo(0L, "");
                stateManager.post(event);
                break;
            case RIDE_REQUEST_ERROR:
                prefManager.updateRideInfo(0L, RIDE_REQUEST_ERROR.name());
                break;

        }
        cachedStatus = status;
        return event;
    }

    private void onNoAvailableDrivers() {
        if (isDriverAssigned) {
            notificationManager.notifyRedispatchFailed();
        } else if (isDirectConnect) {
            notificationManager.notifyDirectConnectFailed();
        } else {
            notificationManager.notifyNoAvailableDrivers();
        }
    }

    private boolean isFemaleDriverModeEditable(RideStatus rideStatus) {
        switch (rideStatus) {
            case REQUESTED:
            case DRIVER_ASSIGNED:
            case DRIVER_REACHED:
            case ACTIVE:
                return false;
        }
        return true;
    }
}
