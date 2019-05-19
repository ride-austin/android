package com.rideaustin.api.model.driver;

import com.google.gson.annotations.SerializedName;
import com.rideaustin.api.config.Alert;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RequestedDriverType implements Serializable {

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("availableInCategories")
    private Set<String> availableInCategories = new LinkedHashSet<>();

    @SerializedName("displayTitle")
    private String displayTitle;

    @SerializedName("displaySubtitle")
    private String displaySubtitle;

    @SerializedName("eligibleGenders")
    private List<String> eligibleGenders;

    @SerializedName("eligibleCategories")
    private List<String> eligibleCategories;

    @SerializedName("unknownGenderAlert")
    private Alert unknownGenderAlert;

    @SerializedName("ineligibleGenderAlert")
    private Alert ineligibleGenderAlert;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getAvailableInCategories() {
        return availableInCategories;
    }

    public void setAvailableInCategories(Set<String> availableInCategories) {
        this.availableInCategories = availableInCategories;
    }

    public String getDisplayTitle() {
        return displayTitle;
    }

    public void setDisplayTitle(String displayTitle) {
        this.displayTitle = displayTitle;
    }

    public String getDisplaySubtitle() {
        return displaySubtitle;
    }

    public void setDisplaySubtitle(String displaySubtitle) {
        this.displaySubtitle = displaySubtitle;
    }

    public List<String> getEligibleGenders() {
        return eligibleGenders;
    }

    public void setEligibleGenders(List<String> eligibleGenders) {
        this.eligibleGenders = eligibleGenders;
    }

    public List<String> getEligibleCategories() {
        return eligibleCategories;
    }

    public void setEligibleCategories(List<String> eligibleCategories) {
        this.eligibleCategories = eligibleCategories;
    }

    public Alert getUnknownGenderAlert() {
        return unknownGenderAlert;
    }

    public void setUnknownGenderAlert(Alert unknownGenderAlert) {
        this.unknownGenderAlert = unknownGenderAlert;
    }

    public Alert getIneligibleGenderAlert() {
        return ineligibleGenderAlert;
    }

    public void setIneligibleGenderAlert(Alert ineligibleGenderAlert) {
        this.ineligibleGenderAlert = ineligibleGenderAlert;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestedDriverType that = (RequestedDriverType) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null)
            return false;
        if (availableInCategories != null ? !availableInCategories.equals(that.availableInCategories) : that.availableInCategories != null)
            return false;
        if (displayTitle != null ? !displayTitle.equals(that.displayTitle) : that.displayTitle != null)
            return false;
        if (displaySubtitle != null ? !displaySubtitle.equals(that.displaySubtitle) : that.displaySubtitle != null)
            return false;
        if (eligibleGenders != null ? !eligibleGenders.equals(that.eligibleGenders) : that.eligibleGenders != null)
            return false;
        return eligibleCategories != null ? eligibleCategories.equals(that.eligibleCategories) : that.eligibleCategories == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (displayTitle != null ? displayTitle.hashCode() : 0);
        result = 31 * result + (displaySubtitle != null ? displaySubtitle.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RequestedDriverType{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", availableInCategories=" + availableInCategories +
                ", displayTitle='" + displayTitle + '\'' +
                ", displaySubtitle='" + displaySubtitle + '\'' +
                ", eligibleGenders=" + eligibleGenders +
                ", eligibleCategories=" + eligibleCategories +
                ", unknownGenderAlert=" + unknownGenderAlert +
                ", ineligibleGenderAlert=" + ineligibleGenderAlert +
                '}';
    }
}