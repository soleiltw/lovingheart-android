package com.lovingheart.app.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.lovingheart.app.R;

/**
 * Created by edward_chiang on 2014/3/9.
 */
public class TermOfUseDialog extends Dialog {

    private WebView webView;

    private String webUrl;

    public TermOfUseDialog(Context context, boolean cancelable, OnCancelListener cancelListener, String webUrl) {
        super(context, cancelable, cancelListener);
        this.webUrl = webUrl;
        if (context instanceof Activity) {
            setOwnerActivity((Activity)context);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_web_view);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getOwnerActivity() != null) {
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

            String urlIntent = webUrl;
            if (urlIntent!=null && urlIntent.length() > 0) {
                webView.loadUrl(urlIntent);
            }

        }
    }

    private void updateRefreshItem(boolean loading) {

    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }
}
