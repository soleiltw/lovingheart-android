package com.lovingheart.app.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;

/**
 * Created by edward_chiang on 2014/3/12.
 */
public class WebDialog extends Dialog {

    private WebView webView;

    private String webUrl;

    public WebDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_web_view);

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

        Log.d(DailyKind.TAG, "Loading: " + webUrl);
        webView.loadUrl(webUrl);
    }

    private void updateRefreshItem(boolean isLoading) {

    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }
}
