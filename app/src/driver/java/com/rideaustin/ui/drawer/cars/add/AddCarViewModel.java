package com.rideaustin.ui.drawer.cars.add;

import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rideaustin.App;
import com.rideaustin.BR;
import com.rideaustin.api.config.DriverRegistration;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.api.model.driver.DriverPhotoType;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.models.AssetsVehicleDataProvider;
import com.rideaustin.models.CarPhoto;
import com.rideaustin.models.CarUpdate;
import com.rideaustin.models.DriverCarData;
import com.rideaustin.models.DriverCarPhotoData;
import com.rideaustin.models.VehicleManager;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.BaseViewModel;
import com.rideaustin.ui.drawer.cars.add.AddCarActivity.AddCarSequence;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.DateHelper;
import com.rideaustin.utils.FileDirectoryUtil;
import com.rideaustin.utils.ImageHelper;
import com.rideaustin.utils.SerializationHelper;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;

import static com.rideaustin.utils.Constants.TEMP_DIRECTORY;

/**
 * Created by crossover on 24/01/2017.
 */

public class AddCarViewModel extends BaseViewModel<AddCarView> {
    private ObservableField<String> logoUrl = new ObservableField<>();
    private static Target<Bitmap> cityLogoTarget;

    private HashMap<String, String> carPhotos = new HashMap<>();
    private DriverRegistration driverRegistration;
    private Car carRegistration = new Car();
    private String stickerImagePath;
    private Date insuranceExpirationDate;
    private Date tncStickerExpirationDate;
    private String insuranceImagePath;
    private boolean isViewVisible = false;

    public AddCarViewModel(final AddCarView listener) {
        super(listener);
        if (App.getDataManager().getVehicleManager() == null) {
            App.getDataManager().setVehicleManager(initVehicleManager());
        } else {
            App.getDataManager().getVehicleManager().clearFilters();
        }
    }

