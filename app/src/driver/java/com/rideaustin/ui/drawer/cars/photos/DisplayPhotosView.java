package com.rideaustin.ui.drawer.cars.photos;

import android.support.annotation.Nullable;

import com.rideaustin.ui.common.BaseView;

/**
 * Created by crossover on 08/02/2017.
 */

public interface DisplayPhotosView extends BaseView {
    void onPhotosDownloadFailed();

    void onPhotosDownloaded(@Nullable String front, @Nullable String back, @Nullable String inside, @Nullable String trunk);
}
