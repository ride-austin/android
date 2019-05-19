package com.rideaustin.ui.drawer.editprofile;

import com.rideaustin.api.config.GenderSelection;

/**
 * Created by hatak on 06.11.2017.
 */

public class SelectedGender {

    private GenderSelection gender;

    private int index;

    public SelectedGender(GenderSelection gender, int index) {
        this.gender = gender;
        this.index = index;
    }

    public GenderSelection getGender() {
        return gender;
    }

    public int getIndex() {
        return index;
    }
}
