package com.rideaustin.ui.drawer.documents;

import android.support.annotation.NonNull;

import com.rideaustin.App;
import com.rideaustin.api.config.DriverRegistration;
import com.rideaustin.api.config.DriverRegistrationWrapper;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.BaseViewModel;

/**
 * Created by crossover on 22/01/2017.
 */

public class DocumentsViewModel extends BaseViewModel<DocumentsView> {

    private static final String DRIVER_REGISTRATION_PARAM = "driverRegistration";

    DriverRegistration driverRegistration;

    public DocumentsViewModel(@NonNull DocumentsView view) {
        super(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!App.getDataManager().isLoggedIn()) {
            // app would be sent to login on resume
            return;
        }
        int cityId = App.getDataManager().getCurrentDriver().getCityId();
        addSubscription(App.getDataManager()
                .getConfigService()
                .getDriverRegistration(cityId, DRIVER_REGISTRATION_PARAM)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber<DriverRegistrationWrapper>() {
                    @Override
                    public void onNext(DriverRegistrationWrapper driverRegistrationWrapper) {
                        super.onNext(driverRegistrationWrapper);
                        performOnView(view -> {
                            driverRegistration = driverRegistrationWrapper.getDriverRegistration();
                            view.onDriverRegistrationLoaded();
                        });
                    }

                    @Override
                    public void onError(BaseApiException e) {
                        super.onError(e);
                    }
                }));
    }

    public DriverRegistration getDriverRegistration() {
        return driverRegistration;
    }
}
