package com.rideaustin.ui.drawer.cars.photos;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.rideaustin.App;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.models.DriverCarPhotoData;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.BaseViewModel;
import com.rideaustin.utils.Constants;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by crossover on 08/02/2017.
 */
public class UpdatePhotoViewModel extends BaseViewModel<UpdatePhotoView> {

    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Constants.CarPhotoType
    private final String carPhotoType;
    private final Car car;

    private String photoFilePath;

    public UpdatePhotoViewModel(@NonNull UpdatePhotoView view, Car car, @Constants.CarPhotoType String carPhotoType) {
        super(view);
        this.carPhotoType = carPhotoType;
        this.car = car;
    }

    public boolean isImageSelected() {
        return !TextUtils.isEmpty(photoFilePath);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!App.getDataManager().isLoggedIn()) {
            return;
        }
        if (isImageSelected()) {
            onPhotoTaken(photoFilePath);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        subscriptions.clear();
    }

    public void onPhotoTaken(String filePath) {
        this.photoFilePath = filePath;
        performOnView(view -> view.onPhotoSelected(filePath));
    }

    public Car getCar() {
        return car;
    }

    @Constants.CarPhotoType
    public String getCarPhotoType() {
        return carPhotoType;
    }

    public void uploadCarPhoto(final BaseActivityCallback callback) {
        callback.showProgress();
        DriverCarPhotoData driverCarPhotoData = new DriverCarPhotoData(photoFilePath, carPhotoType);
        subscriptions.add(App.getDataManager().getDriverService().addCarPhoto(car.getId(), driverCarPhotoData.getPhotoData())
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(carPhoto -> {
                    callback.hideProgress();
                    performOnView(view -> view.onPhotoUploaded(carPhoto));
                }, throwable -> {
                    callback.hideProgress();
                    performOnView(UpdatePhotoView::onPhotoUploadFailed);
                }, callback::hideProgress));
    }
}
