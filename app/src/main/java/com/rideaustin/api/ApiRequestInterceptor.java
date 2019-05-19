package com.rideaustin.api;

import android.text.TextUtils;

import com.rideaustin.App;
import com.rideaustin.utils.AppInfoUtil;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


/**
 * @author vharshyn
 */
public class ApiRequestInterceptor implements Interceptor {
    private static final String AUTH_HEADER = "Authorization";
    private static final String XAUTH_HEADER = "X-Auth-Token";

    private BasicAuthProvider authProvider;

    public ApiRequestInterceptor(BasicAuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder requestBuilder = request.newBuilder();
        if (authProvider != null) {
            if (!TextUtils.isEmpty(App.getDataManager().getXToken())) {
                requestBuilder.addHeader(XAUTH_HEADER, App.getDataManager().getXToken());
            } else if (!TextUtils.isEmpty(authProvider.getAuthString())) {
                requestBuilder.addHeader(AUTH_HEADER, authProvider.getAuthString());
            }
        }

        //Add device info
        Map<String, String> deviceInfo = AppInfoUtil.getDeviceInfo();
        Set<Map.Entry<String, String>> entries = deviceInfo.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }
        return chain.proceed(requestBuilder.build());
    }
}
