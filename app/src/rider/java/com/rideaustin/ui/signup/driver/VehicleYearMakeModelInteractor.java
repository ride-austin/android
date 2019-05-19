package com.rideaustin.ui.signup.driver;

import android.content.Context;

import com.rideaustin.R;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.models.VehicleManager;
import com.rideaustin.models.VehicleModel;

import java.util.Set;

import rx.Observable;
import timber.log.Timber;

/**
 * Created by rost on 8/10/16.
 */
public class VehicleYearMakeModelInteractor implements SetupVehicleInteractor {
    private final VehicleManager manager;
    private final VehicleModel.FieldType myType;
    private final Context context;
    private final Car car;

    public VehicleYearMakeModelInteractor(Context context, VehicleModel.FieldType myType, VehicleManager manager, Car car) {
        this.context = context;
        this.myType = myType;
        this.manager = manager;
        this.car = car;
    }

    @Override
    public Observable<Set<String>> getListItems() {
        return manager.getVehiclesForCurrentFilterCriteria()
                .map(vehicleModels -> VehicleManager.extractUniqueValues(vehicleModels, myType));
    }

    @Override
    public String getTitle() {
        final int titleResId = getTitleResId(myType);
        return context.getString(titleResId);
    }

    @Override
    public void onListItemSelected(String value) {
        applyCarValue(car, myType, value);
        manager.applyFilter(myType, value);
    }

    @Override
    public void clear() {
        Timber.d("clear %s", myType);
        manager.cancelFilter(myType);
    }

    private int getTitleResId(VehicleModel.FieldType type) {
        switch (type) {
            case YEAR:
                return R.string.title_driver_year;
            case MAKE:
                return R.string.title_driver_make;
            case MODEL:
                return R.string.title_driver_model;
            default:
                throw new IllegalArgumentException("unknown filed type: " + type);
        }
    }

    private void applyCarValue(Car car, VehicleModel.FieldType type, String value) {
        switch (type) {
            case YEAR:
                car.setYear(value);
                break;
            case MAKE:
                car.setMake(value);
                break;
            case MODEL:
                car.setModel(value);
                break;
            default:
                throw new IllegalArgumentException("unknown filed type: " + type);
        }
    }

}
