package com.rideaustin.ui.map;

import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.etiennelawlor.discreteslider.library.ui.DiscreteSeekBar;
import com.etiennelawlor.discreteslider.library.ui.DiscreteSlider;
import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.SpecialFee;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseActivity;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentMultipleCarsBinding;
import com.rideaustin.databinding.FragmentMultipleCarsPricingDialogBinding;
import com.rideaustin.databinding.SpecialFeeRowLayoutBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.estimate.FareEstimateActivityHelper;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.utils.ViewUtils;
import com.rideaustin.utils.location.LocationHelper;
import com.rideaustin.utils.toast.RAToast;

import java.util.Collections;
import java.util.List;

import java8.util.stream.StreamSupport;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

/**
 * Created by vokol on 21.07.2016.
 */
public class MultipleCarFragment extends BaseFragment implements DiscreteSlider.OnDiscreteSliderChangeListener, MultipleCarViewModel.MultipleCarView {

    public static final int FIRST_ELEMENT = 0;
    public static final String PRICE_PREFIX = "$ ";
    public static final float VISIBLE_AREA_IN_DP = 95;

    private static final String TAG_SELECTED = "tag_selected";
    private static final String TAG_NOT_SELECTED = "tag_not_selected'";
    private static final int LABEL_SELECTED_Y = (int) ViewUtils.dpToPixels(2);
    private static final int LABEL_NORMAL_Y = (int) ViewUtils.dpToPixels(16);
    private static final int LABEL_HEIGHT = (int) ViewUtils.dpToPixels(30);
    private static final int LABEL_H_MARGIN = (int) ViewUtils.dpToPixels(1);
    private static final int SELECT_ANIMATION_MS = 250;

    private FragmentMultipleCarsBinding binding;
    private CarCategoryListener categoryListener;
    private DiscreteSeekBar carTypesSeekBar;
    private BehaviorSubject<List<RequestedCarType>> carTypes = BehaviorSubject.create(Collections.emptyList());
    private BottomSheetBehavior bottomSheetBehavior;
    private MultipleCarViewModel viewModel;
    private RequestedCarType carType;
    private LatLng pickupLocation;
    private Subscription carTypesSubscription = Subscriptions.empty();

