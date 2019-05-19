package com.rideaustin.entities;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import com.rideaustin.App;
import com.rideaustin.utils.location.CustomLocations;

/**
 * Created by crossover on 27/12/2016.
 */

public class GeoPosition implements Parcelable {

    protected final LatLng location;
    protected String addressLine;
    protected String fullAddress;
    protected String zipCode;
    protected String id;

    // not serialized nor included into parcel
    @Nullable
    protected transient String customName;
    @Nullable
    protected transient LatLngBounds includeBounds;

    public GeoPosition(double lat, double lng, String addressLine, String fullAddress) {
        this.location = new LatLng(lat, lng);
        this.addressLine = addressLine;
        this.fullAddress = fullAddress;
    }

    public GeoPosition(LatLng latLng, String addressLine, String fullAddress) {
        this.location = latLng;
        this.addressLine = addressLine;
        this.fullAddress = fullAddress;
    }

    public GeoPosition(String placeId, String addressLine, String fullAddress, LatLng latLng, String zipCode) {
        this.id = placeId;
        this.addressLine = addressLine;
        this.fullAddress = fullAddress;
        this.location = latLng;
        this.zipCode = zipCode;
    }

    public double getLat() {
        return location.latitude;
    }

    public double getLng() {
        return location.longitude;
    }

    public LatLng getLatLng() {
        return location;
    }

    public String getAddressLine() {
        // TODO: remove this easy quick shortcut, use another method for hint name
        return App.getDataManager().getLocationHintHelper().getHintName(location, addressLine);
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getPlaceId() {
        return id;
    }

    public void setPlaceId(String placeId) {
        this.id = placeId;
    }

    public String getPlaceName() {
        return CustomLocations.getPlaceName(this);
    }

    public boolean hasCustomName() {
        return !TextUtils.isEmpty(customName);
    }

    public void setCustomName(String name) {
        customName = name;
    }

    public String getCustomName() {
        return customName;
    }

    public void setIncludeRadius(double radius) {
        LatLng southwest = SphericalUtil.computeOffset(location, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(location, radius * Math.sqrt(2.0), 45);
        includeBounds = new LatLngBounds(southwest, northeast);
    }

    public boolean includesLocation(LatLng location) {
        if (hasIncludeRadius()) {
            //noinspection ConstantConditions
            return includeBounds.contains(location);
        }
        return this.location.equals(location);
    }

    public boolean hasIncludeRadius() {
        return includeBounds != null;
    }

    @Override
    public String toString() {
        return "GeoPosition{" +
                "location=" + location +
                ", addressLine='" + addressLine + '\'' +
                ", fullAddress='" + fullAddress + '\'' +
                ", zipCode='" + zipCode + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    protected GeoPosition(Parcel in) {
        location = new LatLng(in.readDouble(), in.readDouble());
        addressLine = in.readString();
        fullAddress = in.readString();
        zipCode = in.readString();
        id = in.readString();
        customName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(location.latitude);
        dest.writeDouble(location.longitude);
        dest.writeString(addressLine);
        dest.writeString(fullAddress);
        dest.writeString(zipCode);
        dest.writeString(id);
        dest.writeString(customName);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<GeoPosition> CREATOR = new Parcelable.Creator<GeoPosition>() {
        @Override
        public GeoPosition createFromParcel(Parcel in) {
            return new GeoPosition(in);
        }

        @Override
        public GeoPosition[] newArray(int size) {
            return new GeoPosition[size];
        }
    };

}