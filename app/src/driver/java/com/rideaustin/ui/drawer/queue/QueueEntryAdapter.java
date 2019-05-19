package com.rideaustin.ui.drawer.queue;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.rideaustin.R;
import com.rideaustin.databinding.QueueEntryBinding;

import java.util.Collections;
import java.util.List;

/**
 * Created by hatak on 14.10.16.
 */

public class QueueEntryAdapter extends RecyclerView.Adapter<QueueEntryAdapter.AirportQueueEntryViewHolder>{

    private List<QueueEntry> airportQueueEntries;

    public QueueEntryAdapter(){
        this.airportQueueEntries = Collections.emptyList();
    }

    public void setAirportQueueEntries(final List<QueueEntry> airportQueueEntries){
        this.airportQueueEntries = airportQueueEntries;
    }

    @Override
    public AirportQueueEntryViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        QueueEntryBinding binding = DataBindingUtil.inflate(inflater, R.layout.queue_entry,
                parent, false);
        return new AirportQueueEntryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final AirportQueueEntryViewHolder holder, final int position) {
        holder.bindEntry(airportQueueEntries.get(position));
    }

    @Override
    public int getItemCount() {
        return airportQueueEntries.size();
    }

    static class AirportQueueEntryViewHolder extends RecyclerView.ViewHolder {

        final QueueEntryBinding binding;

        AirportQueueEntryViewHolder(final QueueEntryBinding binding) {
            super(binding.entryView);
            this.binding = binding;
        }
        void bindEntry(final QueueEntry entry){
            if (binding.getViewModel() == null) {
                binding.setViewModel(new QueueEntryViewModel());
            }
            binding.getViewModel().setEntry(entry);
            binding.executePendingBindings();
        }
    }
}
