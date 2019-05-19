package com.rideaustin.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.utils.AppInfoUtil;

/**
 * Created by vokol on 27.06.2016.
 */
public class MaterialDialogCreator {


    private MaterialDialogCreator() {
    }

    public static MaterialDialog createErrorDialog(@NonNull String title, @NonNull String dialogMessage,
                                                   @NonNull AppCompatActivity appCompatActivity) {
        return new MaterialDialog.Builder(appCompatActivity)
                .title(title)
                .content(dialogMessage)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .positiveText(App.getInstance().getString(R.string.btn_ok))
                .canceledOnTouchOutside(true)
                .show();
    }

    public static MaterialDialog createOnlinePopup(@NonNull String selectedCar, @NonNull String message,
                                                   @NonNull Context context) {
        return new MaterialDialog.Builder(context)
                .title(selectedCar)
                .titleGravity(GravityEnum.CENTER)
                .content(message)
                .contentGravity(GravityEnum.CENTER)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .neutralText(App.getInstance().getString(R.string.btn_dismiss))
                .buttonsGravity(GravityEnum.CENTER)
                .btnStackedGravity(GravityEnum.CENTER)
                .canceledOnTouchOutside(true)
                .show();
    }


    public static MaterialDialog createSimpleErrorDialog(@NonNull String dialogMessage,
                                                         @NonNull Context context) {
        return new MaterialDialog.Builder(context)
                .content(dialogMessage)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .positiveText(App.getInstance().getString(R.string.btn_ok))
                .canceledOnTouchOutside(true)
                .show();
    }

