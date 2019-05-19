package com.rideaustin.utils.file;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;

import com.rideaustin.utils.Constants;
import com.rideaustin.utils.DateHelper;
import com.rideaustin.utils.TimeUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import rx.Observable;
import timber.log.Timber;

/**
 * Created by Viktor Kifer
 * On 23-Dec-2016.
 */

public class LogManager {

    private File logDirectory;
    private LogArchiver archiver;

    public LogManager(Context context) {
        logDirectory = new File(context.getFilesDir() + "/RALogs");
        archiver = new LogArchiver();
    }

    public Observable<File> prepareReport() {
        return Observable.just(logDirectory)
                .filter(File::exists)
                .flatMap(this::filterLogs)
                .flatMap(this::compressLogFiles);
    }

    public Intent prepareSendIntent(Activity activity, @Nullable File file, String[] to) {
        LogSender logSender = new LogSender(activity);
        return logSender.sendEmail(file, to);
    }

    public void clearLogs() {
        File copiedFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "archive.7z");
        Observable.fromCallable(() -> {
            LogDirectoryCleaner.cleanLogDirectory(logDirectory);
            LogDirectoryCleaner.cleanLogDirectory(copiedFile);
            return Observable.empty();
        });
    }

    private Observable<File> compressLogFiles(List<File> filteredLogs) {
        final File zipArchive = new File(logDirectory, "archive.7z");
        if (filteredLogs.size() > 0) {
            File[] filteredFiles = new File[filteredLogs.size()];
            archiver.zip(filteredLogs.toArray(filteredFiles), zipArchive.getAbsolutePath());

            try {
                File copied = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "archive.7z");
                ArchiveCopier.copyFile(zipArchive, copied);

                return Observable.just(copied);
            } catch (IOException e) {
                throw new RuntimeException("Failed to send logs", e);
            }
        }
        return Observable.empty();
    }

    private Observable<List<File>> filterLogs(File logDirectory) {
        final long now = TimeUtils.currentTimeMillis();
        final long interval = Constants.TWO_HOURS_IN_MILLIS;
        final long delta = now - interval;
        return Observable.just(logDirectory)
                .flatMap(dir -> Observable.from(dir.listFiles()))
                .filter(file -> {
                    String fileName = file.getName();
                    try {
                        Date date = DateHelper.dateFromServerDateTimeFormat(fileName);
                        long dateInMillis = date.getTime();
                        return dateInMillis <= now && (dateInMillis >= delta && dateInMillis <= now);
                    } catch (ParseException e) {
                        Timber.e(e, "Time format did not match required one. time: %s", fileName);

                    }
                    return false;
                })
                .toList();
    }

}
