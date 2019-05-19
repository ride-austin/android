package com.rideaustin.ui.drawer.dc;

import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.text.TextUtils;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.driver.DirectConnectDriver;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.RxBaseViewModel;

/**
 * Created by hatak on 24.10.2017.
 */

public class SelectDriverViewModel extends RxBaseViewModel {

    public final ObservableField<String> driverId = new ObservableField<>("");
    public final ObservableBoolean buttonEnabled = new ObservableBoolean();
    public final ObservableField<String> description = new ObservableField<>("");

    private DirectConnectViewModel model;

    public SelectDriverViewModel() {
        untilDestroy(App.getConfigurationManager()
                .getLiveConfig()
                .subscribe(config -> {
                    description.set(config.getDirectConnectConfig().getDescription());
                }, throwable -> {
                    description.set(App.getInstance().getString(R.string.connect_driver_info));
                }));
        driverId.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                buttonEnabled.set(!driverId.get().isEmpty());
            }
        });
    }

    public void setModel(DirectConnectViewModel model) {
        this.model = model;
    }

    public void findDriver() {
        String id = driverId.get().trim();
        model.setDirectConnectId(id);
        if (!TextUtils.isEmpty(id)) {
            untilDestroy(App.getDataManager().getDriverService()
                    .getDriverByDirectConnectId(id)
                    .subscribeOn(RxSchedulers.network())
                    .subscribe(new ApiSubscriber2<DirectConnectDriver>(this) {
                        @Override
                        public void onNext(DirectConnectDriver driver) {
                            model.setDriver(driver);
                        }

                        @Override
                        public void onCompleted() {
                            super.onCompleted();
                            model.navigateTo(DirectConnectViewModel.Step.DRIVER_SUMMARY);
                        }
                    }));
        }
    }
}
