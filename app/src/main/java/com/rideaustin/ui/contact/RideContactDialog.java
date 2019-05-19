package com.rideaustin.ui.contact;

import android.animation.LayoutTransition;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.rideaustin.R;
import com.rideaustin.api.model.Ride;
import com.rideaustin.base.BaseDialogFragment;
import com.rideaustin.databinding.RideContactDialogLayoutBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;
import com.rideaustin.utils.ContactHelper;

import java.util.concurrent.TimeUnit;

/**
 * Created on 04/04/2018
 *
 * @author sdelaysam
 */

public class RideContactDialog extends BaseDialogFragment<RideContactViewModel> {

    private RideContactDialogLayoutBinding binding;

    public void setRide(Ride ride) {
        if (getViewModel() == null) {
            setViewModel(obtainViewModel(RideContactViewModel.class));
        }
        getViewModel().setRide(ride);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.ride_contact_dialog_layout, container, false);
        if (getViewModel() == null) {
            setViewModel(obtainViewModel(RideContactViewModel.class));
        }
        binding.setViewModel(getViewModel());
        binding.content.setOnClickListener(v -> {});
        binding.background.setOnClickListener(v -> animatedDismiss());
        binding.imageButton.setOnClickListener(v -> animatedDismiss());
        binding.call.setOnClickListener(v -> call());
        binding.sms.setOnClickListener(v -> sms());
        getDialog().setOnKeyListener(this::onKeyAction);
        getDialog().setCancelable(false);
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().requestWindowFeature(STYLE_NO_TITLE);
        LayoutTransition layoutTransition = binding.content.getLayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        return binding.getRoot();
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
    public void onStart() {
        super.onStart();
        binding.getRoot().setVisibility(View.INVISIBLE);
        RxSchedulers.schedule(this::animatedShow, 100, TimeUnit.MILLISECONDS);
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
        CommonMaterialDialogCreator.animateDialogDismiss(binding.getRoot(), this::dismissAllowingStateLoss);
    }

    private void call() {
        animatedDismiss();
        ContactHelper.call(getActivity(), getViewModel().getPhoneNumber());
    }

    private void sms() {
        animatedDismiss();
        ContactHelper.sms(getActivity(), getViewModel().getPhoneNumber());
    }

}
