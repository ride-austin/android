package com.rideaustin.ui.utils.infodialog;

import android.databinding.ObservableField;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;

import com.rideaustin.App;

/**
 * Created by hatak on 07.08.2017.
 */

public class InfoDialogViewModel {

    private final ObservableField<Drawable> icon = new ObservableField<>();
    private final ObservableField<String> title = new ObservableField<>();
    private final ObservableField<String> content = new ObservableField<>();

    public InfoDialogViewModel(@DrawableRes int icon, final String title, final String content) {
        this.title.set(title);
        this.content.set(content);
        this.icon.set(ContextCompat.getDrawable(App.getInstance(), icon));
    }

    public ObservableField<Drawable> getIcon() {
        return icon;
    }

    public ObservableField<String> getTitle() {
        return title;
    }

    public ObservableField<String> getContent() {
        return content;
    }
}
