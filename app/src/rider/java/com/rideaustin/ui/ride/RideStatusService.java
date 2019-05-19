package com.rideaustin.ui.ride;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.RiderLiveLocation;
import com.rideaustin.api.model.AvatarType;
import com.rideaustin.api.model.Ride;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.entities.GeoPosition;
import com.rideaustin.events.RideStatusEvent;
import com.rideaustin.manager.notification.InAppMessage;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.PermissionUtils;
import com.rideaustin.utils.RetryWhenNoNetwork;
import com.rideaustin.utils.location.LocationHelper;

import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

import java8.util.Optional;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static com.rideaustin.api.model.RideStatus.RIDE_REQUEST_ERROR;
import static com.rideaustin.api.model.RideStatus.valueOf;

/**
 * Created by kshumelchyk on 7/18/16.
 */
public class RideStatusService extends Service {

    private static final int SERVICE_ID = 23456;
    private static final int HTTP_UNPAID_RIDE = 402;

    private Subscription initialSubscription = Subscriptions.empty();
    private Subscription locationSubscription = Subscriptions.unsubscribed();
    private Subscription rideStatusSubscription = Subscriptions.empty();
    private Subscription rideStatusCurrentSubscription = Subscriptions.empty();

    private RideStatusProcessor rideStatusProcessor;
    private Location riderLocation;

    @Override
    public void onCreate() {
        super.onCreate();
        rideStatusProcessor = new RideStatusProcessor(App.getDataManager(),
                App.getStateManager(),
                App.getNotificationManager(),
                App.getPrefs());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(SERVICE_ID, App.getNotificationManager()
                    .createForegroundServiceNotification(getString(R.string.foreground_service_in_ride)));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("starting ride service");
        if (isOngoingRide()) {
            // It was previous ride here
            listenRideStatus(App.getPrefs().getRideId());
        } else if (intent != null && intent.hasExtra(Constants.START_ADDRESS)) {
            GeoPosition startAddress = intent.getParcelableExtra(Constants.START_ADDRESS);
            Optional<GeoPosition> destinationAddress = Optional.ofNullable(intent.getParcelableExtra(Constants.DESTINATION_ADDRESS));
            boolean isSurgeAccepted = intent.getBooleanExtra(Constants.SURGE_ACCEPTED, false);
            Optional<String> comments = Optional.ofNullable(intent.getStringExtra(Constants.PICKUP_COMMENTS));
            String carCategory = intent.getStringExtra(Constants.SELECTED_CAR_CATEGORY);
            Optional<String> directConnectId = Optional.ofNullable(intent.getStringExtra(Constants.DIRECT_CONNECT_ID));
            rideStatusProcessor.setDirectConnect(directConnectId.isPresent());

            App.getDataManager().saveComment(comments, startAddress);
            rideStatusCurrentSubscription.unsubscribe();
            initialSubscription = App.getConfigurationManager()
                    .getConfigurationUpdates()
                    .first()
                    .flatMap(globalConfig -> {
                        final Integer cityId = globalConfig.getCurrentCity().getCityId();
                        if (directConnectId.isPresent()) {
                            return App.getDataManager().requestDirectConnect(directConnectId.get(), startAddress, destinationAddress, cityId, comments, carCategory);
                        } else {
                            return App.getDataManager().requestRide(startAddress, destinationAddress, cityId, isSurgeAccepted, comments, carCategory);
                        }
                    })
                    .subscribeOn(RxSchedulers.network())
                    .observeOn(RxSchedulers.main())
                    .subscribe(new ApiSubscriber2<Ride>(false) {
                        @Override
                        public void onHttpError(BaseApiException e) {
                            super.onHttpError(e);
                            if (e.getCode() == HTTP_UNPAID_RIDE) {
                                App.getDataManager().requestUnpaid();
                            }
                        }

                        @Override
                        public void onAnyError(BaseApiException e) {
                            sendRequestFailedEvent(e.getBody());
                            tryToFetchCurrentRide();
                        }

                        @Override
                        public void onNext(Ride response) {
                            if (response != null) {
                                Timber.d("request ride status: " + response.getStatus() + ", ride_id: " + response.getId());
                                final Long id = response.getId();
                                App.getPrefs().setRideId(id);
                                listenRideStatus(id);
                                processRideStatus(response);
                                if (App.getDataManager().hasUnpaid()) {
                                    // probably push notification did not arrive
                                    // need to check manually
                                    App.getDataManager().requestUnpaid();
                                }
                            } else {
                                sendRequestFailedEvent(null);
                                tryToFetchCurrentRide();
                            }
                        }
                    });

        }
        return START_STICKY;
    }

    private static boolean isOngoingRide() {
        return App.getPrefs().hasRideId();
    }

