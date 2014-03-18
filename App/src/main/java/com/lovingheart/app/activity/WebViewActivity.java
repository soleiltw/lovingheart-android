package com.lovingheart.app.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.google.analytics.tracking.android.EasyTracker;
import com.lovingheart.app.R;

/**
 * Created by edward_chiang on 2014/1/8.
 */
public class WebViewActivity extends ActionBarActivity {

    private WebView webView;

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_web_view);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        webView = (WebView)findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                updateRefreshItem(true);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                updateRefreshItem(false);
            }
        });

        String urlIntent = getIntent().getStringExtra("webUrl");
        if (urlIntent!=null && urlIntent.length() > 0) {
            webView.loadUrl(urlIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.web, menu);
        this.menu = menu;
        return true;
    }

    public void updateRefreshItem(boolean isLoading) {
        if (menu != null) {
            MenuItem refreshItem = menu.findItem(R.id.action_reload);
            if (isLoading) {
                MenuItemCompat.setActionView(refreshItem, R.layout.indeterminate_progress_action);
            } else {
                MenuItemCompat.setActionView(refreshItem, null);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }
}
