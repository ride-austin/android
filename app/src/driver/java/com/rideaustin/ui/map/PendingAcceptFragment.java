package com.rideaustin.ui.map;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.Ride;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentPendingAcceptBinding;
import com.rideaustin.utils.AnswersUtils;
import com.rideaustin.utils.ViewUtils;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by ysych on 8/17/16.
 */
public class PendingAcceptFragment extends BaseFragment<PendingAcceptViewModel> {

    private FragmentPendingAcceptBinding binding;
    private MainMapFragment.OnButtonClickListener acceptButtonClickedListener;

    private Observable<Long> acceptCounter;
    private Subscription timerSubscription = Subscriptions.empty();

    private MapFragmentInterface.MapPaddingListener mapPaddingListener;
    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;

    public void setRide(Ride ride) {
        viewModel().setRide(ride);
    }

    public void setStartAddress(String address) {
        viewModel().startAddress.set(address);
    }

    public void setAcceptCounter(Observable<Long> acceptCounter) {
        this.acceptCounter = acceptCounter;
    }

    public void setAcceptButtonClickedListener(MainMapFragment.OnButtonClickListener acceptButtonClickedListener) {
        this.acceptButtonClickedListener = acceptButtonClickedListener;
    }

    public void setMapPaddingListener(MapFragmentInterface.MapPaddingListener mapPaddingListener) {
        this.mapPaddingListener = mapPaddingListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pending_accept, container, false);
        binding.setViewModel(viewModel());
        onGlobalLayoutListener = this::updateMapPadding;
        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.pendingPickup.setOnClickListener(container -> {
            AnswersUtils.logAcceptRide(App.getDataManager().getCurrentDriver());
            binding.pendingPickup.setOnClickListener(null);
            acceptButtonClickedListener.onClicked();
        });

        timerSubscription = acceptCounter.subscribe(count -> binding.counterText.setText(String.valueOf(count)));
        updateMapPadding();
    }

    @Override
    public void onStart() {
        super.onStart();
        onGlobalLayoutListener.onGlobalLayout();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        timerSubscription.unsubscribe();
    }

    private void updateMapPadding() {
        if (mapPaddingListener != null && binding != null) {
            mapPaddingListener.onBottomPaddingUpdated(binding.getRoot().getHeight() + (int)ViewUtils.dpToPixels(10));
        }
    }

    private PendingAcceptViewModel viewModel() {
        PendingAcceptViewModel viewModel = getViewModel();
        if (viewModel == null) {
            viewModel = obtainViewModel(PendingAcceptViewModel.class);
            setViewModel(viewModel);
        }
        return viewModel;
    }

}
