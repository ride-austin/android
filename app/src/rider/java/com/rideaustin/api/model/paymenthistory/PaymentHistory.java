package com.rideaustin.api.model.paymenthistory;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.utils.DateHelper;

import java.util.Date;

/**
 * Created by supreethks on 19/11/16.
 */

public class PaymentHistory {

    @SerializedName("farePaymentId")
    private int farePaymentId;

    @SerializedName("rideId")
    private long rideId;

    @SerializedName("rideStartAddress")
    private String rideStartAddress;

    @SerializedName("rideEndAddress")
    private String rideEndAddress;

    @SerializedName("driverFirstName")
    private String driverFirstName;

    @SerializedName("driverLastName")
    private String driverLastName;

    @SerializedName("driverNickName")
    private String driverNickName;

    @SerializedName("driverPicture")
    private String driverPicture;

    @SerializedName("mainRiderId")
    private int mainRiderId;

    @SerializedName("mainRiderFistName")
    private String mainRiderFistName;

    @SerializedName("mainRiderLastName")
    private String mainRiderLastName;

    @SerializedName("mainRiderPicture")
    private String mainRiderPicture;

    @SerializedName("rideTotalFare")
    private String rideTotalFare;

    @SerializedName("stripeCreditCharge")
    private String stripeCreditCharge;

    @SerializedName("usedCardId")
    private int usedCardId;

    @SerializedName("usedCardBrand")
    private String usedCardBrand;

    @SerializedName("cardNumber")
    private String cardNumber;

    @SerializedName("startedOn")
    private String startedOn;

    @SerializedName("rideStatus")
    private String rideStatus;

    @SerializedName("mainRider")
    private boolean mainRider;

    @SerializedName("mapUrl")
    private String mapUrl;

    @SerializedName("driverRating")
    private Float driverRating;

    @SerializedName("carBrand")
    private String carBrand;

    @SerializedName("carModel")
    private String carModel;

    @SerializedName("cancelledOnUTC")
    private long cancelledOnUTC;

    @SerializedName("completedOnUTC")
    private long completedOnUTC;

    private Double campaignDiscount;

    private String campaignDescription;

    private String campaignProvider;

    private String campaignDescriptionHistory;

    public Double getCampaignDiscount() {
        return campaignDiscount;
    }

    public void setCampaignDiscount(Double campaignDiscount) {
        this.campaignDiscount = campaignDiscount;
    }

    public String getCampaignDescription() {
        return campaignDescription;
    }

    public void setCampaignDescription(String campaignDescription) {
        this.campaignDescription = campaignDescription;
    }

    public String getCampaignProvider() {
        return campaignProvider;
    }

    public void setCampaignProvider(String campaignProvider) {
        this.campaignProvider = campaignProvider;
    }

    public String getCampaignDescriptionHistory() {
        return campaignDescriptionHistory;
    }

    public void setCampaignDescriptionHistory(String campaignDescriptionHistory) {
        this.campaignDescriptionHistory = campaignDescriptionHistory;
    }

    public void setCancelledOnUTC(long cancelledOnUTC) {
        this.cancelledOnUTC = cancelledOnUTC;
    }

