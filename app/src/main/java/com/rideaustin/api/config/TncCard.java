
package com.rideaustin.api.config;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TncCard implements Serializable {

    @SerializedName("enabled")
    private Boolean enabled;

    @SerializedName("backPhoto")
    private Boolean backPhoto;

    @SerializedName("header")
    private String header;

    @SerializedName("title1")
    private String title1;

    @SerializedName("action1")
    private String action1;

    @SerializedName("text1")
    private String text1;

    @SerializedName("title2")
    private String title2;

    @SerializedName("text2")
    private String text2;

    @SerializedName("title1_back")
    private String title1Back;

    @SerializedName("text1_back")
    private String text1Back;


    public final static long serialVersionUID = -5152090729721720952L;

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
     * @return The action1
     */
    public String getAction1() {
        return action1;
    }

    /**
     * @param action1 The action1
     */
    public void setAction1(String action1) {
        this.action1 = action1;
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

    /**
     * @return The title2
     */
    public String getTitle2() {
        return title2;
    }

    /**
     * @param title2 The title2
     */
    public void setTitle2(String title2) {
        this.title2 = title2;
    }

    /**
     * @return The text2
     */
    public String getText2() {
        return text2;
    }

    /**
     * @param text2 The text2
     */
    public void setText2(String text2) {
        this.text2 = text2;
    }

    public String getTitle1Back() {
        return title1Back;
    }

    public void setTitle1Back(String title1Back) {
        this.title1Back = title1Back;
    }

    public String getText1Back() {
        return text1Back;
    }

    public void setText1Back(String text1Back) {
        this.text1Back = text1Back;
    }

    public Boolean getBackPhotoEnabled() {
        return backPhoto;
    }

    public void setBackPhotoEnabled(final Boolean backPhoto) {
        this.backPhoto = backPhoto;
    }
}
