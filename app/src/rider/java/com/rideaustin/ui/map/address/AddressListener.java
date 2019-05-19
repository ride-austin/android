package com.rideaustin.ui.map.address;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.rideaustin.entities.GeoPosition;

import java8.util.Optional;

/**
 * @author sdelaysam.
 */
interface AddressListener {
    void onAddressSelected(AddressType addressType, Optional<GeoPosition> position);
    void onAddressSelected(AutocompletePrediction prediction);
}
