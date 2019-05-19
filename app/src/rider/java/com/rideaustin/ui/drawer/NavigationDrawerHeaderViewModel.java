package com.rideaustin.ui.drawer;

import android.databinding.BaseObservable;
import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.ConfigAppInfoResponse;
import com.rideaustin.api.model.User;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.manager.notification.InAppMessage;
import com.rideaustin.manager.notification.SplitFareMessage;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.signin.StartupError;
import com.rideaustin.utils.AppInfoUtil;
import com.rideaustin.utils.ImageHelper;
import com.rideaustin.utils.RxImageLoader;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by ysych on 07.07.2016.
 */
public class NavigationDrawerHeaderViewModel extends BaseObservable {

    public final ObservableField<Drawable> userImage = new ObservableField<>();
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private CompositeSubscription untilDestroy = new CompositeSubscription();
    private ObservableField<User> user = new ObservableField<>();
    private final View view;

    public ObservableField<User> getUser() {
        return user;
    }

    public void setUser(User user) {
        if (user != null) {
            this.user.set(user);
            untilDestroy.add(RxImageLoader.execute(new RxImageLoader.Request(user.getPhotoUrl())
                    .target(userImage)
                    .progress(R.drawable.rotating_circle)
                    .error(R.drawable.ic_user_icon)
                    .circular(true)));
        }
    }

    public NavigationDrawerHeaderViewModel(final View view) {
        setUser(App.getDataManager().getCurrentUser());
        this.view = view;
    }

    public void onCreate() {
        if (App.getDataManager().needSync()) {
            // get config
            untilDestroy.add(App.getConfigurationManager()
                    .getConfiguration()
                    .subscribeOn(RxSchedulers.network())
                    .observeOn(RxSchedulers.main())
                    .subscribe(this::doOnAppConfig, throwable -> doOnNoNetwork()));
        }
    }

    public void onResume() {
    }

    private void doOnAppConfig(ConfigAppInfoResponse config) {
        if (AppInfoUtil.isMandatoryRequired(config) && AppInfoUtil.canShowUpdate()) {
            doOnUpgradeNeeded();
        } else {
            // get user
            untilDestroy.add(App.getDataManager().getUserObservable()
                    .subscribeOn(RxSchedulers.network())
                    .observeOn(RxSchedulers.main())
                    .subscribe(this::doOnUser, this::doOnUserFailed));
        }
    }

    private void doOnUser(User user) {
        setUser(user);
    }

    private void doOnUserFailed(Throwable throwable) {
        App.getDataManager().setStartupError(StartupError.USER_FAILED);
        view.onGotoSplash();
    }

    private void doOnUpgradeNeeded() {
        App.getDataManager().setStartupError(StartupError.UPGRADE_NEEDED);
        view.onGotoSplash();
    }

    private void doOnNoNetwork() {
        App.getDataManager().setStartupError(StartupError.NO_NETWORK);
        view.onGotoSplash();
    }
    public void onPause() {
        subscriptions.clear();
    }

    public void onDestroy() {
        untilDestroy.clear();
    }

    public void showMessage(InAppMessage message) {
        App.getInstance().getInAppMessageManager().show(message);
    }

    public void readMessage(InAppMessage message) {
        App.getInstance().getInAppMessageManager().read(message);
    }

    public void acceptSplitFare(SplitFareMessage message, boolean accept) {
        untilDestroy.add(App.getDataManager().getFareService()
                .acceptFareSplitRequest(message.getSplitFareId(), accept)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<>(view)));
    }

    public interface View extends BaseActivityCallback {
        void onGotoSplash();
    }

}
