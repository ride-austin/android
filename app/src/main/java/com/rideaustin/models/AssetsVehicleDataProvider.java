package com.rideaustin.models;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rideaustin.schedulers.RxSchedulers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by rost on 8/10/16.
 */
public class AssetsVehicleDataProvider implements VehicleManager.VehicleDataProvider {
    private final static String JSON_PATH = "automobileV4.json";
    private final Context context;

    public AssetsVehicleDataProvider(Context context) {
        this.context = context;
    }

    @Override
    public Observable<List<VehicleModel>> loadVehicles() {
        return Observable.create(new Observable.OnSubscribe<List<VehicleModel>>() {
            @Override
            public void call(Subscriber<? super List<VehicleModel>> subscriber) {
                try {
                    final List<VehicleModel> result = parseVehicles();
                    subscriber.onNext(result);
                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(RxSchedulers.network());
    }

    private List<VehicleModel> parseVehicles() throws IOException {
        final Gson gson = new Gson();
        final Type listType = new TypeToken<List<VehicleModel>>() {
        }.getType();
        final InputStreamReader inputStreamReader = new InputStreamReader(context.getAssets().open(JSON_PATH));
        return gson.fromJson(inputStreamReader, listType);
    }
}
