
package com.rideaustin.api.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class RideCancellation implements Serializable {

    public static final long serialVersionUID = -5388270240533193336L;
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
