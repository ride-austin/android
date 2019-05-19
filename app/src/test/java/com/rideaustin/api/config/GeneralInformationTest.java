package com.rideaustin.api.config;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by crossover on 02/01/2017.
 */
public class GeneralInformationTest {

    private static final long lastCompatibleVersion = -2536899520919963846L;

    private GeneralInformation newInformation;
    private com.rideaustin.api.config.v2_6_1.GeneralInformation oldInformation;

    @Before
    public void setUp() throws Exception {
        Gson gson = new Gson();

        oldInformation = new com.rideaustin.api.config.v2_6_1.GeneralInformation();
        oldInformation.setApplicationName("RideAustin");
        oldInformation.setApplicationNamePipe("Ride | Austin");
        oldInformation.setIconUrl("https://google.com");
        oldInformation.setLogoBlackUrl("https://google.com");
        oldInformation.setLogoUrl("https://rideaustin.com");
        oldInformation.setSplashUrl("https://ridehou.com");

        newInformation = gson.fromJson(gson.toJson(oldInformation), GeneralInformation.class);

        assertNotNull(newInformation);

    }

    @Test
    public void getSplashUrl() throws Exception {
        assertEquals(oldInformation.getSplashUrl(), newInformation.getSplashUrl());
    }

    @Test
    public void getCompanyDomain() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getPlayStoreLink() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getLogoUrl() throws Exception {
        assertEquals(oldInformation.getLogoUrl(), newInformation.getLogoUrl());
    }

    @Test
    public void getAppstoreLink() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getLegalRider() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getSupportEmail() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getFacebookUrl() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getLegalDriver() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getPlayStoreWeb() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getApplicationNamePipe() throws Exception {
        assertEquals(oldInformation.getApplicationNamePipe(), newInformation.getApplicationNamePipe());
    }

    @Test
    public void getFacebookUrlSchemeiOS() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getLogoBlackUrl() throws Exception {
        assertEquals(oldInformation.getLogoBlackUrl(), newInformation.getLogoBlackUrl());
    }

    @Test
    public void getIconUrl() throws Exception {
        assertEquals(oldInformation.getIconUrl(), newInformation.getIconUrl());
    }

    @Test
    public void getCompanyWebsite() throws Exception {
        //TODO: old api does not have this property, so it should pass
    }

    @Test
    public void getApplicationName() throws Exception {
        assertEquals(oldInformation.getApplicationName(), newInformation.getApplicationName());
    }

    @Test
    public void testSerialUID() throws Exception {
        assertEquals("Once you update config files, you also need to update tests and version number if it is not compatible",
                lastCompatibleVersion, GeneralInformation.serialVersionUID);
    }
}