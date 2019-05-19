package com.rideaustin.ui.drawer.cars;

import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.DriverRegistration;
import com.rideaustin.api.config.InspectionSticker;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.ui.common.RxBaseObservable;
import com.rideaustin.utils.CommonConstants;
import com.rideaustin.utils.ImageHelper;

/**
 * Created by crossover on 18/01/2017.
 */

public class CarViewModel extends RxBaseObservable {

    private ObservableField<Car> car = new ObservableField<>();
    private ObservableField<String> updateInspectionStickerText = new ObservableField<>();
    private ObservableInt updateInspectionStickerVisibility = new ObservableInt(View.GONE);
    private DriverRegistration registrationConfiguration;

    public CarViewModel(final DriverRegistration registrationConfiguration) {
        this.registrationConfiguration = registrationConfiguration;
    }

    public void setCar(Car car) {
        this.car.set(car);
        InspectionSticker inspectionSticker = registrationConfiguration.getInspectionSticker();
        Boolean enabled = inspectionSticker.getEnabled();
        if (enabled && Integer.parseInt(car.getYear()) <= inspectionSticker.getStickerRequiredYear()) {
            updateInspectionStickerVisibility.set(View.VISIBLE);
            updateInspectionStickerText.set(App.getInstance().getString(R.string.update_sticker,
                    inspectionSticker.getHeader()));
        } else {
            updateInspectionStickerVisibility.set(View.GONE);
        }

        notifyChange();
    }

    @Bindable
    public Car getCar() {
        return car.get();
    }

    @BindingAdapter("carPhoto")
    public static void loadCarPhoto(ImageView view, Car car) {
        int placeHolder = R.drawable.icn_car_regular;
        if (car.getCarCategories().contains(CommonConstants.CarCategory.HONDA)
                || car.getCarCategories().contains(CommonConstants.CarCategory.LUXURY)
                || car.getCarCategories().contains(CommonConstants.CarCategory.PREMIUM)) {
            placeHolder = R.drawable.icn_car_luxury;
        } else if (car.getCarCategories().contains(CommonConstants.CarCategory.SUV)) {
            placeHolder = R.drawable.icn_car_suv;
        }
        ImageHelper.loadRoundImageIntoView(view, car.getPhotoUrl(), placeHolder);
    }

    @NonNull
    public ObservableInt getUpdateInspectionStickerVisibility() {
        return updateInspectionStickerVisibility;
    }

    public ObservableField<String> getUpdateInspectionStickerText() {
        return updateInspectionStickerText;
    }
}
