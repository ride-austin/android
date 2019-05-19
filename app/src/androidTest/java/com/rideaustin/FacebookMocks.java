package com.rideaustin;

import com.facebook.GraphRequest;
import com.rideaustin.utils.FacebookHelper;
import com.rideaustin.utils.ResourceUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Sergey Petrov on 11/05/2017.
 */

public class FacebookMocks {

    public static FacebookHelper.MeRequestCreator getMe(String resourceName) {
        return (accessToken, callback) -> {
            GraphRequest.Callback wrapper = response -> {
                if (callback != null) {
                    try {
                        String jsonResponse = ResourceUtils.getContent(FacebookMocks.class, resourceName + ".json");
                        callback.onCompleted(new JSONObject(jsonResponse), response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onCompleted(response.getJSONObject(), response);
                    }
                }
            };
            return new GraphRequest(accessToken, "me", null, null, wrapper);
        };
    }

}
