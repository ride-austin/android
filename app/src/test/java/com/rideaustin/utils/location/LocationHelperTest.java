package com.rideaustin.utils.location;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Sergey Petrov on 30/03/2017.
 */

public class LocationHelperTest {

    @Test
    public void shouldParseValidStringToList() {
        String str = "-97.72531500000002,30.327673 -97.74950899999999,30.334271 -97.74160774072271,30.358845066452204 -97.736312,30.378811 -97.71266800000001,30.347468 -97.72531500000002,30.327673";
        int size = 6;
        List<LatLng> list = LocationHelper.listFrom(str, false);
        assertNotNull(list);
        assertEquals(size, list.size());
        for (int i = 0; i < size; i++) {
            assertTrue(LocationHelper.isLocationValid(list.get(i)));
        }
    }

    @Test
    public void shouldReturnNullOnInvalidString() {
        String str = "This is invalid string with spaces";
        assertNull(LocationHelper.listFrom(str, false));
    }

}
