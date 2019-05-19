
package com.rideaustin.api.config;

import com.google.gson.annotations.SerializedName;
import com.rideaustin.api.config.ut.UT;
import com.rideaustin.api.model.driver.CurrentTerms;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.api.model.driver.RequestedDriverType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import java8.util.Optional;

import static com.rideaustin.utils.CommonConstants.FINGERPRINTED_DRIVER_TYPE;
import static com.rideaustin.utils.CommonConstants.WOMEN_ONLY_DRIVER_TYPE;

public class GlobalConfig implements Serializable {

    @SerializedName("referFriend")
    private ReferFriend referFriend;

    @SerializedName("tipping")
    private Tipping tipping;

    @SerializedName("accessibility")
    private Accessibility accessibility;

    @SerializedName("smsMaskingEnabled")
    private Boolean smsMaskingEnabled;

    @SerializedName("supportedCities")
    private List<SupportedCity> supportedCities = null;

    @SerializedName("directConnectPhone")
    private String directConnectPhone;

    @SerializedName("geocodingConfiguration")
    private GeocodingConfiguration geocodingConfiguration;

    @SerializedName("rideCancellation")
    private RideCancellation rideCancellation;

    @SerializedName("currentCity")
    private CurrentCity currentCity;

    @SerializedName("generalInformation")
    private GeneralInformation generalInformation;

    @SerializedName("rides")
    private Rides rides;

    @SerializedName("riderReferFriend")
    private RiderReferFriend riderReferFriend;

    @SerializedName("riderLiveLocation")
    private RiderLiveLocation riderLiveLocation;

    @SerializedName("currentTerms")
    private CurrentTerms currentTerms;

    @SerializedName("rideUpgrade")
    private RideUpgrade rideUpgrade;

    @SerializedName("rideAcceptance")
    private RideAcceptance rideAcceptance;

    @SerializedName("locationUpdateIntervals")
    private LocationUpdateIntervals locationUpdateIntervals;

    @SerializedName("UT")
    private UT ut;

    @SerializedName("unpaidBalance")
    private UnpaidConfig unpaidConfig;

    @SerializedName("carTypes")
    private List<RequestedCarType> carTypes = new ArrayList<>();

    // NOTE: not available for driver yet
    @SerializedName("driverTypes")
    private List<RequestedDriverType> driverTypes = new ArrayList<>();

    @SerializedName("genderSelection")
    private GenderSelection genderSelection;

    @SerializedName("online")
    private Online online;

    @SerializedName("autoGoOffline")
    private AutoGoOffline autoGoOffline;

    @SerializedName("driverStats")
    private DriverStatsConfig driverStatsConfig;

    @SerializedName("directConnect")
    private DirectConnectConfig directConnectConfig;

    @SerializedName("cancellationFeedback")
    private RideCancellationConfig rideCancellationConfig;

    private DriverActions driverActions;

    private List<CampaignProvider> campaignProviders = new ArrayList<>();

    private transient boolean isEmbedded;

    private final static long serialVersionUID = -1238343532673324249L;

    public UnpaidConfig getUnpaidConfig() {
        return unpaidConfig;
    }

    public UT getUt() {
        return ut;
    }

    public void setUt(UT ut) {
        this.ut = ut;
    }

    public LocationUpdateIntervals getLocationUpdateIntervals() {
        return locationUpdateIntervals;
    }

    public void setLocationUpdateIntervals(LocationUpdateIntervals locationUpdateIntervals) {
        this.locationUpdateIntervals = locationUpdateIntervals;
    }

    public RideAcceptance getRideAcceptance() {
        return rideAcceptance;
    }

    public void setRideAcceptance(RideAcceptance rideAcceptance) {
        this.rideAcceptance = rideAcceptance;
    }

    public RideUpgrade getRideUpgrade() {
        return rideUpgrade;
    }

    public RiderLiveLocation getRiderLiveLocation() {
        return riderLiveLocation;
    }

    public void setRiderLiveLocation(RiderLiveLocation riderLiveLocation) {
        this.riderLiveLocation = riderLiveLocation;
    }

    public ReferFriend getReferFriend() {
        return referFriend;
    }

    public void setReferFriend(ReferFriend referFriend) {
        this.referFriend = referFriend;
    }

    public Tipping getTipping() {
        return tipping;
    }

    public void setTipping(Tipping tipping) {
        this.tipping = tipping;
    }

    public Accessibility getAccessibility() {
        return accessibility;
    }

    public void setAccessibility(Accessibility accessibility) {
        this.accessibility = accessibility;
    }

