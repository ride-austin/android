package com.rideaustin.ui.drawer.triphistory;

import android.databinding.ObservableField;

import com.rideaustin.App;
import com.rideaustin.api.model.SupportRequest;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.RxBaseObservable;
import com.rideaustin.utils.KeyboardUtil;

import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by Sergey Petrov on 17/03/2017.
 */

public class SupportTopicMessageViewModel extends RxBaseObservable {

    public final ObservableField<String> message = new ObservableField<>();

    private final View view;

    private final long rideId;

    private final int parentTopicId;

    private Subscription sendSubscription = Subscriptions.empty();

    public SupportTopicMessageViewModel(View view, long rideId, int parentTopicId) {
        this.view = view;
        this.rideId = rideId;
        this.parentTopicId = parentTopicId;
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
        SupportRequest request = new SupportRequest();
        request.setRideId(rideId > -1 ? rideId : null);
        request.setTopicId(parentTopicId);
        request.setComments(message.get());
        sendSubscription.unsubscribe();
        sendSubscription = App.getDataManager().getSupportService().sendSupportMessage(request)
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

    public interface View {
        BaseActivityCallback getCallback();
        void onMessageSent();
    }
}