    public void setCompletedOnUTC(long completedOnUTC) {
        this.completedOnUTC = completedOnUTC;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getDriverFirstName() {
        return driverFirstName;
    }

    public String getDriverLastName() {
        return driverLastName;
    }

    public String getDriverNickName() {
        return driverNickName;
    }

    public String getDriverPicture() {
        return driverPicture;
    }

    public int getFarePaymentId() {
        return farePaymentId;
    }

    public boolean isMainRider() {
        return mainRider;
    }

    public String getMainRiderFistName() {
        return mainRiderFistName;
    }

    public int getMainRiderId() {
        return mainRiderId;
    }

    public String getMainRiderLastName() {
        return mainRiderLastName;
    }

    public String getMainRiderPicture() {
        return mainRiderPicture;
    }

    public String getRideEndAddress() {
        return rideEndAddress;
    }

    public long getRideId() {
        return rideId;
    }

    public String getRideStartAddress() {
        return rideStartAddress;
    }

    public String getRideTotalFare() {
        return rideTotalFare;
    }

    public String getStartedOn() {
        return startedOn;
    }

    public String getStripeCreditCharge() {
        return stripeCreditCharge;
    }

    public String getUsedCardBrand() {
        return usedCardBrand;
    }

    public int getUsedCardId() {
        return usedCardId;
    }

    public String getMapUrl() {
        return mapUrl;
    }

    public void setMapUrl(String mapUrl) {
        this.mapUrl = mapUrl;
    }

    public Float getDriverRating() {
        return driverRating;
    }

    public RideStatus getRideStatus() {
        return RideStatus.valueOf(rideStatus);
    }

    public String getRideStatusName() {
        switch (getRideStatus()) {
            case ADMIN_CANCELLED:
                return App.getInstance().getString(R.string.ride_status_admin_cancelled);
            case RIDER_CANCELLED:
                return App.getInstance().getString(R.string.ride_status_rider_cancelled);
            case DRIVER_CANCELLED:
                return App.getInstance().getString(R.string.ride_status_driver_cancelled);
            default:
                return ""; // others not used yet
        }
    }

    public boolean isCancelled() {
        switch (getRideStatus()) {
            case ADMIN_CANCELLED:
            case RIDER_CANCELLED:
            case DRIVER_CANCELLED:
                return true;
        }
        return false;
    }

    public String getDateString() {
        switch (getRideStatus()) {
            case ACTIVE:
                return startedOn;
            case COMPLETED:
                return DateHelper.dateToUiDateTimeAtFormat(new Date(completedOnUTC));
            case ADMIN_CANCELLED:
            case RIDER_CANCELLED:
            case DRIVER_CANCELLED:
                return DateHelper.dateToUiDateTimeAtFormat(new Date(cancelledOnUTC));
            default:
                return "";
        }
    }

    public String getCarInfo() {
        boolean hasBrand = !TextUtils.isEmpty(carBrand);
        boolean hasModel = !TextUtils.isEmpty(carModel);
        if (hasBrand && hasModel) {
            return carBrand + " " + carModel;
        } else if (hasBrand) {
            return carBrand;
        } else if (hasModel) {
            return carModel;
        }
        return "";
    }

    public boolean hasCampaignProvider() {
        return campaignProvider != null;
    }

    @Override
    public String toString() {
        return "PaymentHistory{" +
                "cancelledOn='" + cancelledOnUTC + '\'' +
                ", farePaymentId=" + farePaymentId +
                ", rideId=" + rideId +
                ", rideStartAddress='" + rideStartAddress + '\'' +
                ", rideEndAddress='" + rideEndAddress + '\'' +
                ", driverFirstName='" + driverFirstName + '\'' +
                ", driverLastName='" + driverLastName + '\'' +
                ", driverNickName='" + driverNickName + '\'' +
                ", driverPicture='" + driverPicture + '\'' +
                ", mainRiderId=" + mainRiderId +
                ", mainRiderFistName='" + mainRiderFistName + '\'' +
                ", mainRiderLastName='" + mainRiderLastName + '\'' +
                ", mainRiderPicture='" + mainRiderPicture + '\'' +
                ", rideTotalFare='" + rideTotalFare + '\'' +
                ", stripeCreditCharge='" + stripeCreditCharge + '\'' +
                ", usedCardId=" + usedCardId +
                ", usedCardBrand='" + usedCardBrand + '\'' +
                ", cardNumber='" + cardNumber + '\'' +
                ", startedOn='" + startedOn + '\'' +
                ", completedOn='" + completedOnUTC + '\'' +
                ", rideStatus='" + rideStatus + '\'' +
                ", mainRider=" + mainRider + '\'' +
                ", carBrand=" + carBrand + '\'' +
                ", carModel=" + carModel + '\'' +
                ", rideStatus='" + rideStatus + '\'' +
                ", mapUrl='" + mapUrl + '\'' +
                ", driverRating='" + driverRating +
                '}';
    }

    /**
     * Indicates empty item.
     * Used to show loading item in {@link com.rideaustin.ui.drawer.triphistory.TripHistoryModel}
     */
    private static PaymentHistory empty = new PaymentHistory();

    public static PaymentHistory empty() {
        return empty;
    }
}
