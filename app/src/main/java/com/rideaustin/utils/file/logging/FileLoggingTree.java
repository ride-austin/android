package com.rideaustin.utils.file.logging;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.rideaustin.utils.DateHelper;

import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import timber.log.Timber;

/**
 * Created by vokol on 09.09.2016.
 */
public class FileLoggingTree extends Timber.DebugTree {

    private final String tag;
    private File logFile;

    public FileLoggingTree(Context context, String tag) {
        this.tag = tag;
        try {
            createLoggingFile(context);
        } catch (IOException e) {
            Log.e("Timber", "Unable to create log file", e);
        }
    }

    @Override
    public void e(String message, Object... args) {
        try {
            log(tag, String.format(message, args));
        } catch (Exception e) {
            Log.e("Timber", "Unable to log", e);
        }
    }

    @Override
    public void e(Throwable t, String message, Object... args) {
        try {
            log(tag, String.format(message, args));
        } catch (Exception e) {
            Log.e("Timber", "Unable to log", e);
        }
    }

    @Override
    public void v(String message, Object... args) {
        ignore();
    }

    @Override
    public void v(Throwable t, String message, Object... args) {
        ignore();
    }

    @Override
    public void d(String message, Object... args) {
        ignore();
    }

    @Override
    public void d(Throwable t, String message, Object... args) {
        ignore();
    }

    @Override
    public void i(String message, Object... args) {
        ignore();
    }

    @Override
    public void i(Throwable t, String message, Object... args) {
        ignore();
    }

    @Override
    public void w(String message, Object... args) {
        ignore();
    }

    @Override
    public void w(Throwable t, String message, Object... args) {
        ignore();
    }

    private void ignore() {
        // Here we are ignoring some methods.
        // When we are not overriding these methods, default behaviour is to print them to logcat.

        // And we don't want some debug info printed to logcat on prod.
        // Besides not-overriding causes duplicate prints on dev environment, just because we have already
        // planted a Timber.DebugTree() in App.java
    }

    private void createLoggingFile(Context context) throws IOException {
        File logDirectory = new File(context.getFilesDir() + "/RALogs");
        if (!logDirectory.exists()) {
            logDirectory.mkdirs();
        }

        logFile = new File(logDirectory, DateHelper.dateToServerDateTimeFormat(new Date()));
        logFile.createNewFile();
    }

    protected void log(@Nullable String tag, @Nullable String message) {
        BufferedWriter buf = null;
        FileWriter fw = null;
        try {
            fw = new FileWriter(logFile, true);
            buf = new BufferedWriter(fw);
            buf.append(message);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            Log.e("Timber", "Unable to log", e);
        } finally {
            IOUtils.closeQuietly(buf);
            IOUtils.closeQuietly(fw);
        }
    }
}
