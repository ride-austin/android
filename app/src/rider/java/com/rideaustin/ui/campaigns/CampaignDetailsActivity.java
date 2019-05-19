package com.rideaustin.ui.campaigns;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.CampaignProvider;
import com.rideaustin.api.model.campaigns.CampaignDetails;
import com.rideaustin.base.BaseActivity;
import com.rideaustin.base.Transition;
import com.rideaustin.databinding.ActivityCampaignDetailsBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.drawer.donate.DonateFragment;
import com.rideaustin.utils.toast.RAToast;

/**
 * Created on 5/20/18.
 *
 * @author sdelaysam
 */
public class CampaignDetailsActivity extends BaseActivity<CampaignDetailsViewModel> {

    private static final String DATA_KEY = "data_key";

    public static Intent getInstance(Context context, CampaignProvider provider) {
        Intent intent = new Intent(context, CampaignDetailsActivity.class);
        intent.putExtra(DATA_KEY, provider);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!App.getDataManager().isLoggedIn() || !getIntent().hasExtra(DATA_KEY)) {
            super.onCreate(null);
            finish();
            return;
        }

        super.onCreate(savedInstanceState);
        setViewModel(obtainViewModel(CampaignDetailsViewModel.class));

        ActivityCampaignDetailsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_campaign_details);
        binding.setViewModel(getViewModel());

        setToolbar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        untilDestroy(getViewModel().getDetails()
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnDetails, this::doOnDetailsError));

        getViewModel().setProvider((CampaignProvider) getIntent().getSerializableExtra(DATA_KEY));
    }

    private void doOnDetails(CampaignDetails details) {
        if (getSupportFragmentManager().findFragmentById(R.id.content_frame) == null) {
            replaceFragment(CampaignDetailsFragment.getInstance(details), R.id.content_frame, false, Transition.NONE);
        }
    }

    private void doOnDetailsError(Throwable throwable) {
        finish();
    }

    public void onShowMap(CampaignDetails campaignDetails) {
        replaceFragment(CampaignDetailsMapFragment.getInstance(campaignDetails), R.id.content_frame, true);
    }

}
