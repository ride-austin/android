package com.rideaustin.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import java.lang.reflect.Type;

import timber.log.Timber;

/**
 * Created by supreethks on 02/09/16.
 */
public class SerializationHelper {

    public static String serialize(@NonNull Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }

    @Nullable
    public static <T> T deSerialize(@NonNull String serialized, Class<T> type) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(serialized, type);
        } catch (Exception e) {
            Timber.e(e, "cannot deSerialize for class: %s the string: %s", type.getSimpleName(), serialized);
            return null;
        }
    }

    @Nullable
    public static <T> T deSerialize(@NonNull String serialized, Type type) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(serialized, type);
        } catch (Exception e) {
            Timber.e(e, "cannot deSerialize for type: %s the string: %s", type.getClass().getSimpleName(), serialized);
            return null;
        }
    }
}
