package com.rideaustin.utils.toast;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.utils.ResourceHelper;

/**
 * Created by hatak on 28.06.2017.
 */

public class RAToast {
    public static void show(final String message, int duration, ToastMode toastMode) {
        RxSchedulers.schedule(() -> {
            LayoutInflater inflater = (LayoutInflater) App.getInstance().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout;

            switch (toastMode) {
                case DARK:
                    layout = inflater.inflate(R.layout.toast_layout_dark, null);
                    break;
                case LIGHT:
                    layout = inflater.inflate(R.layout.toast_layout, null);
                    break;
                default:
                    layout = inflater.inflate(R.layout.toast_layout, null);

            }

            ImageView appLogo = (ImageView) layout.findViewById(R.id.app_logo);
            setLogo(appLogo, toastMode);

            TextView messageText = (TextView) layout.findViewById(R.id.message_text);
            messageText.setTypeface(ResourcesCompat.getFont(App.getInstance(), R.font.montserrat_light));
            messageText.setText(message);

            Toast toast = new Toast(App.getInstance());
            toast.setDuration(duration);
            toast.setView(layout);
            toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, 0);
            toast.setMargin(0, 0.02f);
            toast.show();
        });
    }

    private static void setLogo(ImageView imageView, ToastMode toastMode) {
        GlobalConfig lastConfiguration = App.getConfigurationManager().getLastConfiguration();
        switch (toastMode) {
            case DARK:
                imageView.setImageDrawable(ContextCompat.getDrawable(App.getInstance(), ResourceHelper.getWhiteLogoDrawableRes(lastConfiguration)));
                break;
            case LIGHT:
                imageView.setImageDrawable(ContextCompat.getDrawable(App.getInstance(), ResourceHelper.getBlackLogoDrawableRes(lastConfiguration)));
                break;
        }
    }

    public static void show(final @StringRes int message, int duration) {
        show(App.getInstance().getString(message), duration, ToastMode.DARK);
    }

    public static void show(final String message, int duration) {
        show(message, duration, ToastMode.DARK);
    }

    public static void showShort(final @StringRes int message) {
        show(message, Toast.LENGTH_SHORT);
    }

    public static void showShort(String message) {
        show(message, Toast.LENGTH_SHORT);
    }

    public static void showLong(final @StringRes int message) {
        show(message, Toast.LENGTH_LONG);
    }

    public static void showLong(String message) {
        show(message, Toast.LENGTH_LONG);
    }

}
