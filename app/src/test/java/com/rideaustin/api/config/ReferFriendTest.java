package com.rideaustin.api.config;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by crossover on 02/01/2017.
 */
public class ReferFriendTest {

    private static final long lastCompatibleVersion = 7637700753852740708L;

    private ReferFriend newFriend;
    private com.rideaustin.api.config.v2_6_1.ReferFriend oldFriend;

    @Before
    public void setUp() throws Exception {
        Gson gson = new Gson();
        //TODO: old api does not have this property, so it should pass
        oldFriend = new com.rideaustin.api.config.v2_6_1.ReferFriend();
        oldFriend.setEmailEnabled(true);
        oldFriend.setSmsEnabled(true);
        oldFriend.setHeader("Header");
        oldFriend.setBody("Body");
        oldFriend.setTitle("Title");

        newFriend = gson.fromJson(gson.toJson(oldFriend), ReferFriend.class);

        assertNotNull(newFriend);
    }


    @Test
    public void getEmailEnabled() throws Exception {
        assertEquals(oldFriend.getEmailEnabled(), newFriend.getEmailEnabled());
    }

    @Test
    public void getSmsEnabled() throws Exception {
        assertEquals(oldFriend.getSmsEnabled(), newFriend.getSmsEnabled());
    }

    @Test
    public void getHeader() throws Exception {
        assertEquals(oldFriend.getHeader(), newFriend.getHeader());
    }

    @Test
    public void getTitle() throws Exception {
        assertEquals(oldFriend.getTitle(), newFriend.getTitle());
    }

    @Test
    public void getBody() throws Exception {
        assertEquals(oldFriend.getBody(), newFriend.getBody());
    }

    @Test
    public void testSerialUID() throws Exception {
        assertEquals("Once you update config files, you also need to update tests and version number if it is not compatible",
                lastCompatibleVersion, ReferFriend.serialVersionUID);
    }
}