package com.rideaustin.api.model.driver;

import com.google.gson.annotations.SerializedName;
import com.rideaustin.api.model.User;
import com.rideaustin.utils.Constants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import java8.util.Optional;
import java8.util.stream.StreamSupport;

public class Driver implements Serializable {

    @SerializedName("type")
    private String type;

    @SerializedName("id")
    private Long id;

    @SerializedName("user")
    private User user;

    @SerializedName("active")
    private Boolean active;

    @SerializedName("cars")
    private List<Car> cars = new ArrayList<>();

    @SerializedName("rating")
    private double rating;

    @SerializedName("payoneerId")
    private String payoneerId;

    @SerializedName("payoneerStatus")
    private String payoneerStatus;

    @SerializedName("checkrReports")
    private List<CheckrReport> checkrReports = new ArrayList<>();

    @SerializedName("email")
    private String email;

    @SerializedName("firstname")
    private String firstname;

    @SerializedName("lastname")
    private String lastname;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("uuid")
    private String uuid;

    @SerializedName("photoUrl")
    private String photoUrl;

    @SerializedName("grantedDriverTypes")
    private List<String> grantedDriverTypes;

    @SerializedName("cityId")
    private int cityId;

    @SerializedName("agreedToLegalTerms")
    private boolean agreedToLegalTerms;

    @SerializedName("chauffeurPermit")
    private boolean chauffeurPermit;

    @SerializedName("directConnectId")
    private String directConnectId;

    private boolean isDeaf;

    public Boolean getActive() {
        return active;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<Car> getCars() {
        return cars;
    }

    public Optional<Car> getSelectedCar() {
        return StreamSupport.stream(cars)
                .filter(car -> car.isSelected() && !car.isRemoved())
                .findFirst();
    }

    public void setCars(List<Car> cars) {
        this.cars = cars;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getPayoneerId() {
        return payoneerId;
    }

    public void setPayoneerId(String payoneerId) {
        this.payoneerId = payoneerId;
    }

    public String getPayoneerStatus() {
        return payoneerStatus;
    }

    public void setPayoneerStatus(String payoneerStatus) {
        this.payoneerStatus = payoneerStatus;
    }

    public List<CheckrReport> getCheckrReports() {
        return checkrReports;
    }

    public void setCheckrReports(List<CheckrReport> checkrReports) {
        this.checkrReports = checkrReports;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public List<String> getGrantedDriverTypes() {
        return grantedDriverTypes;
    }

    public boolean isDirectConnectDriver() {
        return grantedDriverTypes != null && StreamSupport.stream(grantedDriverTypes)
                .anyMatch(Constants.DIRECT_CONNECT_DRIVER_TYPE::equalsIgnoreCase);
    }

    public boolean isFemaleDriver() {
        return grantedDriverTypes != null && StreamSupport.stream(grantedDriverTypes)
                .anyMatch(Constants.WOMEN_ONLY_DRIVER_TYPE::equalsIgnoreCase);
    }

    public void setGrantedDriverTypes(List<String> grantedDriverTypes) {
        this.grantedDriverTypes = grantedDriverTypes;
    }

    public boolean isAgreedToLegalTerms() {
        return agreedToLegalTerms;
    }

    public void setAgreedToLegalTerms(boolean agreedToLegalTerms) {
        this.agreedToLegalTerms = agreedToLegalTerms;
    }

    public boolean isChauffeurPermit() {
        return chauffeurPermit;
    }

    public void setChauffeurPermit(boolean chauffeurPermit) {
        this.chauffeurPermit = chauffeurPermit;
    }

    public String getDirectConnectId() {
        return directConnectId;
    }

    public void setDirectConnectId(String directConnectId) {
        this.directConnectId = directConnectId;
    }

    public boolean isDeaf() {
        return isDeaf;
    }

    public void setDeaf(boolean deaf) {
        isDeaf = deaf;
    }

    public String getName() {
        if (fullName != null) {
            return fullName;
        }
        return firstname;
    }
}
