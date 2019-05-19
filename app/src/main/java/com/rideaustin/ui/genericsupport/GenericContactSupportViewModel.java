package com.rideaustin.ui.genericsupport;

import android.databinding.ObservableField;

import com.rideaustin.App;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.RxBaseObservable;
import com.rideaustin.utils.KeyboardUtil;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by Sergey Petrov on 17/03/2017.
 */

public class GenericContactSupportViewModel extends RxBaseObservable {

    public final ObservableField<String> message = new ObservableField<>();

    private final View view;

    private final long rideId;

    private final int cityId;

    private Subscription sendSubscription = Subscriptions.empty();

    public GenericContactSupportViewModel(View view, long rideId, int cityId) {
        this.view = view;
        this.rideId = rideId;
        if (cityId < 0) {
            this.cityId = App.getConfigurationManager().getLastConfiguration().getCurrentCity().getCityId();
        } else {
            this.cityId = cityId;
        }
    }

    public final void onSubmit(android.view.View view) {
        KeyboardUtil.hideKeyBoard(view.getContext(), view);
        sendMessage();
    }

    @Override
    public void onStop() {
        super.onStop();
        sendSubscription.unsubscribe();
    }

    private void sendMessage() {
        if (!App.getDataManager().isLoggedIn()) {
            // app would be sent to login on resume
            return;
        }
        sendSubscription.unsubscribe();
        sendSubscription = getContactSupportObservable(message.get())
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<Void>(view.getCallback()) {
                    @Override
                    public void onNext(Void aVoid) {
                        super.onNext(aVoid);
                        view.onMessageSent();
                    }
                });
    }

    private Observable<Void> getContactSupportObservable(final String message) {
        if (rideId > 0) {
            return App.getDataManager().getSupportService().sendSupportMessage(message, rideId, cityId);
        } else {
            return App.getDataManager().getSupportService().sendSupportMessage(message, cityId);
        }
    }

    public interface View {
        BaseActivityCallback getCallback();
        void onMessageSent();
    }
}
