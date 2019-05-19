package com.rideaustin.api;

import com.rideaustin.BuildConfig;
import com.rideaustin.api.service.CampaignsService;
import com.rideaustin.api.service.CharitiesService;
import com.rideaustin.api.service.DriverService;
import com.rideaustin.api.service.FareService;
import com.rideaustin.api.service.PaymentService;
import com.rideaustin.api.service.PromoCodeService;
import com.rideaustin.api.service.RiderService;
import com.rideaustin.api.service.RidesService;
import com.rideaustin.api.service.SurgeAreasService;
import com.rideaustin.utils.gradle.BuildConfigProxy;

/**
 * Created by supreethks on 23/10/16.
 */

public class BaseDataManager extends CommonDataManager {

    public static final int DRIVER_SERVICE_TIMEOUT_IN_SECONDS = 180;
    private DriverService driverService;
    private RidesService ridesService;
    private PaymentService paymentService;
    private CharitiesService charitiesService;
    private RiderService riderService;
    private PromoCodeService promoCodeService;
    private SurgeAreasService surgeAreasService;
    private FareService fareService;
    private CampaignsService campaignsService;

    public BaseDataManager() {
        this(BuildConfigProxy.getApiEndpoint());
    }

    public BaseDataManager(final String apiEndpoint) {
        charitiesService = createApi(CharitiesService.class, apiEndpoint, true, true);
        riderService = createApi(RiderService.class, apiEndpoint, true, true);
        driverService = createApi(DriverService.class, apiEndpoint, true, DRIVER_SERVICE_TIMEOUT_IN_SECONDS, true);
        ridesService = createApi(RidesService.class, apiEndpoint, true, true);
        paymentService = createApi(PaymentService.class, apiEndpoint, true, true);
        promoCodeService = createApi(PromoCodeService.class, apiEndpoint, true, true);
        surgeAreasService = createApi(SurgeAreasService.class, apiEndpoint, true, true);
        fareService = createApi(FareService.class, apiEndpoint, true, true);
        campaignsService = createApi(CampaignsService.class, apiEndpoint, true, true);
    }

    public DriverService getDriverService() {
        return driverService;
    }

    public RidesService getRidesService() {
        return ridesService;
    }

    public CharitiesService getCharitiesService() {
        return charitiesService;
    }

    public RiderService getRiderService() {
        return riderService;
    }

    public PaymentService getPaymentService() {
        return paymentService;
    }

    public PromoCodeService getPromoCodeService() {
        return promoCodeService;
    }

    public SurgeAreasService getSurgeAreasService() {
        return surgeAreasService;
    }

    public FareService getFareService() {
        return fareService;
    }

    public CampaignsService getCampaignsService() {
        return campaignsService;
    }
}
