package com.rideaustin.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.GlideModule;
import com.bumptech.glide.request.RequestOptions;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static com.rideaustin.utils.CommonConstants.NETWORK_TIMEOUT_IN_SECONDS;

/**
 * Created by Sergey Petrov on 03/05/2017.
 */

public class RaGlideModule implements GlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setDefaultRequestOptions(new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888).disallowHardwareConfig());
        // TODO: add executors idling
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        OkHttpClient client =  new OkHttpClient.Builder()
                // Increase timeout for image loading
                .readTimeout(NETWORK_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(NETWORK_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .connectTimeout(NETWORK_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .build();

        // TODO: add OkHttp idling
        registry.append(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));

    }
}
