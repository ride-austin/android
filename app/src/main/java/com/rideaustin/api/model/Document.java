package com.rideaustin.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by crossover on 22/01/2017.
 */

public class Document {

    @SerializedName("id")
    public Long id;

    @SerializedName("documentType")
    public String documentType;

    @SerializedName("documentStatus")
    public String documentStatus;

    @SerializedName("documentUrl")
    public String documentUrl;

    @SerializedName("name")
    public String name;

    @SerializedName("cityId")
    public Long cityId;

    /**
     * in the form of "2017-02-27" {@link com.rideaustin.utils.DateHelper#SERVER_DATE_FORMAT}
     */
    @SerializedName("validityDate")
    public String validityDate;

    @SerializedName("removed")
    public Boolean removed;

    public String getValidityDate() {
        return validityDate;
    }

    public void setValidityDate(String validityDate) {
        this.validityDate = validityDate;
    }

    public Boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentStatus() {
        return documentStatus;
    }

    public void setDocumentStatus(String documentStatus) {
        this.documentStatus = documentStatus;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }
}
