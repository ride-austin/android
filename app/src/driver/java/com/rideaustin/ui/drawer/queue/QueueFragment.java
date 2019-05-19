package com.rideaustin.ui.drawer.queue;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rideaustin.R;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentQueueBinding;
import com.rideaustin.schedulers.RxSchedulers;

import java.util.List;

public class QueueFragment extends BaseFragment<QueueViewModel> {

    private FragmentQueueBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_queue, container, false);
        String queueName = ((QueueActivity) getActivity()).getQueueName();
        setViewModel(new QueueViewModel(queueName));
        binding.setViewModel(getViewModel());
        setupAirportQueueView(binding.queueRecyclerView);
        binding.swipeRefreshLayout.setOnRefreshListener(() -> getViewModel().refresh());
        binding.swipeRefreshLayout.setColorSchemeResources(
                R.color.blue_500,
                R.color.green_500,
                R.color.yellow_500);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        untilStop(getViewModel().getQueueEntriesEvents()
                .observeOn(RxSchedulers.main())
                .subscribe(this::onQueueChanged));
    }

    public void onQueueChanged(final List<QueueEntry> queueEntries) {
        QueueEntryAdapter adapter = (QueueEntryAdapter) binding.queueRecyclerView.getAdapter();
        adapter.setAirportQueueEntries(queueEntries);
        adapter.notifyDataSetChanged();
    }

    private void setupAirportQueueView(final RecyclerView recyclerView) {
        QueueEntryAdapter adapter = new QueueEntryAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }
}
