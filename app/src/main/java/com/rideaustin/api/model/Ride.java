package com.rideaustin.api.model;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;
import com.rideaustin.api.model.driver.ActiveDriver;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.api.model.driver.RequestedDriverType;
import com.rideaustin.entities.GeoPosition;
import com.rideaustin.utils.Constants;

import java.io.Serializable;

public class Ride implements Serializable {

    private static final long serialVersionUID = 3612056724676362037L;

    @SerializedName("activeDriver")
    private ActiveDriver activeDriver;

    @SerializedName("freeCancellationExpiresOn")
    private Long freeCancellationExpiresOn;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @SerializedName("driverAcceptedOn")
    public long driverAcceptedOn;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @SerializedName("completedOn")
    public long completedOn;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @SerializedName("cancelledOn")
    public long cancelledOn;

    @SerializedName("stripeCreditCharge")
    private String stripeCreditCharge;

    @SerializedName("tip")
    private String tip;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @SerializedName("tippedOn")
    public long tippedOn;

    @SerializedName("driverRating")
    private Double driverRating;

    @SerializedName("id")
    private Long id;

    @SerializedName("rider")
    private Rider rider;

    @SerializedName("status")
    private String status;

    @SerializedName("minimumFare")
    private String minimumFare;

    @SerializedName("baseFare")
    private String baseFare;

    @SerializedName("ratePerMile")
    private String ratePerMile;

    @SerializedName("ratePerMinute")
    private String ratePerMinute;

    @SerializedName("estimatedFare")
    private String estimatedFare;

    @SerializedName("estimatedTimeArrive")
    private Long estimatedTimeArrive;

    @SerializedName("estimatedTimeCompletion")
    private Long estimatedTimeCompletion;

    @SerializedName("bookingFee")
    private String bookingFee;

    @SerializedName("startLocationLat")
    private Double startLocationLat;

    @SerializedName("startLocationLong")
    private Double startLocationLong;

    @SerializedName("endLocationLat")
    private Double endLocationLat;

    @SerializedName("endLocationLong")
    private Double endLocationLong;

    @SerializedName("startAddress")
    private String startAddress;

    @SerializedName("endAddress")
    private String endAddress;

    @SerializedName("start")
    private Address start;

    @SerializedName("end")
    private Address end;

    @SerializedName("uuid")
    private String uuid;

    @SerializedName("totalFare")
    private Double totalFare;

    @SerializedName("surgeFactor")
    private Double surgeFactor;

    @SerializedName("surgeFare")
    private String surgeFare;

    @SerializedName("driverPayment")
    private double driverPayment;

    @SerializedName("requestedCarType")
    private RequestedCarType requestedCarType;

    @SerializedName("requestedDriverType")
    private RequestedDriverType requestedDriverType;

    @SerializedName("comment")
    private String comment;

    @SerializedName("upgradeRequest")
    private UpgradeRequest upgradeRequest;

    @SerializedName("bevoBucksUrl")
    private String bevoBucksUrl;

    @SerializedName("freeCreditCharged")
    private String freeCreditCharged;

    @SerializedName("nextRide")
    private Ride nextRide;

    @SerializedName("precedingRide")
    private Ride prevRide;

    private String mapUrl;

    private Boolean tippingAllowed;

    private Long tippingUntil;

    @Nullable
    public GeoPosition getStartPosition() {
        LatLng location = getStartLocation();
        if (location != null) {
            return new GeoPosition(location, getStartAddressText(), getStartAddressText());
        }
        return null;
    }

    @Nullable
    public GeoPosition getEndPosition() {
        LatLng location = getEndLocation();
        if (location != null) {
            return new GeoPosition(location, getEndAddress(), getEndAddress());
        }
        return null;
    }

    public LatLng getStartLocation() {
        if (getStartLocationLat() != null && getStartLocationLong() != null) {
            return new LatLng(getStartLocationLat(), getStartLocationLong());
        }
        if (getStart() != null && getStart().getLatitude() != null && getStart().getLongitude() != null) {
            return new LatLng(getStart().getLatitude(), getStart().getLongitude());
        }
        return null;
    }

    public LatLng getEndLocation() {
        if (getEndLocationLat() != null && getEndLocationLong() != null) {
            return new LatLng(getEndLocationLat(), getEndLocationLong());
        }
        if (getEnd() != null && getEnd().getLatitude() != null && getEnd().getLongitude() != null) {
            return new LatLng(getEnd().getLatitude(), getEnd().getLongitude());
        }
        return null;
    }

    public boolean hasStartAddress() {
        return !TextUtils.isEmpty(getStartAddress()) || (start != null && !TextUtils.isEmpty(start.getAddress()));
    }

    public String getStartAddressText() {
        if (!TextUtils.isEmpty(getStartAddress())) {
            return getStartAddress();
        }

        if (start != null) {
            return start.getAddress();
        }

        return null;
    }

