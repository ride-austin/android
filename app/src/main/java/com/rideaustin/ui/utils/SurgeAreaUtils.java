package com.rideaustin.ui.utils;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.api.model.surgearea.SurgeArea;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import java8.util.Optional;

/**
 * Created by RideDriver on 9/15/16.
 */
public final class SurgeAreaUtils {

    private SurgeAreaUtils() {
        //TODO do nothing
    }

    public static List<LatLng> parseCSVGeometry(String csvGeometry) {
        List<LatLng> result = new ArrayList<>();
        String[] geoPoints = csvGeometry.split(" ");
        for (String geo : geoPoints) {
            String latLng[] = geo.split(",");
            if (latLng.length < 1) {
                continue;
            }
            result.add(new LatLng(Double.valueOf(latLng[1]), Double.valueOf(latLng[0])));
        }
        return result;
    }

    public static int getSurgeAreaColor(float f) {
        return Color.argb(SurgeAreaUtils.getSurgeAreaColorAlphaChannel(f), 29, 169, 247);
    }

    public static int getSurgeAreaColorAlphaChannel(float f) {
        float alpha = 1.0f;
        if (f > 1.0 && f <= 1.25) {
            alpha = 0.20f;
        } else if (f > 1.25 && f <= 1.50) {
            alpha = 0.40f;
        } else if (f > 1.50 && f < 2.0) {
            alpha = 0.60f;
        } else if (f >= 2.0) {
            alpha = 0.80f;
        }
        return (int) (alpha * 255);
    }

    public static boolean isSurge(@NonNull SurgeArea surgeArea, String carCategory) {
        return getPriceFactor(surgeArea, carCategory) > 1f;
    }

    public static float getPriceFactor(@NonNull SurgeArea surgeArea, String carCategory) {
        HashMap<String, Float> priceFactors = surgeArea.getCarCategoriesFactors();
        if (priceFactors == null) {
            return 1f;
        }
        Float priceFactor = priceFactors.get(carCategory);
        if (priceFactor == null) {
            return 1f;
        }
        return priceFactor;
    }

    public static float getHighestFactor(@NonNull SurgeArea surgeArea) {
        HashMap<String, Float> priceFactors = surgeArea.getCarCategoriesFactors();
        if (priceFactors == null || priceFactors.isEmpty()) {
            return 1f;
        } else {
            return Collections.max(priceFactors.values());
        }
    }

    public static float getHighestFactor(@NonNull SurgeArea surgeArea, final @NonNull Set<String> categories) {
        HashMap<String, Float> priceFactors = surgeArea.getCarCategoriesFactors();
        if (priceFactors == null || priceFactors.isEmpty()) {
            return 1f;
        } else {
            return Collections.max(priceFactors.entrySet(), (lhs, rhs) -> {
                float lhsValue = categories.contains(lhs.getKey()) ? lhs.getValue() : 0f;
                float rhsValue = categories.contains(rhs.getKey()) ? rhs.getValue() : 0f;
                return Float.compare(lhsValue, rhsValue);
            }).getValue();
        }
    }

    public static boolean containsLocation(@NonNull Collection<SurgeArea> surgeAreas, @NonNull LatLng latLng) {
        return findByLocation(surgeAreas, latLng) != null;
    }

    @Nullable
    public static SurgeArea findByLocation(@NonNull Collection<SurgeArea> surgeAreas, @NonNull LatLng latLng) {
        for (SurgeArea surgeArea : surgeAreas) {
            if (surgeArea.contains(latLng)) {
                return surgeArea;
            }
        }
        return null;
    }
}
