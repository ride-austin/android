package com.rideaustin.manager.sms;

import android.telephony.SmsMessage;

/**
 * Created by Sergey Petrov on 28/06/2017.
 */

public interface SmsHandler {
    void onMessage(SmsMessage message);
}
