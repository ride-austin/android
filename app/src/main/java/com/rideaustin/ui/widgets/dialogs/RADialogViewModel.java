package com.rideaustin.ui.widgets.dialogs;

import android.databinding.ObservableField;
import android.support.annotation.StringRes;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.View;

import com.rideaustin.App;
import com.rideaustin.ui.common.RxBaseObservable;

import rx.functions.Action0;

/**
 * Created by crossover on 18/06/2017.
 */

/**
 * By default, View Model is `NoAction`. However when setting texts and listeners it changes accordingly.
 */
public class RADialogViewModel extends RxBaseObservable {

    private RADialogView dialogView;

    RADialogActionListener listener;
    private Action0 onDismissListener;
    private SingleActionDialogListener singleActionListener;
    private TwoActionsDialogListener twoActionsListener;

    // General properties
    public final ObservableField<Integer> icon = new ObservableField<>(RADialogIcon.WAITING.drawable);
    public final ObservableField<String> title = new ObservableField<>();
    public final ObservableField<String> content = new ObservableField<>();
    public final ObservableField<RADialogType> dialogType = new ObservableField<>(RADialogType.NO_ACTION);
    public final ObservableField<Boolean> hasExtension = new ObservableField<>(false);

    // Single action
    public final ObservableField<SpannableString> singleAction = new ObservableField<>();

    // Two actions
    public final ObservableField<String> actionPositive = new ObservableField<>();
    public final ObservableField<String> actionNegative = new ObservableField<>();

    RADialogViewModel(RADialogView view, RADialogActionListener listener) {
        this();
        dialogView = view;
        this.listener = listener;
    }

    RADialogViewModel(RADialogView view) {
        this();
        dialogView = view;
    }

    RADialogViewModel() {
        setNoActionListener();
    }


    public void setDialog(RADialogView view) {
        dialogView = view;
    }

    public void setOnDismissListener(Action0 listener) {
        onDismissListener = listener;
    }

    public void setNoActionListener() {
        dialogType.set(RADialogType.NO_ACTION);
    }

    public void setSingleActionListener(SingleActionDialogListener listener) {
        dialogType.set(RADialogType.SINGLE_ACTION);
        singleActionListener = listener;
    }

    public void setTwoActionsListener(TwoActionsDialogListener listener) {
        dialogType.set(RADialogType.TWO_ACTIONS);
        twoActionsListener = listener;
    }

    public void setIcon(RADialogIcon icon) {
        this.icon.set(icon.drawable);
    }

    public void setContent(String content) {
        this.content.set(content);
    }

    public void setContent(@StringRes int content) {
        this.content.set(App.getInstance().getString(content));
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public void setTitle(@StringRes int title) {
        this.title.set(App.getInstance().getString(title));
    }

    public final void onDismissClicked(View view) {
        if (onDismissListener != null) {
            onDismissListener.call();
        }
        dialogView.animatedDismiss();
    }

    public void setSingleActionText(@StringRes int actionText) {
        String text = App.getInstance().getString(actionText);
        setSingleActionText(text);
    }

    public void setSingleActionText(String actionText) {
        dialogType.set(RADialogType.SINGLE_ACTION);
        if (TextUtils.isEmpty(actionText)) {
            singleAction.set(new SpannableString(""));
        } else {
            SpannableString content = new SpannableString(actionText);
            content.setSpan(new UnderlineSpan(), 0, actionText.length(), 0);
            singleAction.set(content);
        }
    }

    public void setTwoActions(RADialog.TwoActions actions) {
        dialogType.set(RADialogType.TWO_ACTIONS);
        actionPositive.set(App.getInstance().getString(actions.positiveString));
        actionNegative.set(App.getInstance().getString(actions.negativeString));
    }

    public final void onSingleActionClicked(View view) {
        if (singleActionListener != null) {
            singleActionListener.onSingleActionClicked();
        }
    }

    public final void onPositiveActionClicked(View view) {
        if (twoActionsListener != null) {
            twoActionsListener.onPositiveActionClicked();
        }
    }

    public final void onNegativeActionClicked(View view) {
        if (twoActionsListener != null) {
            twoActionsListener.onNegativeActionClicked();
        }
    }
}
