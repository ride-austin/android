package com.rideaustin.engine.state;

import com.rideaustin.App;
import com.rideaustin.engine.BaseEngineState;
import com.rideaustin.engine.DriverTrackingType;
import com.rideaustin.engine.SwitchNextData;

import rx.Observable;

/**
 * Created by rost on 8/11/16.
 */
public class UnauthorizedState extends BaseEngineState {

    public UnauthorizedState() {
        super(DriverTrackingType.NONE);
        requestNotBindUI();
    }

    @Override
    public Observable<?> switchNext(SwitchNextData data) {
        return Observable.error(new IllegalStateException("unauthorized. Doesn't support switch"));
    }

    @Override
    protected void onActivated() {
        App.getInstance().wakeLockRelease();
        super.onActivated();
    }

    @Override
    public Type getType() {
        return Type.OFFLINE;
    }
}
