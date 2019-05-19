package com.rideaustin.ui.rideupgrade;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rideaustin.R;
import com.rideaustin.api.model.UpgradeRequest;
import com.rideaustin.databinding.RideUpgradeDialogLayoutBinding;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;

import java8.util.Optional;
import rx.functions.Action0;
import timber.log.Timber;

/**
 * Created by hatak on 14.06.2017.
 */

public class RideUpgradeDialog extends DialogFragment {

    private static final String ARG_KEY = "upgrade_request";

    private RideUpgradeDialogLayoutBinding binding;
    private RideUpgradeDialogViewModel viewModel;
    private Optional<Action0> onAcceptAction;
    private Optional<Action0> onDeclineAction;

    public static RideUpgradeDialog create(@NonNull final UpgradeRequest upgradeRequest, @Nullable final Action0 onAcceptAction, @Nullable Action0 onDeclineAction) {
        if (upgradeRequest == null)
            throw new IllegalArgumentException("UpgradeRequest object is null");
        RideUpgradeDialog dialog = new RideUpgradeDialog();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_KEY, upgradeRequest);
        dialog.setArguments(bundle);
        dialog.onAcceptAction = Optional.ofNullable(onAcceptAction);
        dialog.onDeclineAction = Optional.ofNullable(onDeclineAction);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.ride_upgrade_dialog_layout, container, false);
        getDialog().requestWindowFeature(STYLE_NO_TITLE);
        viewModel = new RideUpgradeDialogViewModel((UpgradeRequest) getArguments().getSerializable(ARG_KEY));
        binding.setViewModel(viewModel);
        getDialog().setOnKeyListener(this::onKeyAction);
        binding.acceptButton.setOnClickListener(this::onAccept);
        binding.declineButton.setOnClickListener(this::onDecline);
        binding.cancelButton.setOnClickListener(this::onDecline);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        CommonMaterialDialogCreator.animateDialogShow(binding.getRoot());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        getDialog().setCancelable(false);
        getDialog().setCanceledOnTouchOutside(false);
    }

    private boolean onKeyAction(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            decline();
        }
        return false;
    }

    private void onAccept(final View v) {
        onAcceptAction.orElse(() -> Timber.w("on accept action not provided")).call();
        this.dismiss();
    }

    private void onDecline(final View v) {
        decline();
    }

    private void decline() {
        CommonMaterialDialogCreator.animateDialogDismiss(binding.getRoot(), () -> {
            onDeclineAction.orElse(() -> Timber.w("on decline action not provided")).call();
            this.dismiss();
        });
    }

    public void show(FragmentManager fragmentManager) {
        super.show(fragmentManager, RideUpgradeDialog.class.getName());
    }
}
