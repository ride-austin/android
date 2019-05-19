package com.rideaustin.ui.earnings;

/**
 * Created by vokol on 22.08.2016.
 */
public enum WeekDays {
    Monday(0),
    Tuesday(1),
    Wednesday(2),
    Thursday(3),
    Friday(4),
    Saturday(5),
    Sunday(6);

    int weekId;

    WeekDays(int id){
        this.weekId = id;
    }
}
