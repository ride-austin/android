package com.rideaustin.ui.signup.driver;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.rideaustin.ui.signup.driver.fcra_disclosure.FcraDisclosureFragment;
import com.rideaustin.ui.signup.driver.tos.TermsAndConditionsFragment;
import com.rideaustin.utils.Constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.rideaustin.ui.signup.driver.SetupVehicleListFragment.TYPE_COLOR;
import static com.rideaustin.ui.signup.driver.SetupVehicleListFragment.TYPE_KEY;
import static com.rideaustin.ui.signup.driver.SetupVehicleListFragment.TYPE_MAKE;
import static com.rideaustin.ui.signup.driver.SetupVehicleListFragment.TYPE_MODEL;
import static com.rideaustin.ui.signup.driver.SetupVehicleListFragment.TYPE_YEAR;

/**
 * Created by rost on 8/10/16.
 */
public class DriverSignUpFlowRouter {

    private final static String CURRENT_STATE_INDEX_KEY = "currentStateIndexKey";

    private final DriverSignUpInteractor driverSignUpInteractor;

    private final List<State> order =
            Collections.unmodifiableList(
                    Arrays.asList(
                            new State(DriverPhotoFragment.class, 0),
                            new State(DriverLicenseFragment.class, 1),
                            new State(DriverTNCCardFragment.class, 2, DriverTNCCardFragment.TYPE_KEY, Constants.TNCCardSide.FRONT),
                            new State(DriverTNCCardFragment.class, 3, DriverTNCCardFragment.TYPE_KEY, Constants.TNCCardSide.BACK),
                            new State(DriverCarPhotoFragment.class, 4, DriverCarPhotoFragment.PHOTO_TYPE_KEY, Constants.CarPhotoType.FRONT),
                            new State(DriverCarPhotoFragment.class, 5, DriverCarPhotoFragment.PHOTO_TYPE_KEY, Constants.CarPhotoType.BACK),
                            new State(DriverCarPhotoFragment.class, 6, DriverCarPhotoFragment.PHOTO_TYPE_KEY, Constants.CarPhotoType.INSIDE),
                            new State(DriverCarPhotoFragment.class, 7, DriverCarPhotoFragment.PHOTO_TYPE_KEY, Constants.CarPhotoType.TRUNK),
                            new State(DriverVehicleInformationFragment.class, 8),
                            new State(SetupVehicleListFragment.class, 9, TYPE_KEY, TYPE_YEAR),
                            new State(SetupVehicleListFragment.class, 10, TYPE_KEY, TYPE_MAKE),
                            new State(SetupVehicleListFragment.class, 11, TYPE_KEY, TYPE_MODEL),
                            new State(SetupVehicleListFragment.class, 12, TYPE_KEY, TYPE_COLOR),
                            new State(DriverTNCStickerFragment.class, 13),
                            new State(LicensePlateFragment.class, 14),
                            new State(DriverVehicleInformationSummaryFragment.class, 15),
                            new State(DriverInsuranceFragment.class, 16),
                            new State(FcraDisclosureFragment.class, 17),
                            new State(FCRACheckInvestigationFragment.class, 18),
                            new State(FCRACheckAuthorizationFragment.class, 19),
                            new State(TermsAndConditionsFragment.class, 20)
                    )
            );

    private int currentStateIndex = 0;

    public DriverSignUpFlowRouter(Bundle savedInstanceState, final DriverSignUpInteractor driverSignUpInteractor) {
        this.driverSignUpInteractor = driverSignUpInteractor;
        if (savedInstanceState != null) {
            currentStateIndex = savedInstanceState.getInt(CURRENT_STATE_INDEX_KEY, 0);
        } else {
            currentStateIndex = 0;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_STATE_INDEX_KEY, currentStateIndex);
    }

    public State getInitialState() {
        return order.get(0);
    }

    public State getCurrentState() {
        return order.get(currentStateIndex);
    }

    public boolean hasNextState() {
        return currentStateIndex < order.size() - 1;
    }

    public boolean hasPrevState() {
        return currentStateIndex > 0;
    }

    public State moveToNextState() {
        if (!hasNextState()) {
            throw new IllegalStateException("CurrentState(" + getCurrentState() + ") is last. Can't move to next state");
        }

        currentStateIndex++;
        boolean isSkipped;
        do {
            isSkipped = false;
            final State currentState = getCurrentState();
            if (currentState.clazz.isAssignableFrom(DriverTNCCardFragment.class)) {
                final String subType = currentState.args.getString(DriverTNCCardFragment.TYPE_KEY);
                if (driverSignUpInteractor.shouldSkipTNCCardStep(subType)) {
                    currentStateIndex++;
                    isSkipped = true;
                }
            } else if (currentState.clazz.isAssignableFrom(DriverTNCStickerFragment.class)) {
                if (driverSignUpInteractor.shouldSkipTNCStickerStep()) {
                    currentStateIndex++;
                    isSkipped = true;
                }
            }
        } while (isSkipped);

        return getCurrentState();
    }

    public void moveToPrevState(FragmentManager fragmentManager) {
        if (!hasPrevState()) {
            throw new IllegalStateException("CurrentState(" + getCurrentState() + ") is first. Can't move to previous state");
        }
        fragmentManager.popBackStackImmediate();
        int count = fragmentManager.getBackStackEntryCount();
        if (count > 0) {
            String tag = fragmentManager.getBackStackEntryAt(count - 1).getName();
            currentStateIndex = State.findPosition(fragmentManager.findFragmentByTag(tag));
        } else {
            currentStateIndex = 0;
        }
    }

    public static final class State {

        private static final String KEY_POSITION = "State::position";

        private final Class<? extends BaseDriverSignUpFragment> clazz;

        private final int position;

        private final Bundle args;

        public State(Class<? extends BaseDriverSignUpFragment> clazz, int position, String ...params) {
            this.clazz = clazz;
            this.position = position;
            this.args = new Bundle();
            args.putInt(KEY_POSITION, position);
            if (params.length > 0) {
                if (params.length % 2 != 0) {
                    throw new IllegalArgumentException("Params are key-value pairs, so count should be divided by 2. Actual count=" + params.length);
                }
                for (int i = 0; i < params.length; i = i + 2) {
                    args.putString(params[i], params[i + 1]);
                }
            }
        }

        public BaseDriverSignUpFragment createFragment() {
            try {
                BaseDriverSignUpFragment fragment = clazz.newInstance();
                fragment.setArguments(args);
                return fragment;
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException("Can't create fragment of class " + clazz, e);
            }
        }

        public String getTag() {
            return "State" + position;
        }

        public int getPosition() {
            return position;
        }

        public static int findPosition(@NonNull Fragment fragment) {
            if (fragment.getArguments() == null || !fragment.getArguments().containsKey(KEY_POSITION)) {
                throw new IllegalArgumentException("Can't get position out of fragment arguments: " + fragment.getArguments());
            }
            return fragment.getArguments().getInt(KEY_POSITION);
        }
    }
}
