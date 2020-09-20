package com.arjuj.newsreader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class Main2Activity extends AppCompatActivity {

    public ProgressBar progressBar;
    WebView webView;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition( R.anim.slide_out_right, R.anim.slide_in_right );

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        progressBar=(ProgressBar)findViewById(R.id.pro);

        getWindow().setNavigationBarColor(ContextCompat.getColor(this, android.R.color.white));



        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.txt_layout);

        webView=(WebView)findViewById(R.id.webView);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                setTitle("Loading...");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.INVISIBLE);
                setTitle(view.getTitle());
            }
        });

        Intent intent=getIntent();
        webView.loadUrl(intent.getStringExtra("url"),null);


    }
}
