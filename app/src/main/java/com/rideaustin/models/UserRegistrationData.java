package com.rideaustin.models;

/**
 * Created by kshumelchyk on 7/5/16.
 */
public class UserRegistrationData {
    private String email;
    private String socialId;
    private String firstName;
    private String lastName;
    private String password;
    private String phoneNumber;
    private String timeZone;
    private String imageFilePath;
    private boolean isFacebookAuth;
    private long cityId;

    public String getSocialId() {
        return socialId;
    }

    public void setSocialId(String socialId) {
        this.socialId = socialId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getImageFilePath() {
        return imageFilePath;
    }

    public void setImageFilePath(String imageFilePath) {
        this.imageFilePath = imageFilePath;
    }

    public boolean isFacebookAuth() {
        return isFacebookAuth;
    }

    public void setFacebookAuth(boolean facebookAuth) {
        isFacebookAuth = facebookAuth;
    }

    public long getCityId() {
        return cityId;
    }

    public void setCityId(final long cityId) {
        this.cityId = cityId;
    }
}
