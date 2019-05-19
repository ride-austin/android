package com.rideaustin.api.model;

import android.support.annotation.NonNull;

/**
 * Created by crossover on 21/06/2017.
 */

public class RideSpecificUpgradeStatus {
    private long rideId;
    private UpgradeRequestStatus status;

    public static RideSpecificUpgradeStatus create(long rideId, @NonNull UpgradeRequestStatus status) {
        RideSpecificUpgradeStatus upgradeStatus = new RideSpecificUpgradeStatus();
        upgradeStatus.rideId = rideId;
        upgradeStatus.status = status;
        return upgradeStatus;
    }

    private RideSpecificUpgradeStatus() {
    }

    public long getRideId() {
        return rideId;
    }

    public UpgradeRequestStatus getStatus() {
        return status;
    }

    public boolean isDenied() {
        switch (status) {
            case CANCELLED:
            case DECLINED:
            case EXPIRED:
                return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RideSpecificUpgradeStatus that = (RideSpecificUpgradeStatus) o;

        if (rideId != that.rideId) return false;
        return status == that.status;

    }

    @Override
    public int hashCode() {
        int result = (int) (rideId ^ (rideId >>> 32));
        result = 31 * result + status.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "RideSpecificUpgradeStatus{" +
                "rideId=" + rideId +
                ", status=" + status +
                '}';
    }
}
