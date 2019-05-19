package com.rideaustin.api.config;

import com.google.gson.Gson;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Created by crossover on 02/01/2017.
 */
public class ConfigAppInfoResponseTest {

    private static final long lastCompatibleVersion = -1552922465746563282L;

    private ConfigAppInfoResponse newResponse;
    private com.rideaustin.api.config.v2_6_1.ConfigAppInfoResponse oldResponse;

    @Before
    public void setUp() throws Exception {
        Gson gson = new Gson();

        oldResponse = new com.rideaustin.api.config.v2_6_1.ConfigAppInfoResponse();

        oldResponse.setDownloadUrl("downloadUrl");
        oldResponse.setId(100);
        oldResponse.setMandatoryUpgrade(true);
        oldResponse.setPlatformType("Android");
        oldResponse.setUserAgentHeader("User-Agent");
        oldResponse.setUuid(UUID.randomUUID().toString());
        oldResponse.setVersion("Version");

        newResponse = gson.fromJson(gson.toJson(oldResponse), ConfigAppInfoResponse.class);

        Assert.assertNotNull(newResponse);
    }

    @Test
    public void getDownloadUrl() throws Exception {
        assertEquals(oldResponse.getDownloadUrl(), newResponse.getDownloadUrl());
    }

    @Test
    public void isMandatoryUpgrade() throws Exception {
        assertEquals(oldResponse.isMandatoryUpgrade(), newResponse.isMandatoryUpgrade());
    }

    @Test
    public void getUserAgentHeader() throws Exception {
        assertEquals(oldResponse.getUserAgentHeader(), newResponse.getUserAgentHeader());
    }

    @Test
    public void getVersion() throws Exception {
        assertEquals(oldResponse.getVersion(), newResponse.getVersion());
    }

    @Test
    public void testSerialUID() throws Exception {
        assertEquals("Once you update config files, you also need to update tests and version number if it is not compatible",
                lastCompatibleVersion, ConfigAppInfoResponse.serialVersionUID);
    }
}