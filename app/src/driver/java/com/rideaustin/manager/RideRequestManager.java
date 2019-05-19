package com.rideaustin.manager;

import android.content.Context;
import android.util.Pair;

import com.rideaustin.R;
import com.rideaustin.api.DataManager;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.model.CategoryChangeParams;
import com.rideaustin.api.model.Event;
import com.rideaustin.api.model.driver.ActiveDriver;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.api.model.driver.RequestedDriverType;
import com.rideaustin.engine.StateManager;
import com.rideaustin.ui.drawer.riderequest.RideRequestSummaryData;
import com.rideaustin.utils.CommonConstants;
import com.rideaustin.utils.RetryWhenNoNetwork;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import java8.util.Optional;
import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.rideaustin.utils.CommonConstants.DIRECT_CONNECT_DRIVER_TYPE;
import static com.rideaustin.utils.CommonConstants.UNEXPECTED_STATE_KEY;
import static com.rideaustin.utils.Constants.WOMEN_ONLY_DRIVER_TYPE;

/**
 * @author sdelaysam.
 */

public class RideRequestManager {

    private final Context context;
    private final StateManager stateManager;
    private final PrefManager prefManager;
    private final AppNotificationManager notificationManager;
    private DataManager dataManager;

    private final Subscription configSubscription;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    private int cityId;
    private BehaviorSubject<List<RequestedCarType>> carTypesSubject = BehaviorSubject.create();
    private BehaviorSubject<List<RequestedDriverType>> driverTypesSubject = BehaviorSubject.create();
    private PublishSubject<List<RequestedCarType>> availableCategoriesSubject = PublishSubject.create();
    private BehaviorSubject<List<RequestedCarType>> selectedCategoriesSubject = BehaviorSubject.create();

    public RideRequestManager(Context context,
                              ConfigurationManager configurationManager,
                              DataManager dataManager,
                              StateManager stateManager,
                              PrefManager prefManager,
                              AppNotificationManager notificationManager) {
        this.context = context;
        this.dataManager = dataManager;
        this.stateManager = stateManager;
        this.prefManager = prefManager;
        this.notificationManager = notificationManager;
        configSubscription = configurationManager.getConfigurationUpdates()
                .onBackpressureLatest()
                .subscribe(this::doOnGlobalConfig);
    }

    /**
     * Observe car types. May send request to server if there is no loaded car types.
     * <p/>
     * @return car types observable
     */
    public Observable<List<RequestedCarType>> getCarTypes() {
        Observable<List<RequestedCarType>> observable = carTypesSubject.asObservable();
        if (carTypesSubject.getValue() == null) {
            observable = observable.startWith(loadCarTypes());
        }
        return observable.filter(data -> data != null).distinctUntilChanged();
    }

    /**
     * Observe driver types. May send request to server if there is no loaded driver types.
     * <p/>
     * @return driver types observable
     */
    public Observable<List<RequestedDriverType>> getDriverTypes() {
        Observable<List<RequestedDriverType>> observable = driverTypesSubject.asObservable();
        if (driverTypesSubject.getValue() == null) {
            observable = observable.startWith(loadDriverTypes());
        }
        return observable.filter(data -> data != null).distinctUntilChanged();
    }

    /**
     * Observe available car types. If {@link #isWomenOnly()} is TRUE, will return
     * intersection of categories in {@link Driver#getSelectedCar()} and categories available
     * for {@link com.rideaustin.utils.Constants#WOMEN_ONLY_DRIVER_TYPE}.
     * Same for {@link #isDirectConnect()} and {@link com.rideaustin.utils.Constants#DIRECT_CONNECT_DRIVER_TYPE}
     * Otherwise, will just return all car types from driver's selected car.
     * Note: stream will react on {@link #setWomenOnly(boolean)}
     * <p />
     * @param forceRemote indicates whether to query {@link Driver} from server
     *                    even if already loaded
     * @return car types observable
     */
    public Observable<List<RequestedCarType>> getAvailableCarTypes(boolean forceRemote) {
        Observable<List<RequestedCarType>> observable;
        if (isWomenOnly()) {
            observable = getCustomAvailableCarTypes(WOMEN_ONLY_DRIVER_TYPE, forceRemote);
        } else if (isDirectConnect()) {
            observable = getCustomAvailableCarTypes(DIRECT_CONNECT_DRIVER_TYPE, forceRemote);
        } else {
            observable = getAllAvailableCarTypes(forceRemote);
        }
        observable = observable.doOnNext(availableCategoriesSubject::onNext);
        // NOTE: not using distinctUntilChanged() here
        // When WOMEN_ONLY changed available car types may stay the same
        // and subscribers should be notified.
        // May be refactored to use separate observable for WOMEN_ONLY changes.
        return availableCategoriesSubject.startWith(observable)
                .filter(data -> data != null);
    }

