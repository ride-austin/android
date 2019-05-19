package com.rideaustin.ui.drawer.donate;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.rideaustin.R;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentDonateBinding;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;

import timber.log.Timber;

public class DonateFragment extends BaseFragment implements DonateView {

    private DonateFragmentViewModel viewModel;
    private LinearLayout linearLayoutCharities;
    private DonateCharitiesAdapter adapter;
    private SwitchCompat switchCompat;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setToolbarTitleAligned(R.string.round_up, Gravity.LEFT);
        NavigationDrawerActivity.addRootFragmentClass(this.getClass().getName());

        FragmentDonateBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_donate, container, false);
        viewModel = new DonateFragmentViewModel(this);
        binding.setViewModel(viewModel);
        linearLayoutCharities = binding.charities;
        RecyclerView recyclerView = binding.recyclerRoundUpFare;
        switchCompat = binding.switchToggle;
        switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Timber.d("::onCheckedChanged:: isChecked: %s", isChecked);
            linearLayoutCharities.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) {
                viewModel.updateRidersCharity(null);
            }
        });
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        adapter = new DonateCharitiesAdapter(viewModel, viewModel.getCharities(), viewModel.getRider());
        recyclerView.setAdapter(adapter);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.setCallback(getCallback());
        viewModel.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.setCallback(null);
        viewModel.onStop();
    }

    @Override
    public void onCharitiesUpdated() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRiderUpdated() {
        switchCompat.setChecked(viewModel.getRider().get().getCharity() != null);
        adapter.notifyDataSetChanged();
    }
}