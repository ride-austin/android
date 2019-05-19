package com.rideaustin.api.model.driver;

/**
 * Created by hatak on 07.12.16.
 */

public class DriverSignUpResult {

    private Driver driver;
    private Car car;

    public void setDriver(final Driver driver) {
        this.driver = driver;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setCar(final Car car) {
        this.car = car;
    }

    public Car getCar() {
        return car;
    }
}
