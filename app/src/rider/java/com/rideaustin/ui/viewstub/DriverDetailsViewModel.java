package com.rideaustin.ui.viewstub;

import android.app.Activity;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.driver.ActiveDriver;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.databinding.DriverDetailsStubBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.RxBaseObservable;
import com.rideaustin.ui.utils.ImagePopupHelper;
import com.rideaustin.ui.utils.UIUtils;
import com.rideaustin.utils.ImageHelper;

import java.util.ArrayList;
import java.util.List;

import java8.util.Optional;

/**
 * Created by crossover on 23/12/2016.
 */

public class DriverDetailsViewModel extends RxBaseObservable {

    public final ObservableBoolean tripActivated = new ObservableBoolean(false);
    public final ObservableField<Long> freeCancellationExpiresOn = new ObservableField<>();
    public final ObservableBoolean isDeafDriver = new ObservableBoolean(false);

    private List<Target> glideTargets = new ArrayList<>();
    private ImagePopupHelper imagePopupHelper;
    private String cancellationFee;

    public void tripActive() {
        tripActivated.set(true);
    }


    void fillDriverDetailsView(ActiveDriver reachedDriver, DriverDetailsStubBinding binding, Activity activity) {
        if (reachedDriver == null) {
            return;
        }

        imagePopupHelper = new ImagePopupHelper(activity);

        Driver driver = reachedDriver.getDriver();
        isDeafDriver.set(driver.isDeaf());

        String driverName = driver.getUser().getNickName();
        if (TextUtils.isEmpty(driverName)) {
            driverName = driver.getFirstname();
        }
        Car selectedCar = reachedDriver.getSelectedCar();
        String selectedCarUrl = selectedCar.getPhotoUrl();
        final String photoURL;
        if (TextUtils.isEmpty(driver.getPhotoUrl())) {
            photoURL = driver.getUser().getPhotoUrl();
        } else {
            photoURL = driver.getPhotoUrl();
        }
        String rating = UIUtils.formatRating(driver.getRating());

        binding.tvDriverName.setText(driverName);
        binding.driverNameSmall.setText(driverName);

        String carLabel = selectedCar.getColor() + " " + selectedCar.getMake() + " " + selectedCar.getModel();
        binding.carColorMakeModel.setText(carLabel);
        binding.carMakeSmall.setText(carLabel);


        binding.carPlate.setText(selectedCar.getLicense());
        binding.carPlateSmall.setText(selectedCar.getLicense());

        addSubscription(App.getDataManager().getCurrentRideObservable()
                .observeOn(RxSchedulers.main())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .subscribe(ride -> {
                    setCategoryName(binding, ride.getRequestedCarType().getTitle());
                    cancellationFee = ride.getRequestedCarType().getCancellationFee();
                    freeCancellationExpiresOn.set(ride.getFreeCancellationExpiresOn());
                }));

        binding.tvDriverRate.setText(rating);
        binding.driverRateSmall.setText(rating);

        if (!TextUtils.isEmpty(selectedCarUrl)) {
            glideTargets.add(ImageHelper.cache(selectedCarUrl));
            binding.carImage.setOnClickListener(v -> imagePopupHelper.show(selectedCarUrl));
        }
        glideTargets.add(ImageHelper.loadRoundImageIntoView(binding.carImage, selectedCarUrl, R.drawable.icn_generic_car));


        if (!TextUtils.isEmpty(photoURL)) {
            glideTargets.add(ImageHelper.cache(photoURL));
            binding.driverImage.setOnClickListener(v -> imagePopupHelper.show(photoURL));
        }
        glideTargets.add(ImageHelper.loadRoundImageIntoView(binding.driverImage, photoURL, R.drawable.ic_user_icon));
        glideTargets.add(ImageHelper.loadRoundImageIntoView(binding.driverImageSmall, photoURL, R.drawable.ic_user_icon));
    }

    private void setCategoryName(DriverDetailsStubBinding binding, String carCategory) {
        binding.carCategory.setText(carCategory);
        binding.carCategorySmall.setText(carCategory);
    }

    public String getCancellationFee() {
        return cancellationFee;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (imagePopupHelper != null) {
            imagePopupHelper.hide();
        }
        for (Target glideTarget : glideTargets) {
            Glide.with(App.getInstance()).clear(glideTarget);
        }
        glideTargets.clear();
    }
}
