package com.rideaustin.base;

import android.support.annotation.FloatRange;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;

import com.rideaustin.api.model.QueueResponse;

import java.util.List;

/**
 * Created by kshumelchyk on 7/8/16.
 */
public interface BaseActivityCallback extends ProgressCallback {

    void setToolbarTitle(String title);

    void setToolbarTitle(@StringRes int stringId);

    void setToolbarTitleAligned(String title, int gravity);

    void setToolbarTitleAligned(@StringRes int stringId, int gravity);

    void setToolbarSubtitle(String title);

    void setToolbarSubtitle(@StringRes int stringId);

    void setToolbarAvatar(String photoUrl);

    void clearToolbarTitles();

    void setToolbarIndicatorEnabled(boolean enabled);

    boolean navigateTo(@IdRes int id);

    void hideMenu(@IdRes int id);

    void showMenu(@IdRes int id);

    void onBackgroundAlphaChanged(@FloatRange(from = 0f, to = 1f) float alpha);

    void finish();

}
