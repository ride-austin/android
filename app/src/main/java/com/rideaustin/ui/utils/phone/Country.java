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
import android.support.annotation.DrawableRes;

import static com.rideaustin.ui.utils.phone.PhoneInputUtil.PLUS_SIGN;

public class Country {

    /* Example format:
     *   Cameroon,cm,237
     *   Canada,ca,1,1
     */
    private static final int INDEX_NAME = 0;
    private static final int INDEX_COUNTRY_ISO = 1;
    private static final int INDEX_COUNTRY_CODE = 2;
    private static final int INDEX_PRIORITY = 3;
    private static final String SEPARATOR = ",";

    private String name;
    private String countryISO;
    private int countryCode;
    private String countryCodeText;
    private int priority;
    @DrawableRes
    private int resId;
    /**
     * This is the line number starting from 0 on countries.dat file
     */
    private int index;

    public Country(Context context, String str, int index) {
        String[] data = str.split(SEPARATOR);
        this.index = index;
        name = data[INDEX_NAME];
        countryISO = data[INDEX_COUNTRY_ISO];
        countryCode = Integer.parseInt(data[INDEX_COUNTRY_CODE]);
        countryCodeText = PLUS_SIGN + data[INDEX_COUNTRY_CODE];
        if (data.length > 3) {
            priority = Integer.parseInt(data[INDEX_PRIORITY]);
        }
        String fileName = String.format("f%03d", index);
        resId = context.getApplicationContext().getResources().getIdentifier(fileName, "drawable", context.getApplicationContext().getPackageName());
    }

    public String getName() {
        return name;
    }

    public String getCountryISO() {
        return countryISO;
    }

    public int getCountryCode() {
        return countryCode;
    }

    public String getCountryCodeStr() {
        return countryCodeText;
    }

    public int getPriority() {
        return priority;
    }

    public int getResId() {
        return resId;
    }

    public int getIndex() {
        return index;
    }
}
