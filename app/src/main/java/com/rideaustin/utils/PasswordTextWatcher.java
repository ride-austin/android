package com.rideaustin.utils;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.rideaustin.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ysych on 7/25/16.
 */
public class PasswordTextWatcher implements TextWatcher {

    private EditText editText;
    private Context context;

    public PasswordTextWatcher(EditText editText, Context context) {
        this.editText = editText;
        this.context = context;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if(editText.getText().length() > 0){
            Pattern pattern = Pattern.compile("[\\s]");
            Matcher matcher = pattern.matcher(editText.getText().toString());
            if(matcher.find()){
                editText.setError(context.getString(R.string.pass_spaces));
                editText.setText(editText.getText().toString().replaceAll("[\\s]", ""));
                editText.setSelection(editText.getText().length());
            }
        }
    }
}
