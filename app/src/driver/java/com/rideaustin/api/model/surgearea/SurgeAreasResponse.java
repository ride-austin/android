
package com.rideaustin.api.model.surgearea;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class SurgeAreasResponse {

    @SerializedName("content")
    private List<SurgeArea> surgeAreas = new ArrayList<>();

    @SerializedName("last")
    private Boolean last;

    @SerializedName("firstPage")
    private Boolean firstPage;

    @SerializedName("lastPage")
    private Boolean lastPage;

    @SerializedName("totalPages")
    private Integer totalPages;

    @SerializedName("totalElements")
    private Integer totalElements;

    @SerializedName("numberOfElements")
    private Integer numberOfElements;

    @SerializedName("first")
    private Boolean first;

    @SerializedName("size")
    private Integer size;

    @SerializedName("number")
    private Integer number;

    public List<SurgeArea> getSurgeAreas() {
        return surgeAreas;
    }

    public Boolean getLast() {
        return last;
    }

    public Boolean getFirstPage() {
        return firstPage;
    }

    public Boolean getLastPage() {
        return lastPage;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public Integer getTotalElements() {
        return totalElements;
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
