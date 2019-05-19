package com.rideaustin.api.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * @author shumelchyk
 */
public class User implements Serializable {

    private long id;
    private String photoUrl;
    private String email;
    private String facebookId;

    @SerializedName("firstname")
    private String firstName;

    @SerializedName("lastname")
    private String lastName;

    @SerializedName("nickName")
    private String nickName;
    private String phoneNumber;
    private boolean active;
    private boolean enabled;
    private String fullName;
    private String uuid;

    @SerializedName("avatars")
    private List<Avatar> avatars;

    @SerializedName("gender")
    private String gender;

    public long getId() {
        return id;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getEmail() {
        return email;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUuid() {
        return uuid;
    }

    public List<Avatar> getAvatars() {
        return avatars;
    }

    public String getGender() {
        return gender;
    }

    /**
     * Therefore userId is not required riderId (or driverId)
     * Such data should be extracted from Avatar list (maybe it should be renamed to role)
     *
     * @return -1 is unsuccessful search id
     */
    public long getRiderId() {
        if (avatars != null && !avatars.isEmpty()) {
            for (Avatar avatar:avatars) {
                if (avatar.isRider()) {
                    return avatar.getId();
                }
            }
        }
        return -1;
    }

    public long getDriverId() {
        if (avatars != null && !avatars.isEmpty()) {
            for (Avatar avatar:avatars) {
                if (avatar.isDriver()) {
                    return avatar.getId();
                }
            }
        }
        return -1;
    }

    public boolean isDriver() {
        if (avatars != null && !avatars.isEmpty()) {
            for (Avatar avatar:avatars) {
                if (avatar.isDriver()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isDriverActive() {
        if (avatars != null && !avatars.isEmpty()) {
            for (Avatar avatar:avatars) {
                if (avatar.isDriver() && avatar.isActive()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setAvatars(List<Avatar> avatars) {
        this.avatars = avatars;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
