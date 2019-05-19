package com.rideaustin.ui.stacked;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.Rider;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.RxBaseViewModel;
import com.rideaustin.utils.AnswersUtils;
import com.rideaustin.utils.RxImageLoader;
import com.rideaustin.utils.SingleSubject;

import java8.util.Optional;
import rx.Observable;

/**
 * Created on 21/12/2017
 *
 * @author sdelaysam
 */

public class NextRideDialogViewModel extends RxBaseViewModel {

    public final ObservableBoolean loading = new ObservableBoolean(true);
    public final ObservableField<Drawable> avatar = new ObservableField<>();
    public final ObservableField<String> name = new ObservableField<>();
    public final ObservableField<String> category = new ObservableField<>();
    public final ObservableField<String> address = new ObservableField<>();
    public final ObservableField<String> callAction = new ObservableField<>();
    public final ObservableField<String> smsAction = new ObservableField<>();

    private Optional<Ride> rideOptional = Optional.empty();
    private SingleSubject<Void> dismissSubject = SingleSubject.create();

    void initialize() {
        untilDestroy(App.getInstance()
                .getStateManager()
                .getNextRide()
                .observeOn(RxSchedulers.main())
                .doOnSubscribe(() -> loading.set(true))
                .subscribe(new ApiSubscriber2<Optional<Ride>>(true) {
                    @Override
                    public void onNext(Optional<Ride> optional) {
                        super.onNext(optional);
                        doOnRide(optional);
                    }

                    @Override
                    public void onAnyError(BaseApiException e) {
                        super.onAnyError(e);
                        dismissSubject.onNext(null);
                    }
                }));
    }

    void cancelRider(@Nullable String code, @Nullable String reason) {
        untilDestroy(App.getInstance().getStateManager()
                .cancelNextRide(code, reason)
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<>(this)));
    }

    Observable<Void> getDismissObservable() {
        return dismissSubject.asObservable().serialize().onBackpressureDrop();
    }

    Optional<String> getPhone() {
        return rideOptional.map(Ride::getRider).map(Rider::getPhoneNumber);
    }

    private void doOnRide(Optional<Ride> optional) {
        this.rideOptional = optional;
        optional.ifPresentOrElse(ride -> {
            loading.set(false);
            loadAvatar(ride.getRider().getUser().getPhotoUrl());
            name.set(ride.getRider().getFullName());
            callAction.set(getString(R.string.call_to, ride.getRider().getFirstname()));
            smsAction.set(getString(R.string.sms_to, ride.getRider().getFirstname()));
            category.set(ride.getRequestedCarType().getTitle());
            address.set(ride.getStartAddressText());
        }, () -> {
            dismissSubject.onNext(null);
        });
    }

    private void loadAvatar(String url) {
        untilDestroy(RxImageLoader.execute(new RxImageLoader.Request(url)
                .target(avatar)
                .progress(R.drawable.rotating_circle)
                .error(R.drawable.ic_user_icon)
                .circular(true)));
    }

}
