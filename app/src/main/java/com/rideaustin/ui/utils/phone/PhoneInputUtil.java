package com.rideaustin.ui.utils.phone;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.rideaustin.schedulers.RxSchedulers;

import java.util.ArrayList;
import java.util.TreeSet;

import timber.log.Timber;

/**
 * Created by crossover on 18/10/2016.
 */

public class PhoneInputUtil {

    public static final String PLUS_SIGN = "+";

    private static final int US_COUNTRY_CODE = 1;
    private static final int CANADA_CODE_LENGTH = 3;

    private static final TreeSet<String> CANADA_CODES = new TreeSet<>();

    static {
        CANADA_CODES.add("204");
        CANADA_CODES.add("236");
        CANADA_CODES.add("249");
        CANADA_CODES.add("250");
        CANADA_CODES.add("289");
        CANADA_CODES.add("306");
        CANADA_CODES.add("343");
        CANADA_CODES.add("365");
        CANADA_CODES.add("387");
        CANADA_CODES.add("403");
        CANADA_CODES.add("416");
        CANADA_CODES.add("418");
        CANADA_CODES.add("431");
        CANADA_CODES.add("437");
        CANADA_CODES.add("438");
        CANADA_CODES.add("450");
        CANADA_CODES.add("506");
        CANADA_CODES.add("514");
        CANADA_CODES.add("519");
        CANADA_CODES.add("548");
        CANADA_CODES.add("579");
        CANADA_CODES.add("581");
        CANADA_CODES.add("587");
        CANADA_CODES.add("604");
        CANADA_CODES.add("613");
        CANADA_CODES.add("639");
        CANADA_CODES.add("647");
        CANADA_CODES.add("672");
        CANADA_CODES.add("705");
        CANADA_CODES.add("709");
        CANADA_CODES.add("742");
        CANADA_CODES.add("778");
        CANADA_CODES.add("780");
        CANADA_CODES.add("782");
        CANADA_CODES.add("807");
        CANADA_CODES.add("819");
        CANADA_CODES.add("825");
        CANADA_CODES.add("867");
        CANADA_CODES.add("873");
        CANADA_CODES.add("902");
        CANADA_CODES.add("905");
    }

    private CountryDataProvider countryProvider;

    private PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
    private Spinner spinner;

    private String lastEnteredPhone;
    private EditText phoneEdit;
    private CountryAdapter countryAdapter;

    public PhoneInputUtil(final Context context, final Spinner spinner, EditText phoneEdit) {
        this.spinner = spinner;
        this.phoneEdit = phoneEdit;
        spinner.setOnItemSelectedListener(onItemSelectedListener);

        countryAdapter = new CountryAdapter(context);

        spinner.setAdapter(countryAdapter);

        phoneEdit.addTextChangedListener(new CustomPhoneNumberFormattingTextWatcher(onPhoneChangedListener));

        InputFilter filter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (dstart > 0 && !Character.isDigit(c)) {
                    return "";
                }
            }
            return null;
        };

        phoneEdit.setFilters(new InputFilter[]{filter});

        spinner.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboard(v);
            }
            return false;
        });
        countryProvider = new CountryDataProvider(context);
        countryProvider.loadCountries()
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(countries -> {
                    countryAdapter.addAll(countries);
                    //Sometimes loading precedes editText.setText(phone) and sometimes not.
                    //If have a text then go recall onPhoneChanged just to reload country spinner
                    if (TextUtils.isEmpty(lastEnteredPhone)) {
                        //Otherwise set default/calculated country to spinner.
                        if (countryProvider.getSelection() >= 0) {
                            spinner.setSelection(countryProvider.getSelection());
                        }
                    } else {
                        onPhoneChangedListener.onPhoneChanged(lastEnteredPhone);
                    }
                });

    }

    public void resetCountryCode() {
        Country country = (Country) spinner.getSelectedItem();
        phoneEdit.getText().clear();
        phoneEdit.getText().insert(phoneEdit.getText().length() > 0 ? 1 : 0, String.valueOf(country.getCountryCode()));
        phoneEdit.setSelection(phoneEdit.length());
    }

    private AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Country c = (Country) spinner.getItemAtPosition(position);
            if (lastEnteredPhone != null && lastEnteredPhone.startsWith(c.getCountryCodeStr())) {
                return;
            }
            phoneEdit.getText().clear();
            phoneEdit.getText().insert(phoneEdit.getText().length() > 0 ? 1 : 0, String.valueOf(c.getCountryCode()));
            phoneEdit.setSelection(phoneEdit.length());
            lastEnteredPhone = null;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    @Nullable
    private Pair<Integer, ArrayList<Country>> findCountryByPhoneNumber(String phoneNo) {
        String countryCodeStr = phoneNo.replaceAll("[^0-9]", "");
        countryCodeStr = countryCodeStr.substring(0, Math.min(countryCodeStr.length(), 4));
        while (!countryCodeStr.isEmpty()) {
            Integer countryCode = Integer.parseInt(countryCodeStr);
            ArrayList<Country> list = countryProvider.getCountriesMap().get(countryCode);
            if (list == null) {
                countryCodeStr = countryCodeStr.substring(0, countryCodeStr.length() - 1);
                continue;
            }

            return new Pair<>(countryCode, list);
        }
        return null;
    }

    private OnPhoneChangedListener onPhoneChangedListener = new OnPhoneChangedListener() {
        @Override
        public void onPhoneChanged(String phone) {
            try {
                lastEnteredPhone = phone;
                Pair<Integer, ArrayList<Country>> match = findCountryByPhoneNumber(phone);
                if (match == null) {
                    return;
                }

                int countryCode = match.first;

                Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(phone, null);
                String num = String.valueOf(phoneNumber.getNationalNumber());
                Country foundCountry = null;
                if (countryCode == US_COUNTRY_CODE && num.length() >= CANADA_CODE_LENGTH) {
                    String code = num.substring(0, CANADA_CODE_LENGTH);
                    if (CANADA_CODES.contains(code)) {
                        for (Country country : match.second) {
                            // Canada has priority 1, US has priority 0
                            if (country.getPriority() == 1) {
                                foundCountry = country;
                                break;
                            }
                        }
                    }
                }
                if (foundCountry == null) {
                    for (Country country : match.second) {
                        if (country.getPriority() == 0) {
                            foundCountry = country;
                            break;
                        }
                    }
                }
                if (foundCountry != null) {
                    final int position = foundCountry.getIndex();
                    RxSchedulers.schedule(() -> spinner.setSelection(position));
                }
            } catch (NumberParseException ignore) {
                Timber.e(ignore, "NumberParseException: " + phone);
            }
        }
    };

    @Nullable
    public String validate() {
        String region = null;
        String phone = null;
        if (lastEnteredPhone != null) {
            try {
                Phonenumber.PhoneNumber p = phoneNumberUtil.parse(lastEnteredPhone, null);
                phone = PLUS_SIGN + p.getCountryCode() + p.getNationalNumber();
                region = phoneNumberUtil.getRegionCodeForNumber(p);
            } catch (NumberParseException ignore) {
                Timber.e(ignore, "NumberParseException: " + lastEnteredPhone);
            }
        }
        if (region != null) {
            return phone;
        } else {
            return null;
        }
    }

    void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
