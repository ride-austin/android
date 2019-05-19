package com.rideaustin.ui.drawer.triphistory;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.paymenthistory.PaymentHistory;
import com.rideaustin.databinding.FragmentTripHistoryItemBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.AdapterItemChange;

import java.util.List;

import rx.subscriptions.CompositeSubscription;

public class TripHistoryFragmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM = 0;
    private static final int LOADING = 1;

    private Context context;
    private List<PaymentHistory> list;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    public TripHistoryFragmentAdapter(Context context) {
        this.context = context;
    }

    public void onStart() {
        if (App.getDataManager().getTripHistoryModel() != null) {
            subscriptions.add(getModel().getListObservable()
                    .observeOn(RxSchedulers.main())
                    .subscribe(this::doOnListChanged));
            subscriptions.add(getModel().getRangeObservable()
                    .observeOn(RxSchedulers.main())
                    .subscribe(this::doOnRangeAdded));
            subscriptions.add(getModel().getLoadingMoreObservable()
                    .observeOn(RxSchedulers.main())
                    .subscribe(this::doOnLoadingMore));
            subscriptions.add(getModel().getMapReadyObservable()
                    .observeOn(RxSchedulers.main())
                    .subscribe(this::doOnMapLoaded));
        }
    }

    public void onStop() {
        subscriptions.clear();
    }

    private void doOnListChanged(List<PaymentHistory> list) {
        // list is passed by reference from model
        // any changes in source list MUST BE followed by
        // adapter notify*** calls
        this.list = list;
        notifyDataSetChanged();
    }

    private void doOnRangeAdded(AdapterItemChange change) {
        notifyItemRangeInserted(change.getPosition(), change.getCount());
    }

    private void doOnLoadingMore(AdapterItemChange change) {
        if (change.isRemoveAction()) {
            notifyItemRemoved(change.getPosition());
        } else if (change.isAddAction()) {
            notifyItemInserted(change.getPosition());
        }
    }

    private void doOnMapLoaded(int position) {
        notifyItemChanged(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == LOADING) {
            return new LoadingViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_loading, parent, false));
        } else {
            return new BindingViewHolder(DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_trip_history_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder.getItemViewType() == ITEM) {
            FragmentTripHistoryItemBinding binding = ((BindingViewHolder) holder).getBinding();
            PaymentHistory history = list.get(position);
            binding.setHistory(history);
            if (history.getMapUrl() == null && App.getDataManager().getTripHistoryModel() != null) {
                App.getDataManager().getTripHistoryModel().loadMapForIndex(position);
            }
            binding.executePendingBindings();
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position) == PaymentHistory.empty() ? LOADING : ITEM;
    }

    private TripHistoryModel getModel() {
        return App.getDataManager().getTripHistoryModel();
    }

    @BindingAdapter("tripHistoryImageUrl")
    public static void loadImage(ImageView view, String imageUrl) {
        if (App.getDataManager().getTripHistoryModel() != null) {
            App.getDataManager().getTripHistoryModel().loadMapIntoImageView(view, imageUrl);
        }
    }

    private class BindingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private FragmentTripHistoryItemBinding binding;

        public FragmentTripHistoryItemBinding getBinding() {
            return binding;
        }

        BindingViewHolder(FragmentTripHistoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.binding.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (App.getDataManager().getTripHistoryModel() != null) {
                int index = getAdapterPosition();
                boolean isMapLoaded = App.getDataManager().getTripHistoryModel().isMapLoadedForIndex(index);
                Bitmap bitmap = null;
                Drawable drawable = binding.imageRideMap.getDrawable();
                if (drawable instanceof BitmapDrawable && isMapLoaded) {
                    bitmap = ((BitmapDrawable) drawable).getBitmap();
                }
                App.getDataManager().getTripHistoryModel().selectMapForIndex(index, bitmap);
            }
        }
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {

        LoadingViewHolder(View itemView) {
            super(itemView);
        }
    }
}
