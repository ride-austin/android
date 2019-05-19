package com.rideaustin.utils.file;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import timber.log.Timber;

/**
 * Created by vokol on 09.09.2016.
 */
public class LogArchiver {

    private static final int BUFFER = 8192;

    public void zip(File[] files, String zipFileName) {
        BufferedInputStream origin = null;
        BufferedOutputStream buffer = null;
        FileOutputStream dest = null;
        ZipOutputStream out = null;
        FileInputStream fi = null;
        try {
            dest = new FileOutputStream(zipFileName);
            buffer = new BufferedOutputStream(dest);
            out = new ZipOutputStream(buffer);
            byte data[] = new byte[BUFFER];

            for (File file : files) {
                Timber.w("Compress", "Adding: " + file);
                fi = new FileInputStream(file);
                origin = new BufferedInputStream(fi, BUFFER);

                ZipEntry entry = new ZipEntry(file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
                fi.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(origin);
            IOUtils.closeQuietly(buffer);
            IOUtils.closeQuietly(dest);
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(fi);
        }
    }
}
