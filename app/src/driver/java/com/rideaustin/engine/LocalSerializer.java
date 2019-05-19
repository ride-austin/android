package com.rideaustin.engine;

import android.content.Context;
import android.support.annotation.VisibleForTesting;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import rx.Observable;
import timber.log.Timber;

/**
 * Created by rost on 8/18/16.
 */
public class LocalSerializer {
    private static final String DIR_NAME = "serializedData";
    private static final String JSON_EXTENTION = ".json";
    private final Gson gson;
    private final File localCache;

    public LocalSerializer(Context context) {
        gson = new Gson();
        localCache = new File(context.getCacheDir(), DIR_NAME);
        if (!localCache.exists() && !localCache.mkdirs()) {
            Exception e = new Exception("::CacheDirFailed::");
            Timber.e(e, "Unable to create local cache dir: " + localCache);
        }
    }

    public synchronized void save(String key, Object value) {
        File target = new File(localCache, key + JSON_EXTENTION);
        JsonWriter jw = null;
        FileWriter fw = null;
        try {
            fw = new FileWriter(target);
            jw = new JsonWriter(fw);
            gson.toJson(value, value.getClass(), jw);
        } catch (IOException e) {
            Timber.e(e, "can't save data!");
        } finally {
            IOUtils.closeQuietly(jw);
            IOUtils.closeQuietly(fw);
        }
    }

    public synchronized <T> T read(String key, Class<? extends T> clazz) {
        File target = new File(localCache, key + JSON_EXTENTION);
        if (!target.exists()) {
            return null;
        }
        JsonReader jr = null;
        FileReader fr = null;
        try {
            fr = new FileReader(target);
            jr = new JsonReader(fr);
            return gson.fromJson(jr, clazz);
        } catch (JsonSyntaxException | IOException e) {
            Timber.e(e, "can't read data!");
        } finally {
            IOUtils.closeQuietly(jr);
            IOUtils.closeQuietly(fr);
        }
        return null;
    }

    public <T> Observable<T> readRx(final String key, final Class<? extends T> clazz) {
        return Observable.defer(() -> {
            T item = read(key, clazz);
            if (item != null) {
                return Observable.just(read(key, clazz));
            } else {
                return Observable.empty();
            }
        });
    }

    public synchronized boolean remove(String key) {
        File target = new File(localCache, key + JSON_EXTENTION);
        return target.delete();
    }

    @VisibleForTesting
    public void removeAll() {
        try {
            FileUtils.deleteDirectory(localCache);
        } catch (IOException e) {
            Timber.e(e);
        }
    }
}
