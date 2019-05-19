package com.rideaustin.engine;

import rx.Observable;

/**
 * Created by rost on 8/12/16.
 */
public interface SwitchNext {
    Observable<?> switchNext(SwitchNextData data);
}
