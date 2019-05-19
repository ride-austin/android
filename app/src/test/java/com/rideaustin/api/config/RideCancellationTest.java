package com.rideaustin.api.config;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

/**
 * Created by crossover on 02/01/2017.
 */
public class RideCancellationTest {

    private static final long lastCompatibleVersion = -5388270240533193336L;

    private RideCancellation newCancel;
    private com.rideaustin.api.config.v2_6_1.RideCancellation oldCancel;

    @Before
    public void setUp() throws Exception {
        Gson gson = new Gson();

        oldCancel = new com.rideaustin.api.config.v2_6_1.RideCancellation();
        oldCancel.setRideCancellationPeriod(101);

        newCancel = gson.fromJson(gson.toJson(oldCancel), RideCancellation.class);

        assertNotNull(newCancel);
    }

    @Test
    public void getRideCancellationPeriod() throws Exception {
        assertEquals(oldCancel.getRideCancellationPeriod(), newCancel.getRideCancellationPeriod());
    }

    @Test
    public void testSerialUID() throws Exception {
        assertEquals("Once you update config files, you also need to update tests and version number if it is not compatible",
                lastCompatibleVersion, RideCancellation.serialVersionUID);
    }
}