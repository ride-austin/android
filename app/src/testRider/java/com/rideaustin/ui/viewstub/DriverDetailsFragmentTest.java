package com.rideaustin.ui.viewstub;

import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.driver.ActiveDriver;
import com.rideaustin.ui.map.MapViewModel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Created by Sergey Petrov on 27/04/2017.
 */

@RunWith(RobolectricTestRunner.class)
public class DriverDetailsFragmentTest {

    @Test
    public void testNeedUpdateRide() {
        MapViewModel mapViewModel = mock(MapViewModel.class);
        DriverDetailsFragment.DriverDetailsListener listener = mock(DriverDetailsFragment.DriverDetailsListener.class);
        DriverDetailsFragment fragment;
        ActiveDriver driver;
        Ride ride;

        // null active driver and null ride
        fragment = DriverDetailsFragment.newInstance(null, listener, mapViewModel);
        assertFalse(fragment.needUpdate(null));

        // empty active driver and null ride
        fragment = DriverDetailsFragment.newInstance(new ActiveDriver(), listener, mapViewModel);
        assertFalse(fragment.needUpdate(null));

        // active driver with id and null ride
        driver = new ActiveDriver();
        driver.setId(1);
        fragment = DriverDetailsFragment.newInstance(driver, listener, mapViewModel);
        assertTrue(fragment.needUpdate(null));

        // active driver with id and empty ride
        driver = new ActiveDriver();
        driver.setId(1);
        fragment = DriverDetailsFragment.newInstance(driver, listener, mapViewModel);
        assertTrue(fragment.needUpdate(new Ride()));

        // active driver with id and empty ride
        driver = new ActiveDriver();
        driver.setId(1);
        fragment = DriverDetailsFragment.newInstance(driver, listener, mapViewModel);
        assertTrue(fragment.needUpdate(new Ride()));

        // active driver with id and ride with empty driver
        driver = new ActiveDriver();
        driver.setId(1);
        ride = new Ride();
        ride.setActiveDriver(new ActiveDriver());
        fragment = DriverDetailsFragment.newInstance(driver, listener, mapViewModel);
        assertTrue(fragment.needUpdate(ride));

        // same driver ids
        driver = new ActiveDriver();
        driver.setId(1);
        ride = new Ride();
        ride.setActiveDriver(new ActiveDriver());
        ride.getActiveDriver().setId(1);
        fragment = DriverDetailsFragment.newInstance(driver, listener, mapViewModel);
        assertFalse(fragment.needUpdate(ride));

        // different driver ids
        driver = new ActiveDriver();
        driver.setId(1);
        ride = new Ride();
        ride.setActiveDriver(new ActiveDriver());
        ride.getActiveDriver().setId(2);
        fragment = DriverDetailsFragment.newInstance(driver, listener, mapViewModel);
        assertTrue(fragment.needUpdate(ride));
  }

}
