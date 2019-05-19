package com.rideaustin.ui.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.AppInfoUtil;
import com.rideaustin.utils.DeviceInfoUtil;
import com.rideaustin.utils.ViewUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import java8.util.Optional;
import rx.functions.Action0;
import timber.log.Timber;

/**
 * Created by Viktor Kifer
 * On 24-Dec-2016.
 */

public class CommonMaterialDialogCreator {

    private static final int POPUP_SHOW_MS = 2200;
    private static final int POPUP_ANIMATION_SHOW_MS = 300;
    private static final int POPUP_ANIMATION_HIDE_MS = 150;
    private static final int POPUP_ANIMATION_DELAY_MS = 100;
    private static final int POPUP_SLIDE_UP_MS = 400;
    private static final int POPUP_SLIDE_DOWN_MS = 200;

    private CommonMaterialDialogCreator() {
    }

    public static MaterialDialog.Builder createNetworkFailDialog(@NonNull String dialogMessage, @NonNull Context context) {
        return new MaterialDialog.Builder(context)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .content(dialogMessage)
                .positiveText(R.string.retry)
                .negativeText(R.string.cancel)
                .cancelable(true);
    }

    public static MaterialDialog.Builder createNonCancelNetworkFailDialog(@NonNull String dialogMessage, @NonNull Context context) {
        return createNetworkFailDialog(dialogMessage, context).cancelable(false);
    }

    public static MaterialDialog createOptionalUpdateDialog(@NonNull Activity activity, MaterialDialog.SingleButtonCallback remindLaterAction) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(activity)
                .content(R.string.optional_app_update_message);
        builder.positiveText(R.string.action_update)
                .onPositive((dialog, which) -> AppInfoUtil.openPlayStore());

        builder.negativeText(R.string.action_remind_later)
                .onNegative(remindLaterAction);

        builder.typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_REGULAR_OTF)
                .autoDismiss(false)
                .cancelable(true);
        return builder.show();
    }

    public static MaterialDialog createMandatoryUpdateDialog(@NonNull Activity activity) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(activity)
                .content(R.string.mandatory_app_update_message);
        builder.positiveText(R.string.action_update)
                .onPositive((dialog, which) -> AppInfoUtil.openPlayStore());

        builder.typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_REGULAR_OTF)
                .autoDismiss(false)
                .cancelable(false);
        return builder.show();
    }

    public static void showSupportSuccessDialog(Activity activity, Optional<String> message, boolean isLong) {
        showSupportSuccessDialog(activity, message, isLong, () -> {
            if (activity != null) {
                activity.onBackPressed();
            }
        });
    }

    public static void showSupportSuccessDialog(Activity activity, Optional<String> message, boolean isLong, Action0 closeAction) {
        if (activity != null) {
            new SupportSuccessDialog(activity).showDialog(message, isLong, closeAction);
        }
    }

    public static void animateDialogShow(final View dialogView) {
        animateDialogShow(dialogView, null);
    }

    public static void animateDialogShow(final View dialogView, @Nullable Runnable onAnimationFinished) {
        dialogView.setScaleX(0.0f);
        dialogView.setScaleY(0.0f);
        dialogView.setVisibility(View.VISIBLE);
        dialogView.animate()
                .scaleX(1).scaleY(1)
                .setInterpolator(new OvershootInterpolator())
                .setStartDelay(POPUP_ANIMATION_DELAY_MS)
                .setDuration(POPUP_ANIMATION_SHOW_MS)
                .withEndAction(onAnimationFinished)
                .start();
    }

    public static void animateDialogDismiss(final View dialogView, Runnable onAnimationFinished) {
        dialogView.animate()
                .scaleX(0.0f).scaleY(0.0f).alpha(0.0f)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(POPUP_ANIMATION_HIDE_MS)
                .withEndAction(onAnimationFinished)
                .start();
    }

    public static void slideDialogUp(final View dialogView, @Nullable Runnable onAnimationFinished) {
        dialogView.animate()
                .y(0.0f)
                .setInterpolator(new DecelerateInterpolator())
                .setStartDelay(POPUP_ANIMATION_DELAY_MS)
                .setDuration(POPUP_SLIDE_UP_MS)
                .withEndAction(onAnimationFinished)
                .start();
    }

    public static void slideDialogDown(final View dialogView, Runnable onAnimationFinished) {
        dialogView.animate()
                .y(dialogView.getHeight())
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(POPUP_SLIDE_DOWN_MS)
                .withEndAction(onAnimationFinished)
                .start();
    }

    public static MaterialDialog.Builder createNoVoIPDialog(String errorMessage, Activity activity, MaterialDialog.SingleButtonCallback callback) {
        return new MaterialDialog.Builder(activity)
                .content(errorMessage)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .negativeText(R.string.edit)
                .onNegative(callback)
                .cancelable(false);
    }

    public static void showSkipPhoneVerificationDialog(Activity activity, Action0 onSkipAccepted, Action0 onSkipDeclined) {
        createYesNoDialog(App.getInstance().getString(R.string.bypass_pin_message), activity)
                .title(R.string.bypass_pin_title)
                .onPositive((dialog, yesButton) -> onSkipDeclined.call())
                .onNegative((dialog, noButton) -> onSkipAccepted.call())
                .show();
    }

    public static MaterialDialog.Builder createYesNoDialog(@NonNull String content, @NonNull Context context) {
        return new MaterialDialog.Builder(context)
                .content(content)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_REGULAR_OTF)
                .positiveText(R.string.btn_yes)
                .negativeText(R.string.btn_no)
                .cancelable(false);
    }

    private static class SupportSuccessDialog {

        WeakReference<Activity> activityRef = new WeakReference<>(null);
        private boolean isLong;
        private Action0 onCloseAction;

        SupportSuccessDialog(Activity activity) {
            activityRef = new WeakReference<>(activity);
        }

        void showDialog(Optional<String> message, boolean isLong, Action0 onCloseAction) {
            this.isLong = isLong;
            this.onCloseAction = onCloseAction;
            Activity activity = activityRef.get();
            if (activity != null) {
                LayoutInflater inflater = activity.getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_support_message_sent, null);
                TextView messageView = (TextView) view.findViewById(R.id.message);
                messageView.setText(message.orElse(activity.getString(R.string.trip_history_support_message_success_message)));
                messageView.setVisibility(!TextUtils.isEmpty(messageView.getText()) ? View.VISIBLE : View.GONE);
                Dialog dialog = new Dialog(activity);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(view);
                final Window window = dialog.getWindow();
                if (window != null) {
                    window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                    window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }
                dialog.show();

                View content = view.findViewById(R.id.viewContent);
                RxSchedulers.main().createWorker().schedule(() -> animateContent(dialog, content), 100, TimeUnit.MILLISECONDS); // add small delay to prevent animation twitch
            }
        }

        private void hideDialog(Dialog dialog, View content) {
            animateDialogDismiss(content, () -> dismissDialog(dialog));
        }

        private void animateContent(Dialog dialog, View content) {
            animateDialogShow(content);
            RxSchedulers.schedule(() -> hideDialog(dialog, content), isLong ? POPUP_SHOW_MS * 3 : POPUP_SHOW_MS, TimeUnit.MILLISECONDS);
        }

        private void dismissDialog(Dialog dialog) {
            RxSchedulers.schedule(() -> {
                try {
                    dialog.dismiss();
                    onCloseAction.call();
                } catch (Exception e) {
                    Timber.e(e, "Unable to hide message sent dialog");
                }
            }, 100, TimeUnit.MILLISECONDS); // add small delay to prevent animation twitch

        }
    }
}
