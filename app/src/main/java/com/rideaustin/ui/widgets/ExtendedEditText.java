package com.rideaustin.ui.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.EditText;

import com.rideaustin.R;

/**
 * Source copied from here : https://gist.github.com/kennydude/5407963
 * <p>
 * This is an extended EditText with a Prefix and Suffix.
 * <p>
 * As used by "Allowance" on Google Play (v1.1)
 */
public class ExtendedEditText extends EditText {
    // Stuff to do with our rendering
    TextPaint textPaint = new TextPaint();
    float fontHeight;
    TagDrawable left;

    // The actual suffix
    String suffix = "";

    // These are used to store details obtained from the EditText's rendering process
    Rect line0bounds = new Rect();
    int line0Baseline;

    public ExtendedEditText(Context context) {
        super(context);
        init(context, null);
    }

    public ExtendedEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public ExtendedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ExtendedEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        left = new TagDrawable();

        fontHeight = getTextSize();

        textPaint.setColor(getCurrentHintTextColor());
        textPaint.setTextSize(fontHeight);
        textPaint.setTextAlign(Paint.Align.LEFT);

        // Setup the left side
        setCompoundDrawablesRelative(left, null, null, null);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExtendedEditText);

        try {
            String prefix = a.getString(R.styleable.ExtendedEditText_prefixText);
            if (prefix != null) {
                setPrefix(prefix);
            }
        } finally {
            a.recycle();
        }
    }

    @Override
    public void setTypeface(Typeface typeface) {
        super.setTypeface(typeface);
        if (textPaint != null) {
            // Sometimes TextView itself calls me when i'm naked
            textPaint.setTypeface(typeface);
        }

        postInvalidate();
    }

    public void setPrefix(String s) {
        left.setText(s);
        setCompoundDrawablesRelative(left, null, null, null);
    }

    public void setSuffix(String s) {
        suffix = s;
        setCompoundDrawablesRelative(left, null, null, null);
    }

    @Override
    public void onDraw(Canvas c) {
        line0Baseline = getLineBounds(0, line0bounds);

        super.onDraw(c);

        // Now we can calculate what we need!
        int xSuffix = (int) textPaint.measureText(left.getText() + getText().toString()) + getPaddingLeft();

        // We need to draw this like this because
        // setting a right drawable doesn't work properly and we want this
        // just after the text we are editing (but untouchable)
        c.drawText(suffix, xSuffix, line0bounds.bottom, textPaint);

    }

    // This is for the prefix.
    // It is a drawable for rendering text
    private class TagDrawable extends Drawable {

        private String text = "";

        public void setText(String s) {
            text = s;

            // Tell it we need to be as big as we want to be!
            setBounds(0, 0, getIntrinsicWidth(), getIntrinsicHeight());

            invalidateSelf();
        }

        public String getText() {
            return text;
        }

        @Override
        public void draw(Canvas canvas) {
            // I don't know why this y works here, but it does :)
            // (aka if you are from Google/are Jake Wharton and I have done it wrong, please tell me!)
            canvas.drawText(text, 0.0f, (float) line0Baseline + canvas.getClipBounds().top, textPaint);
        }

        @Override
        public void setAlpha(int i) {
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }

        @Override
        public int getIntrinsicHeight() {
            return (int) fontHeight;
        }

        @Override
        public int getIntrinsicWidth() {
            return (int) textPaint.measureText(text);
        }
    }

}