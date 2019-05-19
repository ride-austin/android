package com.rideaustin.api.model;

import java.io.Serializable;

/**
 * Created on 21/03/2018
 *
 * @author sdelaysam
 */

public class RideCancellationReason implements Serializable {

    private String code;
    private String description;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "RideCancellationReason{" +
                "code='" + code + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public boolean canComment() {
        return "OTHER".equals(code);
    }
}
