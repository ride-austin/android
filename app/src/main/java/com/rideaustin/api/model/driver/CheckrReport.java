
package com.rideaustin.api.model.driver;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CheckrReport implements Serializable {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("createdDate")
    @Expose
    private Long createdDate;
    @SerializedName("restrictionsCount")
    @Expose
    private Integer restrictionsCount;
    @SerializedName("violationsCount")
    @Expose
    private Integer violationsCount;
    @SerializedName("accidentsCount")
    @Expose
    private Integer accidentsCount;
    @SerializedName("firstName")
    @Expose
    private String firstName;
    @SerializedName("lastName")
    @Expose
    private String lastName;
    @SerializedName("dateOfBirth")
    @Expose
    private String dateOfBirth;
    private final static long serialVersionUID = -7663707470569450435L;

    /**
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return The status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status The status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return The createdDate
     */
    public Long getCreatedDate() {
        return createdDate;
    }

    /**
     * @param createdDate The createdDate
     */
    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * @return The restrictionsCount
     */
    public Integer getRestrictionsCount() {
        return restrictionsCount;
    }

    /**
     * @param restrictionsCount The restrictionsCount
     */
    public void setRestrictionsCount(Integer restrictionsCount) {
        this.restrictionsCount = restrictionsCount;
    }

    /**
     * @return The violationsCount
     */
    public Integer getViolationsCount() {
        return violationsCount;
    }

    /**
     * @param violationsCount The violationsCount
     */
    public void setViolationsCount(Integer violationsCount) {
        this.violationsCount = violationsCount;
    }

    /**
     * @return The accidentsCount
     */
    public Integer getAccidentsCount() {
        return accidentsCount;
    }

    /**
     * @param accidentsCount The accidentsCount
     */
    public void setAccidentsCount(Integer accidentsCount) {
        this.accidentsCount = accidentsCount;
    }

    /**
     * @return The firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName The firstName
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return The lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName The lastName
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return The dateOfBirth
     */
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * @param dateOfBirth The dateOfBirth
     */
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

}