    /**
     * Return all car types for specified type.
     * Note: It doesn't check whether driver is eligible to receive these requests.
     * @return car types observable
     */
    public Observable<List<RequestedCarType>> getCustomTypeCarTypes(String customType) {
        return getDriverTypes().take(1)
                .flatMapIterable(list -> list)
                .filter(type -> customType.equalsIgnoreCase(type.getName()))
                .map(RequestedDriverType::getAvailableInCategories)
                .flatMap(this::getCarTypesForCategories)
                .firstOrDefault(new ArrayList<>());
    }


    /**
     * Observe selected car types. First time will try to fetch selected categories from server.
     * Same if {@code forceRemote} is TRUE. Otherwise, will return intersection of cached selected
     * car categories and car categories available for selected car.
     * If cached are empty, will return all categories for selected car.
     * <p/>
     * @param forceRemote indicates whether query {@link ActiveDriver} from server
     *                    even if already loaded
     * @return selected types observable
     */
    public Observable<List<RequestedCarType>> getSelectedCarTypes(boolean forceRemote) {
        Observable<List<RequestedCarType>> observable = selectedCategoriesSubject.asObservable();
        forceRemote = forceRemote || selectedCategoriesSubject.getValue() == null;
        if (forceRemote) {
            observable = observable.startWith(loadSelectedCarTypes(true));
        }
        return observable.filter(data -> data != null).distinctUntilChanged();
    }

    /**
     * Returns selected categories for driver. If driver or his selection is empty
     * returns {@link com.rideaustin.utils.CommonConstants.CarCategory#REGULAR}
     * <p/>
     * @return selected categories
     */
    public Set<String> getSelectedCategories() {
        if (dataManager.getDriver().isPresent()) {
            return getSelectedCategoriesForDriver(dataManager.getDriver().get());
        }
        return new HashSet<>(Collections.singletonList(CommonConstants.CarCategory.REGULAR));
    }

    /**
     * Set women only mode.
     * Will update stream of {@link #getAvailableCarTypes(boolean)} appropriately
     * <p/>
     * @param womenOnly womenOnly indicator
     */
    public void setWomenOnly(boolean womenOnly) {
        if (!isWomenOnlyAvailable() || isWomenOnlyEnabled() == womenOnly
                || !dataManager.getDriver().isPresent()) {
            return;
        }

        prefManager.setWomanOnlyModeEnabled(dataManager.getDriver().get(), womenOnly);
        subscriptions.add(getAvailableCarTypes(false).take(1)
                .subscribe(strings -> {}, Timber::e));
    }

    /**
     * Set direct connect model.
     * Will update stream of {@link #getAvailableCarTypes(boolean)} appropriately
     * <p/>
     * @param directConnect directConnect indicator
     */
    public void setDirectConnect(boolean directConnect) {
        if (!isDirectConnectAvailable() || isDirectConnectEnabled() == directConnect
                || !dataManager.getDriver().isPresent()) {
            return;
        }

        prefManager.setDirectConnectModeEnabled(dataManager.getDriver().get(), directConnect);
        subscriptions.add(getAvailableCarTypes(false).take(1)
                .subscribe(strings -> {}, Timber::e));
    }

