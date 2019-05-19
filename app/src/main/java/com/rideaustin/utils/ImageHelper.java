package com.rideaustin.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.rideaustin.App;
import com.rideaustin.R;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * Created by kshumelchyk on 7/4/16.
 */
public class ImageHelper {

    private static final short MAX_IMAGE_SIZE = 640;

    private static Bitmap resizeImage(Bitmap image, int maxWidth) {
        float aspectRatio = image.getWidth() / (float) image.getHeight();
        int height = Math.round(maxWidth / aspectRatio);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(image, maxWidth, height, false);
        Timber.d("::resizeImage:: Original (%dx%d) Resized: (%d,%d)", image.getWidth(), image.getHeight(), scaledBitmap.getWidth(), scaledBitmap.getHeight());
        return scaledBitmap;
    }

    private static Bitmap rotateImage(String filePath, Bitmap resizedImage) {
        Timber.d("::rotateImage::");
        try {
            ExifInterface exif = new ExifInterface(filePath);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationInDegrees = exifToDegrees(rotation);
            Timber.d("::rotateImage:: rotationInDegrees: %d", rotationInDegrees);
            Matrix matrix = new Matrix();
            if (rotation != 0) {
                matrix.preRotate(rotationInDegrees);
                resizedImage = Bitmap.createBitmap(resizedImage, 0, 0, resizedImage.getWidth(), resizedImage.getHeight(), matrix, true);
            }
        } catch (IOException e) {
            Timber.e(e, "Failed to instantiate ExifInterface");
        }
        return resizedImage;
    }

    public static String getImagePath(ContentResolver contentResolver, Uri selectedImage) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(selectedImage, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                return cursor.getString(columnIndex);
            }
            return selectedImage.getPath();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static int exifToDegrees(int exifOrientation) {
        switch (exifOrientation) {
            case (ExifInterface.ORIENTATION_ROTATE_90):
                return 90;
            case (ExifInterface.ORIENTATION_ROTATE_180):
                return 180;
            case (ExifInterface.ORIENTATION_ROTATE_270):
                return 270;
            default:
                return 0;
        }
    }

