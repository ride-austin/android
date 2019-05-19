package com.rideaustin.api.model.driver;

import com.rideaustin.api.DataManager;
import com.rideaustin.models.DriverRegistrationData;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by hatak on 07.12.16.
 */

public class DriverData {

    private Map<String, RequestBody> multipartTypedOutput = new HashMap<>();

    public DriverData(final DriverRegistrationData driverRegistrationData) {

        File driveJsonFile = new File(driverRegistrationData.getDriverFilePath());
        driveJsonFile.deleteOnExit();

        File licenseImageFile = new File(driverRegistrationData.getLicenseFilePath());
        licenseImageFile.deleteOnExit();
        File insuranceImageFile = new File(driverRegistrationData.getInsuranceFilePath());
        insuranceImageFile.deleteOnExit();

        multipartTypedOutput.put(DataManager.toParam("driver", driveJsonFile.getName()), RequestBody.create(MediaType.parse("application/json"), driveJsonFile));
        multipartTypedOutput.put(DataManager.toParam("licenseData", licenseImageFile.getName()), RequestBody.create(MediaType.parse("image/png"), licenseImageFile));
        multipartTypedOutput.put(DataManager.toParam("insuranceData", insuranceImageFile.getName()), RequestBody.create(MediaType.parse("image/png"), insuranceImageFile));

    }

    public Map<String, RequestBody> getDriverRegistrationData() {
        return multipartTypedOutput;
    }

}
