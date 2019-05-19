package com.rideaustin.api;

import android.content.Context;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.maps.android.PolyUtil;
import com.rideaustin.App;
import com.rideaustin.BuildConfig;
import com.rideaustin.CurrentAvatarType;
import com.rideaustin.api.config.ConfigAppInfoResponse;
import com.rideaustin.api.model.AvatarType;
import com.rideaustin.api.model.DirectionResponse;
import com.rideaustin.api.model.Event;
import com.rideaustin.api.service.AuthService;
import com.rideaustin.api.service.BaseDriverService;
import com.rideaustin.api.service.ConfigService;
import com.rideaustin.api.service.DirectionService;
import com.rideaustin.api.service.EventsService;
import com.rideaustin.api.service.SupportService;
import com.rideaustin.api.service.TokenDeviceType;
import com.rideaustin.base.retrofit.NullOnEmptyConverterFactory;
import com.rideaustin.base.retrofit.RetrofitException;
import com.rideaustin.base.retrofit.RxErrorHandlingCallAdapterFactory;
import com.rideaustin.models.VehicleManager;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.drawer.triphistory.SupportTopicsModel;
import com.rideaustin.ui.drawer.triphistory.forms.SupportFormsModel;
import com.rideaustin.utils.CommonConstants;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.DeviceInfoUtil;
import com.rideaustin.utils.LocationHintHelper;
import com.rideaustin.utils.file.logging.Logger;
import com.rideaustin.utils.gradle.BuildConfigProxy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import timber.log.Timber;

import static com.rideaustin.utils.CommonConstants.NETWORK_TIMEOUT_IN_SECONDS;
import static com.rideaustin.utils.CommonConstants.TOKEN_KEY;
import static com.rideaustin.utils.CommonConstants.X_TOKEN_KEY;

/**
 * @author shumelchyk
 */
public abstract class CommonDataManager {

    public static final int LONG_POOLING_TIMEOUT_IN_SECONDS = 60;
    private static final long HTTP_RESPONSE_DISK_CACHE_MAX_SIZE = 10 * 1024 * 1024L;
    private final PublishSubject<RetrofitException> httpErrorSubject;
    private final PublishSubject<RetrofitException> networkErrorSubject;
    protected final BehaviorSubject<ConfigAppInfoResponse> appInfoSubject = BehaviorSubject.create();

    private List<OkHttpClient> clients = new ArrayList<>();

    private BasicAuthProvider authProvider;
    private SupportService supportService;
    private ConfigService configService;
    private AuthService authService;
    private BaseDriverService baseDriverService;
    private VehicleManager vehicleManager;
    private EventsService eventsService;
    private SupportTopicsModel supportTopicsModel;
    private SupportFormsModel supportFormsModel;
    private DirectionService directionService;
    private LocationHintHelper locationHintHelper;
    protected BehaviorSubject<Boolean> emailVerifiedSubject = BehaviorSubject.create(true);

    private static IdlingRegisterInterface idlingInterface;
    private static Interceptor mockInterceptor;


    public CommonDataManager() {
        authProvider = new SimpleAuthProvider();
        httpErrorSubject = PublishSubject.create();
        networkErrorSubject = PublishSubject.create();
        eventsService = createApi(EventsService.class, BuildConfigProxy.getApiEndpoint(), true, LONG_POOLING_TIMEOUT_IN_SECONDS, false);
        authService = createApi(AuthService.class, BuildConfigProxy.getApiEndpoint(), true, true);
        supportService = createApi(SupportService.class, BuildConfigProxy.getApiEndpoint(), true, true);
        configService = createApi(ConfigService.class, BuildConfigProxy.getApiEndpoint(), true, true);
        baseDriverService = createApi(BaseDriverService.class, BuildConfigProxy.getApiEndpoint(), true, true);
        directionService = createApi(DirectionService.class, BuildConfig.DIRECTION_ENDPOINT, false, true);
        locationHintHelper = new LocationHintHelper();
    }

    public static void setIdlingInterface(IdlingRegisterInterface iInterface) {
        idlingInterface = iInterface;
    }

    public static void setMockInterceptor(Interceptor interceptor) {
        mockInterceptor = interceptor;
    }

    public void dispose() {
        cancelAllRequests();
    }

    protected <T> T createApi(Class<T> clazz, String endpoint, boolean isRequestInterceptor, long timeout, boolean isIdlingResource) {
        ApiRequestInterceptor headerInterceptor = isRequestInterceptor ? new ApiRequestInterceptor(authProvider) : new ApiRequestInterceptor(null);

        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(endpoint)
                .client(getOkHttpClient(App.getInstance(), headerInterceptor, clazz.getSimpleName(), timeout, isIdlingResource))
                .addConverterFactory(new NullOnEmptyConverterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create(httpErrorSubject, networkErrorSubject))
                .build();

        return restAdapter.create(clazz);
    }