    /**
     * Save selected car categories. Not allowed to save NULL or empty set.
     * Will save intersection of available & selected cat categories.
     * Will update stream of {@link #getSelectedCarTypes(boolean)} appropriately
     * <p/>
     * @param categories categories to save
     */
    public void saveSelectedCategories(Set<String> categories) {
        if (categories == null || !dataManager.getDriver().isPresent()) {
            return;
        }
        categories = filterSelectedCategoriesForDriver(dataManager.getDriver().get(), categories);
        prefManager.setCurrentRideRequestType(dataManager.getDriver().get(), categories);
        subscriptions.add(getCarTypesForCategories(categories)
                .subscribe(selectedCategoriesSubject::onNext, Timber::e));
    }

    /**
     * Update available/selected categories based on event type. Show notification with currently
     * available/selected types.
     * If categories changed by admin, find the difference in available categories.
     * If categories changed due to missed requests, check selected categories on server.
     * <p/>
     * @param event server event
     */
    public void postCarCategoriesEvent(Event event) {
        CategoryChangeParams params = event.deserializeParams(CategoryChangeParams.class);
        if (params != null) {
            if (params.isAdminEdit()) {
                doOnChangedByAdmin();
            } else if (params.isMissedRequest()) {
                if (params.getDisabled() != null && !params.getDisabled().isEmpty()) {
                    // added in RA-13658
                    doOnMissedRequests(params.getDisabled());
                } else {
                    // however, keep this as well to be safe
                    doOnMissedRequests();
                }
            }
        }
    }

