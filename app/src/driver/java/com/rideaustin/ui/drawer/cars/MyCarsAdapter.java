package com.rideaustin.ui.drawer.cars;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.rideaustin.R;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.databinding.CarItemBinding;

public class MyCarsAdapter extends RecyclerView.Adapter<MyCarsAdapter.BindingViewHolder> {

    private MyCarsViewModel viewModel;

    @NonNull
    private final ObservableArrayList<Car> cars;

    public MyCarsAdapter(MyCarsViewModel viewModel) {
        this.viewModel = viewModel;
        this.cars = viewModel.getCars();
    }

    @Override
    public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CarItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.layout_card_car, parent, false);
        binding.setMyCarsViewModel(viewModel);
        binding.setCarViewModel(new CarViewModel(viewModel.getRegistrationConfiguration()));
        return new BindingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(BindingViewHolder holder, final int position) {
        final Car car = cars.get(position);
        CarItemBinding binding = holder.getBinding();
        binding.getCarViewModel().setCar(car);
        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return cars.size();
    }

    public static class BindingViewHolder extends RecyclerView.ViewHolder {

        private CarItemBinding binding;

        public CarItemBinding getBinding() {
            return binding;
        }

        public BindingViewHolder(CarItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}