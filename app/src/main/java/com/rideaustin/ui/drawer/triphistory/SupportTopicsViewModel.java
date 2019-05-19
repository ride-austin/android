package com.rideaustin.ui.drawer.triphistory;

import android.support.annotation.Nullable;

import com.rideaustin.App;
import com.rideaustin.api.model.SupportTopic;
import com.rideaustin.ui.drawer.triphistory.base.BaseSupportTopicsViewModel;

/**
 * Created by Sergey Petrov on 16/03/2017.
 */

public class SupportTopicsViewModel extends BaseSupportTopicsViewModel {

    private final View view;

    private final long rideId;

    private final int parentTopicId;

    public SupportTopicsViewModel(View view, long rideId, int parentTopicId) {
        super(view);
        this.view = view;
        this.rideId = rideId;
        this.parentTopicId = parentTopicId;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!App.getDataManager().isLoggedIn()) {
            // app would be sent to login on resume
            return;
        }
        if (App.getDataManager().getSupportTopicsModel() == null) {
            // unexpected state
            view.onUnexpectedState();
            return;
        }

        observeTopics(App.getDataManager().getSupportTopicsModel().getChildTopicsObservable(parentTopicId));
    }

    @Nullable
    public String getTitle() {
        if (App.getDataManager().getSupportTopicsModel() != null) {
            SupportTopic topic = App.getDataManager().getSupportTopicsModel().getTopicById(parentTopicId);
            if (topic != null) {
                return topic.getDescription();
            }
        }
        return null;
    }

    @Override
    protected long getRideId() {
        return rideId;
    }

    public interface View extends BaseSupportTopicsViewModel.View {
        void onUnexpectedState();
    }

}
