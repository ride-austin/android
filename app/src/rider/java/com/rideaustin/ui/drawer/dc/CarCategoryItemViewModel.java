package com.rideaustin.ui.drawer.dc;

import android.databinding.ObservableField;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.rideaustin.R;
import com.rideaustin.utils.RxImageLoader;

/**
 * Created by hatak on 02.11.2017.
 */

public class CarCategoryItemViewModel {

    public final ObservableField<Drawable> carImage = new ObservableField<>();
    public final ObservableField<String> carCategory = new ObservableField<>();
    public final ObservableField<String> carCategorySeats = new ObservableField<>();
    public final ObservableField<String> carCategorySurgeFactor = new ObservableField<>();
    public final ObservableField<Integer> priorityIconVisibility = new ObservableField<>(View.INVISIBLE);
    public final ObservableField<Integer> selectedIconVisibility = new ObservableField<>(View.INVISIBLE);

    public void setItem(CarCategoryItem item) {
        RxImageLoader.execute(new RxImageLoader.Request(item.getCarType().getPlainIconUrl())
                .progress(R.drawable.rotating_circle)
                .error(R.drawable.icn_generic_car)
                .target(carImage));
        carCategory.set(item.getCarType().getTitle());
        carCategorySeats.set(item.getCarType().getMaxPersons() + " seats");
        if (item.isSelected()) {
            selectedIconVisibility.set(View.VISIBLE);
        } else {
            selectedIconVisibility.set(View.INVISIBLE);
        }
        carCategorySurgeFactor.set(item.getSurgeFactor() + "X");
        if (item.getSurgeFactor() > 1) {
            priorityIconVisibility.set(View.VISIBLE);
        } else {
            priorityIconVisibility.set(View.INVISIBLE);
        }
    }
}
