package com.rideaustin.manager.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

/**
 * Created by Sergey Petrov on 28/06/2017.
 */

public class IncomingSmsReceiver extends BroadcastReceiver {

    private final SmsHandler handler;

    public IncomingSmsReceiver(SmsHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();
        if (bundle != null) {
            final Object[] pdusArr = (Object[]) bundle.get("pdus");
            for (int i = 0; i < pdusArr.length; i++) {
                SmsMessage message = SmsMessage.createFromPdu((byte[]) pdusArr[i]);
                handler.onMessage(message);
            }
        }
    }
}
