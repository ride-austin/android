package com.rideaustin.ui.map;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rideaustin.R;
import com.rideaustin.databinding.RateRiderBinding;
import com.rideaustin.ui.map.MapFragmentInterface.OnButtonClickListener;
import com.rideaustin.ui.map.MapFragmentInterface.OnRateChangedListener;

import java.util.Locale;

/**
 * Created by vokol on 01.07.2016.
 */
public class RateRiderDialog extends DialogFragment {
    private RateRiderBinding binding;
    private OnButtonClickListener onButtonClicked;
    private OnRateChangedListener onRateChanged;
    private DialogInterface.OnDismissListener onDismissListener;
    private float initialRating;
    private double driverPayment;

    public static RateRiderDialog newInstance(OnButtonClickListener onButtonClicked,
                                              OnRateChangedListener onRateChanged,
                                              float initialRating,
                                              double driverPayment) {
        RateRiderDialog dialog = new RateRiderDialog();
        dialog.initialRating = initialRating;
        dialog.driverPayment = driverPayment;
        dialog.onButtonClicked = onButtonClicked;
        dialog.onRateChanged = onRateChanged;
        return dialog;
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_rate_rider, container, false);
        getDialog().requestWindowFeature(STYLE_NO_TITLE);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvPrice.setText(getString(R.string.money,
                String.format(Locale.US, "%.2f", driverPayment > 0 ? driverPayment : 0d)));
        if (initialRating > 0) {
            binding.rbRateDriver.setScore(initialRating);
            binding.btnSubmit.setVisibility(View.VISIBLE);
        } else {
            binding.rbRateDriver.setScore(0);
            binding.btnSubmit.setVisibility(View.GONE);
        }

        binding.rbRateDriver.setOnScoreChanged(score -> {
            if (onRateChanged != null) {
                binding.btnSubmit.setVisibility(score > 0 ? View.VISIBLE : View.GONE);
                onRateChanged.onChanged(score);
            }
        });
        binding.btnSubmit.setOnClickListener(v -> {
            if (onButtonClicked != null) {
                onButtonClicked.onClicked();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().setCancelable(false);
    }
}
