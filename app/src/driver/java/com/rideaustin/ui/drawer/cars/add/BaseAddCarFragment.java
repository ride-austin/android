package com.rideaustin.ui.drawer.cars.add;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.rideaustin.base.BaseFragment;
import com.rideaustin.ui.drawer.cars.add.AddCarActivity.AddCarSequence;
import com.rideaustin.ui.common.TakePhotoFragment;

/**
 * Created by crossover on 24/01/2017.
 */

public class BaseAddCarFragment extends BaseFragment {
    protected AddCarView callback;
    protected AddCarViewModel addCarViewModel;

    public static final String SEQUENCE_KEY = "sequence_key";

    protected AddCarSequence sequence;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            callback = (AddCarView) context;
            addCarViewModel = callback.getCarViewModel();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("BaseAddCarFragment can be attached only to AddCarView", e);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        attachListener();
    }

    private void attachListener() {
        TakePhotoFragment.TakePhotoListener listener = null;
        if (this instanceof TakePhotoFragment.TakePhotoListener) {
            listener = (TakePhotoFragment.TakePhotoListener) this;
        }
        callback.setTakePhotoListener(listener);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sequence = (AddCarSequence) getArguments().getSerializable(SEQUENCE_KEY);
    }

    protected boolean canGoBack() {
        return true;
    }

    protected void notifyCompleted() {
        callback.onCompleted(sequence);
    }

    public AddCarSequence getSequence() {
        return sequence;
    }
}
