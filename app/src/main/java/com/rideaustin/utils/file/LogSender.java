package com.rideaustin.utils.file;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rideaustin.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by vokol on 09.09.2016.
 */
public class LogSender {

    private Activity activity;

    public LogSender(@NonNull Activity activity) {
        this.activity = activity;
    }

    public Intent sendEmail(@Nullable File file, String[] to) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("application/zip");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        if (file != null) {
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        }
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.text_subject));

        return createWhiteListIntent(
                emailIntent, "Send report via",
                Collections.singletonList("com.google.android.gm")
        );
    }

    private Intent createWhiteListIntent(Intent target, String title,
                                         List<String> whitelist) {
        final PackageManager pm = activity.getPackageManager();

        Intent dummy = new Intent(target.getAction());
        dummy.setType(target.getType());
        List<ResolveInfo> resInfo = pm.queryIntentActivities(dummy, 0);

        List<HashMap<String, String>> metaInfo = new ArrayList<>();
        for (ResolveInfo ri : resInfo) {
            if (ri.activityInfo == null || !whitelist.contains(ri.activityInfo.packageName))
                continue;

            HashMap<String, String> info = new HashMap<>();
            info.put("packageName", ri.activityInfo.packageName);
            info.put("className", ri.activityInfo.name);
            info.put("simpleName", String.valueOf(ri.activityInfo.loadLabel(pm)));
            metaInfo.add(info);
        }

        if (metaInfo.isEmpty()) {
            // Force empty chooser by setting a nonexistent target class.
            Intent emptyIntent = (Intent) target.clone();
            emptyIntent.setPackage(activity.getPackageName());
            emptyIntent.setClassName(activity.getPackageName(), "NonExistingActivity");
            return Intent.createChooser(emptyIntent, title);
        }

        // Sort items by display name.
        Collections.sort(metaInfo, (map, map2) -> map.get("simpleName").compareTo(map2.get("simpleName")));

        // create the custom intent list
        List<Intent> targetedIntents = new ArrayList<>();
        for (HashMap<String, String> mi : metaInfo) {
            Intent targetedShareIntent = (Intent) target.clone();
            targetedShareIntent.setPackage(mi.get("packageName"));
            targetedShareIntent.setClassName(mi.get("packageName"), mi.get("className"));
            targetedIntents.add(targetedShareIntent);
        }

        Intent chooserIntent = Intent.createChooser(targetedIntents.get(0), title);
        targetedIntents.remove(0);
        Parcelable[] targetedIntentsParcelable =
                targetedIntents.toArray(new Parcelable[targetedIntents.size()]);
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedIntentsParcelable);
        return chooserIntent;
    }
}
