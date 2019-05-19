package com.rideaustin.ui.widgets.dialogs;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.rideaustin.R;
import com.rideaustin.databinding.RADialogBinding;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;

import rx.functions.Action0;

/**
 * Created by crossover on 18/06/2017.
 */

public class RADialog extends DialogFragment implements RADialogView {

    private RADialogViewModel viewModel;
    private RADialogBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.ra_dialog_layout, container, false);
        getDialog().setOnKeyListener(this::onKeyAction);
        getDialog().requestWindowFeature(STYLE_NO_TITLE);
        binding.setViewModel(viewModel);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        CommonMaterialDialogCreator.animateDialogShow(binding.getRoot());
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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().setCancelable(false);
        getDialog().setCanceledOnTouchOutside(false);
    }

    private boolean onKeyAction(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            viewModel.onDismissClicked(null);
        }
        return false;
    }

    @Override
    public void animatedDismiss() {
        CommonMaterialDialogCreator.animateDialogDismiss(binding.getRoot(), this::dismiss);
    }

    public void apply(RADialogViewModel viewModel) {
        if (this.viewModel == viewModel) {
            return;
        }
        this.viewModel.setTwoActionsListener(null);
        this.viewModel.setSingleActionListener(null);
        this.viewModel.setOnDismissListener(null);
        this.viewModel = viewModel;
        if (binding != null) {
            binding.setViewModel(viewModel);
        }
    }

    public void show(FragmentManager fragmentManager, Action0 onDismiss) {
        show(fragmentManager);
        viewModel.setOnDismissListener(onDismiss);
    }

    public void show(FragmentManager fragmentManager) {
        super.show(fragmentManager, RADialog.class.getName());
    }

    public enum TwoActions {

        YES_NO(R.string.btn_yes, R.string.btn_no);
        @StringRes
        int positiveString;
        @StringRes
        int negativeString;

        TwoActions(@StringRes int positiveAction, @StringRes int negativeAction) {
            positiveString = positiveAction;
            negativeString = negativeAction;
        }
    }

    /**
     * Default dialog type is `NoAction`. In order to change it, you should first assign (Single/Two)ActionListener.
     */
    public static class Builder {
        private RADialogViewModel viewModel;

        public Builder() {
            viewModel = new RADialogViewModel();
        }

        public Builder setIcon(RADialogIcon icon) {
            viewModel.setIcon(icon);
            return this;
        }

        public Builder setTitle(@StringRes int title) {
            viewModel.setTitle(title);
            return this;
        }

        public Builder setTitle(String title) {
            viewModel.setTitle(title);
            return this;
        }

        public Builder setContent(@StringRes int content) {
            viewModel.setContent(content);
            return this;
        }

        public Builder setContent(String content) {
            viewModel.setContent(content);
            return this;
        }

        public Builder setOnDismissListener(Action0 listener) {
            viewModel.setOnDismissListener(listener);
            return this;
        }

        public Builder setSingleActionListener(SingleActionDialogListener listener) {
            viewModel.setSingleActionListener(listener);
            return this;
        }

        public Builder setTwoActionsListener(TwoActionsDialogListener listener) {
            viewModel.setTwoActionsListener(listener);
            return this;
        }

        public Builder setSingleActionText(String actionText) {
            viewModel.setSingleActionText(actionText);
            return this;
        }

        public Builder setSingleActionText(@StringRes int actionText) {
            viewModel.setSingleActionText(actionText);
            return this;
        }

        public Builder setTwoActions(TwoActions actions) {
            viewModel.setTwoActions(actions);
            return this;
        }

        public void apply(RADialog dialog) {
            dialog.apply(viewModel);
            viewModel.setDialog(dialog);
        }

        public RADialog build() {
            RADialog dialog = new RADialog();
            dialog.viewModel = viewModel;
            viewModel.setDialog(dialog);
            return dialog;
        }
    }
}