    public Boolean isSmsMaskingEnabled() {
        return smsMaskingEnabled;
    }

    public void setSmsMaskingEnabled(Boolean smsMaskingEnabled) {
        this.smsMaskingEnabled = smsMaskingEnabled;
    }

    public List<SupportedCity> getSupportedCities() {
        return supportedCities;
    }

    public void setSupportedCities(List<SupportedCity> supportedCities) {
        this.supportedCities = supportedCities;
    }

    public String getDirectConnectPhone() {
        return directConnectPhone;
    }

    public void setDirectConnectPhone(String directConnectPhone) {
        this.directConnectPhone = directConnectPhone;
    }

    public GeocodingConfiguration getGeocodingConfiguration() {
        return geocodingConfiguration;
    }

    public void setGeocodingConfiguration(GeocodingConfiguration geocodingConfiguration) {
        this.geocodingConfiguration = geocodingConfiguration;
    }

    public RideCancellation getRideCancellation() {
        return rideCancellation;
    }

    public void setRideCancellation(RideCancellation rideCancellation) {
        this.rideCancellation = rideCancellation;
    }

    public CurrentCity getCurrentCity() {
        return currentCity;
    }

    public void setCurrentCity(CurrentCity currentCity) {
        this.currentCity = currentCity;
    }

    public GeneralInformation getGeneralInformation() {
        return generalInformation;
    }

    public void setGeneralInformation(GeneralInformation generalInformation) {
        this.generalInformation = generalInformation;
    }

    public Rides getRides() {
        return rides;
    }

    public void setRides(Rides rides) {
        this.rides = rides;
    }

    public RiderReferFriend getRiderReferFriend() {
        return riderReferFriend;
    }

    public void setRiderReferFriend(RiderReferFriend riderReferFriend) {
        this.riderReferFriend = riderReferFriend;
    }

    public static long getVersion() {
        return serialVersionUID;
    }

    public CurrentTerms getCurrentTerms() {
        return currentTerms;
    }

    public void setCurrentTerms(CurrentTerms currentTerms) {
        this.currentTerms = currentTerms;
    }

    public List<RequestedCarType> getCarTypes() {
        return carTypes;
    }

    public void setCarTypes(List<RequestedCarType> carTypes) {
        this.carTypes = carTypes;
    }

    public List<RequestedDriverType> getDriverTypes() {
        return driverTypes;
    }

    public void setDriverTypes(List<RequestedDriverType> driverTypes) {
        this.driverTypes = driverTypes;
    }

    public boolean isEmbedded() {
        return isEmbedded;
    }

    public void setEmbedded(boolean embedded) {
        isEmbedded = embedded;
    }

    public Optional<RequestedDriverType> getFemaleDriverType() {
        return Optional.ofNullable(driverTypes).map(list -> {
            for (RequestedDriverType type : list) {
                if (WOMEN_ONLY_DRIVER_TYPE.equalsIgnoreCase(type.getName())) {
                    return type;
                }
            }
            return null;
        });
    }

    public Optional<RequestedDriverType> getFingerprintedDriverType() {
        return Optional.ofNullable(driverTypes).map(list -> {
            for (RequestedDriverType type : list) {
                if (FINGERPRINTED_DRIVER_TYPE.equalsIgnoreCase(type.getName())) {
                    return type;
                }
            }
            return null;
        });
    }

    public Online getOnline() {
        return online;
    }

    public void setOnline(Online online) {
        this.online = online;
    }

    public GenderSelection getGenderSelection() {
        return genderSelection;
    }

    public AutoGoOffline getAutoGoOffline() {
        return autoGoOffline;
    }

    public DriverStatsConfig getDriverStatsConfig() {
        return driverStatsConfig;
    }

    public DirectConnectConfig getDirectConnectConfig() {
        return directConnectConfig;
    }

    public void setDirectConnectConfig(DirectConnectConfig directConnectConfig) {
        this.directConnectConfig = directConnectConfig;
    }

    public RideCancellationConfig getRideCancellationConfig() {
        return rideCancellationConfig;
    }

    public void setRideCancellationConfig(RideCancellationConfig rideCancellationConfig) {
        this.rideCancellationConfig = rideCancellationConfig;
    }

    public List<CampaignProvider> getCampaignProviders() {
        return campaignProviders;
    }

    public void setCampaignProviders(List<CampaignProvider> campaignProviders) {
        this.campaignProviders = campaignProviders;
    }

    public DriverActions getDriverActions() {
        return driverActions;
    }

    public void setDriverActions(DriverActions driverActions) {
        this.driverActions = driverActions;
    }
}
