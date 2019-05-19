package com.rideaustin.ui.stats;

import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.rideaustin.R;
import com.rideaustin.api.model.DriverStat;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.databinding.ListitemStatsBinding;

import java.util.List;

import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by Sergey Petrov on 26/07/2017.
 */

public class StatsAdapter extends RecyclerView.Adapter<StatsAdapter.ViewHolder> {

    private List<DriverStat> stats;

    public void setStats(@Nullable List<DriverStat> stats) {
        this.stats = stats;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(DataBindingUtil.inflate(inflater, R.layout.listitem_stats, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.binding.setViewModel(stats.get(position));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return stats != null ? stats.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final ListitemStatsBinding binding;

        ViewHolder(ListitemStatsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