    @BindingAdapter("addCarCityLogo")
    public static void loadLogo(ImageView view, String imageUrl) {
        if (cityLogoTarget != null) {
            Glide.with(view).clear(cityLogoTarget);
        }
        Log.e("AddCarViewModel", "logoUrl: " + imageUrl);
        cityLogoTarget = ImageHelper.loadImageIntoView(view, imageUrl, 0);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!App.getDataManager().isLoggedIn()) {
            return;
        }
        isViewVisible = true;
        addSubscription(App.getDataManager().getDriverCityConfig()
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<GlobalConfig>(true) {
                    @Override
                    public void onNext(GlobalConfig config) {
                        performOnView(view -> view.onConfigUpdated(config));
                    }
                }));
    }

    @Override
    public void onStop() {
        super.onStop();
        isViewVisible = false;
        if (cityLogoTarget != null) {
            Glide.with(App.getInstance()).clear(cityLogoTarget);
        }
    }

    public boolean isViewVisible() {
        return isViewVisible;
    }

    @Bindable
    public String getLogoUrl() {
        return logoUrl.get();
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl.set(logoUrl);
        notifyPropertyChanged(BR.logoUrl);
    }

    public void addCarPhoto(String currentType, String filePath) {
        carPhotos.put(currentType, filePath);
    }

    public String getCarPhoto(String photoType) {
        return carPhotos.get(photoType);
    }

    public AddCarSequence getNextSequence(AddCarSequence sequence) {
        if (isLastSequence(sequence)) {
            return sequence;
        } else if (sequence == AddCarSequence.CAR_PHOTO_TRUNK) {
            if (shouldSkipTNCStickerStep()) {
                return AddCarSequence.INSURANCE;
            } else {
                return AddCarSequence.INSPECTION_STICKER;
            }
        } else {
            return AddCarSequence.values()[sequence.ordinal() + 1];
        }
    }

    public boolean isLastSequence(AddCarSequence sequence) {
        return AddCarSequence.values().length - 1 == sequence.ordinal();
    }

    public void setDriverRegistration(DriverRegistration driverRegistration) {
        this.driverRegistration = driverRegistration;
    }

    public DriverRegistration getDriverRegistration() {
        return driverRegistration;
    }

    public Car getCarRegistration() {
        return carRegistration;
    }

    private VehicleManager initVehicleManager() {
        VehicleManager.VehicleDataProvider provider = new AssetsVehicleDataProvider(App.getInstance());
        VehicleManager manager = new VehicleManager(provider);
        App.getDataManager().setVehicleManager(manager);
        return manager;
    }

    public boolean shouldSkipTNCStickerStep() {
        return !driverRegistration.getInspectionSticker().getEnabled() ||
                (driverRegistration.getInspectionSticker().getEnabled()
                        && Integer.parseInt(carRegistration.getYear()) >
                        driverRegistration.getInspectionSticker().getStickerRequiredYear());
    }

    public void setStickerImagePath(String driverTncStickerImagePath) {
        this.stickerImagePath = driverTncStickerImagePath;
    }

    public String getStickerImagePath() {
        return stickerImagePath;
    }

    public Date getInsuranceExpirationDate() {
        return insuranceExpirationDate;
    }

    public void setInsuranceExpirationDate(Date insuranceExpirationDate) {
        this.insuranceExpirationDate = insuranceExpirationDate;
    }

    public Date getTncStickerExpirationDate() {
        return tncStickerExpirationDate;
    }

    public void setTncStickerExpirationDate(Date tncStickerExpirationDate) {
        this.tncStickerExpirationDate = tncStickerExpirationDate;
    }

    public void setInsuranceImagePath(String insuranceImagePath) {
        this.insuranceImagePath = insuranceImagePath;
    }

    public String getInsuranceImagePath() {
        return insuranceImagePath;
    }

    private Observable<Car> uploadAllPhotos(final Car car) {
        return Observable.zip(
                uploadCarPhotoData(Constants.CarPhotoType.FRONT, car.getId()),
                uploadCarPhotoData(Constants.CarPhotoType.BACK, car.getId()),
                uploadCarPhotoData(Constants.CarPhotoType.INSIDE, car.getId()),
                uploadCarPhotoData(Constants.CarPhotoType.TRUNK, car.getId()),
                (cr1, cr2, cr3, cr4) -> car);
    }

    private Observable<CarPhoto> uploadCarPhotoData(String type, final long carId) {

        if (!carPhotos.containsKey(type)) {
            return Observable.empty();
        }
        DriverCarPhotoData driverCarPhotoData = new DriverCarPhotoData(carPhotos.get(type), type);
        return App.getDataManager().getDriverService()
                .addCarPhoto(carId, driverCarPhotoData.getPhotoData())
                .subscribeOn(RxSchedulers.network());
    }

    private Observable<Car> uploadSticker(final Car car, final long driverId, String validityDate) {

        return App.getDataManager().getDriverService().uploadDriverDocuments(
                driverId,
                DriverPhotoType.CAR_STICKER.name(),
                car.getId(),
                validityDate,
                ImageHelper.getTypedFileFromPath("fileData", stickerImagePath)).map(driver -> car);

    }

    private Observable<Car> uploadInsurance(Car car, final long driverId, String validtyDate) {

        return App.getDataManager().getDriverService().uploadDriverDocuments(
                driverId,
                DriverPhotoType.INSURANCE.name(),
                car.getId(),
                validtyDate,
                ImageHelper.getTypedFileFromPath("fileData", insuranceImagePath)).map(driver -> car);

    }

    private Observable<Void> removeCar(Car car, final long driverId) {
        return App.getDataManager().getDriverService().removeCar(driverId, car.getId()).subscribeOn(RxSchedulers.network());
    }

    /**
     * This one creates a car, or updates it if exists already.
     */
    public void createCar(final AddCarView listener) {
        Long driverId = App.getDataManager().getCurrentUser().getDriverId();

        Observable
                .fromCallable(() -> new DriverCarData(createCarFile()))
                .flatMap(new Func1<DriverCarData, Observable<Car>>() {
                    @Override
                    public Observable<Car> call(DriverCarData driverCarData) {
                        if (carRegistration.getId() == null || carRegistration.getId().equals(0L)) {
                            return App.getDataManager().getDriverService().addCar(driverId, driverCarData.getCarData());
                        } else {
                            return App.getDataManager().getDriverService().updateCar(driverId, carRegistration.getId(), CarUpdate.fromCar(carRegistration));
                        }
                    }
                })
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber<Car>(listener.getCallback()) {
                    @Override
                    public void onNext(Car car) {
                        carRegistration = car;
                        listener.onCarCreated(car);
                    }

                    @Override
                    public void onError(BaseApiException e) {
                        listener.onCarCreateFailed(e);
                    }
                });
    }

    public void cancelCar(final AddCarView listener) {
        Long driverId = App.getDataManager().getCurrentUser().getDriverId();
        Long carId = carRegistration.getId();

        if (carId == null || carId.equals(0L)) {
            listener.carCancelled();
        } else {
            removeCar(carRegistration, driverId)
                    .subscribeOn(RxSchedulers.network())
                    .observeOn(RxSchedulers.main())
                    .subscribe(new ApiSubscriber<Void>(listener.getCallback()) {
                        @Override
                        public void onNext(Void aVoid) {
                            listener.carCancelled();
                        }

                        @Override
                        public void onError(BaseApiException e) {
                            Timber.e(e, "Car cancellation failed");
                            listener.carCancelledFailed();
                        }
                    });
        }
    }


    public void updateCar(final AddCarView listener) {
        Long driverId = App.getDataManager().getCurrentUser().getDriverId();
        Long carId = carRegistration.getId();

        Observable
                .fromCallable(() -> new DriverCarData(createCarFile()))
                .flatMap(driverCarData -> App.getDataManager().getDriverService().updateCar(driverId, carId, CarUpdate.fromCar(carRegistration)))
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber<Car>(listener.getCallback()) {
                    @Override
                    public void onNext(Car car) {
                        carRegistration = car;
                        listener.onCarUpdated(car);
                    }

                    @Override
                    public void onError(BaseApiException e) {
                        listener.onCarUpdateFailed(e);
                    }
                });
    }

    public void uploadCarPhotos(final AddCarView listener) {
        uploadAllPhotos(carRegistration)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber<Car>(listener.getCallback()) {
                    @Override
                    public void onNext(Car car) {
                        listener.onCarPhotosUploaded(car);
                    }

                    @Override
                    public void onError(BaseApiException e) {
                        listener.onCarPhotosUploadFailed(e);
                    }
                });
    }

    public void uploadSticker(final AddCarView listener) {
        Long driverId = App.getDataManager().getCurrentUser().getDriverId();
        String date = DateHelper.dateToServerDateFormat(tncStickerExpirationDate);
        uploadSticker(carRegistration, driverId, date)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber<Car>(listener.getCallback()) {
                    @Override
                    public void onNext(Car car) {
                        listener.onStickerUploaded(car);
                    }

                    @Override
                    public void onError(BaseApiException e) {
                        listener.onStickerUploadFailed(e);
                    }
                });
    }

    public void uploadInsurance(final AddCarView listener) {
        Long driverId = App.getDataManager().getCurrentUser().getDriverId();
        String date = DateHelper.dateToServerDateFormat(insuranceExpirationDate);
        uploadInsurance(carRegistration, driverId, date)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber<Car>(listener.getCallback()) {
                    @Override
                    public void onNext(Car car) {
                        listener.onInsuranceUploaded(car);
                    }

                    @Override
                    public void onError(BaseApiException e) {
                        listener.onInsuranceUploadFailed(e);
                    }
                });
    }

    private String createCarFile() throws IOException {
        Gson gson = new GsonBuilder().create();
        File driveJsonFile = FileDirectoryUtil.createOrRewriteFile(System.getProperty(TEMP_DIRECTORY), "car.json", gson.toJson(carRegistration));
        return driveJsonFile.getAbsolutePath();
    }

    public void save(Bundle bundle) {
        BundleModel.save(this, bundle);
    }

    public void restore(Bundle bundle) {
        BundleModel.restore(this, bundle);
    }

    public static class BundleModel {

        private static final String KEY = "BundleModel";

        private String logo;
        private HashMap<String, String> carPhotos;
        private DriverRegistration driverRegistration;
        private Car carRegistration;
        private String stickerImagePath;
        private Date insuranceExpirationDate;
        private Date tncStickerExpirationDate;
        private String insuranceImagePath;

        private static void save(AddCarViewModel model, Bundle bundle) {
            BundleModel bundleModel = new BundleModel();
            bundleModel.logo = model.getLogoUrl();
            bundleModel.carPhotos = model.carPhotos;
            bundleModel.driverRegistration = model.driverRegistration;
            bundleModel.carRegistration = model.carRegistration;
            bundleModel.stickerImagePath = model.stickerImagePath;
            bundleModel.insuranceExpirationDate = model.insuranceExpirationDate;
            bundleModel.tncStickerExpirationDate = model.tncStickerExpirationDate;
            bundleModel.insuranceImagePath = model.insuranceImagePath;
            bundle.putSerializable(KEY, SerializationHelper.serialize(bundleModel));
        }

        private static void restore(AddCarViewModel model, Bundle bundle) {
            if (!bundle.containsKey(KEY)) {
                return;
            }
            String modelStr = bundle.getString(KEY);
            if (TextUtils.isEmpty(modelStr)) {
                return;
            }
            BundleModel bundleModel = SerializationHelper.deSerialize(modelStr, BundleModel.class);
            if (bundleModel != null) {
                model.setLogoUrl(bundleModel.logo);
                model.carPhotos = bundleModel.carPhotos;
                model.driverRegistration = bundleModel.driverRegistration;
                model.carRegistration = bundleModel.carRegistration;
                model.stickerImagePath = bundleModel.stickerImagePath;
                model.insuranceExpirationDate = bundleModel.insuranceExpirationDate;
                model.tncStickerExpirationDate = bundleModel.tncStickerExpirationDate;
                model.insuranceImagePath = bundleModel.insuranceImagePath;
            }

        }

    }
}
