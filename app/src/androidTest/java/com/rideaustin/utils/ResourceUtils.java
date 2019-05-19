package com.rideaustin.utils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by Sergey Petrov on 11/05/2017.
 */

public class ResourceUtils {

    public static String getContent(Class<?> host, String resourceName) {
        try {
            return IOUtils.toString(getContentAsStream(host, resourceName), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to get response for name: " + resourceName);
        }
    }

    public static InputStream getContentAsStream(Class<?> host, String resourceName) {
        return host.getClassLoader().getResourceAsStream(resourceName);
    }

}
