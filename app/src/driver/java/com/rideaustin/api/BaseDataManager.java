package com.rideaustin.api;

import com.rideaustin.api.service.ActiveDriverService;
import com.rideaustin.api.service.ConfigService;
import com.rideaustin.api.service.DriverService;
import com.rideaustin.api.service.EarningsService;
import com.rideaustin.api.service.RiderService;
import com.rideaustin.api.service.RidesService;
import com.rideaustin.api.service.SurgeAreasService;
import com.rideaustin.utils.gradle.BuildConfigProxy;

/**
 * @author shumelchyk
 */
public abstract class BaseDataManager extends CommonDataManager {

    private RidesService ridesService;
    private RiderService riderService;
    private DriverService driverService;
    private ActiveDriverService activeDriverService;
    private EarningsService earningsService;
    private SurgeAreasService surgeAreasService;
    private ConfigService configService;

    public BaseDataManager() {
        riderService = createApi(RiderService.class, BuildConfigProxy.getApiEndpoint(), true, true);
        ridesService = createApi(RidesService.class, BuildConfigProxy.getApiEndpoint(), true, true);
        driverService = createApi(DriverService.class, BuildConfigProxy.getApiEndpoint(), true, true);
        activeDriverService = createApi(ActiveDriverService.class, BuildConfigProxy.getApiEndpoint(), true, true);
        earningsService = createApi(EarningsService.class, BuildConfigProxy.getApiEndpoint(), true, true);
        surgeAreasService = createApi(SurgeAreasService.class, BuildConfigProxy.getApiEndpoint(), true, true);
        configService = createApi(ConfigService.class, BuildConfigProxy.getApiEndpoint(), true, true);
    }

    public RidesService getRidesService() {
        return ridesService;
    }

    public RiderService getRiderService() {
        return riderService;
    }

    public DriverService getDriverService() {
        return driverService;
    }

    public ActiveDriverService getActiveDriverService() {
        return activeDriverService;
    }

    public SurgeAreasService getSurgeAreasService() {
        return surgeAreasService;
    }

    public ConfigService getConfigService() {
        return configService;
    }

    public EarningsService getEarningsService() {
        return earningsService;
    }
}
