
package com.rideaustin.api.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RiderReferFriend implements Serializable {

    @SerializedName("detailtexttemplate")
    @Expose
    private String detailtexttemplate;
    @SerializedName("emailbodytemplate")
    @Expose
    private String emailbodytemplate;
    @SerializedName("downloadUrl")
    @Expose
    private String downloadUrl;
    @SerializedName("smsbodytemplate")
    @Expose
    private String smsbodytemplate;
    @SerializedName("enabled")
    @Expose
    private Boolean enabled;

    public final static long serialVersionUID = 6227540503687335256L;

    public String getDetailtexttemplate() {
        return detailtexttemplate;
    }

    public void setDetailtexttemplate(String detailtexttemplate) {
        this.detailtexttemplate = detailtexttemplate;
    }

    public String getEmailbodytemplate() {
        return emailbodytemplate;
    }

    public void setEmailbodytemplate(String emailbodytemplate) {
        this.emailbodytemplate = emailbodytemplate;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getSmsbodytemplate() {
        return smsbodytemplate;
    }

    public void setSmsbodytemplate(String smsbodytemplate) {
        this.smsbodytemplate = smsbodytemplate;
    }

    public boolean isEnabled() {
        return enabled != null && enabled;
    }
}