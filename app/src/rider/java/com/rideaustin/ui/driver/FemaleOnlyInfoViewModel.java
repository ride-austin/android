package com.rideaustin.ui.driver;


import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.Gender;
import com.rideaustin.api.model.User;
import com.rideaustin.api.model.driver.RequestedDriverType;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.toast.RAToast;

import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class FemaleOnlyInfoViewModel {

    public final ObservableField<String> title = new ObservableField<>("");
    public final ObservableField<String> subTitle = new ObservableField<>("");
    public final ObservableBoolean switchEnabled = new ObservableBoolean(true);
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private FemaleOnlyInfoView view;
    private RequestedDriverType femaleDriverType;
    private boolean editable = true;
    private boolean enabled = true;

    public FemaleOnlyInfoViewModel(final FemaleOnlyInfoView view) {
        this.view = view;

        view.getCallback().showProgress();
        subscriptions.add(App.getDataManager().getFemaleDriverType()
                .doOnTerminate(() -> view.getCallback().hideProgress())
                .subscribe(this::doOnFemaleDriverType,
                        throwable -> Timber.e(throwable, "can't fetch woman only categories")));
        subscriptions.add(App.getDataManager().getFemaleModeEditable()
                .distinctUntilChanged()
                .observeOn(RxSchedulers.main())
                .subscribe(editable -> {
                    this.editable = editable;
                    updateSwitch();
                    if (!editable) {
                        RAToast.showShort(R.string.female_only_cannot_edit);
                    }
                }));
    }

    void selectGender(String gender) {
        if (App.getDataManager().getUserGender().toString().equals(gender)) {
            doOnGenderChanged();
            return;
        }
        subscriptions.add(App.getDataManager().updateGender(Gender.fromString(gender))
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<User>(view.getCallback()) {
                    @Override
                    public void onNext(User user) {
                        super.onNext(user);
                        doOnGenderChanged();
                    }
                }));
    }

    void onChooseGender() {
        String gender = App.getDataManager().getUserGender().toString();
        view.getCallback().showProgress();
        subscriptions.add(App.getDataManager()
                .getGenderSelection()
                .observeOn(RxSchedulers.main())
                .doOnUnsubscribe(() -> view.getCallback().hideProgress())
                .subscribe(selection -> {
                    int index = selection.getOptions().indexOf(gender);
                    view.onGenderSelection(selection, index);
                }, throwable -> {
                    // RA-13061: protect against misconfiguration
                    Timber.e(throwable, throwable.getMessage());
                    RAToast.showShort(R.string.error_unknown);
                }));
    }

    void onDisabledSwitch() {
        if (!editable) {
            RAToast.showShort(R.string.female_only_cannot_edit);
        } else {
            validateGender();
        }
    }

    public void unsubscribe() {
        subscriptions.unsubscribe();
    }

    private void doOnFemaleDriverType(RequestedDriverType driverType) {
        this.femaleDriverType = driverType;
        title.set(driverType.getDisplayTitle());
        subTitle.set(driverType.getDisplaySubtitle());
        view.onTitleLoaded(driverType.getDisplayTitle());

        enabled = validateGender();
        updateSwitch();
    }

    private void doOnGenderChanged() {
        enabled = validateGender();
        updateSwitch();
    }

    private boolean validateGender() {
        if (femaleDriverType == null) {
            return false;
        }

        Gender gender = App.getDataManager().getUserGender();
        if (gender == Gender.UNKNOWN) {
            view.onGenderAlert(femaleDriverType.getUnknownGenderAlert());
            return false;
        }

        boolean isGenderAllowed = femaleDriverType.getEligibleGenders()
                .contains(gender.toString());
        if (!isGenderAllowed) {
            view.onGenderAlert(femaleDriverType.getIneligibleGenderAlert());
            return false;
        }

        return true;
    }


    private void updateSwitch() {
        switchEnabled.set(editable && enabled);
    }
}
