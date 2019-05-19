package com.rideaustin.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.ui.drawer.promotions.OnShareAppSelectedListener;
import com.rideaustin.ui.drawer.promotions.ShareOption;
import com.rideaustin.utils.AppInfoUtil;
import com.rideaustin.utils.ImageHelper;

/**
 * Created by vokol on 27.06.2016.
 */
public class MaterialDialogCreator {

    private MaterialDialogCreator() {
    }

    public static MaterialDialog.Builder createDialog(@NonNull String dialogMessage, @NonNull Activity activity) {
        return new MaterialDialog.Builder(activity)
                .content(dialogMessage)
                .contentGravity(GravityEnum.CENTER)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .positiveText(App.getInstance().getString(R.string.btn_ok));
    }

    public static MaterialDialog createErrorDialog(@NonNull String title, @NonNull String dialogMessage,
                                                   @NonNull AppCompatActivity appCompatActivity) {
        return new MaterialDialog.Builder(appCompatActivity)
                .title(title)
                .titleGravity(GravityEnum.CENTER)
                .content(dialogMessage)
                .contentGravity(GravityEnum.CENTER)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .positiveText(App.getInstance().getString(R.string.btn_ok))
                .show();
    }

    public static MaterialDialog createSimpleErrorDialog(@NonNull String dialogMessage,
                                                         @NonNull Activity activity) {
        return new MaterialDialog.Builder(activity)
                .content(dialogMessage)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .positiveText(App.getInstance().getString(R.string.btn_ok))
                .show();
    }

    public static MaterialDialog createSimpleDialog(@NonNull String dialogMessage,
                                                    @NonNull AppCompatActivity appCompatActivity) {

        return createSimpleDialog(dialogMessage, GravityEnum.START, appCompatActivity);
    }

    public static MaterialDialog createSimpleDialog(@NonNull String dialogMessage, GravityEnum alignment,
                                                    @NonNull AppCompatActivity appCompatActivity) {
        return new MaterialDialog.Builder(appCompatActivity)
                .content(dialogMessage)
                .contentGravity(alignment)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .positiveText(App.getInstance().getString(R.string.btn_ok))
                .show();
    }

    public static MaterialDialog createCancelRideDialog(@NonNull AppCompatActivity appCompatActivity) {
        return new MaterialDialog.Builder(appCompatActivity)
                .title(R.string.cancel_trip)
                .positiveText(R.string.btn_yes)
                .negativeText(R.string.btn_no)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .show();
    }

    public static MaterialDialog.Builder createUserExistsDialog(@NonNull String dialogMessage,
                                                                @NonNull AppCompatActivity appCompatActivity) {
        return new MaterialDialog.Builder(appCompatActivity)
                .content(dialogMessage)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .positiveText(R.string.login)
                .negativeText(R.string.edit)
                .cancelable(false);
    }

