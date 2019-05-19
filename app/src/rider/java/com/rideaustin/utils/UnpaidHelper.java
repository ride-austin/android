package com.rideaustin.utils;

import com.rideaustin.api.model.UnpaidBalance;

import java8.util.Optional;

/**
 * Created by Sergey Petrov on 18/08/2017.
 */

public class UnpaidHelper {

    public static boolean isUnpaid(Optional<UnpaidBalance> optional) {
        return optional.map(unpaidBalance -> unpaidBalance.getWillChargeOn() > TimeUtils.currentTimeMillis())
                .orElse(false);
    }

}
