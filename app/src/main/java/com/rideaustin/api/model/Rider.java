package com.rideaustin.api.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Rider implements Serializable {

    @SerializedName("type")
    private String type;

    @SerializedName("id")
    private Long id;

    @SerializedName("user")
    private User user;

    @SerializedName("cardExpired")
    private Boolean cardExpired;

    @SerializedName("firstname")
    private String firstname;

    @SerializedName("lastname")
    private String lastname;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("uuid")
    private String uuid;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("email")
    private String email;

    @SerializedName("rating")
    private double rating;

    @SerializedName("charity")
    public Charity charity;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    public Boolean getCardExpired() {
        return cardExpired;
    }

    public void setCardExpired(Boolean cardExpired) {
        this.cardExpired = cardExpired;
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

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public Charity getCharity() {
        return charity;
    }

    public void setCharity(Charity charity) {
        this.charity = charity;
    }

    @Override
    public String toString() {
        return "Rider{" +
                "type='" + type + '\'' +
                ", id=" + id +
                ", user=" + user +
                ", cardExpired=" + cardExpired +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", fullName='" + fullName + '\'' +
                ", uuid='" + uuid + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", rating=" + rating +
                ", charity=" + charity +
                '}';
    }
}
