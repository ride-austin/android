package com.rideaustin.api.converter;

/**
 * Created by rost on 8/16/16.
 */
public interface Converter<R, V> {
    public R convert(V item);
}
