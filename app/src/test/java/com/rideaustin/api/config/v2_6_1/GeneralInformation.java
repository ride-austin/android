
package com.rideaustin.api.config.v2_6_1;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GeneralInformation {

    @SerializedName("splashUrl")
    @Expose
    private String splashUrl;
    @SerializedName("applicationNamePipe")
    @Expose
    private String applicationNamePipe;
    @SerializedName("iconUrl")
    @Expose
    private String iconUrl;
    @SerializedName("applicationName")
    @Expose
    private String applicationName;
    @SerializedName("logoUrl")
    @Expose
    private String logoUrl;
    @SerializedName("logoBlackUrl")
    @Expose
    private String logoBlackUrl;

    /**
     *
     * @return
     *     The splashUrl
     */
    public String getSplashUrl() {
        return splashUrl;
    }

    /**
     *
     * @param splashUrl
     *     The splashUrl
     */
    public void setSplashUrl(String splashUrl) {
        this.splashUrl = splashUrl;
    }

    /**
     *
     * @return
     *     The applicationNamePipe
     */
    public String getApplicationNamePipe() {
        return applicationNamePipe;
    }

    /**
     * @param applicationNamePipe The applicationNamePipe
     */
    public void setApplicationNamePipe(String applicationNamePipe) {
        this.applicationNamePipe = applicationNamePipe;
    }

    /**
     * @return The iconUrl
     */
    public String getIconUrl() {
        return iconUrl;
    }

    /**
     * 
     * @param iconUrl
     *     The iconUrl
     */
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    /**
     * 
     * @return
     *     The applicationName
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * 
     * @param applicationName
     *     The applicationName
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * 
     * @return
     *     The logoUrl
     */
    public String getLogoUrl() {
        return logoUrl;
    }

    /**
     * 
     * @param logoUrl
     *     The logoUrl
     */
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getLogoBlackUrl() {
        return logoBlackUrl;
    }

    public void setLogoBlackUrl(final String logoBlackUrl) {
        this.logoBlackUrl = logoBlackUrl;
    }
}
