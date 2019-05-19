package com.rideaustin.ui.drawer.triphistory.base;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.rideaustin.R;
import com.rideaustin.api.model.SupportTopic;
import com.rideaustin.base.BaseActivity;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.ui.drawer.triphistory.SupportTopicMessageFragment;
import com.rideaustin.ui.drawer.triphistory.SupportTopicsFragment;
import com.rideaustin.ui.drawer.triphistory.forms.SupportFormFragment;

import java.util.List;

/**
 * Created by Sergey Petrov on 16/03/2017.
 */

public class BaseSupportTopicsFragment extends BaseFragment implements BaseSupportTopicsViewModel.View, View.OnClickListener {

    private BaseSupportTopicsViewModel viewModel;
    private ViewGroup topicsView;
    private ScrollView scrollView;
    private int scrollPosition;

    public void setViewModel(BaseSupportTopicsViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void setTopicsView(ViewGroup topicsView) {
        this.topicsView = topicsView;
    }

    public void setScrollView(ScrollView scrollView) {
        this.scrollView = scrollView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (scrollPosition > 0) {
            // weird bug with ScrollView layout, need delay a bit
            new Handler().postDelayed(() -> scrollView.setScrollY(scrollPosition), 50);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        scrollPosition = scrollView.getScrollY();
    }

    @Override
    public void doOnTopicsLoaded(List<SupportTopic> topics) {
        topicsView.removeAllViews();
        if (topics != null && !topics.isEmpty() && isAttached()) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            for (SupportTopic topic : topics) {
                View view = inflater.inflate(R.layout.view_support_topic_item, topicsView, false);
                TextView textView = (TextView) view.findViewById(R.id.textView);
                textView.setText(topic.getDescription());
                view.setTag(topic);
                view.setOnClickListener(this);
                topicsView.addView(view);
            }
        }
    }

    @Override
    public void gotoTopic(long rideId, int topicId) {
        if (isAttached()) {
            SupportTopicsFragment fragment = SupportTopicsFragment.newInstance(rideId, topicId);
            ((BaseActivity) getActivity()).replaceFragment(fragment, R.id.rootView, true);
        }
    }

    @Override
    public void gotoMessage(long rideId, int topicId) {
        if (isAttached()) {
            SupportTopicMessageFragment fragment = SupportTopicMessageFragment.newInstance(rideId, topicId);
            ((BaseActivity) getActivity()).replaceFragment(fragment, R.id.rootView, true);
        }
    }

    @Override
    public void gotoForms(long rideId, int topicId) {
        if (isAttached()) {
            SupportFormFragment fragment = SupportFormFragment.newInstance(rideId, topicId);
            ((BaseActivity) getActivity()).replaceFragment(fragment, R.id.rootView, true);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() instanceof SupportTopic) {
            SupportTopic topic = (SupportTopic) v.getTag();
            viewModel.onTopicSelected(topic);
        }
    }
}
