package com.rideaustin;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;

/**
 * Created by hatak on 17.05.2017.
 */

public class RequestStats {

    private List<Request> requests;

    public RequestStats(Request request) {
        requests = new ArrayList<>();
        add(request);
    }

    public void add(Request request) {
        requests.add(request);
    }

    public List<Request> getRequests() {
        return requests;
    }

    public int getCount() {
        return requests.size();
    }
}
