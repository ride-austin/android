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

public class DriverCarPhotoData {

    private Map<String, RequestBody> multipartTypedOutput = new HashMap<>();


    public DriverCarPhotoData(final DriverRegistrationData driverRegistrationData, final String type) {
        File carPhotoFile = new File(driverRegistrationData.getCarPhotoFilePathMap().get(type));
        carPhotoFile.deleteOnExit();
        multipartTypedOutput.put(DataManager.toParam("photo", carPhotoFile.getName()), RequestBody.create(MediaType.parse("image/png"), carPhotoFile));
        multipartTypedOutput.put("carPhotoType", RequestBody.create(MediaType.parse("text/plain"), type));
    }

    public Map<String, RequestBody> getPhotoData() {
        return multipartTypedOutput;
    }
}
