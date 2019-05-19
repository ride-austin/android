package com.rideaustin.ui.drawer.editprofile;

import android.databinding.BindingAdapter;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.EditText;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.Gender;
import com.rideaustin.api.model.User;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.RxBaseViewModel;
import com.rideaustin.utils.RxImageLoader;
import com.rideaustin.utils.SingleSubject;
import com.rideaustin.utils.toast.RAToast;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import timber.log.Timber;

/**
 * Created by ysych on 07.07.2016.
 */
public class EditProfileFragmentViewModel extends RxBaseViewModel {

    public final ObservableBoolean isNicknameEnabled =
            new ObservableBoolean(App.getDataManager().getUpdateUserDelegateInstance().isNicknameEnabled());
    public final ObservableBoolean emailVerified = new ObservableBoolean(true);
    public final ObservableField<User> user = new ObservableField<>();
    public final ObservableField<Drawable> userPhoto = new ObservableField<>();
    public final ObservableField<String> genderText = new ObservableField<>();
    private final ObservableField<Boolean> dirtyFlag = new ObservableField<>(false);
    private boolean ignoreDirtyFlagChanges = true;
    private Gender gender;

    private SingleSubject<Boolean> profileUpdateSubject = SingleSubject.create();
    private SingleSubject<SelectedGender> selectedGenderSubject = SingleSubject.create();
    private SingleSubject<String> alreadyUsedMobileSubject = SingleSubject.create();

    public EditProfileFragmentViewModel() {
        if (!App.getDataManager().isLoggedIn()) {
            // app is going to be redirected to login
            return;
        }
        user.set(App.getDataManager().getCurrentUser());
        loadAvatar();
        untilDestroy(App.getDataManager().getEmailVerifiedObservable()
                .subscribe(emailVerified::set));
        gender = App.getDataManager().getUserGender();
        genderText.set(gender != Gender.UNKNOWN ? App.capitalizeFirstLetter(gender.toString()) : null);
        // let data binding set values
        RxSchedulers.schedule(() -> ignoreDirtyFlagChanges = false, 100, TimeUnit.MILLISECONDS);
    }

    private void loadAvatar() {
        String url = App.getDataManager().getUpdateUserDelegateInstance().getPhotoUrl();
        untilDestroy(RxImageLoader.execute(new RxImageLoader.Request(url)
                .progress(R.drawable.rotating_circle)
                .target(userPhoto)
                .error(R.drawable.ic_user_icon)
                .circular(true)));
    }

    public Observable<SelectedGender> getSelectedGenderEvents() {
        return selectedGenderSubject.asObservable().onBackpressureLatest();
    }

    public Observable<String> getAlreadyUsedMobileEvents() {
        return alreadyUsedMobileSubject.asObservable().onBackpressureLatest();
    }

    void saveUser(User user) {
        user.setGender(gender.toString());
        App.getDataManager()
                .getUpdateUserDelegateInstance()
                .updateUserOnServer(user, this, profileUpdateSubject);
    }

    void sendImage(String filePath) {
        untilDestroy(App.getDataManager().getUpdateUserDelegateInstance()
                .updatePhoto(filePath, this, photoUrl -> loadAvatar()));
    }

    void setDirtyFlag(boolean value) {
        if (!ignoreDirtyFlagChanges) {
            dirtyFlag.set(value);
        }
    }

    boolean isPhoneNumberChanged(@NonNull String phoneNumber) {
        return !phoneNumber.equals(user.get().getPhoneNumber());
    }

    void onChooseGender() {
        showProgress();
        untilDestroy(App.getDataManager()
                .getGenderSelection()
                .observeOn(RxSchedulers.main())
                .doOnUnsubscribe(() -> hideProgress())
                .subscribe(selection -> {
                    int index = selection.getOptions().indexOf(gender.toString());
                    selectedGenderSubject.onNext(new SelectedGender(selection, index));
                }, throwable -> {
                    // RA-13061: protect against misconfiguration
                    Timber.e(throwable, throwable.getMessage());
                    RAToast.showShort(R.string.error_unknown);
                }));

    }

    void selectGender(String gender) {
        if (this.gender.toString().equals(gender)) {
            return;
        }
        this.gender = Gender.fromString(gender);
        genderText.set(App.capitalizeFirstLetter(gender));
        dirtyFlag.set(App.getDataManager().getUserGender() != this.gender);
    }

    public ObservableField<Boolean> getDirtyFlag() {
        return dirtyFlag;
    }

    @BindingAdapter("phoneNumber")
    public static void setPhoneNumber(EditText view, String phone) {
        if (TextUtils.isEmpty(phone)) {
            view.setText("+");
        } else {
            // First replace any non-digits except '+' sign
            phone = phone.replaceAll("[^\\d+]", "");
            // Since 00 at start means '+', replace that one too.
            if (phone.startsWith("00")) {
                phone = phone.replaceFirst("00", "+");
            }
            // If phone is still missing '+' sign at start, please add it.
            if (!phone.startsWith("+")) {
                phone = "+" + phone;
            }
            view.setText(phone);
        }
    }

    public void checkInUse(String mobile) {
        untilDestroy(App.getDataManager()
                .getAuthService()
                .isPhoneInUse(mobile)
                .observeOn(RxSchedulers.main())
                .subscribeOn(RxSchedulers.network())
                .subscribe(new ApiSubscriber2<Void>(this) {
                    @Override
                    public void onNext(Void aVoid) {
                        alreadyUsedMobileSubject.onNext(mobile);
                    }
                }));
    }

    public Observable<Boolean> getProfileUpdateEvents() {
        return profileUpdateSubject.asObservable();
    }
}
