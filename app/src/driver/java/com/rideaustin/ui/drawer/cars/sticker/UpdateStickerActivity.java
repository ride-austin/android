package com.rideaustin.ui.drawer.cars.sticker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.rideaustin.R;
import com.rideaustin.api.config.DriverRegistration;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.base.Transition;
import com.rideaustin.ui.documents.UploadDocumentsActivity;
import com.rideaustin.ui.common.TakePhotoFragment;

/**
 * Created by hatak on 2/6/17.
 */

public class UpdateStickerActivity extends UploadDocumentsActivity
        implements UpdateStickerFragmentCallback, TakePhotoFragment.TakePhotoListenerContainer {

    private static final String KEY_CAR = "KEY_CAR";
    private static final String KEY_REGISTRATION_CONFIGURATION = "KEY_REGISTRATION_CONFIGURATION";
    private TakePhotoFragment.TakePhotoListener takePhotoListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        replaceFragment(UpdateStickerFragment.newInstance(), R.id.content_frame, false, Transition.NONE);
    }

    @Override
    public Car getCar() {
        return (Car) getIntent().getSerializableExtra(KEY_CAR);
    }

    @Override
    public DriverRegistration getRegistrationConfiguration() {
        return (DriverRegistration) getIntent().getSerializableExtra(KEY_REGISTRATION_CONFIGURATION);
    }

    @Override
    protected String getTitleString() {
        return getRegistrationConfiguration().getInspectionSticker().getHeader();
    }

    public static Intent newIntent(Context context, DriverRegistration registrationConfiguration, Car car) {
        return new Intent(context, UpdateStickerActivity.class)
                .putExtra(KEY_CAR, car)
                .putExtra(KEY_REGISTRATION_CONFIGURATION, registrationConfiguration);
    }

    @Override
    public TakePhotoFragment.TakePhotoListener getTakePhotoListener() {
        return takePhotoListener;
    }

    @Override
    public void setTakePhotoListener(TakePhotoFragment.TakePhotoListener listener) {
        takePhotoListener = listener;
    }

    @Override
    public void onCompleted() {
        finish();
    }

}