    public void postDriverTypeEvent(Event event) {
        subscriptions.add(dataManager.loadDriver()
                .flatMap(driver -> getAvailableCarTypes(true).take(1))
                .subscribe(strings -> {}, Timber::e));
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void reset() {
        cityId = 0;
        carTypesSubject.onNext(null);
        driverTypesSubject.onNext(null);
        availableCategoriesSubject.onNext(null);
        selectedCategoriesSubject.onNext(null);
    }

    public boolean isWomenOnly() {
        return isWomenOnlyAvailable() && isWomenOnlyEnabled();
    }

    public boolean isWomenOnlyAvailable() {
        return dataManager.getDriver().map(Driver::isFemaleDriver).orElse(false);
    }

    public boolean isWomenOnlyEnabled() {
        return dataManager.getDriver().map(prefManager::isWomanOnlyModeEnabled).orElse(false);
    }

    public boolean isDirectConnect() {
        return isDirectConnectAvailable() && isDirectConnectEnabled();
    }

    public boolean isDirectConnectAvailable() {
        return dataManager.getDriver().map(Driver::isDirectConnectDriver).orElse(false);
    }

    public boolean isDirectConnectEnabled() {
        return dataManager.getDriver().map(prefManager::isDirectConnectModeEnabled).orElse(false);
    }

    private void doOnChangedByAdmin() {
        Observable<Pair<List<RequestedCarType>, List<RequestedCarType>>> localAndRemote =
                Observable.zip(getAllAvailableCarTypes(false),
                        getAllAvailableCarTypes(true),
                        Pair::new);

        subscriptions.add(localAndRemote
                .retryWhen(new RetryWhenNoNetwork(5000))
                .take(1)
                .subscribe(pair -> doOnAvailableChangedByAdmin(pair.first, pair.second),
                        throwable -> doOnAvailableChangedByAdmin()));
    }

    private void doOnMissedRequests() {
        Observable<Pair<List<RequestedCarType>, List<RequestedCarType>>> localAndRemote =
                Observable.zip(readLocalSelectedCarTypes(),
                    loadSelectedCarTypes(true), // this will sync selected categories with server
                    Pair::new);

        subscriptions.add(localAndRemote
                .retryWhen(new RetryWhenNoNetwork(5000))
                .take(1)
                .subscribe(pair -> doOnSelectedChangedDueToMissedRequests(pair.first, pair.second),
                        throwable -> doOnSelectedChangedDueToMissedRequests()));
    }

    private void doOnMissedRequests(Set<String> disabledCategories) {
        Observable<Pair<List<RequestedCarType>, List<RequestedCarType>>> selectedAndDisabled =
                Observable.zip(readLocalSelectedCarTypes(),
                        getCarTypesForCategories(disabledCategories),
                        Pair::new);

        subscriptions.add(selectedAndDisabled.subscribe(pair -> {
            List<RequestedCarType> left = new ArrayList<>(pair.first);
            left.removeAll(pair.second);
            saveSelectedCategories(getCarCategoriesForTypes(left));
            if (!left.isEmpty()) {
                showMissedRideRequestsMessage(pair.second);
            } else {
                // otherwise will receive GO_OFFLINE and show another message
                // however, server sometimes sucks, so send driver offline to be sure
                stateManager.switchState(stateManager.createOfflinePoolingState());
            }
        }));
    }

    private void doOnGlobalConfig(GlobalConfig config) {
        boolean cityChanged = !config.getCurrentCity().getCityId().equals(cityId);
        cityId = config.getCurrentCity().getCityId();
        boolean needUpdate = updateCarTypes(config, cityChanged)
                || updateDriverTypes(config, cityChanged);
        if (needUpdate) {
            subscriptions.add(getAvailableCarTypes(false).take(1)
                    .subscribe(strings -> {}, Timber::e));
        }
    }

    private boolean updateCarTypes(GlobalConfig config, boolean cityChanged) {
        if (config.getCarTypes() != null && !config.getCarTypes().isEmpty()) {
            return applyValues(carTypesSubject, config.getCarTypes());
        } else if (cityChanged) {
            // reset to initiate reload
            return applyValues(carTypesSubject, null);
        }
        return false;
    }

    private boolean updateDriverTypes(GlobalConfig config, boolean cityChanged) {
        if (config.getDriverTypes() != null && !config.getDriverTypes().isEmpty()) {
            return applyValues(driverTypesSubject, config.getDriverTypes());
        } else if (cityChanged) {
            // reset to initiate reload
            return applyValues(driverTypesSubject, null);
        }
        return false;
    }

    private Observable<List<RequestedCarType>> loadCarTypes() {
        return dataManager.loadCarTypes(cityId).doOnNext(carTypesSubject::onNext);
    }

    private Observable<List<RequestedDriverType>> loadDriverTypes() {
        return dataManager.loadDriverTypes(cityId).doOnNext(driverTypesSubject::onNext);
    }

    private Observable<List<RequestedCarType>> loadSelectedCarTypes(boolean fromServer) {
        if (fromServer) {
            return dataManager.loadActiveDriver()
                    .map(ActiveDriver::getCarCategories)
                    .doOnNext(this::saveSelectedCategories)
                    .flatMap(this::getCarTypesForCategories)
                    .onErrorResumeNext(throwable -> readLocalSelectedCarTypes());
        } else {
            return readLocalSelectedCarTypes();
        }
    }

    private Observable<List<RequestedCarType>> readLocalSelectedCarTypes() {
        return dataManager.getDriverObservable().take(1)
                .map(this::getSelectedCategoriesForDriver)
                .flatMap(this::getCarTypesForCategories);

    }

    private <T> boolean applyValues(BehaviorSubject<List<T>> subject, List<T> values) {
        List<T> current = subject.getValue();
        if (current == null || !current.equals(values)) {
            subject.onNext(values);
            return true;
        }
        return false;
    }

    private Observable<List<RequestedCarType>> getAllAvailableCarTypes(boolean fromServer) {
        return (fromServer ? dataManager.loadDriver() : dataManager.getDriverObservable().take(1))
                .map(Driver::getSelectedCar)
                .map(Optional::get)
                .map(Car::getCarCategories)
                .flatMap(this::getCarTypesForCategories)
                .onErrorReturn(throwable -> new ArrayList<>());
    }

    private Observable<List<RequestedCarType>> getCustomAvailableCarTypes(String customType, boolean fromServer) {
        return getCustomTypeCarTypes(customType)
                .zipWith(getAllAvailableCarTypes(fromServer), Pair::new)
                .map(pair -> {
                    // intersection of women only categories
                    // and available driver categories
                    pair.first.retainAll(pair.second);
                    return pair.first;
                });
    }

    private Observable<List<RequestedCarType>> getCarTypesForCategories(Set<String> categories) {
        return getCarTypes().take(1)
                .flatMapIterable(list -> list)
                .filter(carType -> categories.contains(carType.getCarCategory()))
                .toList();
    }

    private Set<String> getCarCategoriesForTypes(List<RequestedCarType> carTypes) {
        Set<String> categories = new LinkedHashSet<>();
        for (RequestedCarType carType : carTypes) {
            categories.add(carType.getCarCategory());
        }
        return categories;
    }

    private Set<String> getSelectedCategoriesForDriver(Driver driver) {
        Set<String> cached = prefManager.getCurrentRideRequestType(driver);
        if (cached == null) {
            cached = new LinkedHashSet<>();
        }
        if (!driver.getSelectedCar().isPresent()) {
            // this should never happen
            Exception e = new Exception(UNEXPECTED_STATE_KEY);
            String message = "No selected car: " + Arrays.toString(driver.getCars().toArray());
            Timber.e(e, message);
            return cached;
        }
        return filterSelectedCategoriesForDriver(driver, cached);
    }

    private Set<String> filterSelectedCategoriesForDriver(Driver driver, Set<String> categories) {
        Car selectedCar = driver.getSelectedCar().get();
        if (!categories.isEmpty()) {
            categories.retainAll(selectedCar.getCarCategories());
        }
        if (categories.isEmpty()) {
            categories.addAll(selectedCar.getCarCategories());
        }
        return categories;
    }

    private void doOnAvailableChangedByAdmin(List<RequestedCarType> local,
                                             List<RequestedCarType> remote) {
        availableCategoriesSubject.onNext(remote);

        List<RequestedCarType> added = new ArrayList<>(remote);
        added.removeAll(local);

        List<RequestedCarType> removed = new ArrayList<>(local);
        removed.removeAll(remote);

        String title = context.getString(R.string.ride_request_by_admin_title);
        StringBuilder sb = new StringBuilder();
        if (!added.isEmpty()) {
            sb.append(context.getString(R.string.ride_request_added,
                    RideRequestSummaryData.carTypesToString(added)));
        }
        if (!removed.isEmpty()) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(context.getString(R.string.ride_request_removed,
                    RideRequestSummaryData.carTypesToString(removed)));
        }
        if (sb.length() > 0) {
            if (!added.isEmpty()) {
                sb.append(context.getString(R.string.ride_request_go_to_enable));
            }
            notificationManager.showMessage(title, sb.toString(), false);
        }

        // previously selected category may have been disabled
        // update selected categories based on recently loaded driver
        saveSelectedCategories(getSelectedCategories());
    }

