package com.rideaustin.utils;

import android.content.Context;

import com.rideaustin.R;

import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertEquals;

/**
 * This could be run with Robolectrics, but having problems with resources under flavors and multi dex.
 *
 * Created by Sergey Petrov on 22/05/2017.
 */

public class LocalizeUtilsTest {

    @Test
    public void shouldFormatDriverEta() {
        Context context = getInstrumentation().getTargetContext().getApplicationContext();
        assertEquals(context.getString(R.string.no_mins), LocalizeUtils.formatDriverEta(context, null));
        assertEquals(context.getString(R.string.zero_mins), LocalizeUtils.formatDriverEta(context, 0L));
        assertEquals(context.getString(R.string.zero_mins), LocalizeUtils.formatDriverEta(context, 59L));
        assertEquals(context.getString(R.string.one_min), LocalizeUtils.formatDriverEta(context, 60L));
        assertEquals(context.getString(R.string.one_min), LocalizeUtils.formatDriverEta(context, 119L));
        assertEquals(context.getString(R.string.many_mins, 2), LocalizeUtils.formatDriverEta(context, 120L));
    }

}
