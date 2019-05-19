
package com.rideaustin.api.config.v2_6_1;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GlobalConfig {

    @SerializedName("driverRegistration")
    @Expose
    private DriverRegistration driverRegistration;
    @SerializedName("referFriend")
    @Expose
    private ReferFriend referFriend;
    @SerializedName("tipping")
    @Expose
    private Tipping tipping;
    @SerializedName("supportedCities")
    @Expose
    private List<SupportedCity> supportedCities = new ArrayList<SupportedCity>();
    @SerializedName("smsMaskingEnabled")
    @Expose
    private Boolean smsMaskingEnabled;
    @SerializedName("rideCancellation")
    @Expose
    private RideCancellation rideCancellation;
    @SerializedName("currentCity")
    @Expose
    private CurrentCity currentCity;
    @SerializedName("generalInformation")
    @Expose
    private GeneralInformation generalInformation;
    @SerializedName("womanOnly")
    @Expose
    private WomanOnly womanOnly;
    @SerializedName("riderReferFriend")
    @Expose
    private RiderReferFriend riderReferFriend;
    @SerializedName("geocodingConfiguration")
    @Expose
    private GeocodingConfiguration geocodingConfiguration;
    @SerializedName("directConnectPhone")
    @Expose
    private String directConnectPhone;

    /**
     * @return The directConnectPhone
     */
    public String getDirectConnectPhone() {
        return directConnectPhone;
    }

    /**
     * @param directConnectPhone The directConnectPhone
     */
    public void setDirectConnectPhone(String directConnectPhone) {
        this.directConnectPhone = directConnectPhone;
    }

    /**
     * @return The geocodingConfiguration
     */
    public GeocodingConfiguration getGeocodingConfiguration() {
        return geocodingConfiguration;
    }

    /**
     * @param geocodingConfiguration The geocodingConfiguration
     */
    public void setGeocodingConfiguration(GeocodingConfiguration geocodingConfiguration) {
        this.geocodingConfiguration = geocodingConfiguration;
    }

    /**
     * @return The driverRegistration
     */
    public DriverRegistration getDriverRegistration() {
        return driverRegistration;
    }

    /**
     * @param driverRegistration The driverRegistration
     */
    public void setDriverRegistration(DriverRegistration driverRegistration) {
        this.driverRegistration = driverRegistration;
    }

    /**
     * @return The referFriend
     */
    public ReferFriend getReferFriend() {
        return referFriend;
    }

    /**
     * @param referFriend The referFriend
     */
    public void setReferFriend(ReferFriend referFriend) {
        this.referFriend = referFriend;
    }

    /**
     * @return The tipping
     */
    public Tipping getTipping() {
        return tipping;
    }

    /**
     * @param tipping The tipping
     */
    public void setTipping(Tipping tipping) {
        this.tipping = tipping;
    }

    /**
     * @return The supportedCities
     */
    public List<SupportedCity> getSupportedCities() {
        return supportedCities;
    }

    /**
     * @param supportedCities The supportedCities
     */
    public void setSupportedCities(List<SupportedCity> supportedCities) {
        this.supportedCities = supportedCities;
    }

    /**
     *
     * @return
     * The smsMaskingEnabled
     */
    public Boolean isSmsMaskingEnabled() {
        return smsMaskingEnabled;
    }

    /**
     *
     * @param smsMaskingEnabled
     * The smsMaskingEnabled
     */
    public void setSmsMaskingEnabled(Boolean smsMaskingEnabled) {
        this.smsMaskingEnabled = smsMaskingEnabled;
    }

    /**
     * @return The rideCancellation
     */
    public RideCancellation getRideCancellation() {
        return rideCancellation;
    }

    /**
     * @param rideCancellation The rideCancellation
     */
    public void setRideCancellation(RideCancellation rideCancellation) {
        this.rideCancellation = rideCancellation;
    }

    /**
     * @return The currentCity
     */
    public CurrentCity getCurrentCity() {
        return currentCity;
    }

    /**
     * @param currentCity The currentCity
     */
    public void setCurrentCity(CurrentCity currentCity) {
        this.currentCity = currentCity;
    }

    /**
     * @return The generalInformation
     */
    public GeneralInformation getGeneralInformation() {
        return generalInformation;
    }

    /**
     * @param generalInformation The generalInformation
     */
    public void setGeneralInformation(GeneralInformation generalInformation) {
        this.generalInformation = generalInformation;
    }

    /**
     * @return The womanOnly
     */
    public WomanOnly getWomanOnly() {
        return womanOnly;
    }

    /**
     * @param womanOnly The womanOnly
     */
    public void setWomanOnly(WomanOnly womanOnly) {
        this.womanOnly = womanOnly;
    }

    /**
     * @return The riderReferFriend
     */
    public RiderReferFriend getRiderReferFriend() {
        return riderReferFriend;
    }

    /**
     * @param riderReferFriend The riderReferFriend
     */
    public void setRiderReferFriend(RiderReferFriend riderReferFriend) {
        this.riderReferFriend = riderReferFriend;
    }

}
