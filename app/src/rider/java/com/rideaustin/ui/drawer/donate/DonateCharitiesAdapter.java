package com.rideaustin.ui.drawer.donate;

import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableField;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rideaustin.R;
import com.rideaustin.api.model.Charity;
import com.rideaustin.api.model.Rider;
import com.rideaustin.databinding.ListItemDonateCharityBinding;
import com.rideaustin.utils.ImageHelper;

/**
 * Created by ysych on 29.06.2016.
 */
public class DonateCharitiesAdapter extends RecyclerView.Adapter<DonateCharitiesAdapter.BindingViewHolder> {

    private DonateFragmentViewModel viewModel;
    private ObservableArrayList<Charity> charities;
    private ObservableField<Rider> currentRider;

    public DonateCharitiesAdapter(DonateFragmentViewModel viewModel, ObservableArrayList<Charity> charities, ObservableField<Rider> currentRider) {
        this.viewModel = viewModel;
        this.charities = charities;
        this.currentRider = currentRider;
    }

    @Override
    public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ListItemDonateCharityBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.list_item_donate_charity, parent, false);
        binding.setCharityClickHandler(view -> {
            viewModel.updateRidersCharity(binding.getCharity());
            // RA-13055: rider can be not loaded yet
            if (currentRider.get() != null) {
                currentRider.get().setCharity(binding.getCharity());
                notifyDataSetChanged();
            }
            // or selection will be applied when update completed
        });
        return new BindingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(BindingViewHolder holder, final int position) {
        final Charity currentCharity = charities.get(position);
        ListItemDonateCharityBinding binding = holder.getBinding();
        binding.setCharity(currentCharity);
        binding.setRider(currentRider.get());
        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return charities == null ? 0 : charities.size();
    }

    @BindingAdapter("charityImageUrl")
    public static void loadImage(ImageView view, String imageUrl) {
        ImageHelper.loadImageIntoView(view, imageUrl);
    }

    public static class BindingViewHolder extends RecyclerView.ViewHolder {

        private ListItemDonateCharityBinding binding;

        public ListItemDonateCharityBinding getBinding() {
            return binding;
        }

        public BindingViewHolder(ListItemDonateCharityBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface DonationCharityAdapterHandler {
        void onCharityClick(View view);
    }

}