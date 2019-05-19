package com.rideaustin.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

/**
 * Created by ysych on 7/27/16.
 */
public class MobilePhoneTextWatcher implements TextWatcher{

    private EditText editText;
    private boolean isDelete;

    public MobilePhoneTextWatcher(EditText editText) {
        this.editText = editText;
        this.editText.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    isDelete = true;
                }
                return false;
            }
        });
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
    }

    public void afterTextChanged(Editable s) {

        if (isDelete) {
            isDelete = false;
            return;
        }
        String val = s.toString();
        String a = "";
        String b = "";
        String c = "";
        if (val.length() > 0) {
            val = val.replace("(", "");
            val = val.replace(")", "");
            val = val.replace(" ", "");
            val = val.replace("-", "");
            if (val.length() >= 3) {
                a = val.substring(0, 3);
            } else if (val.length() < 3) {
                a = val.substring(0, val.length());
            }
            if (val.length() >= 6) {
                b = val.substring(3, 6);
                c = val.substring(6, val.length());
            } else if (val.length() > 3 && val.length() < 6) {
                b = val.substring(3, val.length());
            }
            StringBuilder stringBuffer = new StringBuilder();
            if (a.length() > 0) {
                stringBuffer.append("(");
                stringBuffer.append(a);
                if (a.length() == 3) {
                    stringBuffer.append(")");
                    stringBuffer.append(" ");
                }
            }
            if (b.length() > 0) {
                stringBuffer.append(b);
                if (b.length() == 3) {
                    stringBuffer.append("-");
                }
            }
            if (c.length() > 0) {
                stringBuffer.append(c);
            }
            editText.removeTextChangedListener(this);
            editText.setText(stringBuffer.toString());
            editText.setSelection(editText.getText().toString().length());
            editText.addTextChangedListener(this);
        } else {
            editText.removeTextChangedListener(this);
            editText.setText("");
            editText.addTextChangedListener(this);
        }

    }
}
