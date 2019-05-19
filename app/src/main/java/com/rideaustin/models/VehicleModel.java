package com.rideaustin.models;

/**
 * Created by rost on 8/10/16.
 */
public class VehicleModel {
    public enum FieldType {
        YEAR, MAKE, MODEL
    }

    private String year;
    private String make;
    private String model;

    public VehicleModel(String year, String make, String model) {
        this.year = year;
        this.make = make;
        this.model = model;
    }

    public String getYear() {
        return year;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }
}
