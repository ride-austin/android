package com.rideaustin.ui.signup;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.view.View;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.User;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseActivity;
import com.rideaustin.base.Transition;
import com.rideaustin.databinding.CreateProfileBinding;
import com.rideaustin.models.UserRegistrationData;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.TakePhotoFragment;
import com.rideaustin.ui.utils.ProfileValidator;
import com.rideaustin.utils.ImageHelper;
import com.rideaustin.utils.KeyboardUtil;
import com.rideaustin.utils.localization.AndroidLocalizer;

import rx.Observable;

/**
 * Created by kshumelchyk on 6/30/16.
 */
public class CreateProfileActivity extends BaseActivity implements TakePhotoFragment.TakePhotoListener {
    private CreateProfileBinding binding;
    private BottomSheetBehavior bottomSheetBehavior;
    private UserRegistrationData userData;

    private ProfileValidator validator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_profile);
        if (savedInstanceState != null) {
            App.getDataManager().restoreUserRegistrationData(savedInstanceState);
        }
        userData = App.getDataManager().getUserRegistrationData();

        validator = new ProfileValidator(new AndroidLocalizer(this));

        View editPictureBottomView = binding.editPictureBottom;
        bottomSheetBehavior = BottomSheetBehavior.from(editPictureBottomView);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        binding.agreement.setText(getString(R.string.agreement, App.getCityName()));

        binding.terms.setOnClickListener(view -> {
            Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(App.getConfigurationManager().getLastConfiguration()
                    .getGeneralInformation().getLegalRider()));
            startActivity(browser);
        });

        binding.profile.setOnClickListener(view -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            KeyboardUtil.hideKeyBoard(CreateProfileActivity.this, binding.profile);
        });

        final TakePhotoFragment takePhotoFragment = TakePhotoFragment.newInstance();
        takePhotoFragment.setTakePhotoListener(this);
        replaceFragment(takePhotoFragment, R.id.edit_picture_bottom, false, Transition.NONE);

        binding.done.setOnClickListener(v -> onDoneButtonClicked());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        App.getDataManager().saveUserRegistrationData(outState);
    }

    private void onDoneButtonClicked() {
        String firstName = binding.firstName.getText().toString().trim();
        String lastName = binding.lastName.getText().toString().trim();

        String firstNameError = validator.checkFirstName(firstName);
        binding.firstName.setError(firstNameError);

        String lastNameError = validator.checkLastName(lastName);
        binding.lastName.setError(lastNameError);

        if (firstNameError != null) {
            binding.firstName.requestFocus();
            return;
        }
        if (lastNameError != null) {
            binding.lastName.requestFocus();
            return;
        }

        userData.setFirstName(binding.firstName.getText().toString());
        userData.setLastName(binding.lastName.getText().toString());
        final Integer cityId = App.getConfigurationManager().getLastConfiguration().getCurrentCity().getCityId();
        userData.setCityId(cityId);
        App.getDataManager().setUserRegistrationData(userData);

        getEncodedImageObservable().flatMap(this::getSignUpObservable)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<User>(CreateProfileActivity.this) {
                    @Override
                    public void onNext(User user) {
                        super.onNext(user);
                        App.getDataManager().setCurrentUser(user);
                        startActivity(new Intent(CreateProfileActivity.this, CharityActivity.class));
                    }

                    @Override
                    public void onCompleted() {
                        super.onCompleted();
                        finishAffinity();
                    }
                });
    }


    private void hideBottomSheet() {
        int state = bottomSheetBehavior.isHideable()
                ? BottomSheetBehavior.STATE_HIDDEN
                : BottomSheetBehavior.STATE_COLLAPSED;
        bottomSheetBehavior.setState(state);
    }

    @Override
    public void onPhotoTaken(TakePhotoFragment.Source source, final String filePath) {
        userData.setImageFilePath(filePath);
        ImageHelper.loadImageIntoView(binding.profile, filePath);
        hideBottomSheet();
    }

    @Override
    public void onCanceled() {
        hideBottomSheet();
    }

    @Override
    public boolean shouldBeLoggedIn() {
        return false;
    }

    private Observable<String> getEncodedImageObservable() {
        return App.getDataManager().getBaseEncodedImage(userData.getImageFilePath())
                .subscribeOn(RxSchedulers.computation())
                .onErrorResumeNext(Observable.just(""));
    }

    private Observable<User> getSignUpObservable(String encodedImage) {
        return App.getDataManager().signUpEmail(
                userData.getEmail(),
                userData.getSocialId(),
                userData.getPassword(),
                userData.getFirstName(),
                userData.getLastName(),
                userData.getPhoneNumber(),
                encodedImage,
                userData.getCityId());
    }
}
