package com.rideaustin.ui.drawer.documents.tnc;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.rideaustin.R;
import com.rideaustin.api.config.TncCard;
import com.rideaustin.api.model.Document;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.DriverTNCCardBinding;
import com.rideaustin.ui.common.TakePhotoFragment;
import com.rideaustin.ui.utils.MenuUtil;
import com.rideaustin.utils.CommonConstants;
import com.rideaustin.utils.DateHelper;
import com.rideaustin.utils.ImageHelper;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by crossover on 22/01/2017.
 */

public class DriverTNCCardFragment extends BaseFragment implements DriverTNCCardView {
    public static final String TYPE_KEY = "TYPE_KEY";
    private DriverTNCCardBinding binding;
    private BottomSheetBehavior bottomSheetBehavior;
    private MenuItem nextMenuItem;
    private DriverTNCCardViewModel viewModel;
    private Target<Bitmap> tncCardTarget;
    private DriverTNCCardFragmentCallback listener;
    private boolean isDocumentLoaded;

    public static DriverTNCCardFragment newInstance(@CommonConstants.TNCCardSide String side) {
        Bundle args = new Bundle();
        args.putString(TYPE_KEY, side);
        DriverTNCCardFragment fragment = new DriverTNCCardFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_tnc_card, container, false);
        if (viewModel == null) {
            viewModel = new DriverTNCCardViewModel(this, listener.getUpdateTNCViewModel(), getCurrentType());
        }
        setHasOptionsMenu(true);

        final TncCard tncCard = viewModel.getTncCard();
        binding.tncIcnUpload.setText(tncCard.getAction1());
        binding.tncIcnUpload.setVisibility(View.VISIBLE);
        binding.tncImage.setVisibility(View.GONE);

        binding.tncCardTitle1.setText(getTncCardTitle());
        binding.tncCardText1.setText(getTncCardText());

        if (viewModel.isBackSide()) {
            binding.tncCardTitle2.setVisibility(View.GONE);
            binding.tncCardText2.setVisibility(View.GONE);
        } else {
            binding.tncCardTitle2.setText(tncCard.getTitle2());
            if (!TextUtils.isEmpty(tncCard.getText2())) {
                binding.tncCardText2.setText(Html.fromHtml(tncCard.getText2()));
                binding.tncCardText2.setMovementMethod(LinkMovementMethod.getInstance());
            }
            binding.tncCardTitle2.setVisibility(View.VISIBLE);
            binding.tncCardText2.setVisibility(View.VISIBLE);
        }

        binding.expirationDate.setVisibility(viewModel.isLastScreen() ? View.VISIBLE : View.GONE);
        isDocumentLoaded = false;
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.openTakePhotoControl.setOnClickListener(v -> showBottomSheet());
        if (viewModel.isLastScreen()) {
            binding.selectExpirationDateView.setOnClickListener(v -> onSelectExpirationDateClicked());
            showSelectedDate();
        }

        View editPictureBottomView = binding.editPictureBottom;

