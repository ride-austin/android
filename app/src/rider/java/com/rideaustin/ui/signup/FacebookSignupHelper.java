package com.rideaustin.ui.signup;

import android.content.Intent;
import android.widget.Toast;

import com.rideaustin.App;
import com.rideaustin.api.model.User;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseActivity;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.models.UserRegistrationData;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.toast.RAToast;

import rx.Subscription;
import timber.log.Timber;

/**
 * Created by hatak on 27.06.2017.
 */

public class FacebookSignupHelper {

    public static Subscription doFacebookSignUp(BaseActivity activity, UserRegistrationData userData) {
        return App.getDataManager().signUpEmail(userData.getEmail(),
                userData.getSocialId(),
                userData.getPassword(),
                userData.getFirstName(),
                userData.getLastName(),
                userData.getPhoneNumber(), "", userData.getCityId())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<User>(activity) {
                    @Override
                    public void onNext(User user) {
                        super.onNext(user);
                        App.getDataManager().setCurrentUser(user);
                        activity.startActivity(new Intent(activity, CharityActivity.class));
                        try {
                            activity.finishAffinity();
                        } catch (Exception e) {
                            // https://issue-tracker.devfactory.com/browse/RA-8924
                            Timber.e(e, "Cannot finishAffinity");
                        }
                    }

                    @Override
                    public void onUnknownError(BaseApiException e) {
                        super.onUnknownError(e);
                        RAToast.showLong(e.getBody());
                    }
                });
    }

}
