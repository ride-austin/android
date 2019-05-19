package com.rideaustin.ui.drawer.dc;

import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.rideaustin.R;
import com.rideaustin.databinding.FragmentCarTypeItemBinding;
import com.rideaustin.ui.common.RecyclerViewOnItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hatak on 02.11.2017.
 */

public class CarCategoryAdapter extends RecyclerView.Adapter<CarCategoryAdapter.CarCategoryViewHolder> {

    @Nullable
    private final RecyclerViewOnItemClickListener onItemClickListener;
    private List<CarCategoryItem> items = new ArrayList<>();

    public CarCategoryAdapter(@Nullable RecyclerViewOnItemClickListener listener) {
        onItemClickListener = listener;
    }

    @Override
    public CarCategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FragmentCarTypeItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.fragment_car_type_item, parent, false);
        CarCategoryViewHolder viewHolder = new CarCategoryViewHolder(binding);
        viewHolder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onRecyclerViewItemClicked(viewHolder.getAdapterPosition(), v);
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CarCategoryViewHolder holder, int position) {
        holder.bindItem(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<CarCategoryItem> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    static class CarCategoryViewHolder extends RecyclerView.ViewHolder {
        final FragmentCarTypeItemBinding binding;

        CarCategoryViewHolder(final FragmentCarTypeItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bindItem(final CarCategoryItem item) {
            if (binding.getViewModel() == null) {
                CarCategoryItemViewModel viewModel = new CarCategoryItemViewModel();
                binding.setViewModel(viewModel);
                viewModel.setItem(item);
            } else {
                binding.getViewModel().setItem(item);
            }
            binding.executePendingBindings();
        }
    }
}