    /**
     * Method to get bitmap from VectorDrawable
     *
     * @param drawableID
     * @return
     */
    public static Bitmap createBitmap(Context context, int drawableID) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableID);
        Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }

    public static BitmapImageViewTarget loadRoundImageIntoView(final ImageView imageView, String imageUrl) {
        return loadRoundImageIntoView(imageView, imageUrl, 0);
    }

    public static BitmapImageViewTarget loadRoundImageIntoView(final ImageView imageView, String imageUrl, @DrawableRes int placeholder) {
        return loadRoundImageIntoView(imageView.getContext(), imageView, imageUrl, placeholder);
    }

    public static BitmapImageViewTarget loadRoundImageIntoView(final Context context, final ImageView imageView, String imageUrl) {
        return loadRoundImageIntoView(context, imageView, imageUrl, 0);
    }

    public static BitmapImageViewTarget loadRoundImageIntoView(final Context context, final ImageView imageView, String imageUrl, @DrawableRes int placeholder) {
        return Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .apply(new RequestOptions().centerCrop().placeholder(placeholder))
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }

    public static SimpleTarget<Bitmap> loadImageAsBackgroundIntoView(final View view, String imageUrl, RequestListener<Bitmap> listener) {
        return Glide.with(view.getContext())
                .asBitmap()
                .load(imageUrl)
                .apply(new RequestOptions().centerCrop())
                .listener(listener)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Drawable drawable = new BitmapDrawable(view.getContext().getResources(), resource);
                        view.setBackground(drawable);
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        super.onLoadCleared(placeholder);
                        // RA-12165: should handle this case for custom targets
                        // https://github.com/bumptech/glide/issues/1761#issuecomment-283444560
                        view.setBackground(placeholder);
                    }
                });
    }

    public static SimpleTarget<Bitmap> loadImageIntoMenuItem(final Activity activity, final MenuItem item, final String iconUrl) {

        return Glide.with(activity)
                .asBitmap()
                .load(iconUrl)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Drawable drawable = new BitmapDrawable(App.getInstance().getResources(), resource);
                        drawable.setColorFilter(ContextCompat.getColor(App.getInstance(), R.color.drawer_icon), PorterDuff.Mode.SRC_IN);
                        item.setIcon(drawable);
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        super.onLoadCleared(placeholder);
                        // RA-12165: should handle this case for custom targets
                        // https://github.com/bumptech/glide/issues/1761#issuecomment-283444560
                        item.setIcon(placeholder);
                    }
                });
    }

    private static RoundedBitmapDrawable createBitmapDrawable(Bitmap resource) {
        Resources resources = App.getInstance().getResources();
        return RoundedBitmapDrawableFactory.create(resources, resource);
    }

    public static Target<Bitmap> loadImageIntoView(final ImageView imageView, String imageUrl, @DrawableRes int placeholder) {
        return Glide.with(imageView.getContext())
                .asBitmap()
                .load(imageUrl)
                .apply(new RequestOptions().placeholder(placeholder))
                .into(imageView);
    }

    public static Target<Bitmap> loadImageIntoView(final ImageView imageView, String imageUrl, @DrawableRes int placeholder, @DrawableRes int error) {
        return Glide.with(imageView.getContext())
                .asBitmap()
                .load(imageUrl)
                .apply(new RequestOptions().placeholder(placeholder).error(error))
                .into(imageView);
    }

    public static Target<Bitmap> loadImageIntoViewWithDefaultProgress(final ImageView imageView, String imageUrl, @DrawableRes int defaultImage) {
        return loadImageIntoViewWithDefaultProgress(imageView, imageUrl, defaultImage, null);
    }

    public static Target<Bitmap> loadImageIntoViewWithDefaultProgress(final ImageView imageView, String imageUrl, @DrawableRes int defaultImage, RequestListener<Bitmap> listener) {
        AnimationDrawable progressDrawable = (AnimationDrawable) ContextCompat.getDrawable(App.getInstance(), R.drawable.rotating_circle);
        progressDrawable.start();
        return Glide.with(imageView.getContext())
                .asBitmap()
                .load(imageUrl)
                .apply(new RequestOptions().placeholder(progressDrawable).error(defaultImage))
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        progressDrawable.stop();
                        if (listener != null) {
                            return listener.onLoadFailed(e, model, target, isFirstResource);
                        } else {
                            return false;
                        }
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        progressDrawable.stop();
                        if (listener != null) {
                            return listener.onResourceReady(resource, model, target, dataSource, isFirstResource);
                        } else {
                            return false;
                        }
                    }
                })
                .into(imageView);
    }

    public static Target<Bitmap> loadImageIntoView(final ImageView imageView, String imageUrl, @DrawableRes int placeholder, @DrawableRes int error, RequestListener<Bitmap> listener) {
        return Glide.with(imageView.getContext())
                .asBitmap()
                .load(imageUrl)
                .apply(new RequestOptions().centerCrop().placeholder(placeholder).error(error))
                .listener(listener)
                .into(imageView);
    }

    public static Target<Bitmap> loadImageIntoView(final ImageView imageView, String imageUrl) {
        return Glide.with(imageView.getContext())
                .asBitmap()
                .load(imageUrl)
                .into(imageView);
    }

    public static Target<Bitmap> loadImageIntoTarget(Context context, final SimpleTarget<Bitmap> target, String imageUrl) {
        return Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .into(target);
    }

    public static Target<Bitmap> loadImageIntoTarget(Context context, final SimpleTarget<Bitmap> target, String imageUrl, int width, int height) {
        return Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .apply(new RequestOptions().override(width, height))
                .into(target);
    }


    public static Target<Bitmap> loadCarIconTarget(Context context, final SimpleTarget<Bitmap> target, String iconUrl, Bitmap background, boolean fullSize) {
        int size = (int) ViewUtils.dpToPixels(48);
        return Glide.with(context)
                .asBitmap()
                .load(iconUrl)
                .apply(new RequestOptions().override(size, size).transform(new CarIconizerTransformation(background, fullSize)))
                .into(target);
    }

    public static Target<Bitmap> loadCarIconIntoMarker(Context context, String iconUrl, final Marker marker) {
        int width = context.getResources().getDimensionPixelSize(R.dimen.car_marker_width);
        int height = context.getResources().getDimensionPixelSize(R.dimen.car_marker_height);
        return Glide.with(context)
                .asBitmap()
                .load(iconUrl)
                .apply(new RequestOptions().fitCenter().override(width, height))
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(resource));
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        super.onLoadCleared(placeholder);
                        // Can't set NULL - default google map's marker will be shown in this case.
                        // Its undesirable when marker should be just cleared, like in situation
                        // when rider switches between car categories using slider.
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.transparent_pixel));
                    }
                });
    }

    public static void loadResizedImageIntoView(final ImageView imageView, String imageUrl) {
        Glide.with(imageView.getContext())
                .asBitmap()
                .load(imageUrl)
                .apply(new RequestOptions().override(100, 100))
                .into(imageView);
    }

    public static void loadImageIntoViewWithCompression(ImageView imageView, String imageUrl) {
        Glide.with(imageView.getContext())
                .asBitmap()
                .load(imageUrl)
                .apply(new RequestOptions().dontTransform().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)) // don't populate the cache if not used
                .into(imageView);
    }

    public static MultipartBody.Part getTypedFileFromPath(String filePath) {
        return getTypedFileFromPath("file", filePath);
    }

    public static MultipartBody.Part getTypedFileFromPath(final String dataName, final String filePath) {
        File file = new File(filePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        return MultipartBody.Part.createFormData(dataName, file.getName(), requestFile);
    }

    public static void saveBitmapToFile(Bitmap bitmap, String filePath, Bitmap.CompressFormat compressFormat, int quality) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filePath, false);
            bitmap.compress(compressFormat, quality, out);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    public static String getEncodedImageFromPath(String filePath) {
        BitmapFactory.Options options = getBitmapQualityOptions(filePath);
        Bitmap image = ImageHelper.resizeImage(BitmapFactory.decodeFile(filePath, options), MAX_IMAGE_SIZE);
        ImageHelper.rotateImage(filePath, image);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteFormat = stream.toByteArray();
        return Base64.encodeToString(byteFormat, Base64.DEFAULT);
    }

    @NonNull
    private static BitmapFactory.Options getBitmapQualityOptions(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        // returns null in this case
        BitmapFactory.decodeFile(filePath, options);

        return prepareOptimizedOptions(options);
    }

    /**
     * See: http://stackoverflow.com/a/32121059
     */
    private static BitmapFactory.Options prepareOptimizedOptions(BitmapFactory.Options options) {
        options.inSampleSize = calculateInSampleSize(options, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE);
        options.inJustDecodeBounds = false;

        options.inScaled = true;
        options.inDensity = options.outWidth;
        options.inTargetDensity = MAX_IMAGE_SIZE * options.inSampleSize;

        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inDither = true;
        return options;
    }


    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap drawTextOnMarker(Context context, String text) {
        Bitmap bmp = ImageHelper.createBitmap(context, R.drawable.icn_time_pin);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        int textSize = (int) (ViewUtils.dpToPixels(9) + 0.5f);
        paint.setTextSize(textSize);

        String[] div = text.split(" ");

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bmp);

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2);

        //((paint.descent() + paint.ascent()) / 2) is the distance from the baseline to the center.
        int yPos = (int) ((canvas.getHeight() / 5) - (paint.descent() + paint.ascent()));

        int padding = 5;

        canvas.drawText(div[0], xPos, yPos, paint);
        canvas.drawText(div[1], xPos, yPos + textSize + padding, paint);
        return bmp;
    }

    @NonNull
    public static RoundedBitmapDrawable getRoundedBitmapDrawable(Context context, @DrawableRes int id) {
        Resources res = context.getResources();
        Bitmap src = BitmapFactory.decodeResource(res, id);

        RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(res, src);
        dr.setCornerRadius(Math.max(src.getWidth(), src.getHeight()) / 2.0f);

        return dr;
    }

    public static Bitmap getScaledBitmapFromUri(Context context, Uri uri) throws IOException {
        InputStream input = null;
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inJustDecodeBounds = true;
        try {
            input = context.getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(input, null, bitmapOptions);
        } finally {
            IOUtils.closeQuietly(input);
        }

        if ((bitmapOptions.outWidth == -1) || (bitmapOptions.outHeight == -1)) {
            throw new IOException("Cannot read image file");
        }

        bitmapOptions = prepareOptimizedOptions(bitmapOptions);
        Bitmap bitmap = null;
        try {
            input = context.getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        } finally {
            IOUtils.closeQuietly(input);
        }

        String filePath = ContentProviderUtility.getRealPathFromURI(context, uri);
        return rotateImage(filePath, bitmap);
    }

    public static Bitmap getScaledBitmapFromFile(String filePath) {
        BitmapFactory.Options options = getBitmapQualityOptions(filePath);
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        return ImageHelper.rotateImage(filePath, bitmap);
    }

    public static Bitmap combineImages(Bitmap first, Bitmap second) {
        Bitmap cs;
        int width;
        int height;

        width = first.getWidth() + second.getWidth();
        height = Math.max(first.getHeight(), second.getHeight());
        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(cs);
        comboImage.drawBitmap(first, 0f, 0f, null);
        comboImage.drawBitmap(second, first.getWidth(), 0, null);
        return cs;
    }

    public static boolean isNewGooglePhotosUri(Uri uri) {
        // This is to handle only cloud photos
        // removing this will rotate the local photos read from photos app by 90 degrees
        return "com.google.android.apps.photos.contentprovider".equals(uri.getAuthority())
                && uri.toString().contains("mediakey");
    }

    /**
     * Purpose of this transformation is to draw sole car icon over white disc(background)
     * to generate car type slider icon.
     */
    private static class CarIconizerTransformation implements Transformation<Bitmap> {

        private static final String ID = "com.rideaustin.utils.ImageHelper.CarIconizerTransformation";
        private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

        private final Bitmap background;
        private final boolean fullSize;

        private CarIconizerTransformation(@NonNull Bitmap background, boolean fullSize) {
            this.background = background;
            this.fullSize = fullSize;
        }

        @NonNull
        @Override
        public Resource<Bitmap> transform(@NonNull Context context, @NonNull Resource<Bitmap> resource, int width, int height) {
            int outWidth = background.getWidth();
            int outHeight = background.getHeight();
            final float left;
            final float top;
            final float scale;
            if (fullSize) {
                left = (float)(background.getWidth() - width) / 2;
                top = ViewUtils.dpToPixels(27) - (float) height / 2;
                scale = (float) width / resource.get().getWidth();
            } else {
                final float widthPercentage = (float) outWidth / resource.get().getWidth();
                final float heightPercentage = (float) outHeight / resource.get().getHeight();
                scale = Math.min(widthPercentage, heightPercentage) * 3f / 5f;

                final int targetWidth = (int) (scale * resource.get().getWidth());
                final int targetHeight = (int) (scale * resource.get().getHeight());

                left = (outWidth - targetWidth) / 2f;
                top = (outHeight - targetHeight) / 2f;
            }


            Bitmap.Config config = resource.get().getConfig() != null ? resource.get().getConfig() : Bitmap.Config.ARGB_8888;
            BitmapPool pool = Glide.get(context).getBitmapPool();
            Bitmap toReuse = pool.get(outWidth, outHeight, config);

            Canvas canvas = new Canvas(toReuse);
            Paint paint = new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
            canvas.drawBitmap(background, 0f, 0f, paint);

            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            matrix.postTranslate(left, top);
            canvas.drawBitmap(resource.get(), matrix, paint);

            return BitmapResource.obtain(toReuse, pool);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof CarIconizerTransformation;
        }

        @Override
        public int hashCode() {
            return ID.hashCode();
        }

        @Override
        public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
            messageDigest.update(ID_BYTES);
        }
    }

    public static Target cache(String url) {
        return Glide.with(App.getInstance())
                .asBitmap()
                .load(url)
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .preload();
    }

    public static void clear(View view) {
        Glide.with(view).clear(view);
    }

    public static <T> void clear(View view, Target<T> target) {
        Glide.with(view).clear(target);
    }

}
