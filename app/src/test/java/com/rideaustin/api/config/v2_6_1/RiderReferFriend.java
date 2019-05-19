
package com.rideaustin.api.config.v2_6_1;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RiderReferFriend {

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

    /**
     * @return The detailtexttemplate
     */
    public String getDetailtexttemplate() {
        return detailtexttemplate;
    }

    /**
     * @param detailtexttemplate The detailtexttemplate
     */
    public void setDetailtexttemplate(String detailtexttemplate) {
        this.detailtexttemplate = detailtexttemplate;
    }

    /**
     * @return The emailbodytemplate
     */
    public String getEmailbodytemplate() {
        return emailbodytemplate;
    }

    /**
     * @param emailbodytemplate The emailbodytemplate
     */
    public void setEmailbodytemplate(String emailbodytemplate) {
        this.emailbodytemplate = emailbodytemplate;
    }

    /**
     * @return The downloadUrl
     */
    public String getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * @param downloadUrl The downloadUrl
     */
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    /**
     * @return The smsbodytemplate
     */
    public String getSmsbodytemplate() {
        return smsbodytemplate;
    }

    /**
     * @param smsbodytemplate The smsbodytemplate
     */
    public void setSmsbodytemplate(String smsbodytemplate) {
        this.smsbodytemplate = smsbodytemplate;
    }

}
