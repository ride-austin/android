package com.rideaustin.ui.utils.infodialog;


import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.rideaustin.R;
import com.rideaustin.databinding.InfoDialogLayoutBinding;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;

/**
 * Created by hatak on 07.08.2017.
 */

public class InfoDialog extends DialogFragment {

    public static final String ICON = "icon";
    public static final String TITLE = "title";
    public static final String CONTENT = "content";

    private InfoDialogLayoutBinding binding;
    private InfoDialogViewModel viewModel;

    public static InfoDialog create(@DrawableRes final int icon, @NonNull final String title, @NonNull String content) {
        InfoDialog dialog = new InfoDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(ICON, icon);
        bundle.putString(TITLE, title);
        bundle.putString(CONTENT, content);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.info_dialog_layout, container, false);
        viewModel = new InfoDialogViewModel(getArguments().getInt(ICON), getArguments().getString(TITLE), getArguments().getString(CONTENT));
        binding.setViewModel(viewModel);
        binding.cancelButton.setPaintFlags(binding.cancelButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        binding.cancelButton.setOnClickListener(this::onCloseClicked);
        getDialog().requestWindowFeature(STYLE_NO_TITLE);
        getDialog().setOnKeyListener(this::onKeyAction);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Window window = getDialog().getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void onCloseClicked(View view) {
        CommonMaterialDialogCreator.animateDialogDismiss(binding.getRoot(), () -> dismiss());
    }

    private boolean onKeyAction(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            CommonMaterialDialogCreator.animateDialogDismiss(binding.getRoot(), () -> dismiss());
        }
        return false;
    }

    public void show(FragmentManager fragmentManager) {
        super.show(fragmentManager, InfoDialog.class.getName());
    }

    @Override
    public void onStart() {
        super.onStart();
        CommonMaterialDialogCreator.animateDialogShow(binding.getRoot());
    }
}
