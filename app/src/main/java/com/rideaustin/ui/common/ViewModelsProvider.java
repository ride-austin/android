package com.rideaustin.ui.common;

import android.support.annotation.MainThread;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hatak on 31.10.2017.
 */

public class ViewModelsProvider {

    private static Map<String, ViewModels> viewToViewModels = new HashMap<>();

    @MainThread
    public static ViewModels of(Class<? extends BaseView> aClass) {
        ViewModels viewModels = viewToViewModels.get(aClass.getName());
        if (viewModels == null) {
            viewModels = new ViewModels();
            viewToViewModels.put(aClass.getName(), viewModels);
        }
        return viewModels;
    }

    @MainThread
    public static void clear(Class<? extends BaseView> aClass) {
        viewToViewModels.remove(aClass.getName());
    }

    public static final class ViewModels {

        private Map<String, RxBaseViewModel> typeToModels = new HashMap<>();

        private ViewModels() {
        }

        @MainThread
        public <T extends RxBaseViewModel> T get(Class<T> aClass) {
            //noinspection unchecked
            T viewModel = (T) typeToModels.get(aClass.getName());
            if (viewModel == null) {
                try {
                    viewModel = aClass.newInstance();
                    typeToModels.put(aClass.getName(), viewModel);
                } catch (Exception e) {
                    throw new RuntimeException("Cant instantiate view model of: " + aClass.getName(), e);
                }
            }
            return viewModel;
        }
    }

}
