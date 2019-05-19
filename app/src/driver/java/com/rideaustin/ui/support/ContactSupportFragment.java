package com.rideaustin.ui.support;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rideaustin.R;
import com.rideaustin.databinding.FragmentContactSupportBinding;
import com.rideaustin.ui.drawer.triphistory.base.BaseSupportTopicsFragment;
import com.rideaustin.utils.CommonConstants;

/**
 * Created by Sergey Petrov on 17/03/2017.
 */

public class ContactSupportFragment extends BaseSupportTopicsFragment {

    private ContactSupportViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setToolbarTitleAligned(R.string.contact_support, Gravity.LEFT);
        FragmentContactSupportBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contact_support, container, false);
        long rideId = getActivity() != null && getActivity().getIntent() != null ? getActivity().getIntent().getLongExtra(CommonConstants.RIDEID_KEY, -1) : -1;
        viewModel = new ContactSupportViewModel(this, rideId);
        binding.setViewModel(viewModel);
        setViewModel(viewModel);
        setScrollView(binding.scrollView);
        setTopicsView(binding.viewSupportTopics);
        return binding.getRoot();
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

}
