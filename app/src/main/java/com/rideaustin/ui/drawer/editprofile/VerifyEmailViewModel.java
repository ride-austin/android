package com.rideaustin.ui.drawer.editprofile;

import android.databinding.ObservableField;

import com.rideaustin.App;

/**
 * Created on 15/11/2017
 *
 * @author sdelaysam
 */

public class VerifyEmailViewModel {

    public final ObservableField<String> email = new ObservableField<>();

    VerifyEmailViewModel() {
        if (!App.getDataManager().isLoggedIn()) {
            // app is going to be redirected to login
            return;
        }
        email.set(App.getDataManager().getCurrentUser().getEmail());
    }
}
