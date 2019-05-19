package com.rideaustin.ui.drawer.settings;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.DriverStatsConfig;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.ui.common.RxBaseViewModel;
import com.rideaustin.ui.model.NavigationAppPreference;
import com.rideaustin.utils.RxImageLoader;

import java8.util.Optional;
import timber.log.Timber;

/**
 * Created by ysych on 04.07.2016.
 */
public class SettingsFragmentViewModel extends RxBaseViewModel {

    public final ObservableBoolean showStats = new ObservableBoolean(false);
    private ObservableField<Driver> user = new ObservableField<>();
    public final ObservableField<Drawable> userImage = new ObservableField<>();

    public SettingsFragmentViewModel() {
        untilDestroy(App.getConfigurationManager().getLiveConfig()
                .subscribe(this::doOnConfig, Timber::e));
    }

    public ObservableField<Driver> getUser() {
        return user;
    }

    public void setUser(Driver user) {
        if (user != null) {
            this.user.set(user);
            untilDestroy(RxImageLoader.execute(new RxImageLoader.Request(user.getPhotoUrl())
                    .target(userImage)
                    .progress(R.drawable.rotating_circle)
                    .error(R.drawable.ic_user_icon)
                    .circular(true)));
        }
    }

    public void refreshUser() {
        App.getDataManager().getDriver()
                .ifPresent(this::setUser);
    }


    private void doOnConfig(GlobalConfig config) {
        showStats.set(Optional.ofNullable(config)
                .map(GlobalConfig::getDriverStatsConfig)
                .map(DriverStatsConfig::isEnabled)
                .orElse(false));
    }

    public String getDefaultNavigationApp() {
        NavigationAppPreference preference = App.getPrefs().getDriverNavigationActivity(App.getDataManager().getCurrentDriver());
        Timber.d("::getDefaultNavigationApp:: %s", preference);
        return preference == null ? null : preference.getAppName();
    }
}
