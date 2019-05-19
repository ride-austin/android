package com.rideaustin.ui.drawer.editprofile;

import com.rideaustin.App;
import com.rideaustin.api.model.User;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.ProgressCallback;
import com.rideaustin.schedulers.RxSchedulers;

import rx.Observer;
import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by hatak on 26.06.2017.
 */

public class UpdateDriverDelegate implements UpdateUserDelegate {
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
        return App.getDataManager().getDriver().map(Driver::getPhotoUrl).orElse("");
    }

    @Override
    public Subscription updatePhoto(String photoPath, ProgressCallback callback, Action1<String> onPhotoUpdated) {
        return App.getDataManager().postDriverPhoto(App.getDataManager().getDriver().get().getId(), photoPath)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<Driver>(callback) {
                    @Override
                    public void onNext(Driver driver) {
                        App.getDataManager().setCurrentDriver(driver);
                        onPhotoUpdated.call(driver.getPhotoUrl());
                    }
                });
    }

    @Override
    public boolean isNicknameEnabled() {
        return true;
    }
}
