package com.rideaustin.stub;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by Viktor Kifer
 * On 28-Dec-2016.
 */

public class SimpleTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence text, int start, int count, int after) {
        // should be empty
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int before, int count) {
        // should be empty
    }

    @Override
    public void afterTextChanged(Editable editable) {
        // should be empty
    }
}
