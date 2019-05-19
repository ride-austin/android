package com.rideaustin.api.config;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by crossover on 02/01/2017.
 */
public class AccessibilityTest {

    private static final long lastCompatibleVersion = 4311850825817581329L;

    //TODO: check version code on old api

    private Accessibility newAccessibility;

    @Before
    public void setUp() throws Exception {
        Gson gson = new Gson();
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getPhoneNumber() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getTitle() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getEnabled() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void testSerialUID() throws Exception {
        assertEquals("Once you update config files, you also need to update tests and version number if it is not compatible",
                lastCompatibleVersion, Accessibility.serialVersionUID);
    }
}