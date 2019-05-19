package com.rideaustin.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by vokol on 26.07.2016.
 */
public class PermissionUtils {

    private PermissionUtils() {}

    /**
     * Returns whether both ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION granted by user
     * @param context context for permissions
     * @return <code>true</code> if permissions granted
     */
    public static boolean isLocationPermissionGranted(Context context) {
        return isGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION) && isGranted(context, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    /**
     * Returns whether both ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION are not granted
     * and user selected that app should never ask them again.
     * @param activity activity for permissions
     * @return <code>true</code> if permissions forbidden
     */
    public static boolean isLocationPermissionForbidden(Activity activity) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !isLocationPermissionGranted(activity)
                && !activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
                && !activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    /**
     * Check that we should show request permission rationale for any permission in given array
     * @param activity Activity to perform this check
     * @param permissions Array of permission names
     * @return true if any permission needs rationale, otherwise false
     */
    public static boolean shouldShowRequestPermissionsRationale(Activity activity, String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static void checkDeniedPermissions(Activity activity, String... permissions) {
        if (!shouldShowRequestPermissionsRationale(activity, permissions)) {
            // user denied permanently but tries to access the feature
            PermissionDialogUtils.showDeniedPermissions(activity, permissions);
        }
    }
}

