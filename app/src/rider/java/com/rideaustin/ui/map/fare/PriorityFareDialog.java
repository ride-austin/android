package com.rideaustin.ui.map.fare;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.rideaustin.R;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.databinding.FragmentPriorityFareBinding;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.utils.DialogUtils;

import rx.subjects.PublishSubject;

/**
 * Created by rideclientandroid on 06.10.2016.
 */
public class PriorityFareDialog extends DialogFragment implements PriorityFareView {

    private FragmentPriorityFareBinding binding;
    private PriorityFareDialogViewModel viewModel;
    private BaseActivityCallback baseActivityCallback;
    private PublishSubject<Boolean> resultPublishSubject = PublishSubject.create();
    private MaterialDialog surgeUpdateDialog;
    private PriorityFareDialogListener listener;

    public static PriorityFareDialog createNew(final PriorityFareDialogListener listener) {
        PriorityFareDialog priorityFareDialog = new PriorityFareDialog();
        priorityFareDialog.listener = listener;
        return priorityFareDialog;
    }

    public PriorityFareDialogViewModel getViewModel() {
        return viewModel;
    }

    public static boolean isShowing(FragmentManager fragmentManager) {
        return fragmentManager.findFragmentByTag(PriorityFareDialog.class.getName()) != null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.baseActivityCallback = (BaseActivityCallback) context;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("RateDriverDialog can be attached only to BaseActivity");
        }
    }

    public PublishSubject<Boolean> getResultPublishSubject() {
        return resultPublishSubject;
    }

    public void show(FragmentManager fragmentManager) {
        super.show(fragmentManager, PriorityFareDialog.class.getName());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_priority_fare, container, false);
        getDialog().setTitle(R.string.priority_fare);
        getDialog().requestWindowFeature(STYLE_NO_TITLE);
        viewModel = new PriorityFareDialogViewModel(this, getArguments());
        binding.setViewModel(viewModel);
        getDialog().setOnKeyListener(this::onKeyAction);
        return binding.getRoot();
    }

    private boolean onKeyAction(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (listener != null) {
                listener.onDialogDismiss();
            }
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().setCancelable(true);
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        viewModel.onStart();
        binding.btnSubmit.setOnClickListener(v -> {
            v.setEnabled(false);
            onSubmitButtonClicked();
        });
        binding.btnCancel.setOnClickListener(v -> onCancelButtonClicked());
    }

    @Override
    public void onStop() {
        super.onStop();
        DialogUtils.dismiss(surgeUpdateDialog);
        viewModel.onStop();
    }

    private void onSubmitButtonClicked() {
        resultPublishSubject.onNext(true);
        resultPublishSubject.onCompleted();
        DialogUtils.dismiss(getDialog());
        if (listener != null) {
            listener.onDialogDismiss();
        }
    }

    private void onCancelButtonClicked() {
        resultPublishSubject.onNext(false);
        resultPublishSubject.onCompleted();
        DialogUtils.dismiss(getDialog());
        if (listener != null) {
            listener.onDialogDismiss();
        }
    }

    @Override
    public void onPriorityFareUpdated() {
        closeSurgeDialog();
        surgeUpdateDialog = MaterialDialogCreator.createSimpleErrorDialog(getString(R.string.surge_updated_dialog_msg), getActivity());
    }

    @Override
    public void onPriorityFareDisabled() {
        closeSurgeDialog();
        surgeUpdateDialog = CommonMaterialDialogCreator.createYesNoDialog(getString(R.string.surge_disabled_fialog_msg), getActivity()).build();
        surgeUpdateDialog.setOnShowListener(dialogInterface -> {
            final MDButton positiveButton = surgeUpdateDialog.getActionButton(DialogAction.POSITIVE);
            positiveButton.setOnClickListener(v -> {
                surgeUpdateDialog.dismiss();
                binding.btnSubmit.setEnabled(false);
                onSubmitButtonClicked();
                binding.btnSubmit.setEnabled(true);
            });

            final MDButton negativeButton = surgeUpdateDialog.getActionButton(DialogAction.NEGATIVE);
            negativeButton.setOnClickListener(v -> {
                surgeUpdateDialog.dismiss();
                onCancelButtonClicked();
            });
        });
        surgeUpdateDialog.show();

    }

    private void closeSurgeDialog() {
        DialogUtils.dismiss(surgeUpdateDialog, "Error while closing surge dialog");
    }


}
