package com.rideaustin.ui.drawer.riderequest;

import com.rideaustin.api.model.driver.RequestedCarType;

import java.util.ArrayList;
import java.util.List;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

import static com.rideaustin.utils.Constants.CAR_TYPE_DELIMITER;

/**
 * @author sdelaysam.
 */

public class RideRequestSummaryData {

    private List<RequestedCarType> available;
    private List<RequestedCarType> selected;
    private List<RequestedCarType> womenOnlyAvailable;
    private List<RequestedCarType> directConnectAvailable;

    public RideRequestSummaryData(List<RequestedCarType> available,
                                  List<RequestedCarType> selected,
                                  List<RequestedCarType> womenOnly,
                                  List<RequestedCarType> directConnect) {
        this.available = available;
        this.selected = selected;
        this.selected.retainAll(available);
        this.womenOnlyAvailable = new ArrayList<>(selected);
        this.womenOnlyAvailable.retainAll(womenOnly);
        this.directConnectAvailable = new ArrayList<>(selected);
        this.directConnectAvailable.retainAll(directConnect);

    }

    public boolean isAllSelected() {
        return selected.containsAll(available);
    }

    public boolean isAnySelected() {
        return !selected.isEmpty();
    }

    public String getSelectedAsString() {
        return carTypesToString(selected);
    }

    public boolean hasWomenOnlyAvailable() {
        return !womenOnlyAvailable.isEmpty();
    }

    public String getWomenOnlyAvailableAsString() {
        return carTypesToString(womenOnlyAvailable);
    }

    public boolean hasDirectConnectAvailable() {
        return !directConnectAvailable.isEmpty();
    }

    public String getDirectConnectAvailableAsString() {
        return carTypesToString(directConnectAvailable);
    }

    public static String carTypesToString(List<RequestedCarType> carTypes) {
        return StreamSupport.stream(carTypes)
                .map(RequestedCarType::getTitle)
                .collect(Collectors.joining(CAR_TYPE_DELIMITER));
    }
}
