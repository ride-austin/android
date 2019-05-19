package com.rideaustin;

import android.location.Geocoder;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.rideaustin.api.config.CurrentCity;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.manager.AppNotificationManager;
import com.rideaustin.manager.AppVisibilityState;
import com.rideaustin.manager.ConfigurationManager;
import com.rideaustin.manager.ConnectionStatusManager;
import com.rideaustin.manager.PrefManager;
import com.rideaustin.manager.location.LocationConfiguration;
import com.rideaustin.manager.location.RALocationManager;
import com.rideaustin.manager.notification.InAppMessageManager;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.AppForegroundMonitor;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.CrashlyticsTree;
import com.rideaustin.utils.DeviceInfoUtil;
import com.rideaustin.utils.TimeUtils;
import com.rideaustin.utils.file.logging.FileLoggingTree;
import com.squareup.leakcanary.LeakCanary;

import java.util.Locale;

import io.fabric.sdk.android.Fabric;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

import static com.rideaustin.utils.CommonConstants.LocationManager.LOCATION_UPDATE_SMALLEST_DISPLACEMENT_M;
import static com.rideaustin.utils.CommonConstants.LocationManager.LOCATION_UPDATE_TIME_INTERVAL_MS;

/**
 * Created by supreethks on 23/10/16.
 */

public class BaseApp extends MultiDexApplication implements AppForegroundMonitor.Callback {

    private static PrefManager prefs;
    private static Geocoder geocoder;
    private static RALocationManager locationManager;
    private static ConfigurationManager configurationManager;
    private static AppNotificationManager notificationManager;
    private InAppMessageManager inAppMessageManager;
    private ConnectionStatusManager connectionStatusManager;
    private BehaviorSubject<AppVisibilityState> visibilityState = BehaviorSubject.create(AppVisibilityState.BACKGROUND);

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
        init();
    }

    protected void init() {
        notificationManager = new AppNotificationManager(this);
        inAppMessageManager = new InAppMessageManager(notificationManager);
        registerActivityLifecycleCallbacks(new AppForegroundMonitor(this));
        enableStrictMode();

        initSchedulers();

        initTrueTime();

        initCrashLogger();
        initLogger();
        initializeLibs();
        initManagers();
    }

    private void initSchedulers() {
        RxSchedulers.init();
    }

    private void initTrueTime() {
        TimeUtils.init(this);
    }

    private void enableStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeathOnNetwork()
                    .build());

            StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder()
                    .detectActivityLeaks();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                vmPolicyBuilder = vmPolicyBuilder.detectFileUriExposure();
            }
            StrictMode.setVmPolicy(vmPolicyBuilder
                    .penaltyLog()
                    .build());
        }
    }

    private void initializeLibs() {
        // RA-12162: manifest meta seems not enough for some users
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }

    private void initLogger() {
        Timber.plant(isFabricDisabled() ? new Timber.DebugTree() : new CrashlyticsTree());
        Timber.plant(new FileLoggingTree(getApplicationContext(), Constants.LOG_TAG));
    }

    private void initCrashLogger() {
        CrashlyticsCore core = new CrashlyticsCore.Builder()
                .disabled(isFabricDisabled())
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(core).build());
    }

    private boolean isFabricDisabled() {
        return BuildConfig.DEBUG || DeviceInfoUtil.isEspresso();
    }

    private void initManagers() {
        prefs = new PrefManager();
        geocoder = new Geocoder(this, Locale.getDefault());
        final LocationConfiguration config = new LocationConfiguration(
                LOCATION_UPDATE_TIME_INTERVAL_MS,
                LOCATION_UPDATE_SMALLEST_DISPLACEMENT_M
        );
        locationManager = new RALocationManager(config, this);
        configurationManager = new ConfigurationManager(this);
        connectionStatusManager = new ConnectionStatusManager(this);
    }

    @Nullable
    public static String getCityName() {
        GlobalConfig lastConfiguration = getConfigurationManager().getLastConfiguration();
        if (lastConfiguration == null) {
            return null;
        }
        CurrentCity city = lastConfiguration.getCurrentCity();
        if (city == null) {
            return null;
        }
        return capitalizeFirstLetter(city.getCityName());
    }

    public static String capitalizeFirstLetter(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }

    public static PrefManager getPrefs() {
        return prefs;
    }

    public static Geocoder getGeocoder() {
        return geocoder;
    }

    public static RALocationManager getLocationManager() {
        return locationManager;
    }

    public static ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public static AppNotificationManager getNotificationManager() {
        return notificationManager;
    }

    public InAppMessageManager getInAppMessageManager() {
        return inAppMessageManager;
    }

    public ConnectionStatusManager getConnectionStatusManager() {
        return connectionStatusManager;
    }

    public static void logoutFacebook() {
        if (AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut();
        }
    }

    public Observable<AppVisibilityState> getVisibilityObservable() {
        return visibilityState.asObservable().onBackpressureLatest();
    }

    public AppVisibilityState getVisibilityState() {
        return visibilityState.getValue();
    }

    @Override
    public void onAppForeground() {
        visibilityState.onNext(AppVisibilityState.FOREGROUND);
    }

    @Override
    public void onAppBackground() {
        visibilityState.onNext(AppVisibilityState.BACKGROUND);
    }
}
