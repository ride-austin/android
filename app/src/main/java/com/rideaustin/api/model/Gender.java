package com.rideaustin.api.model;

/**
 * Created by Sergey Petrov on 12/09/2017.
 */

public enum Gender {
    MALE, FEMALE, OTHER, UNKNOWN;

    public static Gender fromString(String str) {
        switch (str) {
            case "MALE" : return MALE;
            case "FEMALE" : return FEMALE;
            case "OTHER" : return OTHER;
            default: return UNKNOWN;
        }
    }

    @Override
    public String toString() {
        return name();
    }
}