    protected <T> T createApi(Class<T> clazz, String endpoint, boolean isRequestInterceptor, boolean isIdlingResource) {
        return createApi(clazz, endpoint, isRequestInterceptor, NETWORK_TIMEOUT_IN_SECONDS, isIdlingResource);
    }

    private OkHttpClient getOkHttpClient(Context context, ApiRequestInterceptor headerInterceptor, String tag, long timeout, boolean isIdlingResource) {
        OkHttpClient.Builder okClientBuilder = new OkHttpClient.Builder();
        okClientBuilder.addInterceptor(headerInterceptor);
        if (mockInterceptor != null) {
            okClientBuilder.addInterceptor(mockInterceptor);
        }
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new Logger(tag));
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        okClientBuilder.addInterceptor(httpLoggingInterceptor);
        final File baseDir = context.getCacheDir();
        if (baseDir != null) {
            final File cacheDir = new File(baseDir, "HttpResponseCache");
            okClientBuilder.cache(new Cache(cacheDir, HTTP_RESPONSE_DISK_CACHE_MAX_SIZE));
        }

        okClientBuilder.readTimeout(timeout, TimeUnit.SECONDS);
        okClientBuilder.writeTimeout(timeout, TimeUnit.SECONDS);
        okClientBuilder.connectTimeout(timeout, TimeUnit.SECONDS);

