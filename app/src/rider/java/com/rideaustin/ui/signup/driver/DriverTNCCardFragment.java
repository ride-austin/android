package com.rideaustin.ui.signup.driver;

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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.DriverRegistration;
import com.rideaustin.api.config.TncCard;
import com.rideaustin.databinding.DriverTNCCardBinding;
import com.rideaustin.models.DriverRegistrationData;
import com.rideaustin.ui.common.TakePhotoFragment;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.ui.utils.MenuUtil;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.DateHelper;
import com.rideaustin.utils.ImageHelper;
import com.rideaustin.utils.toast.RAToast;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;
import java.util.Date;


/**
 * Created by rost on 8/9/16.
 */
public class DriverTNCCardFragment extends BaseDriverSignUpFragment implements DriverTNCCardViewModel.DriverTNCCardListener {
    public static final String TYPE_KEY = "TYPE_KEY";
    private DriverTNCCardBinding binding;
    private BottomSheetBehavior bottomSheetBehavior;
    private MenuItem nextMenuItem;
    private DriverTNCCardViewModel viewModel;
    private Target<Bitmap> tncCardTarget;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_tnc_card, container, false);
        viewModel = new DriverTNCCardViewModel(getSignUpInteractor(), this);
        viewModel.setTNCCardSide(getCurrentType());
        setHasHelpWidget(true);
        setHasOptionsMenu(true);

        final DriverRegistration driverRegistration = viewModel.getDriverRegistration();
        final TncCard tncCard = driverRegistration.getTncCard();
        setToolbarTitle(tncCard.getHeader());
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
        takePhotoFragment.setTakePhotoListener(viewModel);
        replaceFragment(takePhotoFragment, R.id.edit_picture_bottom, false);

    }

    private void onSelectExpirationDateClicked() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = DatePickerDialog.newInstance(onExpirationDateSetListener, year, month, day);
        dialog.setMinDate(calendar);
        dialog.show(getActivity().getFragmentManager(), "ExpirationDateSelector");
    }

    private void showSelectedDate() {
        Date expirationDate = getDriverData().getDriverTncCardExpirationDate();
        if (expirationDate == null) {
            binding.selectExpirationDateView.setText(R.string.select);
        } else {
            binding.selectExpirationDateView.setText(DateHelper.dateToSimpleDateFormat(expirationDate));
        }
    }

    private DatePickerDialog.OnDateSetListener onExpirationDateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
        getDriverData().setDriverTncCardExpirationDate(DateHelper.getDate(year, monthOfYear, dayOfMonth));
        showSelectedDate();
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next:
                viewModel.onContinue();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        nextMenuItem = MenuUtil.inflateNextMenu(menu, inflater, true);
    }

    private String getCurrentType() {
        Bundle args = getArguments();
        return args.getString(TYPE_KEY);
    }

    private String getTncCardTitle() {
        return viewModel.isBackSide()
                ? viewModel.getDriverRegistration().getTncCard().getTitle1Back()
                : viewModel.getDriverRegistration().getTncCard().getTitle1();
    }

    private String getTncCardText() {
        return viewModel.isBackSide()
                ? viewModel.getDriverRegistration().getTncCard().getText1Back()
                : viewModel.getDriverRegistration().getTncCard().getText1();
    }

    private void showMissingTncCardDialog() {
        String message = getString(R.string.missing_tnc_card_photo_warning, getTncCardTitle());
        final MaterialDialog dialog = MaterialDialogCreator.createOkSkipDialog(message, getActivity()).build();
        dialog.setOnShowListener(dialogInterface -> {
            final MDButton negativeButton = dialog.getActionButton(DialogAction.NEGATIVE);
            negativeButton.setOnClickListener(v -> {
                // skip
                if (!viewModel.isBackSide()) {
                    getSignUpInteractor().setFrontTNCSkipped(true);
                }
                dialog.dismiss();
                notifyCompleted();
            });
        });
        dialog.show();
    }

    private void showMissingTncCardExpirationDateDialog() {
        String message = getString(R.string.missing_tnc_card_expiration_date_warning);
        final MaterialDialog dialog = MaterialDialogCreator.createOkSkipDialog(message, getActivity()).build();
        dialog.setOnShowListener(dialogInterface -> {
            final MDButton negativeButton = dialog.getActionButton(DialogAction.NEGATIVE);
            negativeButton.setOnClickListener(v -> {
                // skip
                dialog.dismiss();
                notifyCompleted();
            });
        });
        dialog.show();
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

    @Override
    public void onStart() {
        super.onStart();
        viewModel.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.onStop();
        if (tncCardTarget != null) {
            Glide.with(App.getInstance()).clear(tncCardTarget);
        }
    }

    @Override
    public void onHideBottomSheet() {
        hideBottomSheet();
    }

    @Override
    public void onTncCardsCombined() {
        hideProgress();
        getSignUpInteractor().setFrontTNCSkipped(false);
        final DriverRegistrationData driverRegistrationData = getDriverData();
        if (viewModel.isBackSide()) {
            if (driverRegistrationData.getDriverTncCardBackImagePath() == null) {
                showMissingTncCardDialog();
            } else if (driverRegistrationData.getDriverTncCardExpirationDate() == null) {
                showMissingTncCardExpirationDateDialog();
            } else {
                notifyCompleted();
            }
        } else {
            if (driverRegistrationData.getDriverTncCardFrontImagePath() == null) {
                showMissingTncCardDialog();
            } else if (viewModel.isLastScreen() && driverRegistrationData.getDriverTncCardExpirationDate() == null) {
                showMissingTncCardExpirationDateDialog();
            } else {
                notifyCompleted();
            }
        }
    }

    @Override
    public void onTncCardCombineFailed(final String message) {
        hideProgress();
        RAToast.showShort(message);
    }

    @Override
    public void onShowProgress() {
        showProgress();
    }

    @Override
    public void onHideProgress() {
        hideProgress();
    }

    @Override
    public void onTNCSelected(@Constants.TNCCardSide final String side, final String filePath) {
        tncCardTarget = ImageHelper.loadImageIntoView(binding.tncImage, filePath);
    }

    @Override
    protected void clearState() {
        super.clearState();
        getSignUpInteractor().setFrontTNCSkipped(false);

    }
}
