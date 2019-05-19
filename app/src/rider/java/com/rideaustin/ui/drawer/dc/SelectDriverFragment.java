package com.rideaustin.ui.drawer.dc;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rideaustin.R;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentSelectDriverBinding;
import com.rideaustin.ui.common.ViewModelsProvider;

/**
 * Created by hatak on 24.10.2017.
 */

public class SelectDriverFragment extends BaseFragment<SelectDriverViewModel> {

    private FragmentSelectDriverBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setToolbarTitleAligned(R.string.connect_driver_title, Gravity.LEFT);
        setViewModel(obtainViewModel(SelectDriverViewModel.class));
        getViewModel().setModel(ViewModelsProvider.of(DirectConnectActivity.class).get(DirectConnectViewModel.class));
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_driver, container, false);
        binding.setViewModel(getViewModel());
        return binding.getRoot();
    }
}
