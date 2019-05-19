package com.rideaustin.ui.drawer.riderequest;

import com.rideaustin.api.model.driver.RequestedCarType;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author sdelaysam.
 */
class RideRequestViewData {

    List<RequestedCarType> carTypes;
    List<RequestedCarType> available;
    List<RequestedCarType> selected;

    RideRequestViewData(List<RequestedCarType> carTypes,
                        List<RequestedCarType> available,
                        List<RequestedCarType> selected) {
        this.carTypes = carTypes;
        this.available = available;
        this.selected = selected;
    }

    int numSelected() {
        return selected.size();
    }

    Set<String> getSelectedMinus(RequestedCarType carType) {
        Set<String> set = new LinkedHashSet<>();
        for (RequestedCarType type : selected) {
            if (!type.getCarCategory().equals(carType.getCarCategory())) {
                set.add(type.getCarCategory());
            }
        }
        return set;
    }

    Set<String> getSelectedPlus(RequestedCarType carType) {
        Set<String> set = new LinkedHashSet<>();
        set.add(carType.getCarCategory());
        for (RequestedCarType type : selected) {
            set.add(type.getCarCategory());
        }
        return set;
    }

}
