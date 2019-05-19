package com.rideaustin.api.config;

import com.google.gson.Gson;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by crossover on 02/01/2017.
 */
public class TippingTest {

    private static final long lastCompatibleVersion = -966691874729452835L;

    private Tipping newTipping;
    private com.rideaustin.api.config.v2_6_1.Tipping oldTipping;

    @Before
    public void setUp() throws Exception {
        Gson gson = new Gson();

        oldTipping = new com.rideaustin.api.config.v2_6_1.Tipping();
        oldTipping.setEnabled(true);

        newTipping = gson.fromJson(gson.toJson(oldTipping), Tipping.class);

        assertNotNull(newTipping);
    }

    @Test
    public void getEnabled() throws Exception {
        assertEquals(oldTipping.getEnabled(), newTipping.getEnabled());
    }

    @Test
    public void testSerialUID() throws Exception {
        Assert.assertEquals("Once you update config files, you also need to update tests and version number if it is not compatible",
                lastCompatibleVersion, Tipping.serialVersionUID);
    }
}