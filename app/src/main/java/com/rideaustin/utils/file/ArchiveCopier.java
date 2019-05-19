package com.rideaustin.utils.file;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by vokol on 09.09.2016.
 */
public final class ArchiveCopier {

    private ArchiveCopier(){}

    public static void copyFile(File src, File dst) throws IOException {
        FileInputStream in = new FileInputStream(src);
        FileOutputStream out = new FileOutputStream(dst);
        FileChannel inChannel = in.getChannel();
        FileChannel outChannel = out.getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            IOUtils.closeQuietly(inChannel);
            IOUtils.closeQuietly(outChannel);
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }
}
