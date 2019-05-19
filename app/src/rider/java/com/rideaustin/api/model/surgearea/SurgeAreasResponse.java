
package com.rideaustin.api.model.surgearea;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class SurgeAreasResponse {

    @SerializedName("content")
    public List<SurgeArea> surgeAreas = new ArrayList<>();

    @SerializedName("last")
    public Boolean last;

    @SerializedName("totalElements")
    public Integer totalElements;

    @SerializedName("totalPages")
    public Integer totalPages;

    @SerializedName("firstPage")
    public Boolean firstPage;

    @SerializedName("lastPage")
    public Boolean lastPage;

    @SerializedName("numberOfElements")
    public Integer numberOfElements;

    @SerializedName("first")
    public Boolean first;

    @SerializedName("size")
    public Integer size;

    @SerializedName("number")
    public Integer number;

    @Override
    public String toString() {
        return "SurgeAreasResponse{" +
                "surgeAreas=" + surgeAreas +
                ", last=" + last +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                ", firstPage=" + firstPage +
                ", lastPage=" + lastPage +
                ", numberOfElements=" + numberOfElements +
                ", first=" + first +
                ", size=" + size +
                ", number=" + number +
                '}';
    }
}
