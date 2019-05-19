package com.rideaustin.ui.signup.driver;

import android.content.Context;

import com.rideaustin.R;
import com.rideaustin.api.model.driver.Car;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import rx.Observable;

/**
 * Created by rost on 8/10/16.
 */
public class VehicleColorInteractor implements SetupVehicleInteractor {
    private final Context context;
    private final Car car;

    public VehicleColorInteractor(Context context, Car car) {
        this.context = context;
        this.car = car;
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.title_driver_color);
    }

    @Override
    public void onListItemSelected(String value) {
        car.setColor(value);
    }

    @Override
    public Observable<Set<String>> getListItems() {
        final String[] colors = context.getResources().getStringArray(R.array.driver_vehicle_colors);
        final Set<String> result = new LinkedHashSet<>(Arrays.asList(colors));
        return Observable.just(result);
    }

    @Override
    public void clear() {
        car.setColor(null);
    }
}
