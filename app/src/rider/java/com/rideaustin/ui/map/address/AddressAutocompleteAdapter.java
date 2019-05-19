package com.rideaustin.ui.map.address;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLngBounds;
import com.rideaustin.R;
import com.rideaustin.databinding.ViewAddressSelectionItemBinding;
import com.rideaustin.ui.map.PlaceAutoCompleteAdapter;

import java8.util.Optional;

/**
 * @author sdelaysam.
 */

public class AddressAutocompleteAdapter extends PlaceAutoCompleteAdapter
        implements AdapterView.OnItemClickListener {

    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);
    private final int TYPE_PREDICTION = 0;
    private final int TYPE_CUSTOM_MAP = 1;

    private final LayoutInflater inflater;
    private final AddressListener listener;

    public AddressAutocompleteAdapter(Context context,
                                      GoogleApiClient googleApiClient,
                                      @Nullable LatLngBounds bounds,
                                      AddressListener listener) {
        super(context, googleApiClient, bounds);
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position < super.getCount() ? TYPE_PREDICTION : TYPE_CUSTOM_MAP;
    }


    @Override
    public AutocompletePrediction getItem(int position) {
        if (position < super.getCount()) {
            return super.getItem(position);
        }
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewAddressSelectionItemBinding binding = getBinding(convertView, parent);
        if (getItemViewType(position) == TYPE_PREDICTION) {
            AutocompletePrediction item = getItem(position);
            boolean isTransit = item != null
                    && item.getPlaceTypes() != null
                    && item.getPlaceTypes().contains(Place.TYPE_TRANSIT_STATION);
            binding.icon.setImageResource(isTransit ? R.drawable.ic_baseline_directions_transit_24px : R.drawable.ic_place_black);
            binding.icon.setColorFilter(ContextCompat.getColor(getContext(), R.color.light_icon_tint));
            if (item != null) {
                binding.name.setText(item.getPrimaryText(STYLE_BOLD));
                binding.address.setText(item.getSecondaryText(STYLE_BOLD));
                binding.address.setVisibility(View.VISIBLE);
            } else {
                binding.name.setText("");
                binding.address.setText("");
            }
        } else {
            binding.icon.setImageResource(R.drawable.ic_pin_drop_black);
            binding.icon.setColorFilter(ContextCompat.getColor(getContext(), R.color.light_icon_tint));
            binding.name.setText(R.string.address_select_on_map);
            binding.address.setVisibility(View.GONE);
        }
        binding.executePendingBindings();
        return binding.getRoot();
    }

    private ViewAddressSelectionItemBinding getBinding(View convertView, ViewGroup parent) {
        ViewAddressSelectionItemBinding binding = null;
        if (convertView != null) {
            binding = DataBindingUtil.findBinding(convertView);
        }
        if (binding == null) {
            binding = DataBindingUtil.inflate(inflater,
                    R.layout.view_address_selection_item, parent, false);
        }
        return binding;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (getItemViewType(position) == TYPE_PREDICTION) {
            final AutocompletePrediction item = getItem(position);
            if (item != null) {
                listener.onAddressSelected(item);
            }
        } else {
            listener.onAddressSelected(AddressType.MAP, Optional.empty());
        }
    }
}
