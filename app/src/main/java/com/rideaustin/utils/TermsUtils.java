package com.rideaustin.utils;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.models.TermsResponse;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import okhttp3.HttpUrl;
import rx.Observable;
import timber.log.Timber;

/**
 * Created by Sergey Petrov on 24/05/2017.
 */

public class TermsUtils {

    private static Map<String, String> termsContent = new HashMap<>();

    /**
     * Load terms and conditions or return cached response.
     * You can use {@link #clear()} to remove all previously cached terms.
     * <p>
     * @return observable of {@link TermsResponse} type (use network thread to subscribe)
     */
    public static Observable<TermsResponse> obtainTermsAndConditions(@Nullable String url) {
        boolean isValid = url != null && HttpUrl.parse(url) != null;
        if (!isValid) {
            String error = App.getInstance().getString(R.string.terms_invalid);
            return Observable.just(TermsResponse.withError(error, false));
        }
        if (!TextUtils.isEmpty(termsContent.get(url))) {
            return Observable.just(TermsResponse.withTerms(termsContent.get(url)));
        }
        return Observable.fromCallable(() -> {
            InputStream in = null;
            try {
                URL termsUrl = new URL(url);
                in = termsUrl.openStream();
                // load file content as byte array and try fix encoding
                String terms = fixEncoding(IOUtils.toByteArray(in));
                // check if terms string is valid
                if (TextUtils.isEmpty(terms)) {
                    // show contact support message
                    String error = App.getInstance().getString(R.string.terms_invalid);
                    termsContent.remove(url);
                    return TermsResponse.withError(error, false);
                }
                // seems all ok, return success
                termsContent.put(url, terms);
                return TermsResponse.withTerms(terms);
            } catch (Exception e) {
                Timber.e(e, "Unable to load terms and conditions");
                String error = App.getInstance().getString(R.string.terms_load_error);
                termsContent.remove(url);
                return TermsResponse.withError(error, true);
            } finally {
                IOUtils.closeQuietly(in);
            }
        });
    }

    public static void clear() {
        termsContent.clear();
    }

    // RA-6142
    // Temp solution, while terms encoding varies
    // Austin - UTF-8
    // Houston - Windows-1252
    @Nullable
    private static String fixEncoding(byte[] bytes) {
        try {
            if (!validUTF8(bytes)) {
                return new String(bytes, "Windows-1252");
            }
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Impossible, throw unchecked
            Timber.e(e, "Terms are not UTF-8 nor Windows-1252");
            return null;
        }
    }

    // RA-6142
    // Temp solution, while terms encoding varies
    // Austin - UTF-8
    // Houston - Windows-1252
    // http://stackoverflow.com/a/1447720
    private static boolean validUTF8(byte[] input) {
        int i = 0;
        // Check for BOM
        if (input.length >= 3 && (input[0] & 0xFF) == 0xEF
                && (input[1] & 0xFF) == 0xBB & (input[2] & 0xFF) == 0xBF) {
            i = 3;
        }

        int end;
        for (int j = input.length; i < j; ++i) {
            int octet = input[i];
            if ((octet & 0x80) == 0) {
                continue; // ASCII
            }

            // Check for UTF-8 leading byte
            if ((octet & 0xE0) == 0xC0) {
                end = i + 1;
            } else if ((octet & 0xF0) == 0xE0) {
                end = i + 2;
            } else if ((octet & 0xF8) == 0xF0) {
                end = i + 3;
            } else {
                // Java only supports BMP so 3 is max
                return false;
            }

            while (i < end) {
                i++;
                octet = input[i];
                if ((octet & 0xC0) != 0x80) {
                    // Not a valid trailing byte
                    return false;
                }
            }
        }
        return true;
    }
}
