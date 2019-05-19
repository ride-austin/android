package com.rideaustin.ui.documents;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.databinding.UploadDocumentsBinding;
import com.rideaustin.ui.base.EngineStatelessActivity;
import com.rideaustin.ui.genericsupport.GenericContactSupportFragment;

import java8.util.Optional;

/**
 * Created by crossover on 22/01/2017.
 */

public abstract class UploadDocumentsActivity extends EngineStatelessActivity implements UploadDocumentsView {


    protected UploadDocumentsBinding binding;
    private UploadDocumentsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_upload_documents);
        setToolbar(binding.toolbar);
        setToolbarTitle(getTitleString());
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        viewModel = new UploadDocumentsViewModel(this);
        binding.setViewModel(viewModel);
    }

    protected abstract String getTitleString();

    @Override
    public void onStart() {
        super.onStart();
        viewModel.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.onStop();
    }

    @Override
    public void onConfigLoaded(GlobalConfig config) {
        viewModel.loadLogo(binding.cityLogo, config);

        binding.needHelp.setOnClickListener(v -> {
            Optional<Driver> driver = App.getDataManager().getDriver();
            GenericContactSupportFragment messageFragment = GenericContactSupportFragment.newInstance(Optional.empty(), driver.map(Driver::getCityId));
            replaceFragment(messageFragment, R.id.content_frame, true);
            binding.needHelp.setVisibility(View.GONE);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setToolbarTitle(getTitleString());
        binding.needHelp.setVisibility(View.VISIBLE);
    }
}
