package com.scanny.scanner.activity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;


import com.scanny.scanner.R;
import com.scanny.scanner.utils.AdsUtils;


public class PrivacyPolicyActivity extends BaseActivity {
    protected WebView web;


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_privacy_policy);


        this.web = (WebView) findViewById(R.id.webView);
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        this.web.loadUrl("https://scanny.flycricket.io/privacy.html");
    }
}
