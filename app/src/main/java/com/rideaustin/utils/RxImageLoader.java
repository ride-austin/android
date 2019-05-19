package com.rideaustin.utils;

import android.databinding.ObservableField;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.rideaustin.App;
import com.rideaustin.schedulers.RxSchedulers;

import rx.Single;
import rx.Subscription;
import timber.log.Timber;

/**
 * Created by hatak on 06.11.2017.
 */

public class RxImageLoader {

    public static Subscription execute(Request request) {
        if (!request.hasTarget()) {
            throw new IllegalArgumentException("Target not provided");
        }
        return load(request)
                .subscribeOn(RxSchedulers.main())
                .retryWhen(new RetryWhenNoNetwork(1000))
                .subscribe(drawable -> {}, Timber::e);
    }

    public static Single<Drawable> load(Request request) {
        return Single.create(subscriber -> {
            showProgress(request);
            RequestOptions options = new RequestOptions();
            if (request.circular) {
                options = options.centerCrop();
            }
            if (request.size != 0) {
                options = options.override(request.size, request.size);
            }
            Glide.with(App.getInstance())
                    .asBitmap()
                    .load(request.url)
                    .apply(options)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            subscriber.onSuccess(showLoaded(request, resource));
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                            super.onLoadCleared(placeholder);
                            showError(request);
                            subscriber.onError(new RuntimeException("Bitmap was recycled"));
                        }
                    });
        });
    }

    private static void showProgress(Request request) {
        if (request.hasTarget() && request.hasProgress()) {
            AnimationDrawable drawable = (AnimationDrawable) getDrawable(request.progress);
            request.target.set(drawable);
            drawable.start();
        }
    }

    private static Drawable showLoaded(Request request, Bitmap bitmap) {
        Drawable drawable;
        if (request.circular) {
            drawable = RoundedBitmapDrawableFactory.create(App.getInstance().getResources(), bitmap);
            ((RoundedBitmapDrawable) drawable).setCircular(true);
        } else {
            drawable = new BitmapDrawable(App.getInstance().getResources(), bitmap);
        }
        if (request.hasTarget()) {
            request.target.set(drawable);
        }
        return drawable;
    }

    private static void showError(Request request) {
        if (request.hasTarget() && request.hasError()) {
            request.target.set(getDrawable(request.error));
        }
    }

    private static Drawable getDrawable(@DrawableRes int resId) {
        return ContextCompat.getDrawable(App.getInstance(), resId);
    }

    public static class Request {

        private String url;
        private boolean circular;
        private int size = 0;
        private ObservableField<Drawable> target;
        @DrawableRes private int progress;
        @DrawableRes private int error;

        public Request(String url) {
            this.url = url;
        }

        public Request target(ObservableField<Drawable> target) {
            this.target = target;
            return this;
        }

        public Request circular(boolean circular) {
            return circular(circular, 300);
        }

        public Request circular(boolean circular, int size) {
            this.circular = circular;
            this.size = size;
            return this;
        }


        public Request progress(@DrawableRes int progress) {
            this.progress = progress;
            return this;
        }

        public Request error(@DrawableRes int error) {
            this.error = error;
            return this;
        }

        public boolean hasTarget() {
            return target != null;
        }

        public boolean hasProgress() {
            return progress != 0;
        }

        public boolean hasError() {
            return error != 0;
        }
    }
}
