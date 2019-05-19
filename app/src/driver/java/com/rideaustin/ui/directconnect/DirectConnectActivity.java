package com.rideaustin.ui.directconnect;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.rideaustin.R;
import com.rideaustin.databinding.ActivityDirectConnectBinding;
import com.rideaustin.ui.base.EngineStatelessActivity;
import com.rideaustin.ui.utils.MaterialDialogCreator;

/**
 * Created by hatak on 07.11.2017.
 */

public class DirectConnectActivity extends EngineStatelessActivity<DirectConnectViewModel> {

    private ActivityDirectConnectBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setViewModel(obtainViewModel(DirectConnectViewModel.class));
        binding = DataBindingUtil.setContentView(this, R.layout.activity_direct_connect);
        binding.setViewModel(getViewModel());
        setToolbar(binding.toolbar);
        setToolbarTitle("Direct Connect");
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        getViewModel().initialize();
        binding.done.setOnClickListener(v -> {
            MaterialDialogCreator.createConfirmationDialog(getString(R.string.direct_connect_change_id_confirmation)
                    , this
                    , (dialog1, which) -> getViewModel().getNewDirectConnectId()
                    , (dialog1, which) -> {
                    });

        });
    }

}
