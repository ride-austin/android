package com.rideaustin.ui.ride;

import com.rideaustin.api.DataManager;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.events.RideStatusEvent;
import com.rideaustin.manager.AppNotificationManager;
import com.rideaustin.manager.PrefManager;
import com.rideaustin.manager.StateManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static com.rideaustin.api.model.RideStatus.FINISHED;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Created by Sergey Petrov on 25/04/2017.
 */

@RunWith(RobolectricTestRunner.class)
public class RideStatusProcessorTest {

    @Mock DataManager dataManager;
    @Mock
    StateManager stateManager;
    @Mock AppNotificationManager notificationManager;
    @Mock PrefManager prefManager;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldProcessNullRide() {
        RideStatusProcessor processor = new RideStatusProcessor(dataManager, stateManager, notificationManager, prefManager);
        RideStatusEvent event = processor.processRideStatus(null);
        verify(dataManager).postFemaleModeEditable(true);
        verify(prefManager).updateRideInfo(0L, "");
        verify(stateManager).post(event);
        assertEquals(FINISHED, event.getData());
        assertNull(event.getError());
        assertNull(event.getResp());
        verifyZeroInteractions(notificationManager);
    }

    @Test
    public void shouldProcessRequested() {
        long rideId = 1L;
        RideStatus rideStatus = RideStatus.REQUESTED;
        RideStatusEvent event = processRide(rideId, rideStatus);
        verify(dataManager).postFemaleModeEditable(false);
        verify(prefManager).updateRideInfo(rideId, rideStatus.name());
        verify(stateManager).post(event);
        verifyZeroInteractions(notificationManager);
    }

    @Test
    public void shouldProcessNoAvailableDrivers() {
        long rideId = 1L;
        RideStatus rideStatus = RideStatus.NO_AVAILABLE_DRIVER;
        RideStatusEvent event = processRide(rideId, rideStatus);
        verify(dataManager).postFemaleModeEditable(true);
        verify(prefManager).updateRideInfo(0L, rideStatus.name());
        verify(stateManager).post(event);
        verify(notificationManager).notifyNoAvailableDrivers();
    }

    @Test
    public void shouldProcessAdminCancelled() {
        long rideId = 1L;
        RideStatus rideStatus = RideStatus.ADMIN_CANCELLED;
        RideStatusEvent event = processRide(rideId, rideStatus);
        verify(dataManager).postFemaleModeEditable(true);
        verify(prefManager).updateRideInfo(0L, rideStatus.name());
        verify(stateManager).post(event);
        verify(notificationManager).notifyAdminCancelled(any());
    }

    @Test
    public void shouldProcessDriverCancelled() {
        long rideId = 1L;
        RideStatus rideStatus = RideStatus.DRIVER_CANCELLED;
        RideStatusEvent event = processRide(rideId, rideStatus);
        verify(dataManager).postFemaleModeEditable(true);
        verify(prefManager).updateRideInfo(0L, rideStatus.name());
        verify(stateManager).post(event);
        verify(notificationManager).notifyDriverCancelled(any());
    }

    @Test
    public void shouldProcessRiderCancelled() {
        long rideId = 1L;
        RideStatus rideStatus = RideStatus.RIDER_CANCELLED;
        RideStatusEvent event = processRide(rideId, rideStatus);
        verify(dataManager).postFemaleModeEditable(true);
        verify(prefManager).updateRideInfo(0L, rideStatus.name());
        verify(stateManager).post(event);
        verify(notificationManager).notifyRiderCancelled(any());
    }

    @Test
    public void shouldProcessDriverAssigned() {
        long rideId = 1L;
        RideStatus rideStatus = RideStatus.DRIVER_ASSIGNED;
        RideStatusProcessor processor = new RideStatusProcessor(dataManager, stateManager, notificationManager, prefManager);
        RideStatusEvent event = processRide(processor, rideId, rideStatus);
        verify(dataManager).postFemaleModeEditable(false);
        verify(prefManager).updateRideInfo(rideId, rideStatus.name());
        verify(stateManager).post(event);
        verify(notificationManager).notifyDriverAssigned();
        reset(notificationManager, prefManager);
        // process same ride with same status
        processRide(processor, rideId, rideStatus);
        // check caching status works
        verify(notificationManager, never()).notifyDriverAssigned();
    }

    @Test
    public void shouldProcessDriverReached() {
        long rideId = 1L;
        RideStatus rideStatus = RideStatus.DRIVER_REACHED;
        RideStatusProcessor processor = new RideStatusProcessor(dataManager, stateManager, notificationManager, prefManager);
        RideStatusEvent event = processRide(processor, rideId, rideStatus);
        verify(dataManager).postFemaleModeEditable(false);
        verify(prefManager).updateRideInfo(rideId, rideStatus.name());
        verify(stateManager).post(event);
        verify(notificationManager).notifyDriverReached(event.getResp());
        reset(notificationManager, prefManager);
        // process same ride with same status
        processRide(processor, rideId, rideStatus);
        // check caching status works
        verify(notificationManager, never()).notifyDriverReached(event.getResp());
    }

    @Test
    public void shouldProcessActive() {
        long rideId = 1L;
        RideStatus rideStatus = RideStatus.ACTIVE;
        RideStatusEvent event = processRide(rideId, rideStatus);
        verify(dataManager).postFemaleModeEditable(false);
        verify(prefManager).updateRideInfo(rideId, rideStatus.name());
        verify(stateManager).post(event);
        verifyZeroInteractions(notificationManager);
    }

    @Test
    public void shouldProcessCompleted() {
        long rideId = 1L;
        RideStatus rideStatus = RideStatus.COMPLETED;
        RideStatusProcessor processor = new RideStatusProcessor(dataManager, stateManager, notificationManager, prefManager);
        RideStatusEvent event = processRide(processor, rideId, rideStatus);
        verify(dataManager).postFemaleModeEditable(true);
        verify(prefManager).updateRideInfo(0L, "");
        verify(prefManager).saveRideToRate(rideId);
        verify(stateManager).post(event);
        reset(notificationManager, prefManager);
        // process same ride with same status
        processRide(processor, rideId, rideStatus);
    }

    // this test failed on old code.
    // not sure if RideStatusProcessor's code about FINISHED was correct
    @Test
    public void shouldProcessFinished() {
        long rideId = 1L;
        RideStatus rideStatus = RideStatus.FINISHED;
        RideStatusEvent event = processRide(rideId, rideStatus);
        verify(prefManager).updateRideInfo(0L, "");
        verify(stateManager).post(event);
        verifyZeroInteractions(notificationManager);
    }

    private RideStatusEvent processRide(long rideId, RideStatus status) {
        RideStatusProcessor processor = new RideStatusProcessor(dataManager, stateManager, notificationManager, prefManager);
        return processRide(processor, rideId, status);
    }

    private RideStatusEvent processRide(RideStatusProcessor processor, long rideId, RideStatus status) {
        Ride ride = new Ride();
        ride.setId(rideId);
        ride.setStatus(status.name());
        RideStatusEvent event = processor.processRideStatus(ride);
        assertEquals(status, event.getData());
        assertEquals(ride, event.getResp());
        assertNull(event.getError());
        return event;
    }

}
