package com.rideaustin.ui.driver;

import android.databinding.DataBindingUtil;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rideaustin.R;
import com.rideaustin.databinding.DriverBinding;
import com.rideaustin.databinding.SpinnerItemBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by crossover on 18/11/2016.
 */

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityHolder> {

    private List<CityModel> cities = new ArrayList<>();

    private DriverBinding driverBinding;

    public CityAdapter(DriverBinding binding) {
        driverBinding = binding;
    }

    @Override
    public CityHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SpinnerItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.spinner_item, parent, false);
        return new CityHolder(binding);
    }

    @Override
    public void onBindViewHolder(CityHolder holder, int position) {
        holder.bind(cities.get(position));
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    @UiThread
    public void setCities(List<CityModel> cities) {
        this.cities = cities;
        notifyDataSetChanged();
    }

    public class CityHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private SpinnerItemBinding binding;

        private CityModel city;

        public SpinnerItemBinding getBinding() {
            return binding;
        }

        public CityHolder(SpinnerItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
        }

        public void bind(CityModel city) {
            this.city = city;
            binding.setCity(city);
        }

        @Override
        public void onClick(View view) {
            driverBinding.getViewModel().setSelectedCity(city);
            driverBinding.setSpinnerShown(false);
        }
    }
}
