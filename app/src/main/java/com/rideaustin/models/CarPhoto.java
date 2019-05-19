package com.rideaustin.models;

/**
 * Created by crossover on 25/01/2017.
 */

public class CarPhoto {

    private String photoUrl;
    private Boolean removed;
    private String carPhotoType;
    private Integer id;

    public String getCarPhotoType() {
        return carPhotoType;
    }

    public void setCarPhotoType(String carPhotoType) {
        this.carPhotoType = carPhotoType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Boolean getRemoved() {
        return removed;
    }

    public void setRemoved(Boolean removed) {
        this.removed = removed;
    }
}
