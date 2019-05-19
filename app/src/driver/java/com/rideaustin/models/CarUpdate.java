package com.rideaustin.models;

import com.google.gson.annotations.SerializedName;
import com.rideaustin.api.model.driver.Car;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

public class CarUpdate implements Serializable {

    @SerializedName("id")
    private Long id;
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
    @SerializedName("selected")
    private boolean selected;
    @SerializedName("insurancePictureUrl")
    private String insurancePictureUrl;
    @SerializedName("inspectionStatus")
    private String inspectionStatus;
    @SerializedName("removed")
    private boolean removed;
    @SerializedName("photoUrl")
    private String photoUrl;
    @SerializedName("insuranceExpiryDate")
    private String insuranceExpiryDate;
    @SerializedName("inspectionNotes")
    private String inspectionNotes;

    public String getInspectionNotes() {
        return inspectionNotes;
    }

    public void setInspectionNotes(String inspectionNotes) {
        this.inspectionNotes = inspectionNotes;
    }

    public String getInsuranceExpiryDate() {
        return insuranceExpiryDate;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setInsurancePictureUrl(String insurancePictureUrl) {
        this.insurancePictureUrl = insurancePictureUrl;
    }

    public void setInspectionStatus(String inspectionStatus) {
        this.inspectionStatus = inspectionStatus;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public void setInsuranceExpiryDate(String insuranceExpiryDate) {
        this.insuranceExpiryDate = insuranceExpiryDate;
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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public boolean isSelected() {
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

    public boolean isRemoved() {
        return removed;
    }

    @Override
    public String toString() {
        return "CarUpdate{" +
                "id=" + id +
                ", color='" + color + '\'' +
                ", license='" + license + '\'' +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", year='" + year + '\'' +
                ", carCategories=" + carCategories +
                ", selected=" + selected +
                ", insurancePictureUrl='" + insurancePictureUrl + '\'' +
                ", inspectionStatus='" + inspectionStatus + '\'' +
                ", removed=" + removed +
                ", photoUrl='" + photoUrl + '\'' +
                ", insuranceExpiryDate='" + insuranceExpiryDate + '\'' +
                ", inspectionNotes='" + inspectionNotes + '\'' +
                '}';
    }

    public static CarUpdate fromCar(final Car car) {
        CarUpdate carUpdate = new CarUpdate();
        carUpdate.setInsuranceExpiryDate(car.getInsuranceExpiryDate());
        carUpdate.setCarCategories(car.getCarCategories());
        carUpdate.setColor(car.getColor());
        carUpdate.setId(car.getId());
        carUpdate.setLicense(car.getLicense());
        carUpdate.setMake(car.getMake());
        carUpdate.setModel(car.getModel());
        carUpdate.setSelected(car.isSelected());
        carUpdate.setYear(car.getYear());
        carUpdate.setInspectionStatus(car.getInspectionStatus());
        carUpdate.setInsurancePictureUrl(car.getInsurancePictureUrl());
        carUpdate.setPhotoUrl(car.getPhotoUrl());
        carUpdate.setRemoved(car.isRemoved());
        carUpdate.setInspectionNotes(car.getInspectionNotes());
        return carUpdate;
    }
}
