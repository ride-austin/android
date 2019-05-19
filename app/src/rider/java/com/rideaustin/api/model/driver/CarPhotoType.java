package com.rideaustin.api.model.driver;

import java.io.Serializable;

/**
 * Created by havran on 19-Oct-16.
 *
 * @author Roman Havran
 */

public class CarPhotoType implements Serializable {

    private String carPhotoType;

    public CarPhotoType(String carPhotoType) {
        this.carPhotoType = carPhotoType;
    }

    public String getCarPhotoType() {
        return carPhotoType;
    }

    public void setCarPhotoType(String carPhotoType) {
        this.carPhotoType = carPhotoType;
    }
}
