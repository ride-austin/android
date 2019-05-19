package com.rideaustin.api.model;

import java.io.Serializable;

/**
 * Created on 25/04/2018
 *
 * @author sdelaysam
 */
public class AirportQueueParams implements Serializable {

    private String areaQueueName;

    private String message;

    public String getAreaQueueName() {
        return areaQueueName;
    }

    public void setAreaQueueName(String areaQueueName) {
        this.areaQueueName = areaQueueName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
