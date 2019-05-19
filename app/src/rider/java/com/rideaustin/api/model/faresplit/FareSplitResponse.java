package com.rideaustin.api.model.faresplit;

import com.google.gson.annotations.SerializedName;

public class FareSplitResponse {

    public enum SplitFareState {
        REQUESTED, ACCEPTED, DECLINED
    }

    @SerializedName("id")
    private long id;

    @SerializedName("rideId")
    private long rideId;

    @SerializedName("riderId")
    private long riderId;

    @SerializedName("riderFullName")
    private String riderFullName;

    @SerializedName("riderPhoto")
    private String riderPhoto;

    @SerializedName("status")
    private String status;

    public long getId() {
        return id;
    }

    public long getRideId() {
        return rideId;
    }

    public String getRiderFullName() {
        return riderFullName;
    }

    public long getRiderId() {
        return riderId;
    }

    public String getRiderPhoto() {
        return riderPhoto;
    }

    public SplitFareState getStatus() {
        return SplitFareState.valueOf(status);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "FareSplitResponse{" +
                "id=" + id +
                ", rideId=" + rideId +
                ", riderId=" + riderId +
                ", riderFullName='" + riderFullName + '\'' +
                ", riderPhoto='" + riderPhoto + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}