package com.rideaustin.ui.drawer.refer;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.text.TextUtils;
import android.util.Patterns;

import com.rideaustin.App;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.schedulers.RxSchedulers;

import timber.log.Timber;

/**
 * Created by hatak on 02.11.16.
 */

public class ReferFriendEmailViewModel extends BaseObservable {

    private String email;
    private final ViewModelListener listener;


    public ReferFriendEmailViewModel(final ViewModelListener listener) {
        this.listener = listener;
    }

    @Bindable
    public String getEmail() {
        return email;
    }

    @Bindable
    public void setEmail(final String email) {
        this.email = email;
    }

    public void validateAndSend() {
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Timber.d("User has entered wrong email: %s", email);
            listener.onInputError();
            return;
        }

        int cityId = App.getConfigurationManager().getLastConfiguration().getCurrentCity().getCityId();

        listener.onStarted();
        App.getDataManager().getDriverService().getCurrentDriver()
                .map(Driver::getId)
                .flatMap(id -> App.getDataManager().getDriverService().referFriendByEmail(id, email, cityId))
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        super.onCompleted();
                        listener.onCompleted();
                    }

                    @Override
                    public void onError(final BaseApiException e) {
                        super.onError(e);
                        listener.onError();
                    }
                });
    }

    public interface ViewModelListener {
        void onInputError();

        void onCompleted();

        void onError();

        void onStarted();
    }
}
