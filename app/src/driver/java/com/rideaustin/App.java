package com.rideaustin;

import android.media.AudioManager;
import android.os.PowerManager;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.MapsInitializer;
import com.rideaustin.api.DataManager;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.engine.LocalSerializer;
import com.rideaustin.engine.LongPollingManager;
import com.rideaustin.engine.PendingEventsManager;
import com.rideaustin.engine.StateManager;
import com.rideaustin.engine.DriverLocationManager;
import com.rideaustin.manager.AirportQueueManager;
import com.rideaustin.manager.DefaultSoundManager;
import com.rideaustin.manager.RideRequestManager;
import com.rideaustin.manager.SoundManager;
import com.rideaustin.utils.AutoOfflineHelper;

import timber.log.Timber;

/**
 * @author shumelchyk
 */
public class App extends BaseApp {

    private static App instance;
    private LocalSerializer localSerializer;
    private PowerManager.WakeLock wakeLock;
    private StateManager stateManager;
    private SoundManager soundManager;
    private PendingEventsManager pendingEventsManager;
    private AutoOfflineHelper autoOfflineHelper;
    private RideRequestManager rideRequestManager;
    private AirportQueueManager airportQueueManager;
    private LongPollingManager longPollingManager;
    private DriverLocationManager driverLocationManager;
    private static DataManager dataManager;

    @Override
    protected void init() {
        super.init();
        instance = this;
        initializeMap();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, App.class.getName());
        stateManager = new StateManager();
        localSerializer = new LocalSerializer(this);
        dataManager = initDataManager();
        soundManager = new DefaultSoundManager((AudioManager) getSystemService(AUDIO_SERVICE));
        pendingEventsManager = new PendingEventsManager();
        autoOfflineHelper = new AutoOfflineHelper(getVisibilityObservable(),
                getStateManager(),
                getNotificationManager(),
                getConfigurationManager(),
                getDataManager());
        rideRequestManager = new RideRequestManager(this,
                getConfigurationManager(),
                getDataManager(),
                getStateManager(),
                getPrefs(),
                getNotificationManager());
        airportQueueManager = new AirportQueueManager(dataManager,
                rideRequestManager,
                getNotificationManager());
        longPollingManager = new LongPollingManager(dataManager,
                stateManager,
                pendingEventsManager);
        driverLocationManager = new DriverLocationManager(dataManager,
                stateManager,
                getLocationManager(),
                getConfigurationManager());
    }

    private void initializeMap() {
        try {
            MapsInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            Timber.e(e, e.getMessage());
        }
    }

    @Override
    public void onTerminate() {
        wakeLockRelease();
        super.onTerminate();
    }

    public static String getAppName() {
        GlobalConfig lastConfiguration = App.getConfigurationManager().getLastConfiguration();
        if (lastConfiguration != null) {
            return lastConfiguration.getGeneralInformation().getApplicationName();
        }
        String cityName = getCityName();
        if (cityName == null) {
            return getInstance().getString(R.string.app_name);
        }
        return getInstance().getString(R.string.app_name_placeholder, cityName);
    }

    public static String getFormattedAppName() {
        GlobalConfig lastConfiguration = App.getConfigurationManager().getLastConfiguration();
        if (lastConfiguration != null) {
            return lastConfiguration.getGeneralInformation().getApplicationNamePipe();
        }
        String cityName = getCityName();
        if (cityName == null) {
            return getInstance().getString(R.string.app_name);
        }
        return getInstance().getString(R.string.app_name_formatted_placeholder, cityName);
    }

    public static DataManager getDataManager() {
        return dataManager;
    }

    public static App getInstance() {
        return instance;
    }

    public StateManager getStateManager() {
        return stateManager;
    }

    public LocalSerializer getLocalSerializer() {
        return localSerializer;
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public PendingEventsManager getPendingEventsManager() {
        return pendingEventsManager;
    }

    public RideRequestManager getRideRequestManager() {
        return rideRequestManager;
    }

    public AirportQueueManager getAirportQueueManager() {
        return airportQueueManager;
    }

    public LongPollingManager getLongPollingManager() {
        return longPollingManager;
    }

    public DriverLocationManager getDriverLocationManager() {
        return driverLocationManager;
    }

    public void wakeLockAcquire() {
        if (!wakeLock.isHeld()) {
            wakeLock.acquire();
        }
    }

    public void wakeLockRelease() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    public void refreshEndpoints() {
        if (dataManager != null) {
            dataManager.dispose();
        }
        dataManager = initDataManager();
        autoOfflineHelper.setDataManager(dataManager);
        rideRequestManager.setDataManager(dataManager);
        airportQueueManager.setDataManager(dataManager);
        longPollingManager.setDataManager(dataManager);
        driverLocationManager.setDataManager(dataManager);
    }

    @NonNull
    private DataManager initDataManager() {
        return new DataManager();
    }
}
