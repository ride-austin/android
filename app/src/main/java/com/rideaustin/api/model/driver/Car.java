package com.rideaustin.api.model.driver;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

public class Car implements Serializable {

    @SerializedName("id")
    private Long id;
    @SerializedName("photoUrl")
    private String photoUrl;
    @SerializedName("color")
    private String color;
    @SerializedName("license")
    private String license;
    @SerializedName("make")
    private String make;
    @SerializedName("model")
    private String model;
    @SerializedName("year")
    private String year;
    @SerializedName("carCategories")
    private Set<String> carCategories = new LinkedHashSet<>();
    @SerializedName("uuid")
    private Long uuid;
    @SerializedName("selected")
    private Boolean selected;
    @SerializedName("insurancePictureUrl")
    private String insurancePictureUrl;
    @SerializedName("inspectionStatus")
    private String inspectionStatus;
    @SerializedName("removed")
    private Boolean removed;
    @SerializedName("insuranceExpiryDate")
    private String insuranceExpiryDate;
    @SerializedName("inspectionNotes")
    private String inspectionNotes;

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public void setInsurancePictureUrl(String insurancePictureUrl) {
        this.insurancePictureUrl = insurancePictureUrl;
    }

    public void setInspectionStatus(String inspectionStatus) {
        this.inspectionStatus = inspectionStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public Set<String> getCarCategories() {
        return carCategories;
    }

    public void setCarCategories(Set<String> carCategories) {
        this.carCategories = carCategories;
    }

    public Long getUuid() {
        return uuid;
    }

    public void setUuid(Long uuid) {
        this.uuid = uuid;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public Boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getInsurancePictureUrl() {
        return insurancePictureUrl;
    }

    public String getInspectionStatus() {
        return inspectionStatus;
    }

    public Boolean isRemoved() {
        return removed;
    }

    public void setRemoved(Boolean removed) {
        this.removed = removed;
    }

    public void setInsuranceExpiryDate(String insuranceExpiryDate) {
        this.insuranceExpiryDate = insuranceExpiryDate;
    }

    public String getInsuranceExpiryDate() {
        return insuranceExpiryDate;
    }

    public String getInspectionNotes() {
        return inspectionNotes;
    }

    public void setInspectionNotes(String inspectionNotes) {
        this.inspectionNotes = inspectionNotes;
    }

    @Override
    public String toString() {
        return "Car{" +
                "id=" + id +
                ", photoUrl='" + photoUrl + '\'' +
                ", color='" + color + '\'' +
                ", license='" + license + '\'' +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", year='" + year + '\'' +
                ", carCategories=" + carCategories +
                ", uuid='" + uuid + '\'' +
                ", selected=" + selected +
                ", insurancePictureUrl='" + insurancePictureUrl + '\'' +
                ", inspectionStatus='" + inspectionStatus + '\'' +
                ", removed=" + removed +
                '}';
    }
}
