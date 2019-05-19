package com.rideaustin.ui.drawer.documents.tnc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.DriverRegistration;
import com.rideaustin.api.model.Document;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.base.Transition;
import com.rideaustin.ui.common.TakePhotoFragment;
import com.rideaustin.ui.documents.UploadDocumentsActivity;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.utils.CommonConstants;
import com.rideaustin.utils.toast.RAToast;

public class UpdateTNCActivity extends UploadDocumentsActivity
        implements DriverTNCCardFragment.DriverTNCCardFragmentCallback, UpdateTNCView, TakePhotoFragment.TakePhotoListenerContainer {

    private static final String KEY_REGISTRATION = "KEY_REGISTRATION";

    UpdateTNCViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new UpdateTNCViewModel(this, getDriverRegistrationConfig());
        replaceFragment(DriverTNCCardFragment.newInstance(CommonConstants.TNCCardSide.FRONT), R.id.content_frame, false, Transition.NONE);
    }

    private DriverRegistration getDriverRegistrationConfig() {
        return (DriverRegistration) getIntent().getSerializableExtra(KEY_REGISTRATION);
    }

    @Override
    protected String getTitleString() {
        return getDriverRegistrationConfig().getTncCard().getHeader();
    }

    @Override
    public void onNext() {
        replaceFragment(DriverTNCCardFragment.newInstance(CommonConstants.TNCCardSide.BACK), R.id.content_frame, true);
    }

    @Override
    public void onSave() {
        viewModel.onSave();
    }

    @Override
    public void onTncCardsCombined() {
        viewModel.postPhotos(this);
    }

    @Override
    public UpdateTNCViewModel getUpdateTNCViewModel() {
        return viewModel;
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.onStop();
    }

    public static Intent newIntent(Context context, DriverRegistration driverRegistration) {
        return new Intent(context, UpdateTNCActivity.class).putExtra(KEY_REGISTRATION, driverRegistration);
    }

    @Override
    public TakePhotoFragment.TakePhotoListener getTakePhotoListener() {
        return viewModel;
    }

    @Override
    public void setTakePhotoListener(TakePhotoFragment.TakePhotoListener listener) {

    }

    @Override
    public void onPhotoTaken(TakePhotoFragment.Source source, String filePath) {
        Fragment f = getFragment(R.id.content_frame);
        if (f instanceof DriverTNCCardFragment) {
            DriverTNCCardFragment fragment = (DriverTNCCardFragment) f;
            fragment.onPhotoTaken(source, filePath);
        }
    }

    @Override
    public void onPhotoCanceled() {
        Fragment f = getFragment(R.id.content_frame);
        if (f instanceof DriverTNCCardFragment) {
            DriverTNCCardFragment fragment = (DriverTNCCardFragment) f;
            fragment.onPhotoCanceled();
        }
    }

    @Override
    public void onTncCardCombineFailed(String message) {
        hideProgress();
        RAToast.showShort(message);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onDocumentUploaded() {
        MaterialDialogCreator.createInfoDialogCentered(App.getAppName(), getString(R.string.documents_updated), this)
                .setOnDismissListener(dialog -> finish());
    }

    @Override
    public void onDocumentDownloaded(Document document) {
        Fragment f = getFragment(R.id.content_frame);
        if (f instanceof DriverTNCCardFragment) {
            DriverTNCCardFragment fragment = (DriverTNCCardFragment) f;
            fragment.onDocumentDownloaded(document);
        }
    }

    @Override
    public void onDocumentDownloadFailed() {
        CommonMaterialDialogCreator.createNetworkFailDialog(getString(R.string.failed_tnc_fetch, getTitleString()), getContext())
                .onPositive((dialog, which) -> viewModel.loadDocument(this))
                .onNegative((dialog, which) -> finish())
                .show();
    }

    @Override
    public BaseActivityCallback getCallback() {
        return this;
    }
}
