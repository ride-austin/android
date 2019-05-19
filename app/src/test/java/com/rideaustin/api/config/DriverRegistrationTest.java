package com.rideaustin.api.config;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by crossover on 02/01/2017.
 */
public class DriverRegistrationTest {

    private static final long lastCompatibleVersion = 4345614264672576967L;

    private DriverRegistration newRegistration;
    private com.rideaustin.api.config.v2_6_1.DriverRegistration oldRegistration;

    @Before
    public void setUp() throws Exception {
        Gson gson = new Gson();

        oldRegistration = new com.rideaustin.api.config.v2_6_1.DriverRegistration();
        oldRegistration.setDescription("Description");
        oldRegistration.setRequirements(new ArrayList<>(0));

        newRegistration = gson.fromJson(gson.toJson(oldRegistration), DriverRegistration.class);

        assertNotNull(newRegistration);
    }

    @Test
    public void getRequirements() throws Exception {
        assertNotNull(newRegistration.getRequirements());
    }

    @Test
    public void getInspectionSticker() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getTncCard() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getDescription() throws Exception {
        assertEquals(oldRegistration.getDescription(), newRegistration.getDescription());
    }

    @Test
    public void getEnabled() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getMinCarYear() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void testSerialUID() throws Exception {
        assertEquals("Once you update config files, you also need to update tests and version number if it is not compatible",
                lastCompatibleVersion, DriverRegistration.serialVersionUID);
    }
}