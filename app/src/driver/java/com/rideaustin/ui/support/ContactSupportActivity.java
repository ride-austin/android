package com.rideaustin.ui.support;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.base.BaseActivity;
import com.rideaustin.base.Transition;
import com.rideaustin.ui.drawer.triphistory.SupportTopicsModel;
import com.rideaustin.ui.drawer.triphistory.forms.SupportFormsModel;

public class ContactSupportActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_support);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setToolbar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        App.getDataManager().setSupportTopicsModel(new SupportTopicsModel(this));
        App.getDataManager().setSupportFormsModel(new SupportFormsModel(this));
        replaceFragment(new ContactSupportFragment(), R.id.rootView, false, Transition.NONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.getDataManager().setSupportTopicsModel(null);
        App.getDataManager().setSupportFormsModel(null);
    }
}
