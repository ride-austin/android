package com.rideaustin.ui.directconnect;

import android.databinding.ObservableField;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.driver.DirectConnectResponse;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.ui.common.RxBaseViewModel;

/**
 * Created by hatak on 07.11.2017.
 */

public class DirectConnectViewModel extends RxBaseViewModel {

    public final ObservableField<String> title = new ObservableField<>("");
    public final ObservableField<String> description = new ObservableField<>("");
    public final ObservableField<String> driverId = new ObservableField<>("");

    public void initialize() {
        untilDestroy(App.getDataManager()
                .getDriverObservable()
                .subscribe(driver -> {
                    driverId.set(driver.getDirectConnectId());
                }, throwable -> {
                    driverId.set(null);
                }));
        untilDestroy(App.getConfigurationManager()
                .getLiveConfig()
                .subscribe(config -> {
                    title.set(config.getDirectConnectConfig().getTitle());
                    description.set(config.getDirectConnectConfig().getDescription());
                }, throwable -> {
                    title.set(App.getInstance().getString(R.string.direct_connect_menu));
                    description.set(App.getInstance().getString(R.string.direct_connect_description));
                }));
    }

    public void getNewDirectConnectId() {
        untilDestroy(App.getDataManager()
                .getNewDirectConnectId()
                .subscribe(new ApiSubscriber2<DirectConnectResponse>(this) {
                    @Override
                    public void onNext(DirectConnectResponse response) {
                        super.onNext(response);
                        driverId.set(response.getDirectConnectId());
                    }
                }));
    }

}
