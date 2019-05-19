package com.rideaustin.ui.signup.driver.tos;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.rideaustin.R;
import com.rideaustin.api.model.driver.DriverSignUpResult;
import com.rideaustin.databinding.RCRATermsAndConditionsBinding;
import com.rideaustin.ui.signup.driver.BaseDriverSignUpFragment;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.utils.DialogUtils;

/**
 * Created by RideClient on 8/17/16.
 */
public class TermsAndConditionsFragment extends BaseDriverSignUpFragment implements TermsAndConditionsView {

    private RCRATermsAndConditionsBinding rctaTermsAndConditionsBinding;
    private TermsAndConditionsViewModel viewModel;
    private MaterialDialog termsDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        rctaTermsAndConditionsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_fcra_terms_and_conditions, container, false);
        viewModel = new TermsAndConditionsViewModel(this, getSignUpInteractor());
        setHasOptionsMenu(true);
        setHasHelpWidget(true);
        setToolbarTitle(R.string.title_terms_and_conditions);
        rctaTermsAndConditionsBinding.continueButton.setOnClickListener(v -> {
            if (rctaTermsAndConditionsBinding.checkBox.isChecked()) {
                rctaTermsAndConditionsBinding.checkBox.setError(null);
                // Create new Driver
                viewModel.createNewDriver();
            } else {
                rctaTermsAndConditionsBinding.checkBox.setError(getString(R.string.the_fcra_review_and_approve_required));
            }
        });

        return rctaTermsAndConditionsBinding.getRoot();
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

    @Override
    public void onCompleted() {
        notifyCompleted();
    }

    @Override
    public void onTermsLoading() {
        getCallback().showProgress();
        hideTermsDialog();
    }

    @Override
    public void onTermsUpdated(String terms) {
        getCallback().hideProgress();
        hideTermsDialog();
        rctaTermsAndConditionsBinding.termsAndConditions.setText(terms);
    }

    @Override
    public void onTermsError(String error, boolean shouldRetry) {
        getCallback().hideProgress();
        hideTermsDialog();
        String title = getString(R.string.text_oops);
        if (shouldRetry) {
            termsDialog = MaterialDialogCreator.createDialogWithCallback(getActivity(), title, error, R.string.retry,
                    (dialog, which) -> viewModel.loadTermsOfService())
                    .cancelable(false).negativeText("")
                    .show();
        } else {
            termsDialog = MaterialDialogCreator.createErrorDialog(title, error, (AppCompatActivity) getActivity());
        }
        rctaTermsAndConditionsBinding.termsAndConditions.setText(null);
    }

    @Override
    public void onShowTNCDataErrorDialog(final DriverSignUpResult result) {
        if (!isAttached()) {
            return;
        }
        final MaterialDialog dialog = CommonMaterialDialogCreator.createYesNoDialog(getString(R.string.error_tnc_upload_failed), getActivity())
                .build();
        dialog.setOnShowListener(dialogInterface -> {
            final MDButton positiveButton = dialog.getActionButton(DialogAction.POSITIVE);
            positiveButton.setOnClickListener(v -> {
                dialog.dismiss();
                getCallback().showProgress();
                viewModel.uploadOptionalTNCData(result);
            });
        });
        dialog.show();
    }

    @Override
    public void onShowCarPhotoErrorDialog(final DriverSignUpResult result) {
        if (!isAttached()) {
            return;
        }
        final MaterialDialog dialog = CommonMaterialDialogCreator.createYesNoDialog(getString(R.string.error_car_photo_upload_failed), getActivity())
                .build();
        dialog.setOnShowListener(dialogInterface -> {
            final MDButton positiveButton = dialog.getActionButton(DialogAction.POSITIVE);
            positiveButton.setOnClickListener(v -> {
                dialog.dismiss();
                getCallback().showProgress();
                viewModel.uploadCarPhotoData(result);
            });
        });
        dialog.show();
    }

    @Override
    public void onShowCarDataErrorDialog(final DriverSignUpResult result) {
        if (!isAttached()) {
            return;
        }
        final MaterialDialog dialog = CommonMaterialDialogCreator.createYesNoDialog(getString(R.string.error_car_data_upload_failed), getActivity())
                .build();
        dialog.setOnShowListener(dialogInterface -> {
            final MDButton positiveButton = dialog.getActionButton(DialogAction.POSITIVE);
            positiveButton.setOnClickListener(v -> {
                dialog.dismiss();
                getCallback().showProgress();
                viewModel.uploadCarData(result);
            });
        });
        dialog.show();
    }

    @Override
    public void onShowDriverPhotoErrorDialog(DriverSignUpResult result) {
        if (!isAttached()) {
            return;
        }
        final MaterialDialog dialog = CommonMaterialDialogCreator.createYesNoDialog(getString(R.string.error_driver_photo_upload_failed), getActivity())
                .build();
        dialog.setOnShowListener(dialogInterface -> {
            final MDButton positiveButton = dialog.getActionButton(DialogAction.POSITIVE);
            positiveButton.setOnClickListener(v -> {
                dialog.dismiss();
                getCallback().showProgress();
                viewModel.uploadDriverPhoto(result);
            });
        });
        dialog.show();
    }

    @Override
    public void onShowCreateDriverErrorDialog(String message) {
        if (!isAttached()) {
            return;
        }
        final MaterialDialog dialog = CommonMaterialDialogCreator
                .createNetworkFailDialog(message, getActivity())
                .title(getString(R.string.error_driver_creation_failed))
                .build();
        dialog.setOnShowListener(dialogInterface -> {
            final MDButton positiveButton = dialog.getActionButton(DialogAction.POSITIVE);
            positiveButton.setOnClickListener(v -> {
                dialog.dismiss();
                viewModel.createNewDriver();
            });
            final MDButton negativeButton = dialog.getActionButton(DialogAction.NEGATIVE);
            negativeButton.setOnClickListener(v -> {
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private void hideTermsDialog() {
        DialogUtils.dismiss(termsDialog, "Unable to hide terms dialog");
        termsDialog = null;
    }

}