    public ActiveDriver getActiveDriver() {
        return activeDriver;
    }

    public void setActiveDriver(ActiveDriver activeDriver) {
        this.activeDriver = activeDriver;
    }

    public Long getFreeCancellationExpiresOn() {
        return freeCancellationExpiresOn;
    }

    public void setFreeCancellationExpiresOn(Long freeCancellationExpiresOn) {
        this.freeCancellationExpiresOn = freeCancellationExpiresOn;
    }

    public long getDriverAcceptedOn() {
        return driverAcceptedOn;
    }

    public void setDriverAcceptedOn(long driverAcceptedOn) {
        this.driverAcceptedOn = driverAcceptedOn;
    }

    public long getCompletedOn() {
        return completedOn;
    }

    public void setCompletedOn(long completedOn) {
        this.completedOn = completedOn;
    }

    public long getCancelledOn() {
        return cancelledOn;
    }

    public void setCancelledOn(long cancelledOn) {
        this.cancelledOn = cancelledOn;
    }

    public String getStripeCreditCharge() {
        return stripeCreditCharge;
    }

    public void setStripeCreditCharge(String stripeCreditCharge) {
        this.stripeCreditCharge = stripeCreditCharge;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public long getTippedOn() {
        return tippedOn;
    }

    public void setTippedOn(long tippedOn) {
        this.tippedOn = tippedOn;
    }

    public Double getDriverRating() {
        return driverRating;
    }

    public void setDriverRating(Double driverRating) {
        this.driverRating = driverRating;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Rider getRider() {
        return rider;
    }

    public void setRider(Rider rider) {
        this.rider = rider;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMinimumFare() {
        return minimumFare;
    }

    public void setMinimumFare(String minimumFare) {
        this.minimumFare = minimumFare;
    }

    public String getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(String baseFare) {
        this.baseFare = baseFare;
    }

    public String getRatePerMile() {
        return ratePerMile;
    }

    public void setRatePerMile(String ratePerMile) {
        this.ratePerMile = ratePerMile;
    }

    public String getRatePerMinute() {
        return ratePerMinute;
    }

    public void setRatePerMinute(String ratePerMinute) {
        this.ratePerMinute = ratePerMinute;
    }

    public String getEstimatedFare() {
        return estimatedFare;
    }

    public void setEstimatedFare(String estimatedFare) {
        this.estimatedFare = estimatedFare;
    }

    public Long getEstimatedTimeArrive() {
        return estimatedTimeArrive;
    }

    public void setEstimatedTimeArrive(Long estimatedTimeArrive) {
        this.estimatedTimeArrive = estimatedTimeArrive;
    }

    public Long getEstimatedTimeCompletion() {
        return estimatedTimeCompletion;
    }

    public void setEstimatedTimeCompletion(Long estimatedTimeCompletion) {
        this.estimatedTimeCompletion = estimatedTimeCompletion;
    }

    public String getBookingFee() {
        return bookingFee;
    }

    public void setBookingFee(String bookingFee) {
        this.bookingFee = bookingFee;
    }

    public Double getStartLocationLat() {
        return startLocationLat;
    }

    public void setStartLocationLat(Double startLocationLat) {
        this.startLocationLat = startLocationLat;
    }

    public Double getStartLocationLong() {
        return startLocationLong;
    }

    public void setStartLocationLong(Double startLocationLong) {
        this.startLocationLong = startLocationLong;
    }

    public Double getEndLocationLat() {
        return endLocationLat;
    }

    public void setEndLocationLat(Double endLocationLat) {
        this.endLocationLat = endLocationLat;
    }

    public Double getEndLocationLong() {
        return endLocationLong;
    }

    public void setEndLocationLong(Double endLocationLong) {
        this.endLocationLong = endLocationLong;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public Address getStart() {
        return start;
    }

    public void setStart(Address start) {
        this.start = start;
    }

    public Address getEnd() {
        return end;
    }

    public void setEnd(Address end) {
        this.end = end;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Double getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(Double totalFare) {
        this.totalFare = totalFare;
    }

    public Double getSurgeFactor() {
        return surgeFactor;
    }

    public void setSurgeFactor(Double surgeFactor) {
        this.surgeFactor = surgeFactor;
    }

    public String getSurgeFare() {
        return surgeFare;
    }

    public void setSurgeFare(String surgeFare) {
        this.surgeFare = surgeFare;
    }

    public double getDriverPayment() {
        return driverPayment;
    }

    public void setDriverPayment(Double driverPayment) {
        this.driverPayment = driverPayment;
    }

    public RequestedCarType getRequestedCarType() {
        return requestedCarType;
    }

    public void setRequestedCarType(RequestedCarType requestedCarType) {
        this.requestedCarType = requestedCarType;
    }

    public RequestedDriverType getRequestedDriverType() {
        return requestedDriverType;
    }

    public void setRequestedDriverType(RequestedDriverType requestedDriverType) {
        this.requestedDriverType = requestedDriverType;
    }

    public String getMapUrl() {
        return mapUrl;
    }

    public void setMapUrl(String mapUrl) {
        this.mapUrl = mapUrl;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public UpgradeRequest getUpgradeRequest() {
        return upgradeRequest;
    }

    public void setUpgradeRequest(UpgradeRequest upgradeRequest) {
        this.upgradeRequest = upgradeRequest;
    }

    public String getBevoBucksUrl() {
        return bevoBucksUrl;
    }

    public void setBevoBucksUrl(String bevoBucksUrl) {
        this.bevoBucksUrl = bevoBucksUrl;
    }

    public String getFreeCreditCharged() {
        return freeCreditCharged;
    }

    public void setFreeCreditCharged(String freeCreditCharged) {
        this.freeCreditCharged = freeCreditCharged;
    }

    public Ride getNextRide() {
        return nextRide;
    }

    public void setNextRide(Ride nextRide) {
        this.nextRide = nextRide;
    }

    public Ride getPrevRide() {
        return prevRide;
    }

    public void setPrevRide(Ride prevRide) {
        this.prevRide = prevRide;
    }

    public boolean hasActiveDriver() {
        return activeDriver != null;
    }

    public boolean hasOneOfStatuses(RideStatus... rideStatuses) {
        RideStatus currentRideStatus = RideStatus.valueOf(getStatus());
        for (RideStatus rideStatus : rideStatuses) {
            if (currentRideStatus == rideStatus) {
                return true;
            }
        }
        return false;
    }

    public boolean isFreeCreditsCharged() {
        if (TextUtils.isEmpty(freeCreditCharged)) return false;
        try {
            double credits = Double.parseDouble(freeCreditCharged);
            return credits > 0.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isDirectConnectRequest() {
        return requestedDriverType != null
                && Constants.DIRECT_CONNECT_DRIVER_TYPE.equalsIgnoreCase(requestedDriverType.getName());
    }

    public boolean isFemaleDriverRequest() {
        return requestedDriverType != null
                && Constants.WOMEN_ONLY_DRIVER_TYPE.equalsIgnoreCase(requestedDriverType.getName());
    }

    public Boolean getTippingAllowed() {
        return tippingAllowed;
    }

    public void setTippingAllowed(Boolean tippingAllowed) {
        this.tippingAllowed = tippingAllowed;
    }

    public Long getTippingUntil() {
        return tippingUntil;
    }

    public void setTippingUntil(Long tippingUntil) {
        this.tippingUntil = tippingUntil;
    }

    @Override
    public String toString() {
        return "Ride{" +
                "activeDriver=" + activeDriver +
                ", freeCancellationExpiresOn=" + freeCancellationExpiresOn +
                ", driverAcceptedOn=" + driverAcceptedOn +
                ", completedOn=" + completedOn +
                ", cancelledOn=" + cancelledOn +
                ", stripeCreditCharge='" + stripeCreditCharge + '\'' +
                ", tip='" + tip + '\'' +
                ", tippedOn=" + tippedOn +
                ", driverRating=" + driverRating +
                ", id=" + id +
                ", rider=" + rider +
                ", status='" + status + '\'' +
                ", minimumFare='" + minimumFare + '\'' +
                ", baseFare='" + baseFare + '\'' +
                ", ratePerMile='" + ratePerMile + '\'' +
                ", ratePerMinute='" + ratePerMinute + '\'' +
                ", estimatedFare='" + estimatedFare + '\'' +
                ", estimatedTimeArrive=" + estimatedTimeArrive +
                ", estimatedTimeCompletion=" + estimatedTimeCompletion +
                ", bookingFee='" + bookingFee + '\'' +
                ", startLocationLat=" + startLocationLat +
                ", startLocationLong=" + startLocationLong +
                ", endLocationLat=" + endLocationLat +
                ", endLocationLong=" + endLocationLong +
                ", startAddress='" + startAddress + '\'' +
                ", endAddress='" + endAddress + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", uuid='" + uuid + '\'' +
                ", totalFare=" + totalFare +
                ", surgeFactor=" + surgeFactor +
                ", surgeFare='" + surgeFare + '\'' +
                ", driverPayment=" + driverPayment +
                ", requestedCarType=" + requestedCarType +
                ", requestedDriverType=" + requestedDriverType +
                ", comment='" + comment + '\'' +
                ", upgradeRequest=" + upgradeRequest +
                ", bevoBucksUrl='" + bevoBucksUrl + '\'' +
                ", freeCreditCharged='" + freeCreditCharged + '\'' +
                ", nextRide=" + nextRide +
                ", prevRide=" + prevRide +
                ", mapUrl='" + mapUrl + '\'' +
                ", tippingAllowed=" + tippingAllowed +
                ", tippingUntil=" + tippingUntil +
                '}';
    }
}