        OkHttpClient client = okClientBuilder.build();
        if (isIdlingResource) {
            if (idlingInterface != null) {
                idlingInterface.registerClient(client, tag);
            }
        }
        clients.add(client);
        return client;
    }

    public void setXAuth(String auth) {
        App.getPrefs().putString(X_TOKEN_KEY, auth);
        App.getPrefs().clearValue(TOKEN_KEY);
    }

    public String getXToken() {
        return App.getPrefs().getString(X_TOKEN_KEY, "");
    }

    public void updateCredentials(String username, String password) {
        authProvider.updateCredentials(username, password);
    }

    public void clearAuth() {
        authProvider.updateToken("");
        setXAuth("");
    }

    public void cancelAllRequests() {
        for (OkHttpClient client : clients) {
            client.dispatcher().cancelAll();
        }
    }

    public Observable<RetrofitException> getHttpError() {
        return httpErrorSubject.asObservable();
    }

    public Observable<RetrofitException> getNetworkError() {
        return networkErrorSubject.asObservable();
    }

    public SupportService getSupportService() {
        return supportService;
    }

    public ConfigService getConfigService() {
        return configService;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public BaseDriverService getBaseDriverService() {
        return baseDriverService;
    }

    public Observable<Object> registerGCMToken(final AvatarType avatarType) {
        final String token = FirebaseInstanceId.getInstance().getToken();
        Timber.d("::registerGCMToken:: " + token);
        if (token == null) {
            Exception e = new Exception("::registerGCMToken::");
            Timber.e(e, "Device token is null, failed to register for GCM");
            return Observable.just(null);
        }
        return sendTokenToServer(token, avatarType);
    }

    public Observable<Object> sendTokenToServer(final String token, final AvatarType avatarType) {
        String uniqueDeviceId = DeviceInfoUtil.getUniqueDeviceId();
        return getAuthService()
                .registerGCMToken(token, TokenDeviceType.GOOGLE.name(), uniqueDeviceId, avatarType.name())
                .subscribeOn(RxSchedulers.network());
    }

    public static String toParam(String name, String fileName) {
        return String.format("%s\"; filename=\"%s\"", name, fileName);
    }

    public VehicleManager getVehicleManager() {
        return vehicleManager;
    }

    public void setVehicleManager(VehicleManager vehicleManager) {
        this.vehicleManager = vehicleManager;
    }

    public EventsService getEventsService() {
        return eventsService;
    }

    public SupportTopicsModel getSupportTopicsModel() {
        return supportTopicsModel;
    }

    public void setSupportTopicsModel(SupportTopicsModel supportTopicsModel) {
        if (this.supportTopicsModel != null) {
            this.supportTopicsModel.destroy();
        }
        this.supportTopicsModel = supportTopicsModel;
    }

    public SupportFormsModel getSupportFormsModel() {
        return supportFormsModel;
    }

    public void setSupportFormsModel(SupportFormsModel supportFormsModel) {
        if (this.supportFormsModel != null) {
            this.supportFormsModel.destroy();
        }
        this.supportFormsModel = supportFormsModel;
    }

    public final boolean isAuthorised() {
        return !App.getPrefs().getString(Constants.TOKEN_KEY, "").isEmpty() || !TextUtils.isEmpty(App.getDataManager().getXToken());
    }

    public Observable<ConfigAppInfoResponse> fetchAppInfoConfig() {
        return getConfigService().getConfigAppInfo(CurrentAvatarType.getAvatarType().name(), Constants.PLATFORM_TYPE)
                .doOnNext(this::doOnConfig)
                .subscribeOn(RxSchedulers.network());
    }

    private void doOnConfig(ConfigAppInfoResponse config) {
        App.getPrefs().setConfig(config);
        appInfoSubject.onNext(config);
    }

    /**
     * @return Stream
     */
    public Observable<ConfigAppInfoResponse> getAppInfoObservable() {
        return appInfoSubject.switchIfEmpty(fetchAppInfoConfig());
    }

    public interface IdlingRegisterInterface {
        void registerClient(OkHttpClient client, String tag);
    }

    public Observable<List<LatLng>> getDirection(LatLng start, LatLng end) {
        return directionService.getDirection(start.latitude + "," + start.longitude,
                end.latitude + "," + end.longitude,
                BuildConfig.GOOGLE_DIRECTION_API_KEY)
                .flatMap(response -> {
                    if (!Constants.GOOGLE_DIRECTIONS_STATUS_OK.equals(response.getStatus())) {
                        return Observable.error(new IllegalStateException(response.getStatus()));
                    }
                    return Observable.just(parseDirectionResponse(response));
                })
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.computation());
    }

    public Observable<List<LatLng>> getDirectionWithWayPoints(LatLng start, LatLng end, LatLng... waypoints) {
        String origin = start.latitude + "," + start.longitude;
        String destination = end.latitude + "," + end.longitude;
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (LatLng point : waypoints) {
            if (!first) {
                sb.append('|');
            }
            sb.append("via:");
            sb.append(point.latitude);
            sb.append(',');
            sb.append(point.longitude);
            first = false;
        }
        return directionService.getDirectionWithWaypoints(origin, destination, sb.toString(),
                BuildConfig.GOOGLE_DIRECTION_API_KEY)
                .flatMap(response -> {
                    if (!Constants.GOOGLE_DIRECTIONS_STATUS_OK.equals(response.getStatus())) {
                        return Observable.error(new IllegalStateException(response.getStatus()));
                    }
                    return Observable.just(parseDirectionResponse(response));
                })
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.computation());
    }


    private List<LatLng> parseDirectionResponse(DirectionResponse response) {
        List<LatLng> result = new ArrayList<>();
        DirectionResponse.Routes route = response.getRoutes().get(0);
        for (DirectionResponse.Routes.Legs leg : route.getLegs()) {
            for (DirectionResponse.Routes.Legs.Steps step : leg.getSteps()) {
                result.addAll(PolyUtil.decode(step.getPolylines().getPoitns()));
            }
        }
        return result;
    }

    public LocationHintHelper getLocationHintHelper() {
        return locationHintHelper;
    }

    public void postEmailVerified(boolean isVerified) {
        emailVerifiedSubject.onNext(isVerified);
    }

    public boolean isEmailVerified() {
        return emailVerifiedSubject.getValue();
    }

    public Observable<Boolean> getEmailVerifiedObservable() {
        return emailVerifiedSubject
                .serialize()
                .onBackpressureBuffer()
                .asObservable();
    }

    public Observable<Event> getEvents() {
        final Long lastEventId = App.getPrefs().getLong(CommonConstants.LAST_EVENT_ID,
                CommonConstants.NOT_EXISTING_LAST_EVENT_ID);
        final Observable<List<Event>> events;
        if (lastEventId == CommonConstants.NOT_EXISTING_LAST_EVENT_ID) {
            events = App.getDataManager().getEventsService()
                    .getEvents(CurrentAvatarType.getAvatarType().avatarType);
        } else {
            events = App.getDataManager().getEventsService()
                    .getEvents(CurrentAvatarType.getAvatarType().avatarType, lastEventId);
        }
        return events.subscribeOn(RxSchedulers.eventPolling())
                .flatMap(list -> {
                    Timber.i(":::events::: %s", list);
                    if (list.size() == 0) {
                        return Observable.empty();
                    } else {
                        App.getPrefs().putLong(CommonConstants.LAST_EVENT_ID,
                                list.get(list.size() - 1).getId());
                        return Observable.from(list);
                    }
                });
    }

}
