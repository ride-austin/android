package com.rideaustin.api.config;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by crossover on 02/01/2017.
 */
public class CurrentCityTest {

    private static final long lastCompatibleVersion = -3815516009893710520L;

    private CurrentCity newCity;
    private com.rideaustin.api.config.v2_6_1.CurrentCity oldCity;

    @Before
    public void setUp() throws Exception {
        Gson gson = new Gson();

        oldCity = new com.rideaustin.api.config.v2_6_1.CurrentCity();
        oldCity.setCityId(34);
        oldCity.setCityName("Istanbul");
        List<com.rideaustin.api.config.v2_6_1.CityBoundaryPolygon> oldPolygonList = new ArrayList<>(0);
        oldCity.setCityBoundaryPolygon(oldPolygonList);
        oldCity.setCityCenterLocation("36, 42");

        newCity = gson.fromJson(gson.toJson(oldCity), CurrentCity.class);

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

    //TODO: we can remove this test as it is testing against a deprecated method.
    @Test
    public void getCityCenterLocation() throws Exception {
        assertEquals(oldCity.getCityCenterLocation(), newCity.getCityCenterLocation());
    }

    @Test
    public void getCityCenterLocationData() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getCityBoundaryPolygon() throws Exception {
        assertNotNull(newCity.getCityBoundaryPolygon());
    }

    @Test
    public void testSerialUID() throws Exception {
        assertEquals("Once you update config files, you also need to update tests and version number if it is not compatible",
                lastCompatibleVersion, CurrentCity.serialVersionUID);
    }
}