package com.rideaustin.ui.map;

import android.databinding.Bindable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.etiennelawlor.discreteslider.library.ui.DiscreteSlider;
import com.rideaustin.App;
import com.rideaustin.BR;
import com.rideaustin.R;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.RxBaseObservable;
import com.rideaustin.ui.utils.SurgeAreaUtils;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.ImageHelper;

import timber.log.Timber;

/**
 * Created by hatak on 2/2/17.
 */

@Deprecated
public class MultipleCarViewModel extends RxBaseObservable {

    public static final String EMPTY_ETA = "-     -";
    private String etaValue;
    private String maxPeopleValue;
    private MapViewModel mapViewModel;
    private MultipleCarView view;
    private Bitmap background;
    private Bitmap icon;
    private Target iconTarget;
    private Handler mainHandler;


    public MultipleCarViewModel(final MultipleCarView view, final MapViewModel mapViewModel) {
        this.mapViewModel = mapViewModel;
        this.view = view;
        mainHandler = new Handler(Looper.getMainLooper());
        getBackgroundForCategory();
    }

    @Override
    public void onStart() {
        super.onStart();
        addSubscription(mapViewModel.getTimeEstimateObservable()
                .observeOn(RxSchedulers.main())
                .subscribe(eta -> setEtaValue(getFormattedEta(eta))));

        addSubscription(App.getDataManager().getSurgeAreaUpdates()
                .filter(surgeAreas -> SurgeAreaUtils.containsLocation(surgeAreas, mapViewModel.getPickupLocation()))
                .observeOn(RxSchedulers.main())
                .subscribe(surgeArea -> view.onSurgeUpdated(), throwable -> Timber.w(throwable, "error while loading surge areas")));
    }

    private Bitmap getBackgroundForCategory() {
        if (background == null || background.isRecycled()) {
            background = BitmapFactory.decodeResource(App.getInstance().getResources(), R.drawable.icn_car_background);
        }
        return background;
    }

    public void clear() {
        if (background != null) {
            background.recycle();
            background = null;
        }
        if (icon != null) {
            icon.recycle();
            icon = null;
        }
        if (iconTarget != null) {
//            Glide.clear(iconTarget);
            iconTarget = null;
        }
    }

    @Bindable
    public String getEtaValue() {
        return etaValue;
    }

    public void setEtaValue(String etaValue) {
        this.etaValue = etaValue;
        notifyPropertyChanged(BR.etaValue);
    }

    @Bindable
    public String getMaxPeopleValue() {
        return maxPeopleValue;
    }

    public void setMaxPeopleValue(Integer maxPeopleValue) {
        this.maxPeopleValue = String.valueOf(maxPeopleValue) + " PEOPLE";
        notifyPropertyChanged(BR.maxPeopleValue);
    }

    private String getFormattedEta(int eta) {
        if (eta > 1) {
            return String.valueOf(eta) + Constants.MINS;
        } else if (eta <= 1 && eta > 0) {
            return String.valueOf(eta) + Constants.MIN;
        } else if (eta == 0) {
            return Constants.LESS_THAN_MIN + Constants.MIN;
        } else {
            return EMPTY_ETA;
        }
    }

    public MapViewModel getMapViewModel() {
        return mapViewModel;
    }

    void setCarIcon(RequestedCarType carType, DiscreteSlider carTypesSlider) {
        final boolean fullSize = !TextUtils.isEmpty(carType.getFullIconUrl());
        final String url = fullSize ? carType.getFullIconUrl() : carType.getIconUrl();

        if (iconTarget != null) {
            // clear previous target to prevent multiple concurrent requests
            Glide.with(App.getInstance()).clear(iconTarget);
        }
        iconTarget = ImageHelper.loadCarIconTarget(App.getInstance(), new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                // Unfortunately, there is no way to cancel request but keep resource
                // Every time target is cleared, resource becomes invalid (recycled)
                // That's why we use copy of original bitmap in slider thumb
                icon = Bitmap.createBitmap(resource);
                // do not recycle original bitmap, it may be reused by glide
                carTypesSlider.setThumb(new BitmapDrawable(App.getInstance().getResources(), icon));
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                Timber.e("error loading icon for slider: %s", carType.getIconUrl());
                // retry loading
                mainHandler.post(() -> {
                    // https://github.com/bumptech/glide/issues/2530
                    setCarIcon(carType, carTypesSlider);
                });

            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                super.onLoadCleared(placeholder);
                // glide cleared original bitmap
                // but we are using a copy which is still valid
            }
        }, url, getBackgroundForCategory(), fullSize);
    }

    interface MultipleCarView {
        void onSurgeUpdated();
    }
}
