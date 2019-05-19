package com.rideaustin.ui.drawer.queue;

import android.databinding.BaseObservable;
import android.databinding.ObservableField;
import android.graphics.drawable.Drawable;

import com.rideaustin.R;
import com.rideaustin.utils.RxImageLoader;

import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by hatak on 14.10.16.
 */

public class QueueEntryViewModel extends BaseObservable{

    public final ObservableField<String> carCategory = new ObservableField<>();
    public final ObservableField<String> queuePosition = new ObservableField<>();
    public final ObservableField<Drawable> carImage = new ObservableField<>();

    private Subscription carImageSubscription = Subscriptions.empty();

    public void setEntry(final QueueEntry entry) {
        carCategory.set(entry.getCarCategory());
        queuePosition.set(entry.getQueueValue());
        carImageSubscription.unsubscribe();
        carImageSubscription = RxImageLoader.execute(new RxImageLoader.Request(entry.getImageUrl())
                .target(carImage)
                .error(R.drawable.icn_generic_car));
    }

}
