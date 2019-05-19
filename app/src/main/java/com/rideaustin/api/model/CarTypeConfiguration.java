package com.rideaustin.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CarTypeConfiguration {

    @SerializedName("allowedPolygons")
    @Expose
    private List<NamedPolygon> allowedPolygons = null;

    @SerializedName("restrictedZipCodes")
    private List<String> restrictedZipCodes;

    // default false value in case server will not return this value
    @SerializedName("showAlert")
    private boolean showAlert;

    // default false value in case server will not return this value
    @SerializedName("disableTipping")
    private boolean disableTipping;

    @SerializedName("zeroFareLabel")
    private String zeroFareLabel;

    @SerializedName("available")
    private Available available;

    @SerializedName("disableCharge")
    private Boolean disableCharge;

    public List<String> getRestrictedZipCodes() {
        return restrictedZipCodes;
    }

    public void setRestrictedZipCodes(List<String> restrictedZipCodes) {
        this.restrictedZipCodes = restrictedZipCodes;
    }

    public boolean getShowAlert() {
        return showAlert;
    }

    public void setShowAlert(Boolean showAlert) {
        this.showAlert = showAlert;
    }

    public String getZeroFareLabel() {
        return zeroFareLabel;
    }

    public void setZeroFareLabel(String zeroFareLabel) {
        this.zeroFareLabel = zeroFareLabel;
    }

    public List<NamedPolygon> getAllowedPolygons() {
        return allowedPolygons;
    }

    public void setAllowedPolygons(List<NamedPolygon> allowedPolygons) {
        this.allowedPolygons = allowedPolygons;
    }

    public boolean isShowAlert() {
        return showAlert;
    }

    public void setShowAlert(boolean showAlert) {
        this.showAlert = showAlert;
    }

    public boolean isDisableTipping() {
        return disableTipping;
    }

    public void setDisableTipping(boolean disableTipping) {
        this.disableTipping = disableTipping;
    }

    public Available getAvailable() {
        return available;
    }

    public void setAvailable(Available available) {
        this.available = available;
    }

    public Boolean getDisableCharge() {
        return disableCharge;
    }

    public void setDisableCharge(Boolean disableCharge) {
        this.disableCharge = disableCharge;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CarTypeConfiguration that = (CarTypeConfiguration) o;

        if (showAlert != that.showAlert) return false;
        if (disableTipping != that.disableTipping) return false;
        if (restrictedZipCodes != null ? !restrictedZipCodes.equals(that.restrictedZipCodes) : that.restrictedZipCodes != null)
            return false;
        return zeroFareLabel != null ? zeroFareLabel.equals(that.zeroFareLabel) : that.zeroFareLabel == null;

    }

    @Override
    public int hashCode() {
        int result = restrictedZipCodes != null ? restrictedZipCodes.hashCode() : 0;
        result = 31 * result + (showAlert ? 1 : 0);
        result = 31 * result + (disableTipping ? 1 : 0);
        result = 31 * result + (zeroFareLabel != null ? zeroFareLabel.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CarTypeConfiguration{" +
                "restrictedZipCodes=" + restrictedZipCodes +
                ", showAlert=" + showAlert +
                ", disableTipping=" + disableTipping +
                ", zeroFareLabel='" + zeroFareLabel + '\'' +
                '}';
    }
}