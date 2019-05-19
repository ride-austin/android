package com.rideaustin.utils;

import android.os.Environment;
import android.support.annotation.Nullable;

import com.rideaustin.App;
import com.rideaustin.R;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by vokol on 05.08.2016.
 */
public class FileDirectoryUtil {

    private FileDirectoryUtil() {
    }

    @Nullable
    private static File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (file.exists()) {
            return file;
        } else {
            if (file.mkdirs()) {
                return file;
            }
        }

        return null;
    }

    public static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = String.valueOf(TimeUtils.currentTimeMillis());
        String imageFileName = App.getInstance().getString(R.string.app_name) + "_" + timeStamp;
        File storageDir = getAlbumStorageDir("RAPhotos");
        if (storageDir == null) {
            storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM);
        }

        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    public static File createOrRewriteFile(String path, String fileName, String content) throws IOException {
        File myFoo = new File(path, fileName);
        FileWriter fooWriter = null;
        try {
            fooWriter = new FileWriter(myFoo, false); // true to append
            // false to overwrite.
            fooWriter.write(content);
        } finally {
            IOUtils.closeQuietly(fooWriter);
        }
        return myFoo;
    }

}
