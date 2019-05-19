package com.rideaustin.ui.contact;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.Rider;
import com.rideaustin.api.model.driver.ActiveDriver;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.ui.common.RxBaseViewModel;
import com.rideaustin.utils.Constants;

import java8.util.Optional;

/**
 * Created on 04/04/2018
 *
 * @author sdelaysam
 */

public class RideContactViewModel extends RxBaseViewModel {

    public final ObservableField<String> description = new ObservableField<>();
    public final ObservableBoolean callVisible = new ObservableBoolean(true);
    public final ObservableField<String> callAction = new ObservableField<>();
    public final ObservableField<String> smsAction = new ObservableField<>();

    private final boolean isDriver = Constants.IS_DRIVER;
    private Ride ride;

    public void setRide(Ride ride) {
        this.ride = ride;
        // deaf driver indication is shown only for rider
        boolean isDeaf = !isDriver && Optional.ofNullable(ride)
                .map(Ride::getActiveDriver)
                .map(ActiveDriver::getDriver)
                .map(Driver::isDeaf)
                .orElse(false);
        String name = getName();
        if (isDeaf) {
            description.set(App.getInstance().getString(R.string.call_to_deaf, name));
            callVisible.set(false);
        } else {
            description.set(App.getInstance().getString(R.string.how_do_you_want_to_contact));
            callVisible.set(true);
        }
        callAction.set(App.getInstance().getString(R.string.call_to, name));
        smsAction.set(App.getInstance().getString(R.string.sms_to, name));
    }

    public String getPhoneNumber() {
        if (isDriver) {
            return Optional.ofNullable(ride)
                    .map(Ride::getRider)
                    .map(Rider::getPhoneNumber)
                    .orElse(null);
        } else {
            return Optional.ofNullable(ride)
                    .map(Ride::getActiveDriver)
                    .map(ActiveDriver::getDriver)
                    .map(Driver::getPhoneNumber)
                    .orElse(null);
        }
    }

    private String getName() {
        if (isDriver) {
            return Optional.ofNullable(ride)
                    .map(Ride::getRider)
                    .map(Rider::getFirstname)
                    .orElse("");
        } else {
            return Optional.ofNullable(ride)
                    .map(Ride::getActiveDriver)
                    .map(ActiveDriver::getDriver)
                    .map(Driver::getFirstname)
                    .orElse("");
        }
    }
}
