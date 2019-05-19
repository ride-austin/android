package com.rideaustin.ui.base;

import android.databinding.BaseObservable;
import android.databinding.ObservableField;
import android.widget.TextView;

import com.rideaustin.api.model.driver.earnings.DriverOnlineResponse;
import com.rideaustin.schedulers.RxSchedulers;

import java.util.Locale;

/**
 * Created by vokol on 22.08.2016.
 */
public abstract class BaseEarningsViewModel extends BaseObservable {

    private static final int SECONDS_IN_HOUR = 3600;
    private static final int SECONDS = 60;

    private String convertSeconds(long seconds) {
        int hours = (int) seconds / SECONDS_IN_HOUR;
        int remainder = (int) seconds - hours * SECONDS_IN_HOUR;
        int mins = remainder / SECONDS;

        return String.format(Locale.ENGLISH, "%02d:%02d", hours, mins);
    }

    protected String getTotalHours(DriverOnlineResponse driverOnlineResponse) {
        return convertSeconds(driverOnlineResponse.getSeconds());
    }
}
