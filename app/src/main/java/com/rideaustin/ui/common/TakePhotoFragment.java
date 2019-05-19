package com.rideaustin.ui.common;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rideaustin.R;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.TakePhotoBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.FileDirectoryUtil;
import com.rideaustin.utils.ImageHelper;
import com.rideaustin.utils.PermissionUtils;
import com.rideaustin.utils.TimeUtils;
import com.rideaustin.utils.toast.RAToast;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

/**
 * Created by rost on 8/9/16.
 */
public class TakePhotoFragment extends BaseFragment {

    private static final short CAPTURE_IMAGE_CODE = 0;
    private static final short PICK_IMAGE_CODE = 1;
    private static final String CAMERA_FILE_KEY = "camera_file";
    private Subscription permissionSubscription = Subscriptions.empty();

    private TakePhotoListener takePhotoListener;
    private TakePhotoBinding binding;
    private File cameraFile;

    public static TakePhotoFragment newInstance() {
        return new TakePhotoFragment();
    }

    public void setTakePhotoListener(final TakePhotoListener listener) {
        takePhotoListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_take_photo, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.choosePhoto.setOnClickListener(v -> takePhoto(Source.GALLERY));
        binding.takePhoto.setOnClickListener(v -> takePhoto(Source.CAMERA));
        binding.cancel.setOnClickListener(v -> {
            if (takePhotoListener != null) {
                takePhotoListener.onCanceled();
            }
        });
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof TakePhotoFragment.TakePhotoListenerContainer) {
            takePhotoListener = ((TakePhotoListenerContainer) activity).getTakePhotoListener();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        takePhotoListener = null;
        permissionSubscription.unsubscribe();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (cameraFile != null) {
            outState.putString(CAMERA_FILE_KEY, cameraFile.getAbsolutePath());
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            String path = savedInstanceState.getString(CAMERA_FILE_KEY);
            if (!TextUtils.isEmpty(path)) {
                cameraFile = new File(path);
            }
        }
    }

    private void takePhoto(final Source source) {
        final String[] permission = getPermissionForSource(source);
        permissionSubscription.unsubscribe();
        permissionSubscription = new RxPermissions(getActivity())
                .request(permission)
                .subscribe(granted -> {
                    if (granted) {
                        switch (source) {
                            case CAMERA:
                                sendCameraIntent();
                                break;
                            case GALLERY:
                                sendGalleryIntent();
                                break;
                        }
                    } else {
                        PermissionUtils.checkDeniedPermissions(getActivity(), permission);
                    }
                }, throwable -> {
                    Timber.e(throwable);
                    RAToast.show(R.string.error_unknown, Toast.LENGTH_SHORT);
                });
    }

