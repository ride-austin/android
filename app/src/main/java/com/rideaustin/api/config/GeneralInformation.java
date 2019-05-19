
package com.rideaustin.api.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class GeneralInformation implements Serializable {

    @SerializedName("splashUrl")
    @Expose
    private String splashUrl;
    @SerializedName("companyDomain")
    @Expose
    private String companyDomain;
    @SerializedName("playStoreLink")
    @Expose
    private String playStoreLink;

    /**
     * White logo for dark background
     */
    @SerializedName("logoUrl")
    @Expose
    private String logoUrl;
    @SerializedName("appstoreLink")
    @Expose
    private String appstoreLink;
    @SerializedName("legalRider")
    @Expose
    private String legalRider;
    @SerializedName("supportEmail")
    @Expose
    private String supportEmail;
    @SerializedName("facebookUrl")
    @Expose
    private String facebookUrl;
    @SerializedName("legalDriver")
    @Expose
    private String legalDriver;
    @SerializedName("playStoreWeb")
    @Expose
    private String playStoreWeb;
    @SerializedName("applicationNamePipe")
    @Expose
    private String applicationNamePipe;
    @SerializedName("facebookUrlSchemeiOS")
    @Expose
    private String facebookUrlSchemeiOS;

    /**
     * Black logo for light background
     */
    @SerializedName("logoBlackUrl")
    @Expose
    private String logoBlackUrl;
    @SerializedName("iconUrl")
    @Expose
    private String iconUrl;
    @SerializedName("companyWebsite")
    @Expose
    private String companyWebsite;
    @SerializedName("applicationName")
    @Expose
    private String applicationName;
    public final static long serialVersionUID = -2536899520919963846L;

    public String getSplashUrl() {
        return splashUrl;
    }

    public void setSplashUrl(String splashUrl) {
        this.splashUrl = splashUrl;
    }

    public String getCompanyDomain() {
        return companyDomain;
    }

    public void setCompanyDomain(String companyDomain) {
        this.companyDomain = companyDomain;
    }

    public String getPlayStoreLink() {
        return playStoreLink;
    }

    public void setPlayStoreLink(String playStoreLink) {
        this.playStoreLink = playStoreLink;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getAppstoreLink() {
        return appstoreLink;
    }

    public void setAppstoreLink(String appstoreLink) {
        this.appstoreLink = appstoreLink;
    }

    public String getLegalRider() {
        return legalRider;
    }

    public void setLegalRider(String legalRider) {
        this.legalRider = legalRider;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }

    public String getFacebookUrl() {
        return facebookUrl;
    }

    public void setFacebookUrl(String facebookUrl) {
        this.facebookUrl = facebookUrl;
    }

    public String getLegalDriver() {
        return legalDriver;
    }

    public void setLegalDriver(String legalDriver) {
        this.legalDriver = legalDriver;
    }

    public String getPlayStoreWeb() {
        return playStoreWeb;
    }

    public void setPlayStoreWeb(String playStoreWeb) {
        this.playStoreWeb = playStoreWeb;
    }

    public String getApplicationNamePipe() {
        return applicationNamePipe;
    }

    public void setApplicationNamePipe(String applicationNamePipe) {
        this.applicationNamePipe = applicationNamePipe;
    }

    public String getFacebookUrlSchemeiOS() {
        return facebookUrlSchemeiOS;
    }

    public void setFacebookUrlSchemeiOS(String facebookUrlSchemeiOS) {
        this.facebookUrlSchemeiOS = facebookUrlSchemeiOS;
    }

    public String getLogoBlackUrl() {
        return logoBlackUrl;
    }

    public void setLogoBlackUrl(String logoBlackUrl) {
        this.logoBlackUrl = logoBlackUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getCompanyWebsite() {
        return companyWebsite;
    }

    public void setCompanyWebsite(String companyWebsite) {
        this.companyWebsite = companyWebsite;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
}