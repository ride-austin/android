package com.rideaustin.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.rideaustin.App;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by supreethks on 09/09/16.
 */
public class DeviceInfoUtil {

    public static String getOsVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getDeviceBuild() {
        String brand = Build.MANUFACTURER;
        String model = Build.MODEL;
        return String.format(Locale.US,"%s Build/%s", brand, model);
    }

    private static String deviceId;

    @SuppressLint("HardwareIds")
    public static String getUniqueDeviceId() {
        if (deviceId == null) {
            deviceId = Settings.Secure.getString(App.getInstance().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            if (TextUtils.isEmpty(deviceId)) {
                // https://www.pocketmagic.net/android-unique-device-id/#.UsMCatIW2vE
                // combine MAC address hash and device info
                String macAddress = null;
                WifiManager wm = (WifiManager)App.getInstance().getApplicationContext()
                        .getSystemService(Context.WIFI_SERVICE);
                if (wm != null) {
                    macAddress = wm.getConnectionInfo().getMacAddress();
                }
                if (macAddress != null) {
                    macAddress = String.valueOf(macAddress.hashCode());
                } else {
                    macAddress = "_";
                }
                deviceId = macAddress +
                        Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                        Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                        Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                        Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                        Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                        Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                        Build.USER.length() % 10;
            }
        }
        return deviceId;
    }

    public static boolean isSmallScreen() {
        DisplayMetrics displayMetrics = App.getInstance().getResources().getDisplayMetrics();
        float widthDp = displayMetrics.widthPixels / displayMetrics.density;
        return widthDp < 360;
    }

    public static boolean isEmulator() {
        return Build.FINGERPRINT.contains("generic");
    }

    private static AtomicBoolean isEspresso;

    public static synchronized boolean isEspresso() {
        // https://stackoverflow.com/a/33402055
        if (isEspresso == null) {
            try {
                Class.forName("android.support.test.espresso.Espresso");
                isEspresso = new AtomicBoolean(true);
            } catch (ClassNotFoundException e) {
                isEspresso = new AtomicBoolean(false);
            }
        }
        return isEspresso.get();
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isAirplaneMode(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }

}
