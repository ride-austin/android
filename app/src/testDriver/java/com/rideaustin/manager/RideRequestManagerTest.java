package com.rideaustin.manager;

import android.content.Context;

import com.rideaustin.api.DataManager;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.model.CategoryChangeParams;
import com.rideaustin.api.model.Event;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.api.model.driver.ActiveDriver;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.api.model.driver.RequestedDriverType;
import com.rideaustin.engine.StateManager;
import com.rideaustin.utils.SerializationHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import common.test.BaseTest;
import java8.util.Optional;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.BehaviorSubject;

import static com.rideaustin.utils.Constants.CAR_TYPE_DELIMITER;
import static com.rideaustin.utils.Constants.WOMEN_ONLY_DRIVER_TYPE;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author sdelaysam.
 */

@RunWith(RobolectricTestRunner.class)
public class RideRequestManagerTest extends BaseTest {

    private static String CATEGORY_1 = "Category1";
    private static String CATEGORY_2 = "Category2";
    private static String CATEGORY_3 = "Category3";
    private static String CATEGORY_4 = "Category4";
    private static String CATEGORY_5 = "Category5";

    private static List<RequestedCarType> ALL_CAR_TYPES
            = makeCarTypes(CATEGORY_1, CATEGORY_2, CATEGORY_3, CATEGORY_4, CATEGORY_5);

    private static Set<String> DRIVER_CAR_CATEGORIES =
            makeCategories(CATEGORY_1, CATEGORY_2, CATEGORY_3, CATEGORY_4);
    private static List<RequestedCarType> DRIVER_CAR_TYPES =
            makeCarTypes(CATEGORY_1, CATEGORY_2, CATEGORY_3, CATEGORY_4);

    private static Set<String> ACTIVE_DRIVER_CATEGORIES =
            makeCategories(CATEGORY_1, CATEGORY_3);
    private static List<RequestedCarType> ACTIVE_DRIVER_CAR_TYPES =
            makeCarTypes(CATEGORY_1, CATEGORY_3);

    private static Set<String> REGULAR_DRIVER_CATEGORIES
            = makeCategories(CATEGORY_1, CATEGORY_4);
    private static List<RequestedCarType> REGULAR_DRIVER_CAR_TYPES =
            makeCarTypes(CATEGORY_1, CATEGORY_4);

    private static Set<String> CACHED_CATEGORIES
            = makeCategories(CATEGORY_2, CATEGORY_4, CATEGORY_5);

    private static Set<String> WOMEN_ONLY_CATEGORIES
            = makeCategories(CATEGORY_2, CATEGORY_5);


    @Mock Context context;
    @Mock ConfigurationManager configurationManager;
    @Mock StateManager stateManager;
    @Mock DataManager dataManager;
    @Mock PrefManager prefManager;
    @Mock AppNotificationManager notificationManager;

    private BehaviorSubject<GlobalConfig> configSubject = BehaviorSubject.create();
    private BehaviorSubject<Driver> localDriver = BehaviorSubject.create();

