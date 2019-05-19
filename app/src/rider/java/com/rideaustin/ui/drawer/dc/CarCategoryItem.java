package com.rideaustin.ui.drawer.dc;

import com.rideaustin.api.model.driver.RequestedCarType;

/**
 * Created on 01/12/2017
 *
 * @author sdelaysam
 */

public class CarCategoryItem {

    private RequestedCarType carType;
    private boolean selected;
    private float surgeFactor;

    public RequestedCarType getCarType() {
        return carType;
    }

    public void setCarType(RequestedCarType carType) {
        this.carType = carType;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public float getSurgeFactor() {
        return surgeFactor;
    }

    public void setSurgeFactor(float surgeFactor) {
        this.surgeFactor = surgeFactor;
    }
}
