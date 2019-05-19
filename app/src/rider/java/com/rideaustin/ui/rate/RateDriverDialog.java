package com.rideaustin.ui.rate;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.ut.UT;
import com.rideaustin.api.model.Ride;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.databinding.FragmentRateDriverBinding;
import com.rideaustin.events.DriverRatedEvent;
import com.rideaustin.ui.payment.BevoBucksPaymentActivity;
import com.rideaustin.ui.utils.infodialog.InfoDialog;

import java8.util.Optional;
import timber.log.Timber;

import static com.rideaustin.ui.utils.TextFilters.EMOJI_FILTER;
import static com.rideaustin.ui.utils.TextFilters.MATH_SYMBOL_FILTER;
import static com.rideaustin.ui.utils.TextFilters.OTHER_SYMBOL_FILTER;

/**
 * Created by vokol on 01.07.2016.
 */
public class RateDriverDialog extends DialogFragment implements RateDriverView {

    private FragmentRateDriverBinding binding;
    private RateDriverDialogViewModel viewModel;
    private BaseActivityCallback baseActivityCallback;
    private DialogInterface.OnDismissListener onDismissListener;
    private long rideId;

    public static RateDriverDialog createNew(long rideId) {
        RateDriverDialog rateDriverDialog = new RateDriverDialog();
        rateDriverDialog.rideId = rideId;
        rateDriverDialog.setCancelable(false);
        return rateDriverDialog;
    }

    public RateDriverDialogViewModel getViewModel() {
        return viewModel;
    }

    public static boolean isShowing(FragmentManager fragmentManager) {
        return fragmentManager.findFragmentByTag(RateDriverDialog.class.getName()) != null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.baseActivityCallback = (BaseActivityCallback) context;
        } catch (ClassCastException e) {
            Timber.d(e.getMessage(), e);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.baseActivityCallback = (BaseActivityCallback) activity;
        } catch (ClassCastException e) {
            Timber.d(e.getMessage(), e);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_rate_driver, container, false);
        binding.edtLeaveComment.setFilters(new InputFilter[]{EMOJI_FILTER, OTHER_SYMBOL_FILTER, MATH_SYMBOL_FILTER});
        viewModel = new RateDriverDialogViewModel(binding, baseActivityCallback, this, rideId);
        binding.setViewModel(viewModel);
        getDialog().requestWindowFeature(STYLE_NO_TITLE);
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

    public RateDriverDialog show(FragmentManager fragmentManager) {
        if (!isShowing(fragmentManager)) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.add(this, RateDriverDialog.class.getName());
            // RA-12892: looks like edge case
            // despite the fact, that this method is designed to be only called between onStart..onStop
            // sometimes (very rarely) it is called after onSavedInstanceState.
            // However, it's safe to allow committing fragment transaction after it.
            // On process restart, app will also restart from beginning and check unrated ride again.
            ft.commitAllowingStateLoss();
        }
        return this;
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.onStart();
        updateSubmitButton();
        binding.rbRateDriver.setOnScoreChanged(score -> updateSubmitButton());
        binding.radioGroupTips.setOnCheckedChangeListener((group, checkedId) -> updateSubmitButton());
        binding.btnSubmit.setOnClickListener(view -> viewModel.onSubmitClicked(getActivity(), binding.rbRateDriver.getScore()));
        binding.bevoText.setOnClickListener(this::showBevoInfo);
        binding.bevoInfo.setOnClickListener(this::showBevoInfo);
        binding.bevoSwitch.setOnCheckedChangeListener(this::onBevoSelectedChanged);
    }

    private void onBevoSelectedChanged(CompoundButton button, boolean isChecked) {
        viewModel.selectBevo(isChecked);
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.onDestroy();
    }

    private void updateSubmitButton() {
        boolean starsSelected = binding.rbRateDriver.getScore() > 0;
        boolean tipSelected = binding.radioGroupTips.getCheckedRadioButtonId() > 0 || !viewModel.getShouldShowTipOption().get();
        binding.btnSubmit.setEnabled(starsSelected && tipSelected);
    }

    @Override
    public void onDriverRatingSent() {
        dismiss();
        App.getStateManager().post(new DriverRatedEvent(true));
    }

    @Override
    public void onLongComment() {
        Toast.makeText(getContext(), getString(R.string.comment_is_long), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCustomFieldRequired() {
        binding.customTip.setError(getString(R.string.enter_value));
    }

    @Override
    public void onBevoPayment(Ride ride) {
        Intent intent = new Intent(getContext(), BevoBucksPaymentActivity.class).putExtra(BevoBucksPaymentActivity.URL_KEY, ride.getBevoBucksUrl());
        startActivity(intent);
    }

    @Override
    public void scrollDownToSubmit() {
        binding.rateDriverContainer.smoothScrollTo(0, binding.btnSubmit.getTop());
    }

    private void showBevoInfo(final View view) {
        FragmentManager fragmentManager = getFragmentManager();
        Optional.ofNullable(App.getConfigurationManager().getLastConfiguration().getUt())
                .map(UT::getPayWithBevoBucks)
                .ifPresent(payWithBevoBucks -> {
                    InfoDialog.create(R.drawable.icn_bevobucks_logo, getString(R.string.pay_with_bevobucks),
                            payWithBevoBucks.getDescription())
                            .show(fragmentManager);
                });
    }

}