    private void doOnAvailableChangedByAdmin() {
        String title = context.getString(R.string.ride_request_by_admin_title);
        String message = context.getString(R.string.ride_request_by_admin_message);
        notificationManager.showMessage(title, message, false);
    }

    private void doOnSelectedChangedDueToMissedRequests(List<RequestedCarType> local,
                                                        List<RequestedCarType> remote) {
        if (!remote.isEmpty()) {
            List<RequestedCarType> disabled = new ArrayList<>(local);
            disabled.removeAll(remote);
            if (!disabled.isEmpty()) {
                showMissedRideRequestsMessage(disabled);
            }
        } else {
            // otherwise will receive GO_OFFLINE and show another message
            // however, server sometimes sucks, so send driver offline to be sure
            stateManager.switchState(stateManager.createOfflinePoolingState());
        }
    }

    private void doOnSelectedChangedDueToMissedRequests() {
        String title = context.getString(R.string.ride_request_missed);
        String message = context.getString(R.string.ride_request_missed_message);
        notificationManager.showMessage(title, message, false);
    }

    private void showMissedRideRequestsMessage(List<RequestedCarType> disabled) {
        String title = context.getString(R.string.ride_request_missed_title);
        int stringRes = disabled.size() == 1
                ? R.string.ride_request_missed
                : R.string.ride_request_missed_plural;
        String message = context.getString(stringRes,
                RideRequestSummaryData.carTypesToString(disabled));
        notificationManager.showMessage(title, message, false);
    }
}
