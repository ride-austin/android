package com.rideaustin.manager.notification.firebase;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.rideaustin.App;
import com.rideaustin.CurrentAvatarType;
import com.rideaustin.api.model.AvatarType;
import com.rideaustin.schedulers.RxSchedulers;

import timber.log.Timber;

/**
 * Created by hatak on 30.11.16.
 */

public class RideFirebaseInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        sendRegistrationToServer(refreshedToken, CurrentAvatarType.getAvatarType());
        Timber.d("### token refreshed: " + refreshedToken);
    }

    private void sendRegistrationToServer(final String token, final AvatarType avatarType) {
        App.getDataManager().sendTokenToServer(token, avatarType)
                .subscribeOn(RxSchedulers.network())
                .subscribe(result -> {
                    Timber.d("### token updated on server");
                }, throwable -> {
                    Timber.d("### token update on server failed");
                });
    }


}
