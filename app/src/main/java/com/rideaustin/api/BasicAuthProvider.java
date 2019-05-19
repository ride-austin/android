package com.rideaustin.api;

import android.support.annotation.NonNull;

/**
 * Created by v.garshyn on 24.06.16.
 *
 * @author vharshyn
 *         <p>
 *         Instance of this interface must provide encoded auth string
 */
public interface BasicAuthProvider {
    /**
     * @return encoded Base64 String
     */
    @NonNull
    String getAuthString();

    /**
     * Update credentials with new valid username and password
     *
     * @param username
     * @param password
     */
    void updateCredentials(String username, String password);

    @Deprecated
    void updateToken(String token);
}
