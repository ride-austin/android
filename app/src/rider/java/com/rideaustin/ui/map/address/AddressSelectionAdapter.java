package com.rideaustin.ui.map.address;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.databinding.ViewAddressSelectionItemBinding;
import com.rideaustin.entities.GeoPosition;
import com.rideaustin.ui.map.history.PlaceHistory;
import com.rideaustin.utils.RecentPlacesHelper;

import java.util.List;

import java8.util.Optional;

/**
 * @author sdelaysam.
 */

public class AddressSelectionAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

    private final int TYPE_ITEM = 0;
    private final int TYPE_DELIMITER = 1;

    private final Context context;
    private final LayoutInflater inflater;
    private final AddressListener listener;

    private Optional<GeoPosition> home = App.getPrefs().getHome();
    private Optional<GeoPosition> work = App.getPrefs().getWork();
    private List<PlaceHistory> recents = RecentPlacesHelper.getPlaces();
    private int count;


    public AddressSelectionAdapter(Context context, AddressListener listener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
        count = getTotalCount();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0 :
                listener.onAddressSelected(AddressType.HOME, home);
                break;
            case 1 :
                listener.onAddressSelected(AddressType.WORK, work);
                break;
            default:
                int recentPosition = getRecentPosition(position);
                if (recentPosition >= 0 && recentPosition < recents.size()) {
                    listener.onAddressSelected(AddressType.RECENT,
                            Optional.ofNullable(recents.get(recentPosition))
                                    .map(PlaceHistory::getGeoPosition));
                } else {
                    listener.onAddressSelected(AddressType.MAP, Optional.empty());
                }
        }
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Object getItem(int position) {
        switch (position) {
            case 0: return home.orElse(null);
            case 1: return work.orElse(null);
            case 2: return null; // delimiter
            default:
                int recentPosition = getRecentPosition(position);
                if (recentPosition >= 0 && recentPosition < recents.size()) {
                    return recents.get(recentPosition);
                }
                return null; // set on map
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);
        if (viewType == TYPE_DELIMITER) {
            return getDelimiterView(convertView, parent);
        } else {
            return getAddressView(position, convertView, parent);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 2 ? TYPE_DELIMITER : TYPE_ITEM;
    }

    private View getDelimiterView(View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.view_address_delimiter_item, parent, false);
        }
        return convertView;
    }

    private View getAddressView(int position, View convertView, ViewGroup parent) {
        ViewAddressSelectionItemBinding binding = getBinding(convertView, parent);
        switch (position) {
            case 0 :
                applyHome(binding);
                break;
            case 1:
                applyWork(binding);
                break;
            default:
                int recentPosition = getRecentPosition(position);
                if (recentPosition >= 0 && recentPosition < recents.size()) {
                    applyRecent(binding, recentPosition);
                } else {
                    applyShowOnMap(binding);
                }
        }
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


    private void applyHome(ViewAddressSelectionItemBinding binding) {
        if (home.isPresent()) {
            GeoPosition address = home.get();
            apply(binding, AddressType.HOME, address.getPlaceName(), address.getAddressLine());
        } else {
            String name = context.getString(R.string.address_add_home);
            apply(binding, AddressType.HOME, name, null);
        }
    }

    private void applyWork(ViewAddressSelectionItemBinding binding) {
        if (work.isPresent()) {
            GeoPosition address = work.get();
            apply(binding, AddressType.WORK, address.getPlaceName(), address.getAddressLine());
        } else {
            String name = context.getString(R.string.address_add_work);
            apply(binding, AddressType.WORK, name, null);
        }
    }

    private void applyRecent(ViewAddressSelectionItemBinding binding, int position) {
        Optional.ofNullable(recents.get(position))
                .map(PlaceHistory::getGeoPosition)
                .ifPresentOrElse(a -> {
                    apply(binding, AddressType.RECENT, a.getPlaceName(), a.getFullAddress());
                }, () -> {
                    // should never happen
                    apply(binding, AddressType.RECENT, "", "");
                });
    }

    private void applyShowOnMap(ViewAddressSelectionItemBinding binding) {
        String name = context.getString(R.string.address_select_on_map);
        apply(binding, AddressType.MAP, name, null);
    }

    private void apply(ViewAddressSelectionItemBinding binding, AddressType type, String name, String address) {
        binding.icon.setImageResource(getIconRes(type));
        binding.icon.setColorFilter(getIconColor(type));
        binding.name.setText(name);
        boolean addressEmpty = address == null || address.isEmpty();
        binding.address.setVisibility(addressEmpty ? View.GONE : View.VISIBLE);
        if (!addressEmpty) {
            binding.address.setText(address);
        }
        binding.executePendingBindings();
    }

    @DrawableRes
    private int getIconRes(AddressType type) {
        switch (type) {
            case HOME: return R.drawable.ic_home_black;
            case WORK: return R.drawable.ic_work_black;
            case RECENT: return R.drawable.ic_history_black;
            case MAP: return R.drawable.ic_pin_drop_black;
            case PREDICTION: return R.drawable.ic_place_black;
            default: return 0;
        }
    }

    private int getIconColor(AddressType type) {
        switch (type) {
            case RECENT:
            case PREDICTION:
            case MAP:
                return ContextCompat.getColor(context, R.color.light_icon_tint);
            default:
                return 0;
        }
    }

    private int getTotalCount() {
        // 2 - favorites count
        // 1 - delimiter
        // 1 - select on map
        return recents.size() + 2 + 1 + 1;
    }

    private int getRecentPosition(int position) {
        // 0, 1 - home, work
        // 2 - delimiter
        return position - 2 - 1;
    }

}
