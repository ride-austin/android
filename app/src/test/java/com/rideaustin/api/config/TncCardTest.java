package com.rideaustin.api.config;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by crossover on 02/01/2017.
 */
public class TncCardTest {

    private static final long lastCompatibleVersion = -5152090729721720952L;

    @Before
    public void setUp() throws Exception {
        Gson gson = new Gson();
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getEnabled() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getHeader() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getTitle1() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getAction1() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getText1() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getTitle2() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getText2() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getBackPhotoEnabled() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void testSerialUID() throws Exception {
        assertEquals("Once you update config files, you also need to update tests and version number if it is not compatible",
                lastCompatibleVersion, TncCard.serialVersionUID);
    }
}