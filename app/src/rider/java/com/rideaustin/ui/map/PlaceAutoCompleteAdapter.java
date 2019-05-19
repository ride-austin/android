package com.rideaustin.ui.map;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.QueryHint;
import com.rideaustin.utils.ListUtils;
import com.rideaustin.utils.NetworkHelper;
import com.rideaustin.utils.location.LocationHelper;
import com.rideaustin.utils.toast.RAToast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

/**
 * Created by yshloma on 23.06.2016.
 */
public class PlaceAutoCompleteAdapter extends ArrayAdapter<AutocompletePrediction> implements Filterable {

    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    @LayoutRes
    private static final int RESOURCE_ID = android.R.layout.simple_expandable_list_item_2;

    /**
     * Current results returned by this adapter.
     */
    private ArrayList<AutocompletePrediction> resultList;

    /**
     * Handles autocomplete requests.
     */
    private GoogleApiClient apiClient;

    /**
     * The bounds used for Places Geo Data autocomplete API requests.
     */
    @Nullable
    private LatLngBounds bounds;

    /**
     * The autocomplete filter used to restrict queries to a specific set of place types.
     */
    private AutocompleteFilter placeFilter;

    /**
     * Layout inflater used to inflate rows from null data
     */
    private LayoutInflater inflater;

    private Filter filter;

    public PlaceAutoCompleteAdapter(Context context, GoogleApiClient googleApiClient) {
        this(context, googleApiClient, null);
    }

