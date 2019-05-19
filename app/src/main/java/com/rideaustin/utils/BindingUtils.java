package com.rideaustin.utils;

import android.databinding.BindingAdapter;
import android.databinding.BindingConversion;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Sergey Petrov on 24/05/2017.
 */

public class BindingUtils {

    @BindingConversion
    public static int convertBooleanToVisibility(boolean visible) {
        return visible ? View.VISIBLE : View.GONE;
    }

    @BindingAdapter("android:src")
    public static void setImageResource(ImageView imageView, int resource) {
        imageView.setImageResource(resource);
    }

    @BindingAdapter("android:drawableLeft")
    public static void setDrawableLeft(TextView textView, int resource) {
        Drawable left = resource > 0
                ? ContextCompat.getDrawable(textView.getContext(), resource)
                : null;
        textView.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null);
    }

    @BindingAdapter("backgroundColorRes")
    public static void setBackgroundColorRes(View view, @ColorRes int resource) {
        view.setBackgroundColor(ContextCompat.getColor(view.getContext(), resource));
    }

    @BindingAdapter("backgroundColor")
    public static void setBackgroundColor(View view, @ColorInt int color) {
        view.setBackgroundColor(color);
    }

    @BindingAdapter("backgroundDrawableRes")
    public static void setBackgroundDrawableRes(View view, @DrawableRes int resource) {
        view.setBackgroundResource(resource);
    }

    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView view, String imageUrl) {
        ImageHelper.loadImageIntoView(view, imageUrl);
    }

    @BindingAdapter("htmlText")
    public static void setHtmlText(TextView view, String htmlString) {
        setHtmlText(view, htmlString, false);
    }

    @BindingAdapter("htmlTextInteractive")
    public static void setHtmlTextInteractive(TextView view, String htmlString) {
        setHtmlText(view, htmlString, true);
    }

    private static void setHtmlText(TextView view, String htmlString, boolean interactive) {
        if (htmlString != null) {
            Spanned result;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                result = Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY);
            } else {
                result = Html.fromHtml(htmlString);
            }
            view.setText(result, TextView.BufferType.SPANNABLE);
            if (interactive) {
                view.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }

    @BindingAdapter("fontSize")
    public static void setFontSize(TextView textView, int fontSize) {
        textView.setTextSize(fontSize);
    }
}
