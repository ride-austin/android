package com.rideaustin.api.model.driver;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.rideaustin.api.model.CarTypeConfiguration;
import com.rideaustin.utils.SerializationHelper;

import java.io.Serializable;

public class RequestedCarType implements Serializable {

    private static final long serialVersionUID = -2098159976874825428L;

    @SerializedName("configuration")
    private String configuration;

    @SerializedName("tncFeeRate")
    private Float tncFeeRate;

    @SerializedName("processingFee")
    private String processingFee;

    @SerializedName("carCategory")
    private String carCategory;

    @SerializedName("minimumFare")
    private String minimumFare;

    @SerializedName("baseFare")
    private String baseFare;

    @SerializedName("bookingFee")
    private String bookingFee;

    @SerializedName("raFeeFactor")
    private Float raFeeFactor;

    @SerializedName("ratePerMile")
    private String ratePerMile;

    @SerializedName("ratePerMinute")
    private String ratePerMinute;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("iconUrl")
    private String iconUrl;

    @SerializedName("plainIconUrl")
    private String plainIconUrl;

    @SerializedName("mapIconUrl")
    private String mapIconUrl;

    @SerializedName("fullIconUrl")
    private String fullIconUrl;

    @SerializedName("maxPersons")
    private Integer maxPersons;

    @SerializedName("order")
    private Integer order;

    @SerializedName("cancellationFee")
    private String cancellationFee;

    private double surgeFactor;

    public RequestedCarType(String carCategory, String title) {
        this.carCategory = carCategory;
        this.title = title;
    }

    public CarTypeConfiguration getConfiguration() {
        if (TextUtils.isEmpty(configuration)) {
            return null;
        }
        return SerializationHelper.deSerialize(configuration, CarTypeConfiguration.class);
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public Float getTncFeeRate() {
        return tncFeeRate;
    }

    public void setTncFeeRate(Float tncFeeRate) {
        this.tncFeeRate = tncFeeRate;
    }

    public String getProcessingFee() {
        return processingFee;
    }

    public void setProcessingFee(String processingFee) {
        this.processingFee = processingFee;
    }

    public String getCarCategory() {
        return carCategory;
    }

    public void setCarCategory(String carCategory) {
        this.carCategory = carCategory;
    }

    public String getMinimumFare() {
        return minimumFare;
    }

    public void setMinimumFare(String minimumFare) {
        this.minimumFare = minimumFare;
    }

    public String getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(String baseFare) {
        this.baseFare = baseFare;
    }

    public String getBookingFee() {
        return bookingFee;
    }

    public void setBookingFee(String bookingFee) {
        this.bookingFee = bookingFee;
    }

    public Float getRaFeeFactor() {
        return raFeeFactor;
    }

    public void setRaFeeFactor(Float raFeeFactor) {
        this.raFeeFactor = raFeeFactor;
    }

    public String getRatePerMile() {
        return ratePerMile;
    }

    public void setRatePerMile(String ratePerMile) {
        this.ratePerMile = ratePerMile;
    }

    public String getRatePerMinute() {
        return ratePerMinute;
    }

    public void setRatePerMinute(String ratePerMinute) {
        this.ratePerMinute = ratePerMinute;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getPlainIconUrl() {
        return !TextUtils.isEmpty(plainIconUrl) ? plainIconUrl : iconUrl;
    }

    public String getMapIconUrl() {
        return mapIconUrl;
    }

    public String getFullIconUrl() {
        return fullIconUrl;
    }

    public void setFullIconUrl(String fullIconUrl) {
        this.fullIconUrl = fullIconUrl;
    }

    public Integer getMaxPersons() {
        return maxPersons;
    }

    public void setMaxPersons(Integer maxPersons) {
        this.maxPersons = maxPersons;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getCancellationFee() {
        return cancellationFee;
    }

    public void setCancellationFee(String cancellationFee) {
        this.cancellationFee = cancellationFee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RequestedCarType)) return false;

        RequestedCarType that = (RequestedCarType) o;

        if (configuration != null ? !configuration.equals(that.configuration) : that.configuration != null)
            return false;
        if (tncFeeRate != null ? !tncFeeRate.equals(that.tncFeeRate) : that.tncFeeRate != null)
            return false;
        if (processingFee != null ? !processingFee.equals(that.processingFee) : that.processingFee != null)
            return false;
        if (carCategory != null ? !carCategory.equals(that.carCategory) : that.carCategory != null)
            return false;
        if (minimumFare != null ? !minimumFare.equals(that.minimumFare) : that.minimumFare != null)
            return false;
        if (baseFare != null ? !baseFare.equals(that.baseFare) : that.baseFare != null)
            return false;
        if (bookingFee != null ? !bookingFee.equals(that.bookingFee) : that.bookingFee != null)
            return false;
        if (raFeeFactor != null ? !raFeeFactor.equals(that.raFeeFactor) : that.raFeeFactor != null)
            return false;
        if (ratePerMile != null ? !ratePerMile.equals(that.ratePerMile) : that.ratePerMile != null)
            return false;
        if (ratePerMinute != null ? !ratePerMinute.equals(that.ratePerMinute) : that.ratePerMinute != null)
            return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null)
            return false;
        if (iconUrl != null ? !iconUrl.equals(that.iconUrl) : that.iconUrl != null) return false;
        if (mapIconUrl != null ? !mapIconUrl.equals(that.mapIconUrl) : that.mapIconUrl != null)
            return false;
        if (maxPersons != null ? !maxPersons.equals(that.maxPersons) : that.maxPersons != null)
            return false;
        if (order != null ? !order.equals(that.order) : that.order != null) return false;
        return cancellationFee != null ? cancellationFee.equals(that.cancellationFee) : that.cancellationFee == null;

    }

