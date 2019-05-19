
package com.rideaustin.api.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ServicedZipCodes implements Serializable {

    @SerializedName("servicedZipCodes")
    @Expose
    private List<Integer> servicedZipCodes = null;
    public final static long serialVersionUID = -3994657776739492412L;

    /**
     * @return The servicedZipCodes
     */
    public List<Integer> getServicedZipCodes() {
        return servicedZipCodes;
    }

    /**
     * @param servicedZipCodes The servicedZipCodes
     */
    public void setServicedZipCodes(List<Integer> servicedZipCodes) {
        this.servicedZipCodes = servicedZipCodes;
    }

}
