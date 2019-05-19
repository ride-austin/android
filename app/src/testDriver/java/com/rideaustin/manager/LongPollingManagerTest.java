package com.rideaustin.manager;

import android.support.annotation.Nullable;

import com.rideaustin.api.DataManager;
import com.rideaustin.api.model.Event;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.engine.BaseEngineState;
import com.rideaustin.engine.EngineState;
import com.rideaustin.engine.DriverTrackingType;
import com.rideaustin.engine.LongPollingManager;
import com.rideaustin.engine.PendingEventsManager;
import com.rideaustin.engine.PendingEventsResponse;
import com.rideaustin.engine.StateManager;
import com.rideaustin.engine.SwitchNextData;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import java8.util.Optional;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created on 23/11/2017
 *
 * @author sdelaysam
 */

public class LongPollingManagerTest {

    @Mock DataManager dataManager;
    @Mock StateManager stateManager;
    @Mock PendingEventsManager pendingEventsManager;

    private PublishSubject<Event> serverEvents = PublishSubject.create();
    private BehaviorSubject<BaseEngineState> engineState = BehaviorSubject.create();
    private BehaviorSubject<PendingEventsResponse> pendingEvents = BehaviorSubject.create();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        doReturn(serverEvents).when(dataManager).getEvents();
        doReturn(true).when(dataManager).isLoggedIn();
        doReturn(engineState).when(stateManager).getEngineStateObservable();
        doAnswer(invocation -> pendingEvents.take(1)).when(pendingEventsManager).tryToSend();
    }

    @Test
    public void shouldKeepEventsUntilSubscription() {
        engineState.onNext(createEngineState(EngineState.Type.OFFLINE));
        pendingEvents.onNext(createPendingEvent(PendingEventsResponse.Result.NO_EVENTS, null));
        doReturn(Observable.just(false)).when(stateManager).syncOnPendingEvents(any());

        LongPollingManager manager = new LongPollingManager(dataManager, stateManager, pendingEventsManager);
        manager.start();

        verify(pendingEventsManager, times(1)).tryToSend();
        verify(dataManager, never()).loadRideStatus(anyLong());

        serverEvents.onNext(createEvent(1L, RideStatus.GO_OFFLINE, null));
        serverEvents.onNext(createEvent(2L, RideStatus.ACTIVE, null));

        TestSubscriber<Event> testSubscriber = TestSubscriber.create();
        manager.getEvents().subscribe(testSubscriber);

        testSubscriber.assertValueCount(2);
        testSubscriber.assertNoTerminalEvent();
        List<Event> events = testSubscriber.getOnNextEvents();
        assertEquals(1L, events.get(0).getId());
        assertEquals(2L, events.get(1).getId());

        TestSubscriber<Event> testSubscriber2 = TestSubscriber.create();
        manager.getEvents().subscribe(testSubscriber2);

        testSubscriber2.assertNoValues();
        testSubscriber2.assertNoTerminalEvent();

        serverEvents.onNext(createEvent(3L, RideStatus.ADMIN_CANCELLED, null));

        testSubscriber.assertValueCount(3);
        testSubscriber2.assertValueCount(1);

        events = testSubscriber2.getOnNextEvents();
        assertEquals(3L, events.get(0).getId());
    }

    @Test
    public void shouldTrySendPendingEvents() {
        engineState.onNext(createEngineState(EngineState.Type.OFFLINE));
        pendingEvents.onNext(createPendingEvent(PendingEventsResponse.Result.SEND_FAILED, new Exception("Any error")));

        LongPollingManager manager = new LongPollingManager(dataManager, stateManager, pendingEventsManager);
        manager.start();

        verify(pendingEventsManager, times(1)).tryToSend();
        verify(stateManager, never()).syncOnPendingEvents(any());
        verify(dataManager, never()).loadRideStatus(anyLong());
        verify(dataManager, never()).getEvents();

        TestSubscriber<Event> testSubscriber = TestSubscriber.create();
        manager.getEvents().subscribe(testSubscriber);
        testSubscriber.assertNoValues();
    }

    @Test
    public void shouldNotCheckRideStatusAfterPendingEventsSentButStateNotChanged() {
        engineState.onNext(createEngineState(EngineState.Type.STARTED));
        pendingEvents.onNext(createPendingEvent(PendingEventsResponse.Result.SEND_SUCCEEDED, null));
        doReturn(Observable.just(false)).when(stateManager).syncOnPendingEvents(any());

        LongPollingManager manager = new LongPollingManager(dataManager, stateManager, pendingEventsManager);
        manager.start();

        verify(pendingEventsManager, times(1)).tryToSend();
        verify(stateManager, times(1)).syncOnPendingEvents(any());
        verify(dataManager, never()).loadRideStatus(anyLong());

        TestSubscriber<Event> testSubscriber = TestSubscriber.create();
        manager.getEvents().subscribe(testSubscriber);
        testSubscriber.assertNoValues();

        serverEvents.onNext(createEvent(1L, RideStatus.GO_OFFLINE, null));
        serverEvents.onNext(createEvent(2L, RideStatus.ACTIVE, null));

        testSubscriber.assertValueCount(2);
        testSubscriber.assertNoTerminalEvent();
        List<Event> events = testSubscriber.getOnNextEvents();
        assertEquals(1L, events.get(0).getId());
        assertEquals(2L, events.get(1).getId());
    }

    private Event createEvent(long id, RideStatus status, @Nullable Ride ride) {
        final Event event = new Event();
        event.setId(id);
        event.setRide(ride);
        event.setEventType(status);
        return event;
    }

    private BaseEngineState createEngineState(BaseEngineState.Type type) {
        return new BaseEngineState(DriverTrackingType.NONE) {
            @Override
            public Type getType() {
                return type;
            }

            @Override
            public Observable<?> switchNext(SwitchNextData data) {
                return Observable.empty();
            }
        };
    }

    private PendingEventsResponse createPendingEvent(PendingEventsResponse.Result result,
                                                     @Nullable Exception exception) {
        return new PendingEventsResponse(result, exception, Optional.empty());
    }

}
