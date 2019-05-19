package com.rideaustin.models;


import android.text.TextUtils;

import com.rideaustin.api.model.driver.DriverRegistration;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rost on 8/9/16.
 */
public class DriverRegistrationData {

    private DriverRegistration driver;
    private String driverFilePath;
    private String carJsonFilePath;
    private String licenseImagePath;
    private Date licenseExpirationDate;
    private Date tncStickerExpirationDate;
    private String insuranceImagePath;
    private Date insuranceExpirationDate;
    private Map<String, String> carPhotoFilePathMap = new HashMap<>();
    private String driverTncCardImagePath;
    private Date driverTncCardExpirationDate;
    private String driverTncStickerImagePath;
    private String driverTncCardFrontImagePath;
    private String driverTncCardBackImagePath;
    private String driverPhotoImagePath;
    private long acceptedTermId;

    public DriverRegistrationData(DriverRegistration driver) {
        this.driver = driver;
    }

    public String getDriverFilePath() {
        return driverFilePath;
    }

    public void setDriverFilePath(String filePath) {
        this.driverFilePath = filePath;
    }

    public void setCarJsonFilePath(String filePath) {
        this.carJsonFilePath = filePath;
    }

    public String getCarJsonFilePath() {
        return carJsonFilePath;
    }

    public Map<String, String> getCarPhotoFilePathMap() {
        return carPhotoFilePathMap;
    }

    public void addCarPhotoFile(String key, String carImageFilePath) {
        carPhotoFilePathMap.put(key, carImageFilePath);
    }

    public String getCarPhotoFile(String key) {
        return carPhotoFilePathMap.get(key);
    }

    public void setLicenseFilePath(String filePath) {
        this.licenseImagePath = filePath;
    }

    public String getLicenseFilePath() {
        return licenseImagePath;
    }

    public void setInsuranceFilePath(String filePath) {
        this.insuranceImagePath = filePath;
    }

    public String getInsuranceFilePath() {
        return insuranceImagePath;
    }

    public DriverRegistration getDriverRegistration() {
        return driver;
    }

    public void setDriverTncCardImagePath(final String driverTncCardImagePath) {
        this.driverTncCardImagePath = driverTncCardImagePath;
    }

    public Date getLicenseExpirationDate() {
        return licenseExpirationDate;
    }

    public void setLicenseExpirationDate(Date licenseExpirationDate) {
        this.licenseExpirationDate = licenseExpirationDate;
    }

    public Date getInsuranceExpirationDate() {
        return insuranceExpirationDate;
    }

    public void setInsuranceExpirationDate(Date insuranceExpirationDate) {
        this.insuranceExpirationDate = insuranceExpirationDate;
    }

    public String getDriverTncCardImagePath() {
        return driverTncCardImagePath;
    }

    public void setDriverTncStickerImagePath(final String driverTncStickerImagePath) {
        this.driverTncStickerImagePath = driverTncStickerImagePath;
    }

    public Date getDriverTncCardExpirationDate() {
        return driverTncCardExpirationDate;
    }

    public void setDriverTncCardExpirationDate(Date driverTncCardExpirationDate) {
        this.driverTncCardExpirationDate = driverTncCardExpirationDate;
    }

    public String getDriverTncStickerImagePath() {
        return driverTncStickerImagePath;
    }

    public void setDriverTncCardFrontImagePath(final String driverTncCardFrontImagePath) {
        this.driverTncCardFrontImagePath = driverTncCardFrontImagePath;
    }

    public String getDriverTncCardFrontImagePath() {
        return driverTncCardFrontImagePath;
    }


    public void setDriverTncCardBackImagePath(final String driverTncCardBackImagePath) {
        this.driverTncCardBackImagePath = driverTncCardBackImagePath;
    }

    public String getDriverTncCardBackImagePath() {
        return driverTncCardBackImagePath;
    }

    public boolean driverTncCardHasTwoSides() {
        return driverTncCardHasFrontSide() && driverTncCardHasBackSide();
    }

    public boolean driverTncCardHasFrontSide() {
        return !TextUtils.isEmpty(getDriverTncCardFrontImagePath());
    }

    public boolean driverTncCardHasBackSide() {
        return !TextUtils.isEmpty(getDriverTncCardBackImagePath());
    }

    public String getDriverPhotoImagePath() {
        return driverPhotoImagePath;
    }

    public void setDriverPhotoImagePath(String driverPhotoImagePath) {
        this.driverPhotoImagePath = driverPhotoImagePath;
    }

    public long getAcceptedTermId() {
        return acceptedTermId;
    }

    public void setAcceptedTermId(long acceptedTermId) {
        this.acceptedTermId = acceptedTermId;
    }

    public Date getTncStickerExpirationDate() {
        return tncStickerExpirationDate;
    }

    public void setTncStickerExpirationDate(Date expirationDate) {
        this.tncStickerExpirationDate = expirationDate;
    }

    public void removeFiles() {
        List<String> files = new ArrayList<>(Arrays.asList(driverFilePath,
                carJsonFilePath,
                licenseImagePath,
                insuranceImagePath,
                driverTncCardImagePath,
                driverTncStickerImagePath,
                driverTncCardFrontImagePath,
                driverTncCardBackImagePath,
                driverPhotoImagePath));
        files.addAll(carPhotoFilePathMap.values());

        for (String file : files) {
            if (file != null) {
                FileUtils.deleteQuietly(new File(file));
            }
        }
    }
}
