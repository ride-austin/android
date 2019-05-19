package com.rideaustin.ui.drawer.cars.insurance;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.rideaustin.R;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.base.Transition;
import com.rideaustin.ui.documents.UploadDocumentsActivity;
import com.rideaustin.ui.drawer.cars.UpdateFragmentCallback;
import com.rideaustin.ui.common.TakePhotoFragment;

public class UpdateInsuranceActivity extends UploadDocumentsActivity
        implements UpdateFragmentCallback, TakePhotoFragment.TakePhotoListenerContainer {

    private static final String KEY_CAR = "KEY_CAR";
    private TakePhotoFragment.TakePhotoListener takePhotoListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        replaceFragment(UpdateInsuranceFragment.newInstance(), R.id.content_frame, false, Transition.NONE);
    }

    @Override
    public Car getCar() {
        Car car = (Car) getIntent().getSerializableExtra(KEY_CAR);
        return car;
    }

    @Override
    protected String getTitleString() {
        return getString(R.string.title_insurance);
    }

    public static Intent newIntent(Context context, Car car) {
        return new Intent(context, UpdateInsuranceActivity.class).putExtra(KEY_CAR, car);
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
