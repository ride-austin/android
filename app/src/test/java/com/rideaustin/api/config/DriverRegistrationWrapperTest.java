package com.rideaustin.api.config;

import com.google.gson.Gson;
import com.rideaustin.api.config.v2_6_1.DriverRegistration;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by crossover on 02/01/2017.
 */
public class DriverRegistrationWrapperTest {

    private static final long lastCompatibleVersion = -8936284051755211888L;

    private DriverRegistrationWrapper newWrapper;
    private com.rideaustin.api.config.v2_6_1.DriverRegistrationWrapper oldWrapper;

    @Before
    public void setUp() throws Exception {
        Gson gson = new Gson();

        oldWrapper = new com.rideaustin.api.config.v2_6_1.DriverRegistrationWrapper();
        oldWrapper.setDriverRegistration(new DriverRegistration());

        newWrapper = gson.fromJson(gson.toJson(oldWrapper), DriverRegistrationWrapper.class);

        assertNotNull(newWrapper);
    }


    @Test
    public void getDriverRegistration() throws Exception {
        assertNotNull(newWrapper.getDriverRegistration());
    }


    @Test
    public void testSerialUID() throws Exception {
        assertEquals("Once you update config files, you also need to update tests and version number if it is not compatible",
                lastCompatibleVersion, DriverRegistrationWrapper.serialVersionUID);
    }
}