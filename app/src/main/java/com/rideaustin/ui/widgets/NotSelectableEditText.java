package com.rideaustin.ui.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by rideclientandroid on 29.08.2016.
 */
public class NotSelectableEditText extends EditText {

    public NotSelectableEditText(Context context) {
        super(context);
    }

    public NotSelectableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NotSelectableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public NotSelectableEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onSelectionChanged(int start, int end) {

        CharSequence text = getText();
        if (text != null) {
            if (start != text.length() || end != text.length()) {
                setSelection(text.length(), text.length());
                return;
            }
        }

        super.onSelectionChanged(start, end);
    }

}