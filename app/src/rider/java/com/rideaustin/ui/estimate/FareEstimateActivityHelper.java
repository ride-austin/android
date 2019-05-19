package com.rideaustin.ui.estimate;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.ui.map.MapViewModel;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.toast.RAToast;

/**
 * Created by hatak on 2/2/17.
 */

public class FareEstimateActivityHelper {

    public static void startFareEstimateActivity(final Context context, MapViewModel mapViewModel){
        Intent intent = new Intent(context, FareEstimateActivity.class);
        if (mapViewModel.isPickAnDestinationEntered()) {
            intent.putExtra(Constants.START_ADDRESS, mapViewModel.getStartAddress());
            intent.putExtra(Constants.DESTINATION_ADDRESS, mapViewModel.getDestinationAddress());
            boolean isInSurge = App.getDataManager().isSurge(App.getDataManager().getRequestedCarType().getCarCategory(), mapViewModel.getPickupLocation());
            intent.putExtra(Constants.SURGE_AREA, isInSurge);
            context.startActivity(intent);
        }else{
            RAToast.show(R.string.missing_addresses_fare_estimate_msg, Toast.LENGTH_SHORT);
        }
    }

}