    private String[] getPermissionForSource(Source source) {
        switch (source) {
            case CAMERA:
                return new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE };
            case GALLERY:
                return new String[] { Manifest.permission.READ_EXTERNAL_STORAGE };
            default:
                throw new IllegalArgumentException("unknown type " + source);
        }
    }

    private void sendCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Context context = getContext();
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            try {
                // Create the File where the photo should go
                cameraFile = FileDirectoryUtil.createImageFile();
                cameraFile.deleteOnExit();
            } catch (IOException e) {
                RAToast.showShort(e.getMessage());
                Timber.e(e);
            }
            if (cameraFile != null) {
                Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", cameraFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.setClipData(ClipData.newRawUri(null, uri));
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(intent, CAPTURE_IMAGE_CODE);
            }
        }
    }

    private void sendGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(intent, PICK_IMAGE_CODE);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, final Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (takePhotoListener == null) {
            Timber.d("onActivityResult: takePhotoListener is null");
            return;
        }

        if (resultCode == Activity.RESULT_CANCELED) {
            switch (requestCode) {
                case CAPTURE_IMAGE_CODE:
                    FileUtils.deleteQuietly(cameraFile);
                    cameraFile = null;
                    takePhotoListener.onCanceled();
                    break;
                case PICK_IMAGE_CODE:
                    takePhotoListener.onCanceled();
                    break;
                default:
                    Timber.w("received unexpected requestCode %d", requestCode);
            }
            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            extractImage(requestCode, intent)
                    .subscribeOn(RxSchedulers.computation())
                    .observeOn(RxSchedulers.main())
                    .subscribe(new ApiSubscriber2<SourcedImage>(getCallback(), false) {
                        @Override
                        public void onNext(SourcedImage sourcedImage) {
                            if (getCallback() != null) {
                                getCallback().hideProgress();
                            }
                            Source source = sourcedImage.getSource();
                            String filePath = sourcedImage.getFilePath();
                            takePhotoListener.onPhotoTaken(source, filePath);
                        }

                        @Override
                        public void onAnyError(BaseApiException e) {
                            RAToast.showLong(e.getMessage());
                            Timber.e(e);
                        }
                    });
        }
    }

    private Observable<SourcedImage> extractImage(int requestCode, Intent intent) {
        return Observable.fromCallable(() -> {
            switch (requestCode) {
                case CAPTURE_IMAGE_CODE:
                    return extractImageFromCamera();
                case PICK_IMAGE_CODE:
                    return extractImageFromGallery(intent.getData());
                default:
                    Timber.w("received unexpected request code: %d", requestCode);
                    throw new IllegalArgumentException("received unexpected request code: " + requestCode);
            }
        });
    }

    private SourcedImage extractImageFromCamera() throws IOException {
        if (cameraFile == null) {
            throw new IllegalStateException("Camera file is empty");
        }
        String filePath = cameraFile.getAbsolutePath();
        scaleBitmapFromFile(filePath, filePath);
        return new SourcedImage(Source.CAMERA, filePath);
    }

    private SourcedImage extractImageFromGallery(Uri uri) throws IOException {
        Context context = getContext();
        if (context == null) {
            throw new IllegalStateException("No context");
        }
        String targetPath = context.getFilesDir().getPath() + "temp_pic_" + TimeUtils.currentTimeMillis();
        if (!ImageHelper.isNewGooglePhotosUri(uri)) {
            String sourcePath = ImageHelper.getImagePath(context.getContentResolver(), uri);
            scaleBitmapFromFile(sourcePath, targetPath);
        } else {
            scaleBitmapFromUri(context, uri, targetPath);
        }
        return new SourcedImage(Source.GALLERY, targetPath);
    }

    private void scaleBitmapFromFile(String sourcePath, String targetPath) throws IOException {
        Bitmap bitmap = ImageHelper.getScaledBitmapFromFile(sourcePath);
        ImageHelper.saveBitmapToFile(bitmap, targetPath, Bitmap.CompressFormat.JPEG, 85);
    }

    private void scaleBitmapFromUri(Context context, Uri uri, String targetPath) throws IOException {
        Bitmap bitmap = ImageHelper.getScaledBitmapFromUri(context, uri);
        ImageHelper.saveBitmapToFile(bitmap, targetPath, Bitmap.CompressFormat.JPEG, 85);
    }

    public enum Source {
        CAMERA, GALLERY
    }

    public interface TakePhotoListener {
        void onPhotoTaken(Source source, String filePath);
        void onCanceled();
    }

    public interface TakePhotoListenerContainer {
        TakePhotoListener getTakePhotoListener();

        void setTakePhotoListener(TakePhotoListener listener);
    }

    private static class SourcedImage {
        private Source source;
        private String filePath;

        private SourcedImage(Source source, String filePath) {
            this.source = source;
            this.filePath = filePath;
        }

        public Source getSource() {
            return source;
        }

        public String getFilePath() {
            return filePath;
        }

        public boolean isValid() {
            return source != null && !TextUtils.isEmpty(filePath);
        }
    }
}