    private void startLocationListening() {
        if (locationSubscription.isUnsubscribed() && PermissionUtils.isLocationPermissionGranted(this)) {
            locationSubscription = App.getLocationManager()
                    .getLocationUpdates()
                    .subscribeOn(RxSchedulers.computation())
                    .observeOn(RxSchedulers.computation())
                    .subscribe(location -> riderLocation = location.getLocation(), throwable -> {});
        }
    }

    private void listenRideStatus(final Long ride) {
        startLocationListening();
        rideStatusSubscription.unsubscribe();
        rideStatusSubscription = Observable.interval(Constants.RIDE_STATUS_TIMER_DELAY, Constants.RIDE_STATUS_TIMER_DELAY, TimeUnit.MILLISECONDS, RxSchedulers.computation())
                .onBackpressureLatest()
                .flatMap(aLong -> {
                    if (shouldSendRiderLocation()) {
                        return App.getDataManager()
                                .checkRideAcceptanceStatus(ride, AvatarType.RIDER.name(), riderLocation.getLatitude(), riderLocation.getLongitude())
                                .subscribeOn(RxSchedulers.network());
                    } else {
                        return App.getDataManager().checkRideAcceptanceStatus(ride, AvatarType.RIDER.name())
                                .subscribeOn(RxSchedulers.network());
                    }

                })
                //RA-12743: observe on main to stop service properly on error
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<Ride>(false) {
                    @Override
                    public void onAnyError(BaseApiException e) {
                        if (e.getCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                            tryToFetchCurrentRide();
                        } else if (e.getCode() != HttpURLConnection.HTTP_UNAUTHORIZED) {
                            listenRideStatus(ride);
                        }
                    }

                    @Override
                    public void onNext(Ride ride) {
                        processRideStatus(ride);
                    }
                });
    }

    private void tryToFetchCurrentRide() {
        rideStatusCurrentSubscription.unsubscribe();
        rideStatusCurrentSubscription = App.getDataManager()
                .getRidesService()
                .getCurrentRide(AvatarType.RIDER.name())
                .subscribeOn(RxSchedulers.network())
                .map(Optional::ofNullable)
                .observeOn(RxSchedulers.main())
                .retryWhen(new RetryWhenNoNetwork(1000))
                .subscribe(new ApiSubscriber2<Optional<Ride>>(false) {
                    @Override
                    public void onNext(Optional<Ride> ride) {
                        ride.map(Ride::getId)
                                .ifPresentOrElse(id -> {
                                    App.getPrefs().setRideId(id);
                                    listenRideStatus(id);
                                }, () -> stopService());
                    }

                    @Override
                    public void onAnyError(BaseApiException e) {
                        stopService();
                    }
                });
    }

    private boolean shouldSendRiderLocation() {
        RiderLiveLocation riderLiveLocation = App.getConfigurationManager().getLastConfiguration().getRiderLiveLocation();
        return riderLiveLocation.isEnabled()
                && LocationHelper.isLocationValid(riderLocation)
                && riderLocation.getAccuracy() < riderLiveLocation.getRequiredAccuracy();
    }

    private void sendRequestFailedEvent(String error) {
        String message = TextUtils.isEmpty(error) ? getString(R.string.error_unknown) : error;
        InAppMessage inAppMessage = new InAppMessage(App.getAppName(), message);
        App.getInstance().getInAppMessageManager().show(inAppMessage);
        App.getStateManager().post(new RideStatusEvent(RIDE_REQUEST_ERROR, error, null));
    }

    private void processRideStatus(Ride response) {
        App.getDataManager().setCurrentRide(response);
        rideStatusProcessor.processRideStatus(response);

        if (response == null) {
            Timber.d("Stopping ride service");
            stopService();
            return;
        }

        String status = response.getStatus();
        Timber.d("Processing ride status, status: " + status);

        switch (valueOf(status)) {
            case NO_AVAILABLE_DRIVER:
            case ADMIN_CANCELLED:
            case DRIVER_CANCELLED:
            case RIDER_CANCELLED:
            case COMPLETED:
                stopService();
                break;
        }
    }

    private void stopService() {
        clearSubscriptions();
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearSubscriptions();
        App.getDataManager().postFemaleModeEditable(true);
        App.getDataManager().setCurrentRide(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
    }

    private void clearSubscriptions() {
        initialSubscription.unsubscribe();
        locationSubscription.unsubscribe();
        rideStatusSubscription.unsubscribe();
        rideStatusCurrentSubscription.unsubscribe();
    }

    public static boolean isRunning() {
        ActivityManager manager = (ActivityManager) App.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (RideStatusService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void startIfNeeded() {
        if (isOngoingRide()) {
            Context context = App.getInstance();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context, RideStatusService.class));
            } else {
                context.startService(new Intent(context, RideStatusService.class));
            }
        }
    }

    public static void stop() {
        App.getInstance().stopService(new Intent(App.getInstance(), RideStatusService.class));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
