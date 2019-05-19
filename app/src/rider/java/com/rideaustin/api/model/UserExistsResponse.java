package com.rideaustin.api.model;

/**
 * Created by rost on 10/5/16.
 */

public class UserExistsResponse {
    private final boolean isUserValid;
    private String errorDescription;

    public UserExistsResponse(boolean isUserValid, String errorDescription) {
        this.isUserValid = isUserValid;
        this.errorDescription = errorDescription;
    }

    public static UserExistsResponse valid() {
        return new UserExistsResponse(true, null);
    }

    public static UserExistsResponse error(String message) {
        return new UserExistsResponse(false, message);
    }

    public boolean isUserValid() {
        return isUserValid;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    @Override
    public String toString() {
        return "UserExistsResponse{" +
                "isUserValid=" + isUserValid +
                ", errorDescription='" + errorDescription + '\'' +
                '}';
    }
}
