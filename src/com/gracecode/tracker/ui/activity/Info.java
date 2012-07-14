package com.gracecode.tracker.ui.activity;

import android.os.Bundle;
import android.webkit.WebView;
import com.gracecode.tracker.R;
import com.gracecode.tracker.ui.activity.base.Activity;

public class Info extends Activity {
    private WebView mWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);

        mWebView = (WebView) findViewById(R.id.webview);
    }

    @Override
    public void onStart() {
        super.onStart();
        mWebView.loadUrl("file:///android_asset/about.html");
    }
}
