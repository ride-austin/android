package com.rideaustin.ui.drawer;

import android.databinding.BaseObservable;
import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.config.ReferFriend;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.manager.notification.InAppMessage;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.ImageHelper;
import com.rideaustin.utils.RxImageLoader;

import rx.subscriptions.CompositeSubscription;

;

/**
 * Created by ysych on 07.07.2016.
 */
public class NavigationDrawerHeaderViewModel extends BaseObservable {

    public final ObservableField<Drawable> userImage = new ObservableField<>();
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private CompositeSubscription untilDestroy = new CompositeSubscription();
    private ObservableField<Driver> user = new ObservableField<>();
    private final View view;

    public ObservableField<Driver> getUser() {
        return user;
    }

    public void setUser(Driver user) {
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
        setUser(App.getDataManager().getCurrentDriver());
        this.view = view;
    }

    public void onResume() {
        if (App.getDataManager().isLoggedIn()) {
            subscriptions.add(App.getConfigurationManager()
                    .getConfigurationUpdates()
                    .subscribeOn(RxSchedulers.computation())
                    .observeOn(RxSchedulers.main())
                    .subscribe(new ApiSubscriber<GlobalConfig>() {
                        @Override
                        public void onNext(final GlobalConfig configuration) {
                            updateReferFriend(configuration);
                        }
                    }));
        }
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

    private void updateReferFriend(final GlobalConfig configuration) {
        view.onReferFriendChanged(configuration.getReferFriend());
    }

    public interface View {
        void onReferFriendChanged(final ReferFriend referFriend);
    }
}
