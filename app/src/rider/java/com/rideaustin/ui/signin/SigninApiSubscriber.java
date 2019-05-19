package com.rideaustin.ui.signin;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.ConfigAppInfoResponse;
import com.rideaustin.api.model.User;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.ui.drawer.editprofile.EditProfileActivity;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.ui.utils.ProfileValidator;
import com.rideaustin.utils.AppInfoUtil;
import com.rideaustin.utils.ServerMessageHelper;
import com.rideaustin.utils.toast.RAToast;

import java.util.Calendar;

import timber.log.Timber;

/**
 * Created by supreethks on 09/10/16.
 */

class SigninApiSubscriber extends ApiSubscriber2<User> {

    private static final int ALLOWED_TIME_WINDOW = 12;
    private final boolean isManualSignIn;
    private Activity activity;

    SigninApiSubscriber(Activity activity, boolean isManualSignIn) {
        super((BaseActivityCallback) activity, !isManualSignIn);
        this.activity = activity;
        this.isManualSignIn = isManualSignIn;
    }

    @Override
    public void onStart() {
        Timber.d("request start");
        if (getCallback() != null) {
            if (isManualSignIn) {
                getCallback().showProgress();
            } else {
                getCallback().showLoadingWheel();
            }
        }
    }

    @Override
    public void onNext(User user) {
        super.onNext(user);
        ConfigAppInfoResponse appInfoResponse = App.getDataManager().getConfigAppInfoResponse();
        Timber.d("::onNext:: %s", appInfoResponse);

        String appVersionName = AppInfoUtil.getAppVersionName();
        Timber.d("::onNext:: appVersionName : %s", appVersionName);
        if (appInfoResponse != null) {

            int[] appVersion = AppInfoUtil.extractCode(appVersionName);
            int[] serverVersion = AppInfoUtil.extractCode(appInfoResponse.getVersion());

            if (AppInfoUtil.isGreater(serverVersion, appVersion)) {

                if (!shouldShowUpdateDialog(appInfoResponse)) {
                    Timber.d("Time window for update has not elapsed, not showing dialog");
                    navigateToDashboard();
                    return;
                }

                CommonMaterialDialogCreator.createOptionalUpdateDialog(activity, (dialog, which) -> {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    navigateToDashboard();
                });

                if (!appInfoResponse.isMandatoryUpgrade()) {
                    App.getPrefs().setAppUpdateShownDate(Calendar.getInstance());
                }
            } else {
                navigateToDashboard();
            }
        } else {
            navigateToDashboard();
        }
    }

    private void navigateToDashboard() {
        Timber.d("::navigateToDashboard::");
        if (isUserDataValid()) {
            Intent intent = new Intent(activity, NavigationDrawerActivity.class);
            intent.putExtras(activity.getIntent());
            activity.startActivity(intent);
        } else {
            activity.startActivity(new Intent(activity, EditProfileActivity.class));
        }
        if (isManualSignIn) {
            activity.finishAffinity();
        } else {
            activity.finish();
        }
    }

    private boolean shouldShowUpdateDialog(ConfigAppInfoResponse appInfoResponse) {
        if (appInfoResponse.isMandatoryUpgrade()) {
            return true;
        }
        Calendar appUpdateShownDate = App.getPrefs().getAppUpdateShownDate();
        appUpdateShownDate.add(Calendar.HOUR, ALLOWED_TIME_WINDOW);
        return !appInfoResponse.isMandatoryUpgrade()
                && Calendar.getInstance().after(appUpdateShownDate);
    }

    @Override
    public void onUnauthorized(BaseApiException e) {
        if (isManualSignIn) {
            onAnyError(e);
        } else {
            super.onUnauthorized(e);
        }
    }

    @Override
    public void onAnyError(BaseApiException e) {
        super.onAnyError(e);
        if (isManualSignIn) {
            // RA-9321: show server message on error
            String message = ServerMessageHelper.getErrorMessage(App.getInstance(), e);
            try {
                MaterialDialogCreator.createErrorDialog(App.getInstance().getString(R.string.title_sign_in_failed), message, (AppCompatActivity) activity);
            } catch (Exception error) {
                Timber.e(error, "It seems we cannot create dialog here, activity: " + activity);
                RAToast.show(message, Toast.LENGTH_SHORT);
            }
        }
        if (activity instanceof SplashView) {
            try {
                ((SplashView) activity).onSignInError();
            } catch (Exception error) {
                // activity lifecycle-based error
                RAToast.show(e.getMessage(), Toast.LENGTH_SHORT);
            }
        }
    }

    private boolean isUserDataValid() {
        User user = App.getDataManager().getCurrentUser();
        return ProfileValidator.validateUser(user, activity);
    }
}
