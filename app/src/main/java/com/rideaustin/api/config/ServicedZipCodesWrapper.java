
package com.rideaustin.api.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ServicedZipCodesWrapper implements Serializable {

    @SerializedName("servicedZipCodes")
    @Expose
    private ServicedZipCodes servicedZipCodes;
    public final static long serialVersionUID = -6576758992159655980L;

    /**
     * @return The servicedZipCodes
     */
    public ServicedZipCodes getServicedZipCodes() {
        return servicedZipCodes;
    }

    /**
     * @param servicedZipCodes The servicedZipCodes
     */
    public void setServicedZipCodes(ServicedZipCodes servicedZipCodes) {
        this.servicedZipCodes = servicedZipCodes;
    }

}
