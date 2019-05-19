package com.rideaustin.ui.base;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.WindowManager;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.base.BaseActivity;
import com.rideaustin.service.NavigatorService;
import com.rideaustin.utils.DeviceInfoUtil;
import com.rideaustin.utils.NavigatorShareUtils;

import timber.log.Timber;

/**
 * Created by ridedriverandroid on 23.08.2016.
 */
public abstract class FloatNavigatorBaseActivity extends BaseActivity implements FloatNavigatorCallback {

    private static final int REQUEST_SYSTEM_ALERT_WINDOW = 101;

    private boolean isBound = false;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        if (!isSystemAlertPermissionGranted(this)) {
            requestSystemAlertPermission(this, REQUEST_SYSTEM_ALERT_WINDOW);
        } else {
            bindService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isBound) {
            // returned from permission screen
            bindService();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
        }
    }

    @Override
    public void navigateTo(LatLng navigateTo) {
        Timber.d("::navigateTo::" + "navigateTo = [" + navigateTo + "]");
        NavigatorShareUtils.share(FloatNavigatorBaseActivity.this, navigateTo,
                preference -> App.getPrefs().setDriverNavigationActivity(App.getDataManager().getCurrentDriver(), preference),
                App.getPrefs().getDriverNavigationActivity(App.getDataManager().getCurrentDriver()));
    }

    @Override
    public void navigateTo(String place) {
        Timber.d("::navigateTo::" + "place = [" + place + "]");
        NavigatorShareUtils.share(FloatNavigatorBaseActivity.this, place,
                activityInfo -> App.getPrefs().setDriverNavigationActivity(App.getDataManager().getCurrentDriver(), activityInfo),
                App.getPrefs().getDriverNavigationActivity(App.getDataManager().getCurrentDriver()));
    }

    private void bindService() {
        if (DeviceInfoUtil.isEspresso()) return;

        if (!isBound && isSystemAlertPermissionGranted(this)) {
            Intent intent = new Intent(this, NavigatorService.class);
            bindService(intent, serviceConnection, Context.BIND_ADJUST_WITH_ACTIVITY
                    | Context.BIND_AUTO_CREATE
                    | Context.BIND_IMPORTANT);
        }
    }

    public static void requestSystemAlertPermission(Activity context, int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;

        if (DeviceInfoUtil.isEspresso()) return;

        final String packageName = context.getPackageName();
        final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName));

        context.startActivityForResult(intent, requestCode);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean isSystemAlertPermissionGranted(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
    }
}
