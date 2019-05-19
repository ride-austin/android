package com.rideaustin.ui.earnings;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.driver.earnings.End;
import com.rideaustin.api.model.driver.earnings.Start;
import com.rideaustin.ui.earnings.DailyEarningsViewModel.TripHistoryContent;
import com.rideaustin.ui.earnings.DailyEarningsViewModel.TripHistoryHeader;
import com.rideaustin.ui.support.ContactSupportActivity;
import com.rideaustin.utils.CommonConstants;
import com.rideaustin.utils.ListUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by vokol on 22.08.2016.
 */
class TripHistoryAdapter extends BaseExpandableListAdapter {

    private static final int UNIQUE_CHILD = 1;
    public static final String EMPTY_STRING = "";
    private Context context;
    private List<TripHistory> tripHistories;
    private DailyEarningsViewModel viewModel;
    private ChildViewHolder holder;

    TripHistoryAdapter(Context context, List<TripHistory> tripHistory, DailyEarningsViewModel viewModel) {
        this.context = context;
        this.tripHistories = tripHistory;
        this.viewModel = viewModel;
        Collections.sort(this.tripHistories);
    }

    public void setTripHistories(List<TripHistory> tripHistory) {
        this.tripHistories = tripHistory;
        Collections.sort(this.tripHistories);
        notifyDataSetChanged();
    }

    @Override
    public TripHistoryContent getChild(int groupPosition, int childPosititon) {
        return tripHistories.get(groupPosition).getHistoryContent();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        TripHistoryContent historyContent = getChild(groupPosition, childPosition);

        LayoutInflater inflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.trip_history_child_view, null);

        holder = new ChildViewHolder(convertView, this.context, historyContent.getRideId());

        convertView.setTag(holder);

