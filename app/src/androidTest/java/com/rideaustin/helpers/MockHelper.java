package com.rideaustin.helpers;

import com.rideaustin.MockDelegate;
import com.rideaustin.RequestType;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.driver.ActiveDriver;

import java.lang.reflect.Field;

/**
 * Created on 27/02/2018
 *
 * @author sdelaysam
 */

public class MockHelper {

    public static ActiveDriver getActiveDriverWithRide(RequestType requestType, Ride ride, MockDelegate mockDelegate) {
        ActiveDriver activeDriver = mockDelegate.getResponse(getResponseName(requestType), ActiveDriver.class);
        try {
            // ride in ActiveDriver contains only certain limited number of fields
            // must override only those which are not null
            rewriteNonNulls(ride, activeDriver.getRide(), Ride.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return activeDriver;
    }

    /**
     * Copy all values from {@code source} to {@code target} which are present (not null) in {@code target}
     * If value is {@code NULL} if source - it will be copied
     */
    private static void rewriteNonNulls(Object source, Object target, Class<?> clazz) throws IllegalAccessException {
        if (source != null && target != null) {
            for (Field field : clazz.getDeclaredFields()) {
                Class fieldType = field.getType();
                field.setAccessible(true);
                Object toValue = field.get(target);
                if (toValue != null) {
                    if (isBuiltIn(fieldType)) {
                        field.set(target, field.get(source));
                    } else {
                        rewriteNonNulls(field.get(source), field.get(target), fieldType);
                    }
                }
            }
        }
    }

    private static String getResponseName(RequestType requestType) {
        switch (requestType) {
            case ACTIVE_DRIVER_ASSIGNED_200_GET: return "ACTIVE_DRIVER_ASSIGNED_200";
            case ACTIVE_DRIVER_REACHED_200_GET: return "ACTIVE_DRIVER_REACHED_200";
            case ACTIVE_DRIVER_ACTIVE_RIDE_200_GET: return "ACTIVE_DRIVER_ACTIVE_RIDE_200";
            default:
                throw new IllegalArgumentException("Not implemented for type: " + requestType);
        }
    }

    private static boolean isBuiltIn(Class clazz) {
        return clazz.getName().startsWith("java.lang");
    }
}
