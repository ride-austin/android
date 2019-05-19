package com.rideaustin.ui.drawer.promotions.details;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.rideaustin.R;
import com.rideaustin.api.model.promocode.PromoCode;
import com.rideaustin.databinding.CreditDetailLayoutBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hatak on 04.09.2017.
 */

public class CreditDetailsAdapter extends RecyclerView.Adapter<CreditDetailsAdapter.PromoCodeViewHolder> {

    private final List<PromoCode> promoCodes = new ArrayList<>();

    @Override
    public PromoCodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CreditDetailLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.credit_detail_layout, parent, false);
        return new PromoCodeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(PromoCodeViewHolder holder, int position) {
        holder.bind(promoCodes.get(position));
    }

    @Override
    public int getItemCount() {
        return promoCodes.size();
    }

    public void setData(List<PromoCode> promoCodes) {
        this.promoCodes.clear();
        this.promoCodes.addAll(promoCodes);
        notifyDataSetChanged();
    }

    public static class PromoCodeViewHolder extends RecyclerView.ViewHolder {

        private CreditDetailLayoutBinding binding;

        public PromoCodeViewHolder(CreditDetailLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final PromoCode promoCode) {
            if (binding.getViewModel() == null) {
                CreditDetailViewModel viewModel = new CreditDetailViewModel();
                viewModel.setCreditDetail(promoCode);
                binding.setViewModel(viewModel);
            } else {
                binding.getViewModel().setCreditDetail(promoCode);
                binding.executePendingBindings();
            }
        }
    }
}
