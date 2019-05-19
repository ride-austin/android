package com.rideaustin.utils;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.model.Boundary;
import com.rideaustin.api.model.CarTypeConfiguration;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.utils.geometry.Point;
import com.rideaustin.utils.geometry.Polygon;

import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;

import java8.util.Optional;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import rx.Observable;

/**
 * Created by hatak on 21.06.2017.
 */

public class CarCategoriesHelper {

    public static Observable<List<RequestedCarType>> fetchCarCategoriesAvailableAtLocation(LatLng latLng) {
        return App.getConfigurationManager().getConfigurationUpdates()
                .map(GlobalConfig::getCarTypes)
                .map(requestedCarTypes -> filterCategories(requestedCarTypes, latLng))
                .flatMap(CarCategoriesHelper::filterCarTypesForFingerprintedOnlyMode)
                .flatMap(CarCategoriesHelper::filterCarTypesForWomanOnlyMode);
    }

    private static List<RequestedCarType> filterCategories(final List<RequestedCarType> carTypes, final LatLng latLng) {
        return StreamSupport.stream(carTypes)
                .filter(carType -> isValidByGeometry(latLng, carType))
                .filter(CarCategoriesHelper::isValidByTimeConstraints)
                .sorted((lhs, rhs) -> lhs.getOrder() - rhs.getOrder())
                .collect(Collectors.toList());
    }

    private static boolean isValidByTimeConstraints(RequestedCarType carType) {
        return Optional.ofNullable(carType.getConfiguration())
                .map(CarTypeConfiguration::getAvailable)
                .map(available -> {
                    Interval safeRidesTimeInterval = DateHelper.getTimeIntervalFromServerFormat(available.getFrom(), available.getTo());
                    return safeRidesTimeInterval.contains(TimeUtils.currentTimeMillis());
                })
                .orElse(true);
    }

    private static Boolean isValidByGeometry(LatLng latLng, RequestedCarType carType) {
        return Optional.ofNullable(carType.getConfiguration())
                .map(CarTypeConfiguration::getAllowedPolygons)
                .map(namedPolygons ->
                        StreamSupport.stream(namedPolygons)
                                .anyMatch(namedPolygon -> {
                                    Polygon.Builder builder = Polygon.Builder();
                                    for (Boundary boundary : namedPolygon.getBoundary()) {
                                        builder.addVertex(new Point(boundary.getLng(), boundary.getLat()));
                                    }
                                    return builder.build().contains(new Point(latLng.longitude, latLng.latitude));
                                }))
                .orElse(true);
    }

    private static Observable<List<RequestedCarType>> filterCarTypesForFingerprintedOnlyMode(List<RequestedCarType> requestedCarTypes) {
        if (App.getDataManager().isFingerprintedOnlyEnabled()) {
            return App.getDataManager().getFingerprintedDriverType()
                    .map(driverType -> {
                        if (driverType.getEligibleCategories() == null) {
                            return requestedCarTypes;
                        } else {
                            List<RequestedCarType> availableCarTypes = new ArrayList<>();
                            for (RequestedCarType requestedCarType : requestedCarTypes) {
                                String carCategory = requestedCarType.getCarCategory();
                                if (driverType.getEligibleCategories().contains(carCategory)) {
                                    availableCarTypes.add(requestedCarType);
                                }
                            }
                            return availableCarTypes;
                        }
                    });
        } else {
            return Observable.just(requestedCarTypes);
        }
    }

    private static Observable<List<RequestedCarType>> filterCarTypesForWomanOnlyMode(List<RequestedCarType> requestedCarTypes) {
        return App.getDataManager().isFemaleOnlyAllowed()
                .switchMap(allowed -> {
                    if (!allowed) {
                        return Observable.just(requestedCarTypes);
                    } else {
                        return App.getDataManager().getFemaleDriverType()
                                .map(driverType -> {
                                    if (driverType.getEligibleCategories() == null) {
                                        return requestedCarTypes;
                                    } else {
                                        List<RequestedCarType> availableCarTypes = new ArrayList<>();
                                        for (RequestedCarType requestedCarType : requestedCarTypes) {
                                            String carCategory = requestedCarType.getCarCategory();
                                            if (driverType.getEligibleCategories().contains(carCategory)) {
                                                availableCarTypes.add(requestedCarType);
                                            }
                                        }
                                        return availableCarTypes;
                                    }
                                });
                    }
                });
    }
}
