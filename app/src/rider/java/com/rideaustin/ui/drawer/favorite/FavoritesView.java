package com.rideaustin.ui.drawer.favorite;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.ui.common.BaseView;

/**
 * Created by crossover on 04/07/2017.
 */

public interface FavoritesView extends BaseView {
    void moveCamera(LatLng latLng);
    void setEditableInput(boolean editable);
}
