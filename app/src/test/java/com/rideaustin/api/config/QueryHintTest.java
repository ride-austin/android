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
public class QueryHintTest {

    private final static long lastCompatibleVersion = -3476512643361747519L;

    private QueryHint newHint;
    private com.rideaustin.api.config.v2_6_1.QueryHint oldHint;

    @Before
    public void setUp() throws Exception {
        Gson gson = new Gson();

        oldHint = new com.rideaustin.api.config.v2_6_1.QueryHint();
        oldHint.setContains(new ArrayList<>());
        oldHint.setDescription("Description");
        oldHint.setPrefixes(new ArrayList<>());
        oldHint.setPrimaryAddress("Primary Address");
        oldHint.setReference("Reference");
        oldHint.setSecondaryAddress("Secondary address");

        newHint = gson.fromJson(gson.toJson(oldHint), QueryHint.class);

        assertNotNull(newHint);
    }

    @Test
    public void getPrefixes() throws Exception {
        assertNotNull(newHint.getPrefixes());
    }

    @Test
    public void getContains() throws Exception {
        assertNotNull(newHint.getPrefixes());
    }

    @Test
    public void getDescription() throws Exception {
        assertEquals(oldHint.getDescription(), newHint.getDescription());
    }

    @Test
    public void getReference() throws Exception {
        assertEquals(oldHint.getReference(), newHint.getReference());
    }

    @Test
    public void getPrimaryAddress() throws Exception {
        assertEquals(oldHint.getPrimaryAddress(), newHint.getPrimaryAddress());
    }

    @Test
    public void getSecondaryAddress() throws Exception {
        assertEquals(oldHint.getSecondaryAddress(), newHint.getSecondaryAddress());
    }

    @Test
    public void testSerialUID() throws Exception {
        assertEquals("Once you update config files, you also need to update tests and version number if it is not compatible",
                lastCompatibleVersion, QueryHint.serialVersionUID);
    }
}