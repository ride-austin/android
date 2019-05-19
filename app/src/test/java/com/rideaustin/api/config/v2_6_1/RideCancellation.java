
package com.rideaustin.api.config.v2_6_1;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class RideCancellation {

    @SerializedName("rideCancellationPeriod")
    @Expose
    private Integer rideCancellationPeriod;

    /**
     * 
     * @return
     *     The rideCancellationPeriod
     */
    public Integer getRideCancellationPeriod() {
        return rideCancellationPeriod;
    }

    /**
     * 
     * @param rideCancellationPeriod
     *     The rideCancellationPeriod
     */
    public void setRideCancellationPeriod(Integer rideCancellationPeriod) {
        this.rideCancellationPeriod = rideCancellationPeriod;
    }

}
