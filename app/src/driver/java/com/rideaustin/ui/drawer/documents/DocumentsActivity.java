package com.rideaustin.ui.drawer.documents;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import com.rideaustin.R;
import com.rideaustin.api.config.DriverRegistration;
import com.rideaustin.api.config.TncCard;
import com.rideaustin.databinding.DocumentsBinding;
import com.rideaustin.ui.base.EngineStatelessActivity;
import com.rideaustin.ui.drawer.documents.license.UpdateLicenseActivity;
import com.rideaustin.ui.drawer.documents.tnc.UpdateTNCActivity;

import java8.util.Optional;

public class DocumentsActivity extends EngineStatelessActivity implements DocumentsView {

    DocumentsBinding binding;
    DocumentsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_documents);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        binding.title.setText(R.string.documents);

        viewModel = new DocumentsViewModel(this);

        binding.textTncCard.setVisibility(View.GONE);
        binding.textTncCard.setOnClickListener(v -> onTncClicked());
        binding.textDriverLicense.setOnClickListener(v -> onLicenseClicked());
    }

    private void onLicenseClicked() {
        startActivity(UpdateLicenseActivity.newIntent(this));
    }

    private void onTncClicked() {
        startActivity(UpdateTNCActivity.newIntent(this, viewModel.getDriverRegistration()));
    }

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
    public void onDriverRegistrationLoaded() {
        String tncName = Optional.ofNullable(viewModel.getDriverRegistration())
                .map(DriverRegistration::getTncCard)
                .map(TncCard::getHeader).orElse("");
        if (!tncName.isEmpty()) {
            binding.textTncCard.setText(getString(R.string.my_tnc_card, tncName));
            binding.textTncCard.setVisibility(View.VISIBLE);
        }
    }
}
