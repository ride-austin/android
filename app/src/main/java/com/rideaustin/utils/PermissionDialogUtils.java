package com.rideaustin.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rideaustin.R;
import com.rideaustin.ui.utils.Fonts;
import com.rideaustin.utils.toast.RAToast;

import java.util.HashSet;
import java.util.Set;

import timber.log.Timber;

import static com.rideaustin.utils.CommonConstants.UNEXPECTED_STATE_KEY;

/**
 * Created on 17/11/2017
 *
 * @author sdelaysam
 */

public class PermissionDialogUtils {

    private PermissionDialogUtils() {}

    public static void showDeniedPermissions(Activity activity, String... permissions) {
        LayoutInflater inflater = activity.getLayoutInflater();
        LinearLayout layout = (LinearLayout) inflater
                .inflate(R.layout.view_permission_layout, null, false);

        Set<String> names = new HashSet<>();
        for (String permission : permissions) {
            if (PermissionUtils.isGranted(activity, permission)) {
                continue;
            }
            PermissionViewData data = getViewData(permission);
            if (data.nameRes == -1 || data.iconRes == -1) {
                continue;
            }
            String name = activity.getString(data.nameRes);
            if (names.contains(name)) {
                continue;
            }

            names.add(name);
            ViewGroup item = (ViewGroup) inflater
                    .inflate(R.layout.view_permission_item, null, false);

            ImageView imageView = item.findViewById(R.id.image);
            TextView textView = item.findViewById(R.id.name);
            imageView.setImageResource(data.iconRes);
            textView.setText(name);
            layout.addView(item);
        }

        if (names.isEmpty()) {
            Exception e = new Exception(UNEXPECTED_STATE_KEY);
            Timber.e(e, "No permission to allow: " + permissions);
            RAToast.showShort(R.string.error_unknown);
            return;
        }

        TextView header = layout.findViewById(R.id.header);
        if (permissions.length > 1) {
            header.setText(R.string.permissions_header);
        } else {
            header.setText(R.string.permission_header);
        }

        new MaterialDialog.Builder(activity)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_REGULAR_OTF)
                .customView(layout, true)
                .onPositive((dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                    intent.setData(uri);
                    activity.startActivity(intent);
                })
                .positiveText(R.string.btn_settings)
                .negativeText(R.string.btn_cancel)
                .show();
    }

    /**
     * {@link android.content.pm.PermissionInfo#loadLabel(PackageManager)} could be used, but:
     * - need to trim description
     * - description is localized (app is not)
     * - icons not provided (need to provide manually anyway)
     */
    private static PermissionViewData getViewData(String permission) {
        PermissionViewData item = new PermissionViewData();
        if (permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                || permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
            item.nameRes = R.string.permission_location;
            item.iconRes = R.drawable.ic_location_on_black_24px;
        } else if (permission.equals(Manifest.permission.CAMERA)) {
            item.nameRes = R.string.permission_camera;
            item.iconRes = R.drawable.ic_camera_alt_black_24px;
        } else if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)
                || permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            item.nameRes = R.string.permission_storage;
            item.iconRes = R.drawable.ic_folder_black_24px;
        } else if (permission.equals(Manifest.permission.CALL_PHONE)) {
            item.nameRes = R.string.permission_phone;
            item.iconRes = R.drawable.ic_phone_black_24px;
        } else if (permission.equals(Manifest.permission.READ_CONTACTS)) {
            item.nameRes = R.string.permission_contacts;
            item.iconRes = R.drawable.ic_contacts_black_24px;
        }
        return item;
    }

    private static class PermissionViewData {
        @StringRes int nameRes = -1;
        @DrawableRes int iconRes = -1;
    }
}
