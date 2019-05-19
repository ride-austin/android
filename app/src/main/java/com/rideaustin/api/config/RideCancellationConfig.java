package com.rideaustin.api.config;

import java.io.Serializable;

/**
 * Created on 30/03/2018
 *
 * @author sdelaysam
 */

public class RideCancellationConfig implements Serializable {

    private boolean enabled;

    private long cancellationThreshold;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getCancellationThreshold() {
        return cancellationThreshold;
    }

    public void setCancellationThreshold(long cancellationThreshold) {
        this.cancellationThreshold = cancellationThreshold;
    }

    @Override
    public String toString() {
        return "RideCancellationConfig{" +
                "enabled=" + enabled +
                ", cancellationThreshold=" + cancellationThreshold +
                '}';
    }
}
