package com.rideaustin.api.config.v2_6_1;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by hatak on 21.11.16.
 */

public class DriverRegistrationWrapper {

    @SerializedName("driverRegistration")
    @Expose
    private DriverRegistration driverRegistration;

    /**
     * @return The driverRegistration
     */
    public DriverRegistration getDriverRegistration() {
        return driverRegistration;
    }

    /**
     * @param driverRegistration The driverRegistration
     */
    public void setDriverRegistration(DriverRegistration driverRegistration) {
        this.driverRegistration = driverRegistration;
    }

}
