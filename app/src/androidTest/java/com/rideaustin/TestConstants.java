package com.rideaustin;

import com.rideaustin.utils.ResourceUtils;

import junit.framework.Test;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Sergey Petrov on 16/08/2017.
 */

public final class TestConstants {

    public static final String ADMIN_USERNAME;
    public static final String ADMIN_PASSWORD;

    public static final String INACTIVE_RIDER_EMAIL;
    public static final String INACTIVE_RIDER_PASSWORD;
    public static final String INACTIVE_RIDER_NAME;

    public static final String ACTIVE_RIDER_EMAIL;
    public static final String ACTIVE_RIDER_PASSWORD;
    public static final String ACTIVE_RIDER_NAME;

    public static final String DISABLED_RIDER_EMAIL;
    public static final String DISABLED_RIDER_PASSWORD;

    public static final String FACEBOOK_EMAIL;
    public static final String FACEBOOK_PASSWORD;
    public static final String FACEBOOK_NAME;

    static {
        Properties props = new Properties();
        InputStream in = null;
        try {
            in = ResourceUtils.getContentAsStream(TestConstants.class, "e2e.properties");
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }

        ADMIN_USERNAME = props.getProperty("admin_username");
        ADMIN_PASSWORD = props.getProperty("admin_password");

        INACTIVE_RIDER_EMAIL = props.getProperty("inactive_rider_email");
        INACTIVE_RIDER_PASSWORD = props.getProperty("inactive_rider_password");
        INACTIVE_RIDER_NAME = props.getProperty("inactive_rider_name");

        ACTIVE_RIDER_EMAIL = props.getProperty("active_rider_email");
        ACTIVE_RIDER_PASSWORD = props.getProperty("active_rider_password");
        ACTIVE_RIDER_NAME = props.getProperty("active_rider_name");

        DISABLED_RIDER_EMAIL = props.getProperty("disabled_rider_email");
        DISABLED_RIDER_PASSWORD = props.getProperty("disabled_rider_password");

        FACEBOOK_EMAIL = props.getProperty("facebook_email");
        FACEBOOK_PASSWORD = props.getProperty("facebook_password");
        FACEBOOK_NAME = props.getProperty("facebook_name");
    }
}
