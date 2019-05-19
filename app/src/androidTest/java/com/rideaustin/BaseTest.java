package com.rideaustin;

import android.annotation.TargetApi;
import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.annotation.CallSuper;
import android.support.annotation.StringRes;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingPolicies;
import android.support.test.espresso.IdlingResource;

import com.jakewharton.espresso.OkHttp3IdlingResource;
import com.rideaustin.api.CommonDataManager;
import com.rideaustin.api.model.Coordinates;
import com.rideaustin.utils.DeviceTestUtils;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

/**
 * Created by Sergey Petrov on 04/08/2017.
 */

public class BaseTest {

    public static final long IDLE_TIMEOUT_MS = 25000;
    public static final double DEFAULT_LAT = 30.41655;
    public static final double DEFAULT_LNG = -97.749334;
    public static final float DEFAULT_ACCURACY = 3f;

    public Coordinates[] DEFAULT_ROUTE = {
            new Coordinates(DEFAULT_LAT, DEFAULT_LNG),
            new Coordinates(30.417281, -97.750042),
            new Coordinates(30.416929, -97.750482),
            new Coordinates(30.416615, -97.750117),
            new Coordinates(30.415967, -97.750600),
            new Coordinates(30.415051, -97.751179),
            new Coordinates(30.414718, -97.752338),
            new Coordinates(30.415551, -97.752542),
            new Coordinates(30.415976, -97.751866),
            new Coordinates(30.416643, -97.751330),
            new Coordinates(30.417124, -97.750857),
            new Coordinates(30.416948, -97.750493),
            new Coordinates(30.417272, -97.750031)
    };


    private static final String OK_HTTP = "OkHttp";
    private MockLocationProvider mockLocationProvider;

    @BeforeClass
    public static void before() {
        DeviceTestUtils.setAirplaneMode(false);
    }

    @CallSuper
    @Before
    public void setUp() {
        setUpLongTimeouts();
        registerOkHttpIdlingResources();
        registerMockLocations();
    }

    @CallSuper
    @After
    public void tearDown() {
        clearMockLocations();
        cleanOkHttpIdlingResources();
        DeviceTestUtils.setAirplaneMode(false);
    }

    private void clearMockLocations() {
        if (mockLocationProvider != null) {
            mockLocationProvider.shutdown();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setMockApplication(false);
        }
    }

    private void registerMockLocations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setMockApplication(true);
        }

        mockLocationProvider = new MockLocationProvider(LocationManager.GPS_PROVIDER, getAppContext());
        mockLocation(DEFAULT_LAT, DEFAULT_LNG);
    }

    public void mockLocation(double lat, double lng) {
        mockLocation(lat, lng, DEFAULT_ACCURACY);
    }

    public void mockLocation(double lat, double lng, float accuracy) {
        mockLocationProvider.pushLocation(lat, lng, accuracy);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setMockApplication(boolean allow) {
        final String command = "appops set " + BuildConfig.APPLICATION_ID + " android:mock_location " + (allow ? "allow" : "deny");
        Timber.i("Will try the command: '%s'", command);
        try (ParcelFileDescriptor pd = getInstrumentation().getUiAutomation().executeShellCommand(command)) {
            try (FileReader fileReader = new FileReader(pd.getFileDescriptor())) {
                IOUtils.readLines(fileReader);
            }
        } catch (IOException e) {
            Timber.e("Cannot set mock provider, related tests may fail");
        }
    }

    private static void setUpLongTimeouts() {
        IdlingPolicies.setMasterPolicyTimeout(IDLE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        IdlingPolicies.setIdlingResourceTimeout(IDLE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    private static void registerOkHttpIdlingResources() {
        CommonDataManager.setIdlingInterface((client, tag) -> {
            String name = OK_HTTP + " [" + tag + "]:" + client;
            IdlingResource resource = OkHttp3IdlingResource.create(name, client);
            Espresso.registerIdlingResources(resource);
        });
    }

    private static void cleanOkHttpIdlingResources() {
        List<IdlingResource> idlingResources = Espresso.getIdlingResources();
        for (IdlingResource resource : idlingResources) {
            if (resource.getName().startsWith(OK_HTTP)) {
                Espresso.unregisterIdlingResources(resource);
            }
        }
    }

    public static String getString(@StringRes int id) {
        return getAppContext().getString(id);
    }

    public static String getString(@StringRes int id, Object... args) {
        return getAppContext().getString(id, args);
    }

    public static Context getAppContext() {
        return getInstrumentation().getTargetContext().getApplicationContext();
    }

}
