package com.rideaustin.utils;

import android.support.annotation.Nullable;

import java8.util.Optional;

/**
 * Created by Sergey Petrov on 29/06/2017.
 */

public class CachedValue<T> {

    private Optional<T> value = Optional.empty();

    private long expireAt = 0L;

    /**
     * Caches {@code value} for {@code expirationTime}
     * Returns {@code false} if cached value is the same as {@code value} and not yet expired
     * Returns {@code true} if value was cached.
     *
     * @param value value to cache
     * @param expirationTime expiration time in millis from now
     * @return whether value was cached
     */
    public synchronized boolean cache(@Nullable T value, long expirationTime) {
        if (isValuesEqual(value)) {
            // values are equal, check if cache expired
            if (TimeUtils.currentTimeMillis() < expireAt) {
                // cache is up-to-date
                return false;
            }
        }
        // cached value expired or changed
        setValue(value, expirationTime);
        return true;
    }

    public synchronized void reset() {
        value = Optional.empty();
        expireAt = 0L;
    }

    private void setValue(@Nullable T value, long expirationTime) {
        this.value = Optional.ofNullable(value);
        this.expireAt = TimeUtils.currentTimeMillis() + expirationTime;
    }

    private boolean isValuesEqual(@Nullable T value) {
        if (this.value.isPresent()) {
            return this.value.get().equals(value);
        } else {
            return value == null;
        }
    }

}
