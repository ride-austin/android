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
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.rideaustin.R;
import com.rideaustin.ui.utils.phone.Country;

public class CountryAdapter extends ArrayAdapter<Country> {

    public CountryAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        com.rideaustin.databinding.ItemCountryDropBinding binding;
        if (convertView == null) {
            binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_country_drop, parent, false);
        } else {
            binding = DataBindingUtil.findBinding(convertView);
        }
        Country country = getItem(position);
        binding.countryName.setText(country.getName());
        binding.countryCode.setText(country.getCountryCodeStr());
        binding.image.setImageResource(country.getResId());
        return binding.getRoot();
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        com.rideaustin.databinding.ItemCountryBinding binding;
        if (convertView == null) {
            binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_country, parent, false);
        } else {
            binding = DataBindingUtil.findBinding(convertView);
        }

        Country country = getItem(position);
        binding.image.setImageResource(country.getResId());
        return binding.getRoot();
    }
}
