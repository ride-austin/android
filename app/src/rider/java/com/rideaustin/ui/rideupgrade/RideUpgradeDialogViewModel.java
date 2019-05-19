package com.rideaustin.ui.rideupgrade;

import android.databinding.ObservableField;
import android.view.View;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.UpgradeRequest;
import com.rideaustin.ui.utils.UIUtils;

/**
 * Created by hatak on 14.06.2017.
 */

public class RideUpgradeDialogViewModel {

    private ObservableField<String> priorityFareFactor = new ObservableField<>();
    private ObservableField<Integer> priorityFareVisibility = new ObservableField<>(View.GONE);
    private ObservableField<String> title = new ObservableField<>("Regular");
    private ObservableField<String> content = new ObservableField<>("SUV");

    public RideUpgradeDialogViewModel(UpgradeRequest request) {
        title.set(App.getInstance().getString(R.string.upgrade_ride_title, request.getTarget().toUpperCase()));
        content.set(App.getInstance().getString(R.string.upgrade_ride_content, request.getSource(), request.getTarget().toUpperCase()));
        if (request.getSurgeFactor() != null && request.getSurgeFactor() > 1) {
            priorityFareFactor.set(App.getInstance().getString(R.string.priority_fare_text, UIUtils.formatSurgeFactor(request.getSurgeFactor())));
            priorityFareVisibility.set(View.VISIBLE);
        }
    }


    public ObservableField<String> getPriorityFareFactor() {
        return priorityFareFactor;
    }

    public ObservableField<Integer> getPriorityFareVisibility() {
        return priorityFareVisibility;
    }

    public ObservableField<String> getTitle() {
        return title;
    }

    public ObservableField<String> getContent() {
        return content;
    }
}
