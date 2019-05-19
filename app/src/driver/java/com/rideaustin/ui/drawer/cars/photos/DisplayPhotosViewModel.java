package com.rideaustin.ui.drawer.cars.photos;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.rideaustin.App;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.models.CarPhoto;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.BaseViewModel;
import com.rideaustin.utils.Constants;

import java.util.List;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by crossover on 08/02/2017.
 */

public class DisplayPhotosViewModel extends BaseViewModel<DisplayPhotosView> {

    private CompositeSubscription subscriptions = new CompositeSubscription();

    private Car car;

    private String front;
    private String back;
    private String inside;
    private String trunk;

    private boolean reloadRequired;

    public DisplayPhotosViewModel(@NonNull DisplayPhotosView view, Car car) {
        super(view);
        this.car = car;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (imagesNeedReloading()) {
            loadImages();
        } else {
            performOnView(view -> view.onPhotosDownloaded(front, back, inside, trunk));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        subscriptions.clear();
    }

    private boolean imagesNeedReloading() {
        return reloadRequired || TextUtils.isEmpty(front) || TextUtils.isEmpty(back) || TextUtils.isEmpty(inside) || TextUtils.isEmpty(trunk);
    }

    public void setReloadRequired() {
        reloadRequired = true;
    }

    public void loadImages() {
        subscriptions.add(App.getDataManager().getDriverService().getCarPhotos(car.getId())
                .observeOn(RxSchedulers.main())
                .subscribeOn(RxSchedulers.network())
                .subscribe(
                        new ApiSubscriber2<List<CarPhoto>>(false) {
                            @Override
                            public void onNext(List<CarPhoto> carPhotos) {

                                for (CarPhoto carPhoto : carPhotos) {
                                    switch (carPhoto.getCarPhotoType()) {
                                        case Constants.CarPhotoType.FRONT:
                                            front = carPhoto.getPhotoUrl();
                                            break;
                                        case Constants.CarPhotoType.BACK:
                                            back = carPhoto.getPhotoUrl();
                                            break;
                                        case Constants.CarPhotoType.INSIDE:
                                            inside = carPhoto.getPhotoUrl();
                                            break;
                                        case Constants.CarPhotoType.TRUNK:
                                            trunk = carPhoto.getPhotoUrl();
                                            break;
                                    }
                                }
                                performOnView(view -> view.onPhotosDownloaded(front, back, inside, trunk));
                                reloadRequired = false;
                            }

                            @Override
                            public void onAnyError(BaseApiException e) {
                                performOnView(DisplayPhotosView::onPhotosDownloadFailed);
                            }
                        }
                ));
    }

    public Car getCar() {
        return car;
    }
}
