package com.rideaustin.api.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Sergey Petrov on 26/07/2017.
 */

public class DriverStat implements Serializable {

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("value")
    private int value;

    @SerializedName("outOfTotal")
    private int total;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getPercentage() {
        return String.valueOf((int)(100 * (float)value/total)) + "%";
    }

    @Override
    public String toString() {
        return "DriverStat{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", value=" + value +
                ", total=" + total +
                '}';
    }
}