        bottomSheetBehavior = BottomSheetBehavior.from(editPictureBottomView);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        final TakePhotoFragment takePhotoFragment = TakePhotoFragment.newInstance();
        replaceFragment(takePhotoFragment, R.id.edit_picture_bottom, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.listener = (DriverTNCCardFragmentCallback) context;
            attached = true;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("DriverTNCCardFragment can be attached only to DriverTNCCardFragmentCallback", e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next:
                if (viewModel.isLastScreen()) {
                    listener.onSave();
                } else {
                    listener.onNext();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        nextMenuItem = MenuUtil.inflateNextMenu(menu, inflater, viewModel.isMenuActionEnabled());
        nextMenuItem.setTitle(viewModel.isLastScreen() ? R.string.save : R.string.next);
    }

    private String getCurrentType() {
        return getArguments().getString(TYPE_KEY);
    }

    private String getTncCardTitle() {
        return viewModel.isBackSide()
                ? viewModel.getTncCard().getTitle1Back()
                : viewModel.getTncCard().getTitle1();
    }

    private String getTncCardText() {
        return viewModel.isBackSide()
                ? viewModel.getTncCard().getText1Back()
                : viewModel.getTncCard().getText1();
    }

    private void showBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void hideBottomSheet() {
        int state = bottomSheetBehavior.isHideable()
                ? BottomSheetBehavior.STATE_HIDDEN
                : BottomSheetBehavior.STATE_COLLAPSED;
        bottomSheetBehavior.setState(state);
    }

    private void onSelectExpirationDateClicked() {
        Calendar calendar = Calendar.getInstance();
        if (viewModel.getExpirationDate() != null) {
            calendar.setTime(viewModel.getExpirationDate());
        }
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = DatePickerDialog.newInstance(onExpirationDateSetListener, year, month, day);
        dialog.setMinDate(Calendar.getInstance()); // today
        dialog.show(getActivity().getFragmentManager(), "ExpirationDateSelector");
    }

    private void showSelectedDate() {
        Date expirationDate = viewModel.getExpirationDate();
        if (expirationDate == null) {
            binding.selectExpirationDateView.setText(R.string.select);
        } else {
            binding.selectExpirationDateView.setText(DateHelper.dateToSimpleDateFormat(expirationDate));
            viewModel.setExpirationDate(expirationDate);
        }
        if (nextMenuItem != null) {
            nextMenuItem.setEnabled(viewModel.isMenuActionEnabled());
        }
    }

    private DatePickerDialog.OnDateSetListener onExpirationDateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
        viewModel.setExpirationDate(DateHelper.getDate(year, monthOfYear, dayOfMonth));
        nextMenuItem.setEnabled(viewModel.isMenuActionEnabled());
        showSelectedDate();
    };

    @Override
    public void onStart() {
        super.onStart();
        viewModel.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        // RA-14410: quite ugly fix. In general, whole documents need to be redesigned.
        if (!isDocumentLoaded && listener.getUpdateTNCViewModel().getDocument() != null) {
            onDocumentDownloaded(listener.getUpdateTNCViewModel().getDocument());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.onStop();
        if (tncCardTarget != null) {
            Glide.with(binding.tncImage).clear(tncCardTarget);
            tncCardTarget = null;
        }
    }

    @Override
    public void onHideBottomSheet() {
        hideBottomSheet();
    }

    @Override
    public void onTNCSelected(final String filePath) {
        tncCardTarget = ImageHelper.loadImageIntoView(binding.tncImage, filePath);
        binding.tncImage.setVisibility(View.VISIBLE);
        binding.tncIcnUpload.setVisibility(View.GONE);
    }

    public void onPhotoTaken(TakePhotoFragment.Source source, String filePath) {
        viewModel.onPhotoTaken(source, filePath);
        nextMenuItem.setEnabled(viewModel.isMenuActionEnabled());
    }

    public void onPhotoCanceled() {
        viewModel.onCanceled();
    }

    public void onDocumentDownloaded(Document document) {
        if (!viewModel.isImageSelected()) {
            if (!viewModel.isBackSide()) {
                // show existing tnc image (only on front side)
                isDocumentLoaded = true;
                loadTncImageFromDocument(document);
            }
            if (!viewModel.isExpirationDateSet()) {
                // show document expiration date is it was not changed by user already
                showSelectedDate();
            }
        }
    }

    private void loadTncImageFromDocument(Document document) {
        binding.tncImage.setVisibility(View.VISIBLE);
        binding.tncIcnUpload.setVisibility(View.GONE);
        tncCardTarget = ImageHelper.loadImageIntoViewWithDefaultProgress(binding.tncImage, document.getDocumentUrl(), R.drawable.icn_license, new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                binding.tncImage.setVisibility(View.GONE);
                binding.tncIcnUpload.setVisibility(View.VISIBLE);
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        });
    }

    public interface DriverTNCCardFragmentCallback {

        UpdateTNCViewModel getUpdateTNCViewModel();

        void onNext();

        void onSave();
    }
}