        View mapContainer = convertView.findViewById(R.id.ride_map);
        mapContainer.setVisibility(historyContent.isRideCompleted() ? View.VISIBLE : View.GONE);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.image_ride_map);
        ProgressBar progress = (ProgressBar) convertView.findViewById(R.id.image_progress);
        if (historyContent.isRideCompleted()) {
            viewModel.loadMapIntoImageView(imageView, progress,historyContent);
        }

        holder.startTime.setText(historyContent.getStartedOnStr());
        holder.endTime.setText(historyContent.getEndedOnStr());

        holder.startLocation.setText(formatStartAddress(historyContent.getHomeAddress()));
        holder.endLocation.setText(formatEndAddress(historyContent.getDestinationAddress()));

        holder.cancelledView.setVisibility(historyContent.isRideCompleted() ? View.INVISIBLE : View.VISIBLE);
        holder.tipCountView.setText(historyContent.getTipAmount());

        holder.baseFareCount.setText(historyContent.getBaseFare());
        holder.fareDistanceCount.setText(historyContent.getDistanceFare());
        holder.timeFareCount.setText(historyContent.getTimeFare());
        holder.earningCount.setText(historyContent.isRideCompleted() ? historyContent.getYourEarning() : historyContent.getTotalFare());
        holder.totalFareCount.setText(historyContent.getSubTotal());

        holder.raFeeCount.setText(historyContent.isRideCompleted() ? historyContent.getRideAustinFee() : historyContent.getSubTotal());

        holder.baseFareCount.setVisibility(historyContent.isRideCompleted() ? View.VISIBLE : View.GONE);
        holder.fareDistanceCount.setVisibility(historyContent.isRideCompleted() ? View.VISIBLE : View.GONE);
        holder.timeFareCount.setVisibility(historyContent.isRideCompleted() ? View.VISIBLE : View.GONE);
        holder.totalFareCount.setVisibility(historyContent.isRideCompleted() ? View.VISIBLE : View.GONE);
        holder.tipCountView.setVisibility(historyContent.isRideCompleted() ? View.VISIBLE : View.GONE);

        holder.baseFareLabel.setVisibility(historyContent.isRideCompleted() ? View.VISIBLE : View.GONE);
        holder.distanceFareLabel.setVisibility(historyContent.isRideCompleted() ? View.VISIBLE : View.GONE);
        holder.timeFareLabel.setVisibility(historyContent.isRideCompleted() ? View.VISIBLE : View.GONE);
        holder.totalFareLabel.setVisibility(historyContent.isRideCompleted() ? View.VISIBLE : View.GONE);
        holder.tipLabel.setVisibility(historyContent.isRideCompleted() ? View.VISIBLE : View.GONE);

        holder.priorityFareLabel.setText("Priority Fare (" + historyContent.getPriorityFareAmount() + "X" + ")");
        holder.priorityFareLabel.setVisibility(historyContent.isRideCompleted() && !TextUtils.isEmpty(historyContent.getPriorityFareAmount()) ? View.VISIBLE : View.GONE);

        holder.priorityFareView.setText(context.getString(R.string.money, historyContent.getSurgeFare()));
        holder.priorityFareView.setVisibility(historyContent.isRideCompleted() && !TextUtils.isEmpty(historyContent.getSurgeFare()) ? View.VISIBLE : View.GONE);

        if (!historyContent.isRideCompleted()) {
            holder.rideAustinFeeLabel.setText(R.string.rider_cancel_fee);
        }

        holder.firstDelimiterView.setVisibility(historyContent.isRideCompleted() ? View.VISIBLE : View.GONE);
        holder.secondDelimiterView.setVisibility(historyContent.isRideCompleted() ? View.VISIBLE : View.GONE);
        holder.thirdDelimiterView.setVisibility(historyContent.isRideCompleted() ? View.VISIBLE : View.GONE);

        return convertView;
    }

    private String formatStartAddress(final Start start) {
        if (start != null) {
            return joinSkippNulls(", ", start.getAddress(), start.getZipCode());
        } else {
            return EMPTY_STRING;
        }
    }

    private String formatEndAddress(final End end) {
        if (end != null) {
            return joinSkippNulls(", ", end.getAddress(), end.getZipCode());
        } else {
            return EMPTY_STRING;
        }
    }

    private String joinSkippNulls(String separator, String... parts) {
        final ArrayList<String> list = new ArrayList<>(Arrays.asList(parts));
        ListUtils.removeNullElements(list);
        return TextUtils.join(separator, list);
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return UNIQUE_CHILD;
    }

    @Override
    public TripHistoryHeader getGroup(int groupPosition) {
        return this.tripHistories.get(groupPosition).getHeader();
    }

    @Override
    public int getGroupCount() {
        return this.tripHistories.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        TripHistoryHeader headers = getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater service = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = service.inflate(R.layout.trip_history_group_view, null);
        }

        TextView timeCompleted = (TextView) convertView.findViewById(R.id.tv_trip_time);
        TextView carName = (TextView) convertView.findViewById(R.id.tv_car_name);
        TextView amount = (TextView) convertView.findViewById(R.id.trip_payment);

        timeCompleted.setText(headers.getEndedOnStr());
        carName.setText(headers.getCarInfo());
        amount.setText(headers.getPayment());


        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private static class ChildViewHolder {
        final View convertView;
        final TextView startTime;
        final TextView endTime;
        final TextView startLocation;
        final TextView endLocation;
        final TextView earningCount;
        final TextView raFeeCount;
        final TextView totalFareCount;
        final TextView timeFareCount;
        final TextView fareDistanceCount;
        final TextView baseFareCount;
        final TextView contactSupport;
        final TextView cancelledView;
        final TextView tipCountView;
        final TextView priorityFareView;

        TextView baseFareLabel;
        TextView distanceFareLabel;
        TextView timeFareLabel;
        TextView totalFareLabel;
        TextView tipLabel;
        TextView rideAustinFeeLabel;
        TextView priorityFareLabel;

        View firstDelimiterView;
        View secondDelimiterView;
        View thirdDelimiterView;

        private ChildViewHolder(View convertView, final Context context, final long rideId) {
            this.convertView = convertView;
            startTime = (TextView) convertView.findViewById(R.id.tv_ride_time);
            endTime = (TextView) convertView.findViewById(R.id.tv_end_ride_time);
            startLocation = (TextView) convertView.findViewById(R.id.tv_ride_start);
            endLocation = (TextView) convertView.findViewById(R.id.tv_end_ride_location);
            earningCount = (TextView) convertView.findViewById(R.id.tv_earning_count);
            raFeeCount = (TextView) convertView.findViewById(R.id.tv_ra_fee_count);
            totalFareCount = (TextView) convertView.findViewById(R.id.tv_total_fare_count);
            timeFareCount = (TextView) convertView.findViewById(R.id.tv_time_fare_count);
            fareDistanceCount = (TextView) convertView.findViewById(R.id.tv_fare_distance_count);
            baseFareCount = (TextView) convertView.findViewById(R.id.tv_fare_count);
            contactSupport = (TextView) convertView.findViewById(R.id.text_contact_support);
            tipCountView = (TextView) convertView.findViewById(R.id.tv_tip_count);
            cancelledView = (TextView) convertView.findViewById(R.id.tv_cancelled);
            priorityFareLabel = (TextView) convertView.findViewById(R.id.text_priority_fare);
            priorityFareView = (TextView) convertView.findViewById(R.id.text_priority_fare_view);
            contactSupport.setOnClickListener(v -> {
                Intent intent = new Intent(context, ContactSupportActivity.class);
                intent.putExtra(CommonConstants.RIDEID_KEY, rideId);
                context.startActivity(intent);
            });

            initLabels(convertView);

            initDelimiters(convertView);
        }

        private void initLabels(View view) {
            this.baseFareLabel = (TextView) view.findViewById(R.id.text_base_fare);
            this.distanceFareLabel = (TextView) view.findViewById(R.id.text_distance_fare);
            this.timeFareLabel = (TextView) view.findViewById(R.id.text_time_fare);
            this.totalFareLabel = (TextView) view.findViewById(R.id.text_total_fare_view);
            this.tipLabel = (TextView) view.findViewById(R.id.text_tip_view);
            this.rideAustinFeeLabel = (TextView) view.findViewById(R.id.text_ride_service_fee);
            this.rideAustinFeeLabel.setText(view.getContext().getString(R.string.text_ride_service_fee, App.getAppName()));
        }

        private void initDelimiters(View view) {
            this.firstDelimiterView = view.findViewById(R.id.delimiter2);
            this.secondDelimiterView = view.findViewById(R.id.delimiter6);
            this.thirdDelimiterView = view.findViewById(R.id.delimiter3);
        }
    }
}
