package com.rideaustin.api.model.driver;

import com.rideaustin.api.model.Avatar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rideclientandroid on 17.08.2016.
 */
public class DriverRegistration {

    private DriverUserRegistration user;
    private String licenseState;
    private String email;
    private String type;
    private String licenseNumber;
    private String ssn;
    private List<Car> cars = new ArrayList<>();
    private List<CarPhotoType> carsPhotoType = new ArrayList<>();
    private int cityId;
    private String insuranceExpiryDate;
    private String licenseExpiryDate;

    public List<CarPhotoType> getCarsPhotoType() {
        return carsPhotoType;
    }

    public void setCarsPhotoType(List<CarPhotoType> carsPhotoType) {
        this.carsPhotoType = carsPhotoType;
    }

    public DriverRegistration(DriverUserRegistration user) {
        this.user = user;
    }

    public void setCars(List<Car> cars) {
        this.cars = cars;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public void setLicenseState(String licenseState) {
        this.licenseState = licenseState;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public void setType(@Avatar.UserRole String type) {
        this.type = type;
    }

    public void setUser(DriverUserRegistration user) {
        this.user = user;
    }

    public List<Car> getCars() {
        return cars;
    }

    public String getEmail() {
        return email;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public String getLicenseState() {
        return licenseState;
    }

    public String getSsn() {
        return ssn;
    }

    public String getType() {
        return type;
    }

    public DriverUserRegistration getUser() {
        return user;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(final int cityId) {
        this.cityId = cityId;
    }

    public String getInsuranceExpiryDate() {
        return insuranceExpiryDate;
    }

    public void setInsuranceExpiryDate(String insuranceExpiryDate) {
        this.insuranceExpiryDate = insuranceExpiryDate;
    }

    public String getLicenseExpiryDate() {
        return licenseExpiryDate;
    }

    public void setLicenseExpiryDate(String licenseExpiryDate) {
        this.licenseExpiryDate = licenseExpiryDate;
    }
}
