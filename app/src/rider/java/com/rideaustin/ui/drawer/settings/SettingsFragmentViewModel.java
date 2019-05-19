package com.rideaustin.ui.drawer.settings;

import android.databinding.ObservableField;
import android.graphics.drawable.Drawable;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.User;
import com.rideaustin.entities.GeoPosition;
import com.rideaustin.ui.common.RxBaseViewModel;
import com.rideaustin.utils.RxImageLoader;

/**
 * Created by ysych on 04.07.2016.
 */
public class SettingsFragmentViewModel extends RxBaseViewModel {

    private ObservableField<User> user = new ObservableField<>();
    public final ObservableField<Drawable> userImage = new ObservableField<>();

    public SettingsFragmentViewModel() {
        refreshUser();
    }

    public ObservableField<User> getUser() {
        return user;
    }

    public void setUser(User user) {
        if (user != null) {
            this.user.set(user);
            untilDestroy(RxImageLoader.execute(new RxImageLoader.Request(user.getPhotoUrl())
                    .target(userImage)
                    .progress(R.drawable.rotating_circle)
                    .error(R.drawable.ic_user_icon)
                    .circular(true)));
        }
    }

    public String getFavoritePlaceHome() {
        return App.getPrefs().getHome().map(GeoPosition::getAddressLine).orElse("");
    }

    public String getFavoritePlaceWork() {
        return App.getPrefs().getWork().map(GeoPosition::getAddressLine).orElse("");
    }

    public void refreshUser() {
        setUser(App.getDataManager().getCurrentUser());
    }
}
