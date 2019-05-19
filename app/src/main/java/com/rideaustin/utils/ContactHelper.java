package com.rideaustin.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.utils.toast.RAToast;
import com.tbruyelle.rxpermissions.RxPermissions;

import timber.log.Timber;

/**
 * Created by hatak on 06.03.2017.
 */

public class ContactHelper {

    private static void sendSms(final Activity activity, final String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.fromParts(App.getInstance().getString(R.string.scheme_sms), phoneNumber, null));
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        } else {
            final String message = App.getInstance().getString(R.string.cant_get_app_to_sms) + "\n" + App.getInstance().getString(R.string.users_number) + phoneNumber;
            MaterialDialogCreator.createSimpleErrorDialog(message, activity);
        }
    }

    private static void makeCall(final Activity activity, final String phonePhone) {
        String permission = Manifest.permission.CALL_PHONE;
        new RxPermissions(activity)
                .request(permission)
                .subscribe(granted -> {
                    if (granted) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(App.getInstance().getString(R.string.scheme_telephone) + phonePhone));
                        if (intent.resolveActivity(activity.getPackageManager()) != null) {
                            activity.startActivity(intent);
                        } else {
                            final String message = App.getInstance().getString(R.string.cant_find_app_to_call) + "\n" + App.getInstance().getString(R.string.other_side_number) + phonePhone;
                            MaterialDialogCreator.createSimpleErrorDialog(message, activity);
                        }
                    } else {
                        PermissionUtils.checkDeniedPermissions(activity, permission);
                    }
                }, throwable -> {
                    Timber.e(throwable);
                    RAToast.showShort(R.string.error_unknown);
                });
    }

    private static void contactCall(final Activity activity, final boolean isMaskingEnabled, final String phoneNumber) {
        if (isMaskingEnabled) {
            final GlobalConfig lastConfiguration = App.getConfigurationManager().getLastConfiguration();
            makeCall(activity, lastConfiguration.getDirectConnectPhone());
        } else {
            makeCall(activity, phoneNumber);
        }
    }

    private static void contactSms(final Activity activity, final boolean isMaskingEnabled, final String phoneNumber) {
        if (isMaskingEnabled) {
            final GlobalConfig lastConfiguration = App.getConfigurationManager().getLastConfiguration();
            sendSms(activity, lastConfiguration.getDirectConnectPhone());
        } else {
            sendSms(activity, phoneNumber);
        }
    }

    public static void call(final Activity activity, @Nullable String phoneNumber) {
        if (!TextUtils.isEmpty(phoneNumber)) {
            contactCall(activity, App.getConfigurationManager().getLastConfiguration().isSmsMaskingEnabled(), phoneNumber);
        } else {
            RAToast.show(R.string.has_no_phone_number, Toast.LENGTH_LONG);
        }
    }

    public static void sms(final Activity activity, @Nullable String phoneNumber) {
        if (!TextUtils.isEmpty(phoneNumber)) {
            contactSms(activity, App.getConfigurationManager().getLastConfiguration().isSmsMaskingEnabled(), phoneNumber);
        } else {
            RAToast.show(R.string.has_no_phone_number, Toast.LENGTH_LONG);
        }
    }

}
