package com.rideaustin.api.model.driver;

import com.google.gson.annotations.SerializedName;
import com.rideaustin.api.model.Ride;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ActiveDriver implements Serializable {

    private static final long serialVersionUID = -5824212151426093865L;
    @SerializedName("id")
    private Integer id;
    @SerializedName("latitude")
    private Double latitude;
    @SerializedName("longitude")
    private Double longitude;
    @SerializedName("status")
    private String status;
    @SerializedName("driver")
    private Driver driver;
    @SerializedName("locationUpdatedOn")
    private Long locationUpdatedOn;
    @SerializedName("selectedCar")
    private Car selectedCar;
    @SerializedName("uuid")
    private String uuid;
    //Server does not send this sometimes.
    @SerializedName("course")
    private float course;
    //Server does not send this sometimes.
    @SerializedName("speed")
    private double speed;
    @SerializedName("drivingTimeToRider")
    private Long drivingTimeToRider;
    @SerializedName("carCategories")
    private Set<String> carCategories = new HashSet<>();
    @SerializedName("ride")
    private Ride ride;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Long getLocationUpdatedOn() {
        return locationUpdatedOn;
    }

    public void setLocationUpdatedOn(long locationUpdatedOn) {
        this.locationUpdatedOn = locationUpdatedOn;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public float getCourse() {
        return course;
    }

    public void setCourse(float course) {
        this.course = course;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public Long getDrivingTimeToRider() {
        return drivingTimeToRider;
    }

    public void setDrivingTimeToRider(Long drivingTimeToRider) {
        this.drivingTimeToRider = drivingTimeToRider;
    }

    public Car getSelectedCar() {
        if (selectedCar != null) {
            return selectedCar;
        }
        if (driver.getCars() != null && !driver.getCars().isEmpty()) {
            for (Car car : driver.getCars()) {
                if (car != null && car.isSelected() != null && car.isSelected() && !car.isRemoved()) {
                    selectedCar = car;
                    break;
                }
            }
        }
        return selectedCar;
    }

    public void setSelectedCar(Car selectedCar) {
        this.selectedCar = selectedCar;
    }

    public Set<String> getCarCategories() {
        return carCategories;
    }

    public void setCarCategories(Set<String> carCategories) {
        this.carCategories = carCategories;
    }

    public Ride getRide() {
        return ride;
    }

    public void setRide(Ride ride) {
        this.ride = ride;
    }

    @Override
    public String toString() {
        return "ActiveDriver{" +
                "id=" + id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", status='" + status + '\'' +
                ", driver=" + driver +
                ", locationUpdatedOn=" + locationUpdatedOn +
                ", selectedCar=" + selectedCar +
                ", uuid='" + uuid + '\'' +
                ", course=" + course +
                ", speed=" + speed +
                ", drivingTimeToRider=" + drivingTimeToRider +
                ", carCategories=" + carCategories +
                '}';
    }
}