    private Observable<List<RequestedCarType>> remoteCarTypes = Observable.just(ALL_CAR_TYPES);
    private Observable<List<RequestedDriverType>> remoteDriverTypes = Observable.empty();
    private Observable<ActiveDriver> remoteActiveDriver = Observable.empty();
    private Observable<Driver> remoteDriver = Observable.empty();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        doReturn(configSubject).when(configurationManager).getConfigurationUpdates();
        doAnswer(invocation -> remoteCarTypes).when(dataManager).loadCarTypes(anyInt());
        doAnswer(invocation -> remoteDriverTypes).when(dataManager).loadDriverTypes(anyInt());
        doAnswer(invocation -> remoteActiveDriver).when(dataManager).loadActiveDriver();
        doAnswer(invocation -> remoteDriver.doOnNext(localDriver::onNext)).when(dataManager).loadDriver();
        doReturn(localDriver).when(dataManager).getDriverObservable();
        doAnswer(invocation -> Optional.ofNullable(localDriver.getValue())).when(dataManager).getDriver();
        doCallRealMethod().when(stateManager).createOfflinePoolingState();
    }

    @Test
    public void shouldRequestCarAndDriverTypesWhenNoConfig() {
        RideRequestManager manager = new RideRequestManager(context, configurationManager,
                dataManager, stateManager, prefManager, notificationManager);
        TestSubscriber<List<RequestedCarType>> carTypesSubscriber = TestSubscriber.create();
        TestSubscriber<List<RequestedDriverType>> driverTypesSubscriber = TestSubscriber.create();

        Throwable error = new Throwable("");
        remoteDriverTypes = Observable.error(error);

        manager.getCarTypes().subscribe(carTypesSubscriber);
        manager.getDriverTypes().subscribe(driverTypesSubscriber);

        verify(dataManager, times(1)).loadCarTypes(0);
        verify(dataManager, times(1)).loadDriverTypes(0);

        carTypesSubscriber.assertNotCompleted();
        driverTypesSubscriber.assertNotCompleted();

        carTypesSubscriber.assertValueCount(1);
        assertEquals(5, carTypesSubscriber.getOnNextEvents().get(0).size());

        driverTypesSubscriber.assertError(error);
        driverTypesSubscriber.assertUnsubscribed();
    }

    @Test
    public void shouldTakeWomenOnlyAvailableCategories() {
        RideRequestManager manager = new RideRequestManager(context, configurationManager,
                dataManager, stateManager, prefManager, notificationManager);

        doReturn(true).when(prefManager).isWomanOnlyModeEnabled(any());
        localDriver.onNext(createDriver(DRIVER_CAR_CATEGORIES, true));
        remoteDriverTypes = Observable.just(createDriverTypes(true));

        TestSubscriber<List<RequestedCarType>> availableSubscriber = TestSubscriber.create();
        manager.getAvailableCarTypes(false).subscribe(availableSubscriber);

        verify(dataManager, never()).loadDriver();

        availableSubscriber.assertNoErrors();
        availableSubscriber.assertValueCount(1);

        // should be intersection of WOMEN_ONLY_CATEGORIES and DRIVER_CAR_CATEGORIES
        List<RequestedCarType> types = availableSubscriber.getOnNextEvents().get(0);
        assertEquals(1, types.size());
        assertEquals(CATEGORY_2, types.get(0).getCarCategory());
    }

    @Test
    public void shouldTakeEmptySetIfWomenOnlyMisconfigured() {
        RideRequestManager manager = new RideRequestManager(context, configurationManager,
                dataManager, stateManager, prefManager, notificationManager);

        doReturn(true).when(prefManager).isWomanOnlyModeEnabled(any());
        localDriver.onNext(createDriver(DRIVER_CAR_CATEGORIES, true));
        remoteDriverTypes = Observable.just(createDriverTypes(false));

        TestSubscriber<List<RequestedCarType>> availableSubscriber = TestSubscriber.create();
        manager.getAvailableCarTypes(false).subscribe(availableSubscriber);

        verify(dataManager, never()).loadDriver();

        availableSubscriber.assertNoErrors();
        availableSubscriber.assertValueCount(1);

        List<RequestedCarType> types = availableSubscriber.getOnNextEvents().get(0);
        assertEquals(0, types.size());
    }

    @Test
    public void shouldTakeAvailableCategoriesFromDriver() {
        RideRequestManager manager = new RideRequestManager(context, configurationManager,
                dataManager, stateManager, prefManager, notificationManager);

        TestSubscriber<List<RequestedCarType>> availableSubscriber = TestSubscriber.create();
        manager.getAvailableCarTypes(false).subscribe(availableSubscriber);

        verify(dataManager, never()).loadDriver();

        availableSubscriber.assertNoValues();
        availableSubscriber.assertNoErrors();

        localDriver.onNext(createDriver(DRIVER_CAR_CATEGORIES, false));

        availableSubscriber.assertNoErrors();
        availableSubscriber.assertValueCount(1);

        List<RequestedCarType> types = availableSubscriber.getOnNextEvents().get(0);
        assertEquals(DRIVER_CAR_TYPES, types);
    }

    @Test
    public void shouldShowNoAvailableCategoriesOnDriverFault() {
        RideRequestManager manager = new RideRequestManager(context, configurationManager,
                dataManager, stateManager, prefManager, notificationManager);

        TestSubscriber<List<RequestedCarType>> availableSubscriber = TestSubscriber.create();
        manager.getAvailableCarTypes(false).subscribe(availableSubscriber);

        verify(dataManager, never()).loadDriver();

        availableSubscriber.assertNoValues();
        availableSubscriber.assertNoErrors();

        Throwable error = new Throwable("");
        localDriver.onError(error);

        availableSubscriber.assertValueCount(1);
        assertTrue(availableSubscriber.getOnNextEvents().get(0).isEmpty());
    }

    @Test
    public void shouldReRequestAvailableCategoriesFromServer() {
        RideRequestManager manager = new RideRequestManager(context, configurationManager,
                dataManager, stateManager, prefManager, notificationManager);
        localDriver.onNext(createDriver(DRIVER_CAR_CATEGORIES, false));
        remoteDriver = Observable.just(createDriver(new LinkedHashSet<>(
                Collections.singletonList(CATEGORY_5)), false));

        TestSubscriber<List<RequestedCarType>> availableSubscriber = TestSubscriber.create();
        manager.getAvailableCarTypes(true).subscribe(availableSubscriber);

        verify(dataManager, times(1)).loadDriver();

        availableSubscriber.assertNoErrors();
        availableSubscriber.assertValueCount(1);

        List<RequestedCarType> types = availableSubscriber.getOnNextEvents().get(0);
        assertEquals(1, types.size());
        assertEquals(CATEGORY_5, types.get(0).getCarCategory());
    }

    @Test
    public void shouldUseSelectedCategoriesFromDriver() {
        RideRequestManager manager = new RideRequestManager(context, configurationManager,
                dataManager, stateManager, prefManager, notificationManager);
        doReturn(null).when(prefManager).getCurrentRideRequestType(any());
        localDriver.onNext(createDriver(DRIVER_CAR_CATEGORIES, false));
        remoteActiveDriver = Observable.error(new Throwable("Offline"));

        TestSubscriber<List<RequestedCarType>> selectedSubscriber = TestSubscriber.create();
        manager.getSelectedCarTypes(false).subscribe(selectedSubscriber);

        verify(dataManager, times(1)).loadActiveDriver();
        verify(dataManager, times(1)).getDriverObservable();
        verify(prefManager, times(1)).getCurrentRideRequestType(any());
        selectedSubscriber.assertNoErrors();
        selectedSubscriber.assertNotCompleted();
        selectedSubscriber.assertValueCount(1);

        List<RequestedCarType> types = selectedSubscriber.getOnNextEvents().get(0);
        assertEquals(DRIVER_CAR_TYPES, types);
    }

    @Test
    public void shouldUseValidCachedSelectedCategories() {
        RideRequestManager manager = new RideRequestManager(context, configurationManager,
                dataManager, stateManager, prefManager, notificationManager);
        doReturn(CACHED_CATEGORIES).when(prefManager).getCurrentRideRequestType(any());
        localDriver.onNext(createDriver(DRIVER_CAR_CATEGORIES, false));
        remoteActiveDriver = Observable.error(new Throwable("Offline"));

        TestSubscriber<List<RequestedCarType>> selectedSubscriber = TestSubscriber.create();
        manager.getSelectedCarTypes(false).subscribe(selectedSubscriber);

        verify(dataManager, times(1)).loadActiveDriver();
        verify(dataManager, times(1)).getDriverObservable();
        verify(prefManager, times(1)).getCurrentRideRequestType(any());

        selectedSubscriber.assertNoErrors();
        selectedSubscriber.assertNotCompleted();
        selectedSubscriber.assertValueCount(1);

        List<RequestedCarType> types = selectedSubscriber.getOnNextEvents().get(0);
        assertEquals(2, types.size());
        assertEquals(CATEGORY_2, types.get(0).getCarCategory());
        assertEquals(CATEGORY_4, types.get(1).getCarCategory());
    }

    @Test
    public void shouldRequestSelectedCategoriesFromServer() {
        RideRequestManager manager = new RideRequestManager(context, configurationManager,
                dataManager, stateManager, prefManager, notificationManager);
        doReturn(CACHED_CATEGORIES).when(prefManager).getCurrentRideRequestType(any());
        localDriver.onNext(createDriver(DRIVER_CAR_CATEGORIES, false));
        remoteActiveDriver = Observable.just(createActiveDriver());

        TestSubscriber<List<RequestedCarType>> selectedSubscriber = TestSubscriber.create();
        manager.getSelectedCarTypes(true).subscribe(selectedSubscriber);

        verify(dataManager, never()).getDriverObservable();
        verify(prefManager, never()).getCurrentRideRequestType(any());
        verify(dataManager, times(1)).loadActiveDriver();
        verify(prefManager, times(1)).setCurrentRideRequestType(localDriver.getValue(),
                ACTIVE_DRIVER_CATEGORIES);

        selectedSubscriber.assertValueCount(1);
        List<RequestedCarType> types = selectedSubscriber.getOnNextEvents().get(0);
        assertEquals(ACTIVE_DRIVER_CAR_TYPES, types);
    }

    @Test
    public void shouldSaveCategories() {
        RideRequestManager manager = new RideRequestManager(context, configurationManager,
                dataManager, stateManager, prefManager, notificationManager);
        localDriver.onNext(createDriver(DRIVER_CAR_CATEGORIES, false));
        remoteActiveDriver = Observable.error(new Throwable("Offline"));

        TestSubscriber<List<RequestedCarType>> selectedSubscriber = TestSubscriber.create();
        manager.getSelectedCarTypes(false).subscribe(selectedSubscriber);

        selectedSubscriber.assertValueCount(1);
        assertEquals(DRIVER_CAR_CATEGORIES.size(), selectedSubscriber.getOnNextEvents().get(0).size());

        manager.saveSelectedCategories(null);
        verify(prefManager, never()).setCurrentRideRequestType(any(), any());
        selectedSubscriber.assertValueCount(1);

        manager.saveSelectedCategories(new LinkedHashSet<>());
        verify(prefManager, times(1)).setCurrentRideRequestType(any(), any());
        selectedSubscriber.assertValueCount(1); // not invoked, categories not changed

        manager.saveSelectedCategories(CACHED_CATEGORIES);
        verify(prefManager, times(1)).setCurrentRideRequestType(localDriver.getValue(), CACHED_CATEGORIES);

        selectedSubscriber.assertValueCount(2);
        List<RequestedCarType> types = selectedSubscriber.getOnNextEvents().get(1);
        assertEquals(2, types.size());
        assertEquals(CATEGORY_2, types.get(0).getCarCategory());
        assertEquals(CATEGORY_4, types.get(1).getCarCategory());
    }

    @Test
    public void shouldSelectChangedCategoriesAfterAdminChanged() {
        RideRequestManager manager = new RideRequestManager(context, configurationManager,
                dataManager, stateManager, prefManager, notificationManager);
        localDriver.onNext(createDriver(makeCategories(CATEGORY_1, CATEGORY_2), false));
        remoteDriver = Observable.just(createDriver(makeCategories(CATEGORY_2, CATEGORY_3), false));
        remoteActiveDriver = Observable.error(new Throwable("Offline"));
        doReturn(makeCategories(CATEGORY_1)).when(prefManager).getCurrentRideRequestType(any());

        TestSubscriber<List<RequestedCarType>> selectedSubscriber = TestSubscriber.create();
        manager.getSelectedCarTypes(false).subscribe(selectedSubscriber);
        selectedSubscriber.assertValueCount(1);
        List<RequestedCarType> types = selectedSubscriber.getOnNextEvents().get(0);
        assertEquals(1, types.size());
        assertEquals(CATEGORY_1, types.get(0).getCarCategory());

        manager.postCarCategoriesEvent(createAdminEditEvent());
        verify(dataManager, times(1)).loadDriver();
        verify(prefManager, times(1)).setCurrentRideRequestType(any(), any());

        selectedSubscriber.assertValueCount(2);
        types = selectedSubscriber.getOnNextEvents().get(1);
        assertEquals(2, types.size());
        assertEquals(CATEGORY_2, types.get(0).getCarCategory());
        assertEquals(CATEGORY_3, types.get(1).getCarCategory());
    }

    @Test
    public void shouldUpdateAvailableCategoriesOnAdminEvent() {
        RideRequestManager manager = new RideRequestManager(context, configurationManager,
                dataManager, stateManager, prefManager, notificationManager);
        localDriver.onNext(createDriver(DRIVER_CAR_CATEGORIES, false));
        remoteDriver = Observable.just(createDriver(REGULAR_DRIVER_CATEGORIES, false));

        TestSubscriber<List<RequestedCarType>> availableSubscriber = TestSubscriber.create();
        manager.getAvailableCarTypes(false).subscribe(availableSubscriber);

        verify(dataManager, never()).loadDriver();
        availableSubscriber.assertValueCount(1);
        assertEquals(DRIVER_CAR_TYPES, availableSubscriber.getOnNextEvents().get(0));

        manager.postCarCategoriesEvent(createAdminEditEvent());

        verify(dataManager, times(1)).loadDriver();
        availableSubscriber.assertValueCount(2);
        assertEquals(REGULAR_DRIVER_CAR_TYPES, availableSubscriber.getOnNextEvents().get(1));
    }

    @Test
    public void shouldUpdateSelectedCategoriesOnMissedRequest() {
        RideRequestManager manager = new RideRequestManager(context, configurationManager,
                dataManager, stateManager, prefManager, notificationManager);

        localDriver.onNext(createDriver(DRIVER_CAR_CATEGORIES, false));
        doReturn(CACHED_CATEGORIES).when(prefManager).getCurrentRideRequestType(any());
        remoteActiveDriver = Observable.just(createActiveDriver());

        TestSubscriber<List<RequestedCarType>> selectedSubscriber = TestSubscriber.create();
        manager.getSelectedCarTypes(false).subscribe(selectedSubscriber);

        verify(dataManager, times(1)).loadActiveDriver();
        selectedSubscriber.assertValueCount(1);

        List<RequestedCarType> types = selectedSubscriber.getOnNextEvents().get(0);
        assertEquals(2, types.size());
        assertEquals(CATEGORY_1, types.get(0).getCarCategory());
        assertEquals(CATEGORY_3, types.get(1).getCarCategory());

        manager.saveSelectedCategories(CACHED_CATEGORIES);
        verify(prefManager, times(1)).setCurrentRideRequestType(localDriver.getValue(), CACHED_CATEGORIES);

        selectedSubscriber.assertValueCount(2);
        types = selectedSubscriber.getOnNextEvents().get(1);
        assertEquals(2, types.size());
        assertEquals(CATEGORY_2, types.get(0).getCarCategory());
        assertEquals(CATEGORY_4, types.get(1).getCarCategory());

        manager.postCarCategoriesEvent(createMissedRequestEvent());

        verify(dataManager, times(2)).loadActiveDriver();
        selectedSubscriber.assertValueCount(3);
        assertEquals(ACTIVE_DRIVER_CAR_TYPES, selectedSubscriber.getOnNextEvents().get(2));
    }

    private Driver createDriver(Set<String> categories, boolean isWomenOnly) {
        Driver driver = new Driver();
        driver.setEmail("localDriver@email.com");
        List<Car> cars = new ArrayList<>();
        Car selectedCar = new Car();
        selectedCar.setSelected(true);
        selectedCar.setRemoved(false);
        selectedCar.setCarCategories(categories);
        cars.add(selectedCar);
        driver.setCars(cars);
        if (isWomenOnly) {
            driver.setGrantedDriverTypes(Collections.singletonList(WOMEN_ONLY_DRIVER_TYPE));
        }
        return driver;
    }

    private ActiveDriver createActiveDriver() {
        ActiveDriver activeDriver = new ActiveDriver();
        activeDriver.setCarCategories(ACTIVE_DRIVER_CATEGORIES);
        activeDriver.setDriver(createDriver(DRIVER_CAR_CATEGORIES, false));
        return activeDriver;
    }

    private List<RequestedDriverType> createDriverTypes(boolean hasWomenOnly) {
        RequestedDriverType type1 = new RequestedDriverType();
        type1.setName("REGULAR");
        type1.setAvailableInCategories(REGULAR_DRIVER_CATEGORIES);

        RequestedDriverType type2 = new RequestedDriverType();
        if (hasWomenOnly) {
            type2.setName(WOMEN_ONLY_DRIVER_TYPE);
            type2.setAvailableInCategories(WOMEN_ONLY_CATEGORIES);
        }

        return Arrays.asList(type1, type2);
    }

    private Event createAdminEditEvent() {
        Event event = new Event();
        event.setEventType(RideStatus.CAR_CATEGORY_CHANGE);
        event.setParameters(SerializationHelper.serialize(CategoryChangeParams.adminEdit()));
        return event;
    }

    private Event createMissedRequestEvent() {
        Event event = new Event();
        event.setEventType(RideStatus.CAR_CATEGORY_CHANGE);
        event.setParameters(SerializationHelper.serialize(CategoryChangeParams.missedRequest()));
        return event;
    }

    private static Set<String> makeCategories(String ...categories) {
        return new LinkedHashSet<>(Arrays.asList(categories));
    }

    private static List<RequestedCarType> makeCarTypes(String ... categories) {
        List<RequestedCarType> carTypes = new ArrayList<>();
        for (String category : categories) {
            carTypes.add(new RequestedCarType(category, category));
        }
        return carTypes;
    }

    private String makeCarString(String ... categories) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String category : categories) {
            if (!first) {
                sb.append(CAR_TYPE_DELIMITER);
            }
            sb.append(category);
            first = false;
        }
        return sb.toString();
    }
}
