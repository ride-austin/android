package com.rideaustin.ui.map;

import com.rideaustin.App;
import com.rideaustin.events.MapUpdateEvent;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.Constants;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

/**
 * Created by yshloma on 14.07.2016.
 */
public class RefreshManager {

    private static volatile RefreshManager instance;

    /**
     * Should start only one refresher
     *
     * @return
     */
    public static RefreshManager getInstance() {
        RefreshManager localInstance = instance;
        if (localInstance == null) {
            synchronized (RefreshManager.class) {
                if (localInstance == null) {
                    instance = localInstance = new RefreshManager();
                }
            }
        }
        return localInstance;
    }

    private MapUpdateEvent updateEvent = new MapUpdateEvent();
    private AtomicBoolean emitFlag = new AtomicBoolean(false);
    private Subscription refresherSubscription = Subscriptions.empty();

    private RefreshManager() {
    }

    private void initEmitter() {
        refresherSubscription = Observable
                .interval(Constants.MAP_REFRESH_INTERVAL, TimeUnit.SECONDS)
                .subscribeOn(RxSchedulers.computation())
                .takeWhile(aLong -> emitFlag.get())
                .observeOn(RxSchedulers.main())
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onCompleted() {
                        Timber.i("Refresher complete");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, RefreshManager.class.getName());
                    }

                    @Override
                    public void onNext(Long aLong) {
                        App.getStateManager().post(updateEvent);
                    }
                });
    }

    public void start() {
        emitFlag.set(true);
        initEmitter();
    }

    public void pause() {
        emitFlag.set(false);
        refresherSubscription.unsubscribe();
    }

}
