package com.rideaustin.ui.support;

import com.rideaustin.App;
import com.rideaustin.ui.drawer.triphistory.base.BaseSupportTopicsViewModel;

/**
 * Created by Sergey Petrov on 17/03/2017.
 */

public class ContactSupportViewModel extends BaseSupportTopicsViewModel {

    private final long rideId;

    public ContactSupportViewModel(View view, long rideId) {
        super(view);
        this.rideId = rideId;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!App.getDataManager().isLoggedIn()) {
            // app would be sent to login on resume
            return;
        }
        if (App.getDataManager().getSupportTopicsModel() == null) {
            // unexpected state
            return;
        }
        observeTopics(App.getDataManager().getSupportTopicsModel().getParentTopicsObservable());
    }

    @Override
    protected long getRideId() {
        return rideId;
    }
}
