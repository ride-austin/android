
package com.rideaustin.api.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QueryHint implements Serializable {

    public static final long serialVersionUID = -3476512643361747519L;
    @SerializedName("prefixes")
    @Expose
    private List<String> prefixes = new ArrayList<String>();
    @SerializedName("contains")
    @Expose
    private List<String> contains = new ArrayList<String>();
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("reference")
    @Expose
    private String reference;
    @SerializedName("primaryAddress")
    @Expose
    private String primaryAddress;
    @SerializedName("secondaryAddress")
    @Expose
    private String secondaryAddress;

    /**
     * @return The prefixes
     */
    public List<String> getPrefixes() {
        return prefixes;
    }

    /**
     * @param prefixes The prefixes
     */
    public void setPrefixes(List<String> prefixes) {
        this.prefixes = prefixes;
    }

    /**
     * @return The contains
     */
    public List<String> getContains() {
        return contains;
    }

    /**
     * @param contains The contains
     */
    public void setContains(List<String> contains) {
        this.contains = contains;
    }

    /**
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return The reference
     */
    public String getReference() {
        return reference;
    }

    /**
     * @param reference The reference
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * @return The primaryAddress
     */
    public String getPrimaryAddress() {
        return primaryAddress;
    }

    /**
     * @param primaryAddress The primaryAddress
     */
    public void setPrimaryAddress(String primaryAddress) {
        this.primaryAddress = primaryAddress;
    }

    /**
     * @return The secondaryAddress
     */
    public String getSecondaryAddress() {
        return secondaryAddress;
    }

    /**
     * @param secondaryAddress The secondaryAddress
     */
    public void setSecondaryAddress(String secondaryAddress) {
        this.secondaryAddress = secondaryAddress;
    }
}
