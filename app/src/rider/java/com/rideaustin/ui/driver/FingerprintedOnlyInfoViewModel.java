package com.rideaustin.ui.driver;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.driver.RequestedDriverType;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.RxBaseViewModel;
import com.rideaustin.utils.toast.RAToast;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

/**
 * Created on 11/11/18.
 *
 * @author sdelaysam
 */
public class FingerprintedOnlyInfoViewModel extends RxBaseViewModel {

    public final ObservableField<String> title = new ObservableField<>("");
    public final ObservableField<String> subTitle = new ObservableField<>("");
    public final ObservableBoolean switchEnabled = new ObservableBoolean(false);

    private BehaviorSubject<String> titleSubject = BehaviorSubject.create();
    private boolean enabled = false;
    private boolean editable = true;

    public FingerprintedOnlyInfoViewModel() {
        showProgress();
        untilDestroy(App.getDataManager().getFingerprintedDriverType()
                .doOnTerminate(this::hideProgress)
                .subscribe(this::doOnFemaleDriverType,
                        throwable -> Timber.e(throwable, "can't fetch fingerprinted only categories")));
        untilDestroy(App.getDataManager().getFemaleModeEditable()
                .distinctUntilChanged()
                .observeOn(RxSchedulers.main())
                .subscribe(editable -> {
                    this.editable = editable;
                    updateSwitch();
                    if (!editable) {
                        RAToast.showShort(R.string.fingerprinted_only_cannot_edit);
                    }
                }));

    }

    Observable<String> observeTitle() {
        return titleSubject.distinctUntilChanged().serialize().onBackpressureLatest();
    }

    void onDisabledSwitch() {
        if (!editable) {
            RAToast.showShort(R.string.female_only_cannot_edit);
        }
    }

    private void doOnFemaleDriverType(RequestedDriverType driverType) {
        String title = driverType.getDisplayTitle() != null ? driverType.getDisplayTitle() : App.i().getString(R.string.title_fingerprinted_only);
        String subtitle = driverType.getDisplaySubtitle() != null ? driverType.getDisplaySubtitle() : App.i().getString(R.string.fingerprinted_subtitle);

        this.title.set(title);
        this.subTitle.set(subtitle);
        titleSubject.onNext(title);

        enabled = true;
        updateSwitch();
    }

    private void updateSwitch() {
        switchEnabled.set(editable && enabled);
    }

}
