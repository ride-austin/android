
package com.rideaustin.api.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class SupportedCity implements Serializable {

    public static final long serialVersionUID = 6855631824492187179L;
    @SerializedName("cityId")
    @Expose
    private Integer cityId;
    @SerializedName("cityName")
    @Expose
    private String cityName;
    @SerializedName("logoUrl")
    @Expose
    private String logoUrl;
    @SerializedName("logoBlackUrl")
    @Expose
    private String logoBlackUrl;

    /**
     * @return The cityId
     */
    public Integer getCityId() {
        return cityId;
    }

    /**
     * @param cityId The cityId
     */
    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    /**
     * @return The cityName
     */
    public String getCityName() {
        return cityName;
    }

    /**
     * @param cityName The cityName
     */
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getLogoBlackUrl() {
        return logoBlackUrl;
    }

    public void setLogoBlackUrl(String logoBlackUrl) {
        this.logoBlackUrl = logoBlackUrl;
    }

}
