package com.rideaustin.service;

import android.animation.ValueAnimator;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.engine.EngineState;
import com.rideaustin.manager.AppVisibilityState;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.ImageHelper;
import com.rideaustin.utils.TimeUtils;
import com.rideaustin.utils.ViewUtils;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by ridedriverandroid on 23.08.2016.
 */
public class NavigatorService extends Service {

    private static final int MOVE_ANIMATION_DURATION = 400;                // millis
    private static final int CLOSE_VISIBILITY_ANIMATION_DURATION = 2000;   // millis
    private static final int CONTAINER_SIZE = 80;  //dp
    public static final int OFFSET_PADDING_WIDTH = 0; //dp

    private WindowManager windowManager;
    private View floatView;
    private View closeView;
    private boolean isBackground = false;
    private boolean isButtonEnable = false;

    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    public void onCreate() {
        subscriptions.add(App.getInstance().getStateManager()
                .getEngineStateObservable()
                .observeOn(RxSchedulers.main()).subscribe(state ->
                    setButtonEnable(state.getType() == EngineState.Type.ACCEPTED
                            || state.getType() == EngineState.Type.ARRIVED
                            || state.getType() == EngineState.Type.STARTED)
                ));
        subscriptions.add(App.getInstance()
                .getVisibilityObservable()
                .observeOn(RxSchedulers.main())
                .subscribe(state -> {
                    if (state == AppVisibilityState.BACKGROUND) {
                        isBackground = true;
                        showFloatingNavigation();
                    } else {
                        isBackground = false;
                        hideFloatingNavigation();
                    }
                }));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // this service is not expected to be started,
        // it should only be bind to activity
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        subscriptions.unsubscribe();
        removeFloatingView();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
    }

    private void showFloatingNavigation() {
        if (isButtonEnable) {
            if (floatView == null) {
                createFloatingView();
            }
            floatView.setVisibility(View.VISIBLE);
        }
    }

    private void hideFloatingNavigation() {
        if (floatView != null) {
            floatView.setVisibility(View.GONE);
        }
    }

    private void setButtonEnable(boolean isEnable) {
        this.isButtonEnable = isEnable;
        if (!isEnable) {
            hideFloatingNavigation();
        } else if (isBackground) {
            showFloatingNavigation();
        }
    }

    private void createFloatingView() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // https://developer.android.com/about/versions/oreo/android-8.0-changes.html#cwt
        int windowType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;

        int size = (int) ViewUtils.dpToPixels(CONTAINER_SIZE);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(size,
                size,
                windowType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = (int) ViewUtils.dpToPixels(OFFSET_PADDING_WIDTH);
        params.y = 100;
        params.windowAnimations = android.R.style.Animation_Translucent;
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        floatView = inflater.inflate(R.layout.view_floating_navigator, null);
        ImageView imageView = (ImageView) floatView.findViewById(R.id.float_image);
        imageView.setClickable(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setImageDrawable(ImageHelper.getRoundedBitmapDrawable(this, R.drawable.floating_button));
        imageView.setOnTouchListener(new View.OnTouchListener() {

            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            private long thisTouchTime;
            private long previousTouchTime = 0;
            private long buttonHeldTime;
            private boolean clickHandled = false;
            private long doubleClickInterval = ViewConfiguration.getDoubleTapTimeout();
            private long longHoldTimeout = ViewConfiguration.getLongPressTimeout();

            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        thisTouchTime = TimeUtils.currentTimeMillis();
                        if (thisTouchTime - previousTouchTime <= doubleClickInterval) {
                            // double click detected
                            clickHandled = true;
                            onDoubleClick(v, event);
                        } else {
                            // defer event handling until later
                            clickHandled = false;
                        }
                        previousTouchTime = thisTouchTime;
                        break;
                    case MotionEvent.ACTION_UP:

                        if (!clickHandled) {
                            buttonHeldTime = TimeUtils.currentTimeMillis() - thisTouchTime;
                            if (buttonHeldTime > longHoldTimeout) {
                                clickHandled = true;
                                onLongClick(v, event);
                            } else {
                                Handler myHandler = new Handler() {
                                    public void handleMessage(Message m) {
                                        if (!clickHandled) {
                                            clickHandled = true;
                                            onShortClick(v, event);
                                        }
                                    }
                                };
                                Message m = new Message();
                                myHandler.sendMessageDelayed(m, doubleClickInterval);
                            }

                            alignViewAnimation(params);
                        }

                        break;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatView, params);
                        break;
                }
                return false;
            }
        });
        closeView = floatView.findViewById(R.id.float_close);
        closeView.setClickable(true);
        closeView.setOnClickListener(view -> {
            //Close button for one stage
            setButtonEnable(false);
        });
        if (isSystemAlertPermissionGranted(this)) {
            windowManager.addView(floatView, params);
        }
    }

    private void removeFloatingView() {
        if (floatView != null && floatView.getParent() != null && isSystemAlertPermissionGranted(this)) {
            windowManager.removeViewImmediate(floatView);
        }
    }

    private void onDoubleClick(final View v, final MotionEvent event) {
        openApp(v);
    }

    private void onLongClick(final View v, final MotionEvent event) {
        closeView.setVisibility(View.VISIBLE);
        closeView.postDelayed(() -> {
            if (closeView != null) {
                closeView.setVisibility(View.GONE);
            }
        }, CLOSE_VISIBILITY_ANIMATION_DURATION);
    }

    private void onShortClick(final View v, final MotionEvent event) {
        closeView.setVisibility(View.GONE);
        openApp(v);
    }

    private void openApp(View v) {
        touchViewAnimation(v);

        Intent dialogIntent = new Intent(this, NavigationDrawerActivity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        NavigatorService.this.startActivity(dialogIntent);
    }

    private void touchViewAnimation(final View v) {
        final Animation upAnimation = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_touch);
        v.post(() -> v.startAnimation(upAnimation));
    }

    private void moveAnimation(final View view2animate, int viewX, int endX) {
        ValueAnimator translateLeft = ValueAnimator.ofInt(viewX, endX);
        translateLeft.addUpdateListener(valueAnimator -> {
            int val = (Integer) valueAnimator.getAnimatedValue();
            if (view2animate != null) {
                updateViewLayout(view2animate, val, null, null, null);
            }
        });
        translateLeft.setDuration(MOVE_ANIMATION_DURATION);
        translateLeft.start();
    }

    private void updateViewLayout(View view, Integer x, Integer y, Integer w, Integer h) {
        if (view != null && view.getParent() != null) {
            WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();

            if (x != null) lp.x = x;
            if (y != null) lp.y = y;
            if (w != null && w > 0) lp.width = w;
            if (h != null && h > 0) lp.height = h;
            windowManager.updateViewLayout(view, lp);
        }
    }

    public static boolean isSystemAlertPermissionGranted(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
    }

    private void alignViewAnimation(final WindowManager.LayoutParams params) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        int screenWidth = displaymetrics.widthPixels;
        if (params.x >= screenWidth / 2) {
            moveAnimation(floatView, params.x, screenWidth - floatView.getWidth() - (int) ViewUtils.dpToPixels(OFFSET_PADDING_WIDTH));
        } else {
            moveAnimation(floatView, params.x, (int) ViewUtils.dpToPixels(OFFSET_PADDING_WIDTH));
        }
    }
}