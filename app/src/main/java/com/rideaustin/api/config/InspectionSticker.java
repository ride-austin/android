
package com.rideaustin.api.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class InspectionSticker implements Serializable {

    @SerializedName("enabled")
    @Expose
    private Boolean enabled;
    @SerializedName("sticker_required_year")
    @Expose
    private Integer stickerRequiredYear;
    @SerializedName("header")
    @Expose
    private String header;
    @SerializedName("title1")
    @Expose
    private String title1;
    @SerializedName("text1")
    @Expose
    private String text1;
    public final static long serialVersionUID = -6168448181136731265L;

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
     * @return The stickerRequiredYear
     */
    public Integer getStickerRequiredYear() {
        return stickerRequiredYear;
    }

    /**
     * @param stickerRequiredYear The sticker_required_year
     */
    public void setStickerRequiredYear(Integer stickerRequiredYear) {
        this.stickerRequiredYear = stickerRequiredYear;
    }

    /**
     * @return The header
     */
    public String getHeader() {
        return header;
    }

    /**
     * @param header The header
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * @return The title1
     */
    public String getTitle1() {
        return title1;
    }

    /**
     * @param title1 The title1
     */
    public void setTitle1(String title1) {
        this.title1 = title1;
    }

    /**
     * @return The text1
     */
    public String getText1() {
        return text1;
    }

    /**
     * @param text1 The text1
     */
    public void setText1(String text1) {
        this.text1 = text1;
    }

}
