package com.rideaustin.utils;

import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * Created by Sergey Petrov on 01/03/2017.
 */

public class ViewUtils {

    /**
     * Calculates view's rect in parent
     * @see <a href="http://stackoverflow.com/a/36740277">on StackOverflow</a>
     * @param view view, which drawing rect needs to be calculated
     * @param parent parent of view
     * @return rect
     */
    public static Rect getRectInParent(View view, ViewGroup parent) {
        Rect rect = new Rect();
        view.getDrawingRect(rect);
        parent.offsetDescendantRectToMyCoords(view, rect);
        return rect;
    }

    /**
     * Converts DP to pixels
     * @see <a href="http://stackoverflow.com/questions/4605527/converting-pixels-to-dp">on StackOverflow</a>
     * @param dp
     * @return pixels value
     */
    public static float dpToPixels(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

    public static void setCursorColor(EditText editText, int color) {
        //http://stackoverflow.com/questions/25996032/how-to-change-programatically-edittext-cursor-color-in-android
        try {
            Field fCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            fCursorDrawableRes.setAccessible(true);
            int mCursorDrawableRes = fCursorDrawableRes.getInt(editText);
            Field fEditor = TextView.class.getDeclaredField("mEditor");
            fEditor.setAccessible(true);
            Object editor = fEditor.get(editText);
            Class<?> clazz = editor.getClass();
            Field fCursorDrawable = clazz.getDeclaredField("mCursorDrawable");
            fCursorDrawable.setAccessible(true);
            Drawable[] drawables = new Drawable[2];
            drawables[0] = ContextCompat.getDrawable(editText.getContext(), mCursorDrawableRes);
            drawables[1] = ContextCompat.getDrawable(editText.getContext(), mCursorDrawableRes);
            drawables[0].setColorFilter(color, PorterDuff.Mode.SRC_IN);
            drawables[1].setColorFilter(color, PorterDuff.Mode.SRC_IN);
            fCursorDrawable.set(editor, drawables);
        } catch (final Throwable ignored) {
            //
        }
    }
}
