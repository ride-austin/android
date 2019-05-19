package com.rideaustin.ui.genericsupport;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.rideaustin.R;
import com.rideaustin.base.BaseActivity;
import com.rideaustin.base.Transition;

import java8.util.Optional;

/**
 * Created by Sergey Petrov on 05/06/2017.
 */

public class GenericContactSupportActivity extends BaseActivity {

    private static final String CITY_ID = "cityId";
    private static final String RIDE_ID = "rideId";

    public static void launch(Activity activity, @Nullable Long rideId, @Nullable Integer cityId) {
        Intent intent = new Intent(activity, GenericContactSupportActivity.class);
        if (rideId != null) {
            intent.putExtra(RIDE_ID, rideId);
        }
        if (cityId != null) {
            intent.putExtra(CITY_ID, cityId);
        }
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_support_generic);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setToolbar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        Long rideId = getIntent().hasExtra(RIDE_ID) ? getIntent().getLongExtra(RIDE_ID, -1) : null;
        Integer cityId = getIntent().hasExtra(CITY_ID) ? getIntent().getIntExtra(CITY_ID, -1) : null;

        GenericContactSupportFragment messageFragment = GenericContactSupportFragment.newInstance(
                Optional.ofNullable(rideId),
                Optional.ofNullable(cityId));
        replaceFragment(messageFragment, R.id.content_frame, false, Transition.NONE);
    }
}
