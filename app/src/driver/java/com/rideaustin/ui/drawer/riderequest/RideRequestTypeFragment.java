package com.rideaustin.ui.drawer.riderequest;

import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.rideaustin.R;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentRideRequestTypeBinding;
import com.rideaustin.databinding.RideRequestTypeItemBinding;
import com.rideaustin.utils.CommonConstants;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.ImageHelper;

import java.util.List;

/**
 * @author sdelaysam.
 */

public class RideRequestTypeFragment extends BaseFragment implements RideRequestTypeViewModel.View {

    public static final String TAG_SELECTED = "Selected";
    public static final String TAG_ACTIVE = "Active";

    private static final int COLUMN_COUNT = 3;

    private FragmentRideRequestTypeBinding binding;
    private RideRequestTypeViewModel viewModel;

    public static RideRequestTypeFragment newInstance() {
        Bundle args = new Bundle();
        RideRequestTypeFragment fragment = new RideRequestTypeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ride_request_type, container, false);
        viewModel = new RideRequestTypeViewModel(this);
        binding.setViewModel(viewModel);
        binding.filterNone.setOnClickListener((view) -> {
            viewModel.onWomenModeSelected(false);
            viewModel.onDirectConnectSelected(false);
        });
        binding.filterFemaleOnly.setOnClickListener((view) -> {
            viewModel.onWomenModeSelected(true);
            viewModel.onDirectConnectSelected(false);
        });
        binding.filterFemaleOnlyRight.setOnClickListener((view) -> {
            viewModel.onWomenModeSelected(true);
            viewModel.onDirectConnectSelected(false);
        });
        binding.filterDcOnly.setOnClickListener((view) -> {
            viewModel.onWomenModeSelected(false);
            viewModel.onDirectConnectSelected(true);
        });
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.onDestroy();
    }

    @Override
    public void onRideRequestTypes(List<RideRequestTypeViewModel.RideRequestType> types) {
        binding.itemTypeHolder.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        LinearLayout row = null;
        for (int i = 0; i < types.size(); i++) {
            RideRequestTypeViewModel.RideRequestType type = types.get(i);
            RideRequestTypeItemBinding itemBinding = DataBindingUtil.inflate(inflater,
                    R.layout.layout_ride_request_type, null, false);
            itemBinding.tvCarType.setText(type.name);
            loadCarTypeIcon(itemBinding, type);
            showActive(itemBinding, type.isActive);
            showSelected(itemBinding, type.isSelected);
            String tag = type.isSelected ? TAG_SELECTED : type.isActive ? TAG_ACTIVE : null;
            itemBinding.getRoot().setTag(tag);
            itemBinding.getRoot().setOnClickListener(v -> viewModel.onTypeClicked(type));
            itemBinding.getRoot().setLayoutParams(new ViewGroup.LayoutParams(
                    getResources().getDimensionPixelOffset(R.dimen.ride_request_item_type),
                    getResources().getDimensionPixelOffset(R.dimen.ride_request_item_type)));

            if (i % COLUMN_COUNT == 0) {
                row = new LinearLayout(getActivity());
                row.setOrientation(LinearLayout.HORIZONTAL);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER_HORIZONTAL;
                binding.itemTypeHolder.addView(row, params);
            }
            if (row != null) {
                row.addView(itemBinding.getRoot());
            }
        }
    }

    @Override
    public void onClear() {
        binding.itemTypeHolder.removeAllViews();
    }

    private void showActive(RideRequestTypeItemBinding itemBinding, boolean isActive) {
        itemBinding.carType.setImageAlpha(isActive ? 255 : 50);
        itemBinding.tvCarType.setAlpha(isActive ? 1f : 0.2f);
    }

    private void showSelected(RideRequestTypeItemBinding itemBinding, boolean isSelected) {
        itemBinding.itemBg.setBackgroundResource(isSelected
                ? R.drawable.bg_car_focused_ride_request
                : R.drawable.bg_car_ride_request);
        itemBinding.selectStateIcon.setImageResource(isSelected
                ? R.drawable.icn_check_ride_request
                : android.R.color.transparent);
    }

    private void loadCarTypeIcon(RideRequestTypeItemBinding itemBinding, RideRequestTypeViewModel.RideRequestType type) {
        if (!TextUtils.isEmpty(type.carIcon)) {
            // try to use server icons first
            ImageHelper.loadResizedImageIntoView(itemBinding.carType, type.carIcon);
        } else if (isCategoryDefault(type.carType.getCarCategory())) {
            // if server icon is empty and category is default - show embedded icon
            itemBinding.carType.setImageDrawable(getCarIconDrawable(itemBinding, type.name));
        } else {
            // show regular embedded icon if everything failed
            itemBinding.carType.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                    R.drawable.icn_car_regular_ride_request));
        }
    }

    private Drawable getCarIconDrawable(RideRequestTypeItemBinding itemBinding, String category) {
        switch (category) {
            case CommonConstants.CarCategory.REGULAR:
                return ContextCompat.getDrawable(getContext(), R.drawable.icn_car_regular_ride_request);
            case CommonConstants.CarCategory.SUV:
                return ContextCompat.getDrawable(getContext(), R.drawable.icn_car_suv_ride_request);
            case CommonConstants.CarCategory.PREMIUM:
                return ContextCompat.getDrawable(getContext(), R.drawable.icn_car_luxury_ride_request);
            case CommonConstants.CarCategory.LUXURY:
                return ContextCompat.getDrawable(getContext(), R.drawable.icn_car_luxury_ride_request);
            default:
                return itemBinding.carType.getDrawable();
        }
    }

    private boolean isCategoryDefault(String category) {
        return category.equals(CommonConstants.CarCategory.REGULAR)
                || category.equals(CommonConstants.CarCategory.SUV)
                || category.equals(Constants.CarCategory.PREMIUM)
                || category.equals(Constants.CarCategory.LUXURY);
    }
}
