package com.rideaustin.ui.drawer.cars.photos;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.databinding.DisplayPhotosBinding;
import com.rideaustin.ui.base.EngineStatelessActivity;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.ImageHelper;
import com.rideaustin.utils.toast.RAToast;

public class DisplayPhotosActivity extends EngineStatelessActivity implements DisplayPhotosView {

    private static final int REQUEST_UPLOAD = 10101;

    private static final int FRONT = 0, BACK = 1, INSIDE = 2, TRUNK = 3, COUNT = 4;

    private DisplayPhotosViewModel viewModel;
    private DisplayPhotosBinding binding;

    private final Target[] imageTargets = new Target[COUNT];

    private static final String KEY_CAR = "KEY_CAR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_display_photos);
        setToolbar(binding.toolbar);
        setToolbarTitle(R.string.title_driver_vehicle_information);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        binding.updateBack.setOnClickListener(v -> openUpdateFragment(Constants.CarPhotoType.BACK));
        binding.updateFront.setOnClickListener(v -> openUpdateFragment(Constants.CarPhotoType.FRONT));
        binding.updateTrunk.setOnClickListener(v -> openUpdateFragment(Constants.CarPhotoType.TRUNK));
        binding.updateInside.setOnClickListener(v -> openUpdateFragment(Constants.CarPhotoType.INSIDE));
        viewModel = new DisplayPhotosViewModel(this, getCar());
    }

    public Car getCar() {
        return (Car) getIntent().getSerializableExtra(KEY_CAR);
    }

    public static Intent newIntent(Context context, Car car) {
        return new Intent(context, DisplayPhotosActivity.class).putExtra(KEY_CAR, car);
    }

    public void openUpdateFragment(@Constants.CarPhotoType String carPhotoType) {
        startActivityForResult(UpdatePhotoActivity.newInstance(this, viewModel.getCar(), carPhotoType), REQUEST_UPLOAD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_UPLOAD && resultCode == RESULT_OK) {
            viewModel.setReloadRequired();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.onStop();
        for (Target target : imageTargets) {
            if (target != null) {
                Glide.with(App.getInstance()).clear(target);
            }
        }
    }

    @Override
    public void onPhotosDownloadFailed() {
        RAToast.show(R.string.failed_photos_fetch, Toast.LENGTH_SHORT);
    }

    @Override
    public void onPhotosDownloaded(@Nullable String front, @Nullable String back, @Nullable String inside, @Nullable String trunk) {
        imageTargets[FRONT] = ImageHelper.loadImageIntoViewWithDefaultProgress(binding.imageFront, front, R.drawable.icon_car_front);
        imageTargets[BACK] = ImageHelper.loadImageIntoViewWithDefaultProgress(binding.imageBack, back, R.drawable.icon_car_back);
        imageTargets[INSIDE] = ImageHelper.loadImageIntoViewWithDefaultProgress(binding.imageInside, inside, R.drawable.icon_car_inside);
        imageTargets[TRUNK] = ImageHelper.loadImageIntoViewWithDefaultProgress(binding.imageTrunk, trunk, R.drawable.icon_car_trunk);
    }
}