    public static MultipleCarFragment newInstance(@Nullable CarCategoryListener categoryListener, MapViewModel mapViewModel) {
        MultipleCarFragment fragment = new MultipleCarFragment();
        fragment.categoryListener = categoryListener;
        fragment.viewModel = new MultipleCarViewModel(fragment, mapViewModel);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_multiple_cars, container, false);
        binding.getRoot().setVisibility(View.GONE);
        binding.setViewModel(viewModel);
        binding.setHandler(this);
        bottomSheetBehavior = BottomSheetBehavior.from(binding.mainContent);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setPeekHeight((int) ViewUtils.dpToPixels(VISIBLE_AREA_IN_DP));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        binding.markImage.setRotation(180);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    binding.markImage.setRotation(180);
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    binding.markImage.setRotation(0);
                }
                updateBottomOffset();
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                binding.markImage.setRotation(180 * slideOffset - 180);
            }
        });
        setPriorityCategories();
        binding.getRoot().setVisibility(View.INVISIBLE);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.onStart();
        binding.mainContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                subscribeToCarTypes();
                updateBottomOffset();
                binding.mainContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && carTypes.hasValue()) {
            updateBottomOffset();
            updateSlider(carTypes.getValue());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.onStop();
        carTypesSubscription.unsubscribe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.clear();
    }

    @Override
    public void onPositionChanged(int position) {
        Timber.d("::onPositionChanged:: Position: %d", position);
        RequestedCarType newCarType = getCarType(position);
        if (carType == newCarType) {
            return;
        }
        carType = newCarType;
        if (carType == null) {
            // RA-13057
            return;
        }

        viewModel.setCarIcon(carType, binding.carTypesSlider);
        viewModel.setMaxPeopleValue(carType.getMaxPersons());
        carTypesSeekBar.setPosition(position);
        updateCarTypeLabels(position);
        App.getDataManager().setCarType(carType);
        App.getDataManager().setSliderSelectedCarType(carType);
        carTypesSeekBar.setContentDescription(carType.getTitle());
        if (categoryListener != null && !viewModel.getMapViewModel().isActiveRide()) {
            categoryListener.onCarCategorySelected(carType);
        }
    }

    @Override
    public void onDragPositionChanged(int position) {
        updateCarTypeLabels(position);
    }

    public void setPriorityCategories() {
        // RA-9677: this method can be called
        // when fragment is not attached to activity
        // or view is not created yet
        // Not to miss anything, this is also called in onCreateView
        if (binding != null && getContext() != null) {
            updatePriorityIndicator();
        }
    }

    private RequestedCarType getCarType(int index) {
        List<RequestedCarType> types = carTypes.getValue();
        if (types == null || types.isEmpty()) {
            return null;
        }
        if (index < 0) {
            index = 0;
        } else if (index >= types.size()) {
            index = types.size() - 1;
        }
        return types.get(index);
    }

    private void updatePriorityIndicator() {
        Drawable drawable = null;
        int childCount = binding.carTypesLabelContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            RequestedCarType carType = getCarType(i);
            if (carType == null) {
                // RA-13331
                return;
            }
            boolean priorityEnabled = isPriorityEnabled(carType);
            if (priorityEnabled && drawable == null) {
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_p_inside_circle_small);
            }
            TextView tv = (TextView) binding.carTypesLabelContainer.getChildAt(i);
            boolean hasDrawable = tv.getCompoundDrawables()[1] != null;
            if (priorityEnabled != hasDrawable) {
                tv.setCompoundDrawablesWithIntrinsicBounds(null, priorityEnabled ? drawable : null, null, null);
            }
        }
    }

    public void setCarTypes(@NonNull final List<RequestedCarType> carTypes, LatLng pickupLocation) {
        this.pickupLocation = pickupLocation;
        if (this.carTypes.getValue() != null && this.carTypes.getValue().equals(carTypes)) {
            Timber.d("::setCarTypes:: No change, ignoring.");
            return;
        }
        this.carTypes.onNext(carTypes);
    }

    private void nextCarType() {
        binding.carTypesSlider.setPosition(binding.carTypesSlider.getPosition() + 1);
        onPositionChanged(binding.carTypesSlider.getPosition());
    }

    private void prevCarType() {
        binding.carTypesSlider.setPosition(binding.carTypesSlider.getPosition() - 1);
        onPositionChanged(binding.carTypesSlider.getPosition());
    }

    private int restoreCarIndex(List<RequestedCarType> requestedCarTypes) {
        String title = App.getDataManager()
                .getSliderSelectedCarType()
                .map(RequestedCarType::getTitle)
                .orElse(null);
        return StreamSupport.stream(requestedCarTypes)
                .filter(carType -> carType.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .map(requestedCarTypes::indexOf)
                .orElse(FIRST_ELEMENT);
    }

    private void prepareCarLabels() {
        int size = carTypes.getValue().size();
        int position = binding.carTypesSlider.getPosition();
        binding.carTypesLabelContainer.removeAllViews();

        int width = binding.carTypesLabelContainer.getMeasuredWidth();
        int labelPrefWidth = width / size;
        float sideMargins = (float) labelPrefWidth / 2;
        binding.carTypesSlider.setBackdropLeftMargin(sideMargins);
        binding.carTypesSlider.setBackdropRightMargin(sideMargins);

        float interval = (width - labelPrefWidth) / (size - 1);
        int labelWidth = labelPrefWidth;
        labelWidth -= 2 * LABEL_H_MARGIN;
        float fontSize = getFontSize(labelWidth);

        for (int i = 0; i < size; i++) {
            RequestedCarType carType = getCarType(i);
            if (carType == null) {
                // RA-13331
                return;
            }

            TextView tv = (TextView) getLayoutInflater().inflate(R.layout.tv_slider_car_label, null);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(labelWidth,
                    LABEL_HEIGHT);
            if (carType.getTitle().contains("\n") || carType.getTitle().contains(" ")) {
                tv.setMaxLines(2);
                tv.setSingleLine(false);
            } else {
                tv.setMaxLines(1);
                tv.setSingleLine(true);
            }

            tv.setLayoutParams(layoutParams);
            tv.setText(carType.getTitle());
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            tv.setX(sideMargins + (i * interval) - labelWidth / 2);
            setCarLabelSelected(tv, i == position, false);
            binding.carTypesLabelContainer.addView(tv);
        }
    }

    private float getFontSize(int labelWidth) {
        int size = carTypes.getValue().size();
        String longestText = "";
        for (int i = 0; i < size; i++) {
            RequestedCarType carType = getCarType(i);
            if (carType == null) {
                continue;
            }
            String[] parts = carType.getTitle().split("\n|\\s");
            for (String part : parts) {
                if (part.length() > longestText.length()) {
                    longestText = part;
                }
            }
        }
        // Unfortunately, AutoSize text can't be used as it doesn't yet support custom fonts
        float fontSize = 11;
        float minSize = 8;
        float step = 0.1f;
        TextView tv = (TextView) getLayoutInflater().inflate(R.layout.tv_slider_car_label, null);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        tv.setText(longestText);
        tv.measure(0, 0);
        while (tv.getMeasuredWidth() > labelWidth) {
            fontSize -= step;
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            tv.measure(0, 0);
            if (fontSize < minSize) {
                break;
            }
        }
        return Math.max(minSize, fontSize);
    }

    private void updateCarTypeLabels(int position) {
        int childCount = binding.carTypesLabelContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            TextView carLabel = (TextView) binding.carTypesLabelContainer.getChildAt(i);
            boolean selected = i == position;
            setCarLabelSelected(carLabel, selected, true);
        }
    }

    private void setCarLabelSelected(View view, boolean selected, boolean animate) {
        if (view.getTag() != null) {
            boolean oldSelected = view.getTag().equals(TAG_SELECTED);
            if (oldSelected == selected) {
                return;
            }
        }
        if (selected) {
            view.setTag(TAG_SELECTED);
            if (animate) {
                view.animate().y(LABEL_SELECTED_Y)
                        .setDuration(SELECT_ANIMATION_MS)
                        .setInterpolator(new DecelerateInterpolator());
            } else {
                view.setY(LABEL_SELECTED_Y);
            }
        } else {
            view.setTag(TAG_NOT_SELECTED);
            if (animate) {
                view.animate().y(LABEL_NORMAL_Y)
                        .setDuration(SELECT_ANIMATION_MS)
                        .setInterpolator(new DecelerateInterpolator());
            } else {
                view.setY(LABEL_NORMAL_Y);
            }
        }
    }

    private boolean isPriorityEnabled(RequestedCarType carType) {
        return App.getDataManager().isSurge(carType.getCarCategory(), viewModel.getMapViewModel().getPickupLocation());
    }

    private void setupSlider(final int sliderSize) {
        binding.carTypesSlider.setTickMarkCount(sliderSize);
        // RA-9382: need access to car slider in accessibility mode
        carTypesSeekBar = binding.carTypesSlider.getSeekbar();
        carTypesSeekBar.setAccessibilityDelegate(new View.AccessibilityDelegate() {

            @Override
            public boolean performAccessibilityAction(View host, int action, Bundle args) {
                switch (action) {
                    case AccessibilityNodeInfo.ACTION_SCROLL_FORWARD:
                        nextCarType();
                        return true;
                    case AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD:
                        prevCarType();
                        return true;
                }
                return super.performAccessibilityAction(host, action, args);
            }
        });
        carTypesSeekBar.setOnClickListener(v -> {
            // To receive focus on tap in accessibility mode
        });

        binding.carTypesSlider.setOnDiscreteSliderChangeListener(this);
    }

    private void subscribeToCarTypes() {
        if (!isAttached()) {
            return;
        }
        carTypesSubscription.unsubscribe();
        carTypesSubscription = carTypes
                .observeOn(RxSchedulers.main())
                .subscribe(requestedCarTypes -> {
                    updateSlider(requestedCarTypes);
                    if (!requestedCarTypes.isEmpty()) {
                        binding.getRoot().setVisibility(View.VISIBLE);
                    }
                }, throwable -> Timber.w(throwable, "error while setting up car slider"));
    }

    private void updateSlider(List<RequestedCarType> types) {
        if (types != null && !types.isEmpty()) {
            setupSlider(types.size());
            prepareCarLabels();
            onPositionChanged(restoreCarIndex(types));
            updatePriorityIndicator();
        }
    }

    public void updateBottomOffset() {
        if (!isAdded() || isHidden() || binding == null) {
            return;
        }
        int measuredHeight;
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            measuredHeight = binding.mainContent.getMeasuredHeight();
        } else {
            measuredHeight = bottomSheetBehavior.getPeekHeight();
        }
        if (measuredHeight > 0) {
            viewModel.getMapViewModel().postBottomOffset(measuredHeight);
        }
    }

    @Override
    public void onSurgeUpdated() {
        updatePriorityIndicator();
    }

    public interface CarCategoryListener {
        void onCarCategorySelected(RequestedCarType carType);
    }

    // used by android bindings
    public void onViewPricingClicked(final View v) {
        showPricingDetailsDialog();
    }

    // used by android bindings
    public void onFareEstimateClicked(final View v) {
        FareEstimateActivityHelper.startFareEstimateActivity(getActivity(), viewModel.getMapViewModel());
    }

    private void showPricingDetailsDialog() {
        if (LocationHelper.isLocationNotEmpty(pickupLocation)) {
            if (carType != null) {
                Integer cityId = App.getConfigurationManager().getLastConfiguration().getCurrentCity().getCityId();
                App.getDataManager().getRidesService().getSpecialFees(pickupLocation.latitude, pickupLocation.longitude, carType.getCarCategory(), cityId)
                        .subscribeOn(RxSchedulers.network())
                        .observeOn(RxSchedulers.main())
                        .subscribe(new ApiSubscriber2<List<SpecialFee>>((BaseActivity) getActivity()) {
                            @Override
                            public void onNext(List<SpecialFee> specialFees) {
                                LayoutInflater inflater = LayoutInflater.from(getContext());
                                final FragmentMultipleCarsPricingDialogBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_multiple_cars_pricing_dialog, null, false);
                                binding.processingFeeValue.setText(getFormattedPrice(carType.getProcessingFee()));
                                binding.selectedCategoryText.setText(carType.getTitle());
                                binding.baseFareValue.setText(getFormattedPrice(carType.getBaseFare()));
                                binding.perMileValue.setText(getFormattedPrice(carType.getRatePerMile()));
                                binding.perMinValue.setText(getFormattedPrice(carType.getRatePerMinute()));
                                binding.bookingFeeValue.setText(getFormattedPrice(carType.getBookingFee()));
                                binding.minValue.setText(getFormattedPrice(carType.getMinimumFare()));
                                binding.tncFeeValue.setText(Math.round(carType.getTncFeeRate()) + "%");

                                for (SpecialFee specialFee : specialFees) {
                                    SpecialFeeRowLayoutBinding feeRowLayoutBinding = DataBindingUtil.inflate(inflater, R.layout.special_fee_row_layout, null, false);
                                    feeRowLayoutBinding.feeValue.setText(getFormattedPrice(specialFee.getValue()));
                                    feeRowLayoutBinding.feeText.setText(specialFee.getTitle());
                                    feeRowLayoutBinding.feeDescription.setText(specialFee.getDescription());
                                    binding.specialFeeContainer.addView(feeRowLayoutBinding.getRoot());
                                }
                                MaterialDialogCreator.createPricingDetailsDialog(getActivity(), binding.getRoot(), false);
                            }
                        });
            }
        } else {
            RAToast.show(R.string.no_location_when_show_pricing_msg, Toast.LENGTH_LONG);
        }


    }


    private String getFormattedPrice(final String price) {
        return PRICE_PREFIX + price;
    }
}
