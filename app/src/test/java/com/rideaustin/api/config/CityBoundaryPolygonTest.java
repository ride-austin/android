package com.rideaustin.api.config;

import com.google.gson.Gson;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by crossover on 02/01/2017.
 */
public class CityBoundaryPolygonTest {

    private static final long lastCompatibleVersion = 48427693659515009L;

    private CityBoundaryPolygon newPolygon;
    private com.rideaustin.api.config.v2_6_1.CityBoundaryPolygon oldPolygon;

    @Before
    public void setUp() throws Exception {
        Gson gson = new Gson();

        oldPolygon = new com.rideaustin.api.config.v2_6_1.CityBoundaryPolygon();
        oldPolygon.setLat(90d);
        oldPolygon.setLng(90d);

        newPolygon = gson.fromJson(gson.toJson(oldPolygon), CityBoundaryPolygon.class);

        Assert.assertNotNull(newPolygon);
    }

    @Test
    public void getLat() throws Exception {
        Assert.assertEquals(oldPolygon.getLat(), newPolygon.getLat());
    }

    @Test
    public void getLng() throws Exception {
        Assert.assertEquals(oldPolygon.getLng(), newPolygon.getLng());
    }

    @Test
    public void testSerialUID() throws Exception {
        assertEquals("Once you update config files, you also need to update tests and version number if it is not compatible",
                lastCompatibleVersion, CityBoundaryPolygon.serialVersionUID);
    }
}