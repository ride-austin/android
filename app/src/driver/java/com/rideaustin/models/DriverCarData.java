package com.rideaustin.models;

import com.rideaustin.api.DataManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class DriverCarData {

    private Map<String, RequestBody> multipartTypedOutput = new HashMap<>();

    public DriverCarData(String carJsonPath) {

        File carJsonFile = new File(carJsonPath);
        carJsonFile.deleteOnExit();
        multipartTypedOutput.put(DataManager.toParam("car", carJsonFile.getName()), RequestBody.create(MediaType.parse("application/json"), carJsonFile));
    }
    public Map<String, RequestBody> getCarData() {
        return multipartTypedOutput;
    }
}
