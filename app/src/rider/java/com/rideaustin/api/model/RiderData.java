package com.rideaustin.api.model;

import android.support.annotation.VisibleForTesting;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Sergey Petrov on 28/06/2017.
 */

public class RiderData {

    @SerializedName("rider")
    private Rider rider;

    @SerializedName("ride")
    private Ride ride;

    @SerializedName("cards")
    private List<Payment> payments;

    /**
     * Tmp solution to declare it as array
     * Client is not ready to have several unpaid, but server seem to be.
     * After API will be finally considered, it will be refactored.
     */
    @SerializedName("unpaid")
    private UnpaidBalance[] unpaid;

    public Rider getRider() {
        return rider;
    }

    public Ride getRide() {
        return ride;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public UnpaidBalance getUnpaid() {
        return unpaid != null && unpaid.length > 0 ? unpaid[0] : null;
    }

    @VisibleForTesting
    public void setUnpaid(UnpaidBalance unpaid) {
        this.unpaid = new UnpaidBalance[] { unpaid };
    }
}
