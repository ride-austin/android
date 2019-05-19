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

public class DriverCarData {

    private Map<String, RequestBody> multipartTypedOutput = new HashMap<>();

    public DriverCarData(final DriverRegistrationData driverRegistrationData) {
        File driveJsonFile = new File(driverRegistrationData.getDriverFilePath());
        driveJsonFile.deleteOnExit();

        File carJsonFile = new File(driverRegistrationData.getCarJsonFilePath());
        carJsonFile.deleteOnExit();

        multipartTypedOutput.put(DataManager.toParam("car", carJsonFile.getName()), RequestBody.create(MediaType.parse("application/json"), carJsonFile));
    }

    public Map<String, RequestBody> getCarData() {
        return multipartTypedOutput;
    }
}
