
package com.rideaustin.api.model.surgearea;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;
import com.google.maps.android.PolyUtil;
import com.rideaustin.utils.location.LocationHelper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class SurgeArea implements Serializable {

    private static final long serialVersionUID = -2380050688178568128L;

    @SerializedName("id")
    private int id;

    @Deprecated
    @SerializedName("surgeFactor")
    private Float surgeFactor;

    //@SerializedName("numberOfAvailableCars")
    //@SerializedName("numberOfRequestedRides")
    //@SerializedName("numberOfAcceptedRides")
    //@SerializedName("numberOfEyeballs")
    //@SerializedName("numberOfCars")

    @Deprecated
    @SerializedName("carCategories")
    private List<String> carCategories;

    @SerializedName("carCategoriesFactors")
    private HashMap<String, Float> carCategoriesFactors;

    @SerializedName("csvGeometry")
    private String csvGeometry;

    @SerializedName("centerPointLat")
    private Float centerPointLat;

    @SerializedName("centerPointLng")
    private Float centerPointLng;

    @SerializedName("topLeftCornerLat")
    private Float topLeftCornerLat;

    @SerializedName("topLeftCornerLng")
    private Float topLeftCornerLng;

    @SerializedName("bottomRightCornerLat")
    private Float bottomRightCornerLat;

    @SerializedName("bottomRightCornerLng")
    private Float bottomRightCornerLng;

    @SerializedName("name")
    private String name;

    @SerializedName("uuid")
    private String uuid;

    @SerializedName("createdDate")
    private Long createdDate;

    @SerializedName("updatedDate")
    private Long updatedDate;

    @SerializedName("recommendedSurgeFactor")
    private Float recommendedSurgeFactor;

    @SerializedName("automated")
    private Boolean automated;

    @SerializedName("mandatory")
    private Boolean mandatory;

    @SerializedName("labelLat")
    private Double labelLat;

    @SerializedName("labelLng")
    private Double labelLng;

    @SerializedName("cityId")
    private Integer cityId;

    @SerializedName("active")
    private Boolean active;

    @Nullable
    private transient List<LatLng> boundaries;

    @Nullable
    public List<LatLng> getBoundaries() {
        if (boundaries == null && !TextUtils.isEmpty(csvGeometry)) {
            boundaries = LocationHelper.listFrom(csvGeometry, false);
        }
        return boundaries;
    }

    public boolean contains(LatLng location) {
        if (getBoundaries() != null) {
            return PolyUtil.containsLocation(location, getBoundaries(), true);
        }
        return false;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Deprecated
    public Float getSurgeFactor() {
        return surgeFactor;
    }

    @Deprecated
    public void setSurgeFactor(Float surgeFactor) {
        this.surgeFactor = surgeFactor;
    }

    @Deprecated
    public List<String> getCarCategories() {
        return carCategories;
    }

    public HashMap<String, Float> getCarCategoriesFactors() {
        return carCategoriesFactors;
    }

    public void setCarCategoriesFactors(HashMap<String, Float> carCategoriesFactors) {
        this.carCategoriesFactors = carCategoriesFactors;
    }

    @Deprecated
    public void setCarCategories(List<String> carCategories) {
        this.carCategories = carCategories;
    }

    public String getCsvGeometry() {
        return csvGeometry;
    }

    public void setCsvGeometry(String csvGeometry) {
        this.csvGeometry = csvGeometry;
    }

    public Float getCenterPointLat() {
        return centerPointLat;
    }

    public void setCenterPointLat(Float centerPointLat) {
        this.centerPointLat = centerPointLat;
    }

    public Float getCenterPointLng() {
        return centerPointLng;
    }

    public void setCenterPointLng(Float centerPointLng) {
        this.centerPointLng = centerPointLng;
    }

    public Float getTopLeftCornerLat() {
        return topLeftCornerLat;
    }

    public void setTopLeftCornerLat(Float topLeftCornerLat) {
        this.topLeftCornerLat = topLeftCornerLat;
    }

    public Float getTopLeftCornerLng() {
        return topLeftCornerLng;
    }

    public void setTopLeftCornerLng(Float topLeftCornerLng) {
        this.topLeftCornerLng = topLeftCornerLng;
    }

    public Float getBottomRightCornerLat() {
        return bottomRightCornerLat;
    }

    public void setBottomRightCornerLat(Float bottomRightCornerLat) {
        this.bottomRightCornerLat = bottomRightCornerLat;
    }

    public Float getBottomRightCornerLng() {
        return bottomRightCornerLng;
    }

    public void setBottomRightCornerLng(Float bottomRightCornerLng) {
        this.bottomRightCornerLng = bottomRightCornerLng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }

    public Long getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Long updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Float getRecommendedSurgeFactor() {
        return recommendedSurgeFactor;
    }

    public void setRecommendedSurgeFactor(Float recommendedSurgeFactor) {
        this.recommendedSurgeFactor = recommendedSurgeFactor;
    }

    public Boolean getAutomated() {
        return automated;
    }

    public void setAutomated(Boolean automated) {
        this.automated = automated;
    }

    public Boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }

    public Double getLabelLat() {
        return labelLat;
    }

    public void setLabelLat(Double labelLat) {
        this.labelLat = labelLat;
    }

    public Double getLabelLng() {
        return labelLng;
    }

    public void setLabelLng(Double labelLng) {
        this.labelLng = labelLng;
    }

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SurgeArea surgeArea = (SurgeArea) o;

        return id == surgeArea.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "SurgeArea{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surgeFactors=" + carCategoriesFactors +
                ", cityId=" + cityId +
                ", active=" + active +
                '}';
    }
}