package com.rideaustin.ui.documents;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.rideaustin.App;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.BaseViewModel;
import com.rideaustin.ui.utils.ResourceHelper;
import com.rideaustin.utils.ImageHelper;

/**
 * Created by crossover on 22/01/2017.
 */

public class UploadDocumentsViewModel extends BaseViewModel<UploadDocumentsView> {

    private Target<Bitmap> cityLogoTarget;

    public UploadDocumentsViewModel(@NonNull UploadDocumentsView view) {
        super(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!App.getDataManager().isLoggedIn()) {
            return;
        }
        addSubscription(App.getDataManager().getDriverCityConfig()
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<GlobalConfig>(false) {
                    @Override
                    public void onNext(GlobalConfig globalConfig) {
                        performOnView(view -> view.onConfigLoaded(globalConfig));
                    }
                }));
    }

    @Override
    public void onStop() {
        super.onStop();
        if (cityLogoTarget != null) {
            Glide.with(App.getInstance()).clear(cityLogoTarget);
        }
    }

    public void loadLogo(ImageView view, GlobalConfig globalConfig) {
        int placeholder = ResourceHelper.getWhiteLogoDrawableRes(globalConfig);
        String logoUrl = globalConfig.getGeneralInformation().getLogoUrl();
        cityLogoTarget = ImageHelper.loadImageIntoView(view, logoUrl, placeholder);
    }
}
