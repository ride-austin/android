package com.rideaustin.ui.drawer.cars;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.rideaustin.R;
import com.rideaustin.api.config.DriverRegistration;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.databinding.ActivityMyCarsBinding;
import com.rideaustin.ui.base.EngineStatelessActivity;
import com.rideaustin.ui.drawer.cars.add.AddCarActivity;
import com.rideaustin.ui.drawer.cars.insurance.UpdateInsuranceActivity;
import com.rideaustin.ui.drawer.cars.photos.DisplayPhotosActivity;
import com.rideaustin.ui.drawer.cars.sticker.UpdateStickerActivity;

public class MyCarsActivity extends EngineStatelessActivity implements MyCarsView {

    private ActivityMyCarsBinding binding;
    private MyCarsViewModel viewModel;
    private MyCarsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_cars);
        binding.title.setText(R.string.my_cars);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        viewModel = new MyCarsViewModel(this);
        binding.setViewModel(viewModel);

        adapter = new MyCarsAdapter(viewModel);
        binding.contentMyCars.setHasFixedSize(true);
        binding.contentMyCars.setAdapter(adapter);
        binding.addCar.setOnClickListener(v -> startActivity(new Intent(this, AddCarActivity.class)));
    }


    @Override
    public void onStart() {
        super.onStart();
        viewModel.setCallback(this);
        viewModel.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.setCallback(null);
        viewModel.onStop();
    }

    @Override
    public BaseActivityCallback getCallback() {
        return this;
    }

    @Override
    public void onCarsLoaded() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCarSelectionChanged() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onUpdateInsuranceClicked(Car car) {
        startActivity(UpdateInsuranceActivity.newIntent(this, car));
    }

    @Override
    public void onUpdatePhotosClicked(Car car) {
        startActivity(DisplayPhotosActivity.newIntent(this, car));
    }

    @Override
    public void onUpdateInspectionStickerClicked(DriverRegistration registrationConfiguration, Car car) {
        startActivity(UpdateStickerActivity.newIntent(this, registrationConfiguration, car));
    }

    @Override
    public void onCarsLoadingFailed() {
        finish();
    }
}