package com.rideaustin.ui.splitfare;

import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import com.rideaustin.App;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.api.model.faresplit.FareSplitResponse;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.events.RideStatusEvent;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.RxBaseViewModel;
import com.rideaustin.utils.RetryWhenNoNetwork;
import com.rideaustin.utils.SingleSubject;

import java.util.ArrayList;
import java.util.List;

import rx.subjects.BehaviorSubject;
import timber.log.Timber;

/**
 * Created by hatak on 17.03.2017.
 */

public class FareSplitViewModel extends RxBaseViewModel {

    public final ObservableField<String> phone = new ObservableField<>();
    public final ObservableBoolean enableSendButton = new ObservableBoolean(false);
    public final ObservableBoolean showEmpty = new ObservableBoolean(true);
    public final ObservableBoolean showLoading = new ObservableBoolean(false);

    private BehaviorSubject<List<FareSplitItemViewModel>> listSubject = BehaviorSubject.create(new ArrayList<>());
    private SingleSubject<Void> closeSubject = SingleSubject.create();

    public FareSplitViewModel() {
        phone.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                String s = phone.get();
                enableSendButton.set(s != null && s.trim().length() > 5);
            }
        });
    }

    void initialize() {
        untilDestroy(App.getDataManager().getSplitFareChanged()
                .observeOn(RxSchedulers.main())
                .subscribe(aVoid -> reloadFareSplitStatus(), Timber::e));
        untilDestroy(App.getStateManager()
                .getRideStatus()
                .onErrorReturn(throwable -> new RideStatusEvent(RideStatus.UNKNOWN, "", null))
                .subscribe(this::doOnRideStatus));
        reloadFareSplitStatus();
    }

    rx.Observable<List<FareSplitItemViewModel>> getListObservable() {
        return listSubject.asObservable().onBackpressureLatest();
    }

    rx.Observable<Void> getCloseObservable() {
        return closeSubject.asObservable().onBackpressureDrop();
    }

    public void onSend() {
        String phoneNumber = phone.get().trim();
        untilDestroy(App.getDataManager().requestFareSplit(phoneNumber)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<FareSplitResponse>(this) {
                    @Override
                    public void onNext(FareSplitResponse fareSplitResponse) {
                        super.onNext(fareSplitResponse);
                        List<FareSplitItemViewModel> list = listSubject.getValue();
                        list.add(new FareSplitItemViewModel(fareSplitResponse));
                        showEmpty.set(false);
                        listSubject.onNext(list);
                    }
                }));
    }

    void reloadFareSplitStatus() {
        untilDestroy(App.getDataManager().getFareSplitRequestList()
                .retryWhen(new RetryWhenNoNetwork(5000))
                .observeOn(RxSchedulers.main())
                .doOnSubscribe(() -> {
                    if (showEmpty.get()) {
                        showEmpty.set(false);
                        showLoading.set(true);
                    }
                })
                .doOnUnsubscribe(() -> showLoading.set(false))
                .subscribe(new ApiSubscriber2<List<FareSplitResponse>>(false) {
                    @Override
                    public void onNext(List<FareSplitResponse> fareSplitResponses) {
                        super.onNext(fareSplitResponses);
                        onFareSplitResponses(fareSplitResponses);
                    }

                    @Override
                    public void onAnyError(BaseApiException e) {
                        super.onAnyError(e);
                        // any error except network-related
                        // check RetryWhenNoNetwork in stream
                        onFareSplitResponses(new ArrayList<>());
                    }
                }));
    }

    void deleteFareSplitRequest(long id) {
        untilDestroy(App.getDataManager().deleteFareSplitRequest(id)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<List<FareSplitResponse>>(this) {
                    @Override
                    public void onNext(List<FareSplitResponse> fareSplitResponses) {
                        super.onNext(fareSplitResponses);
                        onFareSplitResponses(fareSplitResponses);
                    }
                }));

    }

    private void doOnRideStatus(RideStatusEvent event) {
        switch (event.getData()) {
            case REQUESTED:
            case DRIVER_ASSIGNED:
            case DRIVER_REACHED:
            case ACTIVE:
                // these statuses are valid
                // do nothing
                break;
            default:
                // close screen
                closeSubject.onNext(null);
                break;
        }
    }

    private void onFareSplitResponses(List<FareSplitResponse> responses) {
        showEmpty.set(responses.isEmpty());
        List<FareSplitItemViewModel> oldList = listSubject.getValue();
        for (FareSplitItemViewModel viewModel : oldList) {
            viewModel.destroy();
        }
        List<FareSplitItemViewModel> newList = new ArrayList<>();
        for (FareSplitResponse response : responses) {
            newList.add(new FareSplitItemViewModel(response));
        }
        listSubject.onNext(newList);

    }

}
