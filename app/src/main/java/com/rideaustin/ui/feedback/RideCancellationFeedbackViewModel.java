package com.rideaustin.ui.feedback;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.RideCancellationReason;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.RxBaseViewModel;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.SingleSubject;
import com.rideaustin.utils.toast.RAToast;
import com.rideaustin.utils.toast.ToastMode;

import java.util.List;

import java8.util.Optional;
import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

/**
 * Created on 21/03/2018
 *
 * @author sdelaysam
 */

public class RideCancellationFeedbackViewModel extends RxBaseViewModel {

    public final ObservableBoolean loading = new ObservableBoolean();
    public final ObservableBoolean submitEnabled = new ObservableBoolean(false);
    public final ObservableBoolean showComment = new ObservableBoolean();
    public final ObservableField<String> comment = new ObservableField<>();
    public final boolean isDriver = Constants.IS_DRIVER;

    private BehaviorSubject<List<RideCancellationReason>> reasons = BehaviorSubject.create();
    private SingleSubject<Boolean> complete = SingleSubject.create();
    private Optional<RideCancellationReason> selectedReason = Optional.empty();
    private Subscription cancelRideSubscription = Subscriptions.empty();
    private Subscription submitSubscription = Subscriptions.unsubscribed();

    Observable<List<RideCancellationReason>> getReasons() {
        if (!App.getDataManager().isLoggedIn()) {
            return Observable.empty();
        }
        return reasons.asObservable()
                .serialize()
                .onBackpressureLatest()
                .doOnSubscribe(this::loadReasons);
    }

    Optional<RideCancellationReason> getSelectedReason() {
        return selectedReason;
    }

    Observable<Boolean> getComplete() {
        return complete.asObservable()
                .serialize()
                .onBackpressureDrop();
    }

    void selectReason(RideCancellationReason reason) {
        selectedReason = Optional.ofNullable(reason);
        boolean canComment = reason.canComment();
        if (canComment != showComment.get()) {
            showComment.set(canComment);
            if (canComment) {
                comment.addOnPropertyChangedCallback(commentChangeCallback);
            } else {
                comment.removeOnPropertyChangedCallback(commentChangeCallback);
            }
        }
        checkSubmitButton();
    }

    void submit(long rideId, @Nullable RideCancellationFeedbackFragment.Action action) {
        if (!submitEnabled.get()) {
            showErrorMessage(App.getInstance().getString(R.string.cancel_feedback_no_reason));
            return;
        }
        if (action != null) {
            cancelRideSubscription.unsubscribe();
            untilDestroy(cancelRideSubscription = App.getDataManager().getCancelledRide()
                    .filter(id -> id == rideId)
                    .subscribe(id -> complete.onNext(true), Timber::e));
            String code = selectedReason.get().getCode();
            action.execute(code, comment.get());
        } else {
            submit(rideId);
        }
    }

    void showErrorMessage(String message) {
        RAToast.show(message, Toast.LENGTH_SHORT, ToastMode.LIGHT);
    }

    boolean isInProgress() {
        return !submitSubscription.isUnsubscribed();
    }

    private void loadReasons() {
        if (reasons.hasValue()) {
            return;
        }
        untilDestroy(App.getDataManager().getRideCancellationReasons()
                .doOnSubscribe(() -> loading.set(true))
                .doOnUnsubscribe(() -> loading.set(false))
                .subscribe(new ApiSubscriber2<List<RideCancellationReason>>(true) {
                    @Override
                    public void onNext(List<RideCancellationReason> list) {
                        super.onNext(list);
                        reasons.onNext(list);
                    }

                    @Override
                    public void onAnyError(BaseApiException e) {
                        super.onAnyError(e);
                        submitEnabled.set(false);
                    }

                    @Override
                    protected void showToast(String message) {
                        showErrorMessage(message);
                    }
                }));
    }

    private void checkSubmitButton() {
        submitEnabled.set(selectedReason
                .map(reason -> !reason.canComment() || !TextUtils.isEmpty(comment.get()))
                .orElse(false));
    }

    private void submit(long rideId) {
        String code = selectedReason.get().getCode();
        submitSubscription.unsubscribe();
        untilDestroy(submitSubscription = App.getDataManager().getSupportService()
                .postRideCancellationFeedback(rideId, code, comment.get())
                .subscribeOn(RxSchedulers.network())
                .subscribe(new ApiSubscriber2<Void>(this) {
                    @Override
                    public void onNext(Void aVoid) {
                        super.onNext(aVoid);
                        complete.onNext(true);
                    }

                    @Override
                    protected void showToast(String message) {
                        showErrorMessage(message);
                    }
                }));
    }

    private android.databinding.Observable.OnPropertyChangedCallback commentChangeCallback = new android.databinding.Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(android.databinding.Observable observable, int i) {
            checkSubmitButton();
        }
    };
}
