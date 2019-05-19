
package com.rideaustin.api.config;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class Tipping implements Serializable {

    public static final long serialVersionUID = -966691874729452835L;

    // server doesn't send the value
    // it was probably removed from protocol
    // keep it here like a hidden feature enabled by default
    @SerializedName("enabled")
    private boolean enabled = true;

    @SerializedName("rideTipLimit")
    private int rideTipLimit;

    @SerializedName("ridePaymentDelay")
    private long ridePaymentDelay;

    /**
     * 
     * @return
     *     The enabled
     */
    public boolean getEnabled() {
        return enabled;
    }

    /**
     *
     * @param enabled
     *     The enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getRideTipLimit() {
        return rideTipLimit;
    }

    public void setRideTipLimit(int rideTipLimit) {
        this.rideTipLimit = rideTipLimit;
    }

    public long getRidePaymentDelay() {
        return ridePaymentDelay;
    }

    public void setRidePaymentDelay(long ridePaymentDelay) {
        this.ridePaymentDelay = ridePaymentDelay;
    }
}
