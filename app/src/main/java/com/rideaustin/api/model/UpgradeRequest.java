
package com.rideaustin.api.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class UpgradeRequest implements Serializable {

    private static final long serialVersionUID = -1893318249276228126L;
    @SerializedName("source")
    private String source;
    @SerializedName("status")
    private String status;
    @SerializedName("surgeFactor")
    private Double surgeFactor;
    @SerializedName("target")
    private String target;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getSurgeFactor() {
        return surgeFactor;
    }

    public void setSurgeFactor(Double surgeFactor) {
        this.surgeFactor = surgeFactor;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "UpgradeRequest{" +
                "source='" + source + '\'' +
                ", status='" + status + '\'' +
                ", surgeFactor=" + surgeFactor +
                ", target='" + target + '\'' +
                '}';
    }
}
