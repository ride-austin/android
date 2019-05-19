package com.rideaustin.manager;

import android.support.annotation.StringRes;
import android.util.Pair;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.DataManager;
import com.rideaustin.api.model.QueueResponse;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.RetryWhenNoNetwork;
import com.rideaustin.utils.toast.RAToast;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

/**
 * Created on 21/11/2017
 *
 * @author sdelaysam
 */

public class AirportQueueManager {

    private DataManager dataManager;
    private RideRequestManager rideRequestManager;
    private AppNotificationManager notificationManager;

    private BehaviorSubject<QueueResponse> currentQueue = BehaviorSubject.create();
    private Subscription queueSubscription = Subscriptions.empty();

    public AirportQueueManager(DataManager dataManager,
                               RideRequestManager rideRequestManager,
                               AppNotificationManager notificationManager) {
        this.dataManager = dataManager;
        this.rideRequestManager = rideRequestManager;
        this.notificationManager = notificationManager;
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void onEntered() {
        queueSubscription.unsubscribe();
        queueSubscription = dataManager.getDriverQueue()
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .retryWhen(new RetryWhenNoNetwork(1000))
                .subscribe(response -> {
                    showEnterDialog(response);
                    currentQueue.onNext(response);
                }, throwable -> {
                    Timber.e(throwable);
                    currentQueue.onNext(null);
                });
    }

    public void onUpdated() {
        queueSubscription.unsubscribe();
        queueSubscription = dataManager.getDriverQueue()
                .zipWith(rideRequestManager.getSelectedCarTypes(false).take(1), Pair::new)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .retryWhen(new RetryWhenNoNetwork(1000))
                .subscribe(pair -> {
                    QueueResponse response = pair.first;
                    List<RequestedCarType> selected = pair.second;
                    Map<RequestedCarType, Integer> positions = new LinkedHashMap<>();
                    for (Map.Entry<String, Integer> entry : response.getPositions().entrySet()) {
                        for (RequestedCarType carType : selected) {
                            if (carType.getCarCategory().equals(entry.getKey())) {
                                positions.put(carType, entry.getValue());
                            }
                        }
                    }
                    currentQueue.onNext(response);
                    if (!positions.isEmpty()) {
                        showQueueUpdateDialog(response.getAreaQueueName(), positions);
                    }
                }, throwable -> {
                    Timber.e(throwable);
                    currentQueue.onNext(null);
                });
    }

    public void onLeaving(String message) {
        currentQueue.onNext(null);
        showLeavingDialog(message);
    }

    public void onLeavedInRide() {
        currentQueue.onNext(null);
    }

    public void onOffline() { currentQueue.onNext(null); }

    public Observable<QueueResponse> getCurrentQueue() {
        Observable<QueueResponse> observable = currentQueue.asObservable();
        if (currentQueue.getValue() == null) {
            observable = observable.startWith(dataManager.getDriverQueue()
                    .onErrorReturn(throwable -> null));
        }
        return observable.serialize().onBackpressureLatest();
    }

    private void showEnterDialog(QueueResponse response) {
        String message = getString(R.string.queue_zone_desc_greetings, response.getAreaQueueName());
        RAToast.showShort(message);
        notificationManager.notifyAirportQueue(message);
    }

    private void showLeavingDialog(String message) {
        RAToast.showLong(message);
        notificationManager.notifyAirportQueue(message);
    }

    private void showQueueUpdateDialog(String name, Map<RequestedCarType, Integer> positions) {
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.queue_zone, name));
        sb.append("\n");
        sb.append(getString(R.string.queue_zone_desc_you));
        sb.append("\n");
        Iterator<Map.Entry<RequestedCarType, Integer>> iterator = positions.entrySet().iterator();
        final String hondaCategory = Constants.CarCategory.HONDA.toLowerCase();
        if (Collections.min(positions.values()) < Constants.MINIMAL_QUEUE_POSITION_TO_NOTIFY) {
            while (iterator.hasNext()) {
                Map.Entry<RequestedCarType, Integer> entry = iterator.next();
                RequestedCarType carType = entry.getKey();
                // ignore honda requests types
                if (carType.getCarCategory().toLowerCase().contains(hondaCategory)) {
                    continue;
                }
                sb.append(entry.getValue() == 0
                        ? getString(R.string.queue_zone_desc_next, carType.getTitle())
                        : getString(R.string.queue_zone_desc_number, entry.getValue() + 1, carType.getTitle())
                );
                sb.append(iterator.hasNext() ? "\n" : "");
            }
            RAToast.showLong(sb.toString());
        }
        notificationManager.notifyAirportQueueUpdated(name, positions.values());
    }

    private String getString(@StringRes int resId) {
        return App.getInstance().getString(resId);
    }

    private String getString(@StringRes int resId, Object... args) {
        return App.getInstance().getString(resId, args);
    }

}
