package com.rideaustin.ui.utils.phone;

import android.content.Context;
import android.util.SparseArray;

import com.google.i18n.phonenumbers.PhoneNumberUtil;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by crossover on 19/10/2016.
 */

public class CountryDataProvider {
    private SparseArray<ArrayList<Country>> countriesMap = new SparseArray<>();

    private static final int UNDEFINED_SELECTION = -1;

    private final static String FILE_PATH = "countries.dat";
    private final Context context;
    private int initialSelection = UNDEFINED_SELECTION;

    public CountryDataProvider(Context context) {
        this.context = context;
    }

    public SparseArray<ArrayList<Country>> getCountriesMap() {
        return countriesMap;
    }

    public Observable<List<Country>> loadCountries() {
        return Observable.fromCallable(() -> {
            List<Country> result = new ArrayList<>();
            countriesMap = parseCountries(result);
            initialSelection = calculateInitialSelection();
            return result;
        });
    }

    private int calculateInitialSelection() {
        String countryRegion = PhoneUtils.getCountryRegionFromPhone(context);

        int code = PhoneNumberUtil.getInstance().getCountryCodeForRegion(countryRegion);
        ArrayList<Country> list = countriesMap.get(code);
        if (list != null) {
            for (Country country : list) {
                if (country.getPriority() == 0) {
                    return country.getIndex();
                }
            }
        }
        return UNDEFINED_SELECTION;
    }

    private SparseArray<ArrayList<Country>> parseCountries(List<Country> outList) throws IOException {
        SparseArray<ArrayList<Country>> data = new SparseArray<>();
        BufferedReader reader = null;
        InputStreamReader streamReader = null;
        try {
            streamReader = new InputStreamReader(context.getApplicationContext().getAssets().open(FILE_PATH), "UTF-8");
            reader = new BufferedReader(streamReader);

            // do reading, usually loop until end of file reading
            String line;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                //process line
                Country country = new Country(context, line, i);
                outList.add(country);
                ArrayList<Country> list = data.get(country.getCountryCode());
                if (list == null) {
                    list = new ArrayList<>();
                    data.put(country.getCountryCode(), list);
                }
                list.add(country);
                i++;
            }
            return data;
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(streamReader);
        }
    }

    public int getSelection() {
        return initialSelection;
    }
}
