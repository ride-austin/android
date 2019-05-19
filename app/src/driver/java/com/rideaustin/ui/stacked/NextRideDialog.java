package com.rideaustin.ui.stacked;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.RideCancellationConfig;
import com.rideaustin.base.BaseDialogFragment;
import com.rideaustin.databinding.NextRideDialogLayoutBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.utils.ContactHelper;
import com.rideaustin.utils.DialogUtils;

import java.util.concurrent.TimeUnit;

/**
 * Created on 21/12/2017
 *
 * @author sdelaysam
 */

public class NextRideDialog extends BaseDialogFragment<NextRideDialogViewModel> {

    private NextRideDialogLayoutBinding binding;
    private MaterialDialog cancelDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.next_ride_dialog_layout, container, false);
        getDialog().setOnKeyListener(this::onKeyAction);
        getDialog().requestWindowFeature(STYLE_NO_TITLE);
        setViewModel(obtainViewModel(NextRideDialogViewModel.class));
        getViewModel().initialize();
        binding.setViewModel(getViewModel());
        ((ViewGroup) binding.getRoot()).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        untilStop(getViewModel().getDismissObservable()
                .observeOn(RxSchedulers.main())
                .subscribe(aVoid -> dismiss()));
        binding.getRoot().setVisibility(View.INVISIBLE);
        RxSchedulers.schedule(this::animatedShow, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Window window = getDialog().getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().setCancelable(false);
        getDialog().setCanceledOnTouchOutside(false);
        binding.content.setOnClickListener(v -> {});
        binding.background.setOnClickListener(v -> animatedDismiss());
        binding.imageButton.setOnClickListener(v -> animatedDismiss());
        binding.call.setOnClickListener(v -> call());
        binding.sms.setOnClickListener(v -> sms());
        binding.cancel.setPaintFlags(binding.cancel.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        binding.cancel.setOnClickListener(v -> showCancelConfirmation());
    }

    private boolean onKeyAction(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            animatedDismiss();
        }
        return false;
    }

    private void animatedShow() {
        CommonMaterialDialogCreator.animateDialogShow(binding.getRoot());
    }

    private void animatedDismiss() {
        CommonMaterialDialogCreator.animateDialogDismiss(binding.getRoot(), this::dismiss);
    }

    private void showCancelConfirmation() {
        DialogUtils.dismiss(cancelDialog);
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        RideCancellationConfig config = App.getConfigurationManager().getLastConfiguration().getRideCancellationConfig();
        if (config != null && config.isEnabled()) {
            App.getInstance().getStateManager().getNextRideIfAny().ifPresent(ride -> {
                ((NavigationDrawerActivity) activity).showRideCancellationFeedback(ride.getId(), getViewModel()::cancelRider);
            });
        } else {
            cancelDialog = MaterialDialogCreator.createConfirmationDialog(getString(R.string.text_cancel_ride_confirmation), activity);
            cancelDialog.setOnShowListener(dialogInterface -> {
                MDButton positiveButton = cancelDialog.getActionButton(DialogAction.POSITIVE);
                positiveButton.setOnClickListener(v -> {
                    getViewModel().cancelRider(null, null);
                    dialogInterface.dismiss();
                });
            });
        }
    }

    private void call() {
        ContactHelper.call(getActivity(), getViewModel().getPhone().orElse(null));
    }

    private void sms() {
        ContactHelper.sms(getActivity(), getViewModel().getPhone().orElse(null));
    }
}
