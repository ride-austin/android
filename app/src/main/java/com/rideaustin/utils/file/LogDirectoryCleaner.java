package com.rideaustin.utils.file;

import android.support.annotation.WorkerThread;

import java.io.File;

/**
 * Created by vokol on 09.09.2016.
 */
public class LogDirectoryCleaner {

    private LogDirectoryCleaner(){}

    @WorkerThread
    public static void cleanLogDirectory(File directory){
        File file = new File(directory.getAbsolutePath());
        if(!file.isDirectory()){
            if(file.exists()){
                file.delete();
            }
        } else{
            File[] files = file.listFiles();
            for(File storedFile : files){
                storedFile.delete();
            }
        }
    }
}
