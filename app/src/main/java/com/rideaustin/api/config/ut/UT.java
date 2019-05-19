
package com.rideaustin.api.config.ut;

import android.support.annotation.VisibleForTesting;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UT implements Serializable {

    @SerializedName("payWithBevoBucks")
    private PayWithBevoBucks payWithBevoBucks;

    public PayWithBevoBucks getPayWithBevoBucks() {
        return payWithBevoBucks;
    }

    @VisibleForTesting
    public void setPayWithBevoBucks(PayWithBevoBucks payWithBevoBucks) {
        this.payWithBevoBucks = payWithBevoBucks;
    }
}
