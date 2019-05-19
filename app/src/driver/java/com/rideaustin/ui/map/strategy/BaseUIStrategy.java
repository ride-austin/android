package com.rideaustin.ui.map.strategy;

import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.base.retrofit.RetrofitException;
import com.rideaustin.engine.BaseEngineState;
import com.rideaustin.manager.LocationNotAvailableException;
import com.rideaustin.manager.LocationTimeoutException;
import com.rideaustin.manager.MissingLocationPermissionException;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.map.MapFragmentInterface;
import com.rideaustin.utils.NetworkHelper;
import com.rideaustin.utils.toast.RAToast;

import java.net.HttpURLConnection;
import java.util.concurrent.TimeoutException;

import java8.util.Optional;
import retrofit2.Response;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

/**
 * Created by rost on 8/12/16.
 */
public abstract class BaseUIStrategy<T extends BaseEngineState> implements UIStrategy {

    private final T state;
    private boolean isAttached;
    private MapFragmentInterface view;
    private CompositeSubscription untilDetachSubscription = new CompositeSubscription();
    private CompositeSubscription untilDestroySubscription = new CompositeSubscription();

    public BaseUIStrategy(T state) {
        this.state = state;
    }

    @Override
    public final void attach(MapFragmentInterface view) {
        this.view = view;
        isAttached = true;
        onAttach(view);
    }

    @Override
    public final void detach() {
        // RA-12279: do not clear reference to a view
        // loading subscriber may still use it after detach
        // Let's revisit it after 3.4 release
        if (isAttached) {
            onDetach();
            isAttached = false;
        }
    }

    @Override
    public final void destroy(MapFragmentInterface view) {
        // RA-12279: do not clear reference to a view,
        // strategy is about to be destroyed and all subscriptions should be gone...
        // Let's revisit it after 3.4 release
        onDestroy();
    }

    @Override
    public boolean hasView() {
        return view != null;
    }

    @Override
    public void onCancelRideSelected(@Nullable String code, @Nullable String reason) {
    }

    @CallSuper
    protected void onAttach(MapFragmentInterface view) {
        listenToGoOfflineEvent();
    }

    @CallSuper
    protected void onDetach() {
        untilDetachSubscription.clear();
    }

    @CallSuper
    protected void onDestroy() {
        untilDestroySubscription.clear();
    }

    public T getState() {
        return state;
    }

    public MapFragmentInterface getView() {
        return view;
    }

    protected void subscribeUntilDetach(Subscription subscription) {
        untilDetachSubscription.add(subscription);
    }

    protected void subscribeUntilDestroy(Subscription subscription) {
        untilDestroySubscription.add(subscription);
    }

    protected void processNetworkError(RetrofitException e, final boolean showDefaultErrors) {
        Timber.e(e, "::processNetworkError::");
        // Handle network error like ApiSubscriber2
        Response response = e.getResponse();
        if (response != null) {
            switch (response.code()) {
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    App.getDataManager().logoutUserFromApp(e.getMessage());
                    break;
                case HttpURLConnection.HTTP_BAD_REQUEST:
                    if (showDefaultErrors) {
                        RAToast.showShort(e.getMessage());
                    }
                    break;
                // terms not accepted
                case HttpURLConnection.HTTP_PRECON_FAILED:
                    if (showDefaultErrors) {
                        view.showTermsDialog(e.getMessage());
                    }
                    break;
                default:
                    RAToast.showShort(R.string.error_unknown);
                    break;
            }
        } else if (!NetworkHelper.isNetworkAvailable() || e.causedByNetwork()) {
            RAToast.showShort(R.string.network_error);
        }
    }

    protected <V> Subscriber<V> createLoadingSubscriber(final boolean showDefaultErrors) {
        return createLoadingSubscriber(true, showDefaultErrors);
    }

    protected <V> Subscriber<V> createLoadingSubscriber(Runnable onSuccess, final boolean showDefaultLoading, final boolean showDefaultErrors) {
        Subscriber<V> subscriber = new Subscriber<V>() {
            @Override
            public void onStart() {
                super.onStart();
                if (showDefaultLoading) {
                    view.showLoading();
                }
            }

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                if (e instanceof RetrofitException) {
                    processNetworkError((RetrofitException) e, showDefaultErrors);
                } else if (e instanceof MissingLocationPermissionException) {
                    view.showMissingLocationPermissionMessage();
                } else if (e instanceof LocationNotAvailableException) {
                    view.showNoLocationAvailableDialog();
                } else if (e instanceof LocationTimeoutException) {
                    RAToast.showShort(R.string.location_timeout_error);
                } else if (e instanceof TimeoutException) {
                    RAToast.showShort(R.string.network_error);
                } else {
                    RAToast.showShort(R.string.error_unknown);
                    Timber.w(e, "Error cause is unknown. Pay attention to this.");
                }
            }

            @Override
            public void onNext(Object o) {
                Optional.ofNullable(onSuccess).ifPresent(Runnable::run);
            }
        };
        if (showDefaultLoading) {
            subscriber.add(Subscriptions.create(() -> view.hideLoading()));
        }
        return subscriber;
    }

    protected <V> Subscriber<V> createLoadingSubscriber(final boolean showDefaultLoading, final boolean showDefaultErrors) {
        return createLoadingSubscriber(null, showDefaultLoading, showDefaultErrors);
    }

    protected class DefaultErrorProcessor implements Action1<Throwable> {
        private boolean wasErrorShown = false;

        @Override
        public void call(Throwable throwable) {
            if (throwable != null) {
                if (!wasErrorShown) {
                    if (throwable instanceof RetrofitException) {
                        processNetworkError((RetrofitException) throwable, true);
                    } else if (throwable instanceof MissingLocationPermissionException) {
                        getView().showNoLocationAvailableDialog();
                    }
                }
                wasErrorShown = true;
            }
        }
    }

    @Override
    public boolean onMenuItemSelected(int menuId) {
        return false;
    }

    protected boolean hasPendingEvents() {
        return App.getInstance().getPendingEventsManager().hasUnsent();
    }

    protected boolean pendingEventsSending() {
        return App.getInstance().getPendingEventsManager().isSending();
    }

    protected void listenToPendingEventsSending() {
        subscribeUntilDetach(App.getInstance().getPendingEventsManager().isSendingObservable()
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnPendingEventsSending));
    }

    protected void doOnPendingEventsSending(boolean sending) {
        // let subclass override it
    }

    private void listenToGoOfflineEvent() {
        subscribeUntilDetach(getState().goOfflineObservable()
                .observeOn(RxSchedulers.main())
                .subscribe(params -> {
                    switch (params.getType()) {
                        case CAR_TYPES_DEACTIVATE:
                            Timber.i("car type deactivate");
                            break;
                        case MISSED_RIDES:
                        case DRIVER_INACTIVE:
                            App.getNotificationManager().showMessage(params.getMessage(), false);
                            break;
                        case TERMS_NOT_ACCEPTED:
                            getView().showTermsDialog(params.getMessage());
                            break;
                        case UNKNOWN:
                            Timber.w("Received unexpected event from server: " + params.getSource());
                            break;
                    }
                }));
    }
}
