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
public class GlobalConfigTest {

    private static final long lastCompatibleVersion = -1238343532673324249L;

    private GlobalConfig newConfig;
    private com.rideaustin.api.config.v2_6_1.GlobalConfig oldConfig;

    @Before
    public void setUp() throws Exception {
        Gson gson = new Gson();

        oldConfig = new com.rideaustin.api.config.v2_6_1.GlobalConfig();
        oldConfig.setReferFriend(new com.rideaustin.api.config.v2_6_1.ReferFriend());
        oldConfig.setTipping(new com.rideaustin.api.config.v2_6_1.Tipping());
        oldConfig.setSmsMaskingEnabled(true);
        oldConfig.setSupportedCities(new ArrayList<>());
        oldConfig.setDirectConnectPhone("+1234567890");
        oldConfig.setGeocodingConfiguration(new com.rideaustin.api.config.v2_6_1.GeocodingConfiguration());
        oldConfig.setRideCancellation(new com.rideaustin.api.config.v2_6_1.RideCancellation());
        oldConfig.setCurrentCity(new com.rideaustin.api.config.v2_6_1.CurrentCity());
        oldConfig.setGeneralInformation(new com.rideaustin.api.config.v2_6_1.GeneralInformation());
        oldConfig.setWomanOnly(new com.rideaustin.api.config.v2_6_1.WomanOnly());
        oldConfig.setRiderReferFriend(new com.rideaustin.api.config.v2_6_1.RiderReferFriend());

        newConfig = gson.fromJson(gson.toJson(oldConfig), GlobalConfig.class);
        assertNotNull(newConfig);
    }

    @Test
    public void getReferFriend() throws Exception {
        assertNotNull(newConfig.getReferFriend());
    }

    @Test
    public void getTipping() throws Exception {
        assertNotNull(newConfig.getTipping());
    }

    @Test
    public void getAccessibility() throws Exception {
        //TODO: old api does not have this property, so it should pass);
    }

    @Test
    public void isSmsMaskingEnabled() throws Exception {
        assertEquals(oldConfig.isSmsMaskingEnabled(), newConfig.isSmsMaskingEnabled());
    }

    @Test
    public void getSupportedCities() throws Exception {
        assertNotNull(oldConfig.getSupportedCities());
    }

    @Test
    public void getDirectConnectPhone() throws Exception {
        assertEquals(oldConfig.getDirectConnectPhone(), newConfig.getDirectConnectPhone());
    }

    @Test
    public void getGeocodingConfiguration() throws Exception {
        assertNotNull(newConfig.getGeocodingConfiguration());
    }

    @Test
    public void getRideCancellation() throws Exception {
        assertNotNull(newConfig.getRideCancellation());
    }

    @Test
    public void getCurrentCity() throws Exception {
        assertNotNull(newConfig.getCurrentCity());
    }

    @Test
    public void getGeneralInformation() throws Exception {
        assertNotNull(newConfig.getGeneralInformation());
    }

    @Test
    public void getRides() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getRiderReferFriend() throws Exception {
        assertNotNull(newConfig.getRiderReferFriend());
    }

    @Test
    public void testSerialUID() throws Exception {
        assertEquals("Once you update config files, you also need to update tests and version number if it is not compatible",
                lastCompatibleVersion, GlobalConfig.getVersion());
    }
}