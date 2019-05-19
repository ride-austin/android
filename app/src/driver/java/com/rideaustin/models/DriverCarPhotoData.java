package com.rideaustin.models;

import com.rideaustin.api.DataManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class DriverCarPhotoData {

    private Map<String, RequestBody> multipartTypedOutput = new HashMap<>();


    public DriverCarPhotoData(String carPhotoPath, final String type) {
        File file = new File(carPhotoPath);
        file.deleteOnExit();
        multipartTypedOutput.put(DataManager.toParam("photo", file.getName()), RequestBody.create(MediaType.parse("image/jpeg"), file));
        multipartTypedOutput.put("carPhotoType", RequestBody.create(MediaType.parse("text/plain"), type));
    }

    public Map<String, RequestBody> getPhotoData() {
        return multipartTypedOutput;
    }
}
