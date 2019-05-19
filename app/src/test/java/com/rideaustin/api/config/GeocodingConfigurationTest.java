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
public class GeocodingConfigurationTest {

    private static final long lastCompatibleVersion = -3754378362191765435L;

    private GeocodingConfiguration newConfig;
    private com.rideaustin.api.config.v2_6_1.GeocodingConfiguration oldConfig;

    @Before
    public void setUp() throws Exception {
        Gson gson = new Gson();

        oldConfig = new com.rideaustin.api.config.v2_6_1.GeocodingConfiguration();
        oldConfig.setQueryHints(new ArrayList<>(0));

        newConfig = gson.fromJson(gson.toJson(oldConfig), GeocodingConfiguration.class);

        assertNotNull(newConfig);
    }

    @Test
    public void getQueryHints() throws Exception {
        assertNotNull(newConfig.getQueryHints());
    }

    @Test
    public void testSerialUID() throws Exception {
        assertEquals("Once you update config files, you also need to update tests and version number if it is not compatible",
                lastCompatibleVersion, GeocodingConfiguration.serialVersionUID);
    }
}