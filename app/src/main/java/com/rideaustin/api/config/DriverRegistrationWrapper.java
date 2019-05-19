
package com.rideaustin.api.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DriverRegistrationWrapper implements Serializable {

    @SerializedName("driverRegistration")
    @Expose
    private DriverRegistration driverRegistration;
    public final static long serialVersionUID = -8936284051755211888L;

    /**
     *
     * @return
     *     The driverRegistration
     */
    public DriverRegistration getDriverRegistration() {
        return driverRegistration;
    }

    /**
     *
     * @param driverRegistration
     *     The driverRegistration
     */
    public void setDriverRegistration(DriverRegistration driverRegistration) {
        this.driverRegistration = driverRegistration;
    }

}
