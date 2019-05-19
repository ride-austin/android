package com.rideaustin.ui.signin;

import android.app.Dialog;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.rideaustin.R;
import com.rideaustin.databinding.FragmentEndpointBinding;
import com.rideaustin.utils.KeyboardUtil;
import com.rideaustin.utils.gradle.BuildConfigProxy;
import com.rideaustin.utils.toast.RAToast;

import okhttp3.HttpUrl;
import timber.log.Timber;

/**
 * Created by Sergey Petrov on 24/03/2017.
 */

public class EndpointFragment extends DialogFragment{

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        FragmentEndpointBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_endpoint, null, false);
        binding.setViewModel(new ViewModel(this));
        AlertDialog alertDialog = builder.setView(binding.getRoot()).create();
        alertDialog.requestWindowFeature(STYLE_NO_TITLE);
        alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        return builder.create();
    }

    public void show(AppCompatActivity activity) {
        super.show(activity.getSupportFragmentManager(), EndpointFragment.class.getSimpleName());
    }

    public static class ViewModel {

        public final String initialText = BuildConfigProxy.getApiEndpoint();
        public final ObservableField<String> endpoint = new ObservableField<>();
        private final DialogFragment dialog;

        public ViewModel(DialogFragment dialog) {
            this.dialog = dialog;
        }

        public final void onSubmit(android.view.View view) {
            String url = endpoint.get();
            boolean isValid = HttpUrl.parse(url) != null;
            if (!isValid) {
                RAToast.show("Please enter valid url", Toast.LENGTH_SHORT);
                return;
            }
            if (!url.endsWith("/")) {
                url += "/";
            }
            BuildConfigProxy.setCustomEnvironmentEndpoint(url);
            KeyboardUtil.hideKeyBoard(view.getContext(), view);
            try {
                dialog.dismiss();
            } catch (Exception e) {
                Timber.e(e, "Unable to hide dialog");
            }
        }

        @BindingAdapter({"initialText", "viewModel"})
        public static void setInitialText(EditText editText, String initialText, ViewModel viewModel) {
            viewModel.endpoint.set(initialText);
            editText.setText(initialText);
            editText.setSelection(initialText.length());
        }

    }

}
