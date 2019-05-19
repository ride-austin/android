package com.rideaustin.manager;

import android.support.annotation.Nullable;

import com.rideaustin.App;
import com.rideaustin.events.DriverRatedEvent;
import com.rideaustin.events.MapUpdateEvent;
import com.rideaustin.events.RideStatusEvent;

import java8.util.Optional;
import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by hatak on 03.10.2017.
 */

public class StateManager {

    private BehaviorSubject<Optional<RideStatusEvent>> rideStatusSubject = BehaviorSubject.create(Optional.empty());
    private BehaviorSubject<Optional<DriverRatedEvent>> rideRateSubject = BehaviorSubject.create(Optional.empty());
    private BehaviorSubject<Optional<MapUpdateEvent>> mapUpdateSubject = BehaviorSubject.create(Optional.empty());


    public void post(@Nullable RideStatusEvent event) {
        rideStatusSubject.onNext(Optional.ofNullable(event));
    }

    public Observable<RideStatusEvent> getRideStatus() {
        return rideStatusSubject
                .filter(Optional::isPresent)
                .map(Optional::get)
                .asObservable()
                .onBackpressureBuffer();
    }

    public Optional<RideStatusEvent> getLastRideStatus() {
        return rideStatusSubject.getValue();
    }

    public void clearRideState() {
        rideStatusSubject.onNext(Optional.empty());
        App.getPrefs().updateRideInfo(0L, "");
    }

    public void clearRideToRateState() {
        rideRateSubject.onNext(Optional.empty());
        App.getPrefs().removeRideToRate();
    }

    public void post(@Nullable DriverRatedEvent driverRatedEvent) {
        clearRideState();
        rideRateSubject.onNext(Optional.ofNullable(driverRatedEvent));
    }

    public void post(@Nullable MapUpdateEvent mapUpdateEvent) {
        mapUpdateSubject.onNext(Optional.ofNullable(mapUpdateEvent));
    }

    public Observable<DriverRatedEvent> getRateStatus() {
        return rideRateSubject
                .filter(Optional::isPresent)
                .map(Optional::get)
                .asObservable()
                .onBackpressureBuffer();
    }

    public Observable<MapUpdateEvent> getMapStatus() {
        return mapUpdateSubject
                .filter(Optional::isPresent)
                .map(Optional::get)
                .asObservable()
                .onBackpressureBuffer();
    }
}
