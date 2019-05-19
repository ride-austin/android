package com.rideaustin.ui.splitfare;

import android.databinding.ObservableField;
import android.graphics.drawable.Drawable;

import com.rideaustin.R;
import com.rideaustin.api.model.faresplit.FareSplitResponse;
import com.rideaustin.utils.RxImageLoader;

import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created on 18/01/2018
 *
 * @author sdelaysam
 */

public class FareSplitItemViewModel {

    public final ObservableField<Drawable> avatar = new ObservableField<>();
    public final ObservableField<String> name = new ObservableField<>();

    private final FareSplitResponse response;
    private Subscription subscription = Subscriptions.empty();

    FareSplitItemViewModel(FareSplitResponse response) {
        this.response = response;
        name.set(response.getRiderFullName());
        subscription = RxImageLoader.execute(new RxImageLoader.Request(response.getRiderPhoto())
                .target(avatar)
                .progress(R.drawable.rotating_circle)
                .error(R.drawable.ic_user_icon)
                .circular(true, 90));
    }

    FareSplitResponse.SplitFareState getStatus() {
        return response.getStatus();
    }

    long getId() {
        return response.getId();
    }

    public void destroy() {
        subscription.unsubscribe();
    }

}
