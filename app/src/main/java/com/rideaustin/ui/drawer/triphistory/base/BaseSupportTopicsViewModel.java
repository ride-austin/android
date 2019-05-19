package com.rideaustin.ui.drawer.triphistory.base;

import com.rideaustin.App;
import com.rideaustin.api.model.SupportTopic;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.RxBaseObservable;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by Sergey Petrov on 16/03/2017.
 */

public abstract class BaseSupportTopicsViewModel extends RxBaseObservable {

    private final View view;

    private Subscription topicSubscription = Subscriptions.empty();
    private Subscription formsSubscription = Subscriptions.empty();

    public BaseSupportTopicsViewModel(View view) {
        this.view = view;
    }

    protected void observeTopics(Observable<List<SupportTopic>> topics) {
        addSubscription(topics.observeOn(RxSchedulers.main())
                .subscribe(this::doOnTopicsLoaded));
    }

    public void onTopicSelected(SupportTopic topic) {
        if (topic.getHasForms()) {
            if (App.getDataManager().getSupportFormsModel() != null) {
                formsSubscription.unsubscribe();
                formsSubscription = App.getDataManager().getSupportFormsModel()
                        .getSupportFormObservable(topic.getId())
                        .observeOn(RxSchedulers.main())
                        .subscribe(topics -> doOnFormLoaded(topic.getId()));
            }
        } else if (App.getDataManager().getSupportTopicsModel() != null) {
            if (topic.getHasChildren()) {
                topicSubscription.unsubscribe();
                topicSubscription = App.getDataManager().getSupportTopicsModel()
                        .getChildTopicsObservable(topic.getId())
                        .observeOn(RxSchedulers.main())
                        .subscribe(topics -> doOnChildTopicsLoaded(topic.getId(), topics));
            } else {
                view.gotoMessage(getRideId(), topic.getId());
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        topicSubscription.unsubscribe();
    }

    private void doOnFormLoaded(int topicId) {
        view.gotoForms(getRideId(), topicId);
    }

    protected void doOnTopicsLoaded(List<SupportTopic> topics) {
        view.doOnTopicsLoaded(topics);
    }

    private void doOnChildTopicsLoaded(int parentTopicId, List<SupportTopic> topics) {
        if (topics == null || topics.isEmpty()) {
            view.gotoMessage(getRideId(), parentTopicId);
        } else {
            view.gotoTopic(getRideId(), parentTopicId);
        }
    }

    abstract protected long getRideId();

    public interface View {
        void doOnTopicsLoaded(List<SupportTopic> topics);

        void gotoTopic(long rideId, int topicId);

        void gotoMessage(long rideId, int topicId);

        void gotoForms(long rideId, int topicId);
    }

}
