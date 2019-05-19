package com.rideaustin.manager.location;


public class RALocationType {
    private final boolean isThisDeviceLocation;
    private final long id;
    public static final RALocationType ME = new RALocationType(true);

    private RALocationType(boolean isThisDeviceLocation) {
        this.isThisDeviceLocation = isThisDeviceLocation;
        this.id = -1;
    }

    public RALocationType(long id) {
        this.isThisDeviceLocation = false;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public boolean isThisDeviceLocation() {
        return isThisDeviceLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RALocationType owner = (RALocationType) o;

        return isThisDeviceLocation == owner.isThisDeviceLocation && id == owner.id;
    }

    @Override
    public int hashCode() {
        int result = (isThisDeviceLocation ? 1 : 0);
        result = 31 * result + (int) (id ^ (id >>> 32));
        return result;
    }
}
