package com.rideaustin.ui.terms;

import android.databinding.BindingAdapter;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.text.TextUtils;
import android.widget.ImageView;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.model.driver.CurrentTerms;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.models.TermsResponse;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.RxBaseViewModel;
import com.rideaustin.ui.utils.ResourceHelper;
import com.rideaustin.utils.ImageHelper;
import com.rideaustin.utils.SingleSubject;
import com.rideaustin.utils.TermsUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by Sergey Petrov on 24/05/2017.
 */

public class TermsViewModel extends RxBaseViewModel {

    public final ObservableField<String> logoUrl = new ObservableField<>("");
    public final ObservableField<String> termsDate = new ObservableField<>("");
    public final ObservableField<String> terms = new ObservableField<>("");
    public final ObservableBoolean termsChecked = new ObservableBoolean(false);
    public final ObservableBoolean termsLoading = new ObservableBoolean(false);

    private final SingleSubject<TermsResponse> badResponse = SingleSubject.create();
    private final SingleSubject<Boolean> finishSubject = SingleSubject.create();

    private CurrentTerms currentTerms;

    TermsViewModel() {
        loadTerms();
        listenToConfigChanges();
    }

    /**
     * Observable of bad terms response.
     * May emit {@code NULL} values in case bad response is erased (and probably next terms request started).
     * @return observable of {@link TermsResponse}
     */
    Observable<TermsResponse> getBadResponseObservable() {
        return badResponse.asObservable();
    }

    Observable<Boolean> getFinishObservable() {
        return finishSubject.asObservable();
    }

    public void acceptDriverTerms() {
        untilDestroy(App.getDataManager().acceptDriverTerms(currentTerms.getId())
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                // using deprecated ApiSubscriber intentionally
                // until we decide what to do with showing both toast and alert
                // when there is no internet
                .subscribe(new ApiSubscriber<Driver>(this) {
                    @Override
                    public void onNext(Driver driver) {
                        App.getDataManager().setCurrentDriver(driver);
                        finishSubject.onNext(true);
                    }
                }));
    }

    public void loadTerms() {
        if (TextUtils.isEmpty(terms.get())) {
            currentTerms = App.getConfigurationManager().getLastConfiguration().getCurrentTerms();
            untilDestroy(TermsUtils.obtainTermsAndConditions(currentTerms.getUrl())
                    .delay(500, TimeUnit.MILLISECONDS, RxSchedulers.computation())
                    .subscribeOn(RxSchedulers.network())
                    .observeOn(RxSchedulers.main())
                    .doOnSubscribe(this::doOnTermsLoading)
                    .subscribe(this::doOnTermsLoaded, this::doOnTermsError));
        }
    }

    private void doOnTermsLoading() {
        termsLoading.set(true);
        termsChecked.set(false);
        terms.set("");
        badResponse.onNext(null);
    }

    private void doOnTermsLoaded(TermsResponse response) {
        termsLoading.set(false);
        termsChecked.set(false);
        if (response.isSuccessfull()) {
            terms.set(response.getTerms());
        } else {
            terms.set("");
            badResponse.onNext(response);
        }
    }

    private void doOnTermsError(Throwable error) {
        Timber.e(error, "This should never happen");
        termsLoading.set(false);
        termsChecked.set(false);
        terms.set("");
        badResponse.onNext(TermsResponse.withError(App.getInstance().getString(R.string.error_unknown), false));
    }

    private void listenToConfigChanges() {
        untilDestroy(App.getConfigurationManager()
                .getConfigurationUpdates()
                .subscribeOn(RxSchedulers.computation())
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnGlobalConfig));
    }

    private void doOnGlobalConfig(GlobalConfig config) {
        logoUrl.set(config.getGeneralInformation().getLogoUrl());
        termsDate.set(config.getCurrentTerms().getHumanReadableDate().toUpperCase());
    }

    @BindingAdapter("termsLogoUrl")
    public static void loadTermsLogoUrl(ImageView imageView, String url) {
        int placeholder = ResourceHelper.getWhiteLogoDrawableRes(App.getConfigurationManager().getLastConfiguration());
        if (!TextUtils.isEmpty(url)) {
            ImageHelper.loadImageIntoView(imageView, url, placeholder, placeholder);
        } else {
            imageView.setImageResource(placeholder);
        }
    }

}
