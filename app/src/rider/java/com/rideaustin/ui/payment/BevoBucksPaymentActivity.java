package com.rideaustin.ui.payment;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.base.BaseActivity;
import com.rideaustin.databinding.ActivityBevoBucksPaymentBinding;
import com.rideaustin.ui.utils.MaterialDialogCreator;

import java8.util.Optional;

public class BevoBucksPaymentActivity extends BaseActivity {

    public static final String URL_KEY = "url.key";
    public static final String ABOUT_BLANK = "about:blank";
    public static final int UNPAID_CODE = 9999;

    private ActivityBevoBucksPaymentBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bevo_bucks_payment);
        binding.toolbarTitle.setText(R.string.pay_with_bevobucks);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        setupWebView();
        loadUrl();

    }

    private void setupWebView() {
        binding.webview.getSettings().setJavaScriptEnabled(true);
        binding.webview.setWebChromeClient(new WebChromeClient());
        binding.webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                showProgress();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                hideProgress();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                binding.webview.loadUrl(ABOUT_BLANK);
                showErrorDialog();
            }
        });
    }

    private void showErrorDialog() {
        MaterialDialog.Builder simpleConfirmDialog = MaterialDialogCreator.createDialog(getString(R.string.bevo_payment_failed_msg), BevoBucksPaymentActivity.this);
        simpleConfirmDialog.onPositive((dialog, which) -> finish());
        simpleConfirmDialog.build().show();
    }

    private void loadUrl() {
        Optional.ofNullable(getIntent().getStringExtra(URL_KEY))
                .ifPresentOrElse(url -> binding.webview.loadUrl(url), () -> finish());
    }

    @Override
    public void onBackPressed() {
        hideProgress();
        super.onBackPressed();
    }

    @Override
    public void finish() {
        setResult(Activity.RESULT_OK, new Intent());
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.getDataManager().requestUnpaid();
    }
}
