package com.rideaustin.base;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.rideaustin.R;

import java.io.Serializable;

import timber.log.Timber;

/**
 * Created by hatak on 21.08.2017.
 */

public class FragmentHelper {

    private static FragmentTransaction createTransaction(FragmentManager fm, Transition transition) {
        final FragmentTransaction transaction = fm.beginTransaction();
        switch (transition) {
            case FORWARD:
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//                transaction.setCustomAnimations(R.anim.slide_in_right,
//                        R.anim.slide_out_left,
//                        R.anim.slide_in_left,
//                        R.anim.slide_out_right);
                break;
            case BACKWARD:
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
//                transaction.setCustomAnimations(R.anim.slide_in_left,
//                        R.anim.slide_out_right,
//                        R.anim.slide_in_right,
//                        R.anim.slide_out_left);
                break;
            case FORWARD_SIMPLE:
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//                transaction.setCustomAnimations(R.anim.slide_in_right_simple,
//                        R.anim.slide_out_left_simple,
//                        android.R.anim.slide_in_left,
//                        android.R.anim.slide_out_right);
                break;
            case BACKWARD_SIMPLE:
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
//                transaction.setCustomAnimations(android.R.anim.slide_in_left,
//                        android.R.anim.slide_out_right,
//                        R.anim.slide_in_right_simple,
//                        R.anim.slide_out_left_simple);
                break;
            case UP:
                transaction.setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_down);
                break;
        }
        return transaction;
    }


    public static boolean isFragmentStateInvalid(FragmentHost host, final Fragment fragment) {
        return fragment == null || host.isInvalid();
    }

    protected static Fragment getFragment(FragmentHost host, int containerId) {
        return host.getSupportFragmentManager().findFragmentById(containerId);
    }

    protected static void clearBackStack(FragmentHost host, String backStackName) {
        if (host.isInvalid()) return;
        try {
            host.getSupportFragmentManager().popBackStack(backStackName, 0);
        } catch (IllegalStateException e) {
            Timber.e(e, e.getMessage());
        }
    }

    public static void showFragment(FragmentHost host, Fragment f, Transition transition) {
        if (isFragmentStateInvalid(host, f)) return;
        host.commitTransaction(createTransaction(host.getSupportFragmentManager(), transition).show(f));
    }

    public static void showFragment(FragmentHost host, Fragment f) {
        showFragment(host, f, Transition.NONE);
    }

    public static void hideFragment(FragmentHost host, Fragment f, Transition transition) {
        if (isFragmentStateInvalid(host, f)) return;
        host.commitTransaction(createTransaction(host.getSupportFragmentManager(), transition).hide(f));
    }

    public static void hideFragment(FragmentHost host, Fragment f) {
        hideFragment(host, f, Transition.NONE);
    }

    public static void removeFragment(FragmentHost host, Fragment f, Transition transition) {
        if (isFragmentStateInvalid(host, f)) return;
        host.commitTransaction(createTransaction(host.getSupportFragmentManager(), transition).remove(f));
    }

    public static void removeFragment(FragmentHost host, Fragment f) {
        removeFragment(host, f, Transition.NONE);
    }

    public static void addFragment(FragmentHost host, Fragment f, int container, boolean addToStack) {
        addFragment(host, f, container, addToStack, Transition.NONE);
    }

    public static void addFragment(FragmentHost host, Fragment f, int container, boolean addToStack, Transition transition) {
        if (isFragmentStateInvalid(host, f)) return;
        FragmentTransaction t = createTransaction(host.getSupportFragmentManager(), transition);
        t.add(container, f);
        if (addToStack) {
            t.addToBackStack(f.getClass().getName());
        }
        host.commitTransaction(t);
    }

    public static void replaceFragment(FragmentHost host, Fragment f, int container, boolean addToStack) {
        replaceFragment(host, f, container, addToStack, f.getClass().getName(), Transition.FORWARD);
    }

    public static void replaceFragment(FragmentHost host, Fragment f, int container, boolean addToStack, Transition transition) {
        replaceFragment(host, f, container, addToStack, f.getClass().getName(), transition);
    }

    public static void replaceFragment(FragmentHost host, Fragment f, int container, boolean addToStack, String backStackName, Transition transition) {
        if (isFragmentStateInvalid(host, f)) return;

        // Fragment transitions have one weird bug: replace shows exit animation over enter
        // Doing remove/add or detach/add either don't work
        // The only possible way is https://stackoverflow.com/a/22986893
        // but it will affect exit fragment lifecycle
        // So its impossible to use parallax effect in this case
        Fragment old = getFragment(host, container);
        if (old != null) {
            transition = transition.toSimple();
        }

        final FragmentTransaction t = createTransaction(host.getSupportFragmentManager(), transition);
        t.replace(container, f, backStackName);
        if (addToStack) {
            t.addToBackStack(backStackName);
        }

        host.commitTransaction(t);
    }

    @Nullable
    public static Fragment findFragmentById(FragmentHost host, @IdRes int id) {
        return host.getSupportFragmentManager().findFragmentById(id);
    }

    @Nullable
    public static Fragment findFragmentByTag(FragmentHost host, @NonNull String tag) {
        return host.getSupportFragmentManager().findFragmentByTag(tag);
    }

    public static Bundle createBundle(@NonNull String key, @NonNull Serializable serializable) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(key, serializable);
        return bundle;
    }

    @Nullable
    public static <T extends Serializable> T fromBundle(@NonNull String key, Fragment fragment) {
        if (fragment.getArguments() != null && fragment.getArguments().containsKey(key)) {
            return (T) fragment.getArguments().getSerializable(key);
        }
        return null;
    }
}
