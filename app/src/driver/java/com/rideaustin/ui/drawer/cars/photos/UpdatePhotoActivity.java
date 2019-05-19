package com.rideaustin.ui.drawer.cars.photos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.rideaustin.R;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.base.Transition;
import com.rideaustin.ui.documents.UploadDocumentsActivity;
import com.rideaustin.ui.common.TakePhotoFragment;
import com.rideaustin.utils.Constants;

/**
 * Created by crossover on 08/02/2017.
 */

public class UpdatePhotoActivity extends UploadDocumentsActivity implements UpdatePhotoFragmentCallback {

    private static final String KEY_CAR = "KEY_CAR";
    private static final String PHOTO_TYPE_KEY = "PHOTO_TYPE_KEY";
    private TakePhotoFragment.TakePhotoListener listener;

    @Override
    protected String getTitleString() {
        return getString(R.string.title_driver_vehicle_information);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        replaceFragment(UpdatePhotoFragment.newInstance(), R.id.content_frame, false, Transition.NONE);
    }

    @Override
    @Constants.CarPhotoType
    public String getCarPhotoType() {
        @Constants.CarPhotoType
        final String carPhotoType = getIntent().getStringExtra(PHOTO_TYPE_KEY);
        return carPhotoType;
    }

    @Override
    public void onCompleted() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public Car getCar() {
        return (Car) getIntent().getSerializableExtra(KEY_CAR);
    }


    public static Intent newInstance(Context context, Car car, String carPhotoType) {
        return new Intent(context, UpdatePhotoActivity.class).putExtra(KEY_CAR, car).putExtra(PHOTO_TYPE_KEY, carPhotoType);
    }

    @Override
    public TakePhotoFragment.TakePhotoListener getTakePhotoListener() {
        return listener;
    }

    @Override
    public void setTakePhotoListener(TakePhotoFragment.TakePhotoListener listener) {
        this.listener = listener;
    }
}
