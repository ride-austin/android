package com.rideaustin.ui.common;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * @author sdelaysam.
 */

public final class LifecycleSubscriptions {

    private CompositeSubscription untilPause;
    private CompositeSubscription untilStop;
    private CompositeSubscription untilDestroy;

    public void untilPause(Subscription subscription) {
        if (untilPause == null) {
            untilPause = new CompositeSubscription();
        }
        untilPause.add(subscription);
    }

    public void untilStop(Subscription subscription) {
        if (untilStop == null) {
            untilStop = new CompositeSubscription();
        }
        untilStop.add(subscription);
    }

    public void untilDestroy(Subscription subscription) {
        if (untilDestroy == null) {
            untilDestroy = new CompositeSubscription();
        }
        untilDestroy.add(subscription);
    }

    public void onPause() {
        if (untilPause != null) {
            untilPause.clear();
        }
    }

    public void onStop() {
        if (untilStop != null) {
            untilStop.clear();
        }
    }

    public void onDestroy() {
        if (untilDestroy != null) {
            untilDestroy.clear();
        }
    }

}
