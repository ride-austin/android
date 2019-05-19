package com.rideaustin.ui.drawer.triphistory;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rideaustin.R;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentTripSupportMessageBinding;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;

import java8.util.Optional;

/**
 * Created by Sergey Petrov on 17/03/2017.
 */

public class SupportTopicMessageFragment extends BaseFragment implements SupportTopicMessageViewModel.View {

    private static final String RIDE_KEY = "ride_key";
    private static final String PARENT_TOPIC_KEY = "parent_topic_key";

    private SupportTopicMessageViewModel viewModel;

    public static SupportTopicMessageFragment newInstance(long rideId, int parentTopicId) {
        SupportTopicMessageFragment fragment = new SupportTopicMessageFragment();
        Bundle args = new Bundle();
        args.putLong(RIDE_KEY, rideId);
        args.putInt(PARENT_TOPIC_KEY, parentTopicId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setToolbarTitleAligned(R.string.trip_history_support_message_title, Gravity.LEFT);
        FragmentTripSupportMessageBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_trip_support_message, container, false);
        long rideId = getArguments() != null && getArguments().containsKey(RIDE_KEY) ? getArguments().getLong(RIDE_KEY) : -1;
        int parentTopicId = getArguments() != null && getArguments().containsKey(PARENT_TOPIC_KEY) ? getArguments().getInt(PARENT_TOPIC_KEY) : -1;
        viewModel = new SupportTopicMessageViewModel(this, rideId, parentTopicId);
        binding.setViewModel(viewModel);
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

    @Override
    public void onMessageSent() {
        if (isAttached()) {
            CommonMaterialDialogCreator.showSupportSuccessDialog(getActivity(), Optional.empty(), false);
        }
    }
}
