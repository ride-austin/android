package com.rideaustin;

import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.List;

import okhttp3.FormBody;

/**
 * Created by hatak on 16.05.2017.
 */

public class FormBasedRequest {

    private List<String> encodedNames;
    private List<String> encodedValues;

    public FormBasedRequest(FormBody formBody) {
        try {
            Field names = FieldUtils.getField(formBody.getClass(), "encodedNames", true);
            encodedNames = (List<String>) FieldUtils.readField(names, formBody, true);

            Field values = FieldUtils.getField(formBody.getClass(), "encodedValues", true);
            encodedValues = (List<String>) FieldUtils.readField(values, formBody, true);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public String getValueForField(String fieldName) {
        int valueIndex = encodedNames.indexOf(fieldName);
        return valueIndex >= 0 ? encodedValues.get(valueIndex) : null;
    }
}
