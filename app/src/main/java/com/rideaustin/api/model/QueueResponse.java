package com.rideaustin.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by ridedriverandroid on 20.09.2016.
 */

public class QueueResponse {
    @SerializedName("areaQueueName")
    private String areaQueueName;
    @SerializedName("iconUrl")
    private String iconUrl;
    @SerializedName("lengths")
    private LinkedHashMap<String, Integer> lengths;
    @SerializedName("positions")
    private LinkedHashMap<String, Integer> positions;

    public String getAreaQueueName() {
        return areaQueueName;
    }

    public LinkedHashMap<String, Integer> getPositions() {
        return positions;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public LinkedHashMap<String, Integer> getLengths() {
        return lengths;
    }
}
