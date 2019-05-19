
package com.rideaustin.api.config;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GeocodingConfiguration implements Serializable {

    public static final long serialVersionUID = -3754378362191765435L;
    @SerializedName("queryHints")
    private List<QueryHint> queryHints = new ArrayList<>();

    @SerializedName("pickupHints")
    private List<LocationHint> pickupHints = new ArrayList<>();

    @SerializedName("destinationHints")
    private List<LocationHint> destinationHints = new ArrayList<>();


    public List<QueryHint> getQueryHints() {
        return queryHints;
    }

    public List<LocationHint> getPickupHints() {
        return pickupHints;
    }

    public List<LocationHint> getDestinationHints() {
        return destinationHints;
    }
}
