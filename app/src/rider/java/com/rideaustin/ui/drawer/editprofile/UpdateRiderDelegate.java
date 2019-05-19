package com.rideaustin.ui.drawer.editprofile;

import com.rideaustin.App;
import com.rideaustin.api.model.User;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.base.ProgressCallback;
import com.rideaustin.schedulers.RxSchedulers;

import rx.Observer;
import rx.Subscription;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Created by hatak on 26.06.2017.
 */

public class UpdateRiderDelegate implements UpdateUserDelegate {
    @Override
    public Subscription updateUserOnServer(User user, ProgressCallback callback, Observer<Boolean> userUpdated) {
        return App.getDataManager().updateUser(user)
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<User>(callback) {
                    @Override
                    public void onCompleted() {
                        super.onCompleted();
                        userUpdated.onNext(Boolean.TRUE);
                    }
                });
    }

    @Override
    public String getPhotoUrl() {
        return App.getDataManager().getCurrentUser().getPhotoUrl();
    }

    @Override
    public Subscription updatePhoto(String photoPath, ProgressCallback callback, Action1<String> onPhotoUpdated) {
        return App.getDataManager().postUsersPhoto(photoPath)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<User>(callback) {
                    @Override
                    public void onNext(User user) {
                        App.getDataManager().setCurrentUser(user);
                        onPhotoUpdated.call(user.getPhotoUrl());
                    }
                });
    }

    @Override
    public boolean isNicknameEnabled() {
        return false;
    }
}
