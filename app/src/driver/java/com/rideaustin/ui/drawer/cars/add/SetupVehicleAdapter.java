package com.rideaustin.ui.drawer.cars.add;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by rideclientandroid on 16.08.2016.
 */
public class SetupVehicleAdapter extends ArrayAdapter<String> {
    boolean isArrowsVisible;
    int textViewId;

    public SetupVehicleAdapter(Context context, @LayoutRes int layoutId, @IdRes int textViewId, @NonNull List<String> items) {
        super(context, layoutId, textViewId, items);
        this.isArrowsVisible = true;
        this.textViewId = textViewId;
    }

    public SetupVehicleAdapter(Context context, @LayoutRes int layoutId, @IdRes int textViewId, @NonNull List<String> items, boolean isArrowsVisible) {
        super(context, layoutId, textViewId, items);
        this.isArrowsVisible = isArrowsVisible;
        this.textViewId = textViewId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = super.getView(position, convertView, parent);

        if (!isArrowsVisible) {
            ((TextView) view.findViewById(textViewId)).setCompoundDrawables(null, null, null, null);
        }

        return view;
    }
}