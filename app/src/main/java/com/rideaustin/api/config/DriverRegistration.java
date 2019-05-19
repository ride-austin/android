
package com.rideaustin.api.config;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class DriverRegistration implements Serializable {

    @SerializedName("requirements")
    private List<String> requirements = null;

    @SerializedName("inspection_sticker")
    private InspectionSticker inspectionSticker;

    @SerializedName("tnc_card")
    private TncCard tncCard;

    @SerializedName("description")
    private String description;

    @SerializedName("enabled")
    private Boolean enabled;

    @SerializedName("minCarYear")
    private Integer minCarYear;

    @SerializedName("driverRegistrationTerms")
    private String driverRegistrationTermsUrl;

    @SerializedName("newCarSuccessMessage")
    private String newCarSuccessMessage;

    public final static long serialVersionUID = 4345614264672576967L;

    /**
     * @return The requirements
     */
    public List<String> getRequirements() {
        return requirements;
    }

    /**
     * @param requirements The requirements
     */
    public void setRequirements(List<String> requirements) {
        this.requirements = requirements;
    }

    /**
     * @return The inspectionSticker
     */
    public InspectionSticker getInspectionSticker() {
        return inspectionSticker;
    }

    /**
     * @param inspectionSticker The inspection_sticker
     */
    public void setInspectionSticker(InspectionSticker inspectionSticker) {
        this.inspectionSticker = inspectionSticker;
    }

    /**
     * @return The tncCard
     */
    public TncCard getTncCard() {
        return tncCard;
    }

    /**
     * @param tncCard The tnc_card
     */
    public void setTncCard(TncCard tncCard) {
        this.tncCard = tncCard;
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
     * @return The enabled
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * @param enabled The enabled
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return The minCarYear
     */
    public Integer getMinCarYear() {
        return minCarYear;
    }

    /**
     * @param minCarYear The minCarYear
     */
    public void setMinCarYear(Integer minCarYear) {
        this.minCarYear = minCarYear;
    }

    public String getDriverRegistrationTermsUrl() {
        return driverRegistrationTermsUrl;
    }

    public void setDriverRegistrationTermsUrl(String driverRegistrationTermsUrl) {
        this.driverRegistrationTermsUrl = driverRegistrationTermsUrl;
    }

    public String getNewCarSuccessMessage() {
        return newCarSuccessMessage;
    }
}
