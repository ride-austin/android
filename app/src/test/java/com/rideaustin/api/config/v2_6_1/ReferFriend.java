
package com.rideaustin.api.config.v2_6_1;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReferFriend {

    @SerializedName("emailEnabled")
    @Expose
    private Boolean emailEnabled;
    @SerializedName("smsEnabled")
    @Expose
    private Boolean smsEnabled;
    @SerializedName("header")
    @Expose
    private String header;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("body")
    @Expose
    private String body;

    /**
     * 
     * @return
     *     The emailEnabled
     */
    public Boolean getEmailEnabled() {
        return emailEnabled;
    }

    /**
     *
     * @param emailEnabled
     *     The emailEnabled
     */
    public void setEmailEnabled(Boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    /**
     * 
     * @return
     *     The smsEnabled
     */
    public Boolean getSmsEnabled() {
        return smsEnabled;
    }

    /**
     * 
     * @param smsEnabled
     *     The smsEnabled
     */
    public void setSmsEnabled(Boolean smsEnabled) {
        this.smsEnabled = smsEnabled;
    }

    /**
     * 
     * @return
     *     The header
     */
    public String getHeader() {
        return header;
    }

    /**
     *
     * @param header
     *     The header
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * 
     * @return
     *     The title
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @param title
     *     The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 
     * @return
     *     The body
     */
    public String getBody() {
        return body;
    }

    /**
     * 
     * @param body
     *     The body
     */
    public void setBody(String body) {
        this.body = body;
    }

}
