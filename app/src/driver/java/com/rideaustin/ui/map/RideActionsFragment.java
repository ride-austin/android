package com.rideaustin.ui.map;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.rideaustin.R;
import com.rideaustin.databinding.FragmentRideActionsBinding;
import com.rideaustin.engine.EngineState;

import timber.log.Timber;

/**
 * Created on 12/12/2017
 *
 * @author sdelaysam
 */

public class RideActionsFragment extends Fragment {

    public interface Listener {
        void onFlowAction(boolean confirmed);
    }

    private FragmentRideActionsBinding binding;

    @Nullable private EngineState.Type type;
    @Nullable private Listener listener;
    @Nullable private MapFragmentInterface.MapPaddingListener mapPaddingListener;
    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;

    public void setType(@Nullable EngineState.Type type) {
        this.type = type;
        updateView();
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    public void setMapPaddingListener(@Nullable MapFragmentInterface.MapPaddingListener mapPaddingListener) {
        this.mapPaddingListener = mapPaddingListener;
    }

    public void clear() {
        type = null;
        listener = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ride_actions, container, false);
        binding.sliderText.setOnClickListener(v -> onFlowAction());
        onGlobalLayoutListener = this::updateMapPadding;
        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateView();
        updateMapPadding();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    private void updateView() {
        if (binding == null || type == null) {
            return;
        }
        switch (type) {
            case ACCEPTED:
                binding.sliderText.setBackgroundResource(R.drawable.rounded_blue_button);
                binding.sliderText.setText(R.string.slide_to_arrive);
                break;
            case ARRIVED:
                binding.sliderText.setBackgroundResource(R.drawable.rounded_blue_button);
                binding.sliderText.setText(R.string.slide_to_start_trip);
                break;
            case STARTED:
                binding.sliderText.setBackgroundResource(R.drawable.rounded_red_button);
                binding.sliderText.setText(R.string.slide_to_finish);
                break;
        }
    }

    private void updateMapPadding() {
        if (mapPaddingListener != null && binding != null) {
            mapPaddingListener.onBottomPaddingUpdated(binding.getRoot().getHeight());
        }
    }

    private void onFlowAction() {
        if (listener != null) {
            listener.onFlowAction(false);
        }
    }
}
