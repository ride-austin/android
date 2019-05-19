package com.rideaustin.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.databinding.DialogShareAppListBinding;
import com.rideaustin.ui.model.NavigationAppPreference;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.utils.toast.RAToast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import timber.log.Timber;

import static com.rideaustin.utils.Constants.GOOGLE_PLAY_HTTP_URL;
import static com.rideaustin.utils.Constants.MARKET_DETAILS_ID;


/**
 * Provides support API to share.
 *
 * @author Nazar Ivanchuk
 */
public class NavigatorShareUtils {

    public interface Action {
        void onClick(NavigationAppPreference activityInfo);
    }

    private static NavigateUriType navigateUriType;

    enum SupportedNavigationApp {
        GOOGLE_MAP("com.google.android.apps.maps",
                "google.navigation:q=%s&mode=d",
                "google.navigation:q=%s&mode=d",
                App.getInstance().getString(R.string.navigation_app_google_map)),
        WAZE("com.waze",
                "waze://?ll=%s&navigate=yes",
                "waze://?q=%s&navigate=yes",
                App.getInstance().getString(R.string.navigation_app_waze));

        private String displayName;
        private String packageName;
        private String uriFormatStringForLatLng;
        private String uriFormatStringForPlace;

        SupportedNavigationApp(String packageName, String uriFormatStringForLatLng, String uriFormatStringForPlace, String displayName) {
            this.packageName = packageName;
            this.uriFormatStringForLatLng = uriFormatStringForLatLng;
            this.uriFormatStringForPlace = uriFormatStringForPlace;
            this.displayName = displayName;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getUriFormatStringForLatLng() {
            return uriFormatStringForLatLng;
        }

        public String getUriFormatStringForPlace() {
            return uriFormatStringForPlace;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static SupportedNavigationApp getValueForPackage(String packageName) {
            Timber.d("::getValueForPackage:: Package name: %s", packageName);
            SupportedNavigationApp[] values = SupportedNavigationApp.values();
            for (SupportedNavigationApp value : values) {
                if (value.getPackageName().equalsIgnoreCase(packageName)) {
                    return value;
                }
            }
            return GOOGLE_MAP; //default
        }
    }

    private enum NavigateUriType {
        PLACE, LATLNG;

        private String uriValueString;

        private void setUriValueString(String uriValueString) {
            this.uriValueString = uriValueString;
        }

        private String getUriValueString() {
            return uriValueString;
        }
    }

    private static String[] NAME_OF_APPS = new String[]{SupportedNavigationApp.GOOGLE_MAP.getPackageName(),
            SupportedNavigationApp.WAZE.getPackageName()};
    private static String[] NAME_OF_BLOCKED_APPS = new String[]{};

    public static void share(AppCompatActivity appCompatActivity, LatLng navigateTo, Action action, NavigationAppPreference defaultApp) {
        Timber.d("::share:: LatLng: %s App: %s", navigateTo, defaultApp);
        navigateUriType = NavigateUriType.LATLNG;
        navigateUriType.setUriValueString(String.valueOf(navigateTo.latitude) + "," + String.valueOf(navigateTo.longitude));
        shareWithUrl(appCompatActivity, action, defaultApp);
    }

    public static void share(AppCompatActivity appCompatActivity, String place, Action action, NavigationAppPreference defaultApp) {
        Timber.d("::share:: Place: %s App: %s", place, defaultApp);
        navigateUriType = NavigateUriType.PLACE;
        navigateUriType.setUriValueString(place);
        shareWithUrl(appCompatActivity, action, defaultApp);
    }


    private static void shareWithUrl(AppCompatActivity appCompatActivity, Action action, NavigationAppPreference defaultApp) {
        Timber.d("::shareWithUrl:: Default App: %s", defaultApp);
        Intent shareIntent = new Intent(Intent.ACTION_VIEW);

        List<ResolveInfo> activities = getSharingIntents(appCompatActivity, shareIntent, NAME_OF_APPS, NAME_OF_BLOCKED_APPS);
        boolean useDefault = false;
        if (defaultApp != null && !TextUtils.isEmpty(defaultApp.getLauncherActivity())) {
            for (ResolveInfo info : activities) {
                if (defaultApp.getLauncherActivity().equals(info.activityInfo.name)) {
                    useDefault = true;
                    fillUri(info, shareIntent);
                    startNavigationActivity(info, shareIntent, action, appCompatActivity);
                    break;
                }
            }
        }
        if (!useDefault) {
            if (activities.isEmpty()) {
                showMissingNavigationApplicationDialog(appCompatActivity);
            } else {
                showShareDialog(activities, appCompatActivity, shareIntent, action);
            }
        }
    }

    private static void showMissingNavigationApplicationDialog(final AppCompatActivity appCompatActivity) {
        MaterialDialogCreator.createListDialog(
                appCompatActivity,
                appCompatActivity.getString(R.string.missing_navigation_application_dialog_title),
                appCompatActivity.getString(R.string.missing_navigation_application_dialog_msg),
                new AppStoreAdapter(appCompatActivity));
    }

    private static void openInPlayStore(String selectedAppPackage, Activity activity) {
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_DETAILS_ID + selectedAppPackage)));
        } catch (ActivityNotFoundException e) {
            Timber.e(e);
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_PLAY_HTTP_URL + selectedAppPackage)));
        }
    }

    private static void showShareDialog(List<ResolveInfo> activities,
                                        final AppCompatActivity appCompatActivity, final Intent shareIntent, final Action action) {

        final ShareIntentAdapter adapter = getShareAppsAdapter(activities, appCompatActivity, R.layout.dialog_list_of_share);
        final DialogShareAppListBinding binding = DataBindingUtil.inflate(LayoutInflater.from(appCompatActivity), R.layout.dialog_share_app_list, null, false);
        binding.viewList.setAdapter(adapter);

        MaterialDialogCreator.createShareNavigationDialog(binding.getRoot(), appCompatActivity, (dialog, which) -> {
            int checkedItemPosition = adapter.getCheckedItemPosition();
            Timber.d("::showShareDialog:: Checked item: %d", checkedItemPosition);
            if (checkedItemPosition == -1) {
                RAToast.show(R.string.navigation_app_not_selected, Toast.LENGTH_SHORT);
                return;
            }
            ShareAppItem item = adapter.getItem(checkedItemPosition);
            if (item.resolveInfo != null) {
                fillUri(item.resolveInfo, shareIntent);
                if (binding.setDefaultView.isChecked()) {
                    handleAction(item.resolveInfo, appCompatActivity, action);
                }
                startNavigationActivity(item.resolveInfo, shareIntent, action, appCompatActivity);
            }
            dialog.dismiss();
        });
    }

    private static void handleAction(ResolveInfo info, AppCompatActivity appCompatActivity, Action action) {
        NavigationAppPreference navigationAppPreference = new NavigationAppPreference.Builder(info.activityInfo, appCompatActivity.getPackageManager()).build();
        Timber.d("::Setting default:: %s", navigationAppPreference);
        action.onClick(navigationAppPreference);
    }

    @NonNull
    private static ShareIntentAdapter getShareAppsAdapter(List<ResolveInfo> activities,
                                                          AppCompatActivity appCompatActivity,
                                                          @LayoutRes int res) {
        Set<ShareAppItem> shareAppItemSet = new LinkedHashSet<>();
        //First put all apps that are installed
        for (ResolveInfo activity : activities) {
            shareAppItemSet.add(new ShareAppItem(activity));
        }
        //Now add supported but not installed apps, since we use set, duplicates will be eliminated
        for (SupportedNavigationApp navigationApp : SupportedNavigationApp.values()) {
            shareAppItemSet.add(new ShareAppItem(navigationApp));
        }
        //Convert set to shareAppItemList
        List<ShareAppItem> shareAppItemList = new ArrayList<>(shareAppItemSet);
        return new ShareIntentAdapter(appCompatActivity, res, shareAppItemList);
    }

    public static void showChooseDefaultNavigationApp(final AppCompatActivity appCompatActivity, final Action action) {
        Timber.d("::showChooseDefaultNavigationApp::");
        final Intent shareIntent = new Intent(Intent.ACTION_VIEW);
        final List<ResolveInfo> activities = getSharingIntents(appCompatActivity, shareIntent, NAME_OF_APPS, NAME_OF_BLOCKED_APPS);
        final ShareIntentAdapter adapter = getShareAppsAdapter(activities, appCompatActivity, R.layout.dialog_list_of_share_non_checkable);
        final DialogShareAppListBinding binding = DataBindingUtil.inflate(LayoutInflater.from(appCompatActivity), R.layout.dialog_share_app_list, null, false);
        binding.viewList.setAdapter(adapter);
        binding.setDefaultView.setVisibility(View.GONE);
        binding.preferenceText.setVisibility(View.GONE);

        MaterialDialog dialog = MaterialDialogCreator.createDefaultNavigationAppDialog(binding.getRoot(), appCompatActivity);
        binding.viewList.setOnItemClickListener((parent, view, position, id) -> {
            ShareAppItem item = adapter.getItem(position);
            if (item.resolveInfo != null) {
                handleAction(item.resolveInfo, appCompatActivity, action);
            } else {
                openInPlayStore(item.packageName, appCompatActivity);
            }
            dialog.dismiss();

        });
    }

    private static void fillUri(ResolveInfo info, Intent shareIntent) {
        String packageName = info.activityInfo.packageName;
        Timber.d("Package: %s", packageName);
        SupportedNavigationApp navigationApp = SupportedNavigationApp.getValueForPackage(packageName);
        shareIntent.setData(getUri(navigationApp));
    }

    private static Uri getUri(SupportedNavigationApp navigationApp) {
        Timber.d("::getUri::" + "navigationApp = [" + navigationApp + "]");
        String uriString = "";
        switch (navigateUriType) {

            case PLACE:
                uriString = String.format(Locale.US, navigationApp.getUriFormatStringForPlace(), navigateUriType.getUriValueString());
                break;

            case LATLNG:
                uriString = String.format(Locale.US, navigationApp.getUriFormatStringForLatLng(), navigateUriType.getUriValueString());
                break;
        }
        Timber.d("::getUri:: Uri: %s", uriString);
        return Uri.parse(uriString);
    }

    private static void startNavigationActivity(ResolveInfo info, Intent shareIntent, Action action, AppCompatActivity appCompatActivity) {
        shareIntent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
        //To restart without navigator dialog to 'rebuild route'
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        appCompatActivity.startActivity(shareIntent);
    }

    private static List<ResolveInfo> getSharingIntents(Context context, Intent prototype, String[] possibleChoices,
                                                       String[] forbiddenChoices) {
        Timber.d("::getSharingIntents::" + "possibleChoices = [" + Arrays.toString(possibleChoices) + "], forbiddenChoices = [" + Arrays.toString(forbiddenChoices) + "]");
        List<ResolveInfo> result = new ArrayList<ResolveInfo>();

        Intent dummy = new Intent(prototype.getAction());
        dummy.setType(prototype.getType());
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resInfo = pm.queryIntentActivities(dummy, 0);

        if (!resInfo.isEmpty()) {
            Set<String> blackListedPackagesHashset = new HashSet<>();
            Set<String> listedPackagesHashset = new HashSet<>();

            for (String blackListPackageName : forbiddenChoices)
                blackListedPackagesHashset.add(blackListPackageName.toLowerCase(Locale.US));
            for (String packagesHashset : possibleChoices)
                listedPackagesHashset.add(packagesHashset.toLowerCase(Locale.US));

            for (ResolveInfo resolveInfo : resInfo) {
                if ((resolveInfo.activityInfo == null)
                        || !listedPackagesHashset.contains(resolveInfo.activityInfo.packageName.toLowerCase(Locale.US))
                        || blackListedPackagesHashset.contains(resolveInfo.activityInfo.packageName.toLowerCase(Locale.US)))
                    continue;

                result.add(resolveInfo);
            }
            sortSharingApplications(result, pm);
        }

        return result;
    }

    private static void sortSharingApplications(List<ResolveInfo> resInfo, final PackageManager pm) {
        if (!resInfo.isEmpty()) {
            Collections.sort(resInfo, new Comparator<ResolveInfo>() {
                @Override
                public int compare(ResolveInfo info1, ResolveInfo info2) {
                    return String.valueOf(info1.activityInfo.loadLabel(pm)).compareTo(
                            String.valueOf(info2.activityInfo.loadLabel(pm)));
                }
            });
        }
    }

    private static class ShareAppItem {
        ResolveInfo resolveInfo;
        //Supported but not installed apps
        SupportedNavigationApp supportedNavigationApp;
        String packageName;

        ShareAppItem(ResolveInfo resolveInfo) {
            this.resolveInfo = resolveInfo;
            this.packageName = resolveInfo.activityInfo.packageName;
        }

        ShareAppItem(SupportedNavigationApp supportedNavigationApp) {
            this.supportedNavigationApp = supportedNavigationApp;
            this.packageName = supportedNavigationApp.getPackageName();
        }

        //We use this object in a set so that there will be no duplicates
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ShareAppItem)) return false;

            ShareAppItem that = (ShareAppItem) o;

            return packageName.equals(that.packageName);

        }

        @Override
        public int hashCode() {
            return packageName.hashCode();
        }
    }

    /**
     * Populates share dialog view.
     *
     * @author Nazar Ivanchuk
     */
    private static class ShareIntentAdapter extends ArrayAdapter<ShareAppItem> {
        private Activity activity;
        private List<ShareAppItem> items;
        private int layoutId;
        private LayoutInflater inflater;
        private int checkedPosition = -1;

        private ShareIntentAdapter(Activity context, int layoutId, List<ShareAppItem> items) {
            super(context, layoutId, items);

            inflater = context.getLayoutInflater();
            activity = context;
            this.items = items;
            this.layoutId = layoutId;
        }

        @Override
        public View getView(int pos, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(layoutId, parent, false);
            }
            final ShareAppItem shareAppItem = items.get(pos);
            View view = convertView.findViewById(R.id.share_item_title);
            if (view instanceof Checkable) {
                CheckBox shareItemTitle = (CheckBox) view;
                shareItemTitle.setButtonDrawable(getIcon(pos));
                shareItemTitle.setOnClickListener(shareAppItem.resolveInfo != null ? null : (View.OnClickListener) v -> {
                    ((Checkable) v).setChecked(false);
                    openInPlayStore(shareAppItem.packageName, (AppCompatActivity) activity);
                });
                if (shareAppItem.resolveInfo != null) {
                    shareItemTitle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            int childCount = parent.getChildCount();
                            Timber.d("::onCheckedChanged:: %d", childCount);
                            for (int i = 0; i < childCount; i++) {
                                CompoundButton childAt = (CompoundButton) parent.getChildAt(i).findViewById(R.id.share_item_title);
                                if (childAt != buttonView) {
                                    childAt.setOnCheckedChangeListener(null);
                                    childAt.setChecked(false);
                                    childAt.setOnCheckedChangeListener(this);
                                } else {
                                    checkedPosition = isChecked ? i : -1;
                                }
                            }
                        }
                    });
                }
            } else {
                TextView textView = (TextView) view;
                textView.setCompoundDrawablesWithIntrinsicBounds(getIcon(pos), null, null, null);
            }
            ((TextView) view).setText(getLabel(pos));
            View installView = convertView.findViewById(R.id.view_install);
            installView.setVisibility(shareAppItem.resolveInfo == null ? View.VISIBLE : View.GONE);
            installView.setOnClickListener(v -> {
                openInPlayStore(shareAppItem.packageName, (AppCompatActivity) activity);
            });
            return convertView;
        }

        private String getLabel(int pos) {
            ShareAppItem item = items.get(pos);
            return item.resolveInfo == null ? item.supportedNavigationApp.displayName : item.resolveInfo.activityInfo.applicationInfo
                    .loadLabel(activity.getPackageManager()).toString();
        }

        private Drawable getIcon(int pos) {
            ShareAppItem item = items.get(pos);
            return item.resolveInfo == null ? null : item.resolveInfo.activityInfo.applicationInfo
                    .loadIcon(activity.getPackageManager());
        }

        int getCheckedItemPosition() {
            return checkedPosition;
        }
    }

    private static class AppStoreAdapter extends RecyclerView.Adapter<AppStoreViewHolder> implements AppStoreViewHolder.OnViewHolderClickListener {

        private final SupportedNavigationApp[] values = SupportedNavigationApp.values();
        private final Activity activity;

        AppStoreAdapter(Activity activity) {
            this.activity = activity;
        }

        @Override
        public AppStoreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_list_of_share_non_checkable, parent, false);
            return new AppStoreViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(AppStoreViewHolder holder, int position) {
            holder.tvAppName.setText(values[position].displayName);
        }

        @Override
        public int getItemCount() {
            return values.length;
        }

        @Override
        public void doOnClick(int position) {
            String selectedAppPackage = SupportedNavigationApp.values()[position].packageName;
            Timber.d("::onSelection:: Selected app: %s", selectedAppPackage);
            openInPlayStore(selectedAppPackage, activity);
        }
    }

    private static class AppStoreViewHolder extends RecyclerView.ViewHolder {

        private TextView tvAppName;

        AppStoreViewHolder(View itemView, OnViewHolderClickListener clickListener) {
            super(itemView);
            tvAppName = (TextView) itemView.findViewById(R.id.share_item_title);
            // add more padding on the left, because this item is used both in:
            // * ListView wrapped in custom view with correct paddings
            // * RecyclerView hosted by MaterialDialogs which adds no padding
            int leftPadding = (int) ViewUtils.dpToPixels(16);
            tvAppName.setPadding(leftPadding, tvAppName.getPaddingTop(), tvAppName.getPaddingRight(), tvAppName.getPaddingBottom());
            itemView.setOnClickListener(v -> clickListener.doOnClick(getAdapterPosition()));
        }

        interface OnViewHolderClickListener {
            void doOnClick(int position);
        }
    }
}
