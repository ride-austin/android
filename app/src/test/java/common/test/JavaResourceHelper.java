package common.test;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * Created by Viktor Kifer
 * On 13-Jan-2017.
 */

public class JavaResourceHelper {

    public static String getResourceAsString(String resourceName) throws IOException {
        return IOUtils.toString(ClassLoader.getSystemResourceAsStream(resourceName));
    }

}
