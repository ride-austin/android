package com.rideaustin.ui.drawer.documents.license;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.rideaustin.R;
import com.rideaustin.base.Transition;
import com.rideaustin.ui.documents.UploadDocumentsActivity;
import com.rideaustin.ui.common.TakePhotoFragment;

public class UpdateLicenseActivity extends UploadDocumentsActivity
        implements UpdateLicenseFragment.UpdateLicenseFragmentCallback, TakePhotoFragment.TakePhotoListenerContainer {

    private TakePhotoFragment.TakePhotoListener takePhotoListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        replaceFragment(UpdateLicenseFragment.newInstance(), R.id.content_frame, false, Transition.NONE);
    }

    @Override
    protected String getTitleString() {
        return getString(R.string.title_driver_license);
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, UpdateLicenseActivity.class);
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
