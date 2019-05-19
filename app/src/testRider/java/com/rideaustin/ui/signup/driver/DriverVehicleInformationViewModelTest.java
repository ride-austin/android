package com.rideaustin.ui.signup.driver;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.rideaustin.api.config.DriverRegistration;
import com.rideaustin.api.config.DriverRegistrationWrapper;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.List;

import common.test.BaseTest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by hatak on 21.11.16.
 */
@RunWith(RobolectricTestRunner.class)
public class DriverVehicleInformationViewModelTest extends BaseTest {

    @Mock
    DriverSignUpInteractor signUpInteractor;

    @Mock
    DriverVehicleInformationViewModel.DriverVehicleInformationListener vehicleInformationListener;

    private final Gson gson = new Gson();
    private DriverVehicleInformationViewModel viewModel;

    @Override
    public void setUp() {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        viewModel = new DriverVehicleInformationViewModel(signUpInteractor, vehicleInformationListener);
    }

    @Test
    public void testDriverVehicleInformationViewModel() throws IOException {
        final String response = IOUtils.toString(ClassLoader.getSystemResourceAsStream("driver_configuration_response.json"));
        DriverRegistration registration = gson.fromJson(response, DriverRegistrationWrapper.class).getDriverRegistration();
        when(signUpInteractor.getDriverRegistrationConfiguration()).thenReturn(registration);

        viewModel.onStart();

        Assert.assertEquals("vehicle description ", "description", viewModel.getVehicleDescription());
        final List<String> expected = Lists.newArrayList("a", "b", "c");
        verify(vehicleInformationListener).onVehicleRequirementsChanged(expected);
    }

}