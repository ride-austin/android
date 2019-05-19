package com.rideaustin.api.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Sergey Petrov on 07/09/2017.
 */

public class LocationHintCoord implements Serializable {

    @SerializedName("name")
    private String name;

    @SerializedName("driverCoord")
    private Coordinates driverCoord;

    private transient String areaName;

    public String getName() {
        return name;
    }

    public Coordinates getDriverCoord() {
        return driverCoord;
    }

    public LocationHintCoord() {
        // empty constructor for serializer
    }

    public LocationHintCoord(String name, Coordinates driverCoord) {
        this.name = name;
        this.driverCoord = driverCoord;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String composeName() {
        if (areaName != null) {
            return areaName + " (" + name + ")";
        } else {
            return name;
        }
    }
}
