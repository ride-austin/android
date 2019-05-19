package com.rideaustin.manager.notification;

import com.google.gson.annotations.SerializedName;
import com.rideaustin.utils.Constants;

/**
 * Created by Sergey Petrov on 03/03/2017.
 */

public class SplitFareMessage extends InAppMessage {

    @SerializedName("sound")
    private String sound;

    @SerializedName("sourceUser")
    private String sourceUser;

    @SerializedName("sourceUserPhoto")
    private String sourceUserPhoto;

    @SerializedName("targetUser")
    private String targetUser;

    @SerializedName("targetUserPhoto")
    private String targetUserPhoto;

    @SerializedName("splitFareId")
    private String splitFareId;

    @Constants.SplitFare
    private String type;

    public @Constants.SplitFare String getType() {
        return type;
    }

    public void setType(@Constants.SplitFare String type) {
        this.type = type;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String getSourceUser() {
        return sourceUser;
    }

    public void setSourceUser(String sourceUser) {
        this.sourceUser = sourceUser;
    }

    public String getSourceUserPhoto() {
        return sourceUserPhoto;
    }

    public void setSourceUserPhoto(String sourceUserPhoto) {
        this.sourceUserPhoto = sourceUserPhoto;
    }

    public String getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(String targetUser) {
        this.targetUser = targetUser;
    }

    public String getTargetUserPhoto() {
        return targetUserPhoto;
    }

    public void setTargetUserPhoto(String targetUserPhoto) {
        this.targetUserPhoto = targetUserPhoto;
    }

    public String getSplitFareId() {
        return splitFareId;
    }

    public void setSplitFareId(String splitFareId) {
        this.splitFareId = splitFareId;
    }

}
