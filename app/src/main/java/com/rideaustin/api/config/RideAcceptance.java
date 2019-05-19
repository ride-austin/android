
package com.rideaustin.api.config;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class RideAcceptance implements Serializable {

    @SerializedName("acceptancePeriod")
    private Long mAcceptancePeriod;

    @SerializedName("decisionThreshold")
    private Long mDecisionThreshold;

    public Long getAcceptancePeriod() {
        return mAcceptancePeriod;
    }

    public Long getDecisionThreshold() {
        return mDecisionThreshold;
    }
}
