package com.rideaustin.ui.map.strategy;

import com.rideaustin.engine.EngineState;
import com.rideaustin.engine.state.AcceptedState;
import com.rideaustin.engine.state.ArrivedState;
import com.rideaustin.engine.state.InactiveState;
import com.rideaustin.engine.state.OfflinePollingState;
import com.rideaustin.engine.state.OnlineState;
import com.rideaustin.engine.state.PendingAcceptState;
import com.rideaustin.engine.state.TripEndedState;
import com.rideaustin.engine.state.TripStartedState;

/**
 * Created by Viktor Kifer
 * On 27-Dec-2016.
 */

public class StrategyFactory {

    public static UIStrategy createStrategyForState(EngineState state) {
        switch (state.getType()) {
            case INACTIVE:
                return new InactiveStrategy((InactiveState) state);
            case OFFLINE:
                return new OfflineStrategy((OfflinePollingState) state);
            case ONLINE:
                return new OnlineStrategy((OnlineState) state);
            case PENDING_ACCEPT:
                return new PendingAcceptStrategy((PendingAcceptState) state);
            case ACCEPTED:
                return new AcceptedStrategy((AcceptedState) state);
            case ARRIVED:
                return new ArrivedStrategy((ArrivedState) state);
            case STARTED:
                return new TripStartedStrategy((TripStartedState) state);
            case ENDED:
                return new TripEndedStrategy((TripEndedState) state);
            default:
                throw new IllegalStateException("unimplemented state " + state);
        }
    }

}
