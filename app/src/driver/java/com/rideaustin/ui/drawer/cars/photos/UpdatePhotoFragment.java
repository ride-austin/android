package com.rideaustin.ui.drawer.cars.photos;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.DriverCarPhotoBinding;
import com.rideaustin.models.CarPhoto;
import com.rideaustin.ui.common.TakePhotoFragment;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.ui.utils.MenuUtil;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.ImageHelper;

/**
 * Created by crossover on 08/02/2017.
 */

public class UpdatePhotoFragment extends BaseFragment implements UpdatePhotoView, TakePhotoFragment.TakePhotoListener {

    UpdatePhotoFragmentCallback callback;
    private DriverCarPhotoBinding binding;
    private UpdatePhotoViewModel viewModel;
    private BottomSheetBehavior bottomSheetBehavior;
    private MenuItem nextMenuItem;
    private Target<Bitmap> photoTarget;

    public static Fragment newInstance() {
        Bundle args = new Bundle();
        UpdatePhotoFragment fragment = new UpdatePhotoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            callback = (UpdatePhotoFragmentCallback) context;
            callback.setTakePhotoListener(this);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("UpdatePhotoFragment can be attached only to UpdatePhotoFragmentCallback", e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_car_photo, container, false);
        viewModel = new UpdatePhotoViewModel(this, callback.getCar(), callback.getCarPhotoType());
        binding.carDetail.setImageResource(getPlaceHolderImage(viewModel.getCarPhotoType()));
        binding.textDetail.setText(getDescriptionText(viewModel.getCarPhotoType()));
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.openTakePhotoControl.setOnClickListener(v -> showBottomSheet());

        View editPictureBottomView = binding.editPictureBottom;

        bottomSheetBehavior = BottomSheetBehavior.from(editPictureBottomView);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        final TakePhotoFragment takePhotoFragment = TakePhotoFragment.newInstance();
        replaceFragment(takePhotoFragment, R.id.edit_picture_bottom, false);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next:
                notifyCompleted();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void notifyCompleted() {
        viewModel.uploadCarPhoto(getCallback());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.onStop();
        if (photoTarget != null) {
            Glide.with(binding.carDetail).clear(photoTarget);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        nextMenuItem = MenuUtil.inflateNextMenu(menu, inflater, viewModel.isImageSelected());
        nextMenuItem.setTitle(R.string.update);
    }

    @Override
    public void onPhotoSelected(String imagePath) {
        photoTarget = ImageHelper.loadImageIntoView(binding.carDetail, imagePath, R.drawable.icn_insurance);
    }

    @Override
    public void onPhotoUploaded(CarPhoto carPhoto) {
        MaterialDialog dialog = MaterialDialogCreator.createCenteredMessageDialog(App.getAppName(), getString(R.string.documents_updated), (AppCompatActivity) getActivity());
        dialog.setOnDismissListener(dialogInterface -> callback.onCompleted());
        dialog.show();
    }

    @Override
    public void onPhotoUploadFailed() {
        CommonMaterialDialogCreator.createNetworkFailDialog(getString(R.string.failed_car_photo_upload), getContext())
                .onPositive((dialog, which) -> viewModel.uploadCarPhoto(getCallback()))
                .show();
    }

    @Override
    public void onPhotoTaken(TakePhotoFragment.Source source, String filePath) {
        viewModel.onPhotoTaken(filePath);
        hideBottomSheet();
        nextMenuItem.setEnabled(viewModel.isImageSelected());
    }

    private void hideBottomSheet() {
        int state = bottomSheetBehavior.isHideable()
                ? BottomSheetBehavior.STATE_HIDDEN
                : BottomSheetBehavior.STATE_COLLAPSED;
        bottomSheetBehavior.setState(state);
    }

    @Override
    public void onCanceled() {
        hideBottomSheet();
    }

    @DrawableRes
    public static int getPlaceHolderImage(@Constants.CarPhotoType String carPhotoType) {
        switch (carPhotoType) {
            case Constants.CarPhotoType.BACK:
                return R.drawable.icon_car_back;
            case Constants.CarPhotoType.FRONT:
                return R.drawable.icon_car_front;
            case Constants.CarPhotoType.TRUNK:
                return R.drawable.icon_car_trunk;
            case Constants.CarPhotoType.INSIDE:
                return R.drawable.icon_car_inside;
            default:
                throw new IllegalArgumentException(carPhotoType + " is not supported");
        }
    }

    @StringRes
    public static int getDescriptionText(@Constants.CarPhotoType String carPhotoType) {
        switch (carPhotoType) {
            case Constants.CarPhotoType.BACK:
                return R.string.car_photo_back;
            case Constants.CarPhotoType.FRONT:
                return R.string.car_photo_front;
            case Constants.CarPhotoType.TRUNK:
                return R.string.car_photo_trunk;
            case Constants.CarPhotoType.INSIDE:
                return R.string.car_photo_inside;
            default:
                throw new IllegalArgumentException(carPhotoType + " is not supported");
        }
    }
}
