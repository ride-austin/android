package com.rideaustin.ui.base;

import com.rideaustin.App;
import com.rideaustin.api.model.AvatarType;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.api.model.ServerMessage;
import com.rideaustin.api.model.UpgradeRequest;
import com.rideaustin.api.model.UpgradeRequestStatus;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.RxponentialBackoffRetry;

import java8.util.Optional;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;


/**
 * Created by hatak on 21.06.2017.
 */

public class BaseRiderViewModel {

    private UpgradeRequestStatus uiRideUpgradeStatus;
    Subscription rideStatusSubscription = Subscriptions.empty();
    Subscription rideUpgradeSubscription = Subscriptions.empty();
    PublishSubject<UpgradeRequest> rideUpgradeRequests = PublishSubject.create();

    public void startRideUpgradeListening(RiderBaseView view) {
        uiRideUpgradeStatus = loadStatus();
        listenToRideStatus(view);
        listenToRideUpgrade(view);
    }

    private void listenToRideStatus(RiderBaseView view) {
        rideStatusSubscription = App.getDataManager()
                .getCurrentRideObservable()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .observeOn(RxSchedulers.main())
                .subscribe(ride -> {
                    RideStatus rideStatus = RideStatus.valueOf(ride.getStatus());
                    switch (rideStatus) {
                        case DRIVER_REACHED:
                        case DRIVER_ASSIGNED:
                            if (ride.getUpgradeRequest() != null
                                    && (uiRideUpgradeStatus == UpgradeRequestStatus.SEND ||
                                    uiRideUpgradeStatus == UpgradeRequestStatus.REQUESTED ||
                                    uiRideUpgradeStatus == UpgradeRequestStatus.NONE)) {
                                rideUpgradeRequests.onNext(ride.getUpgradeRequest());
                            }
                            break;
                        case ADMIN_CANCELLED:
                        case DRIVER_CANCELLED:
                        case RIDER_CANCELLED:
                        case COMPLETED:
                        case NO_AVAILABLE_DRIVER:
                        case REQUESTED:
                            view.onRideUpgradeDialogClose();
                            listenToRideUpgrade(view);
                            saveUpgradeRequestStatus(UpgradeRequestStatus.NONE);
                            break;
                    }
                }, throwable -> Timber.w(throwable, "Error when checking ride upgrade status"));
    }

    private void listenToRideUpgrade(RiderBaseView view) {
        rideUpgradeSubscription.unsubscribe();
        rideUpgradeSubscription = rideUpgradeRequests
                .subscribeOn(RxSchedulers.main())
                .distinctUntilChanged((u1, u2) -> UpgradeRequestStatus.valueOf(u1.getStatus()) == UpgradeRequestStatus.valueOf(u2.getStatus()))
                .subscribe(upgradeRequest -> {
                    UpgradeRequestStatus status = UpgradeRequestStatus.valueOf(upgradeRequest.getStatus());
                    switch (status) {
                        case EXPIRED:
                            saveUpgradeRequestStatus(UpgradeRequestStatus.EXPIRED);
                            view.onRideUpgradeFailed(UpgradeRequestStatus.EXPIRED);
                            rideUpgradeSubscription.unsubscribe();
                            break;
                        case DECLINED:
                            saveUpgradeRequestStatus(UpgradeRequestStatus.DECLINED);
                            view.onRideUpgradeFailed(UpgradeRequestStatus.DECLINED);
                            rideUpgradeSubscription.unsubscribe();
                            break;
                        case REQUESTED:
                            if (uiRideUpgradeStatus != UpgradeRequestStatus.SEND) {
                                uiRideUpgradeStatus = UpgradeRequestStatus.REQUESTED;
                                view.onRideUpgradeRequested(upgradeRequest);
                            }
                            break;
                        case ACCEPTED:
                            saveUpgradeRequestStatus(UpgradeRequestStatus.ACCEPTED);
                            rideUpgradeSubscription.unsubscribe();
                            break;
                        case CANCELLED:
                            saveUpgradeRequestStatus(UpgradeRequestStatus.CANCELLED);
                            view.onRideUpgradeFailed(UpgradeRequestStatus.CANCELLED);
                            rideUpgradeSubscription.unsubscribe();
                    }
                }, throwable -> Timber.w(throwable, "error while processing ride upgrade status"));
    }

    public void stopRideUpgradeListening() {
        rideStatusSubscription.unsubscribe();
        rideUpgradeSubscription.unsubscribe();
    }


    public void acceptRideUpgrade(final RiderBaseView view, final BaseActivityCallback callback) {
        uiRideUpgradeStatus = UpgradeRequestStatus.SEND;
        App.getDataManager().getRidesService()
                .acceptRideUpgrade()
                .retryWhen(new RxponentialBackoffRetry().getNotificationHandler())
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<ServerMessage>(callback) {
                    @Override
                    public void onNext(ServerMessage serverMessage) {
                        view.onRideUpgradeResponse(serverMessage);
                    }
                });

    }

    public void declineRideUpgrade(final BaseActivityCallback callback) {
        uiRideUpgradeStatus = UpgradeRequestStatus.SEND;
        App.getDataManager().getRidesService()
                .declineRideUpgrade(AvatarType.RIDER.name())
                .retryWhen(new RxponentialBackoffRetry().getNotificationHandler())
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<>(callback));
    }

    private UpgradeRequestStatus loadStatus() {
        return App.getPrefs().getRideUpgradeStatus();
    }

    private void saveUpgradeRequestStatus(UpgradeRequestStatus status) {
        uiRideUpgradeStatus = status;
        App.getPrefs().saveRideUpgradeStatus(status);
    }

    protected interface RiderBaseView {
        void onRideUpgradeFailed(UpgradeRequestStatus status);

        void onRideUpgradeRequested(final UpgradeRequest upgradeRequest);

        void onRideUpgradeResponse(ServerMessage serverMessage);

        void onRideUpgradeDialogClose();
    }
}
