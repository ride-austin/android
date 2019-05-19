package com.rideaustin.models;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.rideaustin.schedulers.RxSchedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.Observable;
import timber.log.Timber;

/**
 * Created by rost on 8/10/16.
 */
public class VehicleManager {
    public interface VehicleDataProvider {
        Observable<List<VehicleModel>> loadVehicles();
    }

    private final Observable<List<VehicleModel>> vehicles;
    private final Map<VehicleModel.FieldType, String> filters
            = new HashMap<>();

    public VehicleManager(VehicleDataProvider dataProvider) {
        vehicles = dataProvider.loadVehicles().subscribeOn(RxSchedulers.network()).cache();
    }

    public void applyFilter(VehicleModel.FieldType type, @NonNull String value) {
        synchronized (filters) {
            filters.put(type, value);
            Timber.d("applyFilter: applying %s current filters: %s", type, format(filters.keySet()));
        }
    }

    public void cancelFilter(VehicleModel.FieldType type) {
        synchronized (filters) {
            filters.remove(type);

            Timber.d("cancelFilter: canceling %s current filters: %s", type, format(filters.keySet()));
        }
    }

    public List<String> getFilters() {
        synchronized (filters) {
            return new ArrayList<>(filters.values());
        }
    }

    public String getFilter(VehicleModel.FieldType type) {
        synchronized (filters) {
            String res = filters.get(type);
            return (TextUtils.isEmpty(res) ? "" : res);
        }
    }

    public void clearFilters() {
        synchronized (filters) {
            filters.clear();
        }
    }

    public Observable<List<VehicleModel>> getVehiclesForCurrentFilterCriteria() {
        return vehicles.map(vehicleModels -> {
            Timber.d("load: current filters: %s", format(filters.keySet()));
            final List<VehicleModel> result = new ArrayList<>();
            for (VehicleModel vehicle : vehicleModels) {
                if (isMatchedAllCriteria(vehicle)) {
                    result.add(vehicle);
                }
            }
            Timber.d("load: result count %d", result.size());
            return result;
        }).observeOn(RxSchedulers.main());
    }

    private static String format(Set<?> items) {
        StringBuilder builder = new StringBuilder();
        for (Object string : items) {
            builder.append(string + " ");
        }
        return builder.toString();
    }

    public static Set<String> extractUniqueValues(List<VehicleModel> vehicleModelList, VehicleModel.FieldType type) {
        final List<String> result = new ArrayList<>();
        for (VehicleModel vehicle : vehicleModelList) {
            result.add(extractValue(vehicle, type));
        }
        if (type == VehicleModel.FieldType.YEAR) {
            Collections.sort(result, Collections.reverseOrder());
        } else {
            Collections.sort(result);
        }
        return new LinkedHashSet<>(result);
    }

    public static String extractValue(VehicleModel vehicle, VehicleModel.FieldType type) {
        switch (type) {
            case YEAR:
                return vehicle.getYear();
            case MAKE:
                return vehicle.getMake();
            case MODEL:
                return vehicle.getModel();
            default:
                throw new IllegalArgumentException("unknown field type " + type);
        }
    }

    private boolean isMatchedAllCriteria(VehicleModel vehicle) {
        boolean isMatched = true;
        synchronized (filters) {
            for (VehicleModel.FieldType type : filters.keySet()) {
                final String expectedValue = filters.get(type);
                isMatched = isMatched && isMatched(vehicle, type, expectedValue);
            }
        }
        return isMatched;
    }

    private boolean isMatched(VehicleModel vehicle, VehicleModel.FieldType type, String value) {
        return value.equals(extractValue(vehicle, type));
    }
}
