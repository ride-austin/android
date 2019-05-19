package com.rideaustin.ui.earnings;

/**
 * Created by hatak on 19.12.16.
 */

public class TripHistory implements Comparable<TripHistory> {
    private final DailyEarningsViewModel.TripHistoryHeader header;
    private final DailyEarningsViewModel.TripHistoryContent historyContent;

    public TripHistory(final DailyEarningsViewModel.TripHistoryHeader header, final DailyEarningsViewModel.TripHistoryContent historyContent) {

        this.header = header;
        this.historyContent = historyContent;
    }

    public DailyEarningsViewModel.TripHistoryHeader getHeader() {
        return header;
    }

    public DailyEarningsViewModel.TripHistoryContent getHistoryContent() {
        return historyContent;
    }

    @Override
    public int compareTo(final TripHistory tripHistory) {
        return getHeader().compareTo(tripHistory.getHeader());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TripHistory that = (TripHistory) o;

        if (header != null ? !header.equals(that.header) : that.header != null) return false;
        return historyContent != null ? historyContent.equals(that.historyContent) : that.historyContent == null;
    }

    @Override
    public int hashCode() {
        int result = header != null ? header.hashCode() : 0;
        result = 31 * result + (historyContent != null ? historyContent.hashCode() : 0);
        return result;
    }
}