    @Override
    public int hashCode() {
        int result = configuration != null ? configuration.hashCode() : 0;
        result = 31 * result + (tncFeeRate != null ? tncFeeRate.hashCode() : 0);
        result = 31 * result + (processingFee != null ? processingFee.hashCode() : 0);
        result = 31 * result + (carCategory != null ? carCategory.hashCode() : 0);
        result = 31 * result + (minimumFare != null ? minimumFare.hashCode() : 0);
        result = 31 * result + (baseFare != null ? baseFare.hashCode() : 0);
        result = 31 * result + (bookingFee != null ? bookingFee.hashCode() : 0);
        result = 31 * result + (raFeeFactor != null ? raFeeFactor.hashCode() : 0);
        result = 31 * result + (ratePerMile != null ? ratePerMile.hashCode() : 0);
        result = 31 * result + (ratePerMinute != null ? ratePerMinute.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (iconUrl != null ? iconUrl.hashCode() : 0);
        result = 31 * result + (plainIconUrl != null ? plainIconUrl.hashCode() : 0);
        result = 31 * result + (mapIconUrl != null ? mapIconUrl.hashCode() : 0);
        result = 31 * result + (maxPersons != null ? maxPersons.hashCode() : 0);
        result = 31 * result + (order != null ? order.hashCode() : 0);
        result = 31 * result + (cancellationFee != null ? cancellationFee.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RequestedCarType{" +
                "configuration='" + configuration + '\'' +
                ", tncFeeRate=" + tncFeeRate +
                ", processingFee='" + processingFee + '\'' +
                ", carCategory='" + carCategory + '\'' +
                ", minimumFare='" + minimumFare + '\'' +
                ", baseFare='" + baseFare + '\'' +
                ", bookingFee='" + bookingFee + '\'' +
                ", raFeeFactor=" + raFeeFactor +
                ", ratePerMile='" + ratePerMile + '\'' +
                ", ratePerMinute='" + ratePerMinute + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", plainIconUrl='" + plainIconUrl + '\'' +
                ", mapIconUrl='" + mapIconUrl + '\'' +
                ", maxPersons=" + maxPersons +
                ", order=" + order +
                ", cancellationFee='" + cancellationFee + '\'' +
                '}';
    }

}
