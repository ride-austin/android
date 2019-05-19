
package com.rideaustin.api.model.rideupgrade;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class RideUpgradeMessage implements Serializable {

    @SerializedName("rideId")
    private Long rideId;
    @SerializedName("sound")
    private String sound;
    @SerializedName("source")
    private String source;
    @SerializedName("target")
    private String target;

    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

}