    public PlaceAutoCompleteAdapter(Context context, GoogleApiClient googleApiClient, @Nullable LatLngBounds bounds) {
        super(context, RESOURCE_ID, android.R.id.text1);
        inflater = LayoutInflater.from(context);
        apiClient = googleApiClient;
        this.bounds = bounds;
        placeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_NONE)
                .build();
    }

    /**
     * Sets the bounds for all subsequent queries.
     */
    public void setBounds(LatLngBounds bounds) {
        this.bounds = bounds;
        notifyDataSetChanged();
    }

    /**
     * Returns the number of results received in the last autocomplete query.
     */
    @Override
    public int getCount() {
        return resultList != null ? resultList.size() : 0;
    }

    /**
     * Returns an item from the last autocomplete query.
     */
    @Override
    public AutocompletePrediction getItem(int position) {
        // RA-8911: might be caused by concurrency issues in 2.8
        // In general, position should always be less than getCount()
        // Add this check just to be safe
        if (position < getCount()) {
            return resultList.get(position);
        }
        Timber.e("Can't find auto prediction at position=" + position + ", total=" + getCount());
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AutocompletePrediction item = getItem(position);

        // RA-8913: method should always return non-null view
        // Input data should not contain null elements as we filter them out
        // But to be sure, handle all situations
        if (convertView == null) {
            if (item != null) {
                // if item at position is not null, we may use super call
                convertView = super.getView(position, convertView, parent);
            } else {
                // otherwise, super call would crash, need to inflate manually
                convertView = inflater.inflate(RESOURCE_ID, parent, false);
            }
        }

        // Sets the primary and secondary text for a row.
        // Note that getPrimaryText() and getSecondaryText() return a CharSequence that may contain
        // styling based on the given CharacterStyle.

        TextView textView1 = (TextView) convertView.findViewById(android.R.id.text1);
        TextView textView2 = (TextView) convertView.findViewById(android.R.id.text2);
        if (item != null) {
            textView1.setText(item.getPrimaryText(STYLE_BOLD));
            textView2.setText(item.getSecondaryText(STYLE_BOLD));
        } else {
            textView1.setText("");
            textView2.setText("");
        }

        return convertView;
    }

    /**
     * Returns the filter for the current set of autocomplete results.
     */
    @Override
    @NonNull
    public Filter getFilter() {
        if (filter == null) {
            filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    // Skip the autocomplete query if no constraints are given.
                    if (constraint != null) {
                        // Query the autocomplete API for the (constraint) search string.
                        List<AutocompletePrediction> predictions = getAutocomplete(constraint);
                        if (predictions != null) {
                            // remove all nulls from result
                            ListUtils.removeNullElements(predictions);
                            // move important predictions to the top
                            applyCustomPredictions(constraint, predictions);
                            // publish results
                            results.values = predictions;
                            results.count = predictions.size();
                        }
                    }
                    return results;
                }

                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && isValidList(results)) {
                        resultList = (ArrayList<AutocompletePrediction>) results.values;
                        // The API returned at least one result, update the data.
                        notifyDataSetChanged();
                    } else {
                        resultList = null;
                        // The API did not return any results, invalidate the data set.
                        notifyDataSetInvalidated();
                    }
                }

                @Override
                public CharSequence convertResultToString(Object resultValue) {
                    // Override this method to display a readable result in the AutocompleteTextView
                    // when clicked.
                    if (resultValue instanceof AutocompletePrediction) {
                        AutocompletePrediction autoPrediction = ((AutocompletePrediction) resultValue);
                        return autoPrediction.getPrimaryText(null);
                    } else {
                        return super.convertResultToString(resultValue);
                    }
                }

                private boolean isValidList(FilterResults results) {
                    return results.count > 0 && results.values instanceof ArrayList
                            && !((ArrayList<?>) results.values).isEmpty()
                            && ((ArrayList<?>) results.values).get(0) instanceof AutocompletePrediction;
                }

            };
        }
        return filter;
    }

    private void applyCustomPredictions(@Nullable CharSequence constraint, @NonNull List<AutocompletePrediction> resultList) {
        if (TextUtils.isEmpty(constraint)) {
            return;
        }

        String userInput = constraint.toString().toLowerCase();

        List<QueryHint> hints = App.getConfigurationManager().getLastConfiguration().getGeocodingConfiguration().getQueryHints();
        hints = new ArrayList<>(hints);
        Collections.reverse(hints);
        for (QueryHint hint : hints) {
            boolean applicable = false;

            for (String prefix : hint.getPrefixes()) {
                if (userInput.startsWith(prefix)) {
                    applicable = true;
                    break;
                }
            }

            if (!applicable) {
                for (String word : hint.getContains()) {
                    if (userInput.contains(word)) {
                        applicable = true;
                        break;
                    }
                }
            }

            if (!applicable) {
                // hint is not applicable for given query
                continue;
            }

            addHint(resultList, hint);
        }
    }

    private int getHintPosition(List<AutocompletePrediction> resultList, QueryHint hint) {
        int airportIndex = -1;
        for (int i = 0; i < resultList.size(); i++) {
            CharSequence fullTextCharseq = resultList.get(i).getFullText(null);
            if (!TextUtils.isEmpty(fullTextCharseq)) {
                String fullText = fullTextCharseq.toString();
                if (fullText.contains(hint.getPrimaryAddress())) {
                    airportIndex = i;
                    break;
                }
            }
        }
        return airportIndex;
    }

    private void addHint(List<AutocompletePrediction> resultList, QueryHint hint) {
        int position = getHintPosition(resultList, hint);
        if (position >= 0) {
            resultList.remove(position);
        }
        resultList.add(0, fromHint(hint));
    }

    private AutocompletePrediction fromHint(QueryHint hint) {
        return new AutocompletePrediction() {
            @Override
            public CharSequence getFullText(@Nullable CharacterStyle characterStyle) {
                return hint.getDescription();
            }

            @Override
            public CharSequence getPrimaryText(@Nullable CharacterStyle characterStyle) {
                return hint.getPrimaryAddress();
            }

            @Override
            public CharSequence getSecondaryText(@Nullable CharacterStyle characterStyle) {
                return hint.getSecondaryAddress();
            }

            @Nullable
            @Override
            public String getPlaceId() {
                return hint.getReference();
            }

            @Nullable
            @Override
            public List<Integer> getPlaceTypes() {
                ArrayList<Integer> placeTypes = new ArrayList<>();
                placeTypes.add(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT);
                return placeTypes;
            }

            @Override
            public AutocompletePrediction freeze() {
                return this;
            }

            @Override
            public boolean isDataValid() {
                return true;
            }

            @Override
            public String toString() {
                return "AutocompletePrediction{" +
                        "description='" + hint.getDescription() + '\'' +
                        ", primaryAddress='" + hint.getPrimaryAddress() + '\'' +
                        ", secondaryAddress='" + hint.getSecondaryAddress() + '\'' +
                        '}';
            }


        };
    }

    /**
     * Submits an autocomplete query to the Places Geo Data Autocomplete API.
     * Results are returned as frozen AutocompletePrediction objects, ready to be cached.
     * objects to store the Place ID and description that the API returns.
     * Returns an empty list if no results were found.
     * Returns null if the API client is not available or the query did not complete
     * successfully.
     * This method MUST be called off the main UI thread, as it will block until data is returned
     * from the API, which may include a network request.
     *
     * @param constraint Autocomplete query string
     * @return Results from the autocomplete API or null if the query was not successful.
     * @see Places#GEO_DATA_API#getAutocomplete(CharSequence)
     * @see AutocompletePrediction#freeze()
     */
    @Nullable
    private List<AutocompletePrediction> getAutocomplete(CharSequence constraint) {
        List<AutocompletePrediction> predictions = null;
        if (!NetworkHelper.isNetworkAvailable()) {
            RAToast.show(R.string.network_error, Toast.LENGTH_SHORT);
        } else if (apiClient.isConnected()) {
            try {
                predictions = LocationHelper.getAutocompletePredictions(apiClient, constraint.toString(), bounds, placeFilter)
                        .toBlocking().first();
            } catch (Exception e) {
                RAToast.show(R.string.error_places_autocomplete, Toast.LENGTH_SHORT);
            }
        }
        return predictions;
    }
}
