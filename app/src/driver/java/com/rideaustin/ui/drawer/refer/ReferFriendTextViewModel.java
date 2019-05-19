package com.rideaustin.ui.drawer.refer;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.text.TextUtils;

import com.rideaustin.App;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.utils.phone.PhoneInputUtil;

import timber.log.Timber;

/**
 * Created by hatak on 02.11.16.
 */

public class ReferFriendTextViewModel extends BaseObservable {

    private String phoneNumber;
    private String phonePrefix;
    private final ViewModelListener listener;
    private final PhoneInputUtil phoneInputUtil;


    public ReferFriendTextViewModel(final ViewModelListener listener, final PhoneInputUtil phoneInputUtil) {
        this.listener = listener;
        this.phoneInputUtil = phoneInputUtil;
    }


    public void validateAndSend() {
        if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length() < 10) {
            Timber.w("::validateAndSend:: short phone number");
            listener.onInputError();
            return;
        }

        String formattedPhone = phoneInputUtil.validate();
        if (formattedPhone == null) {
            Timber.w("::validateAndSend:: formattedPhone == null");
            listener.onInputError();
            return;
        }

        int cityId = App.getConfigurationManager().getLastConfiguration().getCurrentCity().getCityId();

        listener.onStarted();
        App.getDataManager()
                .getDriverService()
                .getCurrentDriver()
                .map(Driver::getId)
                .flatMap(id -> App.getDataManager().getDriverService().referFriendBySMS(id, formattedPhone, cityId))
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

    @Bindable
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Bindable
    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Bindable
    public String getPhonePrefix() {
        return phonePrefix;
    }

    @Bindable
    public void setPhonePrefix(final String phonePrefix) {
        this.phonePrefix = phonePrefix;
    }

    public interface ViewModelListener {
        void onInputError();

        void onCompleted();

        void onError();

        void onStarted();
    }
}
