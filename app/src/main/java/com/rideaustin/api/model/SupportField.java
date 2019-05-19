package com.rideaustin.api.model;

import com.google.gson.annotations.SerializedName;
import com.rideaustin.ui.drawer.triphistory.forms.SupportFieldViewModel;

/**
 * Created by crossover on 24/05/2017.
 */

public class SupportField {
    @SerializedName("fieldTitle")
    private String fieldTitle;

    @SerializedName("fieldPlaceholder")
    private String fieldPlaceholder;

    @SerializedName("fieldType")
    private String fieldType;

    @SerializedName("variable")
    private String variable;

    @SerializedName("isMandatory")
    private boolean isMandatory;

    public String getFieldTitle() {
        return fieldTitle;
    }

    public void setFieldTitle(String fieldTitle) {
        this.fieldTitle = fieldTitle;
    }

    public String getFieldPlaceholder() {
        return fieldPlaceholder;
    }

    public void setFieldPlaceholder(String fieldPlaceholder) {
        this.fieldPlaceholder = fieldPlaceholder;
    }

    @SupportFieldViewModel.FieldType
    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public void setMandatory(boolean mandatory) {
        isMandatory = mandatory;
    }

    @Override
    public String toString() {
        return "Field{" +
                "fieldTitle='" + fieldTitle + '\'' +
                '}';
    }
}
