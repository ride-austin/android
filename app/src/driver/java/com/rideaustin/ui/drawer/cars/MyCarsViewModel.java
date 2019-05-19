package com.rideaustin.ui.drawer.cars;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.DriverRegistration;
import com.rideaustin.api.config.DriverRegistrationWrapper;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.engine.EngineState;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.BaseViewModel;
import com.rideaustin.utils.toast.RAToast;

import java.util.List;

import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by crossover on 18/01/2017.
 */

public class MyCarsViewModel extends BaseViewModel<MyCarsView> {

    private static final String DRIVER_REGISTRATION_PARAM = "driverRegistration";

    private CompositeSubscription subscriptions = new CompositeSubscription();

    @NonNull
    private final ObservableArrayList<Car> cars = new ObservableArrayList<>();
    @NonNull
    private ObservableField<String> inspectionStickerTitle = new ObservableField<>();

    private DriverRegistration registrationConfiguration;

    @Nullable
    private BaseActivityCallback callback;

    public MyCarsViewModel(@NonNull MyCarsView view) {
        super(view);
    }

    public void setCallback(@Nullable BaseActivityCallback callback) {
        this.callback = callback;
    }

    @NonNull
    public ObservableArrayList<Car> getCars() {
        return cars;
    }


    @NonNull
    public ObservableField<String> getInspectionStickerTitle() {
        return inspectionStickerTitle;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!App.getDataManager().isLoggedIn()) {
            // RA-13364
            return;
        }
        subscriptions.add(App.getDataManager()
                .getCars()
                .subscribeOn(RxSchedulers.network())
                .zipWith(downloadRegistrationConfiguration(), (cars1, driverRegistration) -> new Pair<>(driverRegistration, cars1))
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<Pair<DriverRegistration, List<Car>>>(callback) {
                    @Override
                    public void onNext(Pair<DriverRegistration, List<Car>> configurationWithCars) {
                        onCarsAnConfigurationLoaded(configurationWithCars.first, configurationWithCars.second);
                    }

                    @Override
                    public void onAnyError(BaseApiException e) {
                        getView().onCarsLoadingFailed();
                    }
                }));
    }

    private Observable<DriverRegistration> downloadRegistrationConfiguration() {
        int cityId = App.getDataManager().getCurrentDriver().getCityId();
        getView().getCallback().showProgress();
        return App.getDataManager()
                .getConfigService()
                .getDriverRegistration(cityId, DRIVER_REGISTRATION_PARAM)
                .subscribeOn(RxSchedulers.network())
                .map(DriverRegistrationWrapper::getDriverRegistration);
    }

    public DriverRegistration getRegistrationConfiguration() {
        return registrationConfiguration;
    }

    private void onCarsAnConfigurationLoaded(final DriverRegistration registrationConfiguration, final List<Car> cars) {
        this.registrationConfiguration = registrationConfiguration;
        App.getDataManager().getCurrentDriver().setCars(cars);
        this.cars.clear();
        for (Car car : cars) {
            if (!car.isRemoved()) {
                this.cars.add(car);
            }
        }
        performOnView(MyCarsView::onCarsLoaded);
    }

    @Override
    public void onStop() {
        super.onStop();
        subscriptions.clear();
    }

    public void onCarClicked(final Car clickedCar) {
        if (clickedCar.isSelected() != null && clickedCar.isSelected()) {
            return;
        }
        if (App.getInstance().getStateManager().getCurrentEngineStateType() == EngineState.Type.OFFLINE) {
            subscriptions.add(App.getDataManager().selectCar(clickedCar.getId())
                    .subscribeOn(RxSchedulers.network())
                    .observeOn(RxSchedulers.main())
                    .subscribe(new ApiSubscriber2<Car>(callback) {
                        @Override
                        public void onNext(Car serverCar) {
                            Driver currentDriver = App.getDataManager().getCurrentDriver();
                            App.getPrefs().setCurrentRideRequestType(currentDriver, serverCar.getCarCategories());
                            App.getPrefs().setWomanOnlyModeEnabled(currentDriver, false);
                            performOnView(view -> {
                                for (int i = 0; i < cars.size(); i++) {
                                    Car listedCar = cars.get(i);
                                    if (serverCar.getId().equals(listedCar.getId())) {
                                        cars.set(i, serverCar);
                                    } else {
                                        listedCar.setSelected(false);
                                    }
                                }
                                currentDriver.setCars(cars);
                                view.onCarSelectionChanged();
                            });
                        }
                    }));

        } else {
            RAToast.showShort(R.string.have_to_be_offline);
        }
    }

    public void onUpdateInsuranceClicked(final CarViewModel carViewModel) {
        performOnView(view -> view.onUpdateInsuranceClicked(carViewModel.getCar()));
    }

    public void onUpdatePhotosClicked(final CarViewModel carViewModel) {
        performOnView(view -> view.onUpdatePhotosClicked(carViewModel.getCar()));
    }

    public void onUpdateInspectionStickerClicked(final CarViewModel carViewModel) {
        performOnView(view -> view.onUpdateInspectionStickerClicked(registrationConfiguration, carViewModel.getCar()));
    }
}