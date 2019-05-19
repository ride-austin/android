package com.rideaustin.api.config;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by crossover on 02/01/2017.
 */
public class RiderReferFriendTest {

    private static final long lastCompatibleVersion = 6227540503687335256L;

    private RiderReferFriend newFriend;
    private com.rideaustin.api.config.v2_6_1.RiderReferFriend oldFriend;

    @Before
    public void setUp() throws Exception {
        Gson gson = new Gson();

        oldFriend = new com.rideaustin.api.config.v2_6_1.RiderReferFriend();
        oldFriend.setDetailtexttemplate("Detail Text");
        oldFriend.setDownloadUrl("http://download.com");
        oldFriend.setEmailbodytemplate("Email Body");
        oldFriend.setSmsbodytemplate("Sms Body");

        newFriend = gson.fromJson(gson.toJson(oldFriend), RiderReferFriend.class);

        assertNotNull(newFriend);
    }

    @Test
    public void getDetailtexttemplate() throws Exception {
        assertEquals(oldFriend.getDetailtexttemplate(), newFriend.getDetailtexttemplate());
    }

    @Test
    public void getEmailbodytemplate() throws Exception {
        assertEquals(oldFriend.getEmailbodytemplate(), newFriend.getEmailbodytemplate());
    }

    @Test
    public void getDownloadUrl() throws Exception {
        assertEquals(oldFriend.getDownloadUrl(), newFriend.getDownloadUrl());
    }

    @Test
    public void getSmsbodytemplate() throws Exception {
        assertEquals(oldFriend.getSmsbodytemplate(), newFriend.getSmsbodytemplate());
    }

    @Test
    public void testSerialUID() throws Exception {
        assertEquals("Once you update config files, you also need to update tests and version number if it is not compatible",
                lastCompatibleVersion, RiderReferFriend.serialVersionUID);
    }
}