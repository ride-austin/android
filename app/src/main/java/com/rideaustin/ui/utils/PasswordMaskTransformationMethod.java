package com.rideaustin.ui.utils;

import android.text.method.PasswordTransformationMethod;
import android.view.View;

/**
 * Created by rideclientandroid on 29.08.2016.
 */
public class PasswordMaskTransformationMethod extends PasswordTransformationMethod {
    private char password = '*';
    private char placeholder;

    public PasswordMaskTransformationMethod(char password, char placeholder) {
        this.password = password;
        this.placeholder = placeholder;
    }


    @Override
    public CharSequence getTransformation(CharSequence source, View view) {
        return new PasswordCharSequence(source);
    }

    class PasswordCharSequence implements CharSequence {
        private CharSequence mSource;

        //XXX-XX-XXX
        public PasswordCharSequence(CharSequence source) {
            mSource = source; // Store char sequence
        }

        public char charAt(int index) {
            if (mSource.charAt(index) == placeholder) {
                return placeholder;
            }
            return password;
        }

        public int length() {
            return mSource.length(); // Return default
        }

        public CharSequence subSequence(int start, int end) {
            return mSource.subSequence(start, end); // Return default
        }
    }
}
