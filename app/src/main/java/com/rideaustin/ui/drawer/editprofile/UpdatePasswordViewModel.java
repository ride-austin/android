package com.rideaustin.ui.drawer.editprofile;

import android.databinding.BaseObservable;
import android.databinding.ObservableField;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.Md5Helper;
import com.rideaustin.utils.SingleSubject;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by rideclientandroid on 04.10.2016.
 */
public class UpdatePasswordViewModel extends BaseObservable {

    private PublishSubject<Integer> validationErrorsSubject = PublishSubject.create();
    private SingleSubject<Boolean> passwordSubmittedSubject = SingleSubject.create();

    public final ObservableField<String> password = new ObservableField<>("");
    public final ObservableField<String> confirmPassword = new ObservableField<>("");

    boolean validateForm() {
        if (password.get().length() < Constants.MIN_PASSWORD_LENGTH) {
            validationErrorsSubject.onNext(R.string.text_choose_password_alert);
            return false;
        } else if (!password.get().equals(confirmPassword.get())) {
            validationErrorsSubject.onNext(R.string.text_password_mismatch);
            return false;
        } else if (confirmPassword.get().length() < password.get().length()) {
            validationErrorsSubject.onNext(R.string.text_password_mismatch);
            return false;
        } else if (confirmPassword.get().length() < Constants.MIN_PASSWORD_LENGTH) {
            validationErrorsSubject.onNext(R.string.text_choose_password_alert);
            return false;
        }
        return true;
    }

    Observable<Integer> subscribeValidationErrors() {
        return validationErrorsSubject
                .asObservable()
                .onBackpressureLatest();
    }

    Observable<Boolean> subscribePasswordSubmitResults() {
        return passwordSubmittedSubject
                .asObservable();
    }

    void submitPassword(BaseActivityCallback callback) {
        if (validateForm()) {
            App.getDataManager().getAuthService()
                    .setNewPassword(Md5Helper.calculateMd5Hash(App.getDataManager().getCurrentUser().getEmail(), password.get().trim()))
                    .subscribeOn(RxSchedulers.network())
                    .subscribe(new ApiSubscriber2<Void>(callback) {
                        @Override
                        public void onNext(Void aVoid) {
                            passwordSubmittedSubject.onNext(Boolean.TRUE);
                        }
                    });
        }
    }
}