    public static MaterialDialog.Builder createOkSkipDialog(@NonNull String content, @NonNull Context context) {
        return new MaterialDialog.Builder(context)
                .content(content)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_REGULAR_OTF)
                .positiveText(R.string.btn_ok)
                .negativeText(R.string.btn_skip)
                .cancelable(false);
    }

    public static MaterialDialog createConfirmationDialog(@NonNull String dialogMessage,
                                                          @NonNull Context context) {
        return new MaterialDialog.Builder(context)
                .content(dialogMessage)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .positiveText(App.getInstance().getString(R.string.btn_yes))
                .neutralText(App.getInstance().getString(R.string.btn_no))
                .show();

    }

    public static MaterialDialog createConfirmationDialog(@NonNull String dialogMessage,
                                                           @NonNull Context context, @NonNull final MaterialDialog.SingleButtonCallback positiveCallback,
                                                          @NonNull final MaterialDialog.SingleButtonCallback negativeCallback) {
        return new MaterialDialog.Builder(context)
                .content(dialogMessage)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .positiveText(App.getInstance().getString(R.string.btn_yes))
                .onPositive(positiveCallback)
                .onNegative(negativeCallback)
                .neutralText(App.getInstance().getString(R.string.btn_no))
                .show();

    }

    public static MaterialDialog createConfirmStartRideDialog(@NonNull String dialogMessage,
                                                              @NonNull AppCompatActivity appCompatActivity) {
        return new MaterialDialog.Builder(appCompatActivity)
                .content(dialogMessage)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .positiveText(App.getInstance().getString(R.string.btn_yes))
                .negativeText(App.getInstance().getString(R.string.btn_no))
                .neutralText(App.getInstance().getString(R.string.btn_cancel))
                .show();
    }

    public static MaterialDialog createInfoDialog(@NonNull String title,
                                                  @NonNull String dialogMessage,
                                                  @NonNull AppCompatActivity appCompatActivity) {
        return new MaterialDialog.Builder(appCompatActivity)
                .title(title)
                .content(dialogMessage)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .positiveText(App.getInstance().getString(R.string.btn_ok))
                .canceledOnTouchOutside(true)
                .show();
    }

    public static MaterialDialog createInfoDialogCentered(@NonNull String title,
                                                          @NonNull String dialogMessage,
                                                          @NonNull AppCompatActivity appCompatActivity) {
        return new MaterialDialog.Builder(appCompatActivity)
                .title(title)
                .titleGravity(GravityEnum.CENTER)
                .content(dialogMessage)
                .contentGravity(GravityEnum.CENTER)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .positiveText(App.getInstance().getString(R.string.btn_ok))
                .canceledOnTouchOutside(true)
                .show();
    }

    public static MaterialDialog createForgotToStartEndTripDialog(@NonNull String title,
                                                                  @NonNull String dialogMessage,
                                                                  @NonNull MaterialDialog.SingleButtonCallback actionCallback,
                                                                  @NonNull AppCompatActivity appCompatActivity) {
        return new MaterialDialog.Builder(appCompatActivity)
                .title(title)
                .content(dialogMessage)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .positiveText(App.getInstance().getString(R.string.btn_yes))
                .onPositive(actionCallback)
                .negativeText(App.getInstance().getString(R.string.btn_no))
                .show();
    }

    public static MaterialDialog createListDialog(@NonNull AppCompatActivity appCompatActivity,
                                                  @NonNull String title,
                                                  @NonNull String message,
                                                  @NonNull RecyclerView.Adapter adapter) {
        return new MaterialDialog.Builder(appCompatActivity)
                .title(title)
                .content(message)
                .adapter(adapter, null)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .show();
    }

    public static MaterialDialog createInfoDialogWithCallback(@NonNull String title,
                                                              @NonNull String dialogMessage,
                                                              @NonNull MaterialDialog.SingleButtonCallback callback,
                                                              @NonNull AppCompatActivity appCompatActivity) {
        return new MaterialDialog.Builder(appCompatActivity)
                .title(title)
                .content(dialogMessage)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .onPositive(callback)
                .positiveText(App.getInstance().getString(R.string.btn_ok))
                .canceledOnTouchOutside(true)
                .show();
    }

    public static MaterialDialog createDefaultNavigationAppDialog(@NonNull View customView,
                                                                  @NonNull AppCompatActivity appCompatActivity) {
        return new MaterialDialog.Builder(appCompatActivity)
                .title(R.string.choose_navigation_app)
                .customView(customView, false)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .show();
    }

    public static MaterialDialog createShareNavigationDialog(@NonNull View customView,
                                                             @NonNull AppCompatActivity appCompatActivity,
                                                             @NonNull MaterialDialog.SingleButtonCallback onNavigateCallBack) {
        return new MaterialDialog.Builder(appCompatActivity)
                .title(R.string.choose_navigation_app)
                .customView(customView, false)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .positiveText(R.string.share_action_navigate)
                .onPositive(onNavigateCallBack)
                .negativeText(R.string.cancel)
                .onNegative((dialog, which) -> dialog.dismiss())
                .autoDismiss(false)
                .show();
    }

    public static MaterialDialog createAppUpdateDialog(@NonNull Activity activity,
                                                       boolean isMandatoryUpgrade,
                                                       MaterialDialog.SingleButtonCallback remindLaterAction) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(activity)
                .content(isMandatoryUpgrade ? R.string.mandatory_app_update_message : R.string.optional_app_update_message);
        builder.positiveText(R.string.action_update)
                .onPositive((dialog, which) -> AppInfoUtil.openPlayStore());
        if (!isMandatoryUpgrade) {
            builder.negativeText(R.string.action_remind_later)
                    .onNegative(remindLaterAction);
        }
        builder.typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_REGULAR_OTF)
                .autoDismiss(false)
                .cancelable(!isMandatoryUpgrade);
        return builder.show();
    }

    public static MaterialDialog createInAppMessageDialog(@NonNull String title,
                                                          @NonNull String dialogMessage,
                                                          @NonNull Activity activity,
                                                          Runnable readCallback) {
        return new MaterialDialog.Builder(activity)
                .title(title)
                .content(dialogMessage)
                .contentGravity(GravityEnum.CENTER)
                .titleGravity(GravityEnum.CENTER)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .positiveText(App.getInstance().getString(R.string.btn_ok))
                .onPositive((dialog, which) -> readCallback.run())
                .cancelListener(dialog -> readCallback.run())
                .show(); // dismiss should not trigger readCallback
    }

    public static MaterialDialog createCenteredMessageDialog(@NonNull String title, @NonNull String dialogMessage,
                                                             @NonNull AppCompatActivity appCompatActivity) {
        return new MaterialDialog.Builder(appCompatActivity)
                .title(title)
                .content(dialogMessage)
                .contentGravity(GravityEnum.CENTER)
                .titleGravity(GravityEnum.CENTER)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .positiveText(App.getInstance().getString(R.string.btn_ok))
                .canceledOnTouchOutside(true)
                .show();
    }

    public static MaterialDialog.Builder createSimpleDialog(@NonNull String dialogMessage, @NonNull AppCompatActivity appCompatActivity) {
        return new MaterialDialog.Builder(appCompatActivity)
                .title(App.getAppName())
                .content(dialogMessage)
                .contentGravity(GravityEnum.CENTER)
                .titleGravity(GravityEnum.CENTER)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .positiveText(R.string.btn_ok)
                .neutralText(R.string.btn_no);
    }

    public static MaterialDialog.Builder createDialogWithCallback(final @NonNull Activity context,
                                                                  @NonNull String title,
                                                                  @NonNull String message,
                                                                  @StringRes int posButtonId,
                                                                  @NonNull MaterialDialog.SingleButtonCallback callback) {
        return new MaterialDialog.Builder(context)
                .title(title)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_REGULAR_OTF)
                .content(message)
                .titleGravity(GravityEnum.CENTER)
                .contentGravity(GravityEnum.CENTER)
                .positiveText(posButtonId)
                .onPositive(callback)
                .negativeText(R.string.btn_cancel)
                .cancelable(true);
    }

}
