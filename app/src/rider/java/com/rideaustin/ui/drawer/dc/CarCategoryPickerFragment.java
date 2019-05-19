package com.rideaustin.ui.drawer.dc;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rideaustin.R;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentCarCategoryPickerBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.DividerItemDecoration;
import com.rideaustin.ui.common.ViewModelsProvider;

import timber.log.Timber;

/**
 * Created by hatak on 31.10.2017.
 */

public class CarCategoryPickerFragment extends BaseFragment<CarCategoryPickerViewModel> {

    private FragmentCarCategoryPickerBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setToolbarTitleAligned(R.string.category_title, Gravity.LEFT);
        setViewModel(obtainViewModel(CarCategoryPickerViewModel.class));
        getViewModel().setModel(ViewModelsProvider.of(DirectConnectActivity.class).get(DirectConnectViewModel.class));
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_car_category_picker, container, false);
        CarCategoryAdapter adapter = new CarCategoryAdapter((position, view) -> getViewModel().selectCarCategoryAt(position));
        binding.listCarTypes.setAdapter(adapter);
        binding.listCarTypes.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.listCarTypes.addItemDecoration(new DividerItemDecoration(getContext(),
                R.drawable.horizontal_divider_line));
        untilDestroy(getViewModel().getItems()
                .observeOn(RxSchedulers.main())
                .subscribe(adapter::setItems, Timber::e));
        return binding.getRoot();
    }
}
