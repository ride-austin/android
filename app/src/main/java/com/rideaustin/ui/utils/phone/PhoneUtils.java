/*
 * Copyright (c) 2014-2015 Amberfog.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rideaustin.ui.utils.phone;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;

import timber.log.Timber;

public class PhoneUtils {

    /**
     * @param context
     * @return Country if found or defaulted to US
     */
    @NonNull
    public static String getCountryRegionFromPhone(Context context) {
        TelephonyManager service = null;
        int res = context.checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE");
        if (res == PackageManager.PERMISSION_GRANTED) {
            service = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        }
        String code = null;
        if (service != null) {
            String line1Number = service.getLine1Number();
            if (!TextUtils.isEmpty(line1Number) && !line1Number.matches("^0*$")) {
                code = parseNumber(line1Number);
            }
        }
        if (TextUtils.isEmpty(code)) {
            if (service != null) {
                code = service.getNetworkCountryIso();
            }
            if (TextUtils.isEmpty(code)) {
                code = context.getResources().getConfiguration().locale.getCountry();
            }
        }
        if (!TextUtils.isEmpty(code)) {
            return code.toUpperCase();
        }
        //Defaulted to US
        return Locale.US.getCountry().toUpperCase();
    }

    @Nullable
    private static String parseNumber(String paramString) {
        if (paramString == null) {
            return null;
        }
        PhoneNumberUtil numberUtil = PhoneNumberUtil.getInstance();
        String result;
        try {
            Phonenumber.PhoneNumber localPhoneNumber = numberUtil.parse(paramString, null);
            result = numberUtil.getRegionCodeForNumber(localPhoneNumber);
            if (result == null) {
                return null;
            }
        } catch (NumberParseException exception) {
            Timber.e(exception, "Cannot parse local number %s", paramString);
            return null;
        }
        return result;
    }
}