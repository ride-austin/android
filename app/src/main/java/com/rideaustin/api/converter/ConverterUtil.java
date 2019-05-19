package com.rideaustin.api.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by rost on 8/16/16.
 */
public final class ConverterUtil {
    public ConverterUtil() {
    }

    // it can be Generalized to:
    // public <R, V> List<R> convertToList(Collection<? extends V> data, Converter<? extends R, ? super V> converter)
    // but lets keep simple variant
    // as long as we don't need more general
    public static <R, V> List<R> convertToList(Collection<V> data, Converter<R, V> converter) {
        List<R> result = new ArrayList<>(data.size());
        for (V item : data) {
            result.add(converter.convert(item));
        }
        return result;
    }

}
