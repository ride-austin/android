package com.rideaustin;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.test.espresso.intent.Intents;
import android.support.test.runner.intent.IntentCallback;
import android.support.test.runner.intent.IntentMonitorRegistry;
import android.support.v4.content.FileProvider;

import com.rideaustin.utils.ImageHelper;
import com.rideaustin.utils.ResourceUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by hatak on 15.05.2017.
 */

public class ImagePickerMock {

    public static void mockImagePickFromGallery(final String imageFileName) throws IOException {

        File outputDir = App.getInstance().getCacheDir();
        File testImageFile = File.createTempFile("test", ".jpg", outputDir);
        if (!testImageFile.exists()) {
            testImageFile.createNewFile();
        }

        FileUtils.copyInputStreamToFile(loadImage(imageFileName), testImageFile);

        Uri imageUri = Uri.parse(ContentResolver.SCHEME_FILE + "://" + testImageFile.getAbsolutePath());

        Intent resultData = new Intent();
        resultData.setData(imageUri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        Matcher<Intent> expectedIntent = hasAction(Intent.ACTION_PICK);
        intending(expectedIntent).respondWith(result);

    }

    public static void mockImagePickFromGalleryCancel() {
        Matcher<Intent> expectedIntent = hasAction(Intent.ACTION_PICK);
        intending(expectedIntent).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null));
    }

    public static void mockImagePickFromCamera(final String imageFileName) throws IOException {
        Matcher<Intent> expectedIntent = hasAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intending(expectedIntent).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
        IntentMonitorRegistry.getInstance().addIntentCallback(new IntentCallback() {
            @Override
            public void onIntentSent(Intent intent) {
                if (MediaStore.ACTION_IMAGE_CAPTURE.equals(intent.getAction())) {
                    OutputStream out = null;
                    try {
                        Uri uri = intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
                        Context context = getInstrumentation().getTargetContext().getApplicationContext();
                        out = context.getContentResolver().openOutputStream(uri);
                        Bitmap bitmap = BitmapFactory.decodeStream(loadImage(imageFileName));
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
                        bitmap.recycle();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        IOUtils.closeQuietly(out);
                        IntentMonitorRegistry.getInstance().removeIntentCallback(this);
                    }
                }
            }
        });
    }

    public static void mockImagePickFromCameraCancel() {
        Matcher<Intent> expectedIntent = hasAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intending(expectedIntent).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null));
    }

    private static InputStream loadImage(final String imageName) {
        return ResourceUtils.getContentAsStream(ImagePickerMock.class, imageName);
    }

}
