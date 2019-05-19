package com.rideaustin.utils;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.rideaustin.R;
import com.rideaustin.utils.toast.RAToast;

import java.util.ArrayList;

import timber.log.Timber;


public class FacebookHelper {

    public static void refreshTokenAndLogin(@NonNull final Fragment hostFragment, @NonNull final ArrayList<String> permissions) {
        AccessToken.refreshCurrentAccessTokenAsync(new AccessToken.AccessTokenRefreshCallback() {
            @Override
            public void OnTokenRefreshed(final AccessToken accessToken) {
                login(hostFragment, permissions);
            }

            @Override
            public void OnTokenRefreshFailed(final FacebookException e) {
                if (!NetworkHelper.isNetworkAvailable())
                    RAToast.show(R.string.network_error, Toast.LENGTH_SHORT);
                else {
                    Timber.d("facebook access token refresh failed, probably user revoked permissions for application");
                    login(hostFragment, permissions);
                }
            }
        });
    }

    public static GraphRequest getMeRequest(AccessToken accessToken, GraphRequest.GraphJSONObjectCallback callback) {
        return meRequestCreator.create(accessToken, callback);
    }

    private static void login(@NonNull final Fragment hostFragment, @NonNull final ArrayList<String> permissions) {
        // RA-8905: check fragment is still attached to activity
        if (hostFragment.isAdded()) {
            LoginManager.getInstance().logInWithReadPermissions(hostFragment, permissions);
        }
    }

    @VisibleForTesting
    public static MeRequestCreator meRequestCreator = GraphRequest::newMeRequest;

    @VisibleForTesting
    public interface MeRequestCreator {
        GraphRequest create(AccessToken accessToken, GraphRequest.GraphJSONObjectCallback callback);
    }

}
