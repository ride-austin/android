
package com.rideaustin.api.model.driver.earnings;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DriverEarningResponse implements Serializable {

    @SerializedName("content")
    private List<DriverEarningResponseContent> content = new ArrayList<DriverEarningResponseContent>();
    @SerializedName("last")
    private Boolean last;
    @SerializedName("totalElements")
    private Integer totalElements;
    @SerializedName("totalPages")
    private Integer totalPages;
    @SerializedName("firstPage")
    private Boolean firstPage;
    @SerializedName("lastPage")
    private Boolean lastPage;
    @SerializedName("numberOfElements")
    private Integer numberOfElements;
    @SerializedName("first")
    private Boolean first;
    @SerializedName("size")
    private Integer size;
    @SerializedName("number")
    private Integer number;

    public List<DriverEarningResponseContent> getContent() {
        return content;
    }

    public Boolean getLast() {
        return last;
    }

    public Integer getTotalElements() {
        return totalElements;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public Boolean getFirstPage() {
        return firstPage;
    }

    public Boolean getLastPage() {
        return lastPage;
    }

    public Integer getNumberOfElements() {
        return numberOfElements;
    }

    public Boolean getFirst() {
        return first;
    }

    public Integer getSize() {
        return size;
    }

    public Integer getNumber() {
        return number;
    }
}