    public static MaterialDialog.Builder createTippingConfirmDialog(float tipping,
                                                                    @NonNull Activity activity) {
        return new MaterialDialog.Builder(activity)
                .title(App.getAppName())
                .content(App.getInstance().getString(R.string.confirm_tipping_content, tipping))
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_REGULAR_OTF)
                .positiveText(R.string.confirm)
                .negativeText(R.string.cancel)
                .titleGravity(GravityEnum.CENTER)
                .contentGravity(GravityEnum.CENTER)
                .cancelable(false);
    }

    public static MaterialDialog.Builder createConfirmDialog(@NonNull String dialogMessage,
                                                             @NonNull AppCompatActivity appCompatActivity) {
        return new MaterialDialog.Builder(appCompatActivity)
                .content(dialogMessage)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_REGULAR_OTF)
                .positiveText(R.string.correct)
                .negativeText(R.string.reenter)
                .cancelable(false);
    }

    public static MaterialDialog.Builder createConfirmDialog(@NonNull String dialogTitle, @NonNull String dialogMessage,
                                                             @NonNull Activity activity) {
        return new MaterialDialog.Builder(activity)
                .title(dialogTitle)
                .content(dialogMessage)
                .titleGravity(GravityEnum.CENTER)
                .contentGravity(GravityEnum.CENTER)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_REGULAR_OTF)
                .positiveText(R.string.correct)
                .negativeText(R.string.reenter)
                .cancelable(false);
    }

    public static MaterialDialog.Builder createConfirmDeletePaymentDialog(@NonNull String dialogMessage,
                                                                          @NonNull AppCompatActivity appCompatActivity) {
        return new MaterialDialog.Builder(appCompatActivity)
                .content(dialogMessage)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_REGULAR_OTF)
                .positiveText(R.string.btn_yes)
                .negativeText(R.string.btn_no)
                .cancelable(false);
    }

    public static MaterialDialog createUpdateRideDialog(@NonNull String title, @NonNull String content,
                                                        @NonNull Context context) {
        return new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_REGULAR_OTF)
                .positiveText(R.string.btn_yes)
                .negativeText(R.string.btn_no)
                .cancelable(false)
                .show();
    }

    public static MaterialDialog createUpdateRideFailedDialog(@NonNull String title, @NonNull String content,
                                                              @NonNull Context context) {
        return new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_REGULAR_OTF)
                .positiveText(R.string.retry)
                .negativeText(R.string.btn_cancel)
                .cancelable(false)
                .show();
    }

    public static MaterialDialog createConfirmLicenseDialog(@NonNull String content,
                                                            @NonNull AppCompatActivity activity) {
        return CommonMaterialDialogCreator.createYesNoDialog(content, activity)
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

    public static MaterialDialog.Builder createNumberDialog(@NonNull String title,
                                                            @NonNull Context context) {
        return new MaterialDialog.Builder(context)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_REGULAR_OTF)
                .title(title)
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .positiveText(R.string.btn_ok)
                .negativeText(R.string.btn_cancel)
                .cancelable(true);
    }


    public static MaterialDialog.Builder createSimpleConfirmDialog(@NonNull String dialogMessage, @NonNull Context context) {
        return new MaterialDialog.Builder(context)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .content(dialogMessage)
                .positiveText(R.string.btn_ok)
                .negativeText(R.string.btn_cancel)
                .cancelable(true);
    }

    public static MaterialDialog createCarTypeDescriptionDialog(@NonNull String title,
                                                                @NonNull String message,
                                                                @NonNull Context context) {
        return new MaterialDialog.Builder(context)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_REGULAR_OTF)
                .content(Html.fromHtml(message)) //We have link in text
                .title(title)
                .titleGravity(GravityEnum.CENTER)
                .positiveText(R.string.btn_ok)
                .show();
    }

    public static MaterialDialog.Builder createCustomViewDialog(@NonNull Context context, @NonNull String title,
                                                                @NonNull View view, boolean wrapInScrollView) {
        return new MaterialDialog.Builder(context)
                .title(title)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_REGULAR_OTF)
                .customView(view, wrapInScrollView)
                .positiveText(R.string.btn_ok)
                .negativeText(R.string.btn_cancel)
                .cancelable(true);
    }

    public static MaterialDialog.Builder createMessageWithRoundedPicDialog(@NonNull Activity context,
                                                                           @NonNull String photoUrl,
                                                                           @NonNull String message) {
        com.rideaustin.databinding.LayoutRoundedPicWithMessageBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.layout_rounded_pic_with_message, null, false);
        ImageHelper.loadRoundImageIntoView(binding.imIcon, photoUrl, R.drawable.ic_person);
        binding.tvMessage.setText(Html.fromHtml(message));
        return new MaterialDialog.Builder(context)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_REGULAR_OTF)
                .customView(binding.getRoot(), false)
                .cancelable(true)
                .autoDismiss(false);
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
                .buttonsGravity(GravityEnum.CENTER)
                .positiveText(posButtonId)
                .onPositive(callback)
                .negativeText(R.string.btn_cancel)
                .cancelable(true);
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
                                                             @NonNull Activity activity) {
        return new MaterialDialog.Builder(activity)
                .title(title)
                .content(dialogMessage)
                .contentGravity(GravityEnum.CENTER)
                .titleGravity(GravityEnum.CENTER)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .positiveText(App.getInstance().getString(R.string.btn_ok))
                .show();
    }

    public static MaterialDialog createShareOnSocialMediaDialog(@NonNull Context context, final OnShareAppSelectedListener selectedListener) {
        return new MaterialDialog.Builder(context)
                .title("Select how would you like to share")
                .customView(R.layout.dialog_social_media, false)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .positiveText("select")
                .onPositive((dialog, which) -> {
                    RadioButton facebook = (RadioButton) dialog.findViewById(R.id.radio_facebook);
                    RadioButton other = (RadioButton) dialog.findViewById(R.id.radio_other);
                    if (facebook.isChecked()) {
                        selectedListener.onApplicationSelected(ShareOption.FACEBOOK);
                        dialog.dismiss();
                    } else if (other.isChecked()) {
                        selectedListener.onApplicationSelected(ShareOption.OTHER);
                        dialog.dismiss();
                    }
                })
                .negativeText(R.string.cancel)
                .onNegative((dialog, which) -> dialog.dismiss())
                .autoDismiss(false)
                .show();
    }

    public static MaterialDialog createPricingDetailsDialog(@NonNull Context context, @NonNull View view, boolean wrapInScrollView) {
        return new MaterialDialog.Builder(context)
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_REGULAR_OTF)
                .customView(view, wrapInScrollView)
                .canceledOnTouchOutside(true)
                .cancelable(true)
                .show();
    }

}
