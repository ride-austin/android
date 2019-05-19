
package com.rideaustin.api.model.driver.earnings;

import android.support.annotation.VisibleForTesting;

import com.google.gson.annotations.SerializedName;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.api.model.driver.RequestedCarType;

import java.io.Serializable;

public class DriverEarningResponseContent implements Serializable, Comparable<DriverEarningResponseContent> {


    @SerializedName("id")
    private Long id;

    @SerializedName("status")
    private String status;

    @SerializedName("startLocationLat")
    private double startLocationLat;

    @SerializedName("startLocationLng")
    private double startLocationLng;

    @SerializedName("start")
    private Start start;

    @SerializedName("endLocationLat")
    private double endLocationLat;

    @SerializedName("endLocationLng")
    private double endLocationLng;

    @SerializedName("end")
    private End end;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @SerializedName("startedOn")
    public long startedOn;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @SerializedName("completedOn")
    public long completedOn;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @SerializedName("cancelledOn")
    public long cancelledOn;

    @SerializedName("minimumFare")
    private String minimumFare;

    @SerializedName("baseFare")
    private String baseFare;

    @SerializedName("ratePerMile")
    private String ratePerMile;

    @SerializedName("ratePerMinute")
    private String ratePerMinute;

    @SerializedName("distanceFare")
    private String distanceFare;

    @SerializedName("timeFare")
    private String timeFare;

    @SerializedName("rideMap")
    private String rideMap;

    @SerializedName("cityFee")
    private String cityFee;

    @SerializedName("raFee")
    private String raFee;

    @SerializedName("subTotal")
    private String subTotal;

    @SerializedName("totalFare")
    private String totalFare;

    @SerializedName("bookingFee")
    private String bookingFee;

    @SerializedName("driverPayment")
    private String driverPayment;

    @SerializedName("requestedCarType")
    private RequestedCarType requestedCarType;

    @SerializedName("car")
    private Car car;

    @SerializedName("roundUpAmount")
    private String roundUpAmount;

    @SerializedName("driverRating")
    private double driverRating;

    @SerializedName("tip")
    private double tip;

    @SerializedName("surgeFactor")
    private double surgeFactor;

    @SerializedName("surgeFare")
    private String surgeFare;

    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public double getStartLocationLat() {
        return startLocationLat;
    }

    public double getStartLocationLng() {
        return startLocationLng;
    }

    public Start getStart() {
        return start;
    }

    public double getEndLocationLat() {
        return endLocationLat;
    }

    public double getEndLocationLng() {
        return endLocationLng;
    }

    public End getEnd() {
        return end;
    }

    public long getStartedOn() {
        return startedOn;
    }

    public long getCompletedOn() {
        return completedOn;
    }

    public String getMinimumFare() {
        return minimumFare;
    }

    public String getBaseFare() {
        return baseFare;
    }

    public String getRatePerMile() {
        return ratePerMile;
    }

    public String getRatePerMinute() {
        return ratePerMinute;
    }

    public String getDistanceFare() {
        return distanceFare;
    }

    public String getTimeFare() {
        return timeFare;
    }

    public String getRideMap() {
        return rideMap;
    }

    public String getCityFee() {
        return cityFee;
    }

    public String getRaFee() {
        return raFee;
    }

    public String getSubTotal() {
        return subTotal;
    }

    public String getTotalFare() {
        return totalFare;
    }

    public String getBookingFee() {
        return bookingFee;
    }

    public String getDriverPayment() {
        return driverPayment;
    }

    public RequestedCarType getRequestedCarType() {
        return requestedCarType;
    }

    public Car getCar() {
        return car;
    }

    public String getRoundUpAmount() {
        return roundUpAmount;
    }

    public double getDriverRating() {
        return driverRating;
    }

    public long getCancelledOn() {
        return cancelledOn;
    }

    public double getSurgeFactor() {
        return surgeFactor;
    }

    public String getSurgeFare() {
        return surgeFare;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DriverEarningResponseContent that = (DriverEarningResponseContent) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public int compareTo(DriverEarningResponseContent another) {
        if (this.getCompletedOn() > another.getCompletedOn()) {
            return -1;
        }

        return 0;
    }

    public double getTip() {
        return tip;
    }


    @Override
    public String toString() {
        return "DriverEarningResponseContent{" +
                "id=" + id +
                ", subTotal='" + subTotal + '\'' +
                '}';
    }
}
