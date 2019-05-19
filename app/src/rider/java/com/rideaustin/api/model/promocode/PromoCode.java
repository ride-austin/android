package com.rideaustin.api.model.promocode;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import java8.util.Optional;

public class PromoCode implements Serializable {

    @SerializedName("codeLiteral")
    @Expose
    private String codeLiteral;
    @SerializedName("codeValue")
    @Expose
    private double codeValue;
    @SerializedName("active")
    @Expose
    private boolean active;
    @SerializedName("createdDate")
    @Expose
    private long createdDate;
    @SerializedName("expiresOn")
    @Expose
    private Long expiresOn;
    @SerializedName("remainingValue")
    @Expose
    private double remainingValue;
    @SerializedName("timesUsed")
    @Expose
    private int timesUsed;
    @SerializedName("maximumUses")
    @Expose
    private int maximumUses;
    private final static long serialVersionUID = 7296103799862217302L;

    public String getCodeLiteral() {
        return codeLiteral;
    }

    public void setCodeLiteral(String codeLiteral) {
        this.codeLiteral = codeLiteral;
    }

    public double getCodeValue() {
        return codeValue;
    }

    public void setCodeValue(double codeValue) {
        this.codeValue = codeValue;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(int createdDate) {
        this.createdDate = createdDate;
    }

    public Optional<Long> getExpiresOn() {
        return Optional.ofNullable(expiresOn);
    }

    public void setExpiresOn(Long expiresOn) {
        this.expiresOn = expiresOn;
    }

    public double getRemainingValue() {
        return remainingValue;
    }

    public void setRemainingValue(double remainingValue) {
        this.remainingValue = remainingValue;
    }

    public int getTimesUsed() {
        return timesUsed;
    }

    public void setTimesUsed(int timesUsed) {
        this.timesUsed = timesUsed;
    }

    public int getMaximumUses() {
        return maximumUses;
    }

    public void setMaximumUses(int maximumUses) {
        this.maximumUses = maximumUses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PromoCode promoCode = (PromoCode) o;

        if (Double.compare(promoCode.codeValue, codeValue) != 0) return false;
        if (active != promoCode.active) return false;
        if (createdDate != promoCode.createdDate) return false;
        if (Double.compare(promoCode.remainingValue, remainingValue) != 0) return false;
        if (timesUsed != promoCode.timesUsed) return false;
        if (maximumUses != promoCode.maximumUses) return false;
        if (codeLiteral != null ? !codeLiteral.equals(promoCode.codeLiteral) : promoCode.codeLiteral != null)
            return false;
        return expiresOn != null ? expiresOn.equals(promoCode.expiresOn) : promoCode.expiresOn == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = codeLiteral != null ? codeLiteral.hashCode() : 0;
        temp = Double.doubleToLongBits(codeValue);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (active ? 1 : 0);
        result = 31 * result + (int) (createdDate ^ (createdDate >>> 32));
        result = 31 * result + (expiresOn != null ? expiresOn.hashCode() : 0);
        temp = Double.doubleToLongBits(remainingValue);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + timesUsed;
        result = 31 * result + maximumUses;
        return result;
    }
}

