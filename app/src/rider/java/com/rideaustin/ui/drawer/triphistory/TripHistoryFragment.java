package com.rideaustin.ui.drawer.triphistory;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentTripHistoryBinding;
import com.rideaustin.ui.common.OnLoadMoreScrollListener;
import com.rideaustin.ui.common.SafeLinearLayoutManager;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.ui.widgets.ItemMarginDecoration;

public class TripHistoryFragment extends BaseFragment implements TripHistoryFragmentViewModel.View {

    private TripHistoryFragmentAdapter adapter;
    private TripHistoryFragmentViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setToolbarTitleAligned(R.string.trip_history_title, Gravity.LEFT);
        NavigationDrawerActivity.addRootFragmentClass(getClass().getName());

        FragmentTripHistoryBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_trip_history, container, false);
        RecyclerView recyclerView = binding.recyclerTripHistory;
        recyclerView.setLayoutManager(new SafeLinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new ItemMarginDecoration(getActivity().getResources().getDimensionPixelSize(R.dimen.trip_history_item_margin)));
        recyclerView.setHasFixedSize(true);

        viewModel = new TripHistoryFragmentViewModel(this);
        binding.setViewModel(viewModel);
        if (adapter == null) {
            adapter = new TripHistoryFragmentAdapter(getActivity());
        }
        recyclerView.addOnScrollListener(new OnLoadMoreScrollListener(viewModel, 1));
        recyclerView.setAdapter(adapter);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.onStart();
        adapter.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.onStop();
        adapter.onStop();
    }

    @Override
    public boolean onBackPressed() {
        App.getDataManager().setTripHistoryModel(null);
        App.getDataManager().setSupportTopicsModel(null);
        App.getDataManager().setSupportFormsModel(null);
        return super.onBackPressed();
    }

    @Override
    public void onUnexpectedState() {
        // TODO: Decide what to do here
        // If data lost on app restart, we would go to login recently
        // If no data because of illegal state, better to throw an Exception
    }

    @Override
    public void onHistorySelected() {
        TripDetailsFragment fragment = new TripDetailsFragment();
        ((NavigationDrawerActivity) getActivity()).replaceFragment(fragment, R.id.rootView, true);
    }
}
