package com.rideaustin.api.model.driver.earnings;


import java.io.Serializable;
import java.util.List;

/**
 * Created by vokol on 15.08.2016.
 */
public class DriverResponse implements Serializable {
    private final List<DriverEarningResponseContent> driverEarnings;
    private final DriverOnlineResponse driverOnlineResponse;

    public DriverResponse(List<DriverEarningResponseContent> driverEarnings, DriverOnlineResponse driverOnlineResponse) {
        this.driverEarnings = driverEarnings;
        this.driverOnlineResponse = driverOnlineResponse;
    }

    public List<DriverEarningResponseContent> getDriverEarnings() {
        return driverEarnings;
    }

    public DriverOnlineResponse getDriverOnlineResponse() {
        return driverOnlineResponse;
    }
}
