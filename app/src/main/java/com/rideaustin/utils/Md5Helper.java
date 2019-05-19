package com.rideaustin.utils;

import com.rideaustin.BuildConfig;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Created by kshumelchyk on 7/5/16.
 */
public class Md5Helper {

    public static String calculateMd5Hash(String email, String password) {
        String message = email.toLowerCase() + BuildConfig.MD5_SAULT + password;
        // this a little bit different implementation than on server
        // but it's due to android limitation
        // for details see:
        // http://stackoverflow.com/questions/9126567/method-not-found-using-digestutils-in-android
        return new String(Hex.encodeHex(DigestUtils.md5(message)));
    }
}
