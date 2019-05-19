package com.rideaustin.utils;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.rideaustin.App;
import com.rideaustin.BuildConfig;
import com.rideaustin.api.config.ConfigAppInfoResponse;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

import static com.rideaustin.utils.Constants.GOOGLE_PLAY_HTTP_URL;
import static com.rideaustin.utils.Constants.MARKET_DETAILS_ID;

/**
 * Created by supreethks on 09/09/16.
 */
public class AppInfoUtil {

    private static final String PLATFORM = "Android";
    private static final String USER_AGENT = "User-Agent";
    private static final String USER_PLATFORM = "User-Platform";
    private static final String USER_DEVICE = "User-Device";
    private static final String USER_DEVICE_ID = "User-Device-Id";

    /**
     * Sample outputs:
     * 1. {User-Agent=RideAustinRider_Android_beta-build#Android Studio (13), User-Platform=Linux; U; Android 6.0, User-Device-Id=8f6a377a0cf2c73f, User-Device=motorola Build/XT1092}
     * <p>
     * 2. On a rooted device:
     * {User-Agent=RideAustinRider_Android_beta-build#Android Studio (13), User-Platform=Linux; U; Android 5.1.1, User-Device-Id=3f5a71bde6b507d1, User-Device=samsung Build/SM-G935F}
     *
     * @return
     */
    @NonNull
    public static Map<String, String> getDeviceInfo() {
        //BuildConfig.
        Map<String, String> map = new HashMap<>();
        if (isProd()) {
            map.put(USER_AGENT,
                    String.format(Locale.US, "%s_%s_%s (%s)",
                            BuildConfig.USER_AGENT,
                            PLATFORM,
                            getAppVersionName(),
                            getAppVersionCode()));
        } else {
            map.put(USER_AGENT,
                    String.format(Locale.US, "%s_%s_%s",
                            BuildConfig.USER_AGENT,
                            PLATFORM,
                            getAppVersionName()));
        }
        map.put(USER_PLATFORM, String.format(Locale.US, "Linux; U; Android %s",
                DeviceInfoUtil.getOsVersion()));
        map.put(USER_DEVICE, DeviceInfoUtil.getDeviceBuild());
        map.put(USER_DEVICE_ID, DeviceInfoUtil.getUniqueDeviceId());
        return map;
    }

    public static boolean isProd() {
        return BuildConfig.FLAVOR.contains(Constants.ENV_PROD);
    }

    public static String getAppVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    public static int getAppVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    public static void openPlayStore() {
        String packageName = App.getInstance().getPackageName();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_DETAILS_ID + packageName));
        if (intent.resolveActivity(App.getInstance().getPackageManager()) == null) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_PLAY_HTTP_URL + packageName));
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            App.getInstance().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Timber.w(e, "Cannot open activity: " + intent);
            //Some says in China it is common not to have any of apps above. So lets not crash for Chinese visitors.
        }
    }

    public static void openPlayStoreForRating() {
        String packageName = App.getInstance().getPackageName();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_DETAILS_ID + packageName));
        if (intent.resolveActivity(App.getInstance().getPackageManager()) == null) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_PLAY_HTTP_URL + packageName));
        }
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        } else {
            //noinspection deprecation
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        }
        intent.setFlags(flags);
        try {
            App.getInstance().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Timber.w(e, "Cannot open activity: " + intent);
            //Some says in China it is common not to have any of apps above. So lets not crash for Chinese visitors.
        }
    }

    /**
     * @param input
     * @return an array of {@link Constants#VERSION_COUNT} ints, at worst case it returns 0.0.0
     */
    public static int[] extractCode(@Nullable final String input) {
        int[] version = new int[Constants.VERSION_COUNT];
        if (!TextUtils.isEmpty(input)) {
            final Pattern pattern = Pattern.compile(Constants.VERSION_PATTERN);
            final Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                try {
                    String versionString = matcher.group(0);
                    String[] versionCodes = versionString.split(Constants.VERSION_DIVIDER);
                    for (int i = 0; i < version.length; i++) {
                        if (versionCodes.length >= i) {
                            version[i] = Integer.valueOf(versionCodes[i]);
                        }
                    }
                } catch (NumberFormatException | NullPointerException | ArrayIndexOutOfBoundsException e) {
                    Timber.e(e, "Version pattern does not match algorithm, pattern is: %s and input is: %s", Constants.VERSION_PATTERN, input, e);
                }
            }
        }
        return version;
    }


    public static boolean isGreater(int[] first, int[] second) {
        for (int i = 0; i < first.length; i++) {
            if (first[i] > second[i]) {
                return true;
            } else if (first[i] < second[i]) {
                return false;
            }
        }
        return false;
    }

    /**
     * @param response
     * @return true if the latest version is mandatory and update is required.
     */
    public static boolean isMandatoryRequired(@Nullable ConfigAppInfoResponse response) {
        if (response == null) {
            Timber.w("This is said that will never happen in prod; ConfigAppInfoResponse is null");
            return false;
        }
        if (!response.isMandatoryUpgrade()) {
            return false;
        }
        int[] appVersion = AppInfoUtil.extractCode(getAppVersionName());
        int[] serverVersion = AppInfoUtil.extractCode(response.getVersion());
        return AppInfoUtil.isGreater(serverVersion, appVersion);
    }

    public static boolean canShowUpdate() {
        return App.getPrefs().hasRideId();
    }
}