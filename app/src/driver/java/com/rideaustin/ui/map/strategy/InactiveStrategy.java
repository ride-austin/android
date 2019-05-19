package com.rideaustin.ui.map.strategy;


import com.rideaustin.engine.state.InactiveState;
import com.rideaustin.ui.map.MapFragmentInterface;

/**
 * Created by kshumelchyk on 8/16/16.
 */
public class InactiveStrategy extends BaseUIStrategy<InactiveState> {

    public InactiveStrategy(InactiveState state) {
        super(state);
    }

    @Override
    protected void onAttach(MapFragmentInterface view) {
        super.onAttach(view);
        view.showInactiveWarning();
        view.showDriverOnMap(true);
        view.hideMenu();
        view.hideTopFragment();
        view.hideBottomFragment();
        view.clearSurgeAreas();
        updateToolbar();
    }

    @Override
    public void updateToolbar() {
        getView().clearToolbarTitle();
        getView().showAppNameTitle();
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        getView().hideInactiveWarning();
        getView().hideDriverOnMap();
    }

    @Override
    public void onFlowAction(boolean confirmed) {
        
    }
}
