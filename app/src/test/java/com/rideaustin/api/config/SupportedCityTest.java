package com.rideaustin.api.config;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by crossover on 02/01/2017.
 */
public class SupportedCityTest {

    private static final long lastCompatibleVersion = 6855631824492187179L;

    private SupportedCity newCity;
    private com.rideaustin.api.config.v2_6_1.SupportedCity oldCity;

    @Before
    public void setUp() throws Exception {
        Gson gson = new Gson();

        oldCity = new com.rideaustin.api.config.v2_6_1.SupportedCity();
        oldCity.setCityId(34);
        oldCity.setCityName("Istanbul");
        oldCity.setLogoUrl("http://google.com");

        newCity = gson.fromJson(gson.toJson(oldCity), SupportedCity.class);

        assertNotNull(newCity);
    }


    @Test
    public void getCityId() throws Exception {
        assertEquals(oldCity.getCityId(), newCity.getCityId());
    }

    @Test
    public void getCityName() throws Exception {
        assertEquals(oldCity.getCityName(), newCity.getCityName());
    }

    @Test
    public void getLogoUrl() throws Exception {
        assertEquals(oldCity.getLogoUrl(), newCity.getLogoUrl());
    }

    @Test
    public void testSerialUID() throws Exception {
        assertEquals("Once you update config files, you also need to update tests and version number if it is not compatible",
                lastCompatibleVersion, SupportedCity.serialVersionUID);
    }
}