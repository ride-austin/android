package com.rideaustin.utils;

import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by sergthedeveloper on 05/04/2017.
 */

public class ListUtils {

    /**
     * Remove null elements from <code>list</code>
     * Substitution for the following approach, which has n^2 complexity:
     * <pre>
     * {@code
     * list.removeAll(Collections.<T>singleton(null));
     * }
     * </pre>
     *
     * @param list list to remove null elements
     * @param <T>
     */
    public static <T> void removeNullElements(List<T> list) {
        Iterator<T> iterator = list.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() == null) {
                iterator.remove();
            }
        }
    }

    public static boolean isEmpty(@Nullable Collection c) {
        return c == null || c.size() == 0;
    }
}
