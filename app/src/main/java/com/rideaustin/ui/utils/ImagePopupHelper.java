package com.rideaustin.ui.utils;

import android.app.Activity;
import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.rideaustin.R;
import com.rideaustin.databinding.ImageDialogBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.DialogUtils;
import com.rideaustin.utils.ImageHelper;

import java.util.concurrent.TimeUnit;

/**
 * Created by crossover on 20/02/2017.
 */

public class ImagePopupHelper {

    private Target target;
    private final ImageView imageView;
    private final Dialog dialog;

    public ImagePopupHelper(Activity activity) {
        ImageDialogBinding binding = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.dialog_image, null, false);
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(binding.getRoot());
        final Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setOnKeyListener((dialogInterface, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                hide();
                return true;
            }
            return false;
        });
        dialog.setOnDismissListener(dialogInterface -> {
            if (target != null) {
                Glide.with(dialog.getContext()).clear(target);
            }
        });
        binding.btnClose.setOnClickListener(v -> hide());
        imageView = binding.dialogImageView;
    }

    public void show(String url) {
        dialog.show();
        RxSchedulers.schedule(() -> load(url), 100, TimeUnit.MILLISECONDS); // add small delay to prevent animation twitch
    }

    public void hide() {
        if (dialog.isShowing()) {
            imageView.animate()
                    .scaleX(0f).scaleY(0f)
                    .setInterpolator(new AnticipateInterpolator())
                    .setDuration(400)
                    .withEndAction(this::dismiss)
                    .start();
        }
    }

    private void load(String url) {
        target = ImageHelper.loadImageIntoTarget(dialog.getContext(), new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                imageView.setImageBitmap(resource);
                imageView.setScaleX(0f);
                imageView.setScaleY(0f);
                imageView.setVisibility(View.VISIBLE);
                imageView.animate()
                        .scaleX(1).scaleY(1)
                        .setInterpolator(new OvershootInterpolator())
                        .setDuration(500)
                        .start();
            }

            @Override
            public void onLoadCleared(Drawable placeholder) {
                super.onLoadCleared(placeholder);
                // RA-12165: should handle this case for custom targets
                // https://github.com/bumptech/glide/issues/1761#issuecomment-283444560
                imageView.setImageDrawable(placeholder);
            }
        }, url);
    }

    private void dismiss() {
        RxSchedulers.schedule(() -> {
            DialogUtils.dismiss(dialog, "Unable to dismiss image popup");
        }, 100, TimeUnit.MILLISECONDS); // add small delay to prevent animation twitch
    }
}
