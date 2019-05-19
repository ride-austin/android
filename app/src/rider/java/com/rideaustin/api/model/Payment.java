package com.rideaustin.api.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import java8.util.Optional;

/**
 * Created by v.garshyn on 09.07.16.
 *
 * {"id":60,"cardNumber":"4242","cardBrand":"VISA","cardExpired":false,"primary":true,"uuid":"60"}
 */
public class Payment implements Serializable {

    @SerializedName("id")
    private long id;

    @SerializedName("cardNumber")
    private String cardNumber;

    @SerializedName("cardBrand")
    private String cardBrand;

    @SerializedName("cardExpired")
    private boolean expired;

    @SerializedName("primary")
    private boolean primary;

    @SerializedName("uuid")
    private String uuid;

    private Boolean localPrimary;

    public long getId() {
        return id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardBrand() {
        return cardBrand;
    }

    public boolean isExpired() {
        return expired;
    }

    public boolean isPrimary() {
        return primary;
    }

    public String getUuid() {
        return uuid;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isLocalPrimary() {
        return Optional.ofNullable(localPrimary).orElse(primary);
    }

    public void setLocalPrimary(Boolean localPrimary) {
        this.localPrimary = localPrimary;
    }
}
