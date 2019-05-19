package com.rideaustin.api;

import android.text.TextUtils;
import android.util.Base64;

import com.rideaustin.App;

import static com.rideaustin.utils.CommonConstants.TOKEN_KEY;

/**
 * Created by v.garshyn on 24.06.16.
 * <p>
 * Sample class provided for debug mode
 */
public class SimpleAuthProvider implements BasicAuthProvider {

    private String authString;

    public SimpleAuthProvider() {
        authString = null;
    }

    @Override
    public String getAuthString() {
        if (TextUtils.isEmpty(authString)) {
            return App.getPrefs().getString(TOKEN_KEY, "");
        } else {
            return authString;
        }
    }

    @Override
    public void updateCredentials(String username, String password) {
        authString = encodeBasicCredentials(username, password);
        App.getPrefs().clearValue(TOKEN_KEY);
    }

    @Override
    public void updateToken(String token) {
        authString = token;
        App.getPrefs().clearValue(TOKEN_KEY);
    }

    /**
     * Encode Username and Password to Base64 auth String
     *
     * @param username
     * @param password
     * @return
     */
    private static String encodeBasicCredentials(String username, String password) {
        String credentials = username + ":" + password;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        return auth;
    }
}
