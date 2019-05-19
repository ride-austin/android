package com.rideaustin.ui.rate;

import com.rideaustin.api.model.Ride;
import com.rideaustin.ui.common.BaseView;

/**
 * Created by Viktor Kifer
 * On 11-Jan-2017.
 */

public interface RateDriverView extends BaseView {

    void onDriverRatingSent();

    void onLongComment();

    void onCustomFieldRequired();

    void onBevoPayment(final Ride ride);

    void scrollDownToSubmit();
}
