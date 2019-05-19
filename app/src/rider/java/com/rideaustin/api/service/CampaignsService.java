package com.rideaustin.api.service;

import com.rideaustin.api.model.campaigns.CampaignDetails;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created on 5/19/18.
 *
 * @author sdelaysam
 */
public interface CampaignsService {

    @GET("rest/campaigns/providers/{id}/campaigns")
    Observable<List<CampaignDetails>> getProviderCampaigns(@Path("id") long providerId);

    @GET("rest/campaigns/{id}")
    Observable<CampaignDetails> getCampaignDetails(@Path("id") long campaignId);
}
