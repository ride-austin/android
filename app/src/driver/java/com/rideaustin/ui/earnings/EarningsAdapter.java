package com.rideaustin.ui.earnings;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

/**
 * Created by vokol on 15.08.2016.
 */
public class EarningsAdapter extends ArrayAdapter<Earning> {

    public EarningsAdapter(Context context, List<Earning> objects) {
        super(context, com.rideaustin.R.layout.row_earnings, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        Earning earning = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(com.rideaustin.R.layout.row_earnings, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.dayView.setText(earning.getDayOfTheWeek());
        holder.tripsCountView.setText(String.format(Locale.US, "%d", earning.getEarningAmount().getTripsPerformed()));
        holder.totalFareView.setText(holder.getStringRepresentation(String.format(Locale.US, "%.2f", earning.getEarningAmount().getTotalFare())));
        holder.totalFeeView.setText(holder.getStringRepresentation(String.format(Locale.US, "%.2f", earning.getEarningAmount().getRaFee())));
        holder.totalEarningsView.setText(holder.getStringRepresentation(String.format(Locale.US, "%.2f", earning.getEarningAmount().getDriverPayment())));
        holder.totalTips.setText(earning.getEarningAmount().getTipsAmount() > 0 ? holder.getStringRepresentation(String.format(Locale.US, "%.2f", earning.getEarningAmount().getTipsAmount()))
                : holder.getStringRepresentation(String.format(Locale.US, "%.2f", 0d)));

        return convertView;
    }

    static class ViewHolder {
        private TextView dayView;
        private TextView tripsCountView;
        private TextView totalFareView;
        private TextView totalFeeView;
        private TextView totalEarningsView;
        private TextView totalTips;

        ViewHolder(View view) {
            dayView = (TextView) view.findViewById(com.rideaustin.R.id.tv_day_of_week);
            tripsCountView = (TextView) view.findViewById(com.rideaustin.R.id.tv_trip_count);
            totalFareView = (TextView) view.findViewById(com.rideaustin.R.id.tv_total_fare);
            totalFeeView = (TextView) view.findViewById(com.rideaustin.R.id.tv_total_fee);
            totalEarningsView = (TextView) view.findViewById(com.rideaustin.R.id.tv_total_earnings);
            totalTips = (TextView) view.findViewById(com.rideaustin.R.id.tv_total_tips);
        }

        @NonNull
        private String getStringRepresentation(String totalFare) {
            return "$" + totalFare;
        }
    }
}
