package com.rideaustin.api.model.driver;

import com.google.gson.annotations.SerializedName;
import com.rideaustin.utils.DateHelper;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Sergey Petrov on 24/05/2017.
 */

public class CurrentTerms implements Serializable {

    @SerializedName("currentTermsId")
    private long id;

    @SerializedName("currentTermsUrl")
    private String url;

    @SerializedName("currentTermsIsMandatory")
    private boolean isMandatory;

    @SerializedName("currentTermsPublicationDate")
    private long date;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public void setMandatory(boolean mandatory) {
        isMandatory = mandatory;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean isEmpty() {
        return url == null || url.isEmpty();
    }

    public String getHumanReadableDate() {
        if (date > 0L) {
            Date local = DateHelper.getLocalDateWithoutShift(date);
            return DateHelper.dateToUiShortDateFormat(local);
        }
        return "";
    }
}
