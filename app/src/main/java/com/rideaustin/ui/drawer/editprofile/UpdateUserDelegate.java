package com.rideaustin.ui.drawer.editprofile;

import com.rideaustin.api.model.User;
import com.rideaustin.base.ProgressCallback;

import rx.Observer;
import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by hatak on 26.06.2017.
 */

public interface UpdateUserDelegate {

    Subscription updateUserOnServer(final User user, ProgressCallback callback, final Observer<Boolean> userUpdated);

    String getPhotoUrl();

    Subscription updatePhoto(String photoPath, ProgressCallback callback, Action1<String> onPhotoUpdated);

    boolean isNicknameEnabled();
}
