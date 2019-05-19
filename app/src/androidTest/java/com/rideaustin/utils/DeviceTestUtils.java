package com.rideaustin.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.manager.ConnectionStatus;

import junit.framework.Assert;

import org.hamcrest.Matcher;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static com.rideaustin.BaseTest.IDLE_TIMEOUT_MS;
import static com.rideaustin.BaseUITest.getString;
import static java.lang.Thread.sleep;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Sergey Petrov on 22/05/2017.
 */

public class DeviceTestUtils {

    public static void pressHome() {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        device.pressHome();
    }

    public static void pressBack() {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        device.pressBack();
    }

    public static void restoreFromRecentApps() throws RemoteException, UiObjectNotFoundException, InterruptedException {
        restoreFromRecentApps(0L);
    }

    public static void restoreFromRecentApps(long afterMs) throws RemoteException, UiObjectNotFoundException, InterruptedException {
        if (afterMs > 0L) {
            // waitFor will not work while app is paused
            sleep(afterMs);
        }

        UiDevice device = UiDevice.getInstance(getInstrumentation());
        device.pressRecentApps();
        UiObject appBackground = device.findObject(new UiSelector().description(getString(R.string.app_name)));
        Thread.sleep(500);
        appBackground.click();
    }

    public static void clearAppState() {
        App.getDataManager().clear();
        clearPrefs();
    }

    @SuppressLint("ApplySharedPref")
    public static void clearPrefs() {
        File filesDir = getTargetContext().getFilesDir();
        if (filesDir != null) {
            File root = filesDir.getParentFile();
            String[] sharedPreferencesFileNames = new File(root, "shared_prefs").list();
            for (String fileName : sharedPreferencesFileNames) {
                getTargetContext().getSharedPreferences(fileName.replace(".xml", ""), Context.MODE_PRIVATE).edit().clear().commit();
            }
        }
    }

    public static void setAirplaneMode(boolean enabled) {
        if (DeviceInfoUtil.isAirplaneMode(App.getInstance()) == enabled) {
            return;
        }

        UiDevice device = UiDevice.getInstance(getInstrumentation());
        device.openQuickSettings();
        SearchBuilder result = search().descContains("Airplane mode");
        if (!result.exists(3000)) {
            // close quick settings
            device.pressBack();
            device.pressBack();
            throw new RuntimeException("Unable to find Airplane mode switch");
        }
        result.object().click();
        // close quick settings
        device.pressBack();
        device.pressBack();
    }

    public static void waitForInternet() {
        App.getInstance().getConnectionStatusManager().getStatusObservable()
                .filter(status -> status == ConnectionStatus.CONNECTED)
                .timeout(IDLE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .toBlocking().first();
    }

    /**
     * Use {@link #setAirplaneMode(boolean)} instead
     */
    @Deprecated
    public static void setWifiEnabled(boolean enabled) {
        Context context = getTargetContext().getApplicationContext();
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(enabled);
    }

    public static SearchBuilder search() {
        return search(null);
    }

    public static SearchBuilder search(@Nullable String message) {
        return new SearchBuilder(message);
    }

    /**
     * Using UIAutomator to match elements on screen.
     * Can be used as alternative to {@link MatcherUtils} methods, which are using Espresso.
     * Sometimes Espresso is not sufficient to find (and especially wait) UI elements across windows.
     */
    public static class SearchBuilder {

        private final Context context;

        private final String message;

        private BySelector selector;

        public SearchBuilder(@Nullable String m) {
            context = getInstrumentation().getTargetContext().getApplicationContext();
            message = m;
        }

        public SearchBuilder id(@IdRes int id) {
            String resource = context.getResources().getResourceName(id);
            if (selector == null) {
                selector = By.res(resource);
            } else {
                selector = selector.res(resource);
            }
            return this;
        }

        public SearchBuilder text(@StringRes int id) {
            return text(context.getString(id));
        }

        public SearchBuilder text(String text) {
            if (selector == null) {
                selector = By.text(text);
            } else {
                selector = selector.text(text);
            }
            return this;
        }

        public SearchBuilder textContains(String text) {
            if (selector == null) {
                selector = By.textContains(text);
            } else {
                selector = selector.textContains(text);
            }
            return this;
        }


        public SearchBuilder res(String res) {
            if (selector == null) {
                selector = By.res(res);
            } else {
                selector = selector.res(res);
            }
            return this;
        }

        public SearchBuilder pkg(String pkg) {
            if (selector == null) {
                selector = By.pkg(pkg);
            } else {
                selector = selector.pkg(pkg);
            }
            return this;
        }

        public SearchBuilder desc(String description) {
            if (selector == null) {
                selector = By.desc(description);
            } else {
                selector = selector.desc(description);
            }
            return this;
        }

        public SearchBuilder descContains(String substring) {
            if (selector == null) {
                selector = By.descContains(substring);
            } else {
                selector = selector.descContains(substring);
            }
            return this;
        }

        public void assertExist() {
            if (message != null) {
                assertTrue(message, exists());
            } else {
                assertTrue(exists());
            }
        }

        public void assertExist(long timeout) {
            if (message != null) {
                assertTrue(message, exists(timeout));
            } else {
                assertTrue(exists(timeout));
            }
        }

        public void assertNotExist() {
            if (message != null) {
                assertFalse(message, exists());
            } else {
                assertFalse(exists());
            }
        }

        public void assertNotExist(long timeout) {
            if (message != null) {
                assertTrue(message, notExist(timeout));
            } else {
                assertTrue(notExist(timeout));
            }
        }

        public void assertCount(Matcher<Integer> matcher) {
            if (!matcher.matches(count())) {
                fail();
            }
        }

        public int count() {
            UiDevice device = UiDevice.getInstance(getInstrumentation());
            return device.findObjects(selector).size();
        }

        public boolean exists() {
            UiDevice device = UiDevice.getInstance(getInstrumentation());
            return device.hasObject(selector);
        }

        public boolean exists(long timeout) {
            UiDevice device = UiDevice.getInstance(getInstrumentation());
            return device.wait(Until.hasObject(selector), timeout);
        }

        public boolean notExist(long timeout) {
            UiDevice device = UiDevice.getInstance(getInstrumentation());
            return device.wait(Until.gone(selector), timeout);
        }

        public UiObject2 object() {
            UiDevice device = UiDevice.getInstance(getInstrumentation());
            return device.findObject(selector);
        }

        public List<UiObject2> objects() {
            UiDevice device = UiDevice.getInstance(getInstrumentation());
            return device.findObjects(selector);
        }

        public void fail() {
            Assert.fail(message);
        }
    }

}
