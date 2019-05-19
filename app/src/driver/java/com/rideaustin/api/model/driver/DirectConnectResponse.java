package com.rideaustin.api.model.driver;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created on 01/12/2017
 *
 * @author sdelaysam
 */

public class DirectConnectResponse implements Serializable {

    @SerializedName("directConnectId")
    private String directConnectId;

    public String getDirectConnectId() {
        return directConnectId;
    }

    public void setDirectConnectId(String directConnectId) {
        this.directConnectId = directConnectId;
    }
}
