package com.rideaustin.ui.drawer.editprofile;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.databinding.VerifyEmailDialogLayoutBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;

import java.util.concurrent.TimeUnit;

/**
 * Created on 15/11/2017
 *
 * @author sdelaysam
 */

public class VerifyEmailDialog extends DialogFragment {

    private VerifyEmailDialogLayoutBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.verify_email_dialog_layout, container, false);
        getDialog().setOnKeyListener(this::onKeyAction);
        getDialog().requestWindowFeature(STYLE_NO_TITLE);
        binding.setViewModel(new VerifyEmailViewModel());
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
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
        binding.background.setOnClickListener(v -> animatedDismiss());
        binding.imageButton.setOnClickListener(v -> animatedDismiss());
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
        App.getDataManager().postEmailVerified(true);
        dismiss();
    }

}
